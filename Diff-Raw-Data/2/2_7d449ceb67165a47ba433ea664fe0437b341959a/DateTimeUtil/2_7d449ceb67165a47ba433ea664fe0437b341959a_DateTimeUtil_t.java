 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package edu.rpi.sss.util;
 
 import edu.rpi.cmt.timezones.Timezones;
 import edu.rpi.cmt.timezones.Timezones.TimezonesException;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 /** Date and time utilities
  *
  * @author Mike Douglass     douglm - rpi.edu
  *  @version 1.0
  */
 public class DateTimeUtil {
   private static final DateFormat isoDateFormat =
       new SimpleDateFormat("yyyyMMdd");
 
   private static final DateFormat rfcDateFormat =
     new SimpleDateFormat("yyyy'-'MM'-'dd");
 
   private static final DateFormat isoDateTimeFormat =
       new SimpleDateFormat("yyyyMMdd'T'HHmmss");
 
   private static final DateFormat rfcDateTimeFormat =
     new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
 
   private static final DateFormat isoDateTimeTZFormat =
       new SimpleDateFormat("yyyyMMdd'T'HHmmss");
 
   private static final DateFormat rfcDateTimeTZFormat =
       new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
 
   private static final DateFormat isoDateTimeUTCTZFormat =
       new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
 
 //  private static final DateFormat rfcDateTimeUTCTZFormat =
   //  new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");
 
   private static final DateFormat isoDateTimeUTCFormat =
       new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
 
   private static final DateFormat rfcDateTimeUTCFormat =
     new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");
 
   private static final DateFormat rfc822GMTFormat =
     new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
 
   static {
     isoDateTimeUTCFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
     isoDateTimeUTCFormat.setLenient(false);
 
     rfcDateTimeUTCFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
     rfcDateTimeUTCFormat.setLenient(false);
 
     rfc822GMTFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
   }
 
   /**
    * @author douglm
    *
    */
   public static class BadDateException extends Throwable {
     /**
      *
      */
     public BadDateException() {
       super("Bad date");
     }
 
     /**
      * @param msg
      */
     public BadDateException(final String msg) {
       super(msg);
     }
   }
 
   private DateTimeUtil() {
   }
 
   /**
    * @return Date value for yesterday.
    */
   public static Date yesterday() {
     Calendar cal = Calendar.getInstance();
     cal.add(Calendar.DATE, -1);
 
     return cal.getTime();
   }
 
   /** Turn Date into "yyyyMMdd"
    *
    * @param val date
    * @return String "yyyyMMdd"
    */
   public static String isoDate(final Date val) {
     synchronized (isoDateFormat) {
       try {
        isoDateFormat.setTimeZone(Timezones.getDefaultTz());
       } catch (TimezonesException tze) {
         throw new RuntimeException(tze);
       }
       return isoDateFormat.format(val);
     }
   }
 
   /** Turn Now into "yyyyMMdd"
    *
    * @return String "yyyyMMdd"
    */
   public static String isoDate() {
     return isoDate(new Date());
   }
 
   /** Turn Date into "yyyy-MM-dd"
    *
    * @param val date
    * @return String "yyyy-MM-dd"
    */
   public static String rfcDate(final Date val) {
     synchronized (rfcDateFormat) {
       return rfcDateFormat.format(val);
     }
   }
 
   /** Turn Now into "yyyy-MM-dd"
    *
    * @return String "yyyy-MM-dd"
    */
   public static String rfcDate() {
     return rfcDate(new Date());
   }
 
   /** Turn Date into "yyyyMMddTHHmmss"
    *
    * @param val date
    * @return String "yyyyMMddTHHmmss"
    */
   public static String isoDateTime(final Date val) {
     synchronized (isoDateTimeFormat) {
       try {
         isoDateTimeTZFormat.setTimeZone(Timezones.getDefaultTz());
       } catch (TimezonesException tze) {
         throw new RuntimeException(tze);
       }
       return isoDateTimeFormat.format(val);
     }
   }
 
   /** Get Now as "yyyyMMddTHHmmss"
    *
    * @return String "yyyyMMddTHHmmss"
    */
   public static String isoDateTime() {
     return  isoDateTime(new Date());
   }
 
   /** Turn Date into "yyyyMMddTHHmmss" for a given timezone
    *
    * @param val date
    * @param tz TimeZone
    * @return String "yyyyMMddTHHmmss"
    */
   public static String isoDateTime(final Date val, final TimeZone tz) {
     synchronized (isoDateTimeTZFormat) {
       isoDateTimeTZFormat.setTimeZone(tz);
       return isoDateTimeTZFormat.format(val);
     }
   }
 
   /** Turn Date into "yyyy-MM-ddTHH:mm:ss"
    *
    * @param val date
    * @return String "yyyy-MM-ddTHH:mm:ss"
    */
   public static String rfcDateTime(final Date val) {
     synchronized (rfcDateTimeFormat) {
       try {
         rfcDateTimeFormat.setTimeZone(Timezones.getDefaultTz());
       } catch (TimezonesException tze) {
         throw new RuntimeException(tze);
       }
       return rfcDateTimeFormat.format(val);
     }
   }
 
   /** Get Now as "yyyy-MM-ddTHH:mm:ss"
    *
    * @return String "yyyy-MM-ddTHH:mm:ss"
    */
   public static String rfcDateTime() {
     return  rfcDateTime(new Date());
   }
 
   /** Turn Date into "yyyy-MM-ddTHH:mm:ss" for a given timezone
    *
    * @param val date
    * @param tz TimeZone
    * @return String "yyyy-MM-ddTHH:mm:ss"
    */
   public static String rfcDateTime(final Date val, final TimeZone tz) {
     synchronized (rfcDateTimeTZFormat) {
       rfcDateTimeTZFormat.setTimeZone(tz);
       return rfcDateTimeTZFormat.format(val);
     }
   }
 
   /** Turn Date into "yyyyMMddTHHmmssZ"
    *
    * @param val date
    * @return String "yyyyMMddTHHmmssZ"
    */
   public static String isoDateTimeUTC(final Date val) {
     synchronized (isoDateTimeUTCFormat) {
       return isoDateTimeUTCFormat.format(val);
     }
   }
 
   /** Turn Date into "yyyy-MM-ddTHH:mm:ssZ"
    *
    * @param val date
    * @return String "yyyy-MM-ddTHH:mm:ssZ"
    */
   public static String rfcDateTimeUTC(final Date val) {
     synchronized (rfcDateTimeUTCFormat) {
       return rfcDateTimeUTCFormat.format(val);
     }
   }
 
   /** Turn Date into "???"
    *
    * @param val date
    * @return String "???"
    */
   public static String rfc822Date(final Date val) {
     synchronized (rfc822GMTFormat) {
       return rfc822GMTFormat.format(val);
     }
   }
 
   /** Get Date from "yyyyMMdd"
    *
    * @param val String "yyyyMMdd"
    * @return Date
    * @throws BadDateException
    */
   public static Date fromISODate(final String val) throws BadDateException {
     try {
       synchronized (isoDateFormat) {
         return isoDateFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Get Date from "yyyy-MM-dd"
    *
    * @param val String "yyyy-MM-dd"
    * @return Date
    * @throws BadDateException
    */
   public static Date fromRfcDate(final String val) throws BadDateException {
     try {
       synchronized (rfcDateFormat) {
         return rfcDateFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Get Date from "yyyyMMddThhmmss"
    *
    * @param val String "yyyyMMddThhmmss"
    * @return Date
    * @throws BadDateException
    */
   public static Date fromISODateTime(final String val) throws BadDateException {
     try {
       synchronized (isoDateTimeFormat) {
         return isoDateTimeFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Get Date from "yyyy-MM-ddThh:mm:ss"
    *
    * @param val String "yyyy-MM-ddThh:mm:ss"
    * @return Date
    * @throws BadDateException
    */
   public static Date fromRfcDateTime(final String val) throws BadDateException {
     try {
       synchronized (rfcDateTimeFormat) {
         return rfcDateTimeFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Get Date from "yyyyMMddThhmmss" with timezone
    *
    * @param val String "yyyyMMddThhmmss"
    * @param tz TimeZone
    * @return Date
    * @throws BadDateException
    */
   public static Date fromISODateTime(final String val,
                                      final TimeZone tz) throws BadDateException {
     try {
       synchronized (isoDateTimeTZFormat) {
         isoDateTimeTZFormat.setTimeZone(tz);
         return isoDateTimeTZFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Get Date from "yyyy-MM-ddThh:mm:ss" with timezone
    *
    * @param val String "yyyy-ddThh:mm:ss"
    * @param tz TimeZone
    * @return Date
    * @throws BadDateException
    */
   public static Date fromRfcDateTime(final String val,
                                      final TimeZone tz) throws BadDateException {
     try {
       synchronized (rfcDateTimeTZFormat) {
         rfcDateTimeTZFormat.setTimeZone(tz);
         return rfcDateTimeTZFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Get Date from "yyyyMMddThhmmssZ" with timezone
    *
    * @param val String "yyyyMMddThhmmssZ"
    * @param tz TimeZone
    * @return Date
    * @throws BadDateException
    */
   public static Date fromISODateTimeUTC(final String val,
                                         final TimeZone tz) throws BadDateException {
     try {
       synchronized (isoDateTimeUTCTZFormat) {
         isoDateTimeUTCTZFormat.setTimeZone(tz);
         return isoDateTimeUTCTZFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Get Date from "yyyyMMddThhmmssZ"
    *
    * @param val String "yyyyMMddThhmmssZ"
    * @return Date
    * @throws BadDateException
    */
   public static Date fromISODateTimeUTC(final String val) throws BadDateException {
     try {
       synchronized (isoDateTimeUTCFormat) {
         return isoDateTimeUTCFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Get Date from "yyyy-MM-ddThh:mm:ssZ"
    *
    * @param val String "yyyy-MM-ddThh:mm:ssZ"
    * @return Date
    * @throws BadDateException
    */
   public static Date fromRfcDateTimeUTC(final String val) throws BadDateException {
     try {
       synchronized (rfcDateTimeUTCFormat) {
         return rfcDateTimeUTCFormat.parse(val);
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Get RFC822 form from "yyyyMMddThhmmssZ"
    *
    * @param val String "yyyyMMddThhmmssZ"
    * @return Date
    * @throws BadDateException
    */
   public static String fromISODateTimeUTCtoRfc822(final String val) throws BadDateException {
     try {
       synchronized (isoDateTimeUTCFormat) {
         return rfc822Date(isoDateTimeUTCFormat.parse(val));
       }
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Check Date is "yyyyMMdd"
    *
    * @param val String to check
    * @return boolean
    * @throws BadDateException
    */
   public static boolean isISODate(final String val) throws BadDateException {
     try {
       if (val.length() != 8) {
         return false;
       }
       fromISODate(val);
       return true;
     } catch (Throwable t) {
       return false;
     }
   }
 
   /** Check Date is "yyyyMMddThhmmddZ"
    *
    * @param val String to check
    * @return boolean
    * @throws BadDateException
    */
   public static boolean isISODateTimeUTC(final String val) throws BadDateException {
     try {
       if (val.length() != 16) {
         return false;
       }
       fromISODateTimeUTC(val);
       return true;
     } catch (Throwable t) {
       return false;
     }
   }
 
   /** Check Date is "yyyyMMddThhmmdd"
    *
    * @param val String to check
    * @return boolean
    * @throws BadDateException
    */
   public static boolean isISODateTime(final String val) throws BadDateException {
     try {
       if (val.length() != 15) {
         return false;
       }
       fromISODateTime(val);
       return true;
     } catch (Throwable t) {
       return false;
     }
   }
 
   /** Return rfc or iso String date or datetime as java Date
    *
    * @param dt
    * @return Date
    * @throws BadDateException
    */
   public static Date fromDate(final String dt) throws BadDateException {
     try {
       if (dt == null) {
         return null;
       }
 
       if (dt.indexOf("T") > 0) {
         return fromDateTime(dt);
       }
 
       if (dt.indexOf("-") < 0) {
         return fromISODate(dt);
       }
 
       return fromRfcDate(dt);
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 
   /** Return rfc or iso String datetime as java Date
    *
    * @param dt
    * @return Date
    * @throws BadDateException
    */
   public static Date fromDateTime(final String dt) throws BadDateException {
     try {
       if (dt == null) {
         return null;
       }
 
       if (dt.indexOf("-") < 0) {
         return fromISODateTimeUTC(dt);
       }
 
       return fromRfcDateTimeUTC(dt);
     } catch (Throwable t) {
       throw new BadDateException();
     }
   }
 }
