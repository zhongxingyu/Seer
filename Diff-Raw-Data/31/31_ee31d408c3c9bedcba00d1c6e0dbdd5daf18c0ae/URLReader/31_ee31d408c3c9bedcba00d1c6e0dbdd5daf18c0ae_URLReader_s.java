 package org.nolat.rsircbot.tools;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 public class URLReader {
 
     public static String readUrl(String urlString) throws Exception {
         BufferedReader reader = null;
         StringBuffer buffer = new StringBuffer();
         try {
             URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
 
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
             }
 
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
 
         return buffer.toString();
     }
 }
