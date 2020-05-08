 /**
  * 
  */
 package com.playground.farecalculator.utils;
 
 import java.util.Calendar;
 import java.util.TimeZone;
 
 /**
  * @author Manveer Chawla (manveer.chawla@gmail.com)
  *
  */
 public class CommonUtils
 {
 	public static boolean isNightFareApplicable(int nightFareStartHour, int nightFareEndHour, int nightFareStartMinute, int nightFareEndMinute)
 	{
 		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("IST"));
 		int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
 		int currentMinute = calendar.get(Calendar.MINUTE);
 		int totalMinutesFromStart = (nightFareEndHour - nightFareStartHour) * 60;
 		if(nightFareStartHour > 12) // greater than noon (it can be something like 11 pm (23 HRS) in night
 			totalMinutesFromStart += 24*60;
 		int currentMinutesFromStart = (currentHour - nightFareStartHour)* 60;
		if(!(nightFareStartHour > 12 && currentHour >= nightFareStartHour && currentHour <= 23)) // greater than noon (it can be something like 11 pm (23 HRS) in night
 			currentMinutesFromStart += 24*60;
 		totalMinutesFromStart += (nightFareEndMinute - nightFareStartMinute);
 		currentMinutesFromStart += (currentMinute - nightFareStartMinute);
 		if(currentMinutesFromStart > totalMinutesFromStart || currentMinutesFromStart < 0)
 			return false;
 		return true;
 	}
 }
