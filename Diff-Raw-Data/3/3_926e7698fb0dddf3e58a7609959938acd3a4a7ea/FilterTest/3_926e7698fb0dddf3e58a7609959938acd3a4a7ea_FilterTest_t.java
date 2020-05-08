 package test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import process.Event;
 import process.EventCalendar;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.junit.*;
 
 /**
  * 
  * @author Gang Song
  * 
  *  Create a JUnit test class for EventCalendar(process part)
  *
  */
 
 public class FilterTest {
 
 	private EventCalendar myCal;
     private static Event myEvent1;
     private static Event myEvent2;
     private static Event myEvent3;
     
 	
 	
     @Before
 	 public void setUp ()
     {
         // initialize stuff here
         myCal = new EventCalendar();
         
         myEvent1=createEvent("20120110T101000", "20120110T111000", "Walking with Lemurs Tour", "Enter the world of the lemur as your guide escorts you where there are no barriers between you and the animals. Be sure to bring your camera as this experience offers views of the animals unlike any other. Tours start at 10:30 am. Tour length: 60 Minutes. Fees-$95.00 per participant. Age Requirement: 10 years through Adult. Maximum group size - Eight Participants");   
         myEvent2=createEvent("20110715T090000", "20111022T170000","'O Say Can You See' Installation by Laura Poitras", "'O Say Can You See' is an installation by filmmaker Laura Poitras, on view in the Kreps Gallery at the Center for Documentary Studies at Duke University until October 22, 2011. The installation features film footage shot by Poitras in the hours after 9/11 from Ground Zero in 2001, combined with audio from the Yankees' Game 4 World Series victory weeks later, and interviews with recently released detainees from Guantanamo Bay. An artist's talk and reception will take place September 20, 2011, 6-9 p.m., talk at 7 p.m., at the Center for Documentary Studies.");        
         myEvent3=createEvent("20110817T110000", "20120108T110000", "Exhibition: \"Becoming: Photographs from the Wedge Collection\"", "The Nasher Museum presents \"Becoming: Photographs from the Wedge Collection,\" featuring more than 100 original photographic portraits of people of color by more than 60 global artists. In some portraits, the subjects have little or no control over the way they are depicted; in others, the subjects become increasingly involved with the photographer. All of the artists reject a common tendency to view black communities in terms of conflict or stereotype. Includes includes studio portraitists (Malick Sidib, James VanDerZee), social documentarians (Milton Rogovin, Jrgen Schadeberg), conceptual artists (Hank Willis Thomas, Carrie Mae Weems) and young contemporary artists whose work is largely unknown in this country (Zanele Muholi, Viviane Sassen).");
    
         myCal.addEvent(myEvent1);
         myCal.addEvent(myEvent2);
         myCal.addEvent(myEvent3);
     }
 	
 	
 	private Event createEvent(String starttime, String endtime, String title, String desc ){
 		
 	        DateTimeFormatter dtf=DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
 	        DateTime start=dtf.parseDateTime(starttime);
 	        DateTime end=dtf.parseDateTime(endtime);
 	        Event myEvent=new Event(start, end);
 	        myEvent.addFeature("title", title);
 	        myEvent.addFeature("description", desc);
 	        return myEvent;		
 	}
 	
 	@Test
 	public void testNameFilter(){
 		
 		ArrayList<Event> list1=new ArrayList<Event>();
 		list1.add(myEvent1);
 		runNameTest(list1, "Lemurs Tour");
 		ArrayList<Event> list2=new ArrayList<Event>();
 		list2.add(myEvent2);
 		runNameTest(list2, "Laura Poitras");
 		ArrayList<Event> list3=new ArrayList<Event>();
 		list3.add(myEvent3);
 		runNameTest(list3, "Exhibition");
 		
 	}
 	
 	@Test
 	public void testSortByStartTime(){
 		
 		ArrayList<Event> list=new ArrayList<Event>();
 		list.add(myEvent2);
 		list.add(myEvent3);
 		list.add(myEvent1);		
 		runSortByStartTimeTest(list);
 	}
 	
 	@Test
 	public void testSortByEndTime(){
 		
 		ArrayList<Event> list=new ArrayList<Event>();
 		list.add(myEvent2);
 		list.add(myEvent3);
 		list.add(myEvent1);		
 		runSortByEndTimeTest(list);
 	}
 	
 	@Test
 	public void testRemoveFilter(){
 		ArrayList<Event> list=new ArrayList<Event>();
 
 		list.add(myEvent1);
 		list.add(myEvent2);
 		
 		runRemoveTest(list);
 	}
 	
 	@Test
 	public void testTimeFilter(){
 		ArrayList<Event> list=new ArrayList<Event>();
 		
 		list.add(myEvent1);
 		
 		runTimeTest(list);
 	}
 	
 	private void runTimeTest(ArrayList<Event> expected) {
 		DateTime time=new DateTime(2012, 1, 10, 10, 50, 0);
 		List<Event> actual=myCal.eventsAtTime(time).getList();
 		assertTrue(actual.equals(expected));
 	}
 
 
 	private void runRemoveTest(ArrayList<Event> expected) {
 		myCal.removeAllContaining("description", "Nasher");
 		List<Event> actual=myCal.getList();
 		assertTrue(actual.equals(expected));
 	}
 
 
 	private void runSortByStartTimeTest(ArrayList<Event> expected) {
 		myCal.sortByStartTime();
 		List<Event> actual=myCal.getList();
 		assertTrue(actual.equals(expected));		
 	}
 	
 	private void runSortByEndTimeTest(ArrayList<Event> expected) {
 		myCal.sortByEndTime();
 		List<Event> actual=myCal.getList();
 		assertTrue(actual.equals(expected));		
 	}
 
 
 
 	private void runNameTest(ArrayList<Event> expectedEvents, String keyword) {
 		
 		EventCalendar newCal=myCal.filterByName(keyword);		
 		assertTrue(newCal.getList().equals(expectedEvents));
 		
 	}
 	 
 	
 	 
 
 	
 	
 }
