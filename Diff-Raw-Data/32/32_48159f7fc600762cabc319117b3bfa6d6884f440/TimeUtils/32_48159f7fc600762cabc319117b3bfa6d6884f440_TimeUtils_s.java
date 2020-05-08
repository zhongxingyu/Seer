 package com.acmetelecom.Utils;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 public class TimeUtils {
     public static final DaytimePeakPeriod peakPeriod = new DaytimePeakPeriod();
 
     public static int getPeakDurationSeconds(DateTime start, DateTime end) {
         Duration duration = new Duration(start, end);
         return getPeakDuration(start, end, duration.toStandardDays().getDays());
     }
 
     private static int getPeakDuration(DateTime start, DateTime end, int numberOfDays) {
        int total = (int)peakPeriod.getPeakPeriodSeconds();
         Duration duration = new Duration(start, end);
         if(numberOfDays < 1){
             if (peakPeriod.offPeak(start.toDate()) && peakPeriod.offPeak(end.toDate())){
                 return duration.getStandardHours() > peakPeriod.getPeakPeriodHours()
                        ? (int)peakPeriod.getPeakPeriodSeconds() : 0;
             } else if(!peakPeriod.offPeak(start.toDate()) && !peakPeriod.offPeak(end.toDate())) {
                 return duration.getStandardHours() > peakPeriod.getPeakPeriodHours()
                         ? (int) (duration.getStandardSeconds() - peakPeriod.getPeakPeriodSeconds())
                        : (int) (((end.getMillis() - start.getMillis()) / 1000));
             } else {
                 return peakPeriod.offPeak(start.toDate())
                         ? getDurationSecondsWhenOverlap(peakPeriod.PeakPeriodStart, end)
                         : getDurationSecondsWhenOverlap(peakPeriod.PeakPeriodEnd, start);
             }
         } else {
            return total + getPeakDuration(start.plusDays(1), end, numberOfDays - 1);
         }
     }
 
     private static int getDurationSecondsWhenOverlap(int peakPeriod, DateTime date) {
        return Math.abs(date.getHourOfDay() - peakPeriod) * 60 * 60
                + Math.abs(0 - date.getMinuteOfHour()) * 60
                + Math.abs(0 - date.getSecondOfMinute());
     }
 }
