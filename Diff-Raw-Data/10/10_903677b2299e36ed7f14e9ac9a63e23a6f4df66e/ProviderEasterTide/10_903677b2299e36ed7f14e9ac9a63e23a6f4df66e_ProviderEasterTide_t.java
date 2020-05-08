 /*
  * Copyright (c) 2011. Uli Fuchs <ufuchs@gmx.com>
  * Released under the terms of the GNU GPL v2.0.
  */
 
 package de.z35.commons.event.provider;
 
 import java.util.Calendar;
 import java.util.Vector;
 
 import de.z35.commons.event.Event;
 import de.z35.commons.event.EventImpl;
 import de.z35.commons.event.Utils;
 
 /**
  * Created by IntelliJ IDEA.
  * User: fuchs
  * Date: 07.07.11
  * Time: 20:09
  * To change this template use File | Settings | File Templates.
  */
 public class ProviderEasterTide {
 
 	/**
 	 * Easter dates from 2010 to 2030
 	 * 
 	 * </p>An astronomical calculation is out of scope.
 	 * </br>We use a table.
 	 * 
 	 * </p>[Citation : http://en.wikipedia.org/wiki/Easter]
 	 * 
 	 * </br>"Easter is a moveable feast, meaning it is not fixed in relation to  
 	 * the civil calendar. 
 	 * </br>The First Council of Nicaea (325) established the date of Easter  
 	 * as the first Sunday after the full moon (the Paschal Full Moon)  
 	 * following the northern hemisphere's vernal equinox.[3] 
 	 * </br>Ecclesiastically, the equinox is reckoned to be on March 21 (even 
 	 * though the equinox occurs, astronomically speaking, on March 20 in most 
 	 * years), and the "Full Moon" is not necessarily the astronomically 
 	 * correct date. 
 	 * </br>The date of Easter therefore varies between March 22 and April 25. 
 	 * </br>Eastern Christianity bases its calculations on the Julian Calendar 
 	 * whose March 21 corresponds, during the 21st century, to the 3rd of 
 	 * April in the Gregorian Calendar, in which calendar their celebration of 
 	 * Easter therefore varies between April 4 and May 8."  
 	 * 
 	 */
 	private static String[] EASTER = {
 
 		"2000-04-23",
 		"2001-04.15",
 		"2002-03-31",
 		"2003-04-20",
 		"2004-04-11",
		"2005-03-27",
 		"2006-04-16",
 		"2007-04-08",
 		"2008-03-23",
 		"2009-04-12",
 		    
 		"2010-04-04",
 		"2011-04-24",
 		"2012-04-08",
 		"2013-03-31",
 		"2014-04-20",
 		"2015-04-05",
 		"2016-03-27",
 		"2017-04-16",
 		"2018-04-01",
 		"2019-04-21",
 		
 		"2020-04-12",
 		"2021-04-04",
 		"2022-04-17",
 		"2023-04-09",
 		"2024-03-31",
 		"2025-04-20",
 		"2026-04-05",
 		"2027-03-28",
 		"2028-04-16",
 		"2029-04-01",
 		"2030-04-21"
 		
 	};
 
 	/**
 	 *  [Citation : http://en.wikipedia.org/wiki/Eastertide]
 	 *  
 	 *  </br>"Eastertide, or the Easter Season, or Paschal Time, is the period 
 	 *  of fifty days from Easter Sunday to Pentecost Sunday"  
  	 */
 	public static enum EasterTide {
 		
 		EASTER_SUNDAY(1),  // the tide starts with day one on Easter
 		EASTER_MONDAY(2),
 		ASCENSION_DAY(40),
 		PENTECOST_SUNDAY(50),
 		PENTECOST_MONDAY(51);
 
 		private int dayNum;
 
 		EasterTide(int dayNum) {
 			this.dayNum = dayNum;
 		}
 
 		public int getDayNum() { return this.dayNum; }
 
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	private String nameOfDay(EasterTide dayOfTide) {
 		
 		String result = null;
 		
 		String[] parts = dayOfTide.name().split("_");
 		
 		for (String part : parts) {
 			
 			String capitalized = part.substring(0,1) 
 				+ part.substring(1).toLowerCase() + " ";
 			
 			if (result == null) {
 				result = capitalized;
 			} else {
 				result += capitalized;
 			}
 			
 		}
 		
 		return result.trim(); 
 	}
 	
 	/**
 	 *
 	 * @param year
 	 * @return
 	 */
 	public Vector<Event> getEasterTide(final Calendar year) {
 
 		Vector<Event> easterTide = new Vector<Event>();
 		
 		Calendar easter = getEaster(year.get(Calendar.YEAR));
 		
 		// save current Easter date
 		int yy = easter.get(Calendar.YEAR);
 		int mm = easter.get(Calendar.MONTH);
 		int dd = easter.get(Calendar.DAY_OF_MONTH);
 
 		for (EasterTide dayOfTide : EasterTide.values()) {
 			
 			Calendar day = getDayOfTide(easter, dayOfTide, yy, mm, dd);
 
			Event e = EventImpl.createEvent(dayOfTide.ordinal(), nameOfDay(dayOfTide), day);
 			
 			easterTide.add(e);
 			
 		}
 
 		return easterTide;
 
 	}
 
 	/**
 	 *
 	 */
 	public static Calendar getEaster(final int year) throws IllegalArgumentException {
 
 		// Protection
		if ((year < 2000) && (year > 2030)) {
 			throw new IllegalArgumentException();
 		}
 		
 		String yy = ((Integer) year).toString();
 
 		for (String other : EASTER) {
 
 			if (yy.equals(other.substring(0, 4))) {
 
 				return Utils.dateToCalendar(other);
 				
 			}
 
 		}
 
 		return Utils.dateToCalendar("0000-01-01");
 		
 	}
 
 	/**
 	 * 
 	 * @param dayOfTide
 	 * @param yy
 	 * @param mm
 	 * @param dd
 	 * @return
 	 */
 	private Calendar getDayOfTide(Calendar easter, EasterTide dayOfTide, int yy, int mm, int dd) {
 
 		// Easter is the _first_ day in the Eastertide.
 		// To calc correctly there is a subtraction of one day necessary.
 		easter.roll(Calendar.DAY_OF_YEAR, dayOfTide.getDayNum() - 1);
 		
 		Calendar day = (Calendar) easter.clone();
 		
 		// reset to Easter date
 		easter.set(yy, mm, dd);
 		
 		return day;
 		
 	}
 
 }
