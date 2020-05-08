 package com.juickadvanced.imaging;
 
 /**
  * Created with IntelliJ IDEA.
  * User: san
  * Date: 9/14/12
  * Time: 10:31 PM
  * To change this template use File | Settings | File Templates.
  */
 public class HTMLImageSourceDetector {
     public static boolean isHTMLImageSource0(String url) {
        if (url.indexOf("gelbooru.com/") != -1) return true;
         return false;
     }
 }
