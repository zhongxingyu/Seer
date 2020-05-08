 package org.hackystat.utilities.tstamp;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.junit.Test;
 
 /**
  * Tests the Tstamp class. 
  * @author Philip Johnson
  */
 public class TestTstamp {
   /**
    * Tests the Tstamp class.
    * @throws Exception if problems occur. 
    */
   @Test public void testTstampArithmetic () throws Exception {
     XMLGregorianCalendar date1 = Tstamp.makeTimestamp("2007-08-01");
     XMLGregorianCalendar date2 = Tstamp.makeTimestamp("2007-08-02");
     XMLGregorianCalendar date3 = Tstamp.makeTimestamp("2007-08-03");
     assertTrue("Test equal", Tstamp.equal(date1, date1));
     assertTrue("Test increment days", Tstamp.equal(date2, Tstamp.incrementDays(date1, 1)));
     
     XMLGregorianCalendar date4 = Tstamp.makeTimestamp("2007-08-01T01:00:00");
     assertTrue("Test increment hours", Tstamp.equal(date4, Tstamp.incrementHours(date1, 1)));
     
     XMLGregorianCalendar date5 = Tstamp.makeTimestamp("2007-08-01T00:01:00");
     assertTrue("Test increment mins", Tstamp.equal(date5, Tstamp.incrementMinutes(date1, 1)));
     
     XMLGregorianCalendar date6 = Tstamp.makeTimestamp("2007-08-01T00:00:01");
     assertTrue("Test increment secs", Tstamp.equal(date6, Tstamp.incrementSeconds(date1, 1)));
 
     assertTrue("Test greater than 1", Tstamp.greaterThan(date2, date1));
     assertFalse("Test greater than 2", Tstamp.greaterThan(date1, date2));
     assertFalse("Test greater than 3", Tstamp.greaterThan(date1, date1));
     
     assertTrue("Test less than 1", Tstamp.lessThan(date1, date2));
     assertFalse("Test less than 2", Tstamp.lessThan(date2, date1));
     assertFalse("Test less than 3", Tstamp.lessThan(date1, date1));   
     
     assertTrue("Test inbetween 1", Tstamp.inBetween(date1, date2, date3));
     assertTrue("Test inbetween 2", Tstamp.inBetween(date1, date1, date2));
     assertTrue("Test inbetween 3", Tstamp.inBetween(date1, date2, date2));    
     assertFalse("Test inbetween 4", Tstamp.inBetween(date1, date3, date2));   
     
     assertFalse("Test bogus time check", Tstamp.isBogusStartTime(date1));
   }
   
   /**
    * Tests time span. 
    * 
    * @throws Exception If problem occurs
    */
   @Test public void testTstampSpan() throws Exception {
     XMLGregorianCalendar date1 = Tstamp.makeTimestamp("2007-08-01T01:00:00.000");
     XMLGregorianCalendar date2 = Tstamp.makeTimestamp("2007-08-01T01:01:20.200");
     XMLGregorianCalendar date3 = Tstamp.makeTimestamp("2007-08-01T01:01:20.400");
     assertEquals("Test timespan 80200 ms", 80200, Tstamp.diff(date1, date2));
     assertEquals("Test timespan 200 ms", 200, Tstamp.diff(date2, date3));
   }
   
   /**
    * Test to make sure that the is* methods work OK. 
    * @throws Exception If problems occur. 
    */
   @Test public void testOrLater() throws Exception {
     long millisInADay = 1000 * 60 * 60 * 24;
     XMLGregorianCalendar today = Tstamp.makeTimestamp();
     long todayInMillis = today.toGregorianCalendar().getTimeInMillis();
     long yesterdayInMillis = todayInMillis - millisInADay;
     XMLGregorianCalendar yesterday = Tstamp.makeTimestamp(yesterdayInMillis);
     long tomorrowInMillis = todayInMillis + millisInADay;
     XMLGregorianCalendar tomorrow = Tstamp.makeTimestamp(tomorrowInMillis);
     long lastWeekInMillis = todayInMillis - (millisInADay * 7);
     XMLGregorianCalendar lastWeek = Tstamp.makeTimestamp(lastWeekInMillis);
     assertTrue("Testing todayOrLater 1", Tstamp.isTodayOrLater(today));
     assertTrue("Testing todayOrLater 2", Tstamp.isTodayOrLater(tomorrow));
     assertFalse("Testing todayOrLater 3", Tstamp.isTodayOrLater(yesterday));
     assertTrue("Testing yesterdayOrLater 1", Tstamp.isYesterdayOrLater(today));
     assertTrue("Testing yesterdayOrLater 2", Tstamp.isYesterdayOrLater(tomorrow));
     assertTrue("Testing yesterdayOrLater 3", Tstamp.isYesterdayOrLater(yesterday));
     assertFalse("Testing yesterdayOrLater 4", Tstamp.isYesterdayOrLater(lastWeek));
   }
   
   /**
    * Test to make sure that the daysBetween method works correctly. 
    * @throws Exception If problems occur. 
    */
   @Test public void testDaysBetween() throws Exception {
     XMLGregorianCalendar today = Tstamp.makeTimestamp();
     assertEquals("Test daysBetween 1", 0, Tstamp.daysBetween(today, today));
     XMLGregorianCalendar tomorrow = Tstamp.incrementDays(today, 1);
     assertEquals("Test daysBetween 2", 1, Tstamp.daysBetween(today, tomorrow));
     XMLGregorianCalendar nextWeek = Tstamp.incrementDays(today, 7);
     assertEquals("Test daysBetween 3", 7, Tstamp.daysBetween(today, nextWeek));
     // Test a whole year, which might find DST issues.
     for (int i = 1; i <= 365; i++) {
       XMLGregorianCalendar newDay = Tstamp.incrementDays(today, i);
       assertEquals("Test daysBetween 4", i, Tstamp.daysBetween(today, newDay));
     }
   }
 
   /**
    * Tests that the sort() method works. 
    * @throws Exception If problems occur. 
    */
   @Test public void testSorting() throws Exception {
     XMLGregorianCalendar tstamp1 = Tstamp.makeTimestamp();
    Thread.sleep(10);
    XMLGregorianCalendar tstamp2 = Tstamp.makeTimestamp();
    Thread.sleep(10);
    XMLGregorianCalendar tstamp3 = Tstamp.makeTimestamp();
    Thread.sleep(10);
    XMLGregorianCalendar tstamp4 = Tstamp.makeTimestamp();
     List<XMLGregorianCalendar> tstamps = new ArrayList<XMLGregorianCalendar>();
     tstamps.add(tstamp2);
     tstamps.add(tstamp1);
     tstamps.add(tstamp4);
     tstamps.add(tstamp3);
    Tstamp.sort(tstamps);
    assertEquals("Test sort1", tstamp1, tstamps.get(0));
    assertEquals("Test sort2", tstamp2, tstamps.get(1));
    assertEquals("Test sort3", tstamp3, tstamps.get(2));
    assertEquals("Test sort4", tstamp4, tstamps.get(3));
   }
   
 
 }
