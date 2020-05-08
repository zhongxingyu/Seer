 package controllers;
 
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.ArrayList;
 
 import javax.activity.InvalidActivityException;
 
 import play.mvc.Controller;
 import play.mvc.With;
 import ch.unibe.ese.calendar.CalendarEvent;
 import ch.unibe.ese.calendar.CalendarManager;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.util.EseDateFormat;
 import ch.unibe.ese.calendar.util.EventIteratorUtils;
 
 @With(Secure.class)
 public class Application extends Controller {
 	
 	public static final int PAGESIZE = 5;
 	public static int selectedDay, selectedMonth, selectedYear;
 	
 	/**
 	 * This method will display the current calendar of a specified user.
 	 * It will automatically select the current day.
 	 * @param userName The name of the user whose calendar will be displayed.
 	 * @param calendarName the name of the calendar which will be displayed.
 	 */
 	public static void currentCalendar(String userName, String calendarName) {
 		java.util.Calendar juc = java.util.Calendar.getInstance(getLocale());
 		juc.setTime(new Date());
 		selectedDay = juc.get(java.util.Calendar.DAY_OF_MONTH);
 		selectedMonth = juc.get(java.util.Calendar.MONTH);
 		selectedYear = juc.get(java.util.Calendar.YEAR);
		calendar(userName, calendarName);
 	}
 
 	/**
 	 * This method shows the calendar page of a specified user. 
 	 * The selected day is read from instance variables.
 	 * 
 	 * @param userName The name of the user whose calendar will be displayed.
 	 * @param calendarName calendarName the name of the calendar which will be displayed
 	 */
 	public static void calendar(String userName, String calendarName, String searchRegex, int curPage) {
 		String connectedUserName = Security.connected();
 		User connectedUser = UserManager.getInstance().getUserByName(connectedUserName);
 		User user = UserManager.getInstance().getUserByName(userName);
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		EseCalendar calendar;
 		Set<EseCalendar> selectedOwnCalendars = new HashSet<EseCalendar>();
 		if (calendarName != null){
 			calendar = calendarManager.getCalendar(calendarName);
 		}
 		else {
 			 calendar = selectCalendarToDisplay(user, connectedUser);
 		}
 		if (calendar.isSelected()){
 			selectedOwnCalendars.add(calendar);
 		}
 		Calendar juc = Calendar.getInstance(getLocale());
 		juc.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
 		final Date selectedDate = juc.getTime();
 		Map<User, Boolean> myContactsMap = user.getMyContacts();
 		Iterator<User> iterMyContacts = myContactsMap.keySet().iterator();
 		Set<EseCalendar> selectedUsersCal = new HashSet<EseCalendar>();
 		Iterator iterator = Collections.EMPTY_LIST.iterator();
 		Iterator<EseCalendar> ownCalendarsIter = calendarManager.getCalendarsOf(user).iterator();
 		while (ownCalendarsIter.hasNext()){
 			EseCalendar ownCalendar = ownCalendarsIter.next();
 			if (ownCalendar.isSelected() || !user.equals(connectedUser)){
 				selectedOwnCalendars.add(ownCalendar);
 				Iterator<CalendarEvent> iteratorCalEvent;
 				iteratorCalEvent = (searchRegex == null)
 					?ownCalendar.getEventsAt(connectedUser,
 						selectedDate).iterator()
 					:ownCalendar.getEventsByRegex(connectedUser,
 						searchRegex).iterator();
 				iterator = EventIteratorUtils.merge(iterator, iteratorCalEvent);
 			}
 		}
 		
 		while (iterMyContacts.hasNext()){
 			User contact = iterMyContacts.next();
 			if (myContactsMap.get(contact)){
 				Set<EseCalendar> contactCalendars = new HashSet <EseCalendar>();
 				if (!contact.equals(user)){
 					 contactCalendars = calendarManager.getCalendarsOf(contact);
 				}
 				selectedUsersCal.addAll(contactCalendars);
 				Iterator<EseCalendar> eseCalendarIter = contactCalendars.iterator();
 				while (eseCalendarIter.hasNext()){
 					EseCalendar contactCal = eseCalendarIter.next();
 					Iterator<CalendarEvent> iteratorCalEvent;
 					iteratorCalEvent = (searchRegex == null)
 						?contactCal.getEventsAt(connectedUser,
 							selectedDate).iterator()
 						:contactCal.getEventsByRegex(connectedUser,
 							searchRegex).iterator();
 					iterator = EventIteratorUtils.merge(iterator, iteratorCalEvent); 
 				}
 			}
 		}
 		CalendarBrowser calendarBrowser = new CalendarBrowser(user, selectedOwnCalendars ,
 				selectedUsersCal, selectedDay, selectedMonth, selectedYear, getLocale());
 		Set<User> myContacts = connectedUser.getSortedContacts();
 		SortedSet<EseCalendar> myCalendars = calendarManager.getCalendarsOf(connectedUser);
 		String selectedDateString = EseDateFormat.getInstance().format(
 				new Date(selectedDate.getTime() + 1000*60*60*12));
 		Iterator<EseCalendar> calendarIter = calendarManager.getCalendarsOf(user).iterator();
 
 		/**
 		 *	The following chunk works, but probably belongs
 		 *	somewhere else..
 		 */
 		Object obj;
 		ArrayList<Object> tmp;
 		int skip = curPage*PAGESIZE;
 		int page = PAGESIZE;
 		tmp = new ArrayList<Object>();
 		if (curPage >= 0) {
 			while (iterator.hasNext()) {
 				obj = iterator.next();
 				if (skip != 0) {
 					skip--;
 					continue;
 				}
 				if (page != 0) {
 					page--;
 					tmp.add(obj);
 				}
 				continue;
 			}
 			iterator = tmp.iterator();
 		}
 
 		render(iterator, calendar, calendarBrowser, myContacts, searchRegex, curPage,
 				connectedUser, selectedDateString, myCalendars, calendarIter);
 	}
	
	public static void calendar(String userName, String calendarName) {
		calendar(userName, calendarName, null, -1);
	}
 
 	public static void searchEvent(String userName, String
 	    calendarName, String searchRegex, int curPage) {
 		calendar(userName, calendarName, searchRegex, curPage);
 	}
 
 	public static void searchEvent(String userName, String
 	    calendarName, String searchRegex) {
 		calendar(userName, calendarName, searchRegex, -1);
 	}
 
 	/**
 	 * Selects the calendar(s) to display according to the user.
 	 * If the user is the connected one, the calendar page will
 	 * display his first calendar when the checkbox 'me' is selected. 
 	 * (TODO: distinguish ALL of his Calendars)
 	 * and the unionCalendars of his contacts.
 	 * If the user is not the connected one, the calendar page
 	 * will simply display the unionCalendar of the user.
 	 * @param user
 	 * @param connectedUser
 	 * @return
 	 */
 	private static EseCalendar selectCalendarToDisplay(User user,
 			User connectedUser) {
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		SortedSet<EseCalendar> connectedUserCalendars = calendarManager.
 				getCalendarsOf(connectedUser);
 		EseCalendar calendar = null;
 		if (!calendarManager.getCalendarsOf(connectedUser).isEmpty()) {
 			calendar = connectedUserCalendars.iterator().next();
 		}
 		if (!user.equals(connectedUser)) {
 			calendar = calendarManager.getUnionCalendarOf(user);
 		}
 		return calendar;
 	}
 
 	/**
 	 * @return the client locale guessed from accept-language haeder
 	 */
 	private static Locale getLocale() {
 		// TODO make real
 		return new Locale("en", "CH");
 	}
 
 	public static void user() {
 		String currentUserName = Security.connected();
 		User connectedUser = UserManager.getInstance().getUserByName(
 				currentUserName);
 		SortedSet<EseCalendar> calendars = CalendarManager.getInstance()
 				.getCalendarsOf(connectedUser);
 		render(connectedUser, calendars);
 	}
 	
 	public static void addToContacts(String name) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		User userToAdd = UserManager.getInstance().getUserByName(name);
 		user.addToMyContacts(userToAdd);
		calendar(name, null);
 	}
 	
 	/**
 	 * Remove the user with the specified name from myContacts.
 	 * @param name of the user to remove
 	 */
 	public static void removeFromContacts(String name) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		User userToRemove = UserManager.getInstance().getUserByName(name);
 		try {
 			user.removeFromMyContacts(userToRemove);
 		} catch (InvalidActivityException e) {
 			e.printStackTrace();
 		}
		calendar(name, null);
 	}
 	
 	/**
 	 * Shows the calendar with the given date selected.
 	 * @param calendarName
 	 * @param day
 	 * @param month
 	 * @param year
 	 * @param calendarName
 	 */
 	public static void selectDate(String userName, int day, int month, int year, String calendarName) {
 		selectedDay = day;
 		selectedMonth = month;
 		selectedYear = year;
		calendar(userName, calendarName);
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
 				juc.get(java.util.Calendar.YEAR), calendarName);
 	}
 	
 	/**
 	 * First sets all Contacts to unselected then sets all Contacts that have 
 	 * their name in checkedContacts[] to selected
 	 * 
 	 * @param checkedContacts: all Contacts who's checkbox is selected
 	 */
 	public static void includeContacts(String[] checkedContacts, String calendarName) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		user.unselectAllContacts();
 		if (checkedContacts != null) {
 			for (String uName: checkedContacts) {
 				User u = UserManager.getInstance().getUserByName(uName);
 				user.setContactSelection(u, true);
 			}
 		}	
		calendar(userName, calendarName);
 	}
 	
 	public static void deleteCalendar(String calendarName) {
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		try {
 			calendarManager.removeCalendar(calendarName);
 		} catch (Exception e) {
 			error(e.getMessage());
 		}
 		user();
 		
 	}
 	
 	public static void addCalendar(String calendarName) {
 		String userName = Security.connected();
 		UserManager um = UserManager.getInstance();
 		User connectedUser = um.getUserByName(userName);
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		EseCalendar cal = calendarManager.createCalendar(connectedUser, calendarName);
 		if (calendarManager.getCalendarsOf(connectedUser).size()==1) {
 			currentCalendar(userName, cal.getName());
 		}
 		user();
 	}
 
 	/**
 	 * First sets all Calendars to unselected then sets all Calendars that have 
 	 * their name in checkedCalendars[] to selected
 	 * 
 	 * @param checkedCalendars: all Calendars who's checkbox is selected
 	 */
 	public static void includeCalendars(String[] checkedCalendars, String calendarName) {
 		String userName = Security.connected();
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		UserManager um = UserManager.getInstance();
 		User user = um.getUserByName(userName);
 		calendarManager.unSelectAllCalendars(user);
 		if (checkedCalendars != null) {
 			for (String calendar: checkedCalendars) {
 				System.out.println(calendar);
 				EseCalendar eseCalendar = calendarManager.getCalendar(calendar);
 				eseCalendar.select(true);
 			}
 		}	
		calendar(userName, calendarName);
 	}
 }
