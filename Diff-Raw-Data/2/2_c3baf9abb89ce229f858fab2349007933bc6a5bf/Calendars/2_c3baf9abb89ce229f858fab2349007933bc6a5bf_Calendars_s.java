 package controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.persistence.Query;
 import javax.persistence.TemporalType;
 
 import models.Calendar;
 import models.Event;
 import models.User;
 
 import org.joda.time.DateTime;
 
 import play.db.jpa.JPA;
 import play.mvc.With;
 
 @With(Secure.class)
 public class Calendars extends Application {
 
     /**
      * shows a list of calendars
      * 
      * @param nickname
      */
     public static void showCalendars(String nickname) {
 	User user = User.find("byNickname", nickname).first();
 	List<Calendar> calendars = user.calendars;
 	render(user, calendars);
     }
 
     /**
      * shows a calendar at the current date
      * 
      * @param nickname
      * @param id the calendar id
      */
     public static void showCalendar(String nickname, Long calendarId) {
 	Date aDate = new Date();
 	showDate(nickname, calendarId, aDate);
     }
 
     /**
      * shows a specific date in the calendar
      * 
      * @param nickname
      * @param id the calendar id
      * @param aDate a specific date
      */
     public static void showDate(String nickname, Long calendarId, Date aDate) {
 	User user = User.find("byNickname", Security.connected()).first();
 	Calendar calendar = Calendar.findById(calendarId);
 
 	Date start = new DateTime(aDate).withTime(0, 0, 0, 0).toDate();
 	Date end = new DateTime(aDate).withTime(23, 59, 59, 999).toDate();
 
 	Query query = JPA
 		.em()
 		.createQuery(
 			"from Event where lowerBound <= :start and upperBound >= :end and calendar_id=:cid order by start")
 		.setParameter("start", start, TemporalType.DATE)
 		.setParameter("end", end, TemporalType.DATE)
 		.setParameter("cid", calendar.id);
 
 	List<Event> events = query.getResultList();
 
	List<Event> list = new ArrayList(events);
 
 	// check if logged in user is not equals to calendar owner
 	if (!user.equals(calendar.owner)) {
 	    // if so, then copy all public events in a list
 	    for (Event ev : events) {
 		if (ev.isPublic) {
 		    list.add(ev);
 		}
 	    }
 	} else { // otherwise logged in user is equals to calendar owner
 	    list = events;
 	}
 
 	renderArgs.put("events", list);
 
 	Locale aLocale = new Locale("en", "CH");
 	render("Calendars/showCalendar.html", user, calendar, aDate, aLocale);
     }
 }
