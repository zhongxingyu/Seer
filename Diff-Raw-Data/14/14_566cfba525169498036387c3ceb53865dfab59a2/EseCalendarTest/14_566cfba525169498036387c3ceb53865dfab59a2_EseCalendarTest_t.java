 package ch.unibe.ese.calendar.impl;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.UnitTest;
 import ch.unibe.ese.calendar.CalendarEvent;
 import ch.unibe.ese.calendar.EventSeries;
 import ch.unibe.ese.calendar.EventSeries.Repetition;
 import ch.unibe.ese.calendar.exceptions.EventNotFoundException;
 import ch.unibe.ese.calendar.util.EseDateFormat;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.Visibility;
 
 public class EseCalendarTest extends UnitTest {
 	
 	private EseCalendarImpl calendar;
 	private User user;
 	private Date start, end;
 	private String eventName;
 	
 	
 	@Before
 	public void setup() throws ParseException {
 		user = new User("dummy");
 		calendar = new EseCalendarImpl("TestCalendar", user);
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
 		assertFalse(calendar.iterate(user.ADMIN, start).hasNext());
 		CalendarEvent event = calendar.addEvent(user.ADMIN, start, end, eventName, Visibility.PUBLIC,"random Kommentar1");
 		assertTrue(calendar.iterate(user.ADMIN, start).hasNext());
 		assertTrue(calendar.getEventById(user.ADMIN, event.getId()) == event);
 	}
 	
 	@Test
 	public void removeEvent() {
 		CalendarEvent event = calendar.addEvent(user.ADMIN, start, end, eventName, Visibility.PUBLIC,"random Kommentar1");
 		assertTrue(calendar.removeEvent(user.ADMIN, event.getId()) == event);
 	}
 	
 	@Test (expected=EventNotFoundException.class)
 	public void removeEventThatsNotExistant() {
 		CalendarEvent event = calendar.addEvent(user.ADMIN, start, end, eventName, Visibility.PUBLIC,"random Kommentar1");
 		assertTrue(calendar.removeEvent(user.ADMIN, event.getId()) == event);
 		calendar.removeEvent(user.ADMIN, event.getId());
 	}
 	
 	@Test
 	public void addEventSeries() {
 		Iterator<CalendarEvent> iter = calendar.iterate(user.ADMIN, start);
 		assertFalse(iter.hasNext());
 		EventSeries eventSeries = calendar.addEventSeries(user.ADMIN, start, end, eventName, Visibility.PUBLIC, Repetition.WEEKLY,"random Kommentar1");
 		iter = calendar.iterate(user.ADMIN, start);
 		int k = 0;
 		//this would go on forever, because a series doesn't end
 		while (iter.hasNext() && k < 100) {
 			assertEquals(eventSeries, iter.next().getSeries());
 			k++;
 		}
 	}
 	
 	@Test
 	public void getEventsAtGivenDate() throws ParseException {
 		CalendarEvent event = calendar.addEvent(user.ADMIN, start, end, eventName, Visibility.PUBLIC,"random Kommentar1");
		Date dayOnWhichEventIsHappening = EseDateFormat.getInstance().parse("12.11.11 20:00");
		assertTrue(calendar.getEventsAt(user.ADMIN, dayOnWhichEventIsHappening).contains(event));
 	}
 
 }
