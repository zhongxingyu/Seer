 package com.acmetelecom.timeutils;
 
 import junit.framework.Assert;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.junit.Test;
 
 import java.io.File;
 
 public class TimeCalculatorTest {
     private PeakOffPeakPeriods peakOffPeakPeriods;
     private DefaultTimeCalculator timeCalculator;
     public  TimeCalculatorTest() throws  FileParseException{
         peakOffPeakPeriods =  TimeConfigurationReader.
                 loadPeakOffPeakPeriods(new File("src/test/java/com/acmetelecom/timeutils/timesTest.xml"));
         timeCalculator = new DefaultTimeCalculator(peakOffPeakPeriods);
     }
 
     @Test
     public void sameEndAndStartTimes() throws Exception {
         DateTime startTime = DateTime.parse("0800", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = DateTime.parse("0800", DateTimeFormat.forPattern("HHmm"));
 
         PeakOffPeakTime time=  timeCalculator.calculateTimes(startTime, endTime);
         Assert.assertEquals(time.getPeakTime(),0);
         Assert.assertEquals(time.getOffPeakTime(),0);
     }
 
     @Test
     public void testWithinOnePeriod() throws Exception {
         DateTime startTime = DateTime.parse("0800", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = DateTime.parse("0900", DateTimeFormat.forPattern("HHmm"));
 
         PeakOffPeakTime time=  timeCalculator.calculateTimes(startTime, endTime);
         Assert.assertEquals(time.getPeakTime(),3600);
         Assert.assertEquals(time.getOffPeakTime(),0);
     }
 
     @Test
     public void beyondOnePeriod() throws Exception {
         DateTime startTime = DateTime.parse("0800", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = DateTime.parse("1100", DateTimeFormat.forPattern("HHmm"));
 
         PeakOffPeakTime time=  timeCalculator.calculateTimes(startTime, endTime);
         Assert.assertEquals(time.getPeakTime(),7200);
         Assert.assertEquals(time.getOffPeakTime(),3600);
     }
 
     @Test
     public void overlappingPeriod() throws Exception {
         DateTime startTime = DateTime.parse("0700", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = DateTime.parse("1300", DateTimeFormat.forPattern("HHmm"));
 
         PeakOffPeakTime time=  timeCalculator.calculateTimes(startTime, endTime);
 
         Assert.assertEquals(time.getPeakTime(),10800);
         Assert.assertEquals(time.getOffPeakTime(),10800);
     }
 
     @Test
     public void completelyOffPeak() throws Exception {
         DateTime startTime = DateTime.parse("0300", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = DateTime.parse("0400", DateTimeFormat.forPattern("HHmm"));
 
        PeakOffPeakTime time = timeCalculator.calculateTimes(startTime, endTime);
 
         Assert.assertEquals(time.getPeakTime(),0);
         Assert.assertEquals(time.getOffPeakTime(),3600);
     }
     
     @Test
     public void callContinuesToNextDay() throws Exception {
         DateTime startTime = DateTime.parse("0700", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = DateTime.parse("0300", DateTimeFormat.forPattern("HHmm")).plusDays(1);
 
         PeakOffPeakTime time=  timeCalculator.calculateTimes(startTime, endTime);
         Assert.assertEquals(time.getPeakTime(),21600);
         Assert.assertEquals(time.getOffPeakTime(),50400);
     }
 
 
     @Test
     public void callLargerThanTwoDays() throws Exception {
         DateTime startTime = DateTime.parse("0700", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = DateTime.parse("0300", DateTimeFormat.forPattern("HHmm")).plusDays(3);
 
         PeakOffPeakTime time=  timeCalculator.calculateTimes(startTime, endTime);
         Assert.assertEquals(time.getPeakTime(),64800);
         Assert.assertEquals(time.getOffPeakTime(),180000);
 
     }
     
     @Test
     public void twentyFourHourCall() throws Exception {
         DateTime startTime = DateTime.parse("0700", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = DateTime.parse("0700", DateTimeFormat.forPattern("HHmm")).plusHours(24);
 
         PeakOffPeakTime time=  timeCalculator.calculateTimes(startTime, endTime);
         Assert.assertEquals(time.getPeakTime(),21600);
         Assert.assertEquals(time.getOffPeakTime(),64800);
     }
 
     @Test
     public void callWithCurrentDates() throws Exception {
     	DateTime startTime = DateTime.parse("0700", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = DateTime.parse("0700", DateTimeFormat.forPattern("HHmm")).plusDays(3);
 
         PeakOffPeakTime time=  timeCalculator.calculateTimes(startTime, endTime);
         Assert.assertEquals(time.getPeakTime(),64800);
         Assert.assertEquals(time.getOffPeakTime(),194400);
     }
     
     @Test
     public void callWithSeconds() throws Exception {
        DateTime startTime = DateTime.parse("0810", DateTimeFormat.forPattern("HHmm"));
         DateTime endTime = startTime.plusSeconds(3);
 
         PeakOffPeakTime time=  timeCalculator.calculateTimes(startTime, endTime);
        Assert.assertEquals(time.getPeakTime(),3);
        Assert.assertEquals(time.getOffPeakTime(),0);
     }
 }
