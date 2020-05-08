 package strawberries.drycleaner;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 
 public class BusinessHourCalculator {
 	enum DayOfWeek {
 		SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
 	}
 
 	Map<DayOfWeek, OpeningTime> openingTimes = new EnumMap<DayOfWeek, OpeningTime>(DayOfWeek.class); //opening time based on DayOfWeek enum
 	Map<String, OpeningTime> additionalOpeningTimes = new HashMap<String, OpeningTime>(); //opening time based on formatted date like "2012-10-12"
 	Set<String> additionalDaysStoreIsClosed = new HashSet<String>(); //based on formatted date like "2012-10-12"
 	
 	public BusinessHourCalculator(String defaultOpeningTime, String defaultClosingTime) throws ParseException {
 		init(defaultOpeningTime, defaultClosingTime);
 	}
 	
 	public Date calculateDeadline(long timeInterval, String desiredDropOffTimestamp) throws ParseException {
 		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 		Date desiredDropOffDateTime = sdf.parse(desiredDropOffTimestamp);
 
 		desiredDropOffDateTime = getClosestValidBusinessDay(desiredDropOffDateTime);
 		Date openingHourForDesiredDropOffDay = getOpeningHourForDate(desiredDropOffDateTime);
 		Date closingHourForDesiredDropOffDay = getClosingHourForDate(desiredDropOffDateTime);
 		
 		if(desiredDropOffDateTime.before(openingHourForDesiredDropOffDay)){
 			desiredDropOffDateTime = openingHourForDesiredDropOffDay;
 		}
 		long diffInMiliseconds = closingHourForDesiredDropOffDay.getTime()	- desiredDropOffDateTime.getTime();
 		long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMiliseconds);		
 		
 		if (diffInSeconds < timeInterval) {	
 			long carryOver = diffInSeconds - timeInterval;
 			carryOver = carryOver<0 ? -carryOver: carryOver;
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTime(desiredDropOffDateTime);
 			calendar.add(Calendar.DAY_OF_MONTH, 1);
 			Date closestValidBusinessDay = getClosestValidBusinessDay(calendar.getTime());
 			Date desiredDropOffDateOpeningTime = getOpeningHourForDate(closestValidBusinessDay);
 			Date desiredDropOffDateClosingTime = getClosingHourForDate(closestValidBusinessDay);
 			while (true) {
 				long businessHoursInSeconds = TimeUnit.MILLISECONDS.toSeconds(desiredDropOffDateClosingTime.getTime() - desiredDropOffDateOpeningTime.getTime());
 				if (carryOver > businessHoursInSeconds) {
 					carryOver = carryOver - businessHoursInSeconds;
 					calendar.add(Calendar.DAY_OF_MONTH, 1);
 					closestValidBusinessDay = getClosestValidBusinessDay(calendar.getTime());
 					desiredDropOffDateOpeningTime = getOpeningHourForDate(closestValidBusinessDay);
 					desiredDropOffDateClosingTime = getClosingHourForDate(closestValidBusinessDay);						
 				} else {
 					timeInterval = carryOver;
 					desiredDropOffDateTime = desiredDropOffDateOpeningTime;
 					break;
 				}
 			}
 		}
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(desiredDropOffDateTime);
 		calendar.add(Calendar.SECOND, (int)timeInterval);
 		return calendar.getTime();
 	}
 
 	private Date getClosestValidBusinessDay(Date desiredDropOffDateTime) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(desiredDropOffDateTime);
 		while (isClosed(calendar.getTime())) {
 			calendar.add(Calendar.DAY_OF_MONTH, 1);
 		}
 		return calendar.getTime();
 	}
 
 	private boolean isClosed(Date desiredDropOffDateTime) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(desiredDropOffDateTime);
 		DayOfWeek dayOfWeek = DayOfWeek.values()[calendar.get(Calendar.DAY_OF_WEEK) - 1];
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		String formatedDate = sdf.format(desiredDropOffDateTime);
 		if (additionalDaysStoreIsClosed.contains(formatedDate) || !openingTimes.containsKey(dayOfWeek)){
 			return true;
 		}
 		return false;
 	}
 	
 	public void setOpeningHours(DayOfWeek dayOfWeek, String openingTime, String closingTime) throws ParseException {
 		openingTimes.put(dayOfWeek, new OpeningTime(openingTime, closingTime));
 	}
 	
 	public void setOpeningHours(String day, String openingTime, String closingTime) throws ParseException {		
 		additionalOpeningTimes.put(day, new OpeningTime(openingTime, closingTime));
 	}
 	
 	public void setClosed(DayOfWeek... daysOfWeek){
 		for(DayOfWeek dayOfWeek: daysOfWeek){
 			openingTimes.remove(dayOfWeek);
 		}		
 	}
 	
 	public void setClosed(String... days){
 		for(String day: days){
 			additionalDaysStoreIsClosed.add(day);
 		}		
 	}
 
 	
 	private Date getOpeningHourForDate(Date aDate) {		
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		String formattedDate = sdf.format(aDate);
 		Calendar aDateCalendar = Calendar.getInstance();
 		aDateCalendar.setTime(aDate);
 		Calendar openingTimeCalendar = Calendar.getInstance();
 		if (additionalOpeningTimes.containsKey(formattedDate)){		
 			Date openingTime = additionalOpeningTimes.get(formattedDate).getStart();
 			openingTimeCalendar.setTime(openingTime);
 			aDateCalendar.set(Calendar.HOUR_OF_DAY,	openingTimeCalendar.get(Calendar.HOUR_OF_DAY));
 			aDateCalendar.set(Calendar.MINUTE, openingTimeCalendar.get(Calendar.MINUTE));		
 		} else {
 			DayOfWeek dayOfWeek = DayOfWeek.values()[aDateCalendar.get(Calendar.DAY_OF_WEEK) - 1];
 			Date openingTime = openingTimes.get(dayOfWeek).getStart();
 			openingTimeCalendar.setTime(openingTime);
 			aDateCalendar.set(Calendar.HOUR_OF_DAY,	openingTimeCalendar.get(Calendar.HOUR_OF_DAY));
 			aDateCalendar.set(Calendar.MINUTE, openingTimeCalendar.get(Calendar.MINUTE));
 		}
 		// the opening time for the requested date
 		return aDateCalendar.getTime();
 	}
 	
 	
 	private Date getClosingHourForDate(Date aDate) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		String formattedDate = sdf.format(aDate);
 		Calendar aDateCalendar = Calendar.getInstance();
 		aDateCalendar.setTime(aDate);
 		Calendar closingTimeCalendar = Calendar.getInstance();
 		if (additionalOpeningTimes.containsKey(formattedDate)){		
 			Date closingTime = additionalOpeningTimes.get(formattedDate).getEnd();
 			closingTimeCalendar.setTime(closingTime);
 			aDateCalendar.set(Calendar.HOUR_OF_DAY,	closingTimeCalendar.get(Calendar.HOUR_OF_DAY));
 			aDateCalendar.set(Calendar.MINUTE, closingTimeCalendar.get(Calendar.MINUTE));		
 		} else {
 			DayOfWeek dayOfWeek = DayOfWeek.values()[aDateCalendar.get(Calendar.DAY_OF_WEEK) - 1];
 			Date closingTime = openingTimes.get(dayOfWeek).getEnd();
 			closingTimeCalendar.setTime(closingTime);
 			aDateCalendar.set(Calendar.HOUR_OF_DAY,	closingTimeCalendar.get(Calendar.HOUR_OF_DAY));
 			aDateCalendar.set(Calendar.MINUTE, closingTimeCalendar.get(Calendar.MINUTE));
 		}
 		// the closing time for the requested date
 		return aDateCalendar.getTime();
 	}
 
 	private void init(String defaultOpeningTime, String defaultClosingTime)
 			throws ParseException {
 		for (DayOfWeek d : DayOfWeek.values()) {
 			openingTimes.put(d, new OpeningTime(defaultOpeningTime,	defaultClosingTime));
 		}
 	}
 
 	 private class OpeningTime {
 		Date start;
 		Date end;
 		DateFormat sdf = new SimpleDateFormat("HH:mm");
 
 		OpeningTime(String start, String end) throws ParseException {
 			this.start = sdf.parse(start);
 			this.end = sdf.parse(end);
 		}
 
 		Date getStart() {
 			return this.start;
 		}
 
 		Date getEnd() {
 			return this.end;
 		}
 
 	}
 
 }
