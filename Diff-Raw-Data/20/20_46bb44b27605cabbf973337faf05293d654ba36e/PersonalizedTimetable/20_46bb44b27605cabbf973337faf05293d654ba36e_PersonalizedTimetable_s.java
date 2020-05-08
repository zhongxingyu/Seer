 package models.timetable;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalTime;
 
 import dto.CourseDTO;
 import dto.CourseMaterialDTO;
 
 import models.Course;
 import models.CustomerPreferences;
 import models.DateUtility;
 import models.TimeInterval;
 import models.calendar.CalendarEntry;
 import models.calendar.CalendarFeed;
 import models.calendar.GoogleCalendarConnector;
 
 public class PersonalizedTimetable {
 	private List<TimetableEntry> allEntries = null;
 	private TimeSlotScheduler timeSlotScheduler = null;
 	
 	public PersonalizedTimetable(){
		CourseDTO agtCourse = new CourseDTO(null, "Algorithmic Game Theory","very tough lecture..",0.9,1.0,new DateTime().plusWeeks(2),new CourseMaterialDTO(20, 40));
		CourseDTO sebaCourse = new CourseDTO(null, "SEBA","Web App",1.0,1.0,new DateTime().plusWeeks(2),new CourseMaterialDTO(30, 40));
 		
 		List<CourseDTO> courses = new ArrayList<CourseDTO>();
 		courses.add(agtCourse);
 		courses.add(sebaCourse);
 		
 		CustomerPreferences dummyCustomerPrefs = new CustomerPreferences(DateUtility.LearningDayTime.MORNING,1.0,0.5,0.25);
 		List<TimeInterval> dummyCustomerRestTimeIntervals = new ArrayList<TimeInterval>();
 		dummyCustomerRestTimeIntervals.add(new TimeInterval(new LocalTime(0, 0), new LocalTime(7, 0)));
 		dummyCustomerRestTimeIntervals.add(new TimeInterval(new LocalTime(22, 0), new LocalTime(23, 59)));
 		dummyCustomerPrefs.setTimeIntervalsOfRest(dummyCustomerRestTimeIntervals);
 		List<DateUtility.Day> dummyRestDays = new ArrayList<DateUtility.Day>();
 		dummyRestDays.add(DateUtility.Day.Saturday);
 		dummyRestDays.add(DateUtility.Day.Sunday);
 		dummyCustomerPrefs.setDaysOfRest(dummyRestDays);
 		
 		timeSlotScheduler = new TimeSlotScheduler(courses, dummyCustomerPrefs);
 	}
 	
 	private List<TimetableEntry> importExternalCalendarData(){
 		List<TimetableEntry> receivedEntries = null;
 		GoogleCalendarConnector calConnector = null;
     	try {
 			calConnector = new GoogleCalendarConnector(
 					new URL("https://www.google.com/calendar/ical/schmi.kalle%40gmail.com/private-82d40d6ada06fa5825d8910c4949e64f/basic.ics"));
 		} catch (MalformedURLException e) {
 		}
     	
   	calConnector.receiveCalendarFeed();
   	CalendarFeed feed = calConnector.getCalendarFeed(); 
 
   	if(feed!=null){
   		receivedEntries = feed.getTimetableEntryList();
   	}
   	
   	return receivedEntries;
     	
 	}
 	
 	public List<TimetableEntry> scheduleTimeSlots(){
 		List<TimetableEntry> receivedEntries = importExternalCalendarData();
 		if(timeSlotScheduler!=null){
 			allEntries = timeSlotScheduler.scheduleTimeSlots(receivedEntries);
 		}
 		return allEntries;
 	}
 }
