 package org.nohope.typetools;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Period;
 
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 /**
  * Date: 21.05.12
  * Time: 13:19
  */
 public final class TTime {
     private TTime() {
     }
 
     public static int deltaInSeconds(final DateTime ts1, final DateTime ts2) {
         final Period delta = new Period(ts1, ts2);
         final int deltaSec = delta.toStandardSeconds().getSeconds();
         return Math.abs(deltaSec);
     }
 
     public static void setUtcTimezone() {
         final DateTimeZone defaultZone = DateTimeZone.forID("UTC");
         DateTimeZone.setDefault(defaultZone);
         setDefaultTimezone("Etc/UTC");
     }
 
     private static void setDefaultTimezone(final String id) {
         TimeZone.setDefault(TimeZone.getTimeZone(id));
     }
 
     /**
     * @return {@link XMLGregorianCalendar XMLGregorianCalendar} representing current date in UTC timezone
     * @see #toXmlUtcCalendar(java.util.Date)
     */
    public static XMLGregorianCalendar xmlCalendarUtcNow() {
        return toXmlUtcCalendar(new Date());
    }

    /**
      * Creates {@link XMLGregorianCalendar XMLGregorianCalendar} for given date in {@code UTC} timezone.
     * @see #toXmlCalendar(java.util.Date, String)
      */
    public static XMLGregorianCalendar toXmlUtcCalendar(final Date date) {
        return toXmlCalendar(date, "UTC");
     }
 
     /**
     * Creates {@link XMLGregorianCalendar XMLGregorianCalendar} for given date in given timezone.
      */
    public static XMLGregorianCalendar toXmlCalendar(final Date date, final String timezoneId) {
         final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone(timezoneId));
         calendar.setTime(date);
 
         try {
             return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
         } catch (DatatypeConfigurationException e) {
             throw new IllegalStateException(e);
         }
     }
 }
