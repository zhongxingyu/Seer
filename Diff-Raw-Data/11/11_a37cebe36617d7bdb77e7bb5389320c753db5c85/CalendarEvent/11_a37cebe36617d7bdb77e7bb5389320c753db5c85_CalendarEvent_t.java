 /**
  *
  */
 package com.kangaroo.calendar;
 
 import java.util.Date;
 import java.util.Locale;
 
 import com.kangaroo.task.TaskConstraintHelper;
 import com.kangaroo.task.Task;
 import com.mobiletsm.routing.Place;
 /**
  * @author mrtazz
  * 
  * class representing calendar events with basic data
  *
  */
 public class CalendarEvent {
 
 	/**
 	 * simple event data
 	 */
 	/** the event id */
 	private String id;
 	/** event title */
 	private String title;
 	/** location name */
 	private String location;
 	/** longitude of the event location */
 	private Double locationLongitude;
 	/** latitude of the event location */
 	private Double locationLatitude;
 	/** Date when the event starts */
 	private Date startDate;
 	/** Date when the event stops */
 	private Date endDate;
 	/** whether or not the event was a task before */
 	private Boolean wasTask;
 	/** all day event */
 	private Boolean allDay;
 	/** description */
 	private String description;
 	/** calendar */
 	private int calendar;
 	/** timezone */
 	private String timezone;
 	/** mobileTSM places */
 	private Place place;
 
 	/**
 	 * @brief Constructor for event object
 	 *
 	 * @param id
 	 * @param title
 	 * @param location
 	 * @param locationLongitude
 	 * @param locationLatitude
 	 * @param startDate
 	 * @param endDate
 	 * @param wasTask
 	 * @param taskLink
 	 * @param allDay
 	 * @param description
 	 */
 	public CalendarEvent(String id, String title, String location,
 			Double locationLongitude, Double locationLatitude, Date startDate,
 			Date endDate, Boolean wasTask, Boolean allDay,
 			String description, int calendar, String timezone, Place place) {
 		this.id = id;
 		this.title = title;
 		this.location = location;
 		this.locationLongitude = locationLongitude;
 		this.locationLatitude = locationLatitude;
 		this.startDate = startDate;
 		this.endDate = endDate;
 		this.wasTask = wasTask;
 		this.allDay = allDay;
 		this.description = description;
 		this.calendar = calendar;
 		this.timezone = timezone;
 		this.place = place;
 	}
 	
 	public CalendarEvent() {
 		this.id = null;
 		this.title = null;
 		this.location = null;
 		this.locationLongitude = null;
 		this.locationLatitude = null;
 		this.startDate = null;
 		this.endDate = null;
 		this.wasTask = null;
 		this.allDay = null;
 		this.description = null;
 		this.calendar = -1;
 		this.timezone = null;
 		this.place = null;
 	}
 
 	
 	public CalendarEvent(Task task, Date now, Place here) {
 		/* TODO: complete this method */
 		
 		TaskConstraintHelper helper = new TaskConstraintHelper(task);
 		Date endDate = new Date(now.getTime() + helper.getDuration() * 1000 * 60);
 		
 		this.id = null;
 		this.title = task.getName();
 		this.location = here.toString();
 		this.locationLongitude = here.getLongitude();
 		this.locationLatitude = here.getLatitude();
 		this.startDate = now;
 		this.endDate = endDate;
 		this.wasTask = true;
 		this.allDay = null;
		this.description = task.getDescription()+"\n---\n"+task.serialize();
 		this.calendar = -1;
 		this.timezone = null;
 		this.place = here;
 	}
 	
 	
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the location
 	 */
 	public String getLocation() {
 		return location;
 	}
 
 	/**
 	 * @param location the location to set
 	 */
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	/**
 	 * @return the locationLongitude
 	 */
 	public Double getLocationLongitude() {
 		return locationLongitude;
 	}
 
 	/**
 	 * @param locationLongitude the locationLongitude to set
 	 */
 	public void setLocationLongitude(Double locationLongitude) {
 		this.locationLongitude = locationLongitude;
 	}
 
 	/**
 	 * @return the locationLatitude
 	 */
 	public Double getLocationLatitude() {
 		return locationLatitude;
 	}
 
 	/**
 	 * @param locationLatitude the locationLatitude to set
 	 */
 	public void setLocationLatitude(Double locationLatitude) {
 		this.locationLatitude = locationLatitude;
 	}
 
 	/**
 	 * @return the startDate
 	 */
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	/**
 	 * @param startDate the startDate to set
 	 */
 	public void setStartDate(Date startDate) {
 		this.startDate = startDate;
 	}
 
 	/**
 	 * @return the endDate
 	 */
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	/**
 	 * @param endDate the endDate to set
 	 */
 	public void setEndDate(Date endDate) {
 		this.endDate = endDate;
 	}
 
 	/**
 	 * @return the wasTask
 	 */
 	public Boolean getWasTask() {
 		return wasTask;
 	}
 
 	/**
 	 * @param wasTask the wasTask to set
 	 */
 	public void setWasTask(Boolean wasTask) {
 		this.wasTask = wasTask;
 	}
 
 	/**
 	 * @return the allDay
 	 */
 	public Boolean getAllDay() {
 		return allDay;
 	}
 
 	/**
 	 * @param allDay the allDay to set
 	 */
 	public void setAllDay(Boolean allDay) {
 		this.allDay = allDay;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description the description to set
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * @return the calendar
 	 */
 	public int getCalendar() {
 		return calendar;
 	}
 
 	/**
 	 * @param calendar the calendar to set
 	 */
 	public void setCalendar(int calendar) {
 		this.calendar = calendar;
 	}
 
 	/**
 	 * @return the timezone
 	 */
 	public String getTimezone() {
 		return timezone;
 	}
 
 	/**
 	 * @param timezone the timezone to set
 	 */
 	public void setTimezone(String timezone) {
 		this.timezone = timezone;
 	}
 
 	
 	/**
 	 * returns true if this calendar events specifies a location as coordinates
 	 * @return
 	 */
 	public boolean hasLocation() {
 		return (locationLatitude != null && locationLongitude != null);
 	}
 	
 	
 	/**
 	 * @return the place
 	 */
 	public Place getPlace() {
 		/* only return null if this calendar event doesn't 
 		 * specify any location as coordinates */
 		if (place == null && hasLocation()) {
 			place = new Place(getLocationLatitude(), getLocationLongitude());		
 		}
 		return place;
 	}
 
 	
 	/**
 	 * @deprecated use setLocationLatitude() and setLocationLongitude() to set location coordinates
 	 * @param place the place to set
 	 */
 	public void setPlace(Place place) {
 		this.place = place;
 	}
 	
 	
 	@Override
 	public String toString() {
 		
 		String startDate = "??";
 		if (getStartDate() != null) {
 			startDate = String.format(Locale.US, "%1$tH:%1$tM", getStartDate());
 		}
 		
 		String endDate = "??";
 		if (getEndDate() != null) {
 			endDate = String.format(Locale.US, "%1$tH:%1$tM", getEndDate());
 		}
 		
 		String title = "<no title>";
 		if (getTitle() != null) {
 			title = getTitle();
 		}
 		
 		return "CalendarEvent: {" + title + ", " + startDate + "-" + endDate + ", " + getPlace().toString() + "}";
 	}
 
 
 }
