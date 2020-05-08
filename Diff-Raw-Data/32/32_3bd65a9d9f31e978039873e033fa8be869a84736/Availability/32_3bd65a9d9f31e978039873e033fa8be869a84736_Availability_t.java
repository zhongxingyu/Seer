 package cours.ulaval.glo4003.domain;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import cours.ulaval.glo4003.controller.model.AvailabilityModel;
 
 public class Availability {
 
 	private static final int STARTING_HOUR = 8;
 	private static final int THIRTY_MINUTES = 30;
 	private String idul;
 	private List<TimeSlot> availabilities = new ArrayList<TimeSlot>();
 
 	public Availability() {
 		super();
 	}
 
	public Availability(AvailabilityModel availability, String idul) {
		findTimeSlots(availability.getMonday(), Calendar.MONDAY);
		findTimeSlots(availability.getTuesday(), Calendar.TUESDAY);
		findTimeSlots(availability.getWednesday(), Calendar.WEDNESDAY);
		findTimeSlots(availability.getThursday(), Calendar.THURSDAY);
		findTimeSlots(availability.getFriday(), Calendar.FRIDAY);
 		this.idul = idul;
 	}
 
 	private void findTimeSlots(List<Boolean> listOfBoolean, int dayOfWeek) {
 		int duration = 0;
 		listOfBoolean.add(false);
 		for (int i = 0; i < listOfBoolean.size(); i++) {
 
 			if (listOfBoolean.get(i)) {
 				duration++;
 			} else if (duration != 0) {
 				int start = i - duration;
 
 				Calendar startTime = convertToTimeSlot(start, dayOfWeek);
 
 				availabilities.add(new TimeSlot(startTime, duration));
 
 				duration = 0;
 			}
 		}
 	}
 
 	private Calendar convertToTimeSlot(int start, int dayOfWeek) {
 		Calendar startTime = Calendar.getInstance();
 
 		if (start + STARTING_HOUR > 12) {
 			// Reformat to 12h format
 			start = (start + STARTING_HOUR) - 12;
 			startTime.set(Calendar.AM_PM, Calendar.PM);
 		} else {
 			start = (start + STARTING_HOUR);
 			startTime.set(Calendar.AM_PM, Calendar.AM);
 		}
 
 		startTime.set(Calendar.HOUR, start);
 		startTime.set(Calendar.MINUTE, THIRTY_MINUTES);
 		startTime.set(Calendar.DAY_OF_WEEK, dayOfWeek);
 
 		return startTime;
 	}
 
 	public String getIdul() {
 		return idul;
 	}
 
 	public void setIdul(String idul) {
 		this.idul = idul;
 	}
 
 	public List<TimeSlot> getAvailabilities() {
 		return availabilities;
 	}
 
	public void setAvailabilities(List<TimeSlot> availabilities) {
		this.availabilities = availabilities;
 	}
 
 }
