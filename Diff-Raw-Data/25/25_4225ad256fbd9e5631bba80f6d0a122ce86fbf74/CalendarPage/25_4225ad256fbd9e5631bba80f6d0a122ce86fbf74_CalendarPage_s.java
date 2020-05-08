 package controllers;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 
 import ch.unibe.ese.calendar.CalendarEvent;
 import ch.unibe.ese.calendar.CalendarManager;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.util.EseDateFormat;
 import ch.unibe.ese.calendar.util.EventIteratorUtils;
 
 import play.i18n.Lang;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class CalendarPage extends Controller{
 	
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
 		calendarWithoutSearch(userName, calendarName);
 	}
 	
 	/**
 	 * Renders the calendar page without search results
 	 * @param userName The name of the user whose calendar will be displayed.
 	 * @param calendarName calendarName the name of the calendar which will be displayed
 	 */
 	public static void calendarWithoutSearch(String userName, String calendarName) {
 		calendar(userName, calendarName, null, -1);
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
		Iterator<CalendarEvent> iterator = Collections.EMPTY_LIST.iterator();
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
		iterator = getPage(curPage, iterator);
		render(iterator, calendar, calendarBrowser, myContacts, searchRegex, curPage,
 				connectedUser, selectedDateString, myCalendars, calendarIter);
 	}
 	
 	private static Iterator<CalendarEvent> getPage(int curPage,
 			Iterator<CalendarEvent> iterator) {
 		/*
 		 *	The following chunk works, but could be implemented more elegantly and requiring less memory
 		 */		
 		int skip = curPage*PAGESIZE;
 		int page = PAGESIZE;
 		List<CalendarEvent> tmp = new ArrayList<CalendarEvent>();
 		while (iterator.hasNext()) {
 			CalendarEvent obj = iterator.next();
 			if (skip != 0) {
 				skip--;
 			} else {
 				if (page != 0) {
 					page--;
 					tmp.add(obj);
 					if (tmp.size() > PAGESIZE) {
 						break;
 					}
 				}
 			}
 		}
 		return tmp.iterator();	
 	}
 
 	/**
 	 * @return the client locale guessed from accept-language header
 	 */
 	private static Locale getLocale() {
 		return new Locale(Lang.get(), "CH");
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
 		calendarWithoutSearch(userName, calendarName);
 	}
 	
 	/**
 	 * First sets all Calendars to unselected then sets all Calendars that have 
 	 * their name in checkedCalendars[] to selected
 	 * 
 	 * @param checkedCalendars: all Calendars which's checkbox is selected
 	 */
 	public static void includeCalendars(String[] checkedCalendars, String calendarName) {
 		String userName = Security.connected();
 		CalendarManager calendarManager = CalendarManager.getInstance();
 		UserManager um = UserManager.getInstance();
 		User user = um.getUserByName(userName);
 		calendarManager.unSelectAllCalendars(user);
 		if (checkedCalendars != null) {
 			for (String calendar: checkedCalendars) {
 				EseCalendar eseCalendar = calendarManager.getCalendar(calendar);
 				eseCalendar.select(true);
 			}
		}	
 		calendarWithoutSearch(userName, calendarName);
 	}
 	
 	public static void searchEvent(String userName, String
 		    calendarName, String searchRegex, int curPage) {
 			calendar(userName, calendarName, searchRegex, curPage);
 		}
 }
