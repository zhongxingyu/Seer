 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.transform;
 
 
 import com.gooddata.Constants;
 import com.gooddata.util.DateUtil;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 /**
  * GoodData DATE transformations
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class DateArithmetics {
 
     private static Logger l = Logger.getLogger(DateArithmetics.class);
 
     private final DateTimeFormatter baseFmt = DateTimeFormat.forPattern(Constants.DEFAULT_DATE_FMT_STRING);
     private final DateTime base = baseFmt.parseDateTime("1900-01-01");
     private final DateTime today = new DateTime();
     private final String todayText = baseFmt.print(today);
 
     public String computeDateFact(String dateValue, String format) {
         String ret = "";
         if(dateValue != null && dateValue.trim().length()>0) {
             try {
                 DateTimeFormatter formatter = DateUtil.getDateFormatter(format, false);
                 DateTime dt = formatter.parseDateTime(dateValue);
                 Days ds = Days.daysBetween(base, dt);
                 ret = Integer.toString(ds.getDays() + 1);
             }
             catch (IllegalArgumentException e) {
                l.debug("Can't parse date "+dateValue);
             }
         }
         return ret;
     }
 
     public String today() {
         return todayText;
     }
 
     public String computeTimeFact(String dateValue, String format) {
         String ret = "";
         if(dateValue != null && dateValue.trim().length()>0) {
             try {
                 DateTimeFormatter formatter = DateUtil.getDateFormatter(format, true);
                 DateTime dt = formatter.parseDateTime(dateValue);
                 int  ts = dt.getSecondOfDay();
                 ret = Integer.toString(ts);
             }
             catch (IllegalArgumentException e) {
                 l.debug("Can't parse date "+dateValue);
             }
         }
         return ret;
     }
 
     public String computeTimeAttribute(String dateValue, String format) {
         String ret = "00";
         if(dateValue != null && dateValue.trim().length()>0) {
             try {
                 DateTimeFormatter formatter = DateUtil.getDateFormatter(format, true);
                 DateTime dt = formatter.parseDateTime(dateValue);
                 int  ts = dt.getSecondOfDay();
                 String scs = Integer.toString(ts);
                 ret = (scs.length()>1)?(scs):("0"+scs);
             }
             catch (IllegalArgumentException e) {
                 l.debug("Can't parse date "+dateValue);
             }
         }
         return ret;
     }
 
 }
