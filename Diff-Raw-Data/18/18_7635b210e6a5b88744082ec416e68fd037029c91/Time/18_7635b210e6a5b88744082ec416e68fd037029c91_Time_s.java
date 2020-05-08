 package util;
 
 /**
  * A time class which can either take a String or an int as an input.
  * When the int is used the time should be in seconds, whereas when
  * the string is used, it should be on the format "hh.mm.ss".
  * 
  * It can also compare times and convert an int in seconds
  * to a String on the format "hh.mm.ss"-
  * 
  * @author Team08
  * 
  */
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 public class Time implements Comparable {
 
 	private int time;
 
 	/**
 	 * Creates a Time object with seconds as an input parameter.
 	 * 
 	 * @param time
 	 *            the time in seconds
 	 */
 	public Time(int time) {
 		this.time = time;
 	}
 
 	private int time() {
 		return time;
 	}
 
 	/**
 	 * Creates the Time based on a string on the format "hh.mm.ss".
 	 * 
 	 * @param time
 	 *            the time in string on the format (hh.mm.ss)
 	 */
 	public Time(String representTime) {
 
		if (representTime.length() == 0) {
 			time = 0;
 		} else {
 			String[] temp = representTime.split("\\.");
 			time = parseTime(temp);
 
 		}
 	}
 
 	/**
 	 * Sets the time in seconds.
 	 * 
 	 * @param time
 	 *            the time in seconds
 	 */
 
 	public void setTime(int time) {
 		this.time = time;
 	}
 
 	/**
 	 * Sets the time (string).
 	 * 
 	 * @param time
 	 *            the time in string on the format (hh.mm.ss)
 	 */
 
 	public void setTime(String time) {
 		String[] temp = time.split("\\.");
 		this.time = parseTime(temp);
 	}
 
 	/**
 	 * Returns the time.
 	 * 
 	 * @return The time in seconds
 	 */
 	public int getTime() {
 		return time;
 	}
 
 	private int parseTime(String[] temp) throws NumberFormatException {
 		int hour = Integer.parseInt(temp[0]);
 		int minute = Integer.parseInt(temp[1]);
 		int second = Integer.parseInt(temp[2]);
 		if (hour < 0 || minute < 0 || second < 0) {
 			throw new NumberFormatException();
 		}
 		int start = 3600 * hour + 60 * minute + second;
 		return start;
 	}
 
 	/**
 	 * Adds a zero if minutes < 10, otherwise just returns the same number that
 	 * was used as the input parameter.
 	 * 
 	 * E.g. if "9" is used as an input parameter, the method should return "09",
 	 * whereas if the input parameter is "11" the method should return "11".
 	 * 
 	 * That is, no zeroes should be added if minutes > 9 Only positive numbers
 	 * are allowed.
 	 * 
 	 * @param minutes
 	 *            the minutes, adds a
 	 * @throws NumberFormatException
 	 *             If minutes < 0
 	 */
 	public static String addZero(int minutes) throws NumberFormatException {
 		if (minutes < 0)
 			throw new NumberFormatException("No negative numbers are allowed");
 		String min;
 		if (minutes < 10) {
 			min = "0" + minutes;
 		} else {
 			min = Integer.toString(minutes);
 		}
 		return min;
 	}
 
 	public static String totalTimeString(int time) {
 		int minutes = (time % 3600) / 60;
 		String min = addZero(minutes);
 
 		int seconds = (time % 3600) % 60;
 		String sec = addZero(seconds);
 
 		String total = time / 3600 + "." + min + "." + sec;
 		return total;
 	}
 
 	/**
 	 * Determine if the times are the same.
 	 * 
 	 * @param t2
 	 *            the time
 	 * @return true if both time is the same: time - t2.getTime() == 0 , false
 	 *         otherwise
 	 */
 	public boolean equals(Time t2) {
 		if (time - t2.getTime() != 0) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Determine if time is greater than the parameter time.
 	 * 
 	 * @param t2
 	 *            the time
 	 * @return true if this.time is greater than t2: time > t2.getTime() , false
 	 *         otherwise
 	 */
 	public boolean greaterThan(Time t2) {
 		if (!(time > t2.getTime())) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Determine if time is lesser than the parameter time.
 	 * 
 	 * @param t2
 	 *            the time
 	 * @return true if this.time is greater than t2: time < t2.getTime() , false
 	 *         otherwise
 	 */
 	public boolean lesserThan(Time time2) {
 		if (!(time < time2.getTime())) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Convert the time to a string and return the string.
 	 * 
 	 * @return The time in string
 	 */
 	public String toString() {
 		int minutes = (time % 3600) / 60;
 		String min = addZero(minutes);
 
 		int seconds = (time % 3600) % 60;
 		String sec = addZero(seconds);
 
 		String total = time / 3600 + "." + min + "." + sec;
 
 		return total;
 	}
 
 	/**
 	 * Computes the difference between time.
 	 * 
 	 * @param t2
 	 *            the time
 	 * @return The difference between the time
 	 */
 	public int timeDiff(Time t2) {
 		if (time > t2.getTime()) {
 			return time - t2.getTime();
 		} else {
 			return t2.getTime() - time;
 		}
 	}
 
 	/**
 	 * Compares the current object with the object in the input parameter, based
 	 * on seconds.
 	 * 
 	 * @return -1, if this time is smaller, 0 if they are equal, or 1 if this
 	 *         time is bigger
 	 */
 	public int compareTo(Object arg0) {
 		final int SMALLER = -1;
 		final int EQUAL = 0;
 		final int BIGGER = 1;
 
 		if (this.time() == ((Time) arg0).time()) {
 			return EQUAL;
 		}
 		if (this.time() < ((Time) arg0).time()) {
 			return SMALLER;
 		}
 		return BIGGER;
 	}
 
 	public void sortArrayInOrder(List<Time> times) {
 		Collections.sort(times);
 	}
 
 	/**
 	 * Creates a Gregorian Calender object and returns the time as a string
 	 * vector where [0]hours, [1]minutes and [2]seconds
 	 * 
 	 * @return A string vector with the current time, [0] hours, [1] minutes,
 	 *         [2] seconds
 	 * 
 	 */
 	public static String[] makeTimeList() {
 		GregorianCalendar calendar = new GregorianCalendar();
 
 		int hours = calendar.get(Calendar.HOUR_OF_DAY);
 		int minutes = calendar.get(Calendar.MINUTE);
 		int seconds = calendar.get(Calendar.SECOND);
 		String[] times = new String[3];
 		String stringMinutes = Time2.addZero(minutes);
 		String stringSeconds = Time2.addZero(seconds);
 
 		times[0] = Integer.toString(hours);
 		times[1] = stringMinutes;
 		times[2] = stringSeconds;
 
 		return times;
 	}
 	
 	public Time multiplyTimeBy(int factor, Time time) {
 		return new Time(factor*time.time);
 	}
 	
 	public Time addToTime(Time time) {
 		return new Time(this.time + time.time);
 	}
 
 }
