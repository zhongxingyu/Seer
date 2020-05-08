 package com.juickadvanced.imaging;
 
 /**
  * Created with IntelliJ IDEA.
  * User: san
  * Date: 9/14/12
  * Time: 10:33 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ValidImageURLDetector {
     public static boolean isValidImageURL0(String urlLower) {
         if (urlLower.indexOf("http://gyazo.com") != -1) return true;
         return urlLower.endsWith(".png") || urlLower.endsWith(".gif") || urlLower.endsWith(".jpg") || urlLower.endsWith(".jpeg");
     }
 }
