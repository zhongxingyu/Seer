 package com.fing.pis.bizativiti.web.api;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 
 class ApiImpl implements Api {
 
     private static final String LOG_SUFFIX = "_log";
     private static final String PROCESSED_SUFFIX = "_processed";
     private static final String UPLOAD_DIRECTORY = "upload";
    private static final String PROCESSED_DIRECTORY = "upload";
     private final File uploadDir;
     private final File processedDir;
     private Map<String, Status> tasks = new HashMap<String, Status>();
 
     public ApiImpl(ServletContext servletContext) {
         File baseFile = new File(servletContext.getInitParameter("server_files_dir"));
         // nos aseguramos que existe el directorio
         baseFile.mkdir();
         uploadDir = new File(baseFile, UPLOAD_DIRECTORY).getAbsoluteFile();
         uploadDir.mkdir();
         processedDir = new File(baseFile, PROCESSED_DIRECTORY).getAbsoluteFile();
         processedDir.mkdir();
     }
 
     private File getUploadFileForTicket(String ticketId) {
         return new File(uploadDir, ticketId);
     }
 
     private File getProcessedFileForTicket(String ticketId) {
         return new File(processedDir, ticketId + PROCESSED_SUFFIX);
     }
 
     private File getLogFileForTicket(String ticketId) {
         return new File(processedDir, ticketId + LOG_SUFFIX);
     }
 
     /**
      * Calculate SHA1 of name
      * 
      * @param name
      * @return
      */
     private String generateTicketId(String name) {
         // calculate SHA1 for name
         MessageDigest md = null;
         try {
             md = MessageDigest.getInstance("SHA-1");
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
         }
         return byteArrayToHexString(md.digest(name.getBytes()));
     }
 
     private static String byteArrayToHexString(byte[] b) {
         String result = "";
         for (int i = 0; i < b.length; i++) {
             result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
         }
         return result;
     }
 
     @Override
     public synchronized String upload(String name, InputStream is) {
         // generamos ticket
         String ticketId = null;
         boolean found = false;
         while (!found) {
             ticketId = generateTicketId(name + System.currentTimeMillis());
             if (!tasks.containsKey(ticketId)) {
                 found = true;
             }
         }
         // subimos archivo
         try {
             File file = getUploadFileForTicket(ticketId);
             FileOutputStream fos = new FileOutputStream(file);
             // copy file
             byte[] buffer = new byte[1024];
             int len;
             while ((len = is.read(buffer)) != -1) {
                 fos.write(buffer, 0, len);
             }
             fos.close();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         // agregamos ticket a lista de tareas
         tasks.put(ticketId, Api.Status.Pending);
         // TODO: encolar la tarea en el worker
         return ticketId;
     }
 
     @Override
     public synchronized Status getStatus(String ticketId) {
         Api.Status status = tasks.get(ticketId);
         return (status == null) ? Api.Status.Unknown : status;
     }
 
     @Override
     public synchronized OutputStream getProcessedFile(String ticketId) throws ApiException {
         Api.Status status = tasks.get(ticketId);
         if (status == null || status != Api.Status.Completed) {
             throw new ApiException();
         }
         // obtener archivo a partir del ticketId
         File file = getProcessedFileForTicket(ticketId);
         try {
             OutputStream os = new FileOutputStream(file);
             return os;
         } catch (FileNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public synchronized OutputStream getLogFile(String ticketId) throws ApiException {
         Api.Status status = tasks.get(ticketId);
         if (status == null || status != Api.Status.Completed && status != Api.Status.Error) {
             throw new ApiException();
         }
         // obtener archivo a partir del ticketId
         File file = getLogFileForTicket(ticketId);
         try {
             OutputStream os = new FileOutputStream(file);
             return os;
         } catch (FileNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
 }
