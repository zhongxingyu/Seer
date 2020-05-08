package com.grooveshark.util;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class DateUtils 
 {
     public static String getNow()
     {
         Date now = new Date();
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         return formatter.format(now);
     }
 }
