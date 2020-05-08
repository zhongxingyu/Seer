 package com.frcscout.util;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public final class StringUtil {
     public static boolean isBlank(String s) { //returns true of string s is either null, has length 0, or only contains 
                                               //white space characters
         if (s != null) {
             s = s.trim();
             if (s.length() <= 0) {
                 return true;
             } else {
                 return false;
             }
         }
         return true;
     }
     
     public static Date stringToDate(String s) {
         Date d = null;
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
         try {
            d = (Date) df.parse(s);
         } catch (ParseException e) {
             System.out.println("Unable to parse date " + s);
         }
         return d;
     }
 }
