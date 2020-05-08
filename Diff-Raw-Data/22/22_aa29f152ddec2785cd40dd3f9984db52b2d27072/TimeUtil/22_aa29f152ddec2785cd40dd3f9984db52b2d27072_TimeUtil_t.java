 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 15th November 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.bookings.impl.slotsengine;
 
 import static au.edu.uts.eng.remotelabs.schedserver.bookings.impl.slotsengine.SlotBookingEngine.TIME_QUANTUM;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import au.edu.uts.eng.remotelabs.schedserver.bookings.impl.BookingEngine.TimePeriod;
 
 /**
  * Date / Time utility to convert dates between different formats.
  */
 public class TimeUtil
 {
     /**
      * Converts a date object to a date string without time portion in the form 
      * &lt;year&gt;-&lt;month&gt;-&lt;day&gt;.
      * 
      * @param date date to convert
      * @return string date
      */
     public static String getDateStr(Date date)
     {
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         
         return TimeUtil.getDateStr(cal);
     }
     
     /**
      * Converts a calendar object to a date string without time portion in the form 
      * &lt;year&gt;-&lt;month&gt;-&lt;day&gt;.
      * 
      * @param cal calendar to convert
      * @return string date
      */
     public static String getDateStr(Calendar cal)
     {        
         StringBuilder dt = new StringBuilder();
         dt.append(cal.get(Calendar.YEAR));
         dt.append('-');
         dt.append(cal.get(Calendar.MONTH));
         dt.append('-');
         dt.append(cal.get(Calendar.DAY_OF_MONTH));
         return dt.toString();
     }
     
     /**
      * Gets a date which is at the beginning of a day.
      * 
      * @param dateStr day key
      * @return start date
      */
     public static Calendar getDayBegin(String dateStr)
     {
         String parts[] = dateStr.split("-");
         
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
         cal.set(Calendar.MONTH, Integer.parseInt(parts[1]));
         cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         
         return cal;
     }
     
     /**
      * Gets a date which is at the beginning of a day.
      * 
     * @param date a date
      * @return start date
      */
     public static Calendar getDayBegin(Date date)
     {
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         
         return cal;
     }
     
     /**
      * Gets a date which is at the beginning of a day.
      * 
     * @param date a date
      * @return start date
      */
     public static Calendar getDayBegin(Calendar date)
     {   
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(date.getTimeInMillis());
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         
         return cal;
     }
     
     /**
      * Gets a date which is at the end of a day.
      * 
      * @param dateStr day key
      * @return end date
      */
     public static Calendar getDayEnd(String dateStr)
     {
         String parts[] = dateStr.split("-");
         
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
         cal.set(Calendar.MONTH, Integer.parseInt(parts[1]));
         cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
         cal.set(Calendar.HOUR_OF_DAY, 23);
         cal.set(Calendar.MINUTE, 59);
         cal.set(Calendar.SECOND, 59);
         cal.set(Calendar.MILLISECOND, 999);
         
         return cal;
     }
     
     /**
      * Gets a date which is at the end of a day.
      * 
     * @param date a date
      * @return end date
      */
     public static Calendar getDayEnd(Date date)
     {
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         cal.set(Calendar.HOUR_OF_DAY, 23);
         cal.set(Calendar.MINUTE, 59);
         cal.set(Calendar.SECOND, 59);
         cal.set(Calendar.MILLISECOND, 999);
         
         return cal;
     }
     
     /**
      * Gets a date which begins at the end of a day.
      * 
     * @param date a date
      * @return end date
      */
     public static Calendar getDayEnd(Calendar date)
     {   
         Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(date.getTimeInMillis());
         cal.set(Calendar.HOUR_OF_DAY, 23);
         cal.set(Calendar.MINUTE, 59);
         cal.set(Calendar.SECOND, 59);
         cal.set(Calendar.MILLISECOND, 999);
         
         return cal;
     }
     
     /**
      * Gets the slot index for the specified date time.
      * 
      * @param date time of slot
      * @return slot index
      */
     public static int getSlotIndex(Date date)
     {
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         return TimeUtil.getSlotIndex(cal);
     }
     
     /**
      * Gets the slot index for the specified date time.
      * 
      * @param cal time of slot
      * @return slot index
      */
     public static int getSlotIndex(Calendar cal)
     {
         return (int)Math.ceil(
                 cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)) / TIME_QUANTUM;
     }
     
     /**
      * Gets the slot index for the specified date/time with the following
      * conditions:
      * <ul>
      *  <li>If the date/time is on a specified day, the time slot index is returned.</li>
      *  <li>If the date/time is on a earlier day, 0 is returned.</li>
      *  <li>If the date/time is on a later day, the last slot index is returned.</li> 
      * </ul>
      * 
      * @param cal date/time
      * @param dayKey day 
      * @return slot index
      */
     public static int getDaySlotIndex(Calendar cal, String dayKey)
     {
         /* Whether the calendar is before the day. */        
         if (cal.before(TimeUtil.getDayBegin(dayKey)))
         {
             return 0;
         }
         
         if (cal.after(TimeUtil.getDayEnd(dayKey)))
         {
             return 86400 / TIME_QUANTUM - 1;
         }
         
         return TimeUtil.getSlotIndex(cal);
     }
     
     /**
      * Gets a date generated from a date string and a slot index.
      * 
      * @param dateStr date
      * @param slot quantum slot
      * @return date
      */
     public static Calendar getCalendarFromSlot(String dateStr, int slot)
     {
         String parts[] = dateStr.split("-");
         Calendar cal = Calendar.getInstance();
         
         cal.set(Calendar.YEAR, Integer.parseInt(parts[0]));
         cal.set(Calendar.MONTH, Integer.parseInt(parts[1]));
         cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
         
         /* Hours. */
         int t = slot * TIME_QUANTUM / 3600; 
         cal.set(Calendar.HOUR_OF_DAY, t);
         
         /* Minutes. */
         int m = (slot - t * 3600 / TIME_QUANTUM) * TIME_QUANTUM / 60;
         cal.set(Calendar.MINUTE, m);
         
         int s = (t - m) / TIME_QUANTUM;
         cal.set(Calendar.SECOND, s);
         
         return cal;
     }
     
     /**
      * Compares day strings. Returns:
      * <ul>
      *  <li> 0 if the two days are equal</li>
      *  <li>-1 if d1 is less than d2</li>
      *  <li> 1 if d1 is greater than d2</li>
      * </ul>
      * 
      * @param d1 day 1
      * @param d2 day 2
      * @return a -1, 0, or 1 as d1 is less than, equal to, or greater d2
      */
     public static int compareDays(String d1, String d2)
     {
         if (d1.equals(d2)) return 0;
         
         String d1pts[] = d1.split("-", 3);
         String d2pts[] = d2.split("-", 3);
         
         /* Compare years. */
         int t1 = Integer.parseInt(d1pts[0]);
         int t2 = Integer.parseInt(d2pts[0]);
         
         if (t1 < t2) return -1;
         else if (t1 != t2) return 1;
         
         /* Compare months. */
         t1 = Integer.parseInt(d1pts[1]);
         t2 = Integer.parseInt(d2pts[1]);
         
         if (t1 < t2) return -1;
         else if (t1 != t2) return 1;
         
         /* Compare days. */
         t1 = Integer.parseInt(d1pts[2]);
         t2 = Integer.parseInt(d2pts[2]);
         
         if (t1 < t2) return -1;
         else if (t1 != t2) return 1;
         
         return 0;
     }
     
     /**
      * Returns the days in the specified date period.
      * 
      * @param period time period
      * @return list of day keys
      */
     public static List<String> getDayKeys(TimePeriod period)
     {
         List<String> days = new ArrayList<String>();
         
         Calendar start = Calendar.getInstance();
         start.setTimeInMillis(period.getStartTime().getTimeInMillis());
         Calendar end = period.getEndTime();
         
         days.add(TimeUtil.getDateStr(start));
         while (end.compareTo(start) < 86.4e6 && start.get(Calendar.DAY_OF_YEAR) != end.get(Calendar.DAY_OF_YEAR))
         {
             start.add(Calendar.DAY_OF_MONTH, 1);
             days.add(TimeUtil.getDateStr(start));
         }
         
         return days;
     }
     
     /**
      * Returns the days in the specified date period.
      * 
     * @param start range start
     * @param end range end
      * @return list of day keys
      */
     public static List<String> getDayKeys(Date start, Date end)
     {
         List<String> days = new ArrayList<String>();
         
         Calendar startCal = Calendar.getInstance();
         startCal.setTime(start);
         Calendar endCal = Calendar.getInstance();
         endCal.setTime(end);
         
         days.add(TimeUtil.getDateStr(start));
         while (end.compareTo(start) < 86.4e6 && startCal.get(Calendar.DAY_OF_YEAR) != endCal.get(Calendar.DAY_OF_YEAR))
         {
             startCal.add(Calendar.DAY_OF_MONTH, 1);
             days.add(TimeUtil.getDateStr(startCal));
         }
         
         return days;
     }
 }
