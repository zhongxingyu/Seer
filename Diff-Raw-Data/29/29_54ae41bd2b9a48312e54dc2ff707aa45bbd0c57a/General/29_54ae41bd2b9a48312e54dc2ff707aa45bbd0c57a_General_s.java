 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 /**
  * 
  */
 package com.andune.minecraft.commonlib;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import com.andune.minecraft.commonlib.i18n.Locale;
 
 /**
  * @author morganm
  *
  */
 @Singleton
 public class General {
     private static final Logger log = LoggerFactory.getLogger(General.class);
 	
 	private final Map<String, String> timeLongHand = new HashMap<String, String>();
 	private final Map<String, String> timeShortHand = new HashMap<String, String>();
 	private final Locale locale;
 
 	@Inject
     private General(Locale locale) {
 	    this.locale = locale;
        setupTimeLocaleStrings();
     }
 
     /** Given time input, such as of the form "1d" "1w 2d 3h", this will return
      * the number of milliseconds that time format equals. For example, "1d" is
      * 86400 seconds, so this method would return 86400000.
      * 
      * @param input
      * @return
      */
     public long parseTimeInput(final String input) throws NumberFormatException {
         log.debug("parseTimeInput() input={}", input);
     	long time = 0;
 
     	String[] args = input.split(" ");
     	for(int i=0; i < args.length; i++) {
     		long multiplier = 1000;	// milliseconds multiplier
     		int index = -1;
     		
             log.debug("parseTimeInput() parsing arg {}, value={}", i, args[i]);
             if( (index = args[i].indexOf(timeShortHand.get("y"))) != -1 ) {             // year
                 multiplier *= 86400 * 365;
             }
             else if( (index = args[i].indexOf(timeShortHand.get("mo"))) != -1 ) {		// month
     			multiplier *= 86400 * 31;
     		}
     		else if( (index = args[i].indexOf(timeShortHand.get("w"))) != -1 ) {		// week
     			multiplier *= 86400 * 7;
     		}
     		else if( (index = args[i].indexOf(timeShortHand.get("d"))) != -1 ) {		// day
     			multiplier *= 86400;
     		}
     		else if( (index = args[i].indexOf(timeShortHand.get("h"))) != -1 ) {		// hours
     			multiplier *= 3600;
     		}
     		else if( (index = args[i].indexOf(timeShortHand.get("m"))) != -1 ) {		// minutes
     			multiplier *= 60;
     		}
     		
 			String value = args[i].substring(0, index);
 			log.debug("parseTimeInput: value={}, multiplier={}", value, multiplier);
 			int v = Integer.valueOf(value);
 			time += v * multiplier;
     	}
     	
 		log.debug("parseTimeInput: return time={}",time);
     	return time;
     }
 
     /**
      * Given milliseconds as input, this will return a string that represents
      * that time format.
      * 
     * @param seconds
      * @param useShortHand
      *            set to true to use shorthand notation. shorthand will return
      *            a string of the form "4d3h2m" whereas this set to false would
      *            return "4 days 3 hours 2 minutes"
      * @param mostSignificant
      *            Most significant string to show. "y" for year, "mo" for
      *            month, "w" for week, "d" for day, "m" for minute and null to
      *            include seconds
      * 
      * @return
      * @throws NumberFormatException
      */
     public String displayTimeString(final long millis, boolean useShortHand, String mostSignificant) throws NumberFormatException {
     	final StringBuffer sb = new StringBuffer();
     	long seconds = millis / 1000;		// chop down to seconds
    	
         if( seconds >= (86400 * 365) ) {
             long years = seconds / (86400 * 365);
             log.debug("years={}",years);
             if( years > 0 ) {
                 sb.append(years);
                 if( useShortHand )
                     sb.append(timeShortHand.get("y"));
                 else {
                     sb.append(" ");
                     if( years > 1 )
                         sb.append(timeLongHand.get("years"));
                     else
                         sb.append(timeLongHand.get("year"));
                 }
             }
             seconds -= years * (86400 * 365);
         }
         // "mostSignificant" is only passed in code (no user input) so this string
         // is not localized.
         if( mostSignificant != null && mostSignificant.startsWith("y") )
             return sb.toString();
         
     	if( seconds >= (86400 * 31) ) {
     		long months = seconds / (86400 * 31);
 	    	log.debug("months={}",months);
 	    	if( months > 0 ) {
 	    		sb.append(months);
 	    		if( useShortHand )
 	    			sb.append(timeShortHand.get("mo"));
 	    		else {
 	    			sb.append(" ");
 		    		if( months > 1 )
 		        		sb.append(timeLongHand.get("months"));
 		    		else
 		        		sb.append(timeLongHand.get("month"));
 	    		}
 	    	}
 	    	seconds -= months * (86400 * 31);
     	}
     	// "mostSignificant" is only passed in code (no user input) so this string
     	// is not localized.
     	if( mostSignificant != null && mostSignificant.startsWith("mo") )
     		return sb.toString();
 
     	if( seconds >= (86400 * 7) ) {
     		long weeks = seconds / (86400 * 7);
 	    	log.debug("weeks={}",weeks);
 	    	if( weeks > 0 ) {
 	    		if( sb.length() > 0 ) {
 	    			if( !useShortHand )
 	    				sb.append(",");
 	    			sb.append(" ");
 	    		}
 	    		sb.append(weeks);
 	    		if( useShortHand )
 	    			sb.append(timeShortHand.get("w"));
 	    		else {
 	    			sb.append(" ");
 		    		if( weeks > 1 )
 		        		sb.append(timeLongHand.get("weeks"));
 		    		else
 		        		sb.append(timeLongHand.get("week"));
 	    		}
 	    	}
 	    	seconds -= weeks * (86400 * 7);
     	}
     	log.debug("week remaining seconds={}",seconds);
     	if( mostSignificant != null && mostSignificant.startsWith("w") )
     		return sb.toString();
     	
     	if( seconds >= 86400 ) {
     		long days = seconds / 86400;
 	    	if( days > 0 ) {
 	    		if( sb.length() > 0 ) {
 	    			if( !useShortHand )
 	    				sb.append(",");
 	    			sb.append(" ");
 	    		}
 	    		sb.append(days);
 	    		if( useShortHand )
 	    			sb.append(timeShortHand.get("d"));
 	    		else {
 	    			sb.append(" ");
 		    		if( days > 1 )
 		        		sb.append(timeLongHand.get("days"));
 		    		else
 		        		sb.append(timeLongHand.get("day"));
 	    		}
 	    	}
 	    	seconds -= days * 86400;
     	}
     	if( mostSignificant != null && mostSignificant.startsWith("d") )
     		return sb.toString();
     	
     	if( seconds >= 3600 ) {
     		long hours = seconds / 3600;
 	    	if( hours > 0 ) {
 	    		if( sb.length() > 0 ) {
 	    			if( !useShortHand )
 	    				sb.append(",");
 	    			sb.append(" ");
 	    		}
 	    		sb.append(hours);
 	    		if( useShortHand )
 	    			sb.append(timeShortHand.get("h"));
 	    		else {
 	    			sb.append(" ");
 		    		if( hours > 1 )
 		        		sb.append(timeLongHand.get("hours"));
 		    		else
 		        		sb.append(timeLongHand.get("hour"));
 	    		}
 	    	}    	
 	    	seconds -= hours * 3600;
     	}
     	if( mostSignificant != null && mostSignificant.startsWith("h") )
     		return sb.toString();
     	
     	if( seconds >= 60 ) {
     		long minutes = seconds / 60;
 	    	if( minutes > 0 ) {
 	    		if( sb.length() > 0 ) {
 	    			if( !useShortHand )
 	    				sb.append(",");
 	    			sb.append(" ");
 	    		}
 	    		sb.append(minutes);
 	    		if( useShortHand )
 	    			sb.append(timeShortHand.get("m"));
 	    		else {
 	    			sb.append(" ");
 		    		if( minutes > 1 )
 		        		sb.append(timeLongHand.get("minutes"));
 		    		else
 		        		sb.append(timeLongHand.get("minute"));
 	    		}
 	    	}    	
 	    	seconds -= minutes * 60;
     	}
     	if( mostSignificant != null && mostSignificant.startsWith("m") )
     		return sb.toString();
     	
     	if( seconds > 0 ) {
     		if( sb.length() > 0 ) {
     			if( !useShortHand )
     				sb.append(",");
     			sb.append(" ");
     		}
     		sb.append(seconds);
     		if( useShortHand )
     			sb.append(timeShortHand.get("s"));
     		else {
     			sb.append(" ");
 	    		if( seconds > 1 )
 	        		sb.append(timeLongHand.get("seconds"));
 	    		else
 	        		sb.append(timeLongHand.get("second"));
     		}
     	}
     	
     	return sb.toString();
     }
 
     private void setupTimeLocaleStrings() {
         timeLongHand.clear();
         timeShortHand.clear();
         
         // default English is hard-coded so that even without a Locale specified,
         // the time-related functions will work.
         if( locale == null || "en".equalsIgnoreCase(locale.getLocale()) ) {
             timeLongHand.put("year", "year");
             timeLongHand.put("years", "years");
             timeLongHand.put("month", "month");
             timeLongHand.put("months", "months");
             timeLongHand.put("week", "week");
             timeLongHand.put("weeks", "weeks");
             timeLongHand.put("day", "day");
             timeLongHand.put("days", "days");
             timeLongHand.put("hour", "hour");
             timeLongHand.put("hours", "hours");
             timeLongHand.put("minute", "minute");
             timeLongHand.put("minutes", "minutes");
             timeLongHand.put("second", "second");
             timeLongHand.put("seconds", "seconds");
             
             timeShortHand.put("y", "y");
             timeShortHand.put("mo", "mo");
             timeShortHand.put("w", "w");
             timeShortHand.put("d", "d");
             timeShortHand.put("h", "h");
             timeShortHand.put("m", "m");
             timeShortHand.put("s", "s");
         }
         
         // even if "en" is set and receives defaults from above, this allows
         // the actual locale specified to override the hardcoded defaults
         if( locale != null ) {
             timeLongHand.put("year", locale.getMessage("year"));
             timeLongHand.put("years", locale.getMessage("years"));
             timeLongHand.put("month", locale.getMessage("month"));
             timeLongHand.put("months", locale.getMessage("months"));
             timeLongHand.put("week", locale.getMessage("week"));
             timeLongHand.put("weeks", locale.getMessage("weeks"));
             timeLongHand.put("day", locale.getMessage("day"));
             timeLongHand.put("days", locale.getMessage("days"));
             timeLongHand.put("hour", locale.getMessage("hour"));
             timeLongHand.put("hours", locale.getMessage("hours"));
             timeLongHand.put("minute", locale.getMessage("minute"));
             timeLongHand.put("minutes", locale.getMessage("minutes"));
             timeLongHand.put("second", locale.getMessage("second"));
             timeLongHand.put("seconds", locale.getMessage("seconds"));
             
             timeShortHand.put("y", locale.getMessage("YEAR_SHORTHAND"));
             timeShortHand.put("mo", locale.getMessage("MONTH_SHORTHAND"));
             timeShortHand.put("w", locale.getMessage("WEEK_SHORTHAND"));
             timeShortHand.put("d", locale.getMessage("DAY_SHORTHAND"));
             timeShortHand.put("h", locale.getMessage("HOUR_SHORTHAND"));
             timeShortHand.put("m", locale.getMessage("MINUTE_SHORTHAND"));
             timeShortHand.put("s", locale.getMessage("SECOND_SHORTHAND"));
         }
     }
 }
