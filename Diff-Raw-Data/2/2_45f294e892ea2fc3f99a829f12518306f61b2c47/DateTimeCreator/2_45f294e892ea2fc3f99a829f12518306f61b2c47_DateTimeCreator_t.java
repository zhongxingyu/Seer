 package com.edge.twitter_research.core;
 
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class DateTimeCreator {
 
     private static DateFormat dateFormat
             = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 
     public static String getDateTimeString(){
        return dateFormat.format(new Date(System.currentTimeMillis()));
     }
 }
