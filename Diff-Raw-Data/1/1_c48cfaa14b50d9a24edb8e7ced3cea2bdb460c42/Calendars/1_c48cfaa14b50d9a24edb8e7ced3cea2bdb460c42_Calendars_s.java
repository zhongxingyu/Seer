 package controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import models.Calendar;
 import models.Event;
 import models.User;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import play.data.validation.Required;
 import play.db.jpa.JPA;
 import play.mvc.With;
 import utilities.CalendarHelper;
 import utilities.GlobalCalendar;
 import utilities.RepeatableType;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 @With(Secure.class)
 public class Calendars extends Main {
     // regex for HH:mm
     private static final String regexTime = "^([0-1][0-9]|[2][0-3]):([0-5][0-9])$";
 
     private static void putCalendarData(Long calendarId, Date currentDate) {
 	Query calendarQuery = JPA.em()
 		.createQuery("SELECT c FROM Calendar c WHERE c.id = :id")
 		.setParameter("id", calendarId);
 	Calendar calendar = (Calendar) calendarQuery.getSingleResult();
 	if (currentDate == null) {
 	    currentDate = new Date();
 	}
 
 	renderArgs.put("calendar", calendar);
 	renderArgs.put("calendarData",
 		calendar.getCalendarData(getUser(), currentDate));
 	renderArgs.put("currentDate", currentDate);
     }
 
     private static void putGlobalCalendarData(Calendar calendar,
 	    Date currentDate) {
 	if (currentDate == null) {
 	    currentDate = new Date();
 	}
 	renderArgs.put("calendar", calendar);
 	renderArgs.put("calendarData",
 		calendar.getCalendarData(getUser(), currentDate));
 	renderArgs.put("currentDate", currentDate);
     }
 
     public static void index() {
 	Query calendarQuery = JPA.em()
 		.createQuery("SELECT c FROM Calendar c WHERE c.owner.id = :id")
 		.setParameter("id", getUser().id);
 	List<Calendar> calendarList = calendarQuery.getResultList();
 	// TODO: estimate what to do if an user don't got any calendars.
 	assert calendarList.size() != 0;
 	viewCalendar(calendarList.get(0).getId(), new Date());
     }
 
     public static void viewGlobalCalendar(Date currentDate) {
 	Calendar calendar = new GlobalCalendar("global", getUser());
 	putGlobalCalendarData(calendar, currentDate);
 	renderTemplate("Calendars/viewStrangerCalendar.html");
     }
 
     public static void viewCalendar(Long calendarId, Date currentDate) {
 	if (calendarId == 0) {
 	    viewGlobalCalendar(currentDate);
 	}
 
 	if (currentDate == null) {
 	    currentDate = new Date();
 	}
 
 	putCalendarData(calendarId, currentDate);
 	Query calendarQuery = JPA.em()
 		.createQuery("SELECT c FROM Calendar c WHERE c.id = :id")
 		.setParameter("id", calendarId);
 	Calendar calendar = (Calendar) calendarQuery.getSingleResult();
 	if (getUser().equals(calendar.owner)) {
 	    renderTemplate("Calendars/viewCalendar.html");
 	} else {
 	    renderTemplate("Calendars/viewStrangerCalendar.html");
 	}
     }
 
     public static void viewNextMonth(Long calendarId, Date currentDate) {
 	viewCalendar(calendarId, new DateTime(currentDate).plusMonths(1)
 		.toDate());
     }
 
     public static void viewPrevMonth(Long calendarId, Date currentDate) {
 	viewCalendar(calendarId, new DateTime(currentDate).minusMonths(1)
 		.toDate());
     }
 
     public static void editCalendar() {
     }
 
     public static void addEvent(Long calendarId, Date currentDate) {
 	putCalendarData(calendarId, currentDate);
 	renderArgs.put("actionName", "Add Event");
 
 	DateTime now = new DateTime();
 	DateTime start = new DateTime(currentDate);
 	DateTime end = new DateTime(currentDate);
 	start = start.withTime(now.getHourOfDay(), 0, 0, 0).plusHours(1);
 	end = end.withTime(now.getHourOfDay(), 0, 0, 0).plusHours(2);
 	flash.put("startDate", start.toString("yyyy-MM-dd"));
 	flash.put("endDate", end.toString("yyyy-MM-dd"));
 	flash.put("startTime", start.toString("HH:mm"));
 	flash.put("endTime", end.toString("HH:mm"));
 
 	renderTemplate("Calendars/viewEvent.html");
     }
 
     public static void editEvent(Long calendarId, Date currentDate, Long eventId) {
 	putCalendarData(calendarId, currentDate);
 	renderArgs.put("actionName", "Edit Event");
 
 	Query eventQuery = JPA.em()
 		.createQuery("SELECT e FROM Event e WHERE e.id = :id")
 		.setParameter("id", eventId);
 	Event event = (Event) eventQuery.getSingleResult();
 
 	flash.put("id", event.id);
 	flash.put("name", event.name);
 	flash.put("startDate", new DateTime(event.start).toString("yyyy-MM-dd"));
 	flash.put("endDate", new DateTime(event.end).toString("yyyy-MM-dd"));
 	flash.put("startTime", new DateTime(event.start).toString("HH:mm"));
 	flash.put("endTime", new DateTime(event.end).toString("HH:mm"));
 	renderArgs.put("repeatableType", event.repeatableType.getId());
 	renderArgs.put("isPublic", event.isPublic);
 	flash.put("note", event.note);
 
 	renderTemplate("Calendars/viewEvent.html");
     }
 
     public static void saveEvent(Long calendarId, Date currentDate, Long id,
 	    @Required String name, @Required Date startDate,
 	    @Required String startTime, @Required Date endDate,
 	    @Required String endTime, int repeatableType, boolean isPublic,
 	    String note) {
 
 	validation.match(startTime, regexTime).message("Invalid!");
 	validation.match(endTime, regexTime).message("Invalid!");
 
 	if (!validation.hasErrors()) {
 	    startDate = helperCreateDate(startDate, startTime, "HH:mm");
 	    endDate = helperCreateDate(endDate, endTime, "HH:mm");
 	    validation.future(endDate, startDate);
 	}
 
 	if (validation.hasErrors()) {
 	    flash.keep();
 
 	    Calendars.putCalendarData(calendarId, currentDate);
 	    renderArgs.put("actionName", "Add Event");
 
 	    renderTemplate("Calendars/viewEvent.html");
 	}
 
 	Calendar calendar = Calendar.findById(calendarId);
 	User owner = calendar.owner;
 
 	// new event
 	if (id == null) {
 	    Event event = new Event(name, note, startDate, endDate, owner,
 		    calendar, isPublic, RepeatableType.getType(repeatableType))
 		    .save();
 
 	    event.save();
 	    calendar.save();
 	}
 
 	// edit event
 	if (id != null) {
 	    Event event = Event.findById(id);
 	    event.name = name;
 	    event.start = helperCreateDate(startDate, startTime, "HH:mm");
 	    event.end = helperCreateDate(endDate, endTime, "HH:mm");
 	    event.note = note;
 	    event.repeatableType = RepeatableType.getType(repeatableType);
 	    event.setPublic(isPublic);
 	    event.save();
 	}
 
 	viewCalendar(calendarId, startDate);
     }
 
     public static void deleteEvent(Long calendarId, Date currentDate,
 	    Long eventId) {
 
 	Query deleteQuery = JPA.em()
 		.createQuery("SELECT e FROM Event e WHERE e.id = :id")
 		.setParameter("id", eventId);
 
 	Event event = (Event) deleteQuery.getSingleResult();
 	event.delete();
 
 	viewCalendar(calendarId, currentDate);
     }
 
     public static void addCalendar(String calendarName) {
 	User loginUser = getUser();
 	Calendar calendar = new Calendar(calendarName, loginUser).save();
 	loginUser.addCalendar(calendar);
 
 	viewCalendar(calendar.id, null);
     }
 
     private static Date helperCreateDate(Date date, String timeString,
 	    String pattern) {
 	DateTimeFormatter parser = DateTimeFormat.forPattern("HH:mm");
 	DateTime time = parser.parseDateTime(timeString);
 	DateTime aDate = new DateTime(date);
 	aDate = aDate.withTime(time.getHourOfDay(), time.getMinuteOfHour(),
 		time.getSecondOfMinute(), time.getMillisOfSecond());
 	return aDate.toDate();
     }
 
     public static void copyEvent(Long calendarId, Date currentDate, Long eventId) {
 	Calendars.putCalendarData(calendarId, currentDate);
 	Event event = (Event) JPA.em()
 		.createQuery("SELECT e FROM Event e WHERE e.id = :id")
 		.setParameter("id", eventId).getSingleResult();
 
 	List<Calendar> calendars = JPA.em()
 		.createQuery("SELECT c FROM Calendar c WHERE c.owner.id = :id")
 		.setParameter("id", getUser().id).getResultList();
 
 	List<Calendar> list = new ArrayList<Calendar>();
 	for (Calendar calendar : calendars) {
 	    if (!calendar.events.contains(event)) {
 		list.add(calendar);
 	    }
 	}
 
 	renderArgs.put("event", event);
 	renderArgs.put("calendars", list);
 	renderTemplate("Calendars/chooseCalendar.html");
     }
 
     public static void followEvent(Long calendarId, Date currentDate,
 	    Long followCalendarId, Long eventId) {
 
 	Event event = Event.find("byId", eventId).first();
 	Calendar calendar = Calendar.find("byId", followCalendarId).first();
 
 	event.follow(calendar);
 	event.save();
 	calendar.save();
 
 	Calendars.viewCalendar(calendarId, currentDate);
     }
 
     public static void unfollowEvent(Long calendarId, Long eventId) {
 	Calendar calendar = Calendar.find("byId", calendarId).first();
 	Event event = Event.find("byId", eventId).first();
 
 	event.unfollow(calendar);
 	event.save();
 	calendar.save();
 
 	Date aDate = event.start;
 	Calendars.viewCalendar(calendarId, aDate);
     }
 
     public static void overlappingEvents(@Required String startDate,
 	    @Required String endDate) {
 	String format = "yyyy-MM-dd-HH-mm";
 	DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
 	// parse dates
 	Date start = formatter.parseDateTime(startDate).toDate();
 
 	Date end = formatter.parseDateTime(endDate).toDate();
 
 	// get events
 	User user = getUser();
 	List<Event> events = Event.find("byOwner", user).fetch();
 
 	// check of overlapping events
 	final List<Event> overlappingEvents = CalendarHelper.overlaps(events,
 		start, end);
 
 	Gson gson = new GsonBuilder().setPrettyPrinting()
 		.excludeFieldsWithoutExposeAnnotation().create();
 
 	renderJSON(gson.toJson(overlappingEvents));
     }
 }
