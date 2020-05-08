 package spitapp.util;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * class to handle specific date operations within the gui
  * @author jaggr2, vonop1
  */
 public class DateUtil {
 	
 	/**
 	 * returns todays date with the submitted time
 	 * @param timeinput the time in the format hh:mm
 	 * @return the date if input is valid or null on failure
 	 */
 	public static Date getTodayWithSpecificTime(String timeinput) {
 
 		// Let's prepare the input
 		String[] parts = timeinput.split(":");
 		Integer hours = null;
 		Integer minutes = null;
 		
 		// Check input and return null if input isn't valid
 		if(parts.length != 2) {
 			return null;
 		}
 		try {
 			hours = Integer.parseInt(parts[0]);
 			minutes = Integer.parseInt(parts[1]);
 			if(hours > 24 || hours < 0 || minutes > 59 || minutes < 0) {
 				return null;
 			}
 		} 
 		catch(NumberFormatException ex) {
 			return null;
 		}
 		
 		//Set the date together
 		Calendar date = new GregorianCalendar();
 		date.setTime(new Date());
		date.set(Calendar.HOUR, hours);
 		date.set(Calendar.MINUTE, minutes);
 		date.set(Calendar.SECOND, 0);
 		return date.getTime();
 	}
 	
 	/**
 	 * Add an offset in days to the input date 
 	 * @param input the date
 	 * @param offset the days (positive to add, negative to substract)
 	 * @return the result
 	 */
 	public static Date addDays(Date input, int offset) {
 		Calendar calendar = new GregorianCalendar();
 		calendar.setTime(input);
 		calendar.add(Calendar.DAY_OF_MONTH, offset);
 		return calendar.getTime();		
 	}
 	
 	/**
 	 * Formats a date with the given format string from SimpleDateFormat
 	 * @param input the input date
 	 * @param format the format - for example HH:mm
 	 * @return the formatted string
 	 */
 	public static String formatDate(Date input, String format) {
 		SimpleDateFormat sdf = new SimpleDateFormat(format);
 		return sdf.format(input);		
 	}
 }
