 package com.lge.calendar;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 public class DateFormatUtil {
 
 	/*
 	 * make an array for minMonth <= month <= maxMonth by adding one in order to
 	 * handle intercalary month
 	 */
 	public static String[] getMonthStrings(Calendar date) {
 		if (date instanceof KoreanLunisolarCalendar) {
 			int leapMonth = KoreanLunisolarCalendar.getLeapMonthInYear(date
 					.get(Calendar.YEAR));
 			return leapMonthString[leapMonth];
 		} else if (date instanceof GregorianCalendar) {
 			return leapMonthString[0];
 		} else {
 			ArrayList<String> monthStrings = new ArrayList<String>(13);
 
 			Calendar c = (Calendar) date.clone();
 			int year = c.get(Calendar.YEAR);
 			c.set(Calendar.MONTH, 0);
 			while (c.get(Calendar.YEAR) == year) {
 				monthStrings.add("" + c.get(Calendar.MONTH));
 				c.add(Calendar.MONTH, 1);
 			}
 			return (String[]) monthStrings.toArray(new String[monthStrings
 					.size()]);
 		}
 	}
 
 	static String[][] leapMonthString = new String[13][];
 	static int[] leapMonthYear;
 	static int[] leapMonthMonth;
 	static {
 		leapMonthString[0] = new String[12];
 		for (int i = 0; i < 12; i++) {
 			leapMonthString[0][i] = (i + 1) + "";
 		}
 		for (int leapMonth = 1; leapMonth <= 12; leapMonth++) {
 			leapMonthString[leapMonth] = new String[13];
 			leapMonthString[leapMonth][leapMonth] = leapMonth + "()";
 			for (int m = 0; m < 12; m++) {
 				if (m + 1 <= leapMonth) {
 					leapMonthString[leapMonth][m] = (m + 1) + "";
 				} else {
 					leapMonthString[leapMonth][m + 1] = (m + 1) + "";
 				}
 			}
 		}
 	}
 
 }
