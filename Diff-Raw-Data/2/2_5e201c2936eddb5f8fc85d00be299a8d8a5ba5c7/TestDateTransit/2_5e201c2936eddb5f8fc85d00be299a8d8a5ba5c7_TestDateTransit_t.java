 /*
  * Created on 5 Apr 2007
  */
 package uk.org.ponder.test.dateutil;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import uk.org.ponder.dateutil.StandardFieldDateTransit;
 import uk.org.ponder.util.UniversalRuntimeException;
 import junit.framework.TestCase;
 
 public class TestDateTransit extends TestCase {
   public void testTZTransit() {
     StandardFieldDateTransit transit = new StandardFieldDateTransit();
     TimeZone offtz = TimeZone.getTimeZone("BST");
     transit.setTimeZone(offtz);
     transit.init();
     Calendar cal = new GregorianCalendar();
     cal.set(2007, 4, 5, 1, 0, 0);
     cal.set(Calendar.MILLISECOND, 0);
     Date caltime = cal.getTime();
    //caltime.setTimeZone(offtz);
     transit.setDate(cal.getTime());
     String tz8601 = transit.getISO8601TZ();
     assertEquals(tz8601, "2007-05-05T01:00:00.000+0100");
     // Test that transit correctly ignores any timezone strewn in by a 
     // client-side knowlessman
     tz8601 = tz8601.substring(0, 23) + "+0200";
     try {
       transit.setISO8601TZ(tz8601);
     }
     catch (Exception e) {
       throw UniversalRuntimeException.accumulate(e, "Error parsing date " + tz8601);
     }
     assertEquals(transit.getDate(), cal.getTime());
   }
 }
