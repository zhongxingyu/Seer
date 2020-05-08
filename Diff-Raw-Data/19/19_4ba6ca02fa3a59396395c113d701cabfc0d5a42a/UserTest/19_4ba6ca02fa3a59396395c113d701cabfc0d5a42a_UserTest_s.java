 
 
 
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 
 import models.*;
 
 public class UserTest extends UnitTest {
 	
 	private User firstUser;
 	private User secondUser;
 	private Calendar firstCalendar;
 	private ArrayList<Event> eventsVisibleToSecondUser;
 	
 	@Before
 	public void init() {
 		this.firstUser = new User("firstUser");
 		this.secondUser = new User("secondUser");
 		this.firstCalendar = firstUser.getCalendar();
 		this.eventsVisibleToSecondUser = new ArrayList<Event>();
 	}
 	
 	@Test
 	public void shouldHaveAName() {
		assertEquals("firstUser", firstUser.toString());
 	}
 	
 	@Test
 	public void shouldInitCalendar() {
 		assertFalse(firstUser.getCalendar().equals(null));
 	}
 	
 	@Test
 	public void shouldParseDateCorrectly() {
 		String strDate = "12.04.95 14:40";
 		Date date = Parser.parseStringToDate(strDate);
 		assertEquals("Wed Apr 12 14:40:00 CEST 1995", date.toString());
 	}
 	
 	@Test
 	public void shouldNotGetPrivateEvent() {
 		Date startDate = Parser.parseStringToDate("23.09.11 18:00");
 		Date endDate = Parser.parseStringToDate("30.09.11 17:00");
 		Event event = new Event("event", startDate, endDate, false);
 		firstCalendar.addEvent(event);
 		Date testDay = Parser.parseStringToDate("24.09.11 13:30");
 		eventsVisibleToSecondUser = secondUser.getVisibleEventsOnSpecificDayFrom(firstUser, testDay);
 		assertFalse(eventsVisibleToSecondUser.contains(event));
 	}
 	
 	@Test
 	public void shouldGetPublicEvent() {
 		Date startDate = Parser.parseStringToDate("24.09.11 17:00");
 		Date endDate = Parser.parseStringToDate("24.09.11 18:00");
 		Event event = new Event("event", startDate, endDate, true);
 		firstCalendar.addEvent(event);
 		Date testDay = Parser.parseStringToDate("24.09.11 13:30");
 		eventsVisibleToSecondUser = secondUser.getVisibleEventsOnSpecificDayFrom(firstUser, testDay);
 		assertTrue(eventsVisibleToSecondUser.contains(event));
 	}
 	
 	@Test
 	public void shouldGetEventsOverMultipleDays() {
 		Date startDate = Parser.parseStringToDate("22.09.11 17:00");
 		Date endDate = Parser.parseStringToDate("26.09.11 18:00");
 		Event event = new Event("event", startDate, endDate, true);
 		firstCalendar.addEvent(event);
 		Date testDay = Parser.parseStringToDate("24.09.11 13:30");
 		eventsVisibleToSecondUser = secondUser.getVisibleEventsOnSpecificDayFrom(firstUser, testDay);
 		assertTrue(eventsVisibleToSecondUser.contains(event));
 	}
 	
 	@Test
 	public void shouldNotGetEventOnOtherDate() {
 		Date startDate = Parser.parseStringToDate("25.09.11 17:00");
 		Date endDate = Parser.parseStringToDate("25.09.11 18:00");
 		Event event = new Event("event", startDate, endDate, true);
 		firstCalendar.addEvent(event);
 		Date testDay = Parser.parseStringToDate("24.09.11 13:30");
 		eventsVisibleToSecondUser = secondUser.getVisibleEventsOnSpecificDayFrom(firstUser, testDay);
 		assertFalse(eventsVisibleToSecondUser.contains(event));
 		assertTrue(eventsVisibleToSecondUser.isEmpty());
 	}
 	
 	@Test
 	public void shouldGetEventPartiallyMatchingDay() {
 		Date startDate = Parser.parseStringToDate("23.09.11 17:00");
 		Date endDate = Parser.parseStringToDate("24.09.11 18:00");
 		Date startDate2 = Parser.parseStringToDate("24.09.11 17:00");
 		Date endDate2 = Parser.parseStringToDate("25.09.11 18:00");
 		Event event = new Event("event1", startDate, endDate, true);
 		Event event2 = new Event("event2", startDate2, endDate2, true);
 		firstCalendar.addEvent(event);
 		firstCalendar.addEvent(event2);
 		Date testDay = Parser.parseStringToDate("24.09.11 13:30");
 		eventsVisibleToSecondUser = secondUser.getVisibleEventsOnSpecificDayFrom(firstUser, testDay);
 		assertTrue(eventsVisibleToSecondUser.contains(event));
 		assertTrue(eventsVisibleToSecondUser.contains(event2));
 	}
 
 }
