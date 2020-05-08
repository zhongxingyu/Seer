 package tests;
 
 /**
  * @author Team TA's
  */
 
 import junit.framework.TestCase;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import fi.helsinki.cs.scheduler3000.model.Event;
 import fi.helsinki.cs.scheduler3000.model.Weekday.Day;
 
 public class EventTest {
 	
 	Event event;
 	
 	@Before
 	public void setUp() {
		event = new Event( Day.MON, "title", "testlaboratory", 8, 12);
 	}
 	
 	@After
 	public void tearDown() {
 		event = null;
 	}
 
 	@Test
 	public void testGetters() {
 		assertEquals(Day.MON, event.getDay() );
 		assertEquals( "title", event.getTitle() );
 		assertEquals( "testlaboratory", event.getLocation() );
 		assertEquals( 8, event.getStartTime() );
 		assertEquals( 12, event.getEndTime() );
 	}
 	
 	@Test
 	public void testSetters() {
 		event.setDay( Day.TUE );
 		assertEquals( Day.TUE, event.getDay() );
 		event.setTitle("title1");
 		assertEquals( "title1", event.getTitle() );
 		event.setLocation("test");
 		assertEquals( "test", event.getLocation() );
 		event.setStartTime(10);
 		assertEquals( 10, event.getStartTime() );
 		event.setEndTime(14);
 		assertEquals( 14, event.getEndTime() );
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testInvalidStart(){
 		event.setStartTime(9);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void testInvalidEnd(){
 		event.setStartTime(10);
 		event.setEndTime(8);
 	}
 	
 	public void testAllValidStartTimes() {
 		for(int j : Event.VALID_TIMES) {
 			assertTrue( Event.isValidStartTime(j) );
 		}
 		// test invalid times
 		assertFalse( Event.isValidStartTime(6) );
 		assertFalse( Event.isValidStartTime(7) );
 		assertFalse( Event.isValidStartTime(9) );
 		assertFalse( Event.isValidStartTime(21) );
 		assertFalse( Event.isValidStartTime(22) );
 	}
 	 
 }
