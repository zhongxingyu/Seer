 package com.acmetelecom.time;
 
 import org.joda.time.Period;
 
 public class Duration{
 	
 	public static long inSeconds(TimeStamp start, TimeStamp end){
 		
 		long result = Period.fieldDifference(start.getDateTime(), end.getDateTime()).toStandardDuration().getStandardSeconds();
 		
 		return result;
 	}
 	
 	public static long inDays(TimeStamp start, TimeStamp end){
 		return Period.fieldDifference(start.getDateTime(), end.getDateTime()).toStandardDuration().getStandardDays();
 	}
 	
 	public static long inSeconds(Time start, Time end){
 		
 		if (end.isBefore(start)) {
 			end = new Time(end.getHour()+24, end.getMin(), end.getSecond());
 		}
 		
 		return end.getSeconds() - start.getSeconds();
 	}
 }
