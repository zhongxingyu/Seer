 package com.acmetelecom;
 
 import junit.framework.Assert;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Tests for the DaytimePeakPeriod class
  */
 public class DaytimePeakPeriodTests {
 
     public static Calendar startOfADay = Calendar.getInstance();
 
     //TODO: these constants need to be gathered from the DaytimePeakPeriod class in constants rather than here.
     public static final int endOfPeak = 19;
     public static final int startOfPeak = 7;
 
 
    @BeforeTest
     public void setupCalendar() {
         startOfADay.set(2005, Calendar.JANUARY, 1);
     }
 
 
     @Test
     public void offPeakEveningTest() {
 
         Calendar afterPeak = adjustDayCalendar(endOfPeak, 1);
 
         boolean result = new DaytimePeakPeriod().offPeak(afterPeak.getTime());
 
         Assert.assertTrue("The time " +  endOfPeak + ":00:01 " +
                 "was not declared off peak when it should have", result);
     }
 
     @Test
     public void onPeakEveningTest() {
 
         Calendar inPeak = adjustDayCalendar(endOfPeak, -1);
         boolean result = new DaytimePeakPeriod().offPeak(inPeak.getTime());
 
         Assert.assertFalse("The time " + (endOfPeak - 1) + ":59:59 " +
                 "was declared off peak when it should have been on peak.", result);
 
     }
 
     @Test
     public void onPeakMorningTest() {
         Calendar inPeak = adjustDayCalendar(startOfPeak, 1);
 
         boolean result = new DaytimePeakPeriod().offPeak(inPeak.getTime());
 
         Assert.assertFalse("The time " + startOfPeak + ":00:01 " +
                 "was declared off peak when it should have been on peak.", result);
     }
 
     @Test
     public void offPeakMorningTest() {
 
         Calendar beforePeak = adjustDayCalendar(startOfPeak, -1);
 
         boolean result = new DaytimePeakPeriod().offPeak(beforePeak.getTime());
 
         Assert.assertTrue("The time " +  (startOfPeak - 1) + ":59:59 " +
                 "was not declared off peak when it should have", result);
     }
 
     private Calendar adjustDayCalendar(int hour, int second) {
         Calendar cal = (Calendar) startOfADay.clone();

         cal.add(Calendar.HOUR, hour);
         cal.add(Calendar.SECOND, second);

         return cal;
     }
 
 }
