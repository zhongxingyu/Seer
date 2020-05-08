 package biz.baldur.busme.model;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 public class BusStop implements Serializable {
 	private static final long serialVersionUID = -1979170903096663321L;
 	
 	String name;
 	Map<Integer, Schedule> schedules;
 	
 	public BusStop(String name) {
 		this.name = name;
 		this.schedules = new HashMap<Integer, Schedule>();
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void putSchedule(int weekday, Schedule schedule) {
 		this.schedules.put(weekday, schedule);
 	}
 	
 	public int getNextArrival(Calendar date) {
 		int weekday = date.get(Calendar.DAY_OF_WEEK);
 		
		int currTime = date.get(Calendar.HOUR)*60 + date.get(Calendar.MINUTE);
 		int arrivalTime = -1;
 		Schedule schedule;
 		
 		int addDays = -1;
 		
 		for(;arrivalTime == -1; addDays++) {
 			schedule = schedules.get(weekday);
 			arrivalTime = schedule.nextArrival(currTime);
 			weekday = nextWeekday(weekday);
 		}
 		
 		return addDays * 24 * 60 + arrivalTime - currTime;
 	}
 	
 	private int nextWeekday(int weekday) {
 		switch(weekday) {
 		case Calendar.SUNDAY:
 			return Calendar.MONDAY;
 		case Calendar.MONDAY:
 			return Calendar.TUESDAY;
 		case Calendar.TUESDAY:
 			return Calendar.WEDNESDAY;
 		case Calendar.WEDNESDAY:
 			return Calendar.THURSDAY;
 		case Calendar.THURSDAY:
 			return Calendar.FRIDAY;
 		case Calendar.FRIDAY:
 			return Calendar.SATURDAY;
 		case Calendar.SATURDAY:
 			return Calendar.SUNDAY;
 		default:
 			return -1;
 		}
 	}
 }
