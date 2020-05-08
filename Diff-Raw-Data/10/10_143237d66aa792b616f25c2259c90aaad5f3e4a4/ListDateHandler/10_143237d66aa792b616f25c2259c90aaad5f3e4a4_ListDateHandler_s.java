 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 Linus Karlsson
  * 
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.plingnote;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Class customizing the the way a note's date is displayed in the list.
  * 
  * @author Linus Karlsson
  * 
  */
 public class ListDateHandler {
 
 	/**
 	 * Changes the way the date is displayed.
 	 * 
 	 * If the note is written on current day, the date's displayed as (hh:mm).
 	 * If it's written in the same week, but not on the specific day, the date
 	 * will be displayed as a weekday (monday, tuesday...). Older notes are
 	 * displayed as (yyyy-mm-dd).
 	 * 
 	 * @return the format of the date
 	 */
 	public static String customDateFormat(String date) {
 
 		SimpleDateFormat dateFormat = new SimpleDateFormat(
 				"yyyy-MM-dd HH:mm:ss");
 
 		Date noteDate = new Date();
 		Date currentDate = new Date();
 
 		// Initiate the string with "null" if the try-catch statement fails
 		String dateText = null;
 
 		Calendar calendar = Calendar.getInstance();
 
 		try {
 			// Convert String to Date for easier manipulation.
 			noteDate = dateFormat.parse(date);
 			calendar.setTime(noteDate);
 
 			// If note is created on current date
 			if (isCreatedThisDay(currentDate, noteDate)) {
				dateText = calendar.get(Calendar.HOUR_OF_DAY) + ":"
						+ calendar.get(Calendar.MINUTE);
 			}
 
 			// If note is created this week
 			else if (isCreatedThisWeek(currentDate, noteDate)) {
 				dateText = getDayOfWeek(noteDate);
 			}
 
 			// If none of the previous statements were true.
 			else {
				dateText = calendar.get(Calendar.YEAR) + "-"
						+ calendar.get(Calendar.MONTH) + "-"
						+ calendar.get(Calendar.DAY_OF_MONTH);
 			}
 
 		} catch (java.text.ParseException e) {
 			e.printStackTrace();
 		}
 
 		return dateText;
 	}
 
 	/**
 	 * Checks if note is created on current day
 	 * 
 	 * @param currentDate
 	 *            the current date
 	 * @param dateOfCreation
 	 *            the date of when a note was created
 	 * @param dateFormat
 	 *            the date format
 	 * @return true if the note is created on the current day.
 	 */
 	public static boolean isCreatedThisDay(Date currentDate, Date noteDate) {
 
 		// Change date format to exclude time
 		SimpleDateFormat temp = new SimpleDateFormat("yyyyMMdd");
 		return temp.format(currentDate).equals(temp.format(noteDate));
 	}
 
 	/**
 	 * Checks if the note is created in current week
 	 * 
 	 * @param currentDate
 	 *            the current date
 	 * @param dateOfCreation
 	 *            the date of when a note was created
 	 * @return true if notes is created in the current week.
 	 */
 	public static boolean isCreatedThisWeek(Date currentDate, Date noteDate) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setFirstDayOfWeek(Calendar.MONDAY);
 
 		// Get current week
 		calendar.setTime(currentDate);
 		int currentWeek = calendar.get(Calendar.WEEK_OF_MONTH);
 		int currentYear = calendar.get(Calendar.YEAR);
 
 		// Get week when note was created
 		calendar.setTime(noteDate);
 		int weekOfCreation = calendar.get(Calendar.WEEK_OF_MONTH);
 		int yearOfCreation = calendar.get(Calendar.YEAR);
 
 		return (currentWeek == weekOfCreation) && (currentYear == yearOfCreation);
 	}
 
 	/**
 	 * Checks which day of the week a note was created.
 	 * 
 	 * @param dateOfCreation
 	 *            the date of when a note was created
 	 * @return which weekday the note was created.
 	 */
 	public static String getDayOfWeek(Date noteDate) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(noteDate);
 
 		// Since week starts on 1 and array on 0, we have to subtract 1.
 		return getWeekdays()[calendar.get(Calendar.DAY_OF_WEEK) - 1];
 
 	}
 
 	/**
 	 * Array containing all weekdays
 	 * 
 	 * @return the weekdays in an array
 	 */
 	public static String[] getWeekdays() {
 		return new String[] { "sunday", "monday", "tuesday", "wednesday", "thursday",
 				"friday", "saturday" };
 	}
 }
