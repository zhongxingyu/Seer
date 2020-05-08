 package org.shawnewald.javatools;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Various date manipulation and formatting methods.
  * @author  Shawn Ewald <shawn.ewald@gmail.com>
   * Copyright (C) 2009,2010,2011,2012 Shawn Ewald
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  *
  */
 public final class DT {
     public static enum DateRange {DAY7,DAY14,DAY28,DAY30,DAY60,DAY90};
     private static final String EMPTY = "";
     private static final SimpleDateFormat fdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     private static final SimpleDateFormat fd = new SimpleDateFormat("yyyy-MM-dd");
     private static final SimpleDateFormat txtDate = new SimpleDateFormat("MMMM d, yyyy",Locale.ENGLISH);
     private static final SimpleDateFormat rfc822 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.ENGLISH);
     private static final SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH);
     private static final SimpleDateFormat twd = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
 
     private DT () {}
     /**
      * Format <code>Date</code> to <code>yyyy-MM-dd HH:mm:ss</code> format.
      * @param d <code>Date</code>
      * @return <code>String</code>
      */
     public synchronized static String formatDateTime (final Date d) {
         return fdt.format(d);
     }
     /**
     * Format <code>Date</code> to <code>MMMM d, yyyy</code> format.
      * @param d <code>Date</code>
      * @return <code>String</code>
      */
     public synchronized static String formatTextDate (final Date d) {
         return txtDate.format(d);
     }
     /**
      * Format <code>Date</code> to RFC-822 (<code>EEE, dd MMM yyyy HH:mm:ss Z</code>) date format.
      * @param d <code>Date</date>
      * @return <code>String</code>
      */
     public synchronized static String formatRFC822Date (final Date d) {
         return rfc822.format(d);
     }
     /**
      * Format <code>Date</code> to ISO (<code>yyyy-MM-dd'T'HH:mm:ssz</code>) date format.
      * @param d <code>Date</code>
      * @return <code>String</code>
      */
     public synchronized static String formatISODate (final Date d) {
         return iso.format(d);
     }
     /**
      * Format <code>Date</code> to Twitter API (<code>yyyy-MM-dd'T'HH:mm:ssz</code>) date format.
      * @param d <code>Date</code>
      * @return <code>String</code>
      */
     public synchronized static String formatTwitterDate (final Date d) {
         return twd.format(d);
     }
     /**
      * Format <code>Date</code> to <code>yyyy-MM-dd</code> date format.
      * @param d <code>Date</code>
      * @return <code>String</code>
      */
     public synchronized static String formatDate (final Date d) {
         return fd.format(d);
     }
     /**
      * Convert <code>String</code> representation of a date to a <code>Date</code>.
      * @param d <code>Date</code>
      * @return <code>String</code>
      */
     public synchronized static Date stringToDate (final String date) {
         Date dateObj = null;
         try { dateObj = fd.parse(date); }
         catch (final Exception e) {}
         return dateObj;
     }
     /**
      * Convert <code>String</code> representation of a date to a <code>Date</code>.
      * @param d <code>Date</code>
      * @return <code>String</code>
      */
     public synchronized static Date stringToDateISO (final String date) {
         Date dateObj = null;
         try { dateObj = iso.parse(date); }
         catch (final Exception e) {}
         return dateObj;
     }
     /**
      * Convert <code>String</code> representation of a date to a <code>Date</code>.
      * @param d <code>Date</code>
      * @return <code>String</code>
      */
     public synchronized static String isoDateToTextDate (final String date) {
         String textDate = EMPTY;
         try {
             final Date dateObj = iso.parse(date);
             textDate = formatTextDate(dateObj);
         }
         catch (final Exception e) {}
         return textDate;
     }
     /**
      * Convert <code>String</code> representation of a date to a <code>Date</code>.
      * @param d <code>Date</code>
      * @return <code>String</code>
      */
     public synchronized static String isoDateToDate (final String date) {
         String textDate = EMPTY;
         try {
             final Date dateObj = iso.parse(date);
             textDate = formatDate(dateObj);
         }
         catch (final Exception e) {}
         return textDate;
     }
     /**
      * Calculate the number of days between two dates represented as <code>String</code>s.
      * @param start <code>String</code>
      * @param end <code>String</code>
      * @return <code>long</code>
      */
     public synchronized static long daysBetweenDates (final String start, final String end) {
         Date s = null;
         Date e = null;
         try {
             s = fd.parse(start);
             e = fd.parse(end);
         }
         catch (final Exception ex) {}
         return daysBetweenDates(s,e);
     }
     /**
      * Calculate the number of days between two <code>Date</code>s.
      * @param start <code>Date</code>
      * @param end <code>Date</code>
      * @return <code>long</code>
      */
     public static long daysBetweenDates (final Date start,final Date end) {
         long days = 0;
         if (start != null && end != null) {
             // Creates two calendars instances
             final Calendar cal1 = Calendar.getInstance();
             final Calendar cal2 = Calendar.getInstance();
 
             // Set the date for both of the calendar instance
             cal1.setTime(start);
             cal2.setTime(end);
 
             // Calculate difference in milliseconds
             final long diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();
 
             // Calculate difference in days
             days = diff / (24 * 60 * 60 * 1000);
         }
         return days;
     }
     /**
      * Add the given date represented as a <code>String</code>
      * by the given number of days. Return date represented as a <code>String</code>.
      * @param date <code>String</code>
      * @param addBy <code>int</code>
      * @return <code>String</code>
      */
     public synchronized static String addDate (final String date, final int addBy) {
         final Date d = stringToDate(date);
         final Calendar cal = Calendar.getInstance();
         cal.setTime(d);
         cal.add(Calendar.DATE, addBy);
         return fd.format(cal.getTime());
     }
     /**
      * Add the given <code>Date</code> by the given number of days.
      * @param date <code>Date</code>
      * @param addBy <code>int</code>
      * @return <code>Date</code>
      */
     public synchronized static Date addDate (final Date date, final int addBy) {
         final Date d = date;
         final Calendar cal = Calendar.getInstance();
         cal.setTime(d);
         cal.add(Calendar.DATE, addBy);
         return cal.getTime();
     }
     /**
      * Calculate a date range from <code>startDate</code> back to <code>endDate</code> or <code>DT.DateRange</code> days past.
      * If <code>startDate</code> is null or greater than <code>endDate</code>, <code>startDate</code> defaults to the present date.
      * If <code>timeInterval</code> is <code>null</code> and <code>endDate</code> is null, <code>endDate</code> defaults to 28 days in the past from <code>startDate</code>
      * if <code>timeInterval</code> is not <code>null</code>, <code>endDate</code> is ignored.
      * @param startDate <code>Date</code>
      * @param endDate <code>Date</code>
      * @param timeInterval <code>String</code>
      * @return <code>Date[]</code>
      */
     public static Date[] setDates (final Date startDate, final Date endDate,
             final DT.DateRange timeInterval) {
         final Date[] dates = new Date[]{((startDate == null)
                                          ? new Date() : startDate), endDate};
         final Calendar theDate = Calendar.getInstance();
         if (timeInterval == null) {
             if (endDate == null
                     || dates[0].getTime() >= endDate.getTime()) {
                 theDate.setTime(dates[0]);
                 theDate.add(Calendar.DATE, -28);
                 dates[1] = theDate.getTime();
             }
         }
         else {
             theDate.setTime(new Date());
             dates[1] = theDate.getTime();
             if (timeInterval == DT.DateRange.DAY7) {
                 theDate.add(Calendar.DATE, -7);
                 dates[0] = theDate.getTime();
             }
             else if (timeInterval == DT.DateRange.DAY14) {
                 theDate.add(Calendar.DATE, -14);
                 dates[0] = theDate.getTime();
             }
             else if (timeInterval == DT.DateRange.DAY28) {
                 theDate.add(Calendar.DATE, -28);
                 dates[0] = theDate.getTime();
             }
             else if (timeInterval == DT.DateRange.DAY30) {
                 theDate.add(Calendar.DATE, -30);
                 dates[0] = theDate.getTime();
             }
             else if (timeInterval == DT.DateRange.DAY60) {
                 theDate.add(Calendar.DATE, -60);
                 dates[0] = theDate.getTime();
             }
             else if (timeInterval == DT.DateRange.DAY90) {
                 theDate.add(Calendar.DATE, -90);
                 dates[0] = theDate.getTime();
             }
             else {
                 theDate.add(Calendar.DATE, -28);
                 dates[0] = theDate.getTime();
             }
         }
         return dates;
     }
     public static String[] setDates (final Date startDate, final int days) {
         final String[] dates = new String[2];
         if (startDate != null && days > 0){
             final Calendar theDate = Calendar.getInstance();
             theDate.add(Calendar.DATE, -days);
             dates[0] = formatDate(theDate.getTime());
             dates[1] = formatDate(startDate);
         }
         return dates;
     }
     /**
      * Calculate a date range.
      * @param startDate
      * @param endDate
      * @return
      */
     public synchronized static String[] setStringDates  (final Date startDate, final Date endDate) {
         final Date[] dates = setDates(startDate,endDate,null);
         return new String[] {formatDate(dates[0]),formatDate(dates[1])};
     }
     /**
      * Calculate a date range.
      * @param startDate
      * @param endDate
      * @return
      */
     public synchronized static String[] setStringDates  (final String startDate, final String endDate) {
         final Date[] dates = setDates(stringToDate(startDate),
                 stringToDate(endDate),null);
         return new String[] {formatDate(dates[0]),formatDate(dates[1])};
     }
     /**
      * Calculate a date range.
      * @param startDate
      * @param endDate
      * @param timeInterval
      * @return
      */
     public synchronized static String[] setStringDates  (final Date startDate, final Date endDate,
             final DT.DateRange timeInterval) {
         final Date[] dates = setDates(startDate,endDate,timeInterval);
         return new String[] {formatDate(dates[0]),formatDate(dates[1])};
     }
     /**
      * Calculate a date range.
      * @param startDate
      * @param endDate
      * @param timeInterval
      * @return
      */
     public synchronized static String[] setStringDates  (final String startDate, final String endDate,
             final DT.DateRange timeInterval) {
         final Date[] dates = setDates(stringToDate(startDate),
                 stringToDate(endDate),timeInterval);
         return new String[] {formatDate(dates[0]),formatDate(dates[1])};
     }
     /**
      * Get a <code>Set</code> of timestamps.
      * @param start <code>long</code>
      * @param end <code>long</code>
      * @return <code>Set</code>
      */
     public static Set<Long> getTimestampRangeArray (final long start, final long end) {
         final Set<Long> dates = new HashSet<Long>();
         final Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(start);
         long current = start;
         while (true) {
             if (current >= end) { break; }
             else {
                 //dates.add((current / 1000));
                 dates.add(current);
                 cal.add(Calendar.DATE, 1);
                 current = cal.getTimeInMillis();
             }
         }
         dates.add(end);
         return dates;
     }
     /**
      * Get a <code>Set</code> of timestamps.
      * @param start <code>Date</code>
      * @param days <code>int</code>
      * @return <code>Set</code>
      */
     public static Set<Long> getTimestampRangeArray (final Date start, final int days) {
         final Set<Long> dates = new HashSet<Long>();
         long current = start.getTime();
         final Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(current);
         for (int i=0;i<days;i++) {
             //dates.add((current / 1000));
             dates.add(current);
             cal.add(Calendar.DATE, 1);
             current = cal.getTimeInMillis();
         }
         dates.add(current);
         cal.add(Calendar.DATE, 1);
         current = cal.getTimeInMillis();
         dates.add(current);
         return dates;
     }
     /**
      * Get a <code>Set</code> of timestamps.
      * @param start <code>String</code>
      * @param days <code>int</code>
      * @return <code>Set</code>
      */
     public static Set<Long> getTimestampRangeArray (final String start, final int days)
             throws ParseException {
         final Set<Long> dates = new HashSet<Long>();
         long current = fd.parse(start).getTime();
         final Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(current);
         for (int i=0;i<days;i++) {
             dates.add(current);
             cal.add(Calendar.DATE, 1);
             current = cal.getTimeInMillis();
         }
         dates.add(current);
         cal.add(Calendar.DATE, 1);
         current = cal.getTimeInMillis();
         dates.add(current);
         return dates;
     }
     /**
      * Get current timestamp.
      * @return <code>Long</code>
      */
     public static Long getNow () {
         return new Date().getTime();
     }
 }
