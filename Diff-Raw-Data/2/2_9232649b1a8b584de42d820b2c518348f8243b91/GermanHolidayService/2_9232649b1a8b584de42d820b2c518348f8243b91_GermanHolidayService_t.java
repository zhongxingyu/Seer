 package com.uwusoft.timesheet.germanholiday;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.extensionpoint.HolidayService;
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.util.BusinessDayUtil;
 
 public class GermanHolidayService implements HolidayService {
 	public static final String PROPERTY = "holiday.german.valid";
 	private static Map<Date, String> holidays = new HashMap<Date, String>();
 	private static Map<Date, String> regionValidHolidays = new HashMap<Date, String>();
 	private static List<String> regionValidHolidaysList 
 						= Arrays.asList(new String[] {"epiphany", "mariaAscension", "allSaintsDay", "dayOfRepentance", "corpusChristi"});
 	
 	public GermanHolidayService() {
 	}
 
 	@Override
 	public List<Date> getOfflimitDates(int year) {
 		Calendar baseCalendar = GregorianCalendar.getInstance();
 		baseCalendar.clear();
 
 		// Add in the static dates for the year.
 		baseCalendar.set(year, Calendar.JANUARY, 1);
 		holidays.put(baseCalendar.getTime(), "newYearsDay");
 
 		baseCalendar.set(year, Calendar.JANUARY, 6);
 		regionValidHolidays.put(baseCalendar.getTime(), "epiphany");
 		
 		baseCalendar.set(year, Calendar.MAY, 1);
		holidays.put(baseCalendar.getTime(), "labourDay");
 
 		baseCalendar.set(year, Calendar.AUGUST, 15);
 		regionValidHolidays.put(baseCalendar.getTime(), "mariaAscension");
 
 		baseCalendar.set(year, Calendar.OCTOBER, 3);
 		holidays.put(baseCalendar.getTime(), "germanUnificationDay");
 
 		baseCalendar.set(year, Calendar.OCTOBER, 31);
 		holidays.put(baseCalendar.getTime(), "reformationDay");
 
 		baseCalendar.set(year, Calendar.NOVEMBER, 1);
 		regionValidHolidays.put(baseCalendar.getTime(), "allSaintsDay");
 
 		// TODO: Bu- und Bettag
 		// Der letzte Mittwoch vor dem 23. November (letzter Sonntag nach Trinitatis)
 		// Gets 3rd Wednesday in November
 		regionValidHolidays.put(BusinessDayUtil.calculateFloatingHoliday(3, Calendar.WEDNESDAY, year, Calendar.NOVEMBER), "dayOfRepentance");		
 
 		baseCalendar.set(year, Calendar.DECEMBER, 25);
 		holidays.put(baseCalendar.getTime(), "xmasDay");
 
 		baseCalendar.set(year, Calendar.DECEMBER, 26);
 		holidays.put(baseCalendar.getTime(), "boxingDay");
 
 		// Now deal with floating holidays.
 		// Ostersonntag
 		Date osterSonntag = BusinessDayUtil.getOsterSonntag(year);
 		holidays.put(BusinessDayUtil.addDays(osterSonntag, -2), "goodFriday");
 		holidays.put(BusinessDayUtil.addDays(osterSonntag, 1), "easterMonday");
 		holidays.put(BusinessDayUtil.addDays(osterSonntag, 39), "ascensionDay");
 		holidays.put(BusinessDayUtil.addDays(osterSonntag, 50), "whitMonday");
 		
 		regionValidHolidays.put(BusinessDayUtil.addDays(osterSonntag, 60), "corpusChristi");
 
 		return new ArrayList<Date>(holidays.keySet());
 	}
 	
 	@Override
 	public boolean isValid(Date date) {
 		return holidays.containsKey(date) || Arrays.asList(Activator.getDefault().getPreferenceStore().getString(PROPERTY)
 				.split(SubmissionService.separator)).contains(regionValidHolidays.get(date));
 	}
 	
 	@Override
 	public String getName(Date date) {
 		if (holidays.get(date) == null)
 			return Messages.getString(regionValidHolidays.get(date));
 		return Messages.getString(holidays.get(date));
 	}
 	
 	@Override
 	public String getName(String name) {
 		return Messages.getString(name);
 	}
 	
 	@Override
 	public Collection<String> getRegionValidHolidays() {
 		return regionValidHolidaysList;
 	}
 }
