 package pizzaProgram.utils;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class DateFormatter {
 	public static String formatDateString(String databaseDateTimeResultString)
 	{
 		String[] separatedDayAndTimeStrings = databaseDateTimeResultString.split(" ");
 		String date = formatDate(separatedDayAndTimeStrings[0]);
 		String time = removeMilliseconds(separatedDayAndTimeStrings[1]);
 		time = stripSeconds(time);
 		return date + time;
 	}
 
 	private static String stripSeconds(String time) {
 		String[] timeParts = time.split(":");
 		return timeParts[0] + ":" + timeParts[1];
 	}
 
 	private static String removeMilliseconds(String time) {
 		int index = time.indexOf('.');
 		if(index > -1)
 		{
 			String output = time.substring(0, index);
 			return output;
 		}
 		return time;
 	}
 
 	private static String formatDate(String date) {
 		if(dateIsYesterday(date))
 		{
			return formatDateOnlyString(date) + " ";
 		} else {
 			return "";
 		}
 	}
 	
	private static String formatDateOnlyString(String date) {
		String[] dateParts = date.split("-");
	    return dateParts[2] + "-" + dateParts[1];
	}

 	private static boolean dateIsYesterday(String date)
 	{
 		int[] dateParts = convertDateStringToComponentValues(date);
 		int[] todayDateParts = getCurrentDate();
		if(((dateParts[0] <= todayDateParts[0]) && (dateParts[1] == todayDateParts[1]) && (dateParts[2] < todayDateParts[2])) 
				|| ((dateParts[0] <= todayDateParts[0]) && (dateParts[1] < todayDateParts[1])) 
				|| ((dateParts[0] < todayDateParts[0])))
 		{
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private static int[] getCurrentDate()
 	{
 		Calendar cal = Calendar.getInstance();
 	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 	    String currentDate = sdf.format(cal.getTime());
 	    int[] currentDateParts = convertDateStringToComponentValues(currentDate);
 	    return currentDateParts;
 	}
 	
 	private static int[] convertDateStringToComponentValues(String currentDate)
 	{
 		String[] dateParts = currentDate.split("-");
		int years = Integer.parseInt(dateParts[0]);
 	    int months = Integer.parseInt(dateParts[1]);
 	    int days = Integer.parseInt(dateParts[2]);
 	    int[] datePartsIntValues = new int[]{years, months, days};
 	    return datePartsIntValues;
 	}
 }
