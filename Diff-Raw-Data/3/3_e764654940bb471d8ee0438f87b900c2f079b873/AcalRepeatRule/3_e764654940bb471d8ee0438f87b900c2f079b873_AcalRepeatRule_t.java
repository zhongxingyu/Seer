 /*
  * Copyright (C) 2011 Morphoss Ltd
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
  *
  */
 
 package com.morphoss.acal.acaltime;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.util.Log;
 
 import com.morphoss.acal.Constants;
 import com.morphoss.acal.davacal.AcalAlarm;
 import com.morphoss.acal.davacal.AcalEvent;
 import com.morphoss.acal.davacal.AcalProperty;
 import com.morphoss.acal.davacal.Masterable;
 import com.morphoss.acal.davacal.RecurrenceId;
 import com.morphoss.acal.davacal.VCalendar;
 import com.morphoss.acal.davacal.VEvent;
 
 /**
  * @author Morphoss Ltd
  */
 
 
 public class AcalRepeatRule {
 
 	final static public String			TAG					= "AcalRepeatRule";
 
 	private final AcalDateTime			baseDate;
 	public final AcalRepeatRuleParser	repeatRule;
 
 	private AcalDateTime[]				rDate				= null;
 	private int							rDatePos			= -1;
 	private AcalDateTime[]				exDate				= null;
 	private int							exDatePos			= -1;
 
 	private List<AcalDateTime>			recurrences			= null;
 	private Map<Long, EventInstance>	eventTimes			= null;
 	private int							lastCalc			= -1;
 	private int							currentPos			= -1;
 	private boolean						finished			= false;
 	private boolean						started				= false;
 	private AcalDuration				baseDuration		= null;
 	private AcalDuration				lastDuration		= null;
 
 	private VCalendar					sourceVCalendar		= null;
 
 	final public static AcalRepeatRuleParser SINGLE_INSTANCE = AcalRepeatRuleParser.parseRepeatRule("FREQ=DAILY;COUNT=1");
 	
 	final private static int			MAX_REPEAT_INSTANCES	= 100;
 	
 	private boolean isPending = false;
 	
 	public AcalRepeatRule(AcalDateTime dtStart, String rRule) {
 		baseDate = dtStart.clone();
 		if ( rRule == null || rRule.equals("")) {
 			recurrences = new ArrayList<AcalDateTime>(1);
 			recurrences.add(baseDate);
 			currentPos	= -1;
 			lastCalc	= 0;
 			started 	= true;
 			finished 	= true;
 			repeatRule = AcalRepeatRule.SINGLE_INSTANCE;
 		}
 		else
 			repeatRule = AcalRepeatRuleParser.parseRepeatRule(rRule);
 		eventTimes = new HashMap<Long,EventInstance>();
 	}
 
 	public AcalRepeatRule(AcalProperty dtStart, AcalProperty rRule) {
 		this((dtStart == null
 					? null
 					: AcalDateTime.fromIcalendar(dtStart.getValue(),dtStart.getParam("VALUE"),dtStart.getParam("TZID"))),
 				(rRule == null ? null : rRule.getValue())
 			);
 	}
 
 
 	public void setUntil(AcalDateTime newUntil) {
 		repeatRule.setUntil(newUntil);
 	}	
 
 	public static AcalRepeatRule fromVCalendar( VCalendar vCal ) {
 		Masterable firstEvent = vCal.getMasterChild();
 		if ( firstEvent == null ) {
 			if ( Constants.debugRepeatRule && Constants.LOG_VERBOSE ) {
 				Log.w(TAG, "Cannot find master instance inside " + vCal.getName() );
 				Log.v(TAG, "Original blob is\n"+vCal.getOriginalBlob() );
 			}
 			return null;
 		}
 
 		AcalProperty repeatFromDate = firstEvent.getProperty("DTSTART");
 		if ( repeatFromDate == null )
 			repeatFromDate = firstEvent.getProperty("DUE");
 		if ( repeatFromDate == null )
 			repeatFromDate = firstEvent.getProperty("COMPLETED");
 		if ( repeatFromDate == null )
 			repeatFromDate = firstEvent.getProperty("DTEND");
 		if ( repeatFromDate == null ) {
 			if ( Constants.debugRepeatRule && Constants.LOG_VERBOSE ) {
 				Log.v(TAG,"Cannot calculate instances of "+firstEvent.getName()+" without DTSTART/DUE inside " + vCal.getName() );
 				repeatFromDate = firstEvent.getProperty("DTSTART");
 				firstEvent = vCal.getMasterChild();
 				repeatFromDate = firstEvent.getProperty("DTSTART");
 				Log.v(TAG, "Original blob is\n"+vCal.getOriginalBlob() );
 			}
 			return null;
 		}
 
 		AcalRepeatRule ret = new AcalRepeatRule( repeatFromDate, firstEvent.getProperty("RRULE") );
 		ret.sourceVCalendar = vCal;
 		if (vCal.isPending()) ret.setPending(true);
 		
 		ret.baseDuration = firstEvent.getDuration();
 
 		String dateLists[] = {"RDATE","EXDATE"};
 		for( String dListPName : dateLists ) {
 			AcalProperty dateListProperty = firstEvent.getProperty(dListPName);
 			if ( dateListProperty == null )	continue;
 		
 			String value = dateListProperty.getValue();
 			if ( value == null )	return null;
 		
 			String isDateParam = dateListProperty.getParam("VALUE");
 			String tzIdParam = dateListProperty.getParam("TZID");
 			
 			final String[] dateList = Constants.splitOnCommas.split(value);
 			AcalDateTime[] timeList = new AcalDateTime[dateList.length];
 			
 			for( int i=0; i < dateList.length; i++ ) {
 				timeList[i] = AcalDateTime.fromIcalendar( dateList[i], isDateParam, tzIdParam );
 			}
 			Arrays.sort(timeList);
 			if ( dListPName.equals("RDATE") ) {
 				ret.rDate = timeList;
 				ret.rDatePos = 0;
 			}
 			else if ( dListPName.equals("EXDATE") ) {
 				ret.exDate = timeList;
 				ret.exDatePos = 0;
 			}
 		}
 		
 
 		return ret;
 	}
 	
 	
 	public void reset() {
 		currentPos = -1;
 	}
 
 	public AcalDateTime next() throws Exception {
 		if (currentPos > lastCalc && finished) return null;
 		currentPos++;
 		getMoreInstances();
 		if (currentPos > lastCalc && finished) return null;
 		if ( currentPos >= recurrences.size() ) {
 			if (Constants.LOG_DEBUG) Log.d(TAG,"Managed to exceed recurrences.size() at " + currentPos+"/"+recurrences.size()
 						+" Last"+lastCalc+(finished?" (finished)":"")
 						+" processing: " + this.repeatRule.toString() );
 		}
 		return recurrences.get(currentPos);
 	}
 
 	public boolean hasNext() throws Exception {
 		if ( currentPos < lastCalc ) return true;
 		getMoreInstances();
 		if (currentPos >= lastCalc && finished) return false;
 		return true;
 	}
 
 	/**
 	 * <h3>Internal enum for types of FREQ=type</h3>
 	 * <p>
 	 * At present we only support Yearly, Monthly, Weekly Daily.  Hourly, Minutely and Secondly are
 	 * omitted, although the code is fairly trivial, and we should probably write it for completeness.
 	 * </p>
 	 * 
 	 * @author Morphoss Ltd
 	 */
 	public enum RRuleFreqType {
 		YEARLY, MONTHLY, WEEKLY, DAILY; //, HOURLY, MINUTELY, SECONDLY;
 
 		private final static Pattern	freqPattern	= Pattern.compile(
 						".*FREQ=((?:WEEK|DAI|YEAR|MONTH|HOUR|MINUTE|SECOND)LY).*",
 						Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
 		
 		/**
 		 * <p>
 		 * Assumes we have essentially correct strings and efficiently turns them into enums efficiently.
 		 * Maybe we could be marginally more efficient if we purely treated the two-byte strings as a short
 		 * int, but hey.
 		 * </p>
 		 * 
 		 * @param stFreq
 		 * @return
 		 */
 		public static RRuleFreqType fromString(String stFreq) {
 			Matcher m = freqPattern.matcher(stFreq);
 			if ( !m.matches() )
 				throw new IllegalArgumentException("RRULE '"+stFreq+"' is not a valid frequency specifier.");
 			switch (m.group(1).charAt(0)) {
 				case 'Y':
 					return YEARLY;
 				case 'W':
 					return WEEKLY;
 				case 'D':
 					return DAILY;
 //				case 'H':
 //					return HOURLY;
 //				case 'S':
 //					return SECONDLY;
 				case 'M':
 					switch (m.group(1).charAt(1)) {
 						case 'O':
 							return MONTHLY;
 //						case 'I':
 //							return MINUTELY;
 					}
 			}
 			throw new IllegalArgumentException("Invalid frequency 'FREQ="+m.group(1)+"' in RRULE definition: "+stFreq);
 		}
 	};
 
 
 
 	private boolean getMoreInstances() throws Exception {
 	    if ( finished ) return false;
 	    if ( currentPos < lastCalc ) return true;
 	    
 	    if ( recurrences != null && recurrences.size() > 3000 ) {
 			Log.e(TAG,"Too many instances (3000):");
 			Log.e(TAG,"Too many " +baseDate.toPropertyString("DTSTART"));
 			Log.e(TAG,"Too many " +repeatRule.toString());
 		    throw new Exception("ETOOTOOMUCHREPETITIONKTHXBAI");
 		}
 
 	    boolean foundSome = false;
 	    int emptySets = 0;
 
 		while ( !finished && currentPos >= lastCalc ) {
 		   	repeatRule.nextBaseDate(baseDate);
 	    	List<AcalDateTime> newSet = repeatRule.buildSet();
 	    	if ( newSet.isEmpty() ) {
 	    		if ( emptySets++ > 50 ) {
 	    			finished = true;
 	    			Log.e(TAG,"Too many empty sets processing "+repeatRule.toString());
 	    		}
 	    		continue;
 	    	}
 			Collections.sort(newSet, new AcalDateTime.AcalDateTimeSorter());
 
 			emptySets = 0;
 	    	AcalDateTime thisInstance = null;
     		int i=0;
 	    	while( !finished && i < newSet.size()
 	    				&& ( repeatRule.count == AcalRepeatRuleParser.INFINITE_REPEAT_COUNT 
 	    							||  lastCalc < repeatRule.count ) ) {
 	    		thisInstance = newSet.get(i++);
 	    		if ( !started ) {
 	    			if ( thisInstance.before(baseDate)) continue;
 	    		    if ( recurrences == null ) {
 	    		    	recurrences = new ArrayList<AcalDateTime>();
 	    		    }
 	    			if ( thisInstance.after(baseDate) ) recurrences.add(baseDate);
 	    			started = true;
 		    		foundSome = true;
 	    		}
 	    		if ( repeatRule.until != null && thisInstance.after(repeatRule.until) ) {
 	    			finished = true;
 	    			break;
 	    		}
 	    		if ( exDate != null && exDatePos < exDate.length ) {
 	    			if ( exDate[exDatePos] == null || exDate[exDatePos].equals(thisInstance) || exDate[exDatePos].before(thisInstance) ) {
 	    				exDatePos++;
 	    				continue;
 	    			}
 	    		}
 	    		while ( rDate != null && rDatePos < rDate.length && 
 	    					(rDate[rDatePos] == null || rDate[rDatePos].before(thisInstance)) ) {
 		    		recurrences.add(rDate[rDatePos++].clone());
 		    		lastCalc++;
 		    		foundSome = true;
 
 		    		if ( repeatRule.count != AcalRepeatRuleParser.INFINITE_REPEAT_COUNT
 			    				&& lastCalc >= repeatRule.count ) {
 			    		finished = true;
 			    		break;
 			    	}
 	    		}
 	    		recurrences.add(thisInstance.clone());
 	    		lastCalc++;
 	    		foundSome = true;
 
 		    	if ( repeatRule.count != AcalRepeatRuleParser.INFINITE_REPEAT_COUNT
 		    				&& lastCalc >= repeatRule.count ) {
 		    		finished = true;
 		    	}
 	    	}
 		}
 //		debugInstanceList("Got More Instances");
 		return foundSome;
 	}
 
 
 	public List<AcalDateTime> getInstancesInRange( AcalDateTime start, AcalDateTime end ) throws Exception {
 		if ( end == null )
 			throw new IllegalArgumentException("getInstancesInRange: End of range may not be null.");
 
 		if ( start == null )
 			start = baseDate;
 		else if ( repeatRule.until != null && start.after(repeatRule.until) )
 			return new ArrayList<AcalDateTime>(0);
 
 		if ( recurrences != null ) {
 			for ( currentPos=0; currentPos<=lastCalc && recurrences.get(currentPos).before(start); currentPos++)
 				;
 		}
 
 		AcalDateTime thisDate = null;
 		do {
 			thisDate = next();
 		}
 		while( thisDate.before(start) );
 
 		List<AcalDateTime> ret = new ArrayList<AcalDateTime>();
 		while( thisDate != null && thisDate.before(end) ) {
 			ret.add(thisDate);
 			thisDate = next();
 		}
 		return ret;
 	}
 
 
 	/**
 	 * Returns a range which is from the earliest start date to the latest end date
 	 * for the recurrence of a VCALENDAR-based rule.
 	 * 
 	 * Instances without an UNTIL or with a COUNT > 3000 will be considered 'infinite'
 	 * and the range end will be null.
 	 * 
 	 * @return
 	 * @throws Exception 
 	 */
 	public AcalDateRange getInstancesRange() throws Exception {
 		AcalDateTime endDate = null;
 		if ( repeatRule.until != null ) endDate = repeatRule.until;
 		else if ( repeatRule.count != AcalRepeatRuleParser.INFINITE_REPEAT_COUNT
 					&& repeatRule.count < MAX_REPEAT_INSTANCES ) {
 
 			if ( Constants.debugRepeatRule && Constants.LOG_DEBUG )
 				Log.d(TAG,"Calculating instance range for count limited repeat: " + repeatRule.toString() );
 
 			while( hasNext() ) {
 				next();
 			}
 			endDate = recurrences.get(currentPos);
 			try {
 				sourceVCalendar.setPersistentOn();
 				RecurrenceId ourRecurrenceId = (RecurrenceId) AcalProperty.fromString(endDate.toPropertyString("RECURRENCE-ID"));
 				Masterable vMaster = sourceVCalendar.getChildFromRecurrenceId(ourRecurrenceId);
 				EventInstance instance = getRecurrence(endDate,vMaster);
 				eventTimes.put(endDate.getEpoch(), instance );
 				endDate = instance.dtend;
 			}
 			catch ( Exception e ) {
 				Log.w(TAG,"Exception while calculating instance range");
 				Log.w(TAG,Log.getStackTraceString(e));
 			}
 			finally {
 				sourceVCalendar.setPersistentOff();
 			}
 		}
 		return new AcalDateRange(baseDate,endDate);
 	}
 
 	private final static AcalDateTime futureish = AcalDateTime.fromMillis(System.currentTimeMillis() + (86400000L*365L*10));
 
 	//TODO dirty hack to get alarms in range.
 	public void appendAlarmInstancesBetween(List<AcalAlarm> alarmList, AcalDateRange range) {
 		List<AcalEvent> events = new ArrayList<AcalEvent>();
 		if ( this.sourceVCalendar.hasAlarm() && this.sourceVCalendar.appendEventInstancesBetween(events, range, false) ) {
 			for( AcalEvent event : events ) {
 				for (AcalAlarm alarm : event.getAlarms()) {
 					alarm.setToLocalTime();
 					if (alarm.getNextTimeToFire().after(range.start)) {
 						//the alarm needs to have event data associated
 						alarm.setEvent(event);
 						alarmList.add(alarm);
 					}
 				}
 			}
 		}
 	}
 
 	
 	public void appendEventsInstancesBetween(List<AcalEvent> eventList, AcalDateRange range) {
 		if ( range.start == null || range.end == null || eventList == null ) return;
 
 		Masterable thisEvent = sourceVCalendar.getMasterChild();
 		if ( thisEvent == null || !(thisEvent instanceof VEvent) ) {
 //			Log.d(TAG,"Skipped non-VEvent VCalendar");
 			return;
 		}
 
 		if ( Constants.debugDateTime && range.start.after(futureish) ) {
 			throw new IllegalArgumentException("The date: " + range.start.fmtIcal() + " is way too far in the future! (after " + futureish.fmtIcal() );
 		}
 		if ( Constants.debugRepeatRule && Constants.LOG_DEBUG ) {
 			 Log.d(TAG, "Fetching instances in "+range.toString());
 			 Log.d(TAG, "Base is: "+this.baseDate.fmtIcal()+", Rule is: "+repeatRule.toString() );
 		}
 
 		if ( repeatRule.until != null && repeatRule.until.before(range.start) )
 			return ;
 
 		int found = 0;
 		long processingStarted = System.currentTimeMillis();
 		AcalDateTime thisDate = null;
 		EventInstance instance = null;
 		Masterable ourVEvent = null;
 		int possiblyInfinite = 0;
 		try {
 			reset();
 			sourceVCalendar.setPersistentOn();
 			ourVEvent = sourceVCalendar.getMasterChild();
 			ourVEvent.setPersistentOn();
 			do {
 				thisDate = next();
 				if ( thisDate == null ) {
 					if ( Constants.debugRepeatRule && Constants.LOG_DEBUG )
 						Log.d(TAG, "Null before finding useful instance for " +repeatRule.toString() );
 					break;
 				}
 				if ( thisDate != null ) {
 					instance = eventTimes.get(thisDate.getEpoch());
 					if ( instance == null ) {
 						instance = getRecurrence(thisDate, ourVEvent);
 						eventTimes.put(thisDate.getEpoch(), instance);
 					}
 				}
 				if ( Constants.debugRepeatRule && Constants.LOG_DEBUG ) {
 					Log.d(TAG, "Skipping Instance: "+thisDate.fmtIcal()+" of " +repeatRule.toString() );
 					Log.d(TAG, "Skipping Instance from: "+instance.dtstart.fmtIcal()+" - "+instance.dtend.fmtIcal() );
 				}
 			}
 			while( thisDate != null && ! instance.dtend.after(range.start) && possiblyInfinite < 20 );
 
 			while( thisDate != null
 						&& instance.dtstart.before(range.end)
 						&& possiblyInfinite < 40 ) {
 
 				instance = eventTimes.get(thisDate.getEpoch());
 				if ( instance == null ) {
 					instance = getRecurrence(thisDate, ourVEvent);
 					eventTimes.put(thisDate.getEpoch(), instance);
 				}
 				if( ! instance.dtstart.before(range.end) ) break;
 
 				eventList.add(instance.getAcalEvent());
 				
 				if ( Constants.debugRepeatRule && Constants.LOG_DEBUG ) {
 					Log.d(TAG, "Adding Instance: "+thisDate.fmtIcal()+" of " +repeatRule.toString() );
 					Log.d(TAG, "Adding Instance range: "+instance.dtstart.fmtIcal()+" - "+instance.dtend.fmtIcal() );
 				}
 				
 				thisDate = next();
 
 				found++;
 			}
 		}
 		catch ( Exception e ) {
 			if ( Constants.LOG_VERBOSE ) {
 				Log.v(TAG,"Exception while appending event instances between "+range.start.fmtIcal()+" and "+range.end.fmtIcal());
 				Log.v(TAG,Log.getStackTraceString(e));
 			}
 		}
 		finally {
 			ourVEvent.setPersistentOff();
 			sourceVCalendar.setPersistentOff();
 		}
 		if ( Constants.debugRepeatRule && Constants.LOG_DEBUG ) {
 			Log.d(TAG, "Took "+(System.currentTimeMillis()-processingStarted )+"ms to find "+found+" in "+repeatRule.toString() );
 			if ( found > 0 ) {
 				Log.d(TAG, "Found "+found+" instances in "+ range.toString());
 				for( int i=0; i<found; i++ ) {
 					AcalEvent thisOne = eventList.get(i);
 					Log.v(TAG, "["+i+"] Start: " + thisOne.getStart().fmtIcal() + ", End: " + thisOne.getEnd().fmtIcal() );
 				}
 			}
 		}
 		return;
 	}
 
 	public void setPending(boolean isPending) {
 		this.isPending = isPending;
 	}
 
 	private EventInstance getRecurrence(AcalDateTime thisDate, Masterable ourVEvent ) {
 		AcalDateTime instanceStart = thisDate.clone();
 
 		if ( lastDuration == null ) lastDuration = baseDuration;
 		AcalDuration ourDuration = lastDuration;
 
 		if ( sourceVCalendar.masterHasOverrides() ) {
 			RecurrenceId instanceId = RecurrenceId.fromString( thisDate.toPropertyString("RECURRENCE-ID"));
 			ourVEvent = sourceVCalendar.getChildFromRecurrenceId(instanceId);
 			RecurrenceId overrideId = (RecurrenceId) ourVEvent.getProperty("RECURRENCE-ID");
 
 			if ( overrideId != null ) {
 				AcalProperty pStart = ourVEvent.getProperty("DTSTART");
 				if ( pStart != null ) { 
 					AcalDateTime start = AcalDateTime.fromIcalendar(pStart.getValue(),
 											pStart.getParam("VALUE"), pStart.getParam("TZID"));
 					if ( start == null ) {
 						Log.w(TAG,"Couldn't find DTSTART for our VEVENT instance!");
 					}
 					else {
 						// modify our instance time by the offset from the calculated instance time
 						AcalDateTime recurBase = AcalDateTime.fromIcalendar(overrideId.getValue(),
 									overrideId.getParam("VALUE"),overrideId.getParam("TZID"));
 						AcalDuration delta = recurBase.getDurationTo(start);
 						instanceStart = AcalDateTime.addDuration(instanceStart,delta);
 						ourDuration = ourVEvent.getDuration();
 					}
 				}
 			}
 		}
 
 		lastDuration = ourDuration;
 		
 		EventInstance ret = new EventInstance(ourVEvent, instanceStart, ourDuration, isPending); 
 
 		return ret;
 	}
 
 	private class EventInstance {
 		final Masterable VEvent;
 		final AcalDateTime dtstart;
 		final AcalDateTime dtend;
 		final AcalDuration duration;
 		final boolean isPending;
 		
 		EventInstance( Masterable VEvent, AcalDateTime dtstart, AcalDuration duration, boolean isPending ) {
 			this.VEvent = VEvent;
 			this.dtstart = dtstart;
 			this.duration = duration;
 			this.isPending = isPending;
			if ( duration.seconds < 0 || duration.days < 0 )
				throw new IllegalArgumentException("Resource duration must be positive. UID: "+VEvent.getUID() );
 			if ( Constants.debugRepeatRule && duration.days > 10 ) {
 				throw new IllegalArgumentException();
 			}
 			this.dtend = AcalDateTime.addDuration(dtstart, duration);
 		}
 
 		AcalEvent getAcalEvent() {
 			return new AcalEvent( VEvent, dtstart, duration,isPending );
 		}
 	}
 /*
 	private String[] debugDates = null;
 	protected void debugInstanceList( String whereAmI ) {
 		if ( recurrences == null || recurrences.isEmpty() ) {
 			if ( Constants.debugRepeatRule && Constants.LOG_VERBOSE )
 				Log.v(TAG, "Instances at "+whereAmI+" is empty" );
 			return;
 		}
 		debugDates = new String[recurrences.size()];
 		String dateList = "";
 
 		int startFrom = debugDates.length - 7;
 		for( int i=0; i<debugDates.length; i++ ) {
 			debugDates[i] = recurrences.get(i).fmtIcal();
 			if ( i >= startFrom ) {
 				if ( i > startFrom && i> 0 ) dateList += ", ";
 				dateList += debugDates[i]; 
 			}
 			if ( i == 3 && debugDates[0].equals(debugDates[1]) && debugDates[1].equals(debugDates[2]) ) {
 				if ( Constants.debugRepeatRule && Constants.LOG_VERBOSE ) {
 					Log.v(TAG, "Managed to build a duplicate list of dates by now" );
 					try {
 						throw new Exception("fake");
 					}
 					catch( Exception e ) {
 						Log.v(TAG, Log.getStackTraceString(e) );
 					}
 				}
 			}
 		}
 
 		if ( Constants.debugRepeatRule && Constants.LOG_VERBOSE )
 			Log.v(TAG, "Instances at "+whereAmI+" ["+debugDates.length+"]: "+dateList );
 	}
 
 */
 
 }
