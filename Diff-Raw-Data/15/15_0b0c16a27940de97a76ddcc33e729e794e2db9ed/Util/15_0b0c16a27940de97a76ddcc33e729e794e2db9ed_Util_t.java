 package org.bxmy.shiftclock;
 
 import java.util.Date;
 
 import android.widget.DatePicker;
 import android.widget.TimePicker;
 
 public final class Util {
 
     public static void updateTime(TimePicker picker, int seconds) {
         picker.setCurrentHour(seconds / 3600 % 24);
         picker.setCurrentMinute(seconds / 60 % 60);
     }
 
     public static int getTime(TimePicker picker) {
         return picker.getCurrentHour() * 3600 + picker.getCurrentMinute() * 60;
     }
 
     public static void updateDate(DatePicker picker, long dayInSeconds) {
         Date date = secondsToDate(dayInSeconds);
         picker.init(date.getYear() + 1900, date.getMonth(), date.getDate(),
                 null);
     }
 
     public static long getDate(DatePicker picker) {
         int year = picker.getYear();
         int month = picker.getMonth();
         int date = picker.getDayOfMonth();
         return new Date(year - 1900, month, date).getTime() / 1000;
     }
 
     public static int getCurrentYear() {
         Date date = new Date();
         return date.getYear() + 1900;
     }
 
     public static long getDateOfTimeInSeconds(long timeInSeconds) {
         Date date = secondsToDate(timeInSeconds);
         return dateToSeconds(new Date(date.getYear(), date.getMonth(),
                 date.getDate()));
     }
 
    public static long getDateInSeconds(Date date) {
        return dateToSeconds(new Date(date.getYear(), date.getMonth(),
                date.getDate()));
    }

     public static String formatTimeIn24Hours(long seconds) {
         return String.format("%02d:%02d", seconds / 3600 % 24,
                 seconds / 60 % 60);
     }
 
     public static String formatHourMinute(Date time) {
         return String.format("%02d:%02d", time.getHours(), time.getMinutes());
     }
 
     public static String formatMonthDate(Date date) {
         return String.format("%2d-%2d", date.getMonth() + 1, date.getDate());
     }
 
     public static String formatMonth2Minute(Date time) {
         return formatMonthDate(time) + " " + formatHourMinute(time);
     }
 
     public static String formatYear2Date(Date date) {
         return String.format("%4d-%2d-%2d", date.getYear() + 1900,
                 date.getMonth() + 1, date.getDate());
     }
 
     public static String formatYear2Minute(Date time) {
         return formatYear2Date(time) + " " + formatHourMinute(time);
     }
 
     public static Date secondsToDate(long seconds) {
         return new Date(seconds * 1000);
     }
 
     public static long dateToSeconds(Date time) {
         return time.getTime() / 1000;
     }
 
     public static boolean isSameDay(Date first, Date second) {
         return first.getYear() == second.getYear()
                 && first.getMonth() == second.getMonth()
                 && first.getDate() == second.getDate();
     }
 
     public static boolean isSameYear(Date first, Date second) {
         return first.getYear() == second.getYear();
     }
 
     public static String formatDate(long dayInSeconds) {
         Date now = new Date();
         Date date = secondsToDate(dayInSeconds);
         if (now.getYear() == date.getYear()) {
             return formatMonthDate(date);
         } else {
             return formatYear2Date(date);
         }
     }
 
     public static String formatWeek(long dayInSeconds) {
         Date date = secondsToDate(dayInSeconds);
         String[] weeks = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
         return weeks[date.getDay() % 7];
     }
 
     public static String formatTimeRelatived(long beginSeconds,
             int relativeSeconds) {
         Date begin = secondsToDate(beginSeconds);
         Date relative = secondsToDate(beginSeconds + relativeSeconds);
         if (isSameDay(begin, relative)) {
             return formatHourMinute(relative);
         } else if (isSameYear(begin, relative)) {
             return formatMonth2Minute(relative);
         } else {
             return formatYear2Minute(relative);
         }
     }
 }
