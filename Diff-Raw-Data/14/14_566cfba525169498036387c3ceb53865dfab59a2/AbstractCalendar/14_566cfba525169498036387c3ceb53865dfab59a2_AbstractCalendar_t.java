 package ch.unibe.ese.calendar.util;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import ch.unibe.ese.calendar.CalendarEvent;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.User;
 
 
 /**
  * An abstract superclass for classes implementing EseCalendar
  *
  */
 public abstract class AbstractCalendar extends EseCalendar {
 
 	public AbstractCalendar() {
 		super();
 	}
 
 	@Override
 	public SortedSet<CalendarEvent> getEventsAt(User user, Date dayStart) {
 		Date dayEnd = new Date(dayStart.getTime()+24*60*60*1000);
		//An Event has now a maximal length of 6 Months:
		long sixMonthInMs = 1000L*3600*24*30*6;
		Date dayStartMaxLength = new Date(dayStart.getTime()-sixMonthInMs);
 		//TODO it seems quite pointless to re-sort the events that are already sorted in the iterator
 		SortedSet<CalendarEvent> result = new TreeSet<CalendarEvent>(new StartDateComparator());
 		Iterator<CalendarEvent> iter = iterate(user, dayStartMaxLength);
 		while (iter.hasNext()) {
 			CalendarEvent ce = iter.next();		
 			if (ce.getStart().after(dayEnd)) {
 				break;
 			}
 			if (ce.getEnd().after(dayStart)) {
 				result.add(ce);
 			}
 				
 		}
 		return result;
 	}
 
 }
