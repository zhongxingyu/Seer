 package de.rentoudu.mensa;
 
 import java.text.DateFormat;
import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
import java.util.Date;
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
 	
	public static boolean isAfter(int hour, int minute) {
		try {
			SimpleDateFormat parser = new SimpleDateFormat("HH:mm", Locale.getDefault());
			Date closingDate = parser.parse(hour + ":" + minute);
			Date currentDate = new Date();
			return currentDate.after(closingDate);
		} catch (ParseException e) {
			return false;
		}
		
	}
	
 	public static Calendar getCalendar() {
 		Calendar calendar = Calendar.getInstance();
     	calendar.setFirstDayOfWeek(Calendar.MONDAY);
     	return calendar;
 	}
 	
 }
