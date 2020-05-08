 package ch.unibe.ese.calendar;
 
 import java.text.ParseException;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.unibe.ese.calendar.EventSeries.Repetition;
 
 import play.test.UnitTest;
 
 public class EseCalendarTest extends UnitTest {
 	
 	private EseCalendar calendar;
 	private User user;
 	private Date start, end;
 	private String eventName;
 	
 	
 	@Before
 	public void setup() throws ParseException {
 		user = new User("dummy");
 		calendar = new EseCalendar("TestCalendar", user);
 		start = EseDateFormat.getInstance().parse("12.11.11 20:00");
 		end = EseDateFormat.getInstance().parse("12.11.11 22:00");
 		eventName = "test";
 	}
 	
 	@Test
 	public void getName() {
 		assertEquals("TestCalendar", calendar.getName());
 	}
 	
 	@Test
 	public void getOwner() {
 		assertEquals(user, calendar.getOwner());
 	}
 	
 	@Test
 	public void addEvent() {
 		assertTrue(calendar.getStartDateSortedSet().isEmpty());
 		CalendarEvent event = calendar.addEvent(user.ADMIN, start, end, eventName, Visibility.PUBLIC,"random Kommentar1");
 		assertFalse(calendar.getStartDateSortedSet().isEmpty());
 		assertTrue(calendar.getStartDateSortedSet().contains(event));
 	}
 	
 	@Test
 	public void removeEvent() {
 		CalendarEvent event = calendar.addEvent(user.ADMIN, start, end, eventName, Visibility.PUBLIC,"random Kommentar1");
 		assertTrue(calendar.getStartDateSortedSet().contains(event));
		calendar.removeEvent(user.ADMIN, event.getId(), event.getStart(), event.getSeries() != null);
 		assertTrue(calendar.getStartDateSortedSet().isEmpty());
 	}
 	
 	@Test
 	public void addEventSeries() {
 		assertTrue(calendar.getStartDateSortedSetOfSeries().isEmpty());
 		EventSeries eventSeries = calendar.addEventSeries(user.ADMIN, start, end, eventName, Visibility.PUBLIC, Repetition.WEEKLY,"random Kommentar1");
 		assertFalse(calendar.getStartDateSortedSetOfSeries().isEmpty());
 		assertTrue(calendar.getStartDateSortedSetOfSeries().contains(eventSeries));
 	}
 	
 	//HashCode of an event is not unique. We will need to refactor this sooner of later
 	@Test
 	public void getEventByHash() {
 		CalendarEvent event = calendar.addEvent(user.ADMIN, start, end, eventName, Visibility.PUBLIC,"random Kommentar1");
 		assertEquals(event, calendar.getEventByHash(user.ADMIN, event.hashCode(), event.getStart()));
 	}
 	
 	@Test
 	public void getEventsAtGivenDate() throws ParseException {
 		CalendarEvent event = calendar.addEvent(user.ADMIN, start, end, eventName, Visibility.PUBLIC,"random Kommentar1");
 		Date dayOnWichEventIsHappening = EseDateFormat.getInstance().parse("12.11.11 20:00");
 		assertTrue(calendar.getEventsAt(user.ADMIN, dayOnWichEventIsHappening).contains(event));
 	}
 
 }
