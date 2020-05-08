 package output;
 
 import model.Event;
 import java.util.*;
 import org.joda.time.*;
import Sorting.StartTimeSorter;
 import com.hp.gagawa.java.elements.*;
 
 /**
  * Condensed calendar output for a single week
  * The week in question is the 7 day period starting from
  * the earliest event in the given list
  * @author herio
  *
  */
 public class WeekDetailOutputter extends DetailOutputter{
     
     public void writeEvents(List<Event> events) {
         DateTime dt;
         if(events.isEmpty())
             dt = new DateTime();
         else{
         	StartTimeSorter sts = new StartTimeSorter();
         	events = sts.sort(events);
             dt = new DateTime(events.get(0).getStartTime());
         }
             
         String filepath = "WeekDetail_of_" + dt.toString("MMMdd");
         
         Html html = new Html();
         Body body = new Body();
         html.appendChild(body);
         Table table = new Table();
         table.setBorder("");
         body.appendChild(table);
         
         Tr row0 = new Tr();
         row0.appendChild(new Text("Week of "+dt.toString("MMM dd, YYYY")));
         Tr row1 = new Tr();
         Tr row2 = new Tr();
         row2.setValign("top");
         for(int i=0; i<DateTimeConstants.DAYS_PER_WEEK; i++){
         	Td dayHeader = new Td();
         	dayHeader.setWidth("1%");
         	B b = new B();
         	Div div = new Div();
         	div.setStyle("text-align:center");
         	div.appendChild(b);
         	dayHeader.appendChild(div);
         	row1.appendChild(dayHeader);
         	
         	b.appendChild(new Text(dt.toString("MM/dd")+" ("+dt.dayOfWeek().getAsText()+")"));
 
         	Td evs = new Td();
         	P p = new P();
         	evs.appendChild(p);
         	writeEventP(p, events, dt, filepath);
         	
         	row2.appendChild(evs);
         	
         	dt = dt.plusDays(1);
         }
         table.appendChild(row0, row1, row2);
         
         writeHtmlFile(html, filepath + FILE_EXT);
     }
     
     /*public static void main (String[] args){
     	AbstractHtmlOutputter ho = new WeekDetailOutputter();
     	DateTime dt1 = new DateTime(2012, 2, 24, 11, 15);
     	DateTime dt2 = new DateTime(2012, 2, 24, 11, 30);
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
