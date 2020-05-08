 package com.amee.domain;
 
 import com.amee.domain.profile.GCDate;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.joda.time.format.ISOPeriodFormat;
 
 import java.util.Date;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 public class StartEndDate extends GCDate {
 
    private static final DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();
 
     public StartEndDate() {
         this(new Date());
     }
 
     public StartEndDate(String dateStr) {
         super(dateStr);
     }
 
     public StartEndDate(Date date) {
         super(date.getTime());
     }
 
     protected long parseStr(String dateStr) {
         try {
             // Seconds are ignored and are not significant in this version of AMEE.
            return FMT.parseDateTime(dateStr).secondOfMinute().withMinimumValue().getMillis();
         } catch (IllegalArgumentException e) {
             throw e;
         }
     }
 
     protected void setDefaultDateStr() {
        this.dateStr = FMT.print(getTime());
     }
 
     protected long defaultDate() {
         return new DateTime().secondOfMinute().withMinimumValue().getMillis();
     }
 
     public StartEndDate plus(String duration) {
         Period period = ISOPeriodFormat.standard().parsePeriod(duration);
         DateTime thisPlusPeriod = new DateTime(getTime()).plus(period);
         return new StartEndDate(thisPlusPeriod.toDate());
     }
 
     public StartEndDate minus(String duration) {
         Period period = ISOPeriodFormat.standard().parsePeriod(duration);
         DateTime thisPlusPeriod = new DateTime(getTime()).minus(period);
         return new StartEndDate(thisPlusPeriod.toDate());
     }
 
     public static boolean validate(String dateStr) {
         try {
             FMT.parseDateTime(dateStr);
         } catch (IllegalArgumentException ex) {
             return false;
         }
         return true;
     }
 
     public static StartEndDate getStartOfMonthDate() {
         return new StartEndDate(new DateTime().dayOfMonth().withMinimumValue().millisOfDay().withMinimumValue().toDate());
     }
 }
