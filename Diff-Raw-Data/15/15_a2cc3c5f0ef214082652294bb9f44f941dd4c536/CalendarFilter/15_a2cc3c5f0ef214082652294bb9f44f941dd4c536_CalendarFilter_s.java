 package com.edgark.monthview;
 
 import android.util.MonthDisplayHelper;
 
 import java.util.Calendar;
 
 public class CalendarFilter {
     private final MonthDisplayHelper helper;
     private final int startDay;
     private final int endDay;
 
     public CalendarFilter(MonthDisplayHelper helper) {
         this.helper = helper;
         this.startDay = helper.getDayAt(0, 0);
         this.endDay = helper.getDayAt(5, 6);
     }
 
     public Calendar[] getFilters() {
         if (previousMonthVisible() && nextMonthVisible()) {
             return getFiltersForPastAndNextMonth();
         } else if (previousMonthVisible() && !nextMonthVisible()) {
             return getFiltersForPastAndCurrentMonth();
         } else if (!previousMonthVisible() && nextMonthVisible()) {
             return getFiltersForCurrentAndNext();
         } else {
             return getFiltersForCurrentMonth();
         }
     }
 
     public boolean nextMonthVisible() {
         int firstDayInLastWeek = helper.getDayAt(5, 0);
         int daysInMonth = helper.getNumberOfDaysInMonth();
         return (firstDayInLastWeek + 6 != daysInMonth);
     }
 
     public boolean previousMonthVisible() {
         return helper.getOffset() > 0;
     }
 
     private Calendar[] getFiltersForPastAndNextMonth() {
         Calendar previousCalendar = getCurrentCalendar();
        previousCalendar.set(Calendar.DAY_OF_MONTH, startDay);
         previousCalendar.add(Calendar.MONTH, -1);
         previousCalendar.getTimeInMillis();
 
         Calendar nextCalendar = getCurrentCalendar();
        nextCalendar.set(Calendar.DAY_OF_MONTH, endDay);
         nextCalendar.add(Calendar.MONTH, 1);
         nextCalendar.getTimeInMillis();
 
         return new Calendar[]{previousCalendar, nextCalendar};
     }
 
     private Calendar[] getFiltersForPastAndCurrentMonth() {
         Calendar previousCalendar = getCurrentCalendar();
        previousCalendar.set(Calendar.DAY_OF_MONTH, startDay);
         previousCalendar.add(Calendar.MONTH, -1);
         previousCalendar.getTimeInMillis();
 
         Calendar currentCalendar = getCurrentCalendar();
         currentCalendar.set(Calendar.DAY_OF_MONTH, endDay);
         currentCalendar.getTimeInMillis();
 
         return new Calendar[]{previousCalendar, currentCalendar};
     }
 
     private Calendar[] getFiltersForCurrentAndNext() {
         Calendar currentCalendar = getCurrentCalendar();
         currentCalendar.set(Calendar.DAY_OF_MONTH, startDay);
         currentCalendar.getTimeInMillis();
 
         Calendar nextCalendar = getCurrentCalendar();
        nextCalendar.set(Calendar.DAY_OF_MONTH, endDay);
         nextCalendar.add(Calendar.MONTH, 1);
         nextCalendar.getTimeInMillis();
 
         return new Calendar[]{currentCalendar, nextCalendar};
     }
 
     private Calendar[] getFiltersForCurrentMonth() {
         Calendar startCalendar = getCurrentCalendar();
         startCalendar.set(Calendar.DAY_OF_MONTH, startDay);
         startCalendar.getTimeInMillis();
 
         Calendar endCalendar = getCurrentCalendar();
         startCalendar.set(Calendar.DAY_OF_MONTH, endDay);
         startCalendar.getTimeInMillis();
 
         return new Calendar[]{startCalendar, endCalendar};
     }
 
     private Calendar getCurrentCalendar() {
         Calendar currentCalendar = Calendar.getInstance();
         currentCalendar.set(Calendar.MONTH, helper.getMonth());
         currentCalendar.getTimeInMillis();
         return currentCalendar;
     }
 }
