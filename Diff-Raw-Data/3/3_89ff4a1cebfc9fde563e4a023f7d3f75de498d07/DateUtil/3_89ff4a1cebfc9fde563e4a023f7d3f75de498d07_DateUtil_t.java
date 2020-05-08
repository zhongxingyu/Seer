 package util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;

 /**
 * @author Mihail Atanassov <mail:saeko.bjagai@gmail.com>
 */

 public class DateUtil {
 
 	private DateUtil() {
 		// static utility class
 	}
 
 	public static Date parseToDate(String dateAsString) throws ParseException {
 		if (checkRegEx(dateAsString)) {
 			String[] str = dateAsString.split("-");
 			String DATE_FORMAT = "";
 			for (int i = 0; i < str.length; i++) {
 				// adding yyyy
 				if (i == 0)
 					DATE_FORMAT += "yyyy";
 
 				// adding MM
 				if (i == 1)
 					if (Integer.parseInt(str[i]) <= 12)
 						DATE_FORMAT += "-MM";
 					else
 						return null;
 
 				// adding dd
 				if (i == 2)
 					if (Integer.parseInt(str[i]) <= 31)
 						DATE_FORMAT += "-dd";
 					else
 						return null;
 			}
 			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
 					DATE_FORMAT);
 			return simpleDateFormat.parse(dateAsString);
 		} else
 			return null;
 	}
 
 	public static boolean checkRegEx(String dateAsString) {
 		return dateAsString.matches("[0-9]{1,4}(-[0-9]{2})?(-[0-9]{2})?");
 	}
 
 	public static String getStringFromDate(Date date) {
 		String DATE_FORMAT = "yyyy-MM-dd";
 		return new SimpleDateFormat(DATE_FORMAT).format(date.getTime());
 	}
 
 }
