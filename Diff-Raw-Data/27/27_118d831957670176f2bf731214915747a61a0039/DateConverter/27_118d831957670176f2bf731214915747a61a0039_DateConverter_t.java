 /**
  * 
  */
 package se.chalmers.watchme.utils;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 /**
  * A utility class for converting date's to common formats
  */
 public class DateConverter {
 	
 	/**
 	 * Recieves a calendar instance and returns a String with simple
 	 * date format(MM/dd/yyyy)
 	 * 
 	 * @param calendar The calendar object to be used to create the string
 	 * @returns A simple string representing a date in the format: MM/dd/yyyy
 	 */
 	public static String toSimpleDate(Calendar calendar) {
 		
 		SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy");
 		String simpleDateString = simpleDate.format(calendar.getTime());
 		
 		return simpleDateString;
 		
 	}
 	
 	
 	/* TODO Is it okay to require the string to be formated in a specific way?
 	 * Is this a place for pre-condition?
 	 */
 	
 	/**
 	 * Recieves a String in the format "MM/dd/yyyy" and returns a Calendar object
 	 * 
 	 * @param string The string to convert to a Calendar object
 	 * @returns Calendar object set to the recieved date
 	 */
 	public static Calendar toCalendar(String simpleDateString) {
 		
		int month = 1;
		int day = 1;
 		int year = 2000;
 		
 		/* TODO Feels like super-ugly code. But this will have to do for now.
 		 * Use token to select substring according to slash (/) signs?
 		 * Better ideas?
 		 */
 		
 		// Try to convert the date in the string to integers
 		try {
 			month = Integer.parseInt(simpleDateString.substring(0, 1));
 			day = Integer.parseInt(simpleDateString.substring(3, 4));
 			year = Integer.parseInt(simpleDateString.substring(6, 9));
 		}
 		
 		catch(NumberFormatException e) {
 			System.err.println("Parse exception: " + e.toString());
 			e.printStackTrace();
 		}
 		
		catch(IndexOutOfBoundsException e) {
			System.err.println("String formatted wrong " + e.toString());
			e.printStackTrace();
		}
		
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(year, month, day);
 		
 		return calendar;
 		
 	}
 
 }
