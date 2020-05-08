 package com.thoughtworks.twu.utils;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 import static java.lang.Integer.parseInt;
 
 public class DateParser {
     private final String testDate;
     private final String testTime;
 
     public DateParser(String testDate, String testTime) {
         this.testDate = testDate;
         this.testTime = testTime;
     }
 
     public DateTime convertToDateTime() {
 
         String dateList[]=testDate.split("/");
         int hour = parseInt(testTime.substring(0, 2));
         int minute = parseInt(testTime.substring(3, 5));
        if (testTime.contains("PM"))
        {
            if(hour!=12){
            hour += 12;
            }
        }
        if(testTime.contains("AM"))
        {
            if(hour==12){
                hour=0;
            }
        }
         return new DateTime(parseInt(dateList[2]),parseInt(dateList[1]),parseInt(dateList[0]),hour,minute, DateTimeZone.UTC);
     }
 }
