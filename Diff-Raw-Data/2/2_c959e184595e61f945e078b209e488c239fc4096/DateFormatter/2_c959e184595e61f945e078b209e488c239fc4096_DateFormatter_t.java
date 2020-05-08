 package com.oppian.oikos.util;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import android.text.format.DateFormat;
 
 public class DateFormatter {
     public static String formatToYesterdayOrToday(Date dateTime) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(dateTime);
         Calendar today = Calendar.getInstance();
         Calendar yesterday = Calendar.getInstance();
         yesterday.add(Calendar.DATE, -1);
 
         if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                 && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
             return "Today";
         } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
                 && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
             return "Yesterday";
         } else {
            return DateFormat.format("MMM d", dateTime).toString();
         }
     }
 }
