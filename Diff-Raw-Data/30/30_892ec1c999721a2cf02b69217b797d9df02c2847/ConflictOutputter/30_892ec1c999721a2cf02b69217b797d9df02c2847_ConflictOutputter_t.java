 package output;
 import model.Event;
 import java.util.*;
 import org.joda.time.*;
import sorting.StartTimeSorter;
 import com.hp.gagawa.java.elements.*;
 
 /**
  * Provides a view of all events in a list and all other
  * events in that list that conflict in time
  * @author herio
  *
  */
 public class ConflictOutputter extends AbstractHtmlOutputter{
 
 	public void writeEvents(List<Event> events) {
 	    DateTime dt;
         if(events.isEmpty())
             dt = new DateTime();
         else{
         	StartTimeSorter sts = new StartTimeSorter();
         	events = sts.sort(events);
             dt = new DateTime(events.get(0).getStartTime());
         }
         
         String filepath = "Conflicts_starting_at_" + dt.toString("MMMdd");
         
         Html html = new Html();
         Body body = new Body();
         html.appendChild(body);
         Table table = new Table();
         table.setBorder("");
         body.appendChild(table);
         
         Tr row0 = new Tr();
         Td colTitle1 = new Td();
         Td colTitle2 = new Td();
         B b1 = new B();
         b1.appendChild(new Text("Event"));
         colTitle1.appendChild(b1);
         B b2 = new B();
         b2.appendChild(new Text("Conflicts with:"));
         colTitle2.appendChild(b2);
         row0.appendChild(colTitle1, colTitle2);
         table.appendChild(row0);
         
         for(Event e: events){
             Tr row1 = new Tr();
             row1.setValign("top");
             Td td1 = new Td();
             Td td2 = new Td();
             row1.appendChild(td1, td2);
             table.appendChild(row1);
             
             P p1 = new P();
             td1.appendChild(p1);
             appendTitleTimes(e, p1);
             
             for(Event e2: filterForConflicts(events, e)){
             	P p2 = new P();
             	td2.appendChild(p2);
             	appendTitleTimes(e2, p2);
             }
         }
         
         writeHtmlFile(html, filepath+FILE_EXT);
 	}
 	
 	/**
 	 * Checks for events from a list that conflict with a given event
 	 * @param events List of events in which to look
 	 * @param e1 Event for which to find conflicts
 	 * @return A list of events conflicing with e1
 	 */
 	private List<Event> filterForConflicts(List<Event> events, Event e1){
 		ArrayList<Event> conflicting = new ArrayList<Event>();
 		DateTime e1s = e1.getStartTime();
     	DateTime e1e = e1.getEndTime();
 	    for(Event e2: events){
 	        if(e1!=e2){
 	        	//there is probably a more efficient way to check for conflicts,
 	        	//but I do not know it
 	        	DateTime e2s = e2.getStartTime();
 	        	DateTime e2e = e2.getEndTime();
 	        	//e1 starts before e2 ends
 	        	if(e1s.isAfter(e2s) && e1s.isBefore(e2e))
 	        		conflicting.add(e2);
 	        	//e1 ends after e2 starts
 	        	else if(e1e.isAfter(e2s) && e1e.isBefore(e2e))
 	        		conflicting.add(e2);
 	        	//e1 starts before and ends after e2
 	        	else if(e1s.isBefore(e2s) && e1e.isAfter(e2e))
 	        		conflicting.add(e2);
 	        	//e1 starts and ends at same time as e2
 	        	else if(e1s.isEqual(e2s) && e1e.isEqual(e2e))
 	        		conflicting.add(e2);
 	        }
 	    }
 	    return conflicting;
 	}
 	
 	/*public static void main (String[] args){
     	AbstractHtmlOutputter ho = new ConflictOutputter();
     	DateTime dt1 = new DateTime(2012, 2, 24, 11, 15);
     	DateTime dt2 = new DateTime(2012, 2, 29, 12, 30);
     	DateTime dt3 = new DateTime(2012, 2, 24, 11, 45);
     	DateTime dt4 = new DateTime(2012, 2, 24, 12, 00);
     	DateTime dt5 = new DateTime(2012, 2, 27, 12, 15);
     	DateTime dt6 = new DateTime(2012, 2, 27, 12, 30);
     	DateTime dt7 = new DateTime(2012, 2, 28, 12, 45);
     	DateTime dt8 = new DateTime(2012, 2, 28, 13, 00);
     	List<String> actor = new ArrayList<String>();
     	actor.add("actor");
     	Event e1 = new Event("Title", dt1, dt2, "Description", "Location", true, null);
     	Event e2 = new Event("Title2", dt3, dt4, "Description2", "Location", false, null);
     	Event e3 = new Event("Title3", dt5, dt6, "Description3", "Location2.333", false, null);
     	Event e4 = new Event("Title4", dt7, dt8, "Description 4", "Location4", false, null);
     	List<Event> l = new ArrayList<Event>();
     	l.add(e2);
     	l.add(e1);
     	l.add(e3);
     	l.add(e4);
     	ho.writeEvents(l);
     }*/
 
 }
