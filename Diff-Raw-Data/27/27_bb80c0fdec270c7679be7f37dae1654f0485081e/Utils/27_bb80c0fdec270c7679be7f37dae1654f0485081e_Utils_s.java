 package de.rentoudu.mensa;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 
 public class Utils {
 
 	public static int getDay() {
 		return getCalendar().get(Calendar.DAY_OF_WEEK);
 	}
 	
 	public static int getWeek() {
 		return getCalendar().get(Calendar.WEEK_OF_YEAR);
 	}
 	
 	public static String getFormattedDateForWeekDay(int week, int day) {
 		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
 		SimpleDateFormat simpleDateFormat = (SimpleDateFormat) dateFormat;
 		Calendar calendar = getCalendar();
 		calendar.set(Calendar.WEEK_OF_YEAR, week);        
 		calendar.set(Calendar.DAY_OF_WEEK, day);
 		String date = simpleDateFormat.format(calendar.getTime());
 		String longYear = String.valueOf(getYear());
 		String shortYear = String.valueOf(getYear()).replace("20", "");
 		String customizedDate = date.replace(longYear, shortYear);
 		return customizedDate;
 	}
 	
 	public static int getYear() {
 		return getCalendar().get(Calendar.YEAR);
 	}
 	
 	public static Calendar getCalendar() {
 		Calendar calendar = Calendar.getInstance();
     	calendar.setFirstDayOfWeek(Calendar.MONDAY);
     	return calendar;
 	}
 	
 }
