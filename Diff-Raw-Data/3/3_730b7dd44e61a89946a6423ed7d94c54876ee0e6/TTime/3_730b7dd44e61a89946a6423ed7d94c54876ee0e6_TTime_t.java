 package org.nohope.typetools;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Period;
 
 import java.util.TimeZone;
 
 /**
  * Date: 21.05.12
  * Time: 13:19
  */
 public class TTime {
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
 
     public static void setDefaultTimezone(final String Id) {
         TimeZone.setDefault(TimeZone.getTimeZone(Id));
     }
 }
