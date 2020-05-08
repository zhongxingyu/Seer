 package nz.net.paulo;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.json.JSONArray;
 
 /**
  * <p>
  * Licence: <a href="http://opensource.org/licenses/MIT">MIT</a>
  * 
  * @author Martin Paulo
  */
 public class FileUploadHandler extends RequestHandler {
 
     private static final String LAST_MODIFIED_DATE_KEY = "lastModifiedDate";
     private static final String FILENAME_KEY = "filename";
     public FileUploadHandler(RequestParser requestParser) {
         super(requestParser);
     }
 
     /**
      * Extracts file name from the content-disposition string
     * @throws UnsupportedEncodingException 
      */
    private static String extractFileName(String contentDisp) throws UnsupportedEncodingException {
         return getValueFrom(contentDisp, FILENAME_KEY);
     }
 
    private static String getValueFrom(String contentDisp, final String key) throws UnsupportedEncodingException {
         if (contentDisp != null) {
             String[] items = contentDisp.split(";");
             for (String s : items) {
                 if (s.trim().startsWith(key)) {
                     // content disposition's value can be encoded if it has different values...
                    return  URLDecoder.decode(s.substring(s.indexOf("=") + 2, s.length() - 1), "UTF-8");
                 }
             }
         }
         return "";
     }
 
     private static Date extractLastModifiedDate(String contentDisp) {
         Date result = new Date();
         // Format is "Mon, 05 Dec 2011 02:28:34 GMT"
         SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
         try {
            String date = getValueFrom(contentDisp, LAST_MODIFIED_DATE_KEY);
             result = sdf.parse(date);
        } catch (ParseException | UnsupportedEncodingException ex) {
            System.out.println("CD: " + contentDisp);
             // TODO Auto-generated catch block
             ex.printStackTrace();
         }
         return result;
     }
 
     private static long getFileSize(String contentRange) {
         if (contentRange != null) {
             String[] items = contentRange.split("/");
             if (items.length == 2) {
                 return Long.parseLong(items[1]);
             }
         }
         return 0L;
     }
 
     // bytes 5000000-9999999/10925556
     private static long getBytesUpTo(String contentRange) {
         if (contentRange != null) {
             String[] items = contentRange.split("\\D+");
             return Long.parseLong(items[1]);
         }
         return 0;
     }
 
     private static File mergeFiles(File outputFile, File partFile) throws IOException {
         try (FileOutputStream fos = new FileOutputStream(outputFile, true);
                 FileInputStream fis = new FileInputStream(partFile)) {
             IOUtils.copy(fis, fos);
             fos.flush();
             fos.close();
         } finally {
             partFile.delete();
         }
         return outputFile;
     }
 
     private String getTempFilePartExtension() {
         if (isFilePart()) {
             return ".tmp";
         }
         return "";
     }
 
     private boolean isFilePart() {
         return !StringUtils.isEmpty(rp.getContentRange());
     }
 
     /*
      * (non-Javadoc) some grotty code here, whilst we work out how best to handle the differing file stuff...
      * 
      * We will either have an entire file, or a part of a file.
      * 
      * @see nz.net.paulo.RequestHandler#write(javax.servlet.http.HttpServletResponse)
      */
     @Override
     public void write(HttpServletResponse response) throws IOException {
         if (!rp.isMultiPart()) {
             throw new IllegalArgumentException("Request is not multipart, please 'multipart/form-data' enctype for your form.");
         }
         response.setContentType("application/json");
         JSONArray jsonArray = new JSONArray();
         String fileName = extractFileName(rp.getContentDisposition());
         Date lastModifiedDate = extractLastModifiedDate(rp.getContentDisposition());
 
         try {
             // we write all the parts to the temp dir...
             for (Part part : rp.getParts()) {
                 if (StringUtils.isBlank(fileName)) {
                     String partContentDisposition = part.getHeader("content-disposition");
                     fileName = extractFileName(partContentDisposition);
                     lastModifiedDate = extractLastModifiedDate(partContentDisposition);
                 }
                 String filePath = getFullFilePath(fileName);
                 System.out.println("FilePath: " + filePath);
                 part.write(filePath);
                 if (isFilePart()) {
                     File tempFile = new File(filePath);
                     File partFile = new File(ResponseInterface.TEMP_DIR, fileName + getPartExtension());
                     if (partFile.exists()) {
                         if (partFile.lastModified() != lastModifiedDate.getTime()) {
                             partFile.delete();
                             tempFile.delete();
                             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The file appears to have been modified since the last upload. Please try again.");
                             return;
                         }
                         partFile = mergeFiles(partFile, tempFile);
                     } else {
                         tempFile.renameTo(partFile);
                     }
                     partFile.setLastModified(lastModifiedDate.getTime());
                     if (partFile.length() == getFileSize(rp.getContentRange())) {
                         File finalFile = new File(RequestHandler.UPLOAD_DIR, fileName);
                         partFile.renameTo(finalFile);
                     }
                 }
                 File mainFile = new File(RequestHandler.UPLOAD_DIR, fileName);
                 if (mainFile.exists()) {
                     jsonArray.put(getFileJson(mainFile.getName(), mainFile.length()));
                 }
             }
             String responseString = "{\"files\": " + jsonArray.toString() + "}";
             try (PrintWriter writer = response.getWriter()) {
                 writer.write(responseString);
             }
         } catch (IllegalStateException | ServletException e) {
             throw new IOException(e);
         }
 
     }
 
     private String getFullFilePath(String fileName) {
         if (isFilePart()) {
             return ResponseInterface.TEMP_DIR + File.separator + fileName + getTempFilePartExtension();
         }
         return RequestHandler.UPLOAD_DIR + File.separator + fileName + getTempFilePartExtension();
     }
 
 }
