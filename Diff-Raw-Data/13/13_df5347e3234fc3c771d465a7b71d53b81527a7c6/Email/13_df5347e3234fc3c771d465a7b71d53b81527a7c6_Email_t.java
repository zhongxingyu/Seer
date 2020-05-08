 package com.kopysoft.chronos.content;
 
 /**
  * 			Copyright (C) 2011 by Ethan Hall
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  * 	in the Software without restriction, including without limitation the rights
  * 	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * 	copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *  
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *  
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  *  
  */
 
 import java.util.GregorianCalendar;
 
 import android.content.Context;
 
 import com.kopysoft.chronos.enums.Defines;
 import com.kopysoft.chronos.enums.TimeFormat;
 import com.kopysoft.chronos.enums.Verbosity;
 import com.kopysoft.chronos.singelton.PreferenceSingelton;
 import com.kopysoft.chronos.types.Day;
 import com.kopysoft.chronos.types.Note;
 import com.kopysoft.chronos.types.PayPeriod;
 import com.kopysoft.chronos.types.Punch;
 
 public class Email {
 
 	int[] i_start;
 	int[] i_end;
 	Verbosity i_verbosLevel;
 	Context i_context;
 	public Email(int[] startDay, int[] endDay, Verbosity verbosity, Context context){
 		i_start = startDay;
 		i_end = endDay;
 		i_verbosLevel = verbosity;
 		i_context = context;
 	}
 
 	/**
 	 * Generates a report for a given week
 	 * @param date Start of the pay period
 	 * @param weeks_in_pp Number of weeks in the pay period
 	 * @param verbosity Verbose level
 	 * @return String with the text to be put into an email
 	 */
 	public String generateEmailText(){
 
 
 		String returnValue = "";
 		GregorianCalendar cal = null;
 		String dateString = null;
 		String timeString = null;
 		String typeString = null;
 		String timeTotal = "";
 
 		PayPeriod thisPP = new PayPeriod(i_start, i_end, i_context);
 		Day workingDay = null;
 		Punch tempPunch = null;
 		Note noteTemp;
 
 		int[] tempTime = null;
 
 		switch(i_verbosLevel){
 		case EVERY_PUNCH:
 			for(int i = 0; i < thisPP.size(); i++){
 				workingDay = thisPP.get(i);
 				tempTime = workingDay.getDay();
 
 				cal = new GregorianCalendar(tempTime[0], tempTime[1], tempTime[2]);
 				dateString = Defines.DAYSABBV[( cal.get(GregorianCalendar.DAY_OF_WEEK) - 1 ) % 7] + " " +
 				Defines.MONTHS[cal.get(GregorianCalendar.MONTH)] + ", " + 
 				cal.get(GregorianCalendar.DAY_OF_MONTH);
 				for( int j = 0; j < workingDay.getSize(); j++){
 					tempPunch = workingDay.get(j);
 					timeString = tempPunch.generatePunchString();
 					typeString = tempPunch.generateTypeString(i_context);
 
 					returnValue += String.format("%s:\t %s (%s)\n",dateString, timeString, typeString);
 				}
 
 				noteTemp = new Note(tempTime, i_context);
 				if(noteTemp.getNote(false).equalsIgnoreCase("") == false){
 					returnValue += String.format("\tNote:\t %s\n", noteTemp.getNote(false));
 				}
 			}
 			break;
 		case ONLY_DAY:
 			for(int i = 0; i < thisPP.size(); i++){
 				workingDay = thisPP.get(i);
 				tempTime = workingDay.getDay();
 
 				cal = new GregorianCalendar(tempTime[0], tempTime[1], tempTime[2]);
 				dateString = Defines.DAYSABBV[( cal.get(GregorianCalendar.DAY_OF_WEEK) - 1 ) % 7] + " " +
 				Defines.MONTHS[cal.get(GregorianCalendar.MONTH)] + ", " +
 				cal.get(GregorianCalendar.DAY_OF_MONTH);
 				timeString = StaticFunctions.generateTimeString(workingDay.getTimeWithBreaks()/1000, 
 						TimeFormat.HOUR_DECIMAL, false);
 				returnValue += String.format("%s:\t %s\n",dateString, timeString);
 
 				noteTemp = new Note(tempTime, i_context);
 				if(noteTemp.getNote(false).equalsIgnoreCase("") == false){
 					returnValue += String.format("\tNote:\t %s\n", noteTemp.getNote(false));
 				}
 			}
 
 			break;
 		default: 
 			break;
 		}
 
 		long timeToday = 0;
 		Day temp;
 
 		for(int i = 0; i < thisPP.size(); i++){
 			temp = thisPP.get(i);
 			if ( temp.getTimeWithBreaks() >= 0 ){
 				timeToday += temp.getTimeWithBreaks();
 			}
 		}
 
 		PreferenceSingelton prefs = new PreferenceSingelton();
 		boolean clockedInStill = false;
 		if(timeToday < 0){
 			clockedInStill = true;
 			timeToday += GregorianCalendar.getInstance().getTimeInMillis();
 		}
 		timeTotal = StaticFunctions.generateTimeWeek(timeToday, TimeFormat.HOUR_DECIMAL, false);
 		String payAmount = StaticFunctions.generateDollarAmount(getTimeForMoney(thisPP), 
 				prefs.getPayRate(i_context));
 		returnValue += String.format("\n\tTotal Time:\t %s\n", timeTotal);
		if(prefs.getShowPay(i_context))
			returnValue += String.format("\tEstimated Pay:\t %s", payAmount);
 		if(clockedInStill){
 			returnValue += "\n\n\tPlease note that the time above is only an estimate. Your times done add up properly... The time was done based on the time it was sent";
 		} 
 		
 		return returnValue;
 	}
 
 	private long getTimeForMoney(PayPeriod running){
 		long returnValue = 0;
 		long tempTime;
 		Day temp;
 		PreferenceSingelton prefs = new PreferenceSingelton();
 
 		int overtimeSetting = prefs.getOvertimeSetting(i_context);
 		float overtimeRate = prefs.getOvertimeRate(i_context);
 		boolean overtimeEnable = prefs.getOvertimeEnable(i_context);
 		for(int i = 0; i < running.size(); i++){
 			temp = running.get(i);
 			if ( temp.getTimeWithBreaks() >= 0 ){
 				tempTime = temp.getTimeWithBreaks();
 				if(overtimeSetting == Defines.OVERTIME_8HOUR && overtimeEnable == true){
 					if(tempTime > Defines.SECONDS_IN_HOUR * 8){
 						returnValue += Defines.SECONDS_IN_HOUR * 8;
 						returnValue += (tempTime - Defines.SECONDS_IN_HOUR * 8) * overtimeRate;	
 
 					} else {
 						returnValue += tempTime;
 					}
 				}  else {
 					returnValue += tempTime;
 					//Log.d(TAG, "time for day: " + tempTime);
 				}
 			}
 		}
 
 		if (overtimeSetting == Defines.OVERTIME_40HOUR && overtimeEnable == true ){
 			returnValue = 0;
 			for(int i = 0; i < prefs.getWeeksInPP(i_context); i++){
 				long weekTemp = 0;
 				for(int j = 0; j < 7; j++){
 					temp = running.get(i * 7 + j);
 					weekTemp += temp.getTimeWithBreaks();
 				}
 				if ( weekTemp > Defines.SECONDS_IN_HOUR * 40){
 					tempTime = weekTemp - Defines.SECONDS_IN_HOUR * 40;
 					weekTemp = Defines.SECONDS_IN_HOUR * 40;
 					weekTemp += tempTime * overtimeRate;
 				}
 				returnValue += weekTemp;
 			}
 		}
 		return returnValue;
 	}
 }
