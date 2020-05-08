 package com.smartsms.controllers;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
import javax.lang.model.util.Elements;
import javax.swing.text.Document;
import javax.swing.text.html.parser.Element;
 import java.io.IOException;
 
 /**
  * @author rajith
  */
 public class Sample {
 
     public static void main(String[] args) throws IOException {

         Document doc;
 
         try {
 
             // need http protocol
            doc = Jsoup.connect("http://onlineslangdictionary.com/word-list/b-b/").get();
 
             // get page title
             String title = doc.title();
             System.out.println("title : " + title);
 
             // get all links
             Elements links = doc.select("a[href]");
            // get the value from href attribute
             for (Element link : links) {
                 System.out.println("text : " + link.text());
             }
 
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
