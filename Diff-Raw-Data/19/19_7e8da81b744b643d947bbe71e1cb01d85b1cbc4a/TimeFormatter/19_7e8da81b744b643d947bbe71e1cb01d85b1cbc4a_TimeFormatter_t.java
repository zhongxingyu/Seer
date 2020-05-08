 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.speech;
 
 import java.util.Locale;
 
 // Utility for formatting times to strings to be read out by the speech feature:
 // For example, 9:45 will be formatted to "quarter to 10"
 public class TimeFormatter {
 	
 	private final Locale locale;
 	private final String am;
 	private final String pm;
 	private final String past;
 	private final String to;
 	private final String quarter;
 	private final String halfPast;
 	
 	public TimeFormatter(final Locale locale) {
 		this.locale = locale;
 			
 		if(TextToSpeechUtil.isEnglish(locale)) {
 			this.am = "a. m. ";
 			this.pm = "p. m. ";
 			past = "past";
 			to = "to";
 			quarter = "quarter";
 			halfPast = "half past";
 		} else if(TextToSpeechUtil.isSwedish(locale)) {
 			this.am = "på förmiddagen";
 			this.pm = "på eftermiddagen";	
 			to = "i";
 			past = "över";
 			quarter = "kvart";
 			halfPast = "halv";
 		} else if(TextToSpeechUtil.isJapanese(locale)) {
 			// These are not needed in the Japanese version. 
 			this.am = null;
 			this.pm = null;	
 			to = null;
 			past = null;
 			quarter = null;
 			halfPast = null;
 		}   else {
 			throw new IllegalArgumentException("The locale is not supported: "+ locale);
 		}
 	}
 
 	// round the minutes to its nearest five minutes
 	// 2 should be rounded to 0, 23 to 25, 22 to 20
 	private int roundMinute(int min) {
 		
 		//hämta entalssiffran.
 		int m = min % 10;
 		// tiotalssiffran
 		int n = min - m;
 		
 		if(m== 0)  {
 			// doesn't need rounding 
 			return n;
 		}else if(m == 1 || m == 2) {
 			return n; 
 		} else if(m == 3 || m==4) {
 			return n + 5;
 		} else if(m == 5) {
 			// doesn't need rounding
 			return min;
 		} else if(m == 6 || m == 7) {
 			return n + 5;
 		} else if(m == 8 || m == 9) {
 			return n + 10;
 		} else {
 			throw new IllegalArgumentException("this should not happen");
 		}
 	}
 		
 	private String minuteToStringUtil(int min) {
 		if(min == 5) {
 			return "5";
 		} else if(min == 10) {
 			return "10";
 		} else if(min == 15) {
 			return quarter;
 		} else if(min == 20) {
 			return "20";
 		} else if(min == 25) {
 			return "25";
 		} else {
 			throw new IllegalArgumentException("this should not happen");
 		}
 	}
 	
 	private String minuteToString(int min) {
 		if(min == 30) {
 			return halfPast;
 		}
 		
 
 		if(TextToSpeechUtil.isSwedish(locale)) {
 
 			// because for example "25 past 3" becomes "5 i half fyra" in Swedish. 
 			if(min == 25) {
 				return "5 " + to + " " + halfPast; 
 			} else if(min == 35) {
 				return "5 " + past + " " + halfPast;
 			}
 		}
 		
 		// is either the value "past" or "to"
 		String pastOrTo;
 		if(min > 30) {
 			pastOrTo =to;
 			min = 60 -min;
 		} else {
 			pastOrTo = past;
 		}
 		
 		return minuteToStringUtil(min) + " " + pastOrTo;
 	}	
 	
 	// returns a string formatted the way humans read it.
 	// if the language is English, then for 10:30 it will return "half past ten"
 	// for 6:10 it will return "ten past six"
 	public String formatTime(int hour, int originalMin) {
 	
 		int min = roundMinute(originalMin);
 		
		
 		
 		// if we rounded to the next hour
 		if(min == 60 && originalMin > 55) {
 			hour = (hour + 1) % 24;
 			min = 0;
 		}
		
		if(TextToSpeechUtil.isJapanese(locale)) {
			if(min == 0) {
				return String.format("%1$s時", hour);
			} else {
				return String.format("%1$s時%2$s分", hour, min);
			}
		}
 				
 		String ampm;
 		// we use 12-hour clock
 		// http://en.wikipedia.org/wiki/12-hour_clock
 		if(hour < 12) {
 			if(hour < 1)  {
 				hour = 12;
 			}
 			ampm = am;
 		} else {
 			
 			if(hour < 13) {
 				// do nothing
 			} else {
 				hour  = hour - 12;
 			}
 			ampm = pm;
 		}
 		
 		String hourStr;
 		if(min == 0)  {
 			hourStr = Integer.toString(hour);
 		} else {
 			
 			int h;
 			
 			
 			/*
 			 * "half past" and "halv" works differently. For example,
 			 * "Half past ten" becomes "halv elva"
 			 */
 			if(TextToSpeechUtil.isSwedish(locale)) {
 				
 				if(min < 30 &&
 						// because for example "25 past 3" becomes "5 i half fyra" in Swedish. 
 						min != 25) {
 					h = hour;
 				} else {
 					h = (1+hour);
 				}
 			} else {
 				if(min <= 30) {
 					h = hour;
 				} else {
 					h = (1+hour);
 				}
 			}
 	
 			
 			if(h == 13) {
 				h = 1;
 			}
 			
 			hourStr = Integer.toString(h);
 		}
 		if(min == 0) {
 			// minutes are 0, so only need to tell the hours. 
 			return hourStr + " " + ampm;
 		}
 		String minStr = minuteToString(min);
 		return minStr +" "+ hourStr + " " + ampm ;
 	}
 }
