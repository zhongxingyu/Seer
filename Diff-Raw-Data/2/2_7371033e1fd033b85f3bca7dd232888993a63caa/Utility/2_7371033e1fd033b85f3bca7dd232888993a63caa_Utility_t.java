 package com.myapp.account.utility;
 
 import java.util.*;
 import java.text.*;
 
 /**
  * Utility Class.
  */
 public class Utility {
 
     public static final String DATE_FORMAT = "yyyy/MM/dd";
     public static final String DATE_AND_TIME_FORMAT = "yyyy/MM/dd-HH-mm-ss";
     public static final int DATE_YEAR_ST_POS = 0;
     public static final int DATE_YEAR_SIZE = 4;
     public static final int DATE_MONTH_ST_POS = 5;
     public static final int DATE_MONTH_SIZE = 2;
     public static final int DATE_DAY_ST_POS = 8;
     public static final int DATE_DAY_SIZE = 2;
     public static final int DATE_MONTH_END_SLASH_POS = 7;
     public static final String DATE_DELIMITER = "/";
 
     /**
      * Is String is NULL.
      * @return true if String Data is NULL.
      */
     public static boolean isStringNULL(String str) {
         if( str == null || 0 == str.length() ) {
             return true;
         }
         return false;
     }
 
     /**
      * Create Date Format.
      * @return Current Date of String Data Type (yyyy/MM/dd).
      */
     public static String CreateDateFormat(int year, int month, int day) {
         Calendar cal_date = Calendar.getInstance(TimeZone.getDefault());
         cal_date.set(year, month - 1, day);
         return (new SimpleDateFormat(DATE_FORMAT)).format(cal_date.getTime());
     }
 
     /**
      * Get Current Date.
      * @return Current Date(String Type).
      */
     public static String getCurrentDate() {
         Calendar cal_date = Calendar.getInstance(TimeZone.getDefault());
         return (new SimpleDateFormat(DATE_FORMAT)).format(cal_date.getTime());
     }
 
     /**
      * Get Current Date.
      * @return Current Date and Time(String Type).
      */
     public static String getCurrentDateAndTime() {
         Calendar cal_date = Calendar.getInstance(TimeZone.getDefault());
         return (new SimpleDateFormat(DATE_AND_TIME_FORMAT)).format(cal_date.getTime());
     }
 
     /**
      * Get First Date of Current Month.
      * @return First Date of Month Date(String Type).
      */
     public static String getFirstDateOfCurrentMonth() {
         Calendar cal_date = Calendar.getInstance(TimeZone.getDefault());
 
         // set first day.
         cal_date.set(Calendar.DATE, 1);
 
         return (new SimpleDateFormat(DATE_FORMAT)).format(cal_date.getTime());
      }
 
     /**
      * Get First Date of Target Date.
      * @param target_date Target Date of First Date(yyyy/MM/dd).
      * @return First Date of Month Date(String Type).
      */
     public static String getFirstDateOfTargetMonth(String target_date) {
         Calendar cal_date = Calendar.getInstance(TimeZone.getDefault());
         int year = Integer.valueOf(target_date.substring(DATE_YEAR_ST_POS, DATE_YEAR_ST_POS + DATE_YEAR_SIZE));
         int month = Integer.valueOf(target_date.substring(DATE_MONTH_ST_POS, DATE_MONTH_ST_POS + DATE_MONTH_SIZE));
 
         cal_date.set(year, month - 1, 1);
 
         return (new SimpleDateFormat(DATE_FORMAT)).format(cal_date.getTime());
     }
 
     /**
      * Get Last Date of Current Month.
      * @return Last Date of Month Date(String Type).
      */
     public static String getLastDateOfCurrentMonth() {
         Calendar cal_date = Calendar.getInstance(TimeZone.getDefault());
 
         //  calculate last day of month.
         cal_date.add(Calendar.MONTH, 1);
         cal_date.set(Calendar.DAY_OF_MONTH, 1);
         cal_date.add(Calendar.DAY_OF_MONTH, -1);
 
         return (new SimpleDateFormat(DATE_FORMAT)).format(cal_date.getTime());
     }
 
     /**
      * Get Last Date of Target Date.
      * @param target_date Target Date of First Date(yyyy/MM/dd).
      */
     public static String getLastDateOfTargetMonth(String target_date) {
         Calendar cal_date = Calendar.getInstance(TimeZone.getDefault());
         int year = Integer.valueOf(target_date.substring(DATE_YEAR_ST_POS, DATE_YEAR_ST_POS + DATE_YEAR_SIZE));
         int month = Integer.valueOf(target_date.substring(DATE_MONTH_ST_POS, DATE_MONTH_ST_POS + DATE_MONTH_SIZE));
         int day = Integer.valueOf(target_date.substring(DATE_DAY_ST_POS));
 
         //  calculate last day of month.
         cal_date.set(year, month - 1, day);
 
         //  calculate last day of month.
         cal_date.add(Calendar.MONTH, 1);
         cal_date.set(Calendar.DAY_OF_MONTH, 1);
         cal_date.add(Calendar.DAY_OF_MONTH, -1);
 
         return (new SimpleDateFormat(DATE_FORMAT)).format(cal_date.getTime());
     }
 
     /**
      * Split Target Date into Month and Day.
      * @param String Data that Target Date (format is yyyy/MM/dd).
      * @return String Date that Month and Day (format is MM/dd).
      */
     public static String splitMonthAndDay(String target_date) {
         return target_date.substring(DATE_MONTH_ST_POS);
     }
 
     /**
      * Split Target Date into Day.
      * @param String Data that Target Date (format is yyyy/MM/dd).
      * @return String Date that Day (format is dd).
      */
     public static String splitDay(String target_date) {
         return target_date.substring(DATE_DAY_ST_POS);
     }
 
     /**
      * Get Current Year and Month.
      * return String Data that Current Year and Month (format is yyyy/MM).
      */
     public static String getCurrentYearAndMonth() {
         return getCurrentDate().substring(DATE_YEAR_ST_POS, DATE_MONTH_END_SLASH_POS);
      }
 
     /**
      * Get Current Year.
      * @return Current year.
      */
     public static String getCurrentYear() {
         return getCurrentDate().substring(DATE_YEAR_ST_POS, DATE_YEAR_SIZE + DATE_YEAR_ST_POS);
     }
 
     /**
      * Get Current Month.
      * @return current month.
      */
     public static String getCurrentMonth() {
         return getCurrentDate().substring(DATE_MONTH_ST_POS, DATE_MONTH_ST_POS + DATE_MONTH_SIZE);
     }
 
     /**
      * Get Current Day.
      * @return String Current Day.
      */
     public static String getCurrentDay() {
         return getCurrentDate().substring(DATE_DAY_ST_POS);
     }
 
     /**
      * Get Current Day Of Week.
      * @return int Current Day of Week.
      */
     public static int getCurrentDayOfWeek() {
         return Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_WEEK);
     }
 
     /**
      * Get Day of Week.
      * @param target date(yyyy/mm/dd).
      * @return int day of week value(Sunday is One).
      */
     public static int getDayOfWeek(String target_date) {
         int year = Integer.valueOf(target_date.substring(DATE_YEAR_ST_POS, DATE_YEAR_ST_POS + DATE_YEAR_SIZE));
         int month = Integer.valueOf(target_date.substring(DATE_MONTH_ST_POS, DATE_MONTH_ST_POS + DATE_MONTH_SIZE));
         int day = Integer.valueOf(target_date.substring(DATE_DAY_ST_POS, DATE_DAY_ST_POS + DATE_DAY_SIZE));
 
         Calendar target_cal = Calendar.getInstance(TimeZone.getDefault());
 
         // setting target date.
        target_cal.set(year, month - 1, day);
 
         return target_cal.get(Calendar.DAY_OF_WEEK);
     }
 }
