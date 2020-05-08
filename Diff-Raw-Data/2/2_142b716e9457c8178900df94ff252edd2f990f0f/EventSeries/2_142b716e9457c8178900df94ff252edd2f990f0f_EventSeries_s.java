 package ch.unibe.ese.calendar;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 public class EventSeries extends CalendarEntry {
 
 	public static enum Repetition {
 		DAILY, WEEKLY, MONTHLY
 	}
 
 	private Repetition repetition;
 
 	EventSeries(Date start, Date end, String name, Visibility visibility, 
 			Repetition repetition, EseCalendar calendar, String description) {
 		super(start, end, name, visibility, calendar, description);
 		this.repetition = repetition;
 	}
 
 
 	public Repetition getRepetition() {
 		return repetition;
 	}
 
 	/**
 	 * 
 	 * @param start the date from which on instances of the series are to be returned
 	 * @returnan iterator over the evnet instances
 	 */
 	public Iterator<CalendarEvent> iterator(Date start) {
 		Iterator<CalendarEvent> result = new DayMergingIterator(start);
 		return result;
 
 	}
 
 	
 	/**
 	 * note that the caller is responsible to check if the date matches this series
 	 * 
 	 * @dayStart Start of the day we want to get a SerialEvent of
 	 * @return 	A single instance of this event (of the type SerialEvent) with the 
 	 * 			parameters this series is defined by.
 	 */
 	private SerialEvent getAsSerialEventForDay(Date dayStart) {
 		java.util.Calendar jucDayStart = java.util.Calendar.getInstance(new Locale(
 				"de", "CH"));
 		java.util.Calendar jucEventStart = java.util.Calendar.getInstance(new Locale(
 				"de", "CH"));
 		java.util.Calendar jucEventEnd = java.util.Calendar.getInstance(new Locale(
 				"de", "CH"));
 		jucEventEnd.setTime(getEnd());
 		jucEventStart.setTime(getStart());
 		jucDayStart.setTime(dayStart);
 		int year = jucDayStart.get(Calendar.YEAR);
 		int month = jucDayStart.get(Calendar.MONTH);
 		int day = jucDayStart.get(Calendar.DAY_OF_MONTH);
 		
 		int hour = jucEventStart.get(Calendar.HOUR_OF_DAY);
 		int min = jucEventStart.get(Calendar.MINUTE);
 		jucDayStart.set(year, month, day, hour, min);
 		Date start = jucDayStart.getTime();
 		
 		int durDay = jucEventEnd.get(Calendar.DAY_OF_YEAR)-jucEventStart.get(Calendar.DAY_OF_YEAR);
 		int durMonth = jucEventEnd.get(Calendar.MONTH)-jucEventStart.get(Calendar.MONTH);
 		int durYear = jucEventEnd.get(Calendar.YEAR)-jucEventStart.get(Calendar.YEAR);
 		int hour2 = jucEventEnd.get(Calendar.HOUR_OF_DAY);
 		int min2 = jucEventEnd.get(Calendar.MINUTE);
 		jucEventEnd.set(year+durYear, month + durMonth, day + durDay, hour2, min2);
 		Date end = jucEventEnd.getTime();
 		SerialEvent se = new SerialEvent(start, end, getName(), getVisibility(), 
 				this, getCalendar(), getDescription());
 		return se;
 	}
 
 	/**
 	 * 
 	 * @dayStart Start of the day we want to get a SerialEvent of
 	 * @return
 	 */
 	private boolean dateMatches(Date dayStart) {
 		Repetition repetition = getRepetition();
 		java.util.Calendar juc1 = java.util.Calendar.getInstance(new Locale(
 				"de", "CH"));
 		java.util.Calendar juc3 = java.util.Calendar.getInstance(new Locale(
 				"de", "CH"));
 		java.util.Calendar juc2 = java.util.Calendar.getInstance(new Locale(
 				"de", "CH"));
 		juc2.setTime(dayStart);
 		int weekDayOfDate = juc2.get(Calendar.DAY_OF_WEEK);
 		int monthDayOfDate = juc2.get(Calendar.DAY_OF_MONTH);
 		juc1.setTime(getStart());
 		juc3.setTime(getEnd());
 		int maxDur = juc3.get(Calendar.DAY_OF_YEAR) - juc1.get(Calendar.DAY_OF_YEAR);
 		System.out.println(maxDur);
 		boolean match = false;
 		for (int dur = 0; dur <= maxDur; dur++) {
 			int weekDayOfEventSerie = juc1.get(Calendar.DAY_OF_WEEK) + dur;
 			int monthDayOfEventSerie = juc1.get(Calendar.DAY_OF_MONTH) + dur;
 			System.out.println ("I'm going to  bc: " + dur);
 			if (repetition.equals(repetition.DAILY)) {
 				match = true;
 			}
 			if (repetition.equals(repetition.WEEKLY)) {
 				if(weekDayOfEventSerie == weekDayOfDate) match = true;
 			}
 			if (repetition.equals(repetition.MONTHLY)) {
 				if(monthDayOfEventSerie == monthDayOfDate) match = true;
 			}
 		}
 		System.out.println("returning false");
 		return match;
 	}
 	
 
 	/**
 	 * Help! what does this thing do?
 	 *
 	 */
 	private class DayMergingIterator implements Iterator<CalendarEvent> {
 
 		private Date currentDate;
 		
 		public DayMergingIterator(Date start) {
 			currentDate = start;
 		}
 
 		/**
 		 * @return true as a Series lasts forever
 		 */
 		@Override
 		public boolean hasNext() {
 			return true;
 		}
 
 		@Override
 		public CalendarEvent next() {
 			while (!dateMatches(currentDate)) {
 				currentDate = nextDay();
 			}
 			CalendarEvent result = getAsSerialEventForDay(currentDate);
 			currentDate = nextDay();
 			return result;
 		}
 
 		private Date nextDay() {
 			//TODO use calendar to take dlst into account
 			return new Date(currentDate.getTime()+24*60*60*1000);
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 
 	}
 
 }
