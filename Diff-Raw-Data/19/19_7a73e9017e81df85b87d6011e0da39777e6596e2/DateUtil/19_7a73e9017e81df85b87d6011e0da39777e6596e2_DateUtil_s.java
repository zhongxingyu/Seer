 package org.motechproject.util;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.LocalDate;
 import org.joda.time.Period;
 import org.motechproject.MotechException;
 import org.motechproject.model.DayOfWeek;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Properties;
 import java.util.TimeZone;
 
 public class DateUtil {
 
     private static DateTimeZone dateTimeZone;
 
     public static DateTime now() {
         DateTimeZone timeZone = getTimeZone();
         return new DateTime(timeZone);
     }
 
     public static LocalDate today() {
         return new LocalDate(getTimeZone());
     }
 
     public static LocalDate tomorrow() {
         return today().plusDays(1);
     }
 
     public static LocalDate newDate(int year, int month, int day) {
         return new LocalDate(getTimeZone()).withYear(year).withMonthOfYear(month).withDayOfMonth(day);
     }
 
     public static DateTime newDateTime(LocalDate localDate, int hour, int minute, int second) {
         return new DateTime(getTimeZone()).
                 withYear(localDate.getYear()).withMonthOfYear(localDate.getMonthOfYear()).withDayOfMonth(localDate.getDayOfMonth())
                 .withHourOfDay(hour).withMinuteOfHour(minute).withSecondOfMinute(second);
     }
 
     public static DateTime setTimeZone(DateTime dateTime) {
         return dateTime.toDateTime(getTimeZone());
     }
 
     public static DateTime newDateTime(Date date) {
         return new DateTime(date.getTime(), getTimeZone()).withMillisOfSecond(0);
     }
 
     public static LocalDate newDate(Date date) {
         if (date == null) return null;
         return new LocalDate(date.getTime(), getTimeZone());
     }
 
     public static LocalDate pastDateWith(DayOfWeek dayOfWeek, int minNumberOfDaysAgo) {
        LocalDate today = DateUtil.today();
        LocalDate startDate = today.withDayOfWeek(dayOfWeek.getValue());
 
        Period period = new Period(startDate, today);
         if (period.getDays() > minNumberOfDaysAgo) return startDate;
 
         startDate = startDate.minusDays(1);
         return startDate.withDayOfWeek(dayOfWeek.getValue());
     }
 

     private static DateTimeZone getTimeZone() {
         if (dateTimeZone != null) return dateTimeZone;
         try {
             Properties dateProperties = new Properties();
             InputStream resourceAsStream = DateUtil.class.getResourceAsStream("/date.properties");
             if (resourceAsStream == null) return DateTimeZone.getDefault();
             dateProperties.load(resourceAsStream);
             String timeZoneString = dateProperties.getProperty("timezone");
             dateTimeZone = DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZoneString));
         } catch (IOException e) {
             throw new MotechException("Error while loading timezone from date.properties", e);
         }
         return dateTimeZone;
     }
 }
