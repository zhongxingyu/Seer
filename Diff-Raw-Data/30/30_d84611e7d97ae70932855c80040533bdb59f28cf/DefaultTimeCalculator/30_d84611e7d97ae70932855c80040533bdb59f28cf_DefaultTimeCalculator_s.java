 package com.acmetelecom.timeutils;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.util.List;
 
 public class DefaultTimeCalculator implements TimeCalculator {
 
     private final PeakOffPeakPeriods peakOffPeakPeriods;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("HHmmSSS");
 
 
     public DefaultTimeCalculator() {
         peakOffPeakPeriods = DefaultPeakOffPeakPeriods.getDefaultPeakOffPeakPeriods();
     }
 
     public DefaultTimeCalculator(PeakOffPeakPeriods peakOffPeakPeriods) {
         this.peakOffPeakPeriods = peakOffPeakPeriods;
     }
 
 
     public PeakOffPeakTime calculateTimes(DateTime startTime,
                                           DateTime endTime) throws IllegalArgumentException {
         if (endTime.compareTo(startTime) < 0) {
           throw new IllegalArgumentException("start time must be less than equal to the end time");
         }
         if (endTime.compareTo(startTime) == 0){
             return  new PeakOffPeakTime(0 ,0);
         }
         long offpeak = endTime.getMillis() - startTime.getMillis();
         long peakTime = 0;
 
         int days = (int)new Duration(startTime, endTime).getStandardDays();
 
         List<JodaTimePeriod> times = peakOffPeakPeriods.getJodaTimes();
 
        startTime = DateTime.parse(startTime.toString(DATE_FORMATTER), DATE_FORMATTER);
        endTime   = DateTime.parse(endTime.toString(DATE_FORMATTER), DATE_FORMATTER);


         DateTime startTimeWithoutDays =
                 DateTime.parse(startTime.toString(DATE_FORMATTER), DATE_FORMATTER);
 
         DateTime endTimeWithoutDays =
                 DateTime.parse(endTime.toString(DATE_FORMATTER), DATE_FORMATTER);
 
         if (days == 0){
             peakTime += calculateTimeForADay(startTimeWithoutDays,endTimeWithoutDays,times) ;
 
         } else{
 
            if (!startTime.toLocalTime().equals(endTime.toLocalTime())){
                 days++;
            }
 
             for (int i = 0; i < days; i++) {
                 peakTime += calculateTimeForADay(startTimeWithoutDays, endTimeWithoutDays, times);
         }
         }
         offpeak -= peakTime;
         return new PeakOffPeakTime((long) peakTime / 1000, (long) offpeak / 1000);
     }
 
 
     private long calculateTimeForADay(DateTime startTime, DateTime endTime,
                                       List<JodaTimePeriod> times) {
 
 
 
         if ( startTime.compareTo(endTime)>=0){
             DateTime midnight = DateTime.parse("0000", DateTimeFormat.forPattern("HHmm")).plusDays(1);
             long tillMidnight = calculateTimeForADay(startTime,midnight,times) ;
             DateTime morning  = DateTime.parse("0000", DateTimeFormat.forPattern("HHmm"));
             long fromMorning  = calculateTimeForADay(morning, endTime, times) ;
             return tillMidnight + fromMorning;
         }
 
         long peak = 0;
         for (JodaTimePeriod period : times) {
             if (startTime.compareTo(period.endTime) <= 0 &&
                     period.startTime.compareTo(endTime) <= 0) {
                 DateTime earlierStart;
                 DateTime earlierEnd;
                 if (startTime.compareTo(period.startTime) <= 0) {
                     earlierStart = period.startTime;
                 } else {
                     earlierStart = startTime;
                 }
                 if (endTime.compareTo(period.endTime) <= 0) {
                     earlierEnd = endTime;
                 } else {
                     earlierEnd = period.endTime;
                 }
                 peak +=(earlierEnd.getMillis() - earlierStart.getMillis());
             }
         }
         return  peak;
     }
 
 }
