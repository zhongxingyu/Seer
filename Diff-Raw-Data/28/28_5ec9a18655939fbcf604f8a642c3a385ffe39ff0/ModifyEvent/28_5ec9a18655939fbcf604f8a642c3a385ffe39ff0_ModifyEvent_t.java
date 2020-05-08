 package controllers;
 
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import ch.unibe.ese.calendar.CalendarEvent;
 import ch.unibe.ese.calendar.CalendarManager;
 import ch.unibe.ese.calendar.EseCalendar;
 import ch.unibe.ese.calendar.User;
 import ch.unibe.ese.calendar.UserManager;
 import ch.unibe.ese.calendar.Visibility;
 import ch.unibe.ese.calendar.EventSeries.Repetition;
 import ch.unibe.ese.calendar.exceptions.EventNotFoundException;
 import ch.unibe.ese.calendar.util.EseDateFormat;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class ModifyEvent extends Controller {
 	
 	public static void createEvent(String calendarName, String name, String startDate, 
 			String dayDuration, String hourDuration, String minDuration, String visibility, 
 			String repetition, String description) throws Throwable {
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 		int duration = calculateDur(dayDuration, hourDuration, minDuration);
 		Date eDate = new Date();
 		eDate.setTime(sDate.getTime()+duration);
 		Visibility vis = Visibility.valueOf(visibility.toUpperCase());
 		//Date eDate = EseDateFormat.getInstance().parse(endDate); //old version
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		if (repetition.equalsIgnoreCase("never")) {
 			calendar.addEvent(user, sDate, eDate, name, vis, description);
 		} else {
 			calendar.addEventSeries(user, sDate, eDate, name, vis, 
 					Repetition.valueOf(repetition.toUpperCase()), description);
 		}
		Application.calendar(userName);
 	}
 	
 	/**
 	 * Calculates the duration in milliseconds.
 	 * @param dayDuration
 	 * @param hourDuration
 	 * @param minDuration
 	 * @return the amount of milliseconds as an <code>Integer</code>.
 	 */
 	private static int calculateDur(String dayDuration, String hourDuration,
 			String minDuration) {
 		if (dayDuration.equals("") || hourDuration.equals("") || minDuration.equals("")) {
 			//TODO: Instead of throwing an exception, assume 0 as value if null
 			throw new IllegalArgumentException();
 		}
 		int minDur = Integer.parseInt(minDuration);
 		int hourDur = Integer.parseInt(hourDuration);
 		int dayDur = Integer.parseInt(dayDuration);
 		int duration = 1000*60*(minDur + 60*hourDur + 1440*dayDur);
 		return duration;
 	}
 
 	/**
 	 * An event is identified by its unique id. For finding it
 	 * easily, we have to know it's startDate.
 	 * 
 	 * @param calendarName
 	 * @param hash hashCode() of the to be deleted event
 	 */
 	public static void deleteEvent(String calendarName, String id,
 			String startDate, boolean isSeries) throws ParseException {
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		CalendarEvent event = calendar.getEventById(user, id);
 		if (isSeries) {
 			render(user, calendar, event);
 		}
 		calendar.removeEvent(user, id);
		Application.calendar(userName);
 	}
 	
 	public static void deleteWholeSeries(String calendarName, String id) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(calendarName);
 		calendar.removeEventSeries(user, id);
		Application.calendar(userName);
 	}
 	
 	/**
 	 * 
 	 * @param calendarName
 	 * @param id The id of a SerialEvent
 	 */
 	public static void deleteSingleSerialEvent(String calendarName, String id) {
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(calendarName);
 		CalendarEvent event = calendar.getEventById(user, id);
 		event.getSeries().addExceptionalInstance(id, null);
		Application.calendar(userName);
 	}
 
 	public static void editEvent(String calendarName, String id, 
 			String startDate, boolean isSeries, String repetition)
 			throws ParseException {
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		Visibility[] visibilities = Visibility.values();
 		Repetition[] repetitions = Repetition.values();
 		try {
 			CalendarEvent event = calendar.getEventById(user, id);
 			long duration = event.getEnd().getTime() - event.getStart().getTime();
 			String[] durations = getDurations(duration);
 			render(calendar, event, durations, visibilities, repetitions);
 		} catch (EventNotFoundException exception) {
 			error(exception.getMessage());
 		}
 	}
 	
 	/**
 	 * Gets an <code>Array</code> of <code>Strings</code>. 
 	 * First Entry has the number of days as a <code>String</code>.
 	 * Second Entry has the number of hours as a <code>String</code>.
 	 * Third Entry has the number of minutes as a <code>String</code>.
 	 * @param duration in milliseconds
 	 * @return an <code>Array</code> of <code>Strings</code>. 
 	 */
 	private static String[] getDurations(long duration) {
 		String[] durations = new String[3];
 		int minDur = (int) (duration / (1000*60));
 		if (minDur % 1440 == 0) {
 			durations[0] = minDur/1440+"";
 			durations[1] = "0";
 			durations[2] = "0";
 		} else {
 			if ((minDur % 1440) % 60 == 0) {
 				durations[0] = minDur/1440+"";
 				durations[1] = (minDur%1440)/60+"";
 				durations[2] = "0";
 			} else {
 				durations[0] = minDur/1440+"";
 				durations[1] = (minDur%1440)/60+"";
 				durations[2] = (minDur % 1440) % 60 +"";
 			}
 		}
 		return durations;
 	}
 
 	public static void saveEditedEvent(String calendarName, String id, String oldStartDate, 
 			String name, String startDate, String dayDuration, String hourDuration, 
 			String minDuration, String visibility, String description, String repetition, 
 			boolean wasSeries) throws ParseException {
 		
 		Date sDate = EseDateFormat.getInstance().parse(startDate);
 		int duration = calculateDur(dayDuration, hourDuration, minDuration);
 		Date eDate = new Date();
 		eDate.setTime(sDate.getTime()+duration);
 		Visibility vis = Visibility.valueOf(visibility.toUpperCase());
 		//Date eDate = EseDateFormat.getInstance().parse(endDate);
 		final EseCalendar calendar = CalendarManager.getInstance().getCalendar(
 				calendarName);
 		String userName = Security.connected();
 		User user = UserManager.getInstance().getUserByName(userName);
 		
 		if (wasSeries) {
 			calendar.removeEventSeries(user, id);
 		} else {
 			calendar.removeEvent(user, id);
 		}
 		if (repetition.equalsIgnoreCase("never")) {
 			calendar.addEvent(user, sDate, eDate, name, vis, 
 					description);
 		} else {
 			calendar.addEventSeries(user, sDate, eDate, name, vis, 
 					Repetition.valueOf(repetition.toUpperCase()), description);
 		}
		Application.calendar(userName);
 	}
 }
