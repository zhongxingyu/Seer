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
 import utilities.OccurrenceType;
 
 public class Events extends Main {
 	// regex for HH:mm
 	static final String regexTime = "^([0-1][0-9]|[2][0-3]):([0-5][0-9])$";
 
 	public static void updateEvent(Long calendarId, Date currentDate, Long id,
 			@Required String name, @Required Date startDate,
 			@Required String startTime, @Required Date endDate,
 			@Required String endTime, int occurrenceType, boolean isPublic,
 			String note) {
 
 		validation.match(startTime, regexTime).message("Invalid!");
 		validation.match(endTime, regexTime).message("Invalid!");
 
 		if (!validation.hasErrors()) {
 			startDate = Events.helperCreateDate(startDate, startTime, "HH:mm");
 			endDate = Events.helperCreateDate(endDate, endTime, "HH:mm");
 			validation.future(endDate, startDate);
 		}
 
 		if (validation.hasErrors()) {
 			params.flash();
 			flash.keep();
 
 			Calendars.putCalendarData(calendarId, currentDate);
 
 			renderTemplate("Events/editEvent.html");
 		}
 
 		Event event = Event.findById(id);
 		event.name = name;
 		event.start = Events.helperCreateDate(startDate, startTime, "HH:mm");
 		event.end = Events.helperCreateDate(endDate, endTime, "HH:mm");
 		event.note = note;
 		event.occurrenceType = OccurrenceType.getType(occurrenceType);
 		event.setPublic(isPublic);
 		event.save();
 
 		Calendars.viewCalendar(calendarId, startDate);
 	}
 
 	public static void saveEvent(Long calendarId, Date currentDate,
 			@Required String name, @Required Date startDate,
 			@Required String startTime, @Required Date endDate,
 			@Required String endTime, int occurrenceType, boolean isPublic,
 			String note) {
 
 		validation.match(startTime, regexTime).message("Invalid!");
 		validation.match(endTime, regexTime).message("Invalid!");
 
 		if (!validation.hasErrors()) {
 			startDate = Events.helperCreateDate(startDate, startTime, "HH:mm");
 			endDate = Events.helperCreateDate(endDate, endTime, "HH:mm");
 			validation.future(endDate, startDate);
 		}
 
 		if (validation.hasErrors()) {
 			params.flash();
 			flash.keep();
 			Calendars.putCalendarData(calendarId, currentDate);
 
 			renderTemplate("Events/addEvent.html");
 		}
 
 		Calendar calendar = Calendar.findById(calendarId);
 		User owner = calendar.owner;
 
 		new Event(name, note, startDate, endDate, owner, calendar, isPublic,
 				OccurrenceType.getType(occurrenceType)).save();
 
 		calendar.save();
 
 		Calendars.viewCalendar(calendarId, startDate);
 	}
 
 	public static void deleteEvent(Long calendarId, Date currentDate,
 			Long eventId) {
 
 		Query deleteQuery = JPA.em()
 				.createQuery("SELECT e FROM Event e WHERE e.id = :id")
 				.setParameter("id", eventId);
 
 		Event event = (Event) deleteQuery.getSingleResult();
 		event.delete();
 
 		Calendars.viewCalendar(calendarId, currentDate);
 	}
 
 	static Date helperCreateDate(Date date, String timeString, String pattern) {
 		DateTimeFormatter parser = DateTimeFormat.forPattern("HH:mm");
 		DateTime time = parser.parseDateTime(timeString);
 		DateTime aDate = new DateTime(date);
 		aDate = aDate.withTime(time.getHourOfDay(), time.getMinuteOfHour(),
 				time.getSecondOfMinute(), time.getMillisOfSecond());
 		return aDate.toDate();
 	}
 
 	public static void addEvent(Long calendarId, Date currentDate) {
 		Calendars.putCalendarData(calendarId, currentDate);
 
 		flash.clear();
 
 		DateTime now = new DateTime();
 		DateTime start = new DateTime(currentDate);
 		DateTime end = new DateTime(currentDate);
 		start = start.withTime(now.getHourOfDay(), 0, 0, 0).plusHours(1);
 		end = end.withTime(now.getHourOfDay(), 0, 0, 0).plusHours(2);
 		flash.put("startDate", start.toString("yyyy-MM-dd"));
 		flash.put("endDate", end.toString("yyyy-MM-dd"));
 		flash.put("startTime", start.toString("HH:mm"));
 		flash.put("endTime", end.toString("HH:mm"));
 
 		renderTemplate("Events/addEvent.html");
 	}
 
 	public static void editEvent(Long calendarId, Date currentDate, Long eventId) {
 		Calendars.putCalendarData(calendarId, currentDate);
 
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
		flash.put("repeatableType", event.occurrenceType.getId());
 		flash.put("isPublic", event.isPublic);
 		flash.put("note", event.note);
 
 		renderTemplate("Events/editEvent.html");
 	}
 
 	public static void copyEvent(Long eventId) {
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
 
 	public static void followEvent(Long followCalendarId, Long eventId) {
 		Event event = Event.find("byId", eventId).first();
 		Calendar calendar = Calendar.find("byId", followCalendarId).first();
 
 		event.follow(calendar);
 		event.save();
 		calendar.save();
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
 
 }
