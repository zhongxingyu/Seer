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
 
 	public static String getFormattedSimple(Date start) {
 		return new SimpleDateFormat("dd.MM").format(start);
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
		return Integer.parseInt(format.format(System.currentTimeMillis()));
 	}
 
 	public static int getCurrentYear() {
 		int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(System.currentTimeMillis()));
 		return year;
 	}
 
 	public static void main(String[] args) {
 		System.out.println(DateManagement.getFormattedFull(new Date()));
 		System.out.println(DateManagement.getFormattedSimple(new Date()));
 		System.out.println("Current years: " + getCurrentYear());
 		System.out.println("Current week: " + getCurrentWeek());
 		System.out.println("Current week (from date): " + getWeek(new Date()));
 		System.out.println(getFormattedSimple(getDateFromString("20.12.2013")));
 	}
 }
