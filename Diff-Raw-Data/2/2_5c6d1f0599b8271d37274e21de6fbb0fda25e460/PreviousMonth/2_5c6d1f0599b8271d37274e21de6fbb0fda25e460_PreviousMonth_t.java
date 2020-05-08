 package com.tech_tec.android.simplecalendar.model;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Iterator;
 
 public class PreviousMonth {
     
     private Month mMonth;
     private int mCount;
     
     PreviousMonth(Month month, int count) {
         mMonth = month;
         mCount = count;
     }
     
     public int getYear() {
         return getCalendar().get(Calendar.YEAR);
     }
     
     public int getMonth() {
         return getCalendar().get(Calendar.MONTH) + 1;
     }
     
     private Calendar getCalendar() {
         Calendar calendar = Calendar.getInstance();
         calendar.set(mMonth.getYear(), mMonth.getMonth() - 1, 1);
         calendar.add(Calendar.MONTH, -1);
         return calendar;
     }
     
     public Iterator<Day> getDays() {
         Calendar calendar = getCalendar();
         int maxDate = calendar.getActualMaximum(Calendar.DATE);
         
         calendar.set(Calendar.DATE, maxDate);
         
         ArrayList<Day> days = new ArrayList<Day>();
         for (int i = 0; i < mCount; i++) {
             days.add(new Day(calendar.getTime()));
            calendar.add(Calendar.DATE, -1);
         }
         Collections.reverse(days);
         return days.iterator();
     }
     
 }
