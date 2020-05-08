 package com.github.browep.wegu;
 
 import java.util.Calendar;
 
 /**
  * Created by IntelliJ IDEA.
  * User: paul
  * Date: 1/23/11
  * Time: 1:28 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Constants {
     public static final String ALARM_ALERT_ACTION = "com.github.browep.wegu.ALARM_ALERT";
     public static final String SCREEN_OFF = "screen_off";
     public static final String PREFS_FILE_NAME = "main_prefs_file";
     public static int CHECKBOX_PREPEND = 333000;
     public static final String CURRENT_DATE_LONG = "current_date_long";
     public static final long WEEK_INTERVAL_MILLIS = 1000 * 60 * 60 * 24 * 7;
 
 
     static final int TIME_DIALOG_ID = 0;
 
 
     public static int[] DAY_MAP = new int[]{
             Calendar.MONDAY,
             Calendar.TUESDAY,
             Calendar.WEDNESDAY,
             Calendar.THURSDAY,
             Calendar.FRIDAY,
             Calendar.SATURDAY,
             Calendar.SUNDAY,
     };
 
     public static String[] DAY_MAP_NAMES = new String[]{ null,
             "Sunday",
             "Monday",
             "Tuesday",
             "Wednesday",
             "Thursday",
             "Friday",
             "Saturday",
     };
     public static final String HOUR_OF_DAY = "hour_of_day";
     public static final String MINUTE_OF_DAY = "minute_of_day";
     public static final String DAY_PREPEND = "day_set_";
     public static final String FROM_ALARM = "from_alarm";
 }
