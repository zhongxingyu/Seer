 package edu.uw.cs.lil.tiny.tempeval.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.joda.time.LocalDate;
 
 import edu.uw.cs.lil.tiny.tempeval.types.TemporalDate;
 import edu.uw.cs.lil.tiny.tempeval.types.TemporalISO;
 
 public class TemporalUtil {
 	private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
 	
 	public static LocalDate convertISOToLocalDate(TemporalISO d) {
 		if (!d.isSet("year") || !d.isSet("month") || !d.isSet("day")){
 			throw new IllegalArgumentException("The date " + d + " doesn't have all the required fields to specify a LocalDate ojbect.");
 		}
 		int year = TemporalISO.getValueFromDate(d, "year");
 		int month = TemporalISO.getValueFromDate(d, "month");
 		int day = TemporalISO.getValueFromDate(d, "day");
 		return new LocalDate(year, month, day);
 	}
 	
 	public static TemporalDate convertLocalDateToISO(LocalDate date) {
 		Map<String, Set<Integer>> tmpMap = new HashMap<String, Set<Integer>>();
 		tmpMap.put("year", new HashSet<Integer>());
 		tmpMap.put("month", new HashSet<Integer>());
 		tmpMap.put("day", new HashSet<Integer>());
 		(tmpMap.get("year")).add(Integer.valueOf(date.getYear()));
 		(tmpMap.get("month")).add(Integer.valueOf(date.getMonthOfYear()));
 		(tmpMap.get("day")).add(Integer.valueOf(date.getDayOfMonth()));
 		return new TemporalDate(tmpMap);
 	}
 	
 	public static Calendar getCalendar(String value) {
 		Calendar cal=Calendar.getInstance();
 	    try {
 			cal.setTime(ISO_FORMAT.parse(value));
 		} catch (ParseException e) {
 			return null;
		} catch (NumberFormatException e) {
			return null;
 		}
 	    return cal;
 	}
 
 }
