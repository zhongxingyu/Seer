 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package model;
 
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 //import java.text.SimpleDateFormat;
 
 /**
  *
  * @author liufeng
  */
 
 public class Time
 {
    /*
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    */
     private Calendar calendar;
 
     public Time() {
         calendar = Calendar.getInstance();
     }
 
     public Time(int year, int month, int day, int hour, int minute, int second) {
         calendar = Calendar.getInstance();
         calendar.set(year, month-1, day, hour, minute, second);
     }
 
     /**
      * Accessor
      */
 
     /**
      * It will return 1 if the year has set to 0.
      * 
      * @return
      */
     public int getYear() {
         return calendar.get(Calendar.YEAR);
     }
 
     public int getMonth() {
         return calendar.get(Calendar.MONTH) + 1;
     }
 
     public int getDay() {
         return calendar.get(Calendar.DAY_OF_MONTH);
     }
 
     public int getHour() {
        return calendar.get(Calendar.HOUR);
     }
 
     public int getMinute() {
         return calendar.get(Calendar.MINUTE);
     }
 
     public int getSecond() {
         return calendar.get(Calendar.SECOND);
     }
     
 
     /**
      * Methods from interface Computable
      *
      * @param Time time
      */
     public void add(Time time) {
         calendar.add(Calendar.SECOND, time.getSecond());
         calendar.add(Calendar.MINUTE, time.getMinute());
         calendar.add(Calendar.HOUR, time.getHour());
         calendar.add(Calendar.DATE, time.getDay());
         calendar.add(Calendar.MONTH, time.getMonth());
         calendar.add(Calendar.YEAR, time.getYear());
     }
 
     public int minus(Time time) {
         long end = time.getTimeInMillis();
         long start = this.getTimeInMillis();
         return (int)((end - start) / (24 * 60 * 60 * 1000));
 
     }
 
     public long getTimeInMillis() {
         return calendar.getTimeInMillis();
     }
 
     /**
      * Compare this with time.
      * @param time
      * @return An integer:
      * <ul>
      *   <li><strong>Negative</strong> if <em>this</em> is earlier than <em>time</em></li>
      *   <li><strong>0</strong> if <em>this</em> is equal to <em>time</em></li>
      *   <li><strong>Positive</strong> if <em>this</em> is later than <em>time</em></li>
      * </ul>
      */
     public int compareTo(Time time) {
         return calendar.compareTo(time.getCalendar());
     }
 
     public Calendar getCalendar() {
         return calendar;
     }
 
     public boolean after(Time time) {
         return calendar.after(time.getCalendar());
     }
 
     public boolean before(Time time) {
         return calendar.before(time.getCalendar());
     }
 
     public Time getCurrentTime() {
         Calendar calendar = Calendar.getInstance();
         int year = calendar.get(Calendar.YEAR);
         int month = calendar.get(Calendar.MONTH);
         int day = calendar.get(Calendar.DATE);
         int hour = calendar.get(Calendar.HOUR);
         int minute = calendar.get(Calendar.MINUTE);
         int second = calendar.get(Calendar.SECOND);
         return new Time(year, month, day, hour, minute, second);
     }
 
     // YYYY-MM-DD HH:MM:SS
     public String toString() {
         return getYear() + "-" + getMonth() + "-" + getDay() + " "
                 + getHour() + ":" + getMinute() + ":" + getSecond();
     }
 }
