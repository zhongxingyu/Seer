 package org.jcommons.lang.time;
 
 import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
 import static org.jcommons.lang.time.DateUtils.toCurrentCentury;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Date;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 /**
  * Tests for <code>DateUtils</code> helper class.
  * 
  * @author Thorsten Goeckeler
  */
 public class DateUtilsTest
 {
   /** Test method for {@link DateUtils#toDay(String)}. */
   @Test
   public void testToDay() {
     DateTime date = new DateTime(2010, 03, 15, 0, 0, 0, 0);
     assertTrue(date.isEqual(DateUtils.toDay("15.03.2010").getTime()));
     assertTrue(date.isEqual(DateUtils.toDay("15.03.10").getTime()));
     assertTrue(date.isEqual(DateUtils.toDay("2010-03-15").getTime()));
     assertTrue(date.isEqual(DateUtils.toDay("15.03.2010 14:15").getTime()));
 
     assertNull(DateUtils.toDay(null));
     assertNull(DateUtils.toDay("01.08."));
   }
 
   /** Test method for {@link DateUtils#toTime(String)}. */
   @Test
   public void testToTime() {
     DateTime date = new DateTime(2010, 03, 15, 14, 15, 45, 0);
     assertTrue(date.isEqual(DateUtils.toTime("15.03.2010 14:15:45").getTime()));
     assertTrue(date.isEqual(DateUtils.toTime("15.03.10 14:15:45").getTime()));
 
     date = date.minusSeconds(45);
     assertTrue(date.isEqual(DateUtils.toTime("2010-03-15 14:15").getTime()));
     assertTrue(date.isEqual(DateUtils.toTime("15.03.2010 14:15").getTime()));
     // without time component it shall return null so we know the difference between time and day
     assertNull(DateUtils.toTime("15.03.2010"));
    assertTrue(date.toLocalDate().toDateTimeAtStartOfDay().isEqual(DateUtils.toTime("15.03.2010 00:00").getTime()));
 
     assertNull(DateUtils.toTime(null));
     assertNull(DateUtils.toTime("01.08."));
   }
 
   /** Test method for {@link DateUtils#toCurrentCentury(java.util.Date)}. */
   @Test
   public void testToCurrentCentury() {
     // this is March 15th, 10 (!)
     DateTime lenientDate = new DateTime(10, 03, 13, 0, 0, 0, 0);
     DateTime date = new DateTime(2010, 03, 15, 0, 0, 0, 0);
 
     // System.out.println(ISO_DATE_FORMAT.format(lenientDate.toDate()));
     // System.out.println(ISO_DATE_FORMAT.format(date.toDate()));
     // System.out.println(ISO_DATE_FORMAT.format(toCurrentCentury(lenientDate.toDate())));
     // System.out.println(ISO_DATE_FORMAT.format(toCurrentCentury(date.toDate())));
 
     assertEquals(ISO_DATE_FORMAT.format(date.toDate()), ISO_DATE_FORMAT.format(toCurrentCentury(lenientDate.toDate())));
 
     lenientDate = new DateTime(95, 03, 13, 0, 0, 0, 0);
     date = new DateTime(1995, 03, 15, 0, 0, 0, 0);
     assertEquals(ISO_DATE_FORMAT.format(date.toDate()), ISO_DATE_FORMAT.format(toCurrentCentury(lenientDate.toDate())));
   }
 
   /** Test method for {@link DateUtils#compare(java.util.Date, java.util.Date)}. */
   @Test
   public void testCompare() {
     assertEquals(0, DateUtils.compare(null, null));
     Date lhs = new Date();
     assertTrue(DateUtils.compare(lhs, null) > 0);
     assertTrue(DateUtils.compare(null, lhs) < 0);
     assertEquals(0, DateUtils.compare(lhs, lhs));
     Date rhs = new Date();
     rhs.setTime(rhs.getTime() + 20);
     assertTrue(DateUtils.compare(lhs, rhs) < 0);
     assertTrue(DateUtils.compare(rhs, lhs) > 0);
   }
 }
