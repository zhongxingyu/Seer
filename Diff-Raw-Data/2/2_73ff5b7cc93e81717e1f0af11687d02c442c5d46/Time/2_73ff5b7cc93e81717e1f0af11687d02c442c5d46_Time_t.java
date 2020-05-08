 package members;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  * @author Andrée & Henrik
  * 
  */
 public class Time implements Comparable<Time> {
 	// private static final SimpleDateFormat BIG_FORMAT = new SimpleDateFormat(
 	// "d/MM/yy HH.mm.ss.ms z");
 	private static final SimpleDateFormat FORMAT = new SimpleDateFormat(
			"HH.mm.ss");
 
 	private static final int[] VALID_FIELDS = { Calendar.DAY_OF_YEAR,
 			Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND };
 
 	/**
 	 * @return Default GregorianCalendar
 	 */
 	public static Calendar defaultCalendar() {
 		return new GregorianCalendar(1970, Calendar.JANUARY, 1, 00, 00, 00);
 	}
 
 	private Calendar cal;
 
 	/**
 	 * Create time from current system time.
 	 * 
 	 * @return Time
 	 */
 	public static Time fromCurrentTime() {
 		Time t = new Time();
 		t.cal = Calendar.getInstance();
 
 		return t;
 	}
 
 	/**
 	 * @param str
 	 *            "HH.mm.ss"
 	 * @return Time
 	 * @see #parse(int, String)
 	 */
 	public static Time parse(String str) {
 		return parse(0, str);
 	}
 
 	/**
 	 * @param dayOfYear
 	 *            Day of year
 	 * @param str
 	 *            "HH.mm.ss"
 	 * @return Time
 	 */
 	public static Time parse(int dayOfYear, String str) {
 		try {
 			Calendar cal = defaultCalendar();
 			cal.setTime(FORMAT.parse(str));
 			cal.set(Calendar.DAY_OF_YEAR, dayOfYear);
 			cal.set(Calendar.MILLISECOND, 0);
 			return new Time(cal);
 		} catch (ParseException e) {
 			return new NullTime();
 		}
 	}
 
 	/**
 	 * Calculates t - this and returns the difference as a new Time object. If
 	 * the difference is less than 0, one day is added to the time. May happen
 	 * when start and finish time are in different days.
 	 * 
 	 * @param t
 	 *            The later time
 	 * @return Difference. A new Time object.
 	 * 
 	 */
 	public Time difference(Time t) {
 		long diff = t.cal.getTime().getTime() - cal.getTime().getTime();
 
 		return new Time((int) diff / 1000);
 	}
 
 	/**
 	 * Returns a string in the following format: hh.mm.ss
 	 * 
 	 * @return a string with the time
 	 */
 	@Override
 	public String toString() {
 		return FORMAT.format(cal.getTime());
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Time other = (Time) obj;
 		return equalDates(other.cal);
 	}
 
 	private boolean equalDates(Calendar cal2) {
 		for (int i : VALID_FIELDS)
 			if (cal.get(i) != cal2.get(i))
 				return false;
 
 		return true;
 	}
 
 	/**
 	 * Time with defaultCalendar
 	 * 
 	 * @see #defaultCalendar()
 	 */
 	public Time() {
 		cal = defaultCalendar();
 	}
 
 	/**
 	 * Time
 	 * 
 	 * @param seconds
 	 *            Seconds since EPOCH
 	 */
 	public Time(int seconds) {
 		this();
 
 		int left = 0;
 		int ss = 0;
 		int mm = 0;
 		int hh = 0;
 		int dd = 0;
 		left = seconds;
 		ss = left % 60;
 		left = left / 60;
 		mm = left % 60;
 		left = left / 60;
 		hh = left % 24;
 		left = left / 24;
 		dd = left;
 
 		cal.set(Calendar.YEAR, 1970);
 		cal.set(Calendar.DAY_OF_YEAR, dd);
 		cal.set(Calendar.HOUR, hh);
 		cal.set(Calendar.MINUTE, mm);
 		cal.set(Calendar.SECOND, ss);
 		cal.set(Calendar.MILLISECOND, 0);
 	}
 
 	/**
 	 * Generates a time from a new long
 	 * 
 	 * @param seconds
 	 */
 	public Time(Calendar cal) {
 		this.cal = cal;
 	}
 
 	@Override
 	public int compareTo(Time time) {
 		if (time instanceof NullTime)
 			return -1;
 
 		return (int) (cal.getTime().getTime() - time.cal.getTime().getTime());
 	}
 
 	/**
 	 * Add the time to this time.
 	 * 
 	 * @param time
 	 *            the time to add
 	 */
 	public Time add(Time time) {
 		for (int i : VALID_FIELDS)
 			cal.add(i, time.cal.get(i));
 
 		return this;
 	}
 
 	/**
 	 * Returns a clone of this object.
 	 */
 	public Time clone() {
 		Time t = new Time();
 		t.cal = (Calendar) cal.clone();
 		return t;
 
 	}
 
 	public boolean isNull() {
 		return false;
 	}
 }
