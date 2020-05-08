 package jKlout2;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.nio.charset.Charset;
 
 class HttpConnector {
 
     private String url;
 
     HttpConnector() {
     }
    
     public void setURL(String url) {
         this.url = url;
     }
 
     public String getContent() throws IOException {
         URL link = new URL(url);
         HttpURLConnection con = (HttpURLConnection) link.openConnection();
         if (con.getResponseCode() == 200) {
             BufferedReader buff = new BufferedReader(new InputStreamReader(
                     con.getInputStream(), Charset.forName("UTF-8")));
             return readAll(buff);
         } else {
             BufferedReader buff = new BufferedReader(new InputStreamReader(
                     con.getErrorStream(), Charset.forName("UTF-8")));
            throw new IllegalStateException(readAll(buff));
         }
     }
    
     private String readAll(Reader rd) throws IOException {
         StringBuilder sb = new StringBuilder();
         int cp;
         while ((cp = rd.read()) != -1) {
             sb.append((char) cp);
         }
         return sb.toString();
     }
 }
