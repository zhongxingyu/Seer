 package net.coprg.coprg;
 
 import java.awt.Desktop;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.SwingWorker;
 
 import net.coprg.coprg.OAuth.CodeExchangeException;
 import net.coprg.coprg.OAuth.NoRefreshTokenException;
import net.coprg.coprg.Executor;
 
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpResponse;
 import com.google.api.client.http.InputStreamContent;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.gson.GsonFactory;
 import com.google.api.services.drive.Drive;
 import com.google.api.services.drive.Drive.Files;
 import com.google.api.services.drive.model.File;
 import com.google.api.services.drive.model.FileList;
 
 class GDrive {
 
     private static Drive service = null;
     private static List<File> fileList = null;
     private static String errorString = null;
 
     public static interface GDriveListener {
         public void onSuccess();
         public void onFailure();
     }
 
     /**
      * Create a drive service and store it as a static class attribute.
      *
      * @return Drive instance.
      */
     public static Drive getService() {
         if (service == null) {
             Credential cred = OAuth.getStoredCredentials();
             if (cred != null) {
                 service = new Drive.Builder(new NetHttpTransport(),
                         new GsonFactory(),
                         OAuth.getStoredCredentials()).setApplicationName("CoProgramming").build();
             }
         }
         return service;
     }
 
     /**
      * Authorize using the provided authorization code.
      *
      * @param authorizationCode Authorization code to use to retrieve an access
      *        token.
      */
     public static void authorize(final String authorizationCode,
             final GDriveListener listener) {
         new SwingWorker<Boolean, Void>() {
             @Override
             protected Boolean doInBackground() {
                 try {
                     OAuth.getCredentials(authorizationCode);
                     return true;
                 } catch (CodeExchangeException e) {
                     e.printStackTrace();
                 } catch (NoRefreshTokenException e) {
                     e.printStackTrace();
                 }
                 return false;
             }
 
             @Override
             public void done() {
                 try {
                     if (get()) {
                         listener.onSuccess();
                     } else {
                         listener.onFailure();
                     }
                 } catch (InterruptedException | ExecutionException e) {
                     listener.onFailure();
                 }
             }
         }.execute();
     }
 
     public static void logout() {
         try {
             OAuth.deleteCredentials();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static boolean isLoggedIn() {
         return OAuth.getStoredCredentials() != null;
     }
 
     public static void openAuthorizePage() {
         try {
             Desktop.getDesktop().browse(
                     new URI(OAuth.getAuthorizationUrl()));
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static void refreshFileList(final GDriveListener listener) {
         new SwingWorker<List<File>, Void>() {
             @Override
             protected List<File> doInBackground() {
                 try {
                     return fileList = retrieveAllFiles(getService());
                 } catch (IOException e) {
                     e.printStackTrace();
                     return null;
                 }
             }
 
             @Override
             public void done() {
                 try {
                     if (get() != null) {
                         listener.onSuccess();
                     } else {
                         listener.onFailure();
                     }
                 } catch (InterruptedException | ExecutionException e) {
                     listener.onFailure();
                 }
             }
         }.execute();
     }
 
     public static void createFile(final String fileName,
             final GDriveListener listener) {
         new SwingWorker<Boolean, Void>() {
             @Override
             protected Boolean doInBackground() {
                 File body = new File();
                 body.setTitle(fileName + " coprg");
                 body.setMimeType("text/plain");
 
                 InputStream fileContent = GDrive.class.getResourceAsStream(Config.TEMPLATE_LOCATION);
                 InputStreamContent mediaContent = new InputStreamContent("text/plain", fileContent);
                 try {
                     if (getService().files().insert(body,
                                 mediaContent).setConvert(true).execute() != null) {
                         return true;
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 return false;
             }
 
             @Override
             public void done() {
                 try {
                     if (get()) {
                         listener.onSuccess();
                     } else {
                         listener.onFailure();
                     }
                 } catch (InterruptedException | ExecutionException e) {
                     listener.onFailure();
                 }
             }
         }.execute();
     }
 
 
     public static String[] getFileTitles() {
         if (fileList == null || fileList.isEmpty()) {
             return null;
         }
         ArrayList<String> titleArray = new ArrayList<String>();
         for (File f : fileList) {
             titleArray.add(f.getTitle().substring(0,
                         f.getTitle().length()-6));
         }
         String[] result = new String[titleArray.size()];
         return titleArray.toArray(result);
     }
 
     public static void openFilePage(int index) {
         if (fileList == null) {
             refreshFileList(null);
         }
         File f = fileList.get(index);
         if (f != null) {
             try {
                 Desktop.getDesktop().browse(
                         new URI(f.getAlternateLink()));
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public static void compileExecuteFile(int index,
             final GDriveListener listener) {
         if (fileList == null) {
             refreshFileList(null);
         }
         File f = fileList.get(index);
         if (f != null) {
             try {
                 downloadFile(f, new GDriveListener() {
                     @Override
                     public void onSuccess() {
                         compileFile(new GDriveListener() {
                             @Override
                             public void onSuccess() {
                                 executeFile();
                             }
 
                             @Override
                             public void onFailure() {
                                 listener.onFailure();
                             }
                         });
                     }
 
                     @Override
                     public void onFailure() {
                         // TODO Auto-generated method stub
 
                     }
                 });
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public static String getErrorString() {
         return errorString;
     }
 
     private static void downloadFile(final File file,
             final GDriveListener listener) {
         new SwingWorker<InputStream, Void>() {
             @Override
             protected InputStream doInBackground() {
                 String downloadLink = file.getExportLinks().get("text/plain");
                 if (downloadLink != null && downloadLink.length() > 0) {
                     try {
                         HttpResponse resp =
                             getService().getRequestFactory().buildGetRequest(
                                     new GenericUrl(downloadLink))
                             .execute();
                         return resp.getContent();
                     } catch (IOException e) {
                         // An error occurred.
                         e.printStackTrace();
                         return null;
                     }
                 } else {
                     // The file doesn't have any content stored on Drive.
                     return null;
                 }
             }
 
             @Override
             public void done() {
                 try {
                     InputStream in = get();
                     if (in != null) {
                         java.io.File file = new java.io.File("prg.cpp");
                         if (!file.exists()) {
                             file.createNewFile();
                         }
                         FileOutputStream fw = new FileOutputStream(file.getAbsoluteFile());
 
                         int read = 0;
                         byte[] bytes = new byte[1024];
 
                         while ((read = in.read(bytes)) != -1) {
                             fw.write(bytes, 0, read);
                         }
                         fw.flush();
                         fw.close();
                         listener.onSuccess();
                     }
                 } catch (InterruptedException | ExecutionException | IOException e) {
                     e.printStackTrace();
                 }
                 listener.onFailure();
             }
         }.execute();
     }
 
     private static void compileFile(final GDriveListener listener) {
         ProcessStreamHandler psh = null;
         try {
             Process process = new ProcessBuilder("MinGW" + java.io.File.separator
                     + "bin" + java.io.File.separator + "g++.exe", "prg.cpp",
                     "-o", "prg.exe").start();
             psh = new ProcessStreamHandler(process.getErrorStream());
             psh.start();
             if (process.waitFor() == 0) {
                 listener.onSuccess();
                 return;
             }
         } catch (IOException | InterruptedException e) {
             e.printStackTrace();
         }
 
         if (psh != null) {
             errorString = psh.getOutputBuffer().toString();
         }
         listener.onFailure();
     }
 
     private static void executeFile() {
         try {
             new ProcessBuilder("cmd.exe", "/C", "start",
                     "cb_console_runner.exe", "prg.exe").start();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     private static List<File> retrieveAllFiles(Drive service) throws IOException {
         List<File> result = new ArrayList<File>();
         Files.List request = service.files().list().setQ(
                 "title contains 'coprg'");
 
         do {
             try {
                 FileList files = request.execute();
                 result.addAll(files.getItems());
                 request.setPageToken(files.getNextPageToken());
             } catch (IOException e) {
                 System.out.println("An error occurred: " + e);
                 request.setPageToken(null);
             }
         } while (request.getPageToken() != null &&
                 request.getPageToken().length() > 0);
 
         Iterator<File> iterator = result.iterator();
         while (iterator.hasNext()) {
             if (!iterator.next().getTitle().endsWith(" coprg")) {
                 iterator.remove();
             }
         }
         return result;
     }
 }
