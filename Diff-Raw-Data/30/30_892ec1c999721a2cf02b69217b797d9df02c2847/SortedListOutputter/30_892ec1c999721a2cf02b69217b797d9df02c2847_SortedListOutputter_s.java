 package output;
 import model.Event;
 import java.util.*;
 import org.joda.time.*;
import Sorting.StartTimeSorter;
 import com.hp.gagawa.java.elements.*;
 
 /**
  * Very condensed output that simply displays the title and
  * start time of a list of events sorted by start time
  * @author herio
  *
  */
 public class SortedListOutputter extends AbstractHtmlOutputter{
 
 	public void writeEvents(List<Event> events) {
 		DateTime dt;
         if(events.isEmpty())
             dt = new DateTime();
         else{
         	StartTimeSorter sts = new StartTimeSorter();
         	events = sts.sort(events);
             dt = new DateTime(events.get(0).getStartTime());
         }
             
         String filepath = "Event_List_from_" + dt.toString("MMMdd");
         
         Html html = new Html();
         Body body = new Body();
         P p = new P();
         html.appendChild(body);
         B titleB = new B();
         body.appendChild(titleB, p);
         
         titleB.appendChild(new Text("Event list from "+dt.toString("MM/dd")+" by starting time"));
         
         B b = new B();
         b.appendChild(new Text(dt.toString("MM/dd")+"<br/>"));
         p.appendChild(b);
         
         for(Event e: events){
         	checkNewDay(p, dt, e.getStartTime());
         	U u = new U();
         	u.appendChild(new Text(e.getTitle()));
         	p.appendChild(u);
         	p.appendChild(new Text(" ("+e.getStartTime().toString("HH:mm")+" start)<br/>"));
         }
         
         writeHtmlFile(html, filepath+FILE_EXT); 
 	}
 	
 	/**
 	 * Writes day headers for each day from the date of the first
 	 * event to the second (so one header will be written for events
 	 * one day apart, two headers for two days apart, etc.)
 	 * @param p P element to which to append
 	 * @param first First date
 	 * @param second Second date
 	 */
 	private void checkNewDay(P p, DateTime first, DateTime second){
 		if(second.getYear()>=first.getYear() && second.getDayOfYear()>first.getDayOfYear()){
 			while(!isSameDate(first, second)){
 				first = first.plusDays(1);
 				B b = new B();
 				b.appendChild(new Text(first.toString("MM/dd")+"<br/>"));
 				p.appendChild(b);
 			}
 		}
 	}
 	
 	/*public static void main (String[] args){
     	AbstractHtmlOutputter ho = new SortedListOutputter();
     	DateTime dt1 = new DateTime(2012, 2, 24, 11, 15);
     	DateTime dt2 = new DateTime(2012, 2, 24, 11, 30);
     	DateTime dt3 = new DateTime(2012, 2, 24, 11, 45);
     	DateTime dt4 = new DateTime(2012, 2, 24, 12, 00);
     	DateTime dt5 = new DateTime(2012, 2, 27, 12, 15);
     	DateTime dt6 = new DateTime(2012, 2, 27, 12, 30);
     	DateTime dt7 = new DateTime(2012, 4, 28, 12, 45);
     	DateTime dt8 = new DateTime(2012, 4, 28, 13, 00);
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
