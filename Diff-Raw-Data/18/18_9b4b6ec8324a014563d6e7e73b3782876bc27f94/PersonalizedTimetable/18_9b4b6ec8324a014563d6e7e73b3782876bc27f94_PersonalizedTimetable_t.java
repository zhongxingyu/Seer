 package models.timetable;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

 import models.User;
 import models.calendar.CalendarFeed;
 import models.calendar.GoogleCalendarConnector;
 import dto.CourseDTO;
 import exception.ExternalSynchronizationFailedException;
 
 /**
  * Represents a timetable for a single user. The timetable is built based on the
  * user preferences.
  * 
  */
 public class PersonalizedTimetable {
 	private TimeSlotScheduler timeSlotScheduler = null;
 	private User user;
 
 	public PersonalizedTimetable(User user, List<CourseDTO> courses) {
 		this.user = user;
 		timeSlotScheduler = new TimeSlotScheduler(courses, user.preferences);
 	}
 
 	/**
 	 * Imports data from external calendar. As calendar synchronisation link a
 	 * property of the user object is used.
 	 * 
 	 * @return List of {@link TimetableEntry} extracted from the external
 	 *         calendar
 	 */
 	private List<TimetableEntry> importExternalCalendarData() {
 		List<TimetableEntry> receivedEntries = new ArrayList<TimetableEntry>();
 		
 		if (user.privateCalendarURL != null && !user.privateCalendarURL.isEmpty()) {
 			GoogleCalendarConnector calConnector = null;
 			try {
 				calConnector = new GoogleCalendarConnector(new URL(user.privateCalendarURL));
 				calConnector.receiveCalendarFeed();
 				CalendarFeed feed = calConnector.getCalendarFeed();
 		
 				if (feed != null) {
 					receivedEntries = feed.getTimetableEntryList();
 				}
 			} catch (Exception e) {
 				throw new ExternalSynchronizationFailedException(e, e.getMessage());
 			}
 		}
 
 		return receivedEntries;
 	}
 
 	/**
 	 * Applies data from external calendar to the current list of entries.
 	 * Builds the merged list of entries and returns back.
 	 * 
 	 * @return Whole list of scheduled entries
 	 */
 	public ScheduledTimetableEntryList scheduleTimeSlots() {
 		List<TimetableEntry> receivedEntries = importExternalCalendarData();
 		ScheduledTimetableEntryList allEntries = null;
 		if (timeSlotScheduler != null) {
 			allEntries = timeSlotScheduler.scheduleTimeSlots(receivedEntries);
 		}
 		return allEntries;
 	}
 }
