 /**
  * @author David S Anderson
  *
  *
  * Copyright (C) 2012 David S Anderson
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.dsanderson.xctrailreport.core;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Scanner;
 
 /**
  * 
  */
 public class ReportDate implements Comparable<ReportDate> {
 	Date date;
 
 	final String monthStrings[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
 			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
 
 	/**
 	 * @brief Creates a new date object with the current date/time
 	 */
 	public ReportDate() {
 		date = new Date();
 	}
 
 	/**
 	 * @brief Creates a new date object based on string of format "MM d"
 	 * @param dateString
 	 */
 	public ReportDate(String dateString) throws Exception {
 		Scanner scanner = new Scanner(dateString);
 		String month;
 		if (scanner.hasNext())
 			month = scanner.next();
		else
 			throw new Exception("Invalid date string: " + dateString);
 
 		int day;
 		if (scanner.hasNextInt())
 			day = scanner.nextInt();
		else
 			throw new Exception("Invalid date string: " + dateString);
 
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(Calendar.MONTH, lookupMonth(month));
 		calendar.set(Calendar.DAY_OF_MONTH, day);
 		// if the date is in the future
 		if (calendar.compareTo(Calendar.getInstance()) > 0) {
 			// its in the previous year
 			calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
 		}
 		// set it to midnight, so it shows up longest in age filter
 		calendar.set(Calendar.HOUR, 11);
 		calendar.set(Calendar.MINUTE, 59);
 		calendar.set(Calendar.SECOND, 59);
 		calendar.set(Calendar.MILLISECOND, 0);
 		date = calendar.getTime();
 	}
 
 	public ReportDate(long timestamp) {
 		date = new Date(timestamp);
 	}
 
 	/**
 	 * @brief Creates a new date object with a given date/time
 	 * @param month
 	 * @param day
 	 * @param year
 	 */
 	public ReportDate(String month, int day, int year) throws Exception {
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(year, lookupMonth(month), day);
 		date = calendar.getTime();
 	}
 	
 	public ReportDate(Date date) {
 		this.date = date;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public String formatDate() {
 		SimpleDateFormat format = new SimpleDateFormat("MMM d");
 		return format.format(date);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo(ReportDate o) {
 		return date.compareTo(o.getDate());
 	}
 
 	private int lookupMonth(String month) throws Exception {
 		Integer monthInt = null;
 		int i = 0;
 		for (String str : monthStrings) {
 			if (str.compareTo(month) == 0) {
 				monthInt = i;
 				break;
 			}
 			i++;
 		}
 
 		if (monthInt == null) {
 			throw new Exception("Invalid month: " + month);
 		}
 		return monthInt;
 	}
 
 }
