 /*
 * Copyright (c) 2013 Daniël W. Crompton <info+straw@specialbrands.net>
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 /**
  * 
  */
 package net.specialbrands.strawberry;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author danielcrompton
  * 
  */
 public class BusinessHourCalculator {
 	OpeningHours openingHours;
 
 	Map<DayOfWeek, OpeningHours> openDays;
 	Map<String, OpeningHours> holiDays;
 
 	/**
 	 * @param _open
 	 * @param _close
 	 */
 	public BusinessHourCalculator(String _open, String _close) {
 		openingHours = new OpeningHours(_open, _close);
 
 		openDays = new HashMap<DayOfWeek, OpeningHours>();
 		holiDays = new HashMap<String, OpeningHours>();
 
 		closedDates = new HashSet<String>();
 
 		closedDays = new HashSet<Integer>();
 	}
 
 	Logger logger = LoggerFactory.getLogger(getClass());
 
 	private Set<String> closedDates;
 
 	private Set<Integer> closedDays;
 
 	/**
 	 * @param day
 	 * @param open
 	 * @param close
 	 */
 	public void setOpeningHours(DayOfWeek day, String open, String close) {
 		openDays.put(day, new OpeningHours(open, close));
 	}
 
 	/**
 	 * @param dateStr
 	 * @param open
 	 * @param close
 	 */
 	public void setOpeningHours(String dateStr, String open, String close) {
 		holiDays.put(dateStr, new OpeningHours(open, close));
 	}
 
 	/**
 	 * @param day1
 	 * @param day2
 	 */
 	public void setClosed(DayOfWeek day1, DayOfWeek day2) {
 		setClosed(day1);
 		setClosed(day2);
 	}
 
 	/**
 	 * @param day
 	 */
 	public void setClosed(DayOfWeek day) {
 		closedDays.add(day.dow);
 	}
 
 	/**
 	 * @param i
 	 * @param string
 	 * @return
 	 * @throws ParseException
 	 */
 	public Date calculateDeadline(int i, String string) throws ParseException {
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 		Date date = formatter.parse(string);
 
 		boolean ret = false;
 
 		logger.debug("{} <-- start", date.toGMTString());
 
 		long longDate = date.getTime() + Long.valueOf(i * 1000L);
 		Date thisDay = new Date(longDate);
 		int open = open = (new Integer(openingHours.getOpen()[0]) * 60)
 				+ new Integer(openingHours.getOpen()[1]);
 		int closed = (new Integer(openingHours.getClose()[0]) * 60)
 				+ new Integer(openingHours.getClose()[1]);
 		int time = (thisDay.getHours() * 60) + thisDay.getMinutes();
 
 		if (thisDay.getHours() >= new Integer(openingHours.getClose()[0])) {
 			int rest = open + ((24 * 60) - closed);
 
 			longDate += new Long(rest * 60 * 1000);
 			thisDay = new Date(longDate);
 		}
 
 		SimpleDateFormat formatlookup = new SimpleDateFormat("yyyy-MM-dd");
 		String day = formatlookup.format(thisDay);
 
 		logger.debug("Calculated Date: {}", thisDay.toGMTString());
 
 		while (closedDates.contains(day) || holiDays.containsKey(day)
 				|| closedDays.contains(thisDay.getDay())) {
 
 			OpeningHours oh = (OpeningHours) holiDays.get(day);
 			if (oh != null) {
 				longDate = date.getTime() + Long.valueOf(i * 1000L);
 				int starttime = time;
 
 				int openHoli = (new Integer(oh.getOpen()[0]) * 60)
 						+ new Integer(oh.getOpen()[1]);
 				int closedHoli = (new Integer(oh.getClose()[0]) * 60)
 						+ new Integer(oh.getClose()[1]);
 
 				int rest = openHoli + ((24 * 60) - closedHoli);
 
 				longDate += new Long((openHoli + openHoli - time) * 60 * 1000);
 
 				longDate += new Long(rest * 60 * 1000);
 				thisDay = new Date(longDate);
 
 			} else
 				longDate += 86400000L;
 
 			thisDay = new Date(longDate);
 			day = formatlookup.format(thisDay);
 			logger.debug(day + "  {}", thisDay.toGMTString());
 		}
 
 		/*
 		 * logger.debug("DoW: " + thisDay.getDay()); while
 		 * (closedDays.contains(thisDay.getDay())) { logger.debug("IN"); ret =
 		 * true; longDate += 86400000L; thisDay = new Date(longDate);
 		 * logger.debug("day{}", thisDay.toGMTString()); }
 		 */
 
 		return thisDay;
 	}
 
 	/**
 	 * @param string
 	 */
 	public void setClosed(String string) {
 		closedDates.add(string);
 	}
 }
