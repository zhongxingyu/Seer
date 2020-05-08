 package com.acmetelecom;
 
 import static junit.framework.Assert.assertEquals;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalTime;
 import org.testng.annotations.Test;
 
import com.acmetelecom.billcalculator.PeakPeriodDatasource;
 import com.acmetelecom.billcalculator.peak.PeakPeriodManager;
 
 /**
  * Tests for the PeakPeriodManager used by the system
  */
 public class DaytimePeakPeriodTests {
 
     private PeakPeriodDatasource peakPeriodManager = new PeakPeriodManager();
     private DateTime beginPeak = new LocalTime(7,0,0).toDateTimeToday();
     private DateTime endPeak = new LocalTime(19,0,0).toDateTimeToday();
 
     @Test
     public void offPeakEveningTest() {
 
         // 19:00:01
         DateTime offPeakEvening = new LocalTime(19,0,1).toDateTimeToday();
         long peakSecs = this.peakPeriodManager.secondsInPeak(endPeak, offPeakEvening);
         assertEquals("The time " + offPeakEvening +
                 " was not declared off peak when it should have", 0, peakSecs);
     }
 
 
     @Test
     public void onPeakEveningTest() {
 
         // 18:59:59
         DateTime peakEvening = new LocalTime(18,59,59).toDateTimeToday();
         long peakSecs = this.peakPeriodManager.secondsInPeak(peakEvening,endPeak);
         assertEquals("The time " + peakEvening +
                 " was declared off peak when it should have been on peak.", 1, peakSecs);
 
     }
 
     @Test
     public void onPeakMorningTest() {
 
         // 07:00:01
         DateTime peakMorning = new LocalTime(7,0,1).toDateTimeToday();
         long peakSecs = this.peakPeriodManager.secondsInPeak(beginPeak, peakMorning);
         assertEquals("The time " + peakMorning +
                 " was declared off peak when it should have been on peak.", 1, peakSecs);
     }
 
     @Test
     public void offPeakMorningTest() {
 
         // 06:59:59
         DateTime offPeakMorning = new LocalTime(6,59,59).toDateTimeToday();
         long peakSecs = this.peakPeriodManager.secondsInPeak(offPeakMorning, beginPeak);
         assertEquals("The time " + offPeakMorning +
                 "was not declared off peak when it should have", 0, peakSecs);
     }
 
 }
