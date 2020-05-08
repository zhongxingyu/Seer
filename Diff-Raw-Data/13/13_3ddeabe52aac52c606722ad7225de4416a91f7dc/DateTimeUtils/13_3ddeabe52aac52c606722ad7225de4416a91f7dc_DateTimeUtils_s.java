 package com.exadel.borsch.util;
 
 import org.joda.time.DateTime;
 
 /**
  * @author zubr
  */
 public final class DateTimeUtils {
     public static final int WORKING_DAYS_IN_WEEK = 5;
     public static final int DAYS_IN_WEEK = 7;
 
     private DateTimeUtils() {
     }
 
     public static boolean sameDates(DateTime firstDate, DateTime secondDate) {
        return (firstDate.getDayOfMonth() == secondDate.getDayOfMonth() &&
                firstDate.getMonthOfYear() == secondDate.getMonthOfYear() &&
                firstDate.getYear() == secondDate.getYear());
     }
 
     public static boolean isDateBefore(DateTime date, DateTime datePoint) {
        return (date.getYear() > datePoint.getYear() ||
                date.getMonthOfYear() > date.getMonthOfYear() ||
                date.getDayOfMonth() > date.getDayOfMonth());
     }
 
     public static DateTime getStartOfCurrentWeek() {
         DateTime today = DateTime.now();
         return today.minusDays(today.getDayOfWeek() - 1);
     }
 
     public static DateTime getStartOfWeek(DateTime ref) {
         return ref.plusDays(DAYS_IN_WEEK - (ref.getDayOfWeek() - 1));
     }
 
     public static DateTime getStartOfNextWeek() {
         return getStartOfWeek(DateTime.now());
     }
 }
