 package com.easycard;
 
 import java.util.Calendar;
 import java.util.Locale;
 
 public class Util {
 	public static String getFormattedDayDateTime() {
 		Calendar currentTime = Calendar.getInstance();
 		String day = currentTime.getDisplayName(currentTime.get(Calendar.DAY_OF_WEEK), Calendar.LONG, Locale.US);
 		String date = (currentTime.get(Calendar.MONTH)+1) + "/" +
 				currentTime.get(Calendar.DAY_OF_MONTH) + "/" + 
 				currentTime.get(Calendar.YEAR);
		String minutes = (currentTime.get(Calendar.MINUTE)/5)*5 + "";
		if(minutes.length() == 1) {
			minutes = "0"+minutes;
		}
 		String time = currentTime.get(Calendar.HOUR) + ":" + 
					  minutes;
 		String ampm = "AM";
 		if(currentTime.get(Calendar.HOUR) != currentTime.get(Calendar.HOUR_OF_DAY)) {
 			ampm = "PM";
 		}
 		return day + ", " + date + " at " + time + " " + ampm;
 	}
 }
