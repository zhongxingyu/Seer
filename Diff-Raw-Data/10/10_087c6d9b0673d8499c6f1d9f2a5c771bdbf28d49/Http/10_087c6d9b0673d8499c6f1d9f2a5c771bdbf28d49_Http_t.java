 package util.net;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  *
  * @author Eugene Susla
  */
 public class Http {
 
     public static InputStream getResponseAsStream(String url) throws IOException {
         URLConnection urlConnection = new URL(url).openConnection();
         return urlConnection.getInputStream();
     }
     
     public static String getResponse(URL url) throws IOException {
         StringBuilder result = new StringBuilder();
         
         URLConnection urlConnection = url.openConnection();
         BufferedReader in = new BufferedReader(
                 new InputStreamReader(
                 urlConnection.getInputStream()));
         
         String inputLine;
         while ((inputLine = in.readLine()) != null) {
             //System.out.println(inputLine);
             result.append(inputLine).append('\n');
         }
         in.close();
         return result.toString();
     }
     
     public static String getResponse(String url) throws IOException {
         return getResponse(new URL(url));
     }
     
     public static String getResponsePart(URL url, String start, String end) throws IOException {
         StringBuilder result = new StringBuilder();
         
         URLConnection urlConnection = url.openConnection();
         BufferedReader in = new BufferedReader(
                 new InputStreamReader(
                 urlConnection.getInputStream()));
         
         String inputLine;
         boolean writing = false;
         while ((inputLine = in.readLine()) != null) {
             if (writing) {
                 String toWrite = inputLine;
                 int endIndex = toWrite.indexOf(end);
                 if (endIndex != -1) {
                     result.append(inputLine.substring(
                                 0, endIndex + end.length()));
                     writing = false;
                 } else {
                     result.append(inputLine).append('\n');
                 }
             } else {
                 int startIndex = inputLine.indexOf(start);
                 int endIndex = inputLine.indexOf(end);
                 if (startIndex != -1) {
                     writing = true;
                     if (endIndex == -1) {
                         result.append(inputLine.substring(startIndex)).append('\n');
                     } else {
                         result.append(inputLine.substring(
                                 startIndex, endIndex + end.length()));
                         break;
                     }
                 }
             }
         }
         in.close();
         return result.toString();
     }
     
     public static String getResponsePart(String url, String start, String end) throws IOException {
         return getResponsePart(new URL(url), start, end);
     }
 
     public static void writeResponseToFile(URL url, File file) throws IOException {
         URLConnection urlConnection = url.openConnection();
         InputStream inputStream = urlConnection.getInputStream();
         OutputStream out = new FileOutputStream(file);
         byte buf[] = new byte[1024];
         int len;
         while ((len = inputStream.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         out.close();
         inputStream.close();
     }
     
     public static void writeResponseToFile(String url, String fileName) throws IOException {
         writeResponseToFile(new URL(url), new File(fileName));
     }
 }
