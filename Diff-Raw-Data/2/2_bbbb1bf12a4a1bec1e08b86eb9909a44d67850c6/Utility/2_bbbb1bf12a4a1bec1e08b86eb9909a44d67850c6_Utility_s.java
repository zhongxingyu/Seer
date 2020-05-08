 package tools;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public abstract class Utility {
	private static SimpleDateFormat df = new SimpleDateFormat("dd.mm.yyyy");
 
 	public static Date stringToDate(String dateString) {
 		try {
 			return df.parse(dateString);
 		} catch (ParseException e) {
 			return null;
 		}
 	}
 
 	public static String dateToString(Date date) {
 		if (date == null)
 			return "";
 		return df.format(date);
 	}
 }
