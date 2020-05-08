 package com.core.util;
 /**
  * TimerThread.java
  * Copyright (c) 2002 by Dr. Herong Yang
  */
 import java.util.*;
 import java.text.*;
 
 public class TimerThread extends Thread {
    public static final int NORMAL_CLOCK = 1;
    public static final int COUNT_DOWN = 2;
    public static final int STOP_WATCH = 3;
    private int type; // type of clock
    private int c_millisecond, c_second, c_minute, c_hour;
    private static int remaining_seconds = 2;
    private static int clock_interval = 100; // in milliseconds < 1000
 
    public TimerThread(int t) {
       type = t;
       if (type==NORMAL_CLOCK) {
          GregorianCalendar c = new GregorianCalendar();
          c_hour = c.get(Calendar.HOUR_OF_DAY);
          c_minute = c.get(Calendar.MINUTE);
          c_second = c.get(Calendar.SECOND);
          c_millisecond = c.get(Calendar.MILLISECOND);
       } else if (type==COUNT_DOWN) {
          c_hour = remaining_seconds/60/60;
          c_minute = (remaining_seconds%(60*60))/60;
          c_second = remaining_seconds%60;
          c_millisecond = 0;
       } else {
          c_hour = 0;
          c_minute = 0;
          c_second = 0;
          c_millisecond = 0;
       }
    }
 
    public void setRemainingMinutes(int mins) {
        remaining_seconds = mins * 60;
        c_minute = mins%60;
    }
 
    public void setRemainingSeconds(int secs) {
        remaining_seconds = secs;
        c_minute = secs/60;
        c_second = secs % 60;
    }
 
    public void run() {
       while (!isInterrupted()) {
          try {
             sleep(clock_interval);
          } catch (InterruptedException e) {
             break; // the main thread wants this thread to end
          }
          if (type==NORMAL_CLOCK || type==STOP_WATCH)
             c_millisecond +=clock_interval;
          else c_millisecond -= clock_interval;
          if (c_millisecond>=1000) {
             c_second += c_millisecond/1000;
             c_millisecond = c_millisecond%1000;
          }
          if (c_second>=60) {
             c_minute += c_second/60;
             c_second = c_second%60;
          }
          if (c_minute>=60) {
             c_hour += c_minute/60;
             c_minute = c_minute%60;
          }
          if (c_millisecond<0) {
             c_second--;
             c_millisecond += 1000;
          }
          if (c_second<0) {
             c_minute--;
             c_second += 60;
          }
          if (c_minute<0) {
             c_hour--;
             c_minute += 60;
          }
          if (c_hour<0) {
             c_hour = 0;
            // fix 59:59
            c_minute = 0;
            c_second = 0;
             break; // end this thread 
          }
       }
    }
 
    public String getClock() {
       // returning the clock as a string of HH:mm:ss format
       GregorianCalendar c = new GregorianCalendar();
       c.set(Calendar.HOUR_OF_DAY,c_hour);
       c.set(Calendar.MINUTE,c_minute);
       c.set(Calendar.SECOND,c_second);
       c.set(Calendar.MILLISECOND,c_millisecond);
       //SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
       SimpleDateFormat f = new SimpleDateFormat("mm:ss");
       return f.format(c.getTime());
    }
 }
