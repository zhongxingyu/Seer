 /*
 Copyright (c) 2005, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.justjournal.db;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 /**
  * Created by IntelliJ IDEA.
  * User: laffer1
  * Date: Jan 4, 2004
  * Time: 3:11:01 PM
  *
  * @author Lucas Holt
  * @version 1.2
  * @since 1.0
  */
 public final class DateTimeBean implements DateTime {
     // default is epoch -1 sec
     private int day = 31;
     private int month = 12;
     private int year = 1969;
     private int hour = 23;
     private int minutes = 59;
 
     public int getDay() {
         return this.day;
     }
 
     public void setDay(final int day) {
         if (day < 1)
             throw new IllegalArgumentException("illegal day");
 
         if (day > 31)
             throw new IllegalArgumentException("illegal day");
 
         this.day = day;
     }
 
     public int getMonth() {
         return this.month;
     }
 
     public void setMonth(final int month) {
         if (month < 1)
             throw new IllegalArgumentException("illegal month");
 
         if (month > 12)
             throw new IllegalArgumentException("illegal month");
 
         this.month = month;
     }
 
     public int getYear() {
         return this.year;
     }
 
     public void setYear(final int year) {
         if (year < 1970)
             throw new IllegalArgumentException("illegal year");
 
         this.year = year;
     }
 
     public int getHour() {
         return this.hour;
     }
 
     public void setHour(int hour) {
         if (hour == 24)
             hour = 0;
 
         if (hour < 0)
             throw new IllegalArgumentException("illegal hour");
 
         if (hour > 23)
             throw new IllegalArgumentException("illegal hour");
 
         this.hour = hour;
     }
 
     public int getMinutes() {
         return this.minutes;
     }
 
     public void setMinutes(final int minutes) {
         if (minutes < 0)
             throw new IllegalArgumentException("illegal minutes");
 
         if (minutes > 59)
             throw new IllegalArgumentException("illegal minutes");
 
         this.minutes = minutes;
     }
 
     public void set(final String mysqlDate)
             throws java.text.ParseException {
         final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         final java.util.Date myDate = fmt.parse(mysqlDate);
        final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
 
         calendarg.setTime(myDate);
         year = calendarg.get(Calendar.YEAR);
         month = calendarg.get(Calendar.MONTH) + 1;  // months are zero based in java!  DUMB!
         day = calendarg.get(Calendar.DAY_OF_MONTH);
         hour = calendarg.get(Calendar.HOUR_OF_DAY);
         minutes = calendarg.get(Calendar.MINUTE);
     }
 
     public void set(final java.util.Date date) {
        final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
 
         calendarg.setTime(date);
         year = calendarg.get(Calendar.YEAR);
         month = calendarg.get(Calendar.MONTH) + 1;  // not sure about this one
         day = calendarg.get(Calendar.DAY_OF_MONTH);
         hour = calendarg.get(Calendar.HOUR_OF_DAY);
         minutes = calendarg.get(Calendar.MINUTE);
     }
 
     public String toPubDate() {
         //Sat, 07 Sep 2002 09:43:33 GMT
         final java.util.GregorianCalendar cal =
                new java.util.GregorianCalendar(year, month, day, hour, minutes);
         final java.util.Date current = cal.getTime();
         final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zz");
 
         return formatmydate.format(current);
     }
 
     public String toString() {
         final StringBuffer sb = new StringBuffer();
 
         // 2004-01-04 14:24:18
 
         sb.append(Integer.toString(year));
         sb.append("-");
 
         if (month < 10)
             sb.append("0");
 
         sb.append(Integer.toString(month));
         sb.append("-");
 
         if (day < 10)
             sb.append("0");
 
         sb.append(Integer.toString(day));
         sb.append(" ");
 
         if (hour < 10)
             sb.append("0");
 
         sb.append(Integer.toString(hour));
         sb.append(":");
 
         if (minutes < 10)
             sb.append("0");
 
         sb.append(Integer.toString(minutes));
 
         return sb.toString();
     }
 }
