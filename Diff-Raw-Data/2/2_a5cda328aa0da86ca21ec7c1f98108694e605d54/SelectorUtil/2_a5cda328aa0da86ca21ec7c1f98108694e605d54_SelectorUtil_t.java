 package com.huydung.utils;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.TimeZone;
 import java.util.TreeMap;
 
 import com.huydung.helpers.DateFormatOption;
 import com.huydung.helpers.TimeZoneOption;
 
 public class SelectorUtil {
 	
 	public static ArrayList<TimeZoneOption> getTimezones(){
 		Date d = new Date();
 		SimpleDateFormat dF = new SimpleDateFormat("HH:mm''");
 		String[] ids = TimeZone.getAvailableIDs();
 		final String TIMEZONE_ID_PREFIXES =
 		      "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";
 		ArrayList<TimeZoneOption> timezones = new ArrayList<TimeZoneOption>();
 		
 		for( String id : ids ){
 			if( id.matches(TIMEZONE_ID_PREFIXES) ){
 				TimeZone tz = TimeZone.getTimeZone(id);
 				dF.setTimeZone(tz);
			    TimeZoneOption tzo = new TimeZoneOption(id, dF.format(d) + "(" + id + ")");
 			    timezones.add(tzo);
 			}			
 		}
 		
 		Collections.sort(timezones, new Comparator<TimeZoneOption>() {
 			@Override
 			public int compare(TimeZoneOption o1, TimeZoneOption o2) {
 				int result = o1.getLabel().compareTo(o2.getLabel());
 				if( result == 0 ){
 					result = o1.getId().compareTo(o2.getId());
 				}
 				return result;
 			}
 		});
 		return timezones;
 	}
 	
 	public static ArrayList<DateFormatOption> getDateFormats(){
 		ArrayList<DateFormatOption> formats = new ArrayList<DateFormatOption>();
 		formats.add(new DateFormatOption("dd/MM/YYYY", "14/06/2011"));
 		formats.add(new DateFormatOption("MM/dd/YYYY", "06/14/2011"));
 		formats.add(new DateFormatOption("dd MMM, YYYY", "14 Jun, 2011"));
 		formats.add(new DateFormatOption("MMMMM dd, YYYY", "June 14, 2011"));
 		formats.add(new DateFormatOption("dd-MM-YYYY", "14-06-2011"));
 		formats.add(new DateFormatOption("MM-dd-YYYY", "06-14-2011"));
 		formats.add(new DateFormatOption("dd.MM.YYYY", "14.06.2011"));
 		formats.add(new DateFormatOption("MM.dd.YYYY", "06.14.2011"));
 		formats.add(new DateFormatOption("dd/MM", "14/06"));
 		formats.add(new DateFormatOption("MM/dd", "06/14"));		
 		formats.add(new DateFormatOption("dd-MM", "14-06"));
 		formats.add(new DateFormatOption("MM-dd", "06-14"));		
 		formats.add(new DateFormatOption("dd.MM", "14.06"));
 		formats.add(new DateFormatOption("MM.dd", "06.14"));
 		return formats;
 	}
 }
