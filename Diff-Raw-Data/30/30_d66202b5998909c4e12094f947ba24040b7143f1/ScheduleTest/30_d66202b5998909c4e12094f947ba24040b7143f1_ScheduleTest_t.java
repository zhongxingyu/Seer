 package tests;
 
 /**
  * @author Team TA's
  */
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import fi.helsinki.cs.scheduler3000.model.Event;
 import fi.helsinki.cs.scheduler3000.model.Schedule;
 import fi.helsinki.cs.scheduler3000.model.Weekday.Day;
 
 public class ScheduleTest {
 
 	Schedule schedule;
 	ArrayList<Day> week;
 	ArrayList<Event> events;
 	HashMap<Day, ArrayList<Event>> map;
 	
 	@Before
 	public void setUp() throws Exception {
 		week = new ArrayList<Day>();
 		week.add(Day.MON);
 		week.add(Day.TUE);
 		week.add(Day.WED);
 		week.add(Day.THU);
 		week.add(Day.FRI);
 		
 		events = new ArrayList<Event>();
		events.add(new Event( Day.MON, "first", "firstLocation", 8, 10, 1));
		events.add(new Event( Day.TUE, "second", "secondLocation", 18, 20, 1));
		events.add(new Event( Day.WED, "third", "thirdLocation", 12, 14, 1));
		events.add(new Event( Day.THU, "fourth", "fourthLocation", 16, 18, 1));
 		
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		
 		map = null;
 		events = null;
 		week = null;		
 		schedule = null;
 
 	}
 	
 
 	@Test
 	public void testAllConstructors(){
 		schedule = new Schedule(week);
 		
 		assertNotNull(schedule);
 		
 		assertEquals(week.size(), schedule.getDays().size());
 		
 		for (Day d : week){
 			assertNotNull(schedule.getEventsOn(d));
 		}
 		schedule = null;
 		
 		assertNull(schedule);
 		
 		String testperiod = "testperiod";
 		schedule = new Schedule(week, testperiod);
 		
 		assertNotNull(schedule);
 		
 		for (Day d : week){
 			assertNotNull(schedule.getEventsOn(d));
 		}
 		
 		assertNotNull(schedule.getPeriod());
 		assertEquals(testperiod, schedule.getPeriod());
 		schedule = null;
 	}
 	
 	@Test
 	public void testAllSetSchedules(){
 		schedule = new Schedule(week, "first period");
 
 		schedule.addEvent( events.get(0) );
 		
 		Schedule anotherSchedule = new Schedule(week);
 		anotherSchedule.setSchedule(schedule);
 		
 		assertEquals( 1 , schedule.getEventsOn( events.get(0).getDay()  ).size() );
 		
 	}
 	
 	@Test
 	public void testSetPeriod(){
 		String period = "first period";
 		schedule = new Schedule(week, period);
 	
 		assertEquals(period, schedule.getPeriod());
 		
 		period = "totally another period";
 		schedule.setPeriod(period);
 		assertEquals(period, schedule.getPeriod());
 	}
 	
 	@Test
 	public void testAddEvent(){
 		schedule = new Schedule(week, "period");
		Event e = new Event( Day.FRI, "testAddEvent", "testAddEventLocation", 16, 20, 1);
 		schedule.addEvent(e);
 		
 		assertEquals(1, schedule.getEventsOn(Day.FRI).size());
 		assertEquals(e, schedule.getEventsOn(Day.FRI).toArray()[0] );
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testAddEventWhenWrongDay(){
 		schedule = new Schedule(week, "period");
 		
		Event invalid = new Event( Day.SUN, "testAddEvent", "testAddEventLocation", 16, 20, 1 );
 		schedule.addEvent(invalid);
 		
 	}
 	
 }
