 package output;
 import java.util.*;
 import model.Event;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
import Sorting.StartTimeSorter;
 import com.hp.gagawa.java.elements.*;
 
 /**
  * Condensed calendar output for a single month
  * The month in question is the month of the earliest
  * element in the given list
  * @author herio
  *
  */
 public class MonthDetailOutputter extends DetailOutputter{
 	
     public void writeEvents(List<Event> events) {
         DateTime dt;
         if(events.isEmpty())
             dt = new DateTime();
         else{
         	StartTimeSorter sts = new StartTimeSorter();
         	events = sts.sort(events);
             dt = new DateTime(events.get(0).getStartTime());
         }
         
         String filepath = "MonthDetail_of_" + dt.toString("MMMYYYY");
         
         Html html = new Html();
         Body body = new Body();
         html.appendChild(body);
         Table table = new Table();
         table.setBorder("");
         body.appendChild(table);
         
         Tr row0 = new Tr();
         row0.appendChild(new Text(dt.toString("MMMM YYYY")));
         table.appendChild(row0);
         
         int thisMonth = dt.getMonthOfYear();  
         dt = dt.minusDays(dt.getDayOfWeek()-1);
         while(dt.getMonthOfYear()==thisMonth)
         	dt = dt.minusWeeks(1);
         
         Tr row1 = new Tr();
         DateTime dummy = new DateTime(dt);
         for(int i=0;i<DateTimeConstants.DAYS_PER_WEEK;i++){
         	Td DoW = new Td();
         	DoW.setWidth("1%");
         	B b = new B();
         	Div div = new Div();
         	div.setStyle("text-align:center");
         	b.appendChild(new Text(dummy.dayOfWeek().getAsText()));
         	div.appendChild(b);
         	DoW.appendChild(div);
         	row1.appendChild(DoW);
         	dummy = dummy.plusDays(1);
         }
         table.appendChild(row1);
         
         do{
         	Tr rown = new Tr();
         	rown.setValign("top");
         	for(int i=0;i<DateTimeConstants.DAYS_PER_WEEK;i++){
         		Td td = new Td();
         		P p = new P();
         		B b = new B();
         		b.appendChild(new Text(dt.toString("MM/dd")+"<br/>"));
         		p.appendChild(b);
         		writeEventP(p, events, dt, filepath);
         		td.appendChild(p);
         		rown.appendChild(td);
         		
         		dt = dt.plusDays(1);
         	}
         	table.appendChild(rown);
         }while(dt.getMonthOfYear()==thisMonth);
         
         writeHtmlFile(html, filepath+FILE_EXT);
     }
     
     /*public static void main (String[] args){
     	AbstractHtmlOutputter ho = new MonthDetailOutputter();
     	DateTime dt1 = new DateTime(2012, 2, 2, 11, 15);
     	DateTime dt2 = new DateTime(2012, 2, 2, 11, 30);
     	DateTime dt3 = new DateTime(2012, 2, 24, 11, 45);
     	DateTime dt4 = new DateTime(2012, 2, 24, 12, 00);
     	DateTime dt5 = new DateTime(2012, 2, 24, 12, 15);
     	DateTime dt6 = new DateTime(2012, 2, 24, 12, 30);
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
