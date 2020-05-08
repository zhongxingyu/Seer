 package com.abudko.reseller.huuto.query.service.list;
 
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
 
 public class HtmlParserTestUtils {

     public static String readHtmlFromFile(String path) throws IOException {
         String html = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
         String s = null;
         while ((s = reader.readLine()) != null) {
             html += s;
             html += '\n';
         }
         reader.close();
 
         return html;
     }
 
 }
