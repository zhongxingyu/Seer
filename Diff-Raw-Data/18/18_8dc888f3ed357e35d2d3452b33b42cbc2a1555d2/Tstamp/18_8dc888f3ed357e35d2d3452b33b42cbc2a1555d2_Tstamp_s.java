 package org.hackystat.utilities.tstamp;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.utilities.time.period.Day;
 
 /**
  * Utility class that facilitates Timestamp representation and processing. There
  * are too many classes already named "Timestamp", thus the abbreviated name.
  * @author Philip Johnson
  */
 public final class Tstamp {
   
   /** Make this class noninstantiable. */
   private Tstamp() {
     // Do nothing.
   }
 
   private static final String factoryErrorMsg = "Bad DataTypeFactory";
   
   private static long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;
   
 
   /**
    * Returns true if the passed string can be parsed into an
    * XMLGregorianCalendar object.
    * @param lexicalRepresentation The string representation.
    * @return True if the string is a legal XMLGregorianCalendar.
    */
   public static boolean isTimestamp(String lexicalRepresentation) {
     try {
       DatatypeFactory factory = DatatypeFactory.newInstance();
       factory.newXMLGregorianCalendar(lexicalRepresentation);
       return true;
 
     }
     catch (Exception e) {
       return false;
     }
   }
 
   /**
    * Returns an XMLGregorianCalendar, given its string representation.
    * Missing hours, minutes, second, millisecond, and timezone fields are given defaults.
    * @param rep The string representation.
    * @return The timestamp.
    * @throws Exception If the string cannot be parsed into a timestamp.
    */
   public static XMLGregorianCalendar makeTimestamp(String rep)
       throws Exception {
     DatatypeFactory factory = DatatypeFactory.newInstance();
     long mills = factory.newXMLGregorianCalendar(rep).toGregorianCalendar().getTimeInMillis();  
     return makeTimestamp(mills);
   }
 
   /**
    * Converts a javax.sql.Timestamp into a
    * javax.xml.datatype.XMLGregorianCalendar.
    * @param tstamp The javax.sql.Timestamp
    * @return A new instance of a javax.xml.datatype.XmlGregorianCalendar
    */
   public static XMLGregorianCalendar makeTimestamp(java.sql.Timestamp tstamp) {
     DatatypeFactory factory = null;
     try {
       factory = DatatypeFactory.newInstance();
       GregorianCalendar calendar = new GregorianCalendar();
       calendar.setTimeInMillis(tstamp.getTime());
       return factory.newXMLGregorianCalendar(calendar);
     }
     catch (DatatypeConfigurationException e) {
       throw new RuntimeException(factoryErrorMsg, e);
     }
   }
 
   /**
    * Converts the specified time in milliseconds into a
    * javax.xml.datatype.XMLGregorianCalendar.
    * @param timeInMillis the specified time in milliseconds to convert.
    * @return A new instance of a javax.xml.datatype.XmlGregorianCalendar
    */
   public static XMLGregorianCalendar makeTimestamp(long timeInMillis) {
     DatatypeFactory factory = null;
     try {
       factory = DatatypeFactory.newInstance();
       GregorianCalendar calendar = new GregorianCalendar();
       calendar.setTimeInMillis(timeInMillis);
       return factory.newXMLGregorianCalendar(calendar);
     }
     catch (DatatypeConfigurationException e) {
       throw new RuntimeException(factoryErrorMsg, e);
     }
   }
   
   /**
    * Converts the specified Day into a javax.xml.datatype.XMLGregorianCalendar.
    * @param day The day to be converted.
    * @return A new instance of a javax.xml.datatype.XmlGregorianCalendar.
    */
   public static XMLGregorianCalendar makeTimestamp(Day day) {
     DatatypeFactory factory = null;
     try {
       factory = DatatypeFactory.newInstance();
       GregorianCalendar calendar = new GregorianCalendar();
       calendar.setTimeInMillis(day.getDate().getTime());
       return factory.newXMLGregorianCalendar(calendar);
     }
     catch (DatatypeConfigurationException e) {
       throw new RuntimeException(factoryErrorMsg, e);
     }
   }
 
   /**
    * Returns a new XMLGregorianCalendar corresponding to the passed tstamp
    * incremented by the number of days.
    * @param tstamp The base date and time.
    * @param days The number of days to increment. This can be a negative number.
    * @return A new XMLGregorianCalendar instance representing the inc'd time.
    */
   public static XMLGregorianCalendar incrementDays(XMLGregorianCalendar tstamp, int days) {
     DatatypeFactory factory = null;
     try {
       factory = DatatypeFactory.newInstance();
       GregorianCalendar calendar = new GregorianCalendar();
       long millis = tstamp.toGregorianCalendar().getTimeInMillis();
       millis += 1000L * 60 * 60 * 24 * days;
       calendar.setTimeInMillis(millis);
       return factory.newXMLGregorianCalendar(calendar);
     }
     catch (DatatypeConfigurationException e) {
       throw new RuntimeException(factoryErrorMsg, e);
     }
   }
 
   /**
    * Returns a new XMLGregorianCalendar corresponding to the passed tstamp
    * incremented by the number of hours.
    * @param tstamp The base date and time.
    * @param hours The number of hours to increment. This can be a negative
    * number.
    * @return A new XMLGregorianCalendar instance representing the inc'd time.
    */
   public static XMLGregorianCalendar incrementHours(XMLGregorianCalendar tstamp, int hours) {
     DatatypeFactory factory = null;
     try {
       factory = DatatypeFactory.newInstance();
       GregorianCalendar calendar = new GregorianCalendar();
       long millis = tstamp.toGregorianCalendar().getTimeInMillis();
       millis += 1000L * 60 * 60 * hours;
       calendar.setTimeInMillis(millis);
       return factory.newXMLGregorianCalendar(calendar);
     }
     catch (DatatypeConfigurationException e) {
       throw new RuntimeException(factoryErrorMsg, e);
     }
   }
 
   /**
    * Returns a new XMLGregorianCalendar corresponding to the passed tstamp
    * incremented by the number of minutes.
    * @param tstamp The base date and time.
    * @param minutes The number of minutes to increment. This can be a negative
    * number.
    * @return A new XMLGregorianCalendar instance representing the inc'd time.
    */
   public static XMLGregorianCalendar incrementMinutes(XMLGregorianCalendar tstamp, int minutes) {
     DatatypeFactory factory = null;
     try {
       factory = DatatypeFactory.newInstance();
       GregorianCalendar calendar = new GregorianCalendar();
       long millis = tstamp.toGregorianCalendar().getTimeInMillis();
       millis += 1000L * 60 * minutes;
       calendar.setTimeInMillis(millis);
       return factory.newXMLGregorianCalendar(calendar);
     }
     catch (DatatypeConfigurationException e) {
       throw new RuntimeException(factoryErrorMsg, e);
     }
   }
 
   /**
    * Returns a new XMLGregorianCalendar corresponding to the passed tstamp
    * incremented by the number of seconds.
    * @param tstamp The base date and time.
    * @param seconds The number of seconds to increment. This can be a negative
    * number.
    * @return A new XMLGregorianCalendar instance representing the inc'd time.
    */
   public static XMLGregorianCalendar incrementSeconds(XMLGregorianCalendar tstamp, int seconds) {
     DatatypeFactory factory = null;
     try {
       factory = DatatypeFactory.newInstance();
       GregorianCalendar calendar = new GregorianCalendar();
       long millis = tstamp.toGregorianCalendar().getTimeInMillis();
       millis += 1000L * seconds;
       calendar.setTimeInMillis(millis);
       return factory.newXMLGregorianCalendar(calendar);
     }
     catch (DatatypeConfigurationException e) {
       throw new RuntimeException(factoryErrorMsg, e);
     }
   }
   
   /**
    * Returns a new XMLGregorianCalendar corresponding to the passed tstamp
    * incremented by the number of milliseconds.
    * @param tstamp The base date and time.
    * @param milliseconds The number of milliseconds to increment. This can be a negative
    * number.
    * @return A new XMLGregorianCalendar instance representing the inc'd time.
    */
   public static XMLGregorianCalendar incrementMilliseconds(XMLGregorianCalendar tstamp, 
       long milliseconds) {
     DatatypeFactory factory = null;
     try {
       factory = DatatypeFactory.newInstance();
       GregorianCalendar calendar = new GregorianCalendar();
       long millis = tstamp.toGregorianCalendar().getTimeInMillis();
       millis += milliseconds;
       calendar.setTimeInMillis(millis);
       return factory.newXMLGregorianCalendar(calendar);
     }
     catch (DatatypeConfigurationException e) {
       throw new RuntimeException(factoryErrorMsg, e);
     }
   }
 
   /**
    * Returns a new java.sql.Timestamp created from a
    * javax.xml.datatype.XMLGregorianCalendar.
    * @param calendar The XML timestamp.
    * @return The SQL timestamp.
    */
   public static java.sql.Timestamp makeTimestamp(XMLGregorianCalendar calendar) {
     return new java.sql.Timestamp(calendar.toGregorianCalendar().getTimeInMillis());
   }
 
   /**
    * Returns an XMLGregorianCalendar corresponding to the current time.
    * @return The timestamp.
    */
   public static XMLGregorianCalendar makeTimestamp() {
     try {
       DatatypeFactory factory = DatatypeFactory.newInstance();
       return factory.newXMLGregorianCalendar(new GregorianCalendar());
     }
     catch (Exception e) {
       throw new RuntimeException(factoryErrorMsg, e);
     }
   }
 
   /**
   * Returns an XMLGregorianCalendar corresponding to today's date.
   * @return A timestamp representing today's date. 
    */
   public static XMLGregorianCalendar getDefaultProjectStartTime() {
     XMLGregorianCalendar tstamp = Tstamp.makeTimestamp();
    tstamp.setTime(0, 0, 0);
     return tstamp;
   }
 
   /**
   * Returns an XMLGregorianCalendar corresponding to one year after the default start time.
   * @return The timestamp.
    */
   public static XMLGregorianCalendar getDefaultProjectEndTime() {
    return Tstamp.incrementDays(Tstamp.getDefaultProjectStartTime(), 365);
   }
 
   
   /**
    * In the early days of Hackystat, default project start times were 1000-01-01.
    * This was stupid.  The following hack exists to correct projects containing this old 
    * value.  This code and its callers can be removed when the disease is eradicated.
    * @param startTime The startTime in question.
    * @return True if it's before 1950.
    */
   public static boolean isBogusStartTime(XMLGregorianCalendar startTime) {
     try {
       XMLGregorianCalendar bogusTime = Tstamp.makeTimestamp("1950-01-01");
       return Tstamp.lessThan(startTime, bogusTime);
     }
     catch (Exception e) {
       return true;
     }
   }
 
   /**
    * Returns true if tstamp is equal to or between start and end.
    * @param start The start time.
    * @param tstamp The timestamp to test.
    * @param end The end time.
    * @return True if tstamp is between start and end.
    */
   public static boolean inBetween(XMLGregorianCalendar start, XMLGregorianCalendar tstamp,
       XMLGregorianCalendar end) {
     long startMillis = start.toGregorianCalendar().getTimeInMillis();
     long endMillis = end.toGregorianCalendar().getTimeInMillis();
     long tstampMillis = tstamp.toGregorianCalendar().getTimeInMillis();
     return ((tstampMillis >= startMillis) && (tstampMillis <= endMillis));
   }
 
   /**
    * Returns true if time1 > time2.
    * @param time1 The first time.
    * @param time2 The second time.
    * @return True if time1 > time2
    */
   public static boolean greaterThan(XMLGregorianCalendar time1, XMLGregorianCalendar time2) {
     long time1Millis = time1.toGregorianCalendar().getTimeInMillis();
     long time2Millis = time2.toGregorianCalendar().getTimeInMillis();
     return (time1Millis > time2Millis);
   }
   
   /**
    * Returns the number of days between time1 and time2.
    * Returns a negative number if day1 is after day2.
    * Takes into account daylight savings time issues. 
    * @param day1 The first day.
    * @param day2 The second day. 
    * @return The number of days between the two days. 
    */
   public static int daysBetween(XMLGregorianCalendar day1, XMLGregorianCalendar day2) {
     return (int) (getUnixDay(day2) - getUnixDay(day1));
   }
   
   /**
    * Returns a long representing the number of days since the Unix epoch to the passed day.
    * @param day The day of interest.
    * @return The number of days since the epoch.
    */
   private static long getUnixDay(XMLGregorianCalendar day) {
     GregorianCalendar greg = day.toGregorianCalendar();
     long offset = greg.get(Calendar.ZONE_OFFSET) + greg.get(Calendar.DST_OFFSET);
     long daysSinceEpoch = (long)Math.floor((double)(greg.getTime().getTime() + offset ) / 
         ((double)MILLISECS_PER_DAY) );
     return daysSinceEpoch;
 }
  
 
   /**
    * Returns true if timeString1 > timeString2. Throws an unchecked
    * IllegalArgument exception if the strings can't be converted to timestamps.
    * @param timeString1 The first time.
    * @param timeString2 The second time.
    * @return True if time1 > time2
    */
   public static boolean greaterThan(String timeString1, String timeString2) {
     try {
       DatatypeFactory factory = DatatypeFactory.newInstance();
       XMLGregorianCalendar time1 = factory.newXMLGregorianCalendar(timeString1);
       XMLGregorianCalendar time2 = factory.newXMLGregorianCalendar(timeString2);
       return greaterThan(time1, time2);
     }
     catch (Exception e) {
       throw new IllegalArgumentException("Illegal timestring", e);
     }
   }
 
   /**
    * Returns true if time1 < time2.
    * @param time1 The first time.
    * @param time2 The second time.
    * @return True if time1 < time2
    */
   public static boolean lessThan(XMLGregorianCalendar time1, XMLGregorianCalendar time2) {
     long time1Millis = time1.toGregorianCalendar().getTimeInMillis();
     long time2Millis = time2.toGregorianCalendar().getTimeInMillis();
     return (time1Millis < time2Millis);
   }
 
   /**
    * Returns true if time1 equals time2. 
    * @param time1 The first time.
    * @param time2 The second time.
    * @return True if time1 equals time2
    */
   public static boolean equal(XMLGregorianCalendar time1, XMLGregorianCalendar time2) {
     long millis1 = time1.toGregorianCalendar().getTimeInMillis();
     long millis2 = time2.toGregorianCalendar().getTimeInMillis();
     return (millis1 == millis2);
   }
 
   /**
    * Returns differences between time1 and time2 in milliseconds.
    * 
    * @param time1 Start.
    * @param time2 End.
    * @return Difference between two times in milliseconds.
    */
   public static long diff(XMLGregorianCalendar time1, XMLGregorianCalendar time2) {
     long millis1 = time1.toGregorianCalendar().getTimeInMillis();
     long millis2 = time2.toGregorianCalendar().getTimeInMillis();
     return millis2 - millis1;
   }
   
   /**
    * Returns true if the passed timestamp indicates some time today or some time 
    * in the future. 
    * 
    * @param timestamp The timestamp of interest.
    * @return True if it's today or some day in the future. 
    */
   public static boolean isTodayOrLater(XMLGregorianCalendar timestamp) {
     XMLGregorianCalendar today = Tstamp.makeTimestamp();
     boolean isToday = (today.getYear() == timestamp.getYear()) && 
                       (today.getMonth() == timestamp.getMonth()) && 
                       (today.getDay() == timestamp.getDay());
     boolean afterToday = today.toGregorianCalendar().getTimeInMillis() <
                        timestamp.toGregorianCalendar().getTimeInMillis();
     return (isToday || afterToday);
   }
   
   /**
    * Returns true if the passed timestamp indicates some time yesterday or some time 
    * in the future. This is useful for the Commit/Churn DPDs, where the sensor typically
    * sends data not from the current day, but from the day before. 
    * 
    * @param timestamp The timestamp of interest.
    * @return True if it's today or some day in the future. 
    */
   public static boolean isYesterdayOrLater(XMLGregorianCalendar timestamp) {
     XMLGregorianCalendar yesterday = Tstamp.incrementDays(Tstamp.makeTimestamp(), -1);
 
     boolean isYesterday = (yesterday.getYear() == timestamp.getYear()) && 
                           (yesterday.getMonth() == timestamp.getMonth()) && 
                           (yesterday.getDay() == timestamp.getDay());
     boolean afterYesterday = yesterday.toGregorianCalendar().getTimeInMillis() <
                              timestamp.toGregorianCalendar().getTimeInMillis();
     return (isYesterday || afterYesterday);
   }
   
   /**
    * Returns a newly created sorted list of tstamps from the passed collection.
    * @param tstamps The timestamps to be sorted. 
    * @return A new list of tstamps, now in sorted order. 
    */
   public static List<XMLGregorianCalendar> sort(Collection<XMLGregorianCalendar> tstamps) {
     List<XMLGregorianCalendar> tstampList = new ArrayList<XMLGregorianCalendar>(tstamps);
     Collections.sort(tstampList, new TstampComparator());
     return tstampList;
   }
 }
