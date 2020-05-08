 package com.acmetelecom;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 
 public class Call implements CallInterface{
 	  private CallStartInterface start;
     private CallEndInterface end;
 
     public Call(CallStartInterface start, CallEndInterface end) {
         this.start = start;
         this.end = end;
     }
     
     public String callee() {
         return start.getCallee();
     }
 
     public int durationSeconds() {
         return (int) new Duration(new DateTime(start.time()), new DateTime(end.time())).getStandardSeconds();
     }
     
     public long durationPeakSeconds() {
   		DateTime startTime = startTime();
   		DateTime endTime = endTime();
   		
 		DateTime peakStart=new DateTime(startTime.withTime(DaytimePeakPeriod.PEAK_RATE_START_TIME, 0,0,0));
   		DateTime peakEnd=new DateTime(startTime.withTime(DaytimePeakPeriod.OFF_PEAK_RATE_START_TIME,0,0,0));
   		Interval callInterval;
   		
 		if (startTime.isBefore(endTime))
   			callInterval=new Interval(startTime, endTime);
   		else return 0;
   		long peakSeconds=0;
   		long daysBetween=new Duration(startTime, endTime).getStandardDays();
  		if (daysBetween>0) daysBetween++;
  		for (int i=0;i<=daysBetween;i++){
   			Interval peakHours=new Interval(peakStart.plusDays(i),peakEnd.plusDays(i));
   			Interval overlapWithPeak = callInterval.overlap(peakHours);
 
   			if (overlapWithPeak != null)
   				peakSeconds+=overlapWithPeak.toDuration().getStandardSeconds();
   		}
      	return peakSeconds;
     }
     
     public long durationOffPeakSeconds() { 	
     	long between=0;
     	DateTime startTime = startTime();
 		DateTime endTime = endTime();
 		if (startTime.isBefore(endTime))
     		between=new Duration(startTime,endTime).getStandardSeconds();
     	return between-durationPeakSeconds();
     }
 
     public String date() {
     	return new DateTime(start.time()).toString("dd/MM/yy HH:mm:ss");
     }
 
     public DateTime startTime() {
         return new DateTime(start.time());
     }
 
     public DateTime endTime() {
         return new DateTime(end.time());
     }
 }
