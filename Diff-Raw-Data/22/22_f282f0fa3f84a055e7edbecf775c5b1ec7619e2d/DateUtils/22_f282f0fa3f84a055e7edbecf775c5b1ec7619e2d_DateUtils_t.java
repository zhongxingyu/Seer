 package com.connectsy.utils;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class DateUtils{
	// This is here because apparently Java is a shitty programming
	// language...
	final static String[] thArray = new String[] {
		"st","nd","rd","th","th","th","th","th","th","th",
		"th","th","th","th","th","th","th","th","th","th",
		"st","nd","rd","th","th","th","th","th","th","th",
		"st"
	};
	
 	public static String formatDate(Date date){
 		Calendar formatting = Calendar.getInstance();
 		formatting.setTime(date);
     	Calendar today = Calendar.getInstance();
 		String dateString;
     	
     	if (formatting.get(Calendar.MONTH) == today.get(Calendar.MONTH)
     			&& formatting.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
     		if (formatting.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH))
     			dateString = "Today";
     		else if (formatting.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)+1)
     			dateString = "Tomorrow";
     		else{
     			dateString = new SimpleDateFormat("EEEE 'the' d").format(date);
     			dateString = dateString+thArray[formatting.get(Calendar.DAY_OF_MONTH)-1];
     		}
     	}else if (formatting.get(Calendar.YEAR) == today.get(Calendar.YEAR)){
     		dateString = new SimpleDateFormat("MMM d").format(date);
     	}else{
     		dateString = new SimpleDateFormat("MMM d, yyyy").format(date);
     	}
     	return dateString;
 	}
 	
 	public static String formatTime(Date date){
     	SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
     	return timeFormat.format(date);
 	}
 	
 	public static String formatTimestamp(long timestamp){
		Date date = new Date(timestamp);
     	String timeString = formatTime(date);
     	String dateString = formatDate(date);
 		return dateString+" at "+timeString;
 	}
 	
 	public static boolean isCacheExpired(Date cached, int hours){
 		Date now = new Date();
 		now.setHours(now.getHours()+hours);
 		return (cached.compareTo(now) > 0);
 	}
 }
