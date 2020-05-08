 package org.social.core.util;
 
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class UtilDateTime {
 
     private static Logger logger = LoggerFactory.getLogger(UtilDateTime.class);
 
     /**
      * Return a Timestamp for right now
      * 
      * @return Timestamp for right now
      */
     public static java.sql.Timestamp nowTimestamp() {
         return getTimestamp(System.currentTimeMillis());
     }
 
     /**
      * Convert a millisecond value to a Timestamp.
      * 
      * @param time
      *            millsecond value
      * @return Timestamp
      */
     private static java.sql.Timestamp getTimestamp(long time) {
         return new java.sql.Timestamp(time);
     }
 
     /**
      * Converts a date string with the format 2011-05-10T18:35:38+0000 and Wed,
      * 19 Jan 2011 20:21:46 +0000 to a new timestamp.
      * 
      * @param dateString
      * @return
      */
     public static java.sql.Timestamp toTimestamp(String dateString) {
         Timestamp timestamp = null;
         if (UtilValidate.isEmpty(dateString)) {
             return timestamp;
         }
 
         Calendar calendar;
         if (dateString.matches(".{10}T.{1,13}")) {
             calendar = toCalendarFromFbTime(dateString);
         } else if (dateString.matches("\\w{1,3}, \\d{1,2} \\w{1,3} .*")) {
             calendar = toCalendarFromTwitterTime(dateString);
        } else if (dateString.matches("\\d{1,2}/\\d{1,2}/\\d{4,4}")) {
             calendar = toCalendarFromYelpTime(dateString);
         } else if (dateString.matches("Reviewed .*")) {
             calendar = toCalendarFromTripAdvisorUsTime(dateString);
         } else if (dateString.matches("Bewertet am .*")) {
             calendar = toCalendarFromTripAdvisorDeTime(dateString);
         } else if (dateString.matches("Published .*")) {
             calendar = toCalendarFromZagatTime(dateString);
         } else if (dateString.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{2}:\\d{2}")) {
             calendar = toCalendarFromQypeTime(dateString);
         } else if (dateString.matches("\\d{10,}")) {
             calendar = toCalendarFromFoursquareTime(dateString);
         } else {
             return timestamp;
         }
 
         timestamp = new Timestamp(calendar.getTimeInMillis());
 
         return timestamp;
     }
 
     // create Calendar from date time string 1325687943 (unix time)
     private static Calendar toCalendarFromFoursquareTime(String dateString) {
         long dateLong = Long.valueOf(dateString);
 
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(dateLong * 1000);
 
         return cal;
     }
 
     private static Calendar toCalendarFromQypeTime(String dateString) {
         String pattern = "yyyy-MM-dd'T'hh:mm:ssZ";
         dateString = dateString.replaceAll("([\\+\\-]\\d\\d):(\\d\\d)", "$1$2");
 
         return parseDateTimeToCalendar(dateString, pattern, Locale.ENGLISH);
     }
 
     // create Calendar from date time string Published June 12, 2012
     private static Calendar toCalendarFromZagatTime(String dateString) {
         dateString = dateString.replace("Published ", "");
 
         String pattern = "MMM dd, yyyy";
 
         Calendar calendar = parseDateTimeToCalendar(dateString, pattern, Locale.ENGLISH);
 
         setTimeToDayEnd(calendar);
 
         return calendar;
     }
 
     private static void setTimeToDayEnd(Calendar calendar) {
         calendar.set(Calendar.HOUR, 23);
         calendar.set(Calendar.MINUTE, 59);
         calendar.set(Calendar.SECOND, 59);
     }
 
     // create Calendar from date time string Bewertet am 24. Juli 2012
     private static Calendar toCalendarFromTripAdvisorDeTime(String dateString) {
         dateString = dateString.replace("Bewertet am ", "");
 
         String pattern = "dd. MMM yyyy";
 
         Calendar calendar = parseDateTimeToCalendar(dateString, pattern, Locale.GERMAN);
 
         setTimeToDayEnd(calendar);
 
         return calendar;
     }
 
     // create Calendar from date time string Reviewed July 24, 2012
     private static Calendar toCalendarFromTripAdvisorUsTime(String dateString) {
         dateString = dateString.replace("Reviewed ", "");
 
         String pattern = "MMM dd, yyyy";
 
         Calendar calendar = parseDateTimeToCalendar(dateString, pattern, Locale.ENGLISH);
 
         setTimeToDayEnd(calendar);
 
         return calendar;
     }
 
     // create Calendar from date time string 12/20/2012
     private static Calendar toCalendarFromYelpTime(String dateString) {
         String pattern = "MM/dd/yyyy";
 
         Calendar calendar = parseDateTimeToCalendar(dateString, pattern, Locale.ENGLISH);
 
         setTimeToDayEnd(calendar);
 
         return calendar;
     }
 
     // create Calendar from date time string Wed, 19 Jan 2011 20:21:46 +0000
     private static Calendar toCalendarFromTwitterTime(String dateString) {
         String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
 
         return parseDateTimeToCalendar(dateString, pattern, Locale.ENGLISH);
     }
 
     // create Calendar from date time string 2011-05-10T18:35:38+0000
     private static Calendar toCalendarFromFbTime(String dateString) {
         String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
 
         return parseDateTimeToCalendar(dateString, pattern, Locale.ENGLISH);
     }
 
     private static Calendar parseDateTimeToCalendar(String dateString, String pattern, Locale locale) {
         SimpleDateFormat formatter = new SimpleDateFormat(pattern, locale);
 
         Calendar calendar = null;
         try {
             Date date = formatter.parse(dateString);
             calendar = Calendar.getInstance();
             calendar.setTime(date);
         } catch (ParseException e) {
             logger.error("Could not parse date: " + dateString, e);
         }
 
         return calendar;
     }
 
     public static String connvertTimestampToTwitterTime(Timestamp timestamp) {
         if (timestamp == null) {
             return "";
         }
         String[] splitedTs = timestamp.toString().split(" ");
         return splitedTs[0];
     }
 
     public static String connvertTimestampToFacebookTime(Timestamp timestamp) {
         if (timestamp == null) {
             return "";
         }
         Long unixTimestamp = timestamp.getTime() / 1000L;
         return unixTimestamp.toString();
     }
 
     public static boolean isMessageDateBeforeLastNetworkAccess(Timestamp messageNetworkTs, Timestamp lastNetworkAccess) {
         return (messageNetworkTs != null && lastNetworkAccess != null && lastNetworkAccess.after(messageNetworkTs));
     }
 
     public static Timestamp stirngToTimestamp(String tsString) {
         Timestamp newTimestamp = null;
 
         if (UtilValidate.isNotEmpty(tsString)) {
             newTimestamp = Timestamp.valueOf(tsString);
         }
 
         return newTimestamp;
     }
 }
