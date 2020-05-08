 /*
  * investovator, Stock Market Gaming Framework
  *     Copyright (C) 2013  investovator
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package org.investovator.dataPlayBackEngine.utils;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class DateUtils {
 
     public static String DATE_FORMAT_1="yyyy-MM-dd-kk-mm-ss";
 
     /**
      * Increments a date by a given number of seconds and returns a new date
      *
      * @param seconds  number of seconds to increment the time by
      * @param currentTime the time value that needs to be incremented
      * @return incremented time
      */
     public static Date incrementTimeBySeconds(int seconds, Date currentTime){
         Calendar cal = Calendar.getInstance();
         cal.setTime(currentTime);
         cal.add(Calendar.SECOND, seconds); //minus number would decrement the days
         return cal.getTime();
 
     }
 
     public static Date dateStringToDateObject(String dateString,String dateFromat) throws ParseException {
         Date date;
 
         //should be in format year-month-date-24hr-minute-second
         SimpleDateFormat format = new SimpleDateFormat(dateFromat);
 
         date =format.parse(dateString);
 
 
         return date;
 
     }
 }
