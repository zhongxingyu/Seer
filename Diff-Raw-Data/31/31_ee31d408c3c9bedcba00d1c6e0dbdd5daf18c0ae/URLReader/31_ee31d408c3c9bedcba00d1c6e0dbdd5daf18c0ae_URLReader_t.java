 package org.nolat.rsircbot.tools;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
import java.net.URLConnection;
 
 public class URLReader {
 
     public static String readUrl(String urlString) throws Exception {
         BufferedReader reader = null;
         StringBuffer buffer = new StringBuffer();
         try {
             URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) " +
                    "AppleWebKit/537.11 (KHTML, like Gecko) " +
                    "Chrome/23.0.1271.95 " +
                    "Safari/537.11");
 
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
             }
 
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
 
         return buffer.toString();
     }
 }
