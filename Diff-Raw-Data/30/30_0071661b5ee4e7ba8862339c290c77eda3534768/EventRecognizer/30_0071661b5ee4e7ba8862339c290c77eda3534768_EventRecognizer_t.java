 package edu.usc.vakacalendar.commons;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public class EventRecognizer {
 	private final int TITLE_SIZE = 25;
 	private final String AUDIO_URL = "https://dl.dropbox.com/s/n5u4h3qrx1s2rj5/dd.mp3?dl=1";
 	private final String PLACE = "LA";
 	private class ParseFailedExcepion extends Exception {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 	}
 
 	private Integer parseNumber(Integer min, Integer max, String token)
 			throws ParseFailedExcepion {
 		Integer number = null;
 		
 		if ("first".compareToIgnoreCase(token) == 0)
 			number = 1;
 		if ("second".compareToIgnoreCase(token) == 0)
 			number = 2;
 		if ("third".compareToIgnoreCase(token) == 0)
 			number = 3;
 		if ("forth".compareToIgnoreCase(token) == 0)
 			number = 4;
 		if ("fifth".compareToIgnoreCase(token) == 0)
 			number = 5;
 		if ("sixth".compareToIgnoreCase(token) == 0)
 			number = 6;
 		if ("seventh".compareToIgnoreCase(token) == 0)
 			number = 7;
 		if ("eighth".compareToIgnoreCase(token) == 0)
 			number = 8;
 		if ("nineth".compareToIgnoreCase(token) == 0)
 			number = 9;
 		
 		if (number != null){
 			if ((number <= max) && (number >= min)) {
 				return number;
 			}
 		}
 		
 		StringBuffer buffer = new StringBuffer();
		char[] chars = new char[token.length()];
		token.getChars(0, token.length(), chars, 0);
		for (char ch : chars) {
 			if (Character.isDigit(ch))
 			{
				buffer.append(Character.toString(ch));
 			}
 		}		
 		
 		try {
			number = Integer.parseInt(buffer.toString());
 		} catch (NumberFormatException e) {
 			throw new ParseFailedExcepion();
 		}
 		if ((number <= max) && (number >= min)) {
 			return number;
 		} else {
 			throw new ParseFailedExcepion();
 		}
 	}
 
 	private Integer parseMonth(String token) throws ParseFailedExcepion {
 		if ("January".compareToIgnoreCase(token) == 0)
 			return Calendar.JANUARY;
 		if ("February".compareToIgnoreCase(token) == 0)
 			return Calendar.FEBRUARY;
 		if ("March".compareToIgnoreCase(token) == 0)
 			return Calendar.MARCH;
 		if ("April".compareToIgnoreCase(token) == 0)
 			return Calendar.APRIL;
 		if ("May".compareToIgnoreCase(token) == 0)
 			return Calendar.MAY;
 		if ("June".compareToIgnoreCase(token) == 0)
 			return Calendar.JUNE;
 		if ("July".compareToIgnoreCase(token) == 0)
 			return Calendar.JULY;
 		if ("August".compareToIgnoreCase(token) == 0)
 			return Calendar.AUGUST;
 		if ("September".compareToIgnoreCase(token) == 0)
 			return Calendar.SEPTEMBER;
 		if ("October".compareToIgnoreCase(token) == 0)
 			return Calendar.OCTOBER;
 		if ("November".compareToIgnoreCase(token) == 0)
 			return Calendar.NOVEMBER;
 		if ("December".compareToIgnoreCase(token) == 0)
 			return Calendar.DECEMBER;
 		throw new ParseFailedExcepion();
 	}
 
 	private Integer parseWeekDay(String token) throws ParseFailedExcepion {
 		if ("MONDAY".compareToIgnoreCase(token) == 0)
 			return Calendar.MONDAY;
 		if ("TUESDAY".compareToIgnoreCase(token) == 0)
 			return Calendar.TUESDAY;
 		if ("WEDNESDAY".compareToIgnoreCase(token) == 0)
 			return Calendar.WEDNESDAY;
 		if ("THURSDAY".compareToIgnoreCase(token) == 0)
 			return Calendar.THURSDAY;
 		if ("FRIDAY".compareToIgnoreCase(token) == 0)
 			return Calendar.FRIDAY;
 		if ("SATURDAY".compareToIgnoreCase(token) == 0)
 			return Calendar.SATURDAY;
 		if ("SUNDAY".compareToIgnoreCase(token) == 0)
 			return Calendar.SUNDAY;
 		throw new ParseFailedExcepion();
 	}
 
 	private String[] getNextNWords(String[] array, Integer n) {
 		if (array.length > n) {
 			String[] outputArray = new String[n];
 			for (int i = 0; i < n; i++) {
 				outputArray[i] = array[i];
 			}
 			return outputArray;
 		} else {
 			return array;
 		}
 	}
 
 	private Boolean checkTemplate(String[] array, Integer startPositoin,
 			String... pattern) {
 		int len = array.length - startPositoin;
 		if (pattern.length <= len) {
 			int index = startPositoin;
 			for (String patternItem : pattern) {
 				if (patternItem.compareToIgnoreCase(array[index++]) != 0) {
 					return false;
 				}
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private Date findFromTime(String[] array, Date fromDate)
 			throws ParseFailedExcepion {
 		Calendar c = Calendar.getInstance();
 		c.setTime(fromDate);
 		c.set(Calendar.MINUTE, 0);
 		for (int i = 0; i < array.length; i++) {
 			if (checkTemplate(array, i, "at")) {
 				int nextItemIndex = i + 1;
 				if (nextItemIndex + 1 < array.length) {
 					// at <# 1-12> am/pm
 					try {
 						Integer hour = parseNumber(1, 12, array[nextItemIndex]);
 						c.set(Calendar.HOUR, hour);
 						if (checkTemplate(array, nextItemIndex + 2, "a.m."))
 							c.set(Calendar.AM_PM, Calendar.AM);
 						else
 							c.set(Calendar.AM_PM, Calendar.PM);
 						return c.getTime();
 					} catch (ParseFailedExcepion e) {
 					}
 				}
 
 			}
 		}
 		throw new ParseFailedExcepion();
 	}
 
 	private Date findFromDate(String[] array) throws ParseFailedExcepion {
 		Calendar c = Calendar.getInstance();
 		for (int i = 0; i < array.length; i++) {
 			int nextItemIndex = i + 1;
 			if ((nextItemIndex < array.length)
 					&& checkTemplate(array, i, "next")) {
 				// next <weekDay>
 				try {
 					// FIXME: check
 					Integer weekDay = parseWeekDay(array[nextItemIndex]);
 					c.set(Calendar.DAY_OF_WEEK, weekDay);
 					return c.getTime();
 				} catch (ParseFailedExcepion e) {
 				}
 			}
 			// on ...
 			if (checkTemplate(array, i, "on")) {
 				if ((nextItemIndex + 1 < array.length)
 						&& checkTemplate(array, nextItemIndex, "next")) {
 					// next <weekDay>
 					try {
 						// FIXME: check
 						Integer weekDay = parseWeekDay(array[nextItemIndex + 1]);
 						c.set(Calendar.DAY_OF_WEEK, weekDay);
 						return c.getTime();
 					} catch (ParseFailedExcepion e) {
 					}
 				}
 				if (nextItemIndex < array.length) {
 					// <weekDay>
 					try {
 						// FIXME: check
 						Integer weekDay = parseWeekDay(array[nextItemIndex]);
 						c.set(Calendar.DAY_OF_WEEK, weekDay);
 						return c.getTime();
 					} catch (ParseFailedExcepion e) {
 					}
 				}
 				if (nextItemIndex + 1 < array.length) {
 					// <month> <# 1-31>
 					try {
 						Integer month = parseMonth(array[nextItemIndex]);
 						Integer day = parseNumber(1, 31,
 								array[nextItemIndex + 1]);
 						c.set(Calendar.MONTH, month);
 						c.set(Calendar.DAY_OF_MONTH, day);
 						return c.getTime();
 					} catch (ParseFailedExcepion e) {
 					}
 					// <# 1-31> <month>
 					try {
 						Integer month = parseMonth(array[nextItemIndex + 1]);
 						Integer day = parseNumber(1, 31, array[nextItemIndex]);
 						c.set(Calendar.MONTH, month);
 						c.set(Calendar.DAY_OF_MONTH, day);
 						return c.getTime();
 					} catch (ParseFailedExcepion e) {
 					}
 				}
 				if (nextItemIndex + 2 < array.length) {
 					// <# 1-31> of <month>
 					try {
 						Integer month = parseMonth(array[nextItemIndex + 2]);
 						Integer day = parseNumber(1, 31, array[nextItemIndex]);
 						c.set(Calendar.MONTH, month);
 						c.set(Calendar.DAY_OF_MONTH, day);
 						return c.getTime();
 					} catch (ParseFailedExcepion e) {
 					}
 				}
 			}
 			if (checkTemplate(array, i, "tomorrow")) {
 				c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
 				return c.getTime();
 			}
 			if (checkTemplate(array, i, "day", "after", "tomorrow")) {
 				c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 2);
 				return c.getTime();
 			}
 			if (checkTemplate(array, i, "date")) {
 				if (nextItemIndex + 1 < array.length) {
 					try {
 						Integer month = parseMonth(array[nextItemIndex]);
 						Integer day = parseNumber(1, 31,
 								array[nextItemIndex + 1]);
 						c.set(Calendar.MONTH, month);
 						c.set(Calendar.DAY_OF_MONTH, day);
 						return c.getTime();
 					} catch (ParseFailedExcepion e) {
 					}
 				}
 			}
 		}
 		throw new ParseFailedExcepion();
 	}
 
 	public BasicEvent recognize(String inputText) {
 		BasicEvent event = new BasicEvent();
 		String[] tokens = inputText.split("\\s");
 		Calendar c = Calendar.getInstance();
 
 		Date fromDate;
 		try {
 			fromDate = findFromDate(tokens);
 		} catch (ParseFailedExcepion e) {
 			fromDate = c.getTime();
 		}
 		event.setFrom(fromDate);
 		event.setTo(null);
 		setTruncatedTitle(event, inputText);
 		event.setDescription(inputText);
 		event.setPlace(this.PLACE);
 		event.setType(BasicEvent.AUDIO);
 		event.setMetadata(c.getTime());
 		event.setMediaURL(this.AUDIO_URL);
 		return event;
 	}
 
 	private void setTruncatedTitle(BasicEvent event, String title) {
 		if (title.length() > this.TITLE_SIZE) {
 			StringBuffer strBuff = new StringBuffer(title);
 			strBuff.delete(this.TITLE_SIZE, strBuff.length()).append("...");
 			event.setTitle(strBuff.toString());
 			return;
 		} 
 		event.setTitle(title);
 	}
 }
