 package com.schmal.util;
 
 import java.net.URL;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class LinkUtil
 {
     public static String getLinkURL(Elements links, String text)
     {
         for (Element link : links)
         {
             if (text.equals(link.ownText()))
             {
                 return link.attr("href");
             }
         }
 
        return "/FUCK";
     }
 }
