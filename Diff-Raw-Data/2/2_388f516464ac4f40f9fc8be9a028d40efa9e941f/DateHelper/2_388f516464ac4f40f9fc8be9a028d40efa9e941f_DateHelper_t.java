 package rpisdd.rpgme.gamelogic.quests;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 
 public class DateHelper {
 
 	// Take in a date and format it, returning a reader-friendly string.
 	// Later this could use words/phrases like "Tomorrow", "In a week",
 	// "In an hour", etc.
 	public static String formatDate(DateTime date) {
		String formatDate = DateTimeFormat.forPattern("M/d/Y 'at' h:mm a")
 				.print(date);
 		return formatDate;
 	}
 
 	// Is date at least one minute ago from now?
 	public static boolean oneMinuteAgo(DateTime date) {
 		return oneDayAgo(date)
 				|| date.getMinuteOfDay() < DateTime.now().getMinuteOfDay();
 	}
 
 	// Is date at least one day ago from now?
 	public static boolean oneDayAgo(DateTime date) {
 		return oneYearAgo(date)
 				|| date.getDayOfYear() < DateTime.now().getDayOfYear();
 	}
 
 	// Is date at least one week ago from now?
 	public static boolean oneWeekAgo(DateTime date) {
 		return oneYearAgo(date)
 				|| date.getWeekOfWeekyear() < DateTime.now()
 						.getWeekOfWeekyear();
 	}
 
 	// Is day at least one month ago from now?
 	public static boolean oneMonthAgo(DateTime date) {
 		return oneYearAgo(date)
 				|| date.getMonthOfYear() < DateTime.now().getMonthOfYear();
 	}
 
 	// Is day at least one year ago from now?
 	public static boolean oneYearAgo(DateTime date) {
 		return date.getYear() < DateTime.now().getYear();
 	}
 
 }
