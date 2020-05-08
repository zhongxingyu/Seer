 package org.strangeforest.currencywatch;
 
 import java.util.*;
 
 import org.joda.time.*;
 import org.strangeforest.currencywatch.core.*;
 import org.strangeforest.util.*;
 
 public abstract class Util {
 
 	public static final String BASE_CURRENCY = "RSD";
 
 	public static final LocalDate START_DATE = new LocalDate(2002, 5, 15);
 
	public static final long MILLISECONDS_PER_DAY = Period.days(1).getMillis();
 
 	public static LocalDate getLastDate() {
 		LocalDate lastDate = new LocalDate();
 		if (new LocalTime().isBefore(new LocalTime(8, 0)))
 			lastDate = lastDate.minusDays(1);
 		return lastDate;
 	}
 
 	public static DateRange trimDateRange(DateRange range) {
 		Date minFrom = START_DATE.toDate();
 		Date maxTo = getLastDate().toDate();
 		return new DateRange(ObjectUtil.max(range.getFrom(), minFrom), ObjectUtil.min(range.getTo(), maxTo));
 	}
 
 	public static int dayDifference(Date fromDate, Date toDate) {
 		return (int)new Duration(new DateTime(fromDate), new DateTime(toDate)).getStandardDays();
 	}
 
 	public static Date extractDate(Date date) {
 		return new DateTime(date).toLocalDate().toDateTimeAtStartOfDay().toDate();
 	}
 }
