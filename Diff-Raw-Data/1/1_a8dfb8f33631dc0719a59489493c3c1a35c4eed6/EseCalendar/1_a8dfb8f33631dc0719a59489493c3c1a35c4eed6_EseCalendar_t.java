 package ch.unibe.ese.calendar;
 
 import java.security.AccessControlException;
 import java.security.AccessController;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.NoSuchElementException;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import ch.unibe.ese.calendar.EventSeries.Repetition;
 import ch.unibe.ese.calendar.security.Policy;
 import ch.unibe.ese.calendar.security.PrivilegedCalendarAccessPermission;
 
 public class EseCalendar {
 	
 	private SortedSet<CalendarEvent> startDateSortedSet = new TreeSet<CalendarEvent>(new StartDateComparator());
 	private SortedSet<EventSeries> startDateSortedSetOfSeries = new TreeSet<EventSeries>(new StartDateComparator());
 	/**
 	 * Constructs a calendar. Application typically create and retrieve calendars using the CalendarManager.
 	 * 
 	 * @param name the name of the calendar
 	 * @param owner the owner of the calendar
 	 */
 	protected EseCalendar(String name, User owner) {
 		this.name = name;
 		this.owner = owner;
 	}
 
 	private String name;
 	private User owner;
 	
 	/**
 	 * 
 	 * @return the name of this calendar
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * When <code>ch.unibe.ese.calendar.security.CalendarPolicy</code> is in place the owner can write to this calendar.
 	 * 
 	 * @return the owner of this calendar
 	 */
 	public User getOwner() {
 		return owner;
 	}
 	
 	public SortedSet<CalendarEvent> getStartDateSortedSet() {
 		return startDateSortedSet;
 	}
 	
 	public SortedSet<EventSeries> getStartDateSortedSetOfSeries() {
 		return startDateSortedSetOfSeries;
 	}
 
 	/**
 	 * Adds the event to the calendar
 	 * 
 	 * @param calendarEvent
 	 */
 	public void addEvent(User user, CalendarEvent calendarEvent) {
 		Policy.getInstance().checkPermission(user, new PrivilegedCalendarAccessPermission(name));
 		startDateSortedSet.add(calendarEvent);
 	}
 	
 	public void addEventSeries(User user, EventSeries eventSerie){
 		Policy.getInstance().checkPermission(user, new PrivilegedCalendarAccessPermission(name));
 		startDateSortedSetOfSeries.add(eventSerie);
 	}
 	
 	/**
 	 * Removes an event from the calendar
 	 * 
 	 * Needs a startDate so we don't have to go through the whole list for finding the right event.
 	 * @return the event removed
 	 */
 	public CalendarEntry removeEvent(User user, int hash, Date start) {
 		Policy.getInstance().checkPermission(user, new PrivilegedCalendarAccessPermission(name));
 		CalendarEntry e = getEventByHash(user, hash, start);
 		startDateSortedSet.remove(e);
 		return e;
 	}
 	
 	/**
 	 * Only returns an event if the user has privileged access.
 	 * @param hash The hash the event produces by calling hashCode()
 	 * @return null, if the Event is not found.
 	 */
 	public CalendarEntry getEventByHash(User user, int hash, Date start) {
 		Policy.getInstance().checkPermission(user, new PrivilegedCalendarAccessPermission(name));
 		CalendarEvent compareDummy = new CalendarEvent(start, start, "compare-dummy", false);
 		Iterator<CalendarEvent> afterStart = startDateSortedSet.tailSet(compareDummy).iterator();
		//TODO: also check, startDateSortedSet for this hash (or create own method for deleting series)
 		CalendarEntry e;
 		do {
 			e = afterStart.next();
 		} while (e.hashCode() != hash);
 		return e;
 	}
 
 	/**
 	 * Iterates through the non-serial events with a start date after start
 	 * 
 	 * @param start the date at which to start iterating events
 	 * @return an iterator with events starting after start
 	 */
 	public Iterator<CalendarEvent> iterate(User user, Date start) {
 		CalendarEvent compareDummy = new CalendarEvent(start, start, "compare-dummy", false);
 		Iterator<CalendarEvent> unfilteredEvents = startDateSortedSet.tailSet(compareDummy).iterator();
 		return new ACFilteringEventIterator(user, unfilteredEvents);
 	}
 	/**
 	 * Iterates through all serial events
 	 * 
 	 * @return an iterator with all serial events
 	 */
 	public Iterator<EventSeries> iterateSeries(User user){
 		Iterator<EventSeries> allEventSeries = startDateSortedSetOfSeries.iterator();
 		return new ACFilteringEventSeriesIterator(user, allEventSeries);
 	}
 	
 	/**
 	 * Gets a list of events starting within the 24 hour period starting at date;
 	 * 
 	 * @param date the point in time specifying the start of the 24h period for which events are to be returned
 	 * @return a list of the events
 	 */
 	public SortedSet<CalendarEntry> getEventsAt(User user, Date date) {
 		Date endDate = new Date(date.getTime()+24*60*60*1000);
 		SortedSet<CalendarEntry> result = new TreeSet<CalendarEntry>(new StartDateComparator());
 		Iterator<CalendarEvent> iter = iterate(user, date);
 		while (iter.hasNext()) {
 			CalendarEvent ce = iter.next();
 			if (ce.getStart().compareTo(endDate) > 0) {
 				break;
 			}
 			result.add(ce);
 		}
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param user the user requesting the event
 	 * @param date a point in time that is part of the day for which the vents are requested
 	 * @return a sorted list of SerialEventS for the specified day
 	 */
 	public SortedSet<CalendarEntry> getSerialEventsForDay(User user, Date date){
 		SortedSet<CalendarEntry> result =  new TreeSet<CalendarEntry>(new StartDateComparator());
 		Iterator<EventSeries> iter = iterateSeries(user);
 		while (iter.hasNext()) {
 			EventSeries es = iter.next();
 			
 			if (dateMatches(date, es)) {
 				java.util.Calendar juc = java.util.Calendar.getInstance(new Locale("de", "CH"));
 				juc.setTime(date);
 				int year = juc.get(Calendar.YEAR);
 				int month =juc.get(Calendar.MONTH);
 				int day = juc.get(Calendar.DAY_OF_MONTH);
 				juc.setTime(es.start);
 				int hour = juc.get(Calendar.HOUR_OF_DAY);
 				int min = juc.get(Calendar.MINUTE);
 				juc.set(year, month, day, hour, min);
 				Date start = juc.getTime();
 				juc.setTime(es.end);
 				int hour2 = juc.get(Calendar.HOUR_OF_DAY);
 				int min2 = juc.get(Calendar.MINUTE);
 				juc.set(year, month, day, hour2, min2);
 				Date end = juc.getTime();
 				SerialEvent se = new SerialEvent(start, end, es.name, es.isPublic, es);
 				result.add(se);
 			}
 			
 		}
 		return result;
 		
 	} 
 	
 	private boolean dateMatches(Date date, EventSeries es) {
 			Repetition repetition = es.getRepetition();
 			java.util.Calendar juc1 = java.util.Calendar.getInstance(new Locale("de", "CH"));
 			juc1.setTime(es.getStart());
 			
 			int weekDayOfEventSerie = juc1.get(Calendar.DAY_OF_WEEK);
 			int monthDayOfEventSerie = juc1.get(Calendar.DAY_OF_MONTH);
 			java.util.Calendar juc2 = java.util.Calendar.getInstance(new Locale("de", "CH"));
 			juc2.setTime(date);
 			int weekDayOfDate = juc2.get(Calendar.DAY_OF_WEEK);
 			int monthDayOfDate = juc2.get(Calendar.DAY_OF_MONTH);
 			if (repetition.equals(repetition.DAILY)) {
 				System.out.println("daily");
 				return true;
 			}
 			if (repetition.equals(repetition.WEEKLY)){
 				System.out.println("weekly");
 				System.out.println("weekDayOfEventSerie" + weekDayOfEventSerie);
 				System.out.println("weekDayOfDate" + weekDayOfDate);
 				return (weekDayOfEventSerie == weekDayOfDate);
 			}
 			if (repetition.equals(repetition.MONTHLY)) {
 				System.out.println("monthly");
 				return (monthDayOfEventSerie == monthDayOfDate);
 			}
 		return false;
 	}
 
 	private class ACFilteringEventIterator implements Iterator<CalendarEvent> {
 
 		private boolean hasNext;
 		private CalendarEvent next;
 		private Iterator<CalendarEvent> unfilteredEvents;
 		private User user;
 		
 		public ACFilteringEventIterator(User user, Iterator<CalendarEvent> unfilteredEvents) {
 			this.unfilteredEvents = unfilteredEvents;
 			this.user = user;
 			prepareNext();
 		}
 
 		private void prepareNext() {
 			hasNext = false;
 			if (unfilteredEvents.hasNext()) {
 				CalendarEvent ce = unfilteredEvents.next();
 				if (!ce.isPublic()) {
 					if (!Policy.getInstance().hasPermission(user, new PrivilegedCalendarAccessPermission(name))) {
 						prepareNext();
 						return;
 					}
 				}
 				next = ce;
 				hasNext = true;
 			}
 		}
 
 		@Override
 		public boolean hasNext() {
 			return hasNext;
 		}
 
 		@Override
 		public CalendarEvent next() {
 			if (!hasNext) {
 				throw new NoSuchElementException();
 			}
 			CalendarEvent result = next;
 			prepareNext();
 			return result;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException("not supported yet");
 		}
 
 	}
 	private class ACFilteringEventSeriesIterator implements Iterator<EventSeries> {
 
 		private boolean hasNext;
 		private EventSeries next;
 		private Iterator<EventSeries> eventSeries;
 		private User user;
 		
 		public ACFilteringEventSeriesIterator(User user, Iterator<EventSeries> eventSeries) {
 			this.eventSeries = eventSeries;
 			this.user = user;
 			prepareNext();
 		}
 
 		private void prepareNext() {
 			hasNext = false;
 			if (eventSeries.hasNext()) {
 				EventSeries es = eventSeries.next();
 				if (!es.isPublic()) {
 					if (!Policy.getInstance().hasPermission(user, new PrivilegedCalendarAccessPermission(name))) {
 						prepareNext();
 						return;
 					}
 				}
 				next = es;
 				hasNext = true;
 			}
 		}
 
 		@Override
 		public boolean hasNext() {
 			return hasNext;
 		}
 
 		@Override
 		public EventSeries next() {
 			if (!hasNext) {
 				throw new NoSuchElementException();
 			}
 			EventSeries result = next;
 			prepareNext();
 			return result;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException("not supported yet");
 		}
 
 	}
 
 	
 
 }
