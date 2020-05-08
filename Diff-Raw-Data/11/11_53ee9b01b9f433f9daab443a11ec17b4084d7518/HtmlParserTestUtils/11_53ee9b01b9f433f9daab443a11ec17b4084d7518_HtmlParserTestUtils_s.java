 package com.abudko.reseller.huuto.query.service.list;
 
import java.io.*;
 
 public class HtmlParserTestUtils {
    
     public static String readHtmlFromFile(String path) throws IOException {
         String html = null;
        BufferedReader reader = new BufferedReader(new FileReader(path));
         String s = null;
         while ((s = reader.readLine()) != null) {
             html += s;
             html += '\n';
         }
         reader.close();
 
         return html;
     }
 
 }
