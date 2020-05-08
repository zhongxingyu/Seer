 package com.ratethisfest.shared;
 
 public class DateTimeUtils {
 
   public static String militaryToCivilianTime(int milTime) {
     String ampm;
    if (milTime < 1200 || milTime == 2400) {
       ampm = "a";
     } else {
       ampm = "p";
     }
 
     if (milTime < 100) {
       milTime += 1200;
     }
 
     if (milTime >= 1300) {
       milTime -= 1200;
     }
 
     String timeStr = milTime + "";
 
     int timeStrLen = timeStr.length();
     timeStr = timeStr.substring(0, timeStrLen - 2) + ":"
         + timeStr.substring(timeStrLen - 2, timeStrLen) + ampm;
     return timeStr;
   }
 
 }
