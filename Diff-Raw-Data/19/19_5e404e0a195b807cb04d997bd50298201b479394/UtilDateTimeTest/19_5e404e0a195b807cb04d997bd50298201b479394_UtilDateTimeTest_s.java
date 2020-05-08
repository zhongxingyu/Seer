 package org.social.util;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.social.core.util.UtilDateTime;
 
 public class UtilDateTimeTest {
 
     @Before
     public void setUp() throws Exception {
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void testConvertToTimestamp_FbTimeFormat() throws Exception {
         // 2011-05-10T18:35:38+0000
 
         // +0000 means no offset to UTC. Germany has an offset from +0200 -->
         // +2h
         String tsString = "2011-05-10T18:35:38+0000";
         Timestamp ts = UtilDateTime.toTimestamp(tsString);
         assertEquals("2011-05-10 20:35:38.0", ts.toString());
     }
 
     @Test
     public void testConvertToTimestamp_TwitterTimeFormat() throws Exception {
         // Wed, 19 Jan 2011 20:21:46 +0000
         String tsString = "Wed, 19 Jan 2011 20:21:46 +0000";
         Timestamp ts = UtilDateTime.toTimestamp(tsString);
         assertEquals("2011-01-19 21:21:46.0", ts.toString());
     }
 
     @Test
     public void testConvertToTimestamp_QypeTimeFormat() throws Exception {
         // 2012-07-27T22:13:03+02:00
         String tsString = "2012-07-27T22:13:03+02:00";
         Timestamp ts = UtilDateTime.toTimestamp(tsString);
 
         assertEquals("2012-07-27 22:13:03.0", ts.toString());
     }
 
     @Test
     public void testConvertToTimestamp_FoursquareTimeFormat() throws Exception {
         // 1341085680
         String tsString = "1341085680";
         Timestamp ts = UtilDateTime.toTimestamp(tsString);
 
         assertEquals("2012-06-30 21:48:00.0", ts.toString());
     }
 
     @Test
     public void testConvertToTimestamp_TribAdvisorTimeFormat() throws Exception {
         // Reviewed July 24, 2012
         String tsString = "Reviewed July 24, 2012";
         Timestamp ts = UtilDateTime.toTimestamp(tsString);
         assertEquals("2012-07-24 23:59:59.0", ts.toString());
 
         tsString = "Reviewed December 24, 2012";
         ts = UtilDateTime.toTimestamp(tsString);
         assertEquals("2012-12-24 23:59:59.0", ts.toString());
 
         // Bewertet am 24. Juli 2012
         tsString = "Bewertet am 24. Juli 2012";
         ts = UtilDateTime.toTimestamp(tsString);
         assertEquals("2012-07-24 23:59:59.0", ts.toString());
     }
 
     @Test
     public void testConvertToTimestamp_ZagatTimeFormat() throws Exception {
         // Published June 12, 2012
         String tsString = "Published June 12, 2012";
         Timestamp ts = UtilDateTime.toTimestamp(tsString);
         assertEquals("2012-06-12 23:59:59.0", ts.toString());
     }
 
     @Test
     public void testConvertToTimestamp_YelpTimeFormat() throws Exception {
        String tsString = "7/8/2012";
         Timestamp ts = UtilDateTime.toTimestamp(tsString);
         assertEquals("2012-07-08 23:59:59.0", ts.toString());
 
         tsString = "12/20/2012";
         ts = UtilDateTime.toTimestamp(tsString);
         assertEquals("2012-12-20 23:59:59.0", ts.toString());
     }
 
     @Test
     public void testConvertTimestampToTwitterTime() throws Exception {
         Calendar calendar = Calendar.getInstance();
 
         calendar.set(2012, 07 - 1, 10, 12, 54, 06);
         calendar.set(Calendar.MILLISECOND, new Integer(966));
 
         Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
 
         String ts = UtilDateTime.connvertTimestampToTwitterTime(timestamp);
         assertEquals("2012-07-10", ts);
     }
 
     @Test
     public void testConvertTimestampToFbTime() throws Exception {
         Calendar calendar = Calendar.getInstance();
 
         calendar.set(2012, 07 - 1, 10, 12, 54, 06);
         calendar.set(Calendar.MILLISECOND, new Integer(966));
 
         Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
 
         String ts = UtilDateTime.connvertTimestampToFacebookTime(timestamp);
 
         Long unixTs = timestamp.getTime() / 1000L;
         assertEquals(unixTs.toString(), ts);
     }
 
     @Test
     public void testIfMessageDateIsYounger() throws Exception {
         Calendar calendar = Calendar.getInstance();
 
         Timestamp messageNetworkTs = UtilDateTime.nowTimestamp();
         Timestamp lastNetworkAccess = UtilDateTime.nowTimestamp();
 
         boolean younger = UtilDateTime.isMessageDateBeforeLastNetworkAccess(messageNetworkTs, lastNetworkAccess);
         assertFalse(younger);
 
         messageNetworkTs = UtilDateTime.toTimestamp("3/24/2012");
 
         calendar.set(2012, 7 - 1, 26, 11, 3, 0);
         lastNetworkAccess = new Timestamp(calendar.getTimeInMillis());
 
         younger = UtilDateTime.isMessageDateBeforeLastNetworkAccess(messageNetworkTs, lastNetworkAccess);
         assertTrue(younger);
 
         messageNetworkTs = UtilDateTime.toTimestamp("7/26/2012");
 
         calendar.set(2012, 7 - 1, 26, 11, 3, 0);
         lastNetworkAccess = new Timestamp(calendar.getTimeInMillis());
 
         younger = UtilDateTime.isMessageDateBeforeLastNetworkAccess(messageNetworkTs, lastNetworkAccess);
         assertFalse(younger);
     }
 
     @Test
     public void testToTimestamp() throws Exception {
         Timestamp resultTs = UtilDateTime.stirngToTimestamp(null);
         assertTrue(resultTs == null);
 
         resultTs = UtilDateTime.stirngToTimestamp(UtilDateTime.nowTimestamp().toString());
         assertTrue(resultTs != null);
     }
 }
