 package twigkit.jurl;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.*;
 import java.net.URL;
 
 public class JUrl {
 
     public JUrl(String[] args) {
         try {
             URL url = new URL(args[0]);
 
             print(url.toExternalForm());
             print();
 
             HttpResponse response = request(url);
             HttpEntity entity = response.getEntity();
 
             if (entity != null) {
                 print(response.getStatusLine());
                 print(response.getAllHeaders());
 
                 String fileName = getFileName(args, url);
                 print("Writing to: %s", fileName);
                 write(fileName, response);
 
                 print("Done!");
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             print();
         }
     }
 
     public HttpResponse request(URL url) throws IOException {
         HttpClient httpclient = new DefaultHttpClient();
         HttpGet httpget = new HttpGet(url.toExternalForm());
 
         return httpclient.execute(httpget);
     }
 
     public String getFileName(String[] args, URL url) {
         String fileName;
         if (args.length > 1) {
             fileName = args[1];
         } else {
             fileName = getFileNameFromURL(url);
         }
 
         if (fileName.length() < 1) {
             fileName = "file";
         }
 
         return fileName;
     }
 
     public String getFileNameFromURL(URL url) {
         return url.getFile().substring(url.getFile().lastIndexOf(File.separator) + 1, url.getFile().length());
     }
 
     public void print(StatusLine statusLine) {
         print(statusLine.toString());
     }
 
     public void print(Header[] headers) {
         printSeparator();
 
         for (Header header : headers) {
             print("%s: %s", header.getName(), header.getValue());
         }
 
         printSeparator();
         print();
     }
 
     public static void print(String string, Object... args) {
         print(String.format(string, args));
     }
 
     public static void printSeparator() {
         print("---------------------------------------");
     }
 
     public static void print(String string) {
         System.out.println(string);
     }
 
     public static void print() {
         System.out.println();
     }
 
     public boolean write(String fileName, HttpResponse response) throws IOException {
         File file = new File(fileName);
         if (!file.exists()) {
             file.createNewFile();
         }
 
         BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
 
         BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
         int inByte;
         while ((inByte = bis.read()) != -1) {
             bos.write(inByte);
         }
         bis.close();
         bos.close();
 
         return true;
     }
 
     public static void main(String[] args) {
         print();
         print("     ___  __   __  ______    ___     ");
         print("    |   ||  | |  ||    _ |  |   |    ");
         print("    |   ||  | |  ||   | ||  |   |    ");
         print("    |   ||  |_|  ||   |_||_ |   |    ");
         print(" ___|   ||       ||    __  ||   |___ ");
         print("|       ||       ||   |  | ||       |");
         print("|_______||_______||___|  |_||_______| 1.0-SNAPSHOT");
 
         print();
 
         if (args.length > 0) {
             new JUrl(args);
         } else {
             print("Usage:");
            print("java -jar jURL.jar http://twigkit.com/resources/images/screen-twigkit.png [somefilename.png]");
         }
 
         print();
     }
 }
