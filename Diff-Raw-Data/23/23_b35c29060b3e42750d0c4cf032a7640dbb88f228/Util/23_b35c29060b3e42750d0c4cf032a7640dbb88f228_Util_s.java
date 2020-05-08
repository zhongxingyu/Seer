 package com.yetanotherx.xbot.util;
 
 import com.google.common.base.Joiner;
 import com.yetanotherx.xbot.XBotDebug;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.PeriodFormatter;
 import org.joda.time.format.PeriodFormatterBuilder;
 
 public class Util {
 
    public final static PeriodFormatter periodFormatter = new PeriodFormatterBuilder().appendWeeks().appendSuffix(" weeks, ").appendDays().appendSuffix(" days, ").appendHours().appendSuffix(" hours, ").appendMinutes().appendSuffix(" minutes, ").appendSeconds().appendSuffix(" seconds").printZeroNever().toFormatter();
 
     /**
      * Converts the given number of days/hours/minutes/seconds/millis
      * to a single millisecond value. Used for assigning jobs.
      * @param days
      * @param hours
      * @param minutes
      * @param seconds
      * @param milliseconds
      * @return 
      */
     public static long toLong(int days, int hours, int minutes, int seconds, int milliseconds) {
         return milliseconds
                 + (seconds * 1000)
                 + (minutes * 60 * 1000)
                 + (hours * 60 * 60 * 1000)
                 + (days * 24 * 60 * 60 * 1000);
     }
 
     /**
      * Returns all parameters as a List<String>
      * @param params
      * @return 
      */
     public static List<String> asList(String... params) {
         return Arrays.asList(params);
     }
 
     /**
      * Converts milliseconds elapsed to a string representing elapsed time.
      * @param millis
      * @return 
      */
     public static String millisToString(long millis) {
         Period period = new Period(millis);
         return periodFormatter.print(period);
     }
 
     /**
      * Formats the number of millis since the epoch.
      * @param millis
      * @return 
      */
     public static String formatDate(long millis) {
         DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
         return format.print(millis);
     }
 
     public static String join(String key, String... args) {
         return Joiner.on(key).join(args);
     }
 
     public static String join(String key, List<String> args) {
         return Joiner.on(key).join(args);
     }
 
     public static int[] parseTZDate(String date) {
         Matcher m = RegexUtil.getMatcher("(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})Z", date);
         if (!m.matches()) {
             XBotDebug.error("Util", "Could not parse expiry string: " + date);
             return null;
         }
 
         int year = Integer.parseInt(m.group(1));
         int month = Integer.parseInt(m.group(2));
         int day = Integer.parseInt(m.group(3));
         int hour = Integer.parseInt(m.group(4));
         int min = Integer.parseInt(m.group(5));
         int sec = Integer.parseInt(m.group(6));
         return new int[] {year, month, day, hour, min, sec};
     }
     
     public static long dateToLong(int[] date) {
         return new DateTime(date[0], date[1], date[2], date[3], date[4], date[5]).getMillis();
     }
 }
