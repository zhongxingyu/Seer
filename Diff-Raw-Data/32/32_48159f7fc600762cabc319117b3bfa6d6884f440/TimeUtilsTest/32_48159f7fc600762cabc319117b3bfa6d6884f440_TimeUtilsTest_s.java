 package test.unit;
 
import static org.junit.Assert.*;

 import org.joda.time.DateTime;
 import org.junit.Test;
 
import com.acmetelecom.Utils.TimeUtils;
 
 public class TimeUtilsTest {
 
     @Test
     public void testTwoDaysDuration() {
         DateTime startTime = new DateTime(2012, 11, 1, 18, 0, 0);
         DateTime endTime = startTime.plusDays(2);
         int peakDuration = TimeUtils.getPeakDurationSeconds(startTime, endTime);
         assertEquals(2*12*60*60, peakDuration);
     }
 
     @Test
     public void testOffPeak() {
         DateTime startTime = new DateTime(2012, 11, 1, 19, 30, 0);
         DateTime endTime = startTime.plusMinutes(5);
         int peakDuration = TimeUtils.getPeakDurationSeconds(startTime, endTime);
         assertEquals(0,peakDuration);
 
         // over night
         DateTime startTime2 = new DateTime(2012, 11, 1, 23, 30, 0);
         DateTime endTime2 = startTime.plusHours(4);
         int peakDuration2 = TimeUtils.getPeakDurationSeconds(startTime2, endTime2);
         assertEquals(0,peakDuration2);
     }
 
     @Test
     public void testOffPeakOverPeakPeriod() {
         DateTime startTime = new DateTime(2012, 11, 1, 6, 30, 0);
         DateTime endTime = startTime.plusHours(14);
         int peakDuration = TimeUtils.getPeakDurationSeconds(startTime, endTime);
         assertEquals(12*60*60,peakDuration);
     }
 
     @Test
     public void testPeakPeriod() {
         DateTime startTime = new DateTime(2012, 11, 1, 8, 30, 0);
         DateTime endTime = startTime.plusMinutes(10);
         int peakDuration = TimeUtils.getPeakDurationSeconds(startTime, endTime);
         assertEquals(10*60,peakDuration);
     }
 
     @Test
     public void testPeakPeriodOverNight() {
         DateTime startTime = new DateTime(2012, 11, 1, 18, 0, 0);
         DateTime endTime = startTime.plusHours(14);
         int peakDuration = TimeUtils.getPeakDurationSeconds(startTime, endTime);
         assertEquals(2*60*60,peakDuration);
     }
 
     @Test
     public void testOffPeakToPeak() {
         DateTime startTime = new DateTime(2012, 11, 1, 22, 0, 0);
         DateTime endTime = startTime.plusHours(14);
         int peakDuration = TimeUtils.getPeakDurationSeconds(startTime, endTime);
         assertEquals(5*60*60,peakDuration);
     }
 
     @Test
     public void testPeakToOffPeak() {
         DateTime startTime = new DateTime(2012, 11, 1, 16, 0, 0);
         DateTime endTime = startTime.plusHours(5);
         int peakDuration = TimeUtils.getPeakDurationSeconds(startTime, endTime);
         assertEquals(3*60*60,peakDuration);
 
         // over night
         /*DateTime startTime2 = new DateTime(2012, 11, 1, 18, 30, 0);
         DateTime endTime2 = startTime.plusHours(10);
         int peakDuration2 = TimeUtils.getPeakDurationSeconds(startTime2, endTime2);
         assertEquals(30*60,peakDuration2);           */
     }
 
     @Test
     public void testThreeSecondDuration() {
         DateTime startTime = new DateTime(2012, 11, 1, 18, 0, 0);
         DateTime endTime = startTime.plusSeconds(3);
         int peakDuration = TimeUtils.getPeakDurationSeconds(startTime, endTime);
         assertEquals(3,peakDuration);
     }


	
 }
