 package com.worthsoln.patientview.utils;
 
 import java.util.Calendar;
 
 public class TimestampUtils {
 
     public static Calendar createTimestamp(String dateTimeString) {
         Calendar cal = null;
 
         if (-1 != dateTimeString.indexOf("-")) {
             cal = createIsoDatestamp(dateTimeString);
         } else {
             cal = createNormalDatestamp(dateTimeString);
         }
 
         return cal;
     }
 
     private static Calendar createNormalDatestamp(String dateTimeString) {
         Calendar datestamp = Calendar.getInstance();
         int year = Integer.parseInt(dateTimeString.substring(6, 10));
         int month = Integer.parseInt(dateTimeString.substring(3, 5));
         int day = Integer.parseInt(dateTimeString.substring(0, 2));
 
        datestamp.set(year, month - 1, day, 0, 0, 0);
 
         if (dateTimeString.length() == 16) {
             datestamp.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateTimeString.substring(11, 13)));
             datestamp.set(Calendar.MINUTE, Integer.parseInt(dateTimeString.substring(14, 16)));
         } else {
             datestamp.set(Calendar.HOUR_OF_DAY, 0);
             datestamp.set(Calendar.MINUTE, 0);
         }
 
        datestamp.set(Calendar.SECOND, 0);
         datestamp.set(Calendar.MILLISECOND, 0);
 
         return datestamp;
     }
 
     private static Calendar createIsoDatestamp(String dateTimeString) {
         Calendar datestamp = Calendar.getInstance();
         int year = Integer.parseInt(dateTimeString.substring(0, 4));
         int month = Integer.parseInt(dateTimeString.substring(5, 7));
         int day = Integer.parseInt(dateTimeString.substring(8, 10));
 
        datestamp.set(year, month - 1, day, 0, 0, 0);
 
         if (-1 != dateTimeString.indexOf("T")) {
             datestamp.set(Calendar.HOUR_OF_DAY, Integer.parseInt(dateTimeString.substring(11, 13)));
             datestamp.set(Calendar.MINUTE, Integer.parseInt(dateTimeString.substring(14, 16)));
 
             if (dateTimeString.length() == 19) {
                 datestamp.set(Calendar.SECOND, Integer.parseInt(dateTimeString.substring(17, 19)));
             }
         } else {
             datestamp.set(Calendar.HOUR_OF_DAY, 0);
             datestamp.set(Calendar.MINUTE, 0);
            datestamp.set(Calendar.SECOND, 0);
         }
 
         datestamp.set(Calendar.MILLISECOND, 0);
 
         return datestamp;
     }
 }
