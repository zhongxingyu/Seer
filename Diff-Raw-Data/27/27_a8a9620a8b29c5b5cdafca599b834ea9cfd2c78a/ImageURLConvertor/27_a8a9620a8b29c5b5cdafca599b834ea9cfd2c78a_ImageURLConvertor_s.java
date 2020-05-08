 package com.juickadvanced.imaging;
 
 /**
  * Created with IntelliJ IDEA.
  * User: san
  * Date: 9/14/12
  * Time: 10:35 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ImageURLConvertor {
     public static String convertURLToDownloadable(String url) {
         if (url.indexOf("www.dropbox.com/") != -1 && url.indexOf("dl=1") == -1) {
             url += "&dl=1";
         }
         if (url.indexOf("gyazo.com/") != -1) {
             if (url.lastIndexOf(".") < url.length() - 10) {
                 url += ".png";
             }
         }
        if (url.indexOf("%") != 1) {
             return URIUtils.decode(url);
         }
         return url;
     }
 }
