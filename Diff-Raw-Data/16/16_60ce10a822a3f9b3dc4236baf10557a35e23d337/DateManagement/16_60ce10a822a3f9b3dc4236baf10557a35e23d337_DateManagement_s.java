 package chronos;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import client.model.CalendarModel.Weekday;
 
 /**
  * Handles date management
  */
 public class DateManagement {
 
 	public static String getFormattedFull(Date start) {
 		return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(start);
 	}
 	public static String getFormattedDate(Date start) {
 		return new SimpleDateFormat("dd.MM.yyyy").format(start);
 	}
 
 	public static String getFormattedSimple(Date start) {
 		return new SimpleDateFormat("dd.MM").format(start);
 	}
 	public static String getFormattedDateIntervall(Date start) {
 		String returnString = "";
 		returnString += getFormattedDate(start);
 		Calendar c = Calendar.getInstance();
 		c.setTime(start);
 		c.add(Calendar.DATE, 6);  // number of days to add
 		returnString += " - " + getFormattedDate(c.getTime()); 
 		return returnString;
 		
 	}
 
 	public static Date getDateFromString(String str) {
 		try {
 			return new SimpleDateFormat("dd.MM.yyyy").parse(str);
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static Weekday getWeekday(Date start) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(start);
 		int ordinal = cal.get(Calendar.DAY_OF_WEEK);
 		return Weekday.getWeekday(ordinal);
 	}
 
 	public static int getCurrentWeek() {
 		return getWeek(new Date());
 	}
 
 	/**
 	 * method that returns which week the date-parameter is in.
 	 */
 	public static int getWeek(Date start) {
 		Calendar cal = Calendar.getInstance();
 		cal.setFirstDayOfWeek(Calendar.MONDAY);
 
 		SimpleDateFormat format = new SimpleDateFormat("ww");
 		format.setCalendar(cal);
 		return Integer.parseInt(format.format(start));
 	}
 
 	public static int getCurrentYear() {
 		int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(System.currentTimeMillis()));
 		return year;
 	}
 	/**
 	 * Method to increase a date by 1 
 	 * @param start
 	 * @return
 	 */
 	public static Date getNextDay(Date start) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(start);
 		c.add(Calendar.DATE, 1);  // number of days to add
 		return c.getTime(); 
 	}
 	public static int getYear(Date start) {
 		SimpleDateFormat format = new SimpleDateFormat("yyyy");
 		return Integer.parseInt(format.format(start));
 	}
 	public static String getNextWeek(Date start) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(start);
 		c.add(Calendar.DATE, 7);
 		return getFormattedDate(c.getTime());
 	}
 	public static String getPrevWeek(Date start) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(start);
 		c.add(Calendar.DATE, -7);
 		return getFormattedDate(c.getTime());
 	}
 
 	public static void main(String[] args) {
 		System.out.println(DateManagement.getFormattedFull(new Date()));
 		System.out.println(DateManagement.getFormattedSimple(new Date()));
 		System.out.println("Current years: " + getCurrentYear());
 		System.out.println("Current week: " + getCurrentWeek());
 		System.out.println("Current week (from date): " + getWeek(new Date()));
 		System.out.println(getFormattedSimple(getDateFromString("13.03.2013")));
 		System.out.println("Next day: " + getNextDay(new Date()));
 		System.out.println("Date today: " + getFormattedDate(new Date()));
 		System.out.println("Date intervall(one week from date): " + getFormattedDateIntervall(new Date()));
 		System.out.println("Date 7 days from now: " + getNextWeek(new Date()));
 		System.out.println("Year (from Date): " + getYear(new Date()));
 	}
 }
