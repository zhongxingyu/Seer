 /*
  * Created on May 4, 2012
  * @author verkel
  */
 package mobilenoppa.model;
 
 import java.util.Locale;
 
 import org.joda.time.*;
 import org.joda.time.format.*;
 
 /**
  * Static utils for calculating and formatting dates. Uses the Joda Time
  * library.
  * 
  * @author verkel
  */
 public class DateUtils {
 
	private static final DateTimeFormatter dateFormatFin = DateTimeFormat.forPattern("dd.MM.yy");
	private static final DateTimeFormatter dateFormatNoYearFin = DateTimeFormat.forPattern("dd.MM.");
 	private static final DateTimeFormatter dateFormatUs = DateTimeFormat.forPattern("dd MMM yy")
 		.withLocale(Locale.US);
 	private static final DateTimeFormatter dateFormatNoYearUs = DateTimeFormat.forPattern("dd MMM")
 		.withLocale(Locale.US);
 
	private static final DateTimeFormatter dayFormatFin = DateTimeFormat.forPattern("E");
	private static final DateTimeFormatter dayFormatUs = DateTimeFormat.forPattern("E").withLocale(
		Locale.US);;
 
 	private static final DateTimeFormatter[] dateFormatters = { dateFormatFin, dateFormatNoYearFin,
 		dateFormatUs, dateFormatNoYearUs };
 
 	private static final DateTimeFormatter[] dayFormatters = { dayFormatFin, dayFormatUs };
 
 	/**
 	 * Return either a ReadablePartial or DateTime depending on if the date
 	 * included year
 	 */
 	public static Object parseDate(String str) {
 		for (DateTimeFormatter format : dateFormatters) {
 			try {
 				if (format == dateFormatNoYearFin) return MonthDay.parse(str, dateFormatNoYearFin);
 				else if (format == dateFormatNoYearUs) return MonthDay.parse(str, dateFormatNoYearUs);
 				else return format.parseDateTime(str);
 			}
 			catch (IllegalArgumentException e) {}
 		}
 
 		throw new IllegalArgumentException("Unable to parse date string: " + str);
 	}
 
 	public static String toUsString(DateTime date) {
 		return dateFormatUs.print(date);
 	}
 
 	/**
 	 * Return a weekday number from weekday name
 	 */
 	public static int parseWeekday(String str) {
 		str = str.toLowerCase(); // Joda wants lowercase weekdays!
 		for (DateTimeFormatter format : dayFormatters) {
 			try {
 				return format.parseDateTime(str).getDayOfWeek();
 			}
 			catch (IllegalArgumentException e) {}
 		}
 
 		throw new IllegalArgumentException("Unable to parse date string: " + str);
 	}
 
 	/**
 	 * Normalize date string into Finnish dd.MM.yy style string
 	 */
 	public static String normalizeDateStr(String dateStr) {
 		try {
 			DateTime date = dateFormatFin.parseDateTime(dateStr);
 			return dateFormatUs.print(date);
 		}
 		catch (IllegalArgumentException e) {
 			return dateStr;
 		}
 	}
 
 	/**
 	 * Parse an interval string like "14.03. - 09.05.2012" or
 	 * "07 Sep - 19 Oct 11" into Interval
 	 */
 	public static Interval parseInterval(String durationStr) {
 
 		String[] tokens = durationStr.split("-");
 		tokens[0] = tokens[0].trim();
 		tokens[1] = tokens[1].trim();
 
 		Object dateObj1 = parseDate(tokens[0]);
 		DateTime date2 = (DateTime)parseDate(tokens[1]);
 		DateTime date1;
 		if (dateObj1 instanceof ReadablePartial) {
 			date1 = ((ReadablePartial)dateObj1).toDateTime(date2);
 		}
 		else date1 = (DateTime)dateObj1;
 
 		return new Interval(date1, date2);
 	}
 }
