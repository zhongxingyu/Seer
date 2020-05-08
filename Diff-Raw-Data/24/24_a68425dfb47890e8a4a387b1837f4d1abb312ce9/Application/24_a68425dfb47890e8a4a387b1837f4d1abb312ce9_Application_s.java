 package controllers;
 
 import java.security.PrivilegedAction;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.regex.PatternSyntaxException;
 
 import javax.security.auth.Subject;
 
 import play.*;
 import play.data.binding.types.CalendarBinder;
 import play.mvc.*;
 import sun.security.action.GetLongAction;
 
 import ch.unibe.ese.calendar.CalendarEntry;
 import ch.unibe.ese.calendar.CalendarEvent;
 import ch.unibe.ese.calendar.CalendarManager;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.EseDateFormat;
 import ch.unibe.ese.calendar.EventIteratorMerger;
 import ch.unibe.ese.calendar.EventSeries;
 import ch.unibe.ese.calendar.SerialEvent;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.exceptions.CalendarAlreadyExistsException;
 import ch.unibe.ese.calendar.exceptions.EventNotFoundException;
 
 @With(Secure.class)
 public class Application extends Controller {
 	
 	public static int selectedDay, selectedMonth, selectedYear;
 	
 	public static void currentCalendar(String name) {
 		java.util.Calendar juc = java.util.Calendar.getInstance(getLocale());
 		juc.setTime(new Date());
 		selectedDay = juc.get(java.util.Calendar.DAY_OF_MONTH);
 		selectedMonth = juc.get(java.util.Calendar.MONTH);
 		selectedYear = juc.get(java.util.Calendar.YEAR);
 		calendar(name);
 	}
 
 	/**
 	 * This method shows the calendar page. 
 	 * The selected day is read from instance variables.
 	 * 
 	 * @param name The name of the calendar
 	 */
 	public static void calendar(String name) {
 		
 		System.out.println("name: " + name);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		final EseCalendar calendar = calendarManager.getCalendar(name);
 		Calendar juc = Calendar.getInstance(getLocale());
 		juc.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
 		final Date date = juc.getTime();
 
 		SortedSet<CalendarEvent> set1 = calendar.getEventsAt(user, date);
 		Iterator<CalendarEvent> iterator = set1.iterator();
 		
 		Map<User, Boolean> contacts = user.getMyContacts();
 		Iterator<User> iterU = contacts.keySet().iterator();
 		Set<EseCalendar> selectedUsersCal = new HashSet<EseCalendar>();
 		
 		while (iterU.hasNext()){
 			User contact = iterU.next();
 			if (contacts.get(contact)){
 				Set<EseCalendar> contactCalendars = calendarManager.getCalendarsOf(contact);
 				selectedUsersCal.addAll(contactCalendars);
 				Iterator<EseCalendar> eseCIter = contactCalendars.iterator();
 				while (eseCIter.hasNext()){
 					EseCalendar contactCal = eseCIter.next();
 					Iterator<CalendarEvent> iteratorCalEvent =  contactCal.getEventsAt(user, date).iterator();
 					iterator = new EventIteratorMerger(iterator, iteratorCalEvent); 
 				}
 			}
 		}
 		
		
 		CalendarBrowser calendarBrowser = new CalendarBrowser(user, calendar,
 				selectedUsersCal, selectedDay, selectedMonth, selectedYear, getLocale());
 		
 		Set<User> myContacts = user.getMyContacts().keySet();
 		render(iterator, calendar, calendarBrowser, myContacts, user);
 	}
 
 	/**
 	 * @return the client locale guessed from accept-language haeder
 	 */
 	private static Locale getLocale() {
 		// TODO make real
		return new Locale("de", "CH");
 	}
 
 	public static void user(String name) {
 		String currentUserName = Security.connected();
 		User currentUser = UserManager.getInstance().getUserByName(
 				currentUserName);
 		User user = UserManager.getInstance().getUserByName(name);
 		Set<EseCalendar> otherCalendars = CalendarManager.getInstance()
 				.getCalendarsOf(user);
 		render(currentUser, user, otherCalendars);
 	}
 	
 	public static void addToContacts(String calendarName, String name) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		User userToAdd = UserManager.getInstance().getUserByName(name);
 		user.addToMyContacts(userToAdd);
 		calendar(calendarName);
 	}
 	
 	public static void removeFromContacts(String calendarName, String name) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		User userToRemove = UserManager.getInstance().getUserByName(name);
 		user.removeFromMyContacts(userToRemove);
 		calendar(calendarName);
 	}
 
 	public static void createEvent(String calendarName, String name,
 			String startDate, String endDate, boolean isPublic, String repetition)
 			throws Throwable {
 		System.out.println("creating event");
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 		Date eDate = EseDateFormat.getInstance().parse(endDate);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 
 		if (repetition.equalsIgnoreCase("never")) {
 				System.out.println("selected repetition: never");
 				final CalendarEvent event = calendar.addEvent(user, sDate, 
 						eDate, name, isPublic);
 				System.out.println("pre created size "
 				+ calendar.getEventsAt(user,
 						new Date(event.getStart().getTime() - 2000)).size());
 				System.out.println("created event " + event);
 				System.out.println("created size "
 					+ calendar.getEventsAt(user,
 							new Date(event.getStart().getTime() - 2000)).size());
 
 				System.out.println("created event  in " + calendarName);
 		}
 		else{
 			final EventSeries eventseries = calendar.addEventSeries(user, sDate, 
 					eDate, name, isPublic, repetition);
 		}
 		calendar(calendarName);
 	}
 
 	/**
 	 * Shows the calendar with the given date selected.
 	 * @param calendarName
 	 * @param day
 	 * @param month
 	 * @param year
 	 */
 	public static void selectDate(String calendarName, int day, int month, int year) {
 		selectedDay = day;
 		selectedMonth = month;
 		selectedYear = year;
 		calendar(calendarName);
 	}
 	
 	/**
 	 * -- this method is not in use right now --
 	 * 
 	 * Does same as selectDate(String calendarName, int day, int month, int year),
 	 * but with other parameters. Small helper method.
 	 * Shows the calendar with the given Date selected.	 * 
 	 * @param calendarName
 	 * @param date 
 	 */
 	private static void selectDate(String calendarName, Date date) {
 		Calendar juc = Calendar.getInstance(getLocale());
 		juc.setTime(date);
 		selectDate(calendarName, juc.get(java.util.Calendar.DAY_OF_MONTH),
 				juc.get(java.util.Calendar.MONTH),
 				juc.get(java.util.Calendar.YEAR));
 	}
 	
 	/**
 	 * An event is identified by its unique hash. For finding it
 	 * easily, we have to know it's startDate.
 	 * 
 	 * TODO: Introduce an ID for identifying an event
 	 * 
 	 * @param calendarName
 	 * @param hash hashCode() of the to be deleted event
 	 */
 	public static void deleteEvent(String calendarName, int hash,
 			String startDate) throws ParseException {
 
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		try {
 			CalendarEntry e = calendar.removeEvent(user, hash, sDate);
 			calendar(calendarName);
 		} catch (EventNotFoundException exception) {
 			error(exception.getMessage());
 		}
 	}
 
 	public static void editEvent(String calendarName, int hash, String startDate)
 			throws ParseException {
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		try {
 			CalendarEntry event = calendar.getEventByHash(user, hash, sDate);
 			render(calendar, event);
 		} catch (EventNotFoundException exception) {
 			error(exception.getMessage());
 		}
 	}
 	
 	public static void saveEditedEvent(String calendarName, int hash, String oldStartDate, 
 			String name, String startDate, String endDate, boolean isPublic) 
 			throws ParseException {
 		
 		Date oldDate = EseDateFormat.getInstance().parse(oldStartDate);
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 		Date eDate = EseDateFormat.getInstance().parse(endDate);
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		CalendarEntry event = calendar.getEventByHash(user, hash, oldDate);
 		event.set(name, sDate, eDate, isPublic);
 		selectDate(calendarName, sDate);
 	}
 	
 	/**
 	 * First sets all Contacts to unselected then sets all Contacts that have 
 	 * their name in checkedContacts[] to selected
 	 * 
 	 * @param calendarName
 	 * @param checkedContacts: all Contacts who's checkbox is selected
 	 */
 	public static void includeContacts(String calendarName, String[] checkedContacts) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		user.unselectAllContacts();
 		if (checkedContacts != null) {
 			for (String uName: checkedContacts) {
 				User u = UserManager.getInstance().getUserByName(uName);
 				user.setContactSelection(u, true);
 			}
 		}	
 		calendar(calendarName);
 	}
 }
