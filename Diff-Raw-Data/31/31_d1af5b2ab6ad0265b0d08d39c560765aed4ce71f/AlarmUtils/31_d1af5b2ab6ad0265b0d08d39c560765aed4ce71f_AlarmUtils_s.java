 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugard, Andreas Rolen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.utils;
 
 import java.util.Calendar;
 
 /**
  * Utility class to help Alarm.
  * 
  * @author Daniel Augurell
  * 
  */
 public final class AlarmUtils {
 	public static final int DAYS_OF_WEEK = 7;
 	public static final int HOUR_OF_DAY = 24;
 	public static final int MINUTES_OF_HOUR = 60;
 
 	/**
 	 * No objects should be created of this class.
 	 */
 	private AlarmUtils() {
 	}
 
 	/**
 	 * Changes an boolean array so the first day is Sunday instead of Monday.
 	 * 
 	 * @param dayOfWeek
 	 *            The days of week which is set to be recurring
 	 * @return The array but with the Sunday as the first day of the week.
 	 */
 	public static boolean[] changeToCalendar(boolean[] dayOfWeek) {
 		boolean[] calendarDays = new boolean[DAYS_OF_WEEK];
 		for (int i = 0; i < calendarDays.length; i++) {
 			if (i == 0) {
 				calendarDays[i] = dayOfWeek[calendarDays.length - 1];
 			} else {
 				calendarDays[i] = dayOfWeek[i - 1];
 			}
 		}
 		return calendarDays;
 	}
 
 	/**
 	 * Generates a boolean array with every value representing a bit in an
 	 * integer. Only the seven last bits is checked. The first index of the
 	 * array represent the lowest bit.
 	 * 
 	 * @param bits
 	 *            Any integer with seven bits
 	 * @return The boolean array
 	 */
 	public static boolean[] getBooleanArray(int bits) {
 		boolean[] days = new boolean[DAYS_OF_WEEK];
 		for (int i = 0; i < DAYS_OF_WEEK; i++) {
 			days[i] = (bits & (1 << i)) > 0;
 		}
 		return days;
 	}
 
 	/**
 	 * Gets the number of minutes left until the next occurrence of a given
 	 * minute from now.
 	 * 
 	 * @param minutes
 	 *            the given minute
 	 * @return The number of minutes left.
 	 */
 	public static int getMinutesToTime(int minutes) {
 		int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
 		int minutesToAlarm = (minutes - currentMinute + AlarmUtils.MINUTES_OF_HOUR)
 				% AlarmUtils.MINUTES_OF_HOUR;
 
 		return minutesToAlarm;
 	}
 
 	/**
 	 * Gets the number of hours left until the next occurrence of a given time
 	 * from now.
 	 * 
 	 * @param hour
 	 *            The given hour
 	 * @param minute
 	 *            The given minute
 	 * @return The number of hours left.
 	 */
 	public static int getHoursToTime(int hour, int minute) {
 		int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
 		int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		int hoursToAlarm = (hour - currentHour + AlarmUtils.HOUR_OF_DAY)
 				% AlarmUtils.HOUR_OF_DAY;
 
 		if (getMinutesToTime(minute) == 0 && hoursToAlarm == 0) {
 			return AlarmUtils.HOUR_OF_DAY;
 		}
 
 		if (currentMinute > minute) {
 			if (hoursToAlarm == 0) {
 				hoursToAlarm = AlarmUtils.HOUR_OF_DAY;
 			}
 			hoursToAlarm--;
 		}
 
 		return hoursToAlarm;
 	}
 
 	/**
 	 * Add days to a given calendar according to an integer representing all
 	 * days of the week.
 	 * 
 	 * @param cal
 	 *            The calendar
 	 * @param allDays
 	 *            Integer that represents every day of a week as a bit.
 	 */
 	public static void addDays(Calendar cal, int allDays) {
 		int currentDay = cal.get(Calendar.DAY_OF_WEEK);
		int nextDay = getDaysToNextDay(currentDay, allDays);
 		if (nextDay < 1) {
 			return;
 		}
 		cal.add(Calendar.DAY_OF_YEAR, nextDay);
 	}
 
 	/**
 	 * Gets the number of days until the given day according to an integer
 	 * representing all days of the week.
 	 * 
 	 * @param currentDay
 	 *            The calendars integer representation of the current day
 	 * @param allDays
 	 *            Integer that represents every day of a week as a bit.
 	 * @return days left
 	 */
 	public static int getDaysToNextDay(int currentDay, int allDays) {
 		boolean[] days = AlarmUtils.changeToCalendar(AlarmUtils
 				.getBooleanArray(allDays));
 		for (int i = 0; i < AlarmUtils.DAYS_OF_WEEK; i++) {
 			if (days[(currentDay + i) % AlarmUtils.DAYS_OF_WEEK]) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	/**
 	 * Returns a integer representation of the days that the alarm should be
 	 * recurring on.
 	 * 
 	 * @param daysOfWeek
 	 *            The boolean array that represents the days that the alarms
 	 *            should be recurring on
 	 * @return The days of the week that should be recurring as bits in a
 	 *         integer
 	 */
 	public static int getIntegerFromBooleanArray(boolean[] daysOfWeek) {
 		StringBuilder days = new StringBuilder();
 		for (int i = daysOfWeek.length - 1; i >= 0; i--) {
 			if (daysOfWeek[i]) {
 				days.append("1");
 			} else {
 				days.append("0");
 			}
 		}
 		return Integer.valueOf(days.toString(), 2);
 	}
 }
