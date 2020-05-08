 package calendar_importers_test;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.thoughtworks.xstream.XStream;
 
 import calendar.Availability;
 import calendar.CalendarGroup;
 import calendar.CalendarSlots;
 import calendar.When2MeetEvent;
 import calendar.When2MeetOwner;
 import calendar_exporters.When2MeetExporter;
 import calendar_exporters.When2MeetExporter.NameAlreadyExistsException;
 import calendar_importers.When2MeetImporter;
 
 import ftf.TimeFinderSlots;
 
 public class When2MeetTest {
 	
 	/*
 	public void postToWeb() throws IOException {
 		When2MeetImporter wtmi = new When2MeetImporter("http://www.when2meet.com/?353066-BlwWl");
 
 		When2MeetEvent w2me = wtmi.importCalendarGroup();
 		w2me.setName("Test Event");
 		w2me.setURL("no url");
 		When2MeetExporter exporter = new When2MeetExporter(w2me);
 		exporter.postNewEvent();
 	}
 	*/
 	
 	public static void main(String[] args) throws IOException, NameAlreadyExistsException {
 		//String str = JOptionPane.showInputDialog(null, "Enter When2Meet URL: ", 
 		//		"http://www.when2meet.com/?353066-BlwWl", 1);
 		//String url = "http://www.when2meet.com/?353066-BlwWl";
 		//"http://www.when2meet.com/?353066-BlwWl");
 		
 		
 		When2MeetImporter wtmi = new When2MeetImporter(); 
 		When2MeetEvent bDemo = wtmi.importCalendarGroup("http://www.when2meet.com/?426631-GoPHt");
 		CalendarSlots jeanetteCal = bDemo.getCalByName("Jeanette");
 		
		/*
 		for(int i = 0; i < jeanetteCal.getTotalSlots(); i++) 
 			jeanetteCal.setAvail(i, Availability.free);
 		
 		for(int i = 8; i < 14; i++)
 			jeanetteCal.setAvail(i, Availability.busy);
 		
 		When2MeetExporter exporter = new When2MeetExporter();
		exporter.postAllAvailability(bDemo);
		*/
 
 		
 		/*
 		XStream xstream = new XStream();
 		xstream.alias("calendarslots", CalendarSlots.class);
 		xstream.alias("when2meetevent", When2MeetEvent.class);
 		xstream.alias("when2meetowner", When2MeetOwner.class);
 		xstream.alias("avail", Availability.class);
 		
 		String xml = xstream.toXML(specs);
 		System.out.println(xml);
 		
 		When2MeetEvent specsRecreated = (When2MeetEvent) xstream.fromXML(xml);
 		*/
 		/*
 		ArrayList<CalendarSlots> calsAfter = specsRecreated.getCalendars();
 		for(int i = 0; i < calsAfter.size(); i++) {
 			assert calsBefore.get(i).equals(calsAfter.get(i));
 		}*/
 		// assert calsBefore.equals(specsRecreated.getCalendars());
 		
 		
 		//wtmi = new When2MeetImporter("http://www.when2meet.com/?408906-BySEr");
 		//w2me = wtmi.importCalendarGroup();
 		
 		
 		/*
 		CalendarSlots cal1 = w2me.getCalByName("Password");
 		cal1.getOwner().setName("Test");
 		System.out.println("Name: " + cal1.getOwner().getName());
 		*/
 		
 	
 		/*
 		for(int i = 0; i < cal1.getTotalSlots(); i++) {
 			if(i%3 == 0)
 				cal1.setAvail(i, Availability.free);
 			else
 				cal1.setAvail(i, Availability.busy);
 		}
 		
 		*/
 		//exporter.createNewUser("world");
 		//exporter.postAllAvailability();
 		
 		// CalendarSlots cal2 = new CalendarSlots(w2me.getStartTime(), w2me.getEndTime(), 15, Availability.busy);
 		
 		//exporter.postAvailability(slotIDs,  Availability.free);
 		//exporter.createNewUser();
 		System.out.println("After updating availability");
 /*
 		TimeFinderSlots timeFind = new TimeFinderSlots();
 		CalendarSlots times = timeFind.findBestTimes(w2me, 15, 60, 20, 4);
 		
 		times.print();
 		*/
 		
 	}
 }
