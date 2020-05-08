 package ftf_test;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import calendar.*;
 import ftf.TimeFinder;
 
 
 
 public class TimeFinderTest {
 
 	
 	public static void main(String[] args){
 		TimeFinderTest tft = new TimeFinderTest();
 		TimeFinder timeFind = new TimeFinder();
 		
	/*	{0,0,1,1},
 		{0,1,1,1},
 		{1,0,0,1},
 		{1,1,1,1},
 		{0,1,1,1} */
 		
 		DateTime start = new DateTime();
 		TestCalGroup calGroup = tft.new TestCalGroup(); 
 		ArrayList<Response> resp = new ArrayList<Response>();
 		
 		resp.add(new Response(start, start.plusMinutes(15)));
 		resp.add(new Response(start.plusMinutes(15), start.plusMinutes(30)));
 		resp.add(new Response(start.plusMinutes(60), start.plusMinutes(65)));
 		assert(calGroup != null) : "calGroup";
 		assert(start != null) : "start";
 		assert(start.plusMinutes(65) != null) : "end";
 		assert(resp != null) : "resp";
 		assert(tft.new TestOwner("A") != null) : "owner";
 		assert(tft.new TestCalendar(start, start.plusMinutes(65), tft.new TestOwner("A"), resp) != null) : "cal";
		//calGroup.addCalendar(tft.new TestCalendar(start, start.plusMinutes(65), tft.new TestOwner("A"), resp));
 		
 		ArrayList<Response> resp2 = new ArrayList<Response>();
 		resp2.add(new Response(start, start.plusMinutes(15)));
 		resp2.add(new Response(start.plusMinutes(30), start.plusMinutes(45)));
 		calGroup.addCalendar(tft.new TestCalendar(start, start.plusMinutes(65), tft.new TestOwner("B"), resp2));
 		
 		ArrayList<Response> resp3 = new ArrayList<Response>();
 		resp3.add(new Response(start.plusMinutes(30), start.plusMinutes(45)));
 		calGroup.addCalendar(tft.new TestCalendar(start, start.plusMinutes(65), tft.new TestOwner("C"), resp3));
 		
 		ArrayList<Response> resp4 = new ArrayList<Response>();
 		calGroup.addCalendar(tft.new TestCalendar(start, start.plusMinutes(65), tft.new TestOwner("D"), resp4));
 		
 		Response[] toPrint = timeFind.findBestTimes(calGroup, 15, 30, 5, 1);
 		for(int i=0; i<toPrint.length; i++){
 			System.out.println(toPrint[i].getStartTime().dayOfWeek().getAsText() + ", " + toPrint[i].getStartTime().hourOfDay().getAsText() + ":" + toPrint[i].getStartTime().minuteOfHour().getAsText() + " -- " + toPrint[i].getEndTime().dayOfWeek().getAsText() + ", " + toPrint[i].getEndTime().hourOfDay().getAsText() + ":" + toPrint[i].getEndTime().minuteOfHour().getAsText() );
 		}
 	}
 	
 	
 	
 	
 	
 	private class TestCalendar implements Calendar{
 
 		private DateTime _start, _end;
 		private Collection<Response> _resp;
 		private Owner _own;
 		
 		public TestCalendar(DateTime start, DateTime end, Owner o, Collection<Response> r){
 			_start = start;
 			_end = end;
 			_own = o;
 			_resp = r;
 		}
 		@Override
 		public DateTime getStartTime() {
 			return _start;
 		}
 
 		@Override
 		public DateTime getEndTime() {
 			return _end;
 		}
 
 		@Override
 		public Owner getOwner() {
 			// TODO Auto-generated method stub
 			return _own;
 		}
 
 		@Override
 		public Collection<Response> getResponses() {
 			return _resp;
 		}
 		
 	}
 	
 	private class TestCalGroup implements CalendarGroup {
 
 		private Collection<TestCalendar> _cals;
 		
 		public TestCalGroup(){
 			_cals = new ArrayList<TestCalendar>();
 		}
 		
 		@Override
 		public Collection<TestCalendar> getCalendars() {
 			// TODO Auto-generated method stub
 			return _cals;
 		}
 
 		@Override
 		public void addCalendar(Calendar c) {
 			assert((TestCalendar) c != null) : "casting";
 			_cals.add((TestCalendar) c);
 			
 		}
 	}
 	
 	private class TestOwner implements Owner {
 
 		private String _name;
 		
 		public TestOwner(String name){
 			_name = name;
 		}
 		
 		@Override
 		public String getName() {
 			return _name;
 		}
 		
 	}
 }
