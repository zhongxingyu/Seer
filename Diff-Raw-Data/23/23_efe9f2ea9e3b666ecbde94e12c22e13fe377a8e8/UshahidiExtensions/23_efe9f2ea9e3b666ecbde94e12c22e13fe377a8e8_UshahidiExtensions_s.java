 package edu.grinnell.csc207.fletcher1.hw5;
 
 import java.io.PrintWriter;
 import java.util.GregorianCalendar;
 import edu.grinnell.glimmer.ushahidi.*;
 
 public class UshahidiExtensions {
 
 	public static void printIncident(PrintWriter pen, UshahidiIncident event){
 		pen.println("Incident #: "+ event.getTitle()
 				+ "\n  " + event.getDescription()
 				+ "\n  Location: "+ event.getLocation()
 				+ "\n  Status: (" + event.getMode() + event.getActive() 
 				+ event.getVerified() + ")"
 				+ "\n");
 	} // printIncident(PrintWriter, UshahidiIncident)
 	
 	public static UshahidiIncidentList UshahidiIncidentList() {
		
 		return null;
 	} // UshahidiIncidentList()
 	
 	public static void extremeIncidents(UshahidiClient client) throws Exception {
 		int currentId, largest, smallest;
 		smallest = 0;
 		largest = 0;
		UshahidiIncident small;
		UshahidiIncident big;
 		UshahidiIncident current;
 		PrintWriter pen = new PrintWriter(System.out, true);
 		
 		for (client.hasMoreIncidents();;) {
 			current = client.nextIncident();
 			currentId = current.getId();
 			if (currentId > largest) {
 				largest = currentId;
 				big = current;
 			} // if
 			if (currentId < smallest) {
 				smallest = currentId;
 				small = current;
 			} // if
 		} // for
 		
 		pen.println("Lowest ID\n");
 		printIncident(pen, small);
 		pen.println("Highest ID\n");
 		printIncident(pen, big);
 		
 	} // extremeIncidents(UshahidiClient)
 
 	public void identifyIncidents(UshahidiClient client, GregorianCalendar start, 
 			GregorianCalendar end) throws Exception {
 		
 		boolean afterStart = false;
 		boolean afterEnd = false;
 		UshahidiIncident current;
 		GregorianCalendar currentDate;
 		PrintWriter pen = new PrintWriter(System.out, true);
 		
 		for (client.hasMoreIncidents();;) {
 			current = client.nextIncident();
 			currentDate = (GregorianCalendar) current.getDate();
 			
 			if (currentDate == start) {
 				afterStart = true;
 			}
 			
 			if (currentDate == end) {
 				afterEnd = true;
 			}
 			
 			if (afterStart && afterEnd == false) {
 				printIncident(pen, current);
 			}
 		}
 	} // identifyIncidents(UshahidiClient, GregorianCalendar, GregorianCalendar)
 	
 	public static UshahidiIncidentList selectIncidents(UshahidiClient client, 
 			GregorianCalendar start, GregorianCalendar end) throws Exception {
 		
 		boolean afterStart = false;
 		boolean afterEnd = false;
 		UshahidiIncident current;
 		GregorianCalendar currentDate;
 		UshahidiIncidentList found = UshahidiIncidentList();
 		
 		for (client.hasMoreIncidents();;) {
 			current = client.nextIncident();
 			currentDate = (GregorianCalendar) current.getDate();
 			
 			if (currentDate == start) {
 				afterStart = true;
 			}
 			
 			if (currentDate == end) {
 				afterEnd = true;
 			}
 			
 			if (afterStart && afterEnd == false) {
 				found.addIncident(current);
 			}
 		}
 		
 		return found;
 		
 	} // identifyIncidents(GregorianCalendar, GregorianCalender)
 	
 } // UshahidiExtensions
