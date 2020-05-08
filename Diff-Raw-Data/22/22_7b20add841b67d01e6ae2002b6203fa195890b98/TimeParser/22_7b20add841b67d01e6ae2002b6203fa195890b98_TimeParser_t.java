 package xmlParse;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.joda.time.DateTime;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import Process.Event;
 
 /*
  * @author Gang Song
  */
 
 public class TimeParser {
 	
 	HashMap<String, Integer> monthMap;
 	int myMonth;
 	int myDay;
 	int myYear;
 	int myHour;
 	int myMinute;
 
 	/*
 	 * create Joda Time from a specific period (a subnode of an event like
 	 * 'start' or 'end') in an event
 	 */
 	public  DateTime createTime(Node nEvent, String period) {
 		NodeList children = nEvent.getChildNodes();
 		Node myTime = null;
 		for (int j = 0; j < children.getLength(); j++) {
 			Node child = children.item(j);
 			if (child.getNodeName().equals(period)) {
 				myTime = child;
 				break;
 			}
 		}
 		DateTime dt = null;
 		if (myTime != null)
 			dt = new DateTime(getIntOfTime(myTime, "year"), 
 								getIntOfTime(myTime, "month"), 
 								getIntOfTime(myTime, "day"),
 								getIntOfTime(myTime, "hour24"), 
 								getIntOfTime(myTime, "minute"), 0, 0);
 		return dt;
 	}
 
 	/*
 	 * return an integer of specific time scale
 	 */
 	private int getIntOfTime(Node myTime, String scale) {
 		return Integer.parseInt(xmlParse.extractNodeText(myTime, scale));
 	}
 	
 	public DateTime getGoogleCalTime(String content,String period){
 		
 		String[] sections=content.split(" ");
 		if(sections[0].equals("Recurring")){
 			DateTime dt=new DateTime(sections[4]+"T"+sections[5]);
 			if(period.equals("start"))
 				return dt;
 			else
 				return dt.plusSeconds(Integer.parseInt(sections[9]));							
 		}
 		else {
 			createMapOfMonth();
 			myMonth = monthMap.get(sections[2]);
 			StringBuilder tempday = new StringBuilder(sections[3]);
 			myDay = Integer.parseInt(tempday.delete(tempday.length() - 1,
 					tempday.length()).toString());
 			myYear = Integer.parseInt(sections[4]);
 
 			if (period.equals("start")) {
 				
 				createHourAndMinute(sections[5]);
 				
 			} else {
 				
				createHourAndMinute(sections[7].substring(0, sections[7].length()-26));				
 			}
 			
 			return new DateTime(myYear,myMonth,myDay,myHour,myMinute);
 		}
 		
 	}
 
 	private void createHourAndMinute(String section) {
 		String ampm = section.substring(section.length() - 2);
 		String hourandmin = section.substring(0,
 				section.length() - 2);
 		if(hourandmin.contains(":")){
 			String[] handm = hourandmin.split(":");
 		    myHour = Integer.parseInt(handm[0]);
 		    myMinute = Integer.parseInt(handm[1]);
 		
 		}
 		else{
 			myHour = Integer.parseInt(hourandmin);
 			myMinute=0;
 		}
 		
 		if (ampm.equals("pm"))
 			myHour = myHour + 12;
 		
 	}
 	private  void createMapOfMonth(){
 		monthMap=new HashMap<String, Integer>();
 		monthMap.put("Jan", 1);
 		monthMap.put("Feb", 2);
 		monthMap.put("Mar", 3);
 		monthMap.put("Apr", 4);
 		monthMap.put("May", 5);
 		monthMap.put("Jun", 6);
 		monthMap.put("Jul", 7);
 		monthMap.put("Aug", 8);
 		monthMap.put("Sep", 9);
 		monthMap.put("Oct", 10);
 		monthMap.put("Nov", 11);
 		monthMap.put("Dec", 12);
 	}
 	
       public static void main (String[] args) throws Exception{
 		
 		ArrayList<Event> test =new ArrayList<Event>();
 		test.add(new Event("meet with TA", "perkins", "talk about TiVOO", new DateTime(2011,8,30,11,45,2), new DateTime("2012-05-01T18:00:00"), ""));
 		test.add(new Event("talk with TA", "bbb", "cd", new DateTime("2011-11-16T13:30:00"), new DateTime("2012-05-01T18:00:00"), ""));
 		DateTime start=new TimeParser().getGoogleCalTime("When: Fri Sep 23, 2011 4:30pm to 5:30pm&nbsp; EDT<br> <br>Event Status: confirmed", "start");
 		DateTime end=new TimeParser().getGoogleCalTime("When: Fri Sep 23, 2011 4:30pm to 5:30pm&nbsp; EDT<br> <br>Event Status: confirmed", "end");
 		test.add(new Event("talk with TA", "bbb", "cd", start, end, ""));
 		//test=new xmlProcess().process(test);
 		
 		for(Event m: test){
 			System.out.println(m.toString());
 			System.out.println(m.getStartTime().toString("dd-MM-yyyy HH:mm:ss") + " to " + m.getEndTime().toString("dd-MM-yyyy HH:mm:ss"));
 		}
 		googleCalParse s=new googleCalParse("http://www.cs.duke.edu/courses/cps108/current/assign/02_tivoo/data/googlecal.xml");
 		NodeList myEvents = s.myDocument.getElementsByTagName("entry");
 		Node nEvent = myEvents.item(1);
 		System.out.println(s.extractNodeText(nEvent, "summary"));
 		
 	}
     
 
 }
