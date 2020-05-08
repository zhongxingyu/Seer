 /**
  * Copyright 2010 Kurtis Nusbaum
  *
  * This file is part of LinkSchedule.  
  *
  * LinkSchedule is free software: you can 
  * redistribute it and/or modify it under the terms of the GNU General Public 
  * License as published by the Free Software Foundation, either version 2 of the 
  * License, or (at your option) any later version.  
  *
  * LinkSchedule is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
  * for more details.  You should have received a copy of the GNU  General 
  * Public License along with LinkSchedule. If not, see 
  * http://www.gnu.org/licenses/.
  */
 
 package org.klnusbaum.linkschedule;
 
 import android.content.Context;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.content.res.TypedArray;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.LayoutInflater;
 
 import java.util.GregorianCalendar;
 import java.util.Map;
 
 /**
  * Compound component displaying a Bus Stop name and the time the next
  * bus for that stop comes
  * 
  * @author Kurtis Nusbaum
  * @version 1.0
  */
 public class ClockView extends RelativeLayout implements CalendarBackedView{
 
 	/** The two textviews that make up a ClockView */
 	private TextView time, stopLabel, timeTill;
 
 	/** The calendar representing the content in the "time" TextView */
 	private GregorianCalendar cal;
 
 	/**
 	 * Constructs a new ClockView.
 	 *
 	 * @param context The context in which the view is being displayed.
 	 * @param stopName The name of the stop the ClockView is displaying.
 	 */
 	public ClockView(Context context, CharSequence stopName){
 		super(context);
 		initClock(context, stopName);
 	}
 
 	/**
 	 * Constructs a new ClockView.
 	 *
 	 * @param context The context in which the view is being displayed.
 	 * @param attrs Set of attributes used to define the ClockView.
 	 */
 	public ClockView(Context context, AttributeSet attrs){
 		super(context, attrs);
 		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClockView);
 		CharSequence stopName = a.getString(R.styleable.ClockView_stopName);
 		initClock(context, stopName);
 	}
 
 	/**
    * Initializes the ClockView.
    *
 	 * @param context The context in which the view is being displayed.
 	 * @param stopName The name of the stop the ClockView is displaying.
 	 */
 	private void initClock(Context context, CharSequence stopName){
 		LayoutInflater inflater = 
 			(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			inflater.inflate(R.layout.clock_layout, this);
 		//setOrientation(VERTICAL);
 		stopLabel = (TextView)findViewById(R.id.stopLabel);
 		time = (TextView)findViewById(R.id.time);
 		timeTill = (TextView)findViewById(R.id.timeTill);
 		if(stopName != null){
 			stopLabel.setText(stopName);
 		}
 	}
 
 	/**
    * Set the time displayed by the clock.
    *
    * @param calendarAndLabel A value is the time to be displayed in the 
 	 * ClockView and whose key is a GregorianCalendar that corresponds to the
    * time to be displyed.
 	 * @param currentTime A gregorian calednar representing what the current time
 	 * is. This is used to calculate the value placed in the timeTill field.
 	 * If this arguement is null, the timeTill field will not be populated and it
 	 * will be hidden.
    */
 	public void setClockTime(
 		Map.Entry calendarAndLabel, 
 		GregorianCalendar currentTime)
 	{
 		time.setText((String)calendarAndLabel.getValue());
 		if(currentTime != null){
 			timeTill.setText(getTimeTillString(
 				currentTime, 
 				(GregorianCalendar)calendarAndLabel.getKey()));
 			timeTill.setVisibility(View.VISIBLE);
 		}
 		else{
 			timeTill.setVisibility(View.INVISIBLE);
 		}
 		cal = ((GregorianCalendar)calendarAndLabel.getKey());
 	}
 
 	/**
    * Set the time displayed by the clock.
 	 *
    * Eequivalent to calling setClockTime(calendarAndLabel, null)
    */
 	public void setClockTime(Map.Entry calendarAndLabel){
 		setClockTime(calendarAndLabel, null);
 	}
 
 	
 	/**
    * Gets the stop name being used by this ClockView.
    *
  	 * @return The stop name being displayed by this ClockView.
 	 */
 	public CharSequence getStopName(){
 		return stopLabel.getText();
 	}	
 
 	/**
    * Gets the time being displayed by this ClockView.
    *
  	 * @return The time being displayed by the ClockView.
 	 */
 	public CharSequence getClockTime(){
 		return time.getText();
 	}
 
 	@Override
 	public GregorianCalendar getCalendar(){
 		return cal;
 	}
 
 	/**
 	 * Generates a string representing the time left until the next bus
    * comes based on the given arguements.
 	 *
  	 * @param currentTime A GregorianCalendar representing the current time.
 	 * @param stopTime When the next bus is coming for given stop.
 	 */
 	public static String getTimeTillString(
 		GregorianCalendar currentTime,
 		GregorianCalendar stopTime)
 	{
 		long diff = stopTime.getTimeInMillis() - currentTime.getTimeInMillis();
 		String timeLabel;
 		String displayString;
 		double displayTime;
 		long mins = diff / (60*1000);		
 		if(mins >= 60){
 			displayTime = diff / (60.0*60.0*1000.0);	
 			if(displayTime == 1.0){
 				timeLabel = "hour";
 			}
 			else{
 				timeLabel = "hours";
 			}
 			displayString = String.valueOf(displayTime);
 			int dotIndex = displayString.indexOf(".");
 			displayString = displayString.substring(0,dotIndex+2);	
 		}
 		else{
 			if(mins == 1){
 				timeLabel = "minute";
 				displayTime = (float)mins;
 			}
 			else{
 				timeLabel = "minutes";
 				displayTime = (double)mins;
 			}
 			displayString = String.valueOf(displayTime);
 			int dotIndex = displayString.indexOf(".");
 			displayString = displayString.substring(0,dotIndex);	
 		}
 		return "(" + displayString + " " + timeLabel + ")";
 	}
 }
 		
