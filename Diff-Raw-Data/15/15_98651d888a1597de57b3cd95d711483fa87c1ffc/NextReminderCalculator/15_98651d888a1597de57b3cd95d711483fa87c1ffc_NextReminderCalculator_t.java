 package com.jtbdevelopment.rest.helpers;
 
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
 
 public class NextReminderCalculator {
     public static LocalTime getReminderTime() {
         //  TODO - make configurable
         return new LocalTime(21, 0, 0, 0);
     }
 
     private static LocalDateTime adjustToThanksgiving(final LocalDate dateToAdjust) {
         LocalDateTime turkeyDay = dateToAdjust.toLocalDateTime(getReminderTime());
         turkeyDay = turkeyDay.withMonthOfYear(DateTimeConstants.NOVEMBER).withDayOfMonth(1);
         while (turkeyDay.getDayOfWeek() != DateTimeConstants.THURSDAY) {
             turkeyDay = turkeyDay.plusDays(1);
         }
         //  At first thursday
         return turkeyDay.plusWeeks(3);
     }
 
     private static LocalDateTime getThisYearThanksgiving() {
         LocalDate now = new LocalDate();
         return adjustToThanksgiving(now);
     }
 
     private static LocalDateTime getNextYearThanksgiving() {
         LocalDate now = new LocalDate();
         return adjustToThanksgiving(now.plusYears(1));
     }
 
     private static LocalDateTime getThisYearChristmasEve() {
         LocalDateTime now = new LocalDate().toLocalDateTime(getReminderTime());
         return now.withMonthOfYear(DateTimeConstants.DECEMBER).withDayOfMonth(24);
     }
 
     public static LocalDateTime getNextDailyReminderTime() {
         LocalDateTime currentYearStart = getThisYearThanksgiving();
         LocalDateTime currentYearEnd = getThisYearChristmasEve();
         LocalDateTime reminderForToday = new LocalDate().toLocalDateTime(getReminderTime());
        LocalDateTime now = new LocalDateTime();
        if(reminderForToday.compareTo(now) > 0) {
            reminderForToday = reminderForToday.plusDays(1);
        }
         if (reminderForToday.compareTo(currentYearEnd) > 0) {
             //  This Christmas is over (Dec 24 reminder time thru Dec 31)
             return getNextYearThanksgiving();
         } else if (reminderForToday.compareTo(currentYearStart) < 0) {
             //  It's not thanksgiving day yet
             return currentYearStart;
         }
         return reminderForToday;
     }
 }
