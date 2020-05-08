 package org.javaz.util;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 /**
  * Sometimes you need store Day in DB, and Timestamp/Date is too big.
  * With this util you can use Integer, or even Short in DB
  */
 public class DayUtil
 {
     private static final int A = 100000;
     private static final int B = 100;
     private static final double DAY_KOEF = 4.16666666667;
     private static final double MINUTE_KOEF = 0.0694444444444;
 
     public static final long START_2011 = 1293876000000l; // 01.01.2011 00-00
 
     public static HashMap<Integer, Integer> DAYS_2011 = new HashMap<Integer, Integer>();
 
     static
     {
         DAYS_2011.put(2011, 0);
         DAYS_2011.put(2012, 365);
         DAYS_2011.put(2013, 731);
         DAYS_2011.put(2014, 1096);
         DAYS_2011.put(2015, 1461);
         DAYS_2011.put(2016, 1826);
         DAYS_2011.put(2017, 2192);
         DAYS_2011.put(2018, 2557);
         DAYS_2011.put(2019, 2922);
         DAYS_2011.put(2020, 3287);
     }
 
     public static Integer getIntegerTime()
     {
         Calendar instance = Calendar.getInstance();
         return A * instance.get(Calendar.YEAR) + B * instance.get(Calendar.DAY_OF_YEAR) +
                 (int) (
                         DAY_KOEF * instance.get(Calendar.HOUR_OF_DAY)
                                 + MINUTE_KOEF * instance.get(Calendar.MINUTE));
     }
 
 
     public static int getDayShort(Date date)
     {
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(START_2011);
 
         Calendar targetCalendar = Calendar.getInstance();
         targetCalendar.setTime(date);
         targetCalendar.set(Calendar.HOUR_OF_DAY, 0);
         targetCalendar.set(Calendar.MINUTE, 0);
         targetCalendar.set(Calendar.SECOND, 0);
         targetCalendar.set(Calendar.MILLISECOND, 0);
 
         int daysBetween = 0;
         int targetYear = targetCalendar.get(Calendar.YEAR);
         if (calendar.get(Calendar.YEAR) <= targetYear)
         {
             if (DAYS_2011.containsKey(targetYear))
             {
                 daysBetween += DAYS_2011.get(targetYear);
                 calendar.set(Calendar.YEAR, targetYear);
             }
         }
         while (calendar.before(targetCalendar))
         {
             //todo optimize;
             calendar.add(Calendar.DAY_OF_MONTH, 1);
             daysBetween++;
         }
         return 1 + daysBetween;
     }
 
     public static int getFirstDayOfMonth()
     {
         return getFirstDayOfMonth(null);
     }
 
     public static int getFirstDayOfMonth(Date date)
     {
         Calendar calendar = Calendar.getInstance();
         if (date != null)
         {
             calendar.setTime(date);
         }
         calendar.set(Calendar.DAY_OF_MONTH, 1);
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         return DayUtil.getDayShort(calendar.getTime());
     }
 
     public static int getFirstDayOfYear()
     {
         return getFirstDayOfYear(null);
 
     }
 
     public static int getFirstDayOfYear(Date date)
     {
         Calendar calendar = Calendar.getInstance();
         if (date != null)
         {
             calendar.setTime(date);
         }
         calendar.set(Calendar.MONTH, 0);
         calendar.set(Calendar.DAY_OF_MONTH, 1);
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         return DayUtil.getDayShort(calendar.getTime());
     }
 
     public static Date getDateFromDay(int day)
     {
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(START_2011);
         calendar.add(Calendar.DAY_OF_MONTH, day - 1);
         return calendar.getTime();
     }
 
     /**
     * @return day from beginning of 2011 Year, or from beginning of 2000 Year, if newScheme = false;
      *         It's NOT the same as ((extract(year from NOW()) - 2011)*365 + extract(doy from NOW())) in database;
      *         As leap years counting.
      */
     public static int getDayShort()
     {
         return getDayShort(Calendar.getInstance().getTime());
     }
 
     public static void main(String[] args)
     {
         System.out.println(getDayShort());
     }
 }
