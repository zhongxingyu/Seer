 package common;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 public class TimeUtil {
 
 	public static void main(String[] args) {
 //		 System.out.println(getTime("1000","PST"));
 		// System.out.println(getTime("0200","MST"));
 		// System.out.println(getTime("0300","CST"));
 		// System.out.println(getTime("0400","EST"));
		System.out.println(dayOfWeek("EST"));
 	}
 
 	public static Long getCurrTime() {
 		Date dt = new Date();
 		return dt.getTime();
 	}
 
 	public static Long getTime(String time, String tz) {
 		Date dt = new Date();
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(dt);
 		int yyyy = cal.get(Calendar.YEAR);
 		int mm = cal.get(Calendar.MONTH) + 1;
 		int dd = cal.get(Calendar.DAY_OF_MONTH);
 
 		String prefix = yyyy + "-" + mm + "-" + dd + "T";
 
 		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmm");
 		isoFormat.setTimeZone(TimeZone.getTimeZone(tz));
 		Date date;
 		try {
 			date = isoFormat.parse(prefix + time);
 		} catch (ParseException e) {
 			return -1L;
 		}
 		return date.getTime();
 	}
 
 	public static String getTZ() {
 		String tz = java.util.TimeZone.getDefault().getDisplayName(false,
 				TimeZone.SHORT);
 		if (tz == null || "".equals(tz)) {
 			tz = "PST";
 		}
 		return tz;
 	}
 
 	public static String dayOfWeek(String tz) {
 		TimeZone tzz = TimeZone.getTimeZone(tz);
 		Calendar cal = Calendar.getInstance(tzz);
 		Date dt = new Date();
 		int day = cal.get(Calendar.DAY_OF_WEEK);
 		switch (day) {
 		case 1:
 			return "Sun";
 		case 2:
 			return "Mon";
 		case 3:
 			return "Tue";
 		case 4:
 			return "Wed";
 		case 5:
 			return "Thu";
 		case 6:
 			return "Fri";
 		case 7:
 			return "Sat";
 		}
 		return null;
 
 	}
 }
