 package ee.webmedia.mikker.upload;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 public class Uploader {
    private static final String BOUNDARY = "----WebKitFormBoundary8NHXoPOgtdmTKB7e";
     
     private String url;
     private String fieldName;
 
     public Uploader(String url, String uploadFieldName) {
         this.url = url;
         this.fieldName = uploadFieldName;
     }
 
     public void upload(String filename, String mime, byte content[]) throws IOException {
         URL url = new URL(this.url);
         HttpURLConnection theUrlConnection = (HttpURLConnection) url.openConnection();
         theUrlConnection.setRequestMethod("POST");
         theUrlConnection.setDoOutput(true);
         theUrlConnection.setDoInput(true);
         theUrlConnection.setUseCaches(false);
 
         theUrlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary="
                 + BOUNDARY);
 
         DataOutputStream httpOut = new DataOutputStream(theUrlConnection.getOutputStream());
 
         String str = "--" + BOUNDARY + "\r\n"
                   + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n"
                    + "Content-Type: " + mime + "\r\n"
                    + "\r\n";
 
         httpOut.write(str.getBytes());
 
         httpOut.write(content);

        httpOut.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());
 
         httpOut.flush();
         httpOut.close();
 
         // read & parse the response
         InputStream is = theUrlConnection.getInputStream();
         StringBuilder response = new StringBuilder();
         byte[] respBuffer = new byte[4096];
         while (is.read(respBuffer) >= 0) {
             response.append(new String(respBuffer).trim());
         }
         is.close();
         System.out.println(response.toString());
     }
 }
