 package ee.webmedia.mikker.upload;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 public class Uploader {
     private static final String BOUNDARY = "----WebKitFormBoundary8NHXoPOgtdmTKB7e";
     
     private Configuration ctx;
 
     public Uploader(Configuration ctx) {
         this.ctx = ctx;
     }
 
     public void upload(byte content[]) throws IOException {
         URL url = new URL(ctx.getUploadUrl());
         HttpURLConnection theUrlConnection = (HttpURLConnection) url.openConnection();
         theUrlConnection.setRequestMethod("POST");
         theUrlConnection.setDoOutput(true);
         theUrlConnection.setDoInput(true);
         theUrlConnection.setUseCaches(false);
 
 
         theUrlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary="
                 + BOUNDARY);
 
         theUrlConnection.setRequestProperty("Cookie", CookieParser.toRequestHeader(ctx.getCookies()));
 
         theUrlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Chrome/7.0.517.44 Safari/534.7");
 
         DataOutputStream httpOut = new DataOutputStream(theUrlConnection.getOutputStream());
 
         String str = "--" + BOUNDARY + "\r\n"
                    + "Content-Disposition: form-data; name=\"" + ctx.getFileFieldName() + "\"; filename=\"" + ctx.getUploadFilename() + "\"\r\n"
                    + "Content-Type: " + ctx.getUploadMimeType() + "\r\n"
                    + "\r\n";
 
         httpOut.write(str.getBytes());
 
         httpOut.write(content);
 
         KeyValuePairParser.Pair postParameters[] = ctx.getAdditionalPostParameters();
         for (KeyValuePairParser.Pair pair : postParameters) {
             String name = pair.key;
             String value = pair.value;
             httpOut.write(("\r\n--" + BOUNDARY + "\r\n").getBytes());
             httpOut.write(("Content-Disposition: form-data; name=\""+ name +"\"\r\n" +
                     "\r\n").getBytes());
             httpOut.write(value.getBytes());
         }
 
         // last one gets extra hypens at the end
         httpOut.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());
  
         httpOut.flush();
         httpOut.close();
 
         // read & parse the response
         InputStream is = theUrlConnection.getInputStream();
         StringBuilder response = new StringBuilder();
         byte[] respBuffer = new byte[4096];
        int len = 0;
         while ((len = is.read(respBuffer)) >= 0) {
             response.append(new String(respBuffer, 0, len).trim());
         }
         is.close();
         System.out.println(response.toString());
     }
 
     public static void main(String[] args) throws Exception {
         String filename = "matti_testib_uploadi_appletist.ogg";
         CookieParser.Cookie c[] = new CookieParser.Cookie[] {
                 new CookieParser.Cookie("fe_typo_user", "afa679555031be44ae3dd2784cbf2472")
         };
 
        KeyValuePairParser.Pair pairs[] = new KeyValuePairParser.Pair[] {
                new KeyValuePairParser.Pair("MAX_FILE_SIZE", "20000000"),
                new KeyValuePairParser.Pair("tx_fileupload_pi1[do_upload]", "Saada fail!")
        };
 
         String url =
         "http://www.kitarrikool.ee/minu-kool/akordsaate-kursus/ii-tase/taseme-kontroll.html"
         // "http://localhost:9999/~matti/recorder/upload.php"
               ;
 
         Configuration ctx = new Configuration(url,
                 "tx_fileupload_pi1",
                 filename,
                 c);
         new Uploader(ctx).upload(
                 readFile(filename));
     }
 
     private static byte[] readFile(String filename) throws IOException {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         FileInputStream fin = new FileInputStream(filename);
         byte buf[] = new byte[1024];
        int len = 0;
         while ((len = fin.read(buf)) != -1) {
             bos.write(buf, 0, len);
         }
         fin.close();
         return bos.toByteArray();
     }
 }
