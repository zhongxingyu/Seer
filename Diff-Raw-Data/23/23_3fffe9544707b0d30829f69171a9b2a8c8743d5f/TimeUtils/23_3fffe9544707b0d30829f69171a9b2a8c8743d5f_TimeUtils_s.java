 package com.acmetelecom.Utils;
 
import java.util.Calendar;
import java.util.Date;

 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 public class TimeUtils {
 
 	/**
	 *  assume call duration is no longer than 24h
 	 * @param startTime
 	 * @param endTime
 	 * @return
 	 */
 	public static int getPeakDurationSeconds(DateTime startTime, DateTime endTime) {
 		
 		DaytimePeakPeriod peakPeriod = new DaytimePeakPeriod();		
 		int peakDurationSeconds = 0;		
 		if (peakPeriod.offPeak(startTime.toDate()) && peakPeriod.offPeak(endTime.toDate())
 				&& new Duration(startTime,endTime).getStandardHours() > peakPeriod.getPeakPeriodDuration() * 60 * 60) {
 			// phone call covered whole peak period
 			peakDurationSeconds = peakPeriod.getPeakPeriodDuration() * 60 * 60;
 		} else if (!peakPeriod.offPeak(startTime.toDate())
 				&& !peakPeriod.offPeak(endTime.toDate())) {						
 			if(endTime.getHourOfDay() > startTime.getHourOfDay()){
 				// call starts in peak period and covers off peak period then ends in peak period
 				peakDurationSeconds = ( peakPeriod.PeakPeriodEnd - startTime.getHourOfDay() ) * 60*60 
 						+ (60-startTime.getMinuteOfHour()) * 60 + (60-startTime.getSecondOfMinute())
 						+ (endTime.getHourOfDay() - peakPeriod.PeakPeriodStart) * 60 * 60 
 						+ endTime.getMinuteOfHour() * 60 + endTime.getSecondOfMinute();
 			}else{
 				// phone call happened within peak period
 				peakDurationSeconds = (int) new Duration(startTime,endTime).getStandardSeconds();
 			}			
 		} else if (peakPeriod.offPeak(startTime.toDate())
 				&& !peakPeriod.offPeak(endTime.toDate())) {
 			// peak time duration starts from 7am till call ends
 			peakDurationSeconds = (endTime.getHourOfDay() - peakPeriod.PeakPeriodStart) * 60 * 60 
 					+ endTime.getMinuteOfHour() * 60 + endTime.getSecondOfMinute();
 		} else if (!peakPeriod.offPeak(startTime.toDate())
 				&& peakPeriod.offPeak(endTime.toDate())) {
 			// peak time duration starts since call starts, ends at 7pm (19:00)
 			peakDurationSeconds += (peakPeriod.PeakPeriodEnd - startTime.getHourOfDay()) * 60 * 60 
 					+ (60-startTime.getMinuteOfHour()) * 60 + (60-startTime.getSecondOfMinute());
 		}
 
 		return peakDurationSeconds;
 	}
 
 }
