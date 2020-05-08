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
 
 package com.morphoss.acal.davacal;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 import com.morphoss.acal.Constants;
 import com.morphoss.acal.acaltime.AcalDateRange;
 import com.morphoss.acal.acaltime.AcalDateTime;
 import com.morphoss.acal.acaltime.AcalRepeatRule;
 import com.morphoss.acal.database.cachemanager.CacheObject;
 import com.morphoss.acal.dataservice.EventInstance;
 import com.morphoss.acal.dataservice.Resource;
 
 public class VCalendar extends VComponent {
 	public static final String TAG = "aCal VCalendar";
 	private AcalDateRange dateRange = null;
 	private AcalRepeatRule repeatRule = null;
 	private Boolean masterHasOverrides = null;
 	private Boolean hasAlarms = null;
 	private Boolean hasRepeatRule = null;
 	private Long earliestStart;
 	private Long latestEnd;
 	private boolean isPending = false;
 	public static final Pattern tzOlsonExtractor = Pattern.compile(".*((?:Antarctica|America|Africa|Atlantic|Asia|Australia|Indian|Europe|Pacific|US)/(?:(?:[^/\"]+)/)?[^/\"]+)\"?");
 	private static final String	TZNAME_UTC	= "UTC";
 
 
 	public VCalendar(ComponentParts splitter, Resource r, Long earliestStart, Long latestEnd,VComponent parent) {
 		super(splitter, r,parent);
 		this.earliestStart = earliestStart;
 		this.latestEnd = latestEnd;
 		if ( earliestStart != null ) {
 			this.dateRange = new AcalDateRange(AcalDateTime.fromMillis(earliestStart),
 					(latestEnd == null ? null : AcalDateTime.fromMillis(latestEnd)));
 		}
 	}
 
 
 	protected VCalendar(long collectionId) {
 		super( VComponent.VCALENDAR, collectionId, null );
 		try { setPersistentOn(); } catch (YouMustSurroundThisMethodInTryCatchOrIllEatYouException e) { }
 		addProperty(new AcalProperty("CALSCALE","GREGORIAN"));
 		addProperty(new AcalProperty("PRODID","-//morphoss.com//aCal 1.0//EN"));
 		addProperty(new AcalProperty("VERSION","2.0"));
 	}
 
 
 	public static VCalendar getGenericCalendar( long collectionId, EventInstance newEventData) {
 		VCalendar vcal = new VCalendar(collectionId);
 		new VEvent(vcal);
 		return vcal;
 	}
 
 	public void setPending(boolean isPending) {
 		this.isPending = isPending;
 	}
 	public boolean isPending() {
 		return this.isPending;
 	}
 	
 	public VCalendar clone() {
 		return new VCalendar(this.content, this.getResource(), this.earliestStart, this.latestEnd, this.parent);
 	}
 
 	public String applyEventAction(EventInstance action) {
 		//TODO
 		//needs to be refactored
 		/**try {
 			this.setEditable();
 
 			VEvent vEvent = (VEvent) this.getMasterChild();
 
 			// first, strip any existing properties which we always modify
 			vEvent.removeProperties( new PropertyName[] {PropertyName.DTSTAMP, PropertyName.LAST_MODIFIED } );
 
 			// change DTStamp
 			AcalDateTime lastModified = new AcalDateTime();
 			lastModified.setTimeZone(TimeZone.getDefault().getID());
 			lastModified.shiftTimeZone("UTC");
 
 			vEvent.addProperty(AcalProperty.fromString(lastModified.toPropertyString(PropertyName.DTSTAMP)));
 			vEvent.addProperty(AcalProperty.fromString(lastModified.toPropertyString(PropertyName.LAST_MODIFIED)));
 
 			if ( action.getAction() == WriteableEventInstance.ACTION_DELETE_SINGLE) {
 				AcalProperty exDate = vEvent.getProperty("EXDATE");
 				if ( exDate == null || exDate.getValue().equals("") ) 
 					exDate = AcalProperty.fromString(action.getStart().toPropertyString(PropertyName.EXDATE));
 				else {
 					vEvent.removeProperties( new PropertyName[] {PropertyName.EXDATE} );
 					exDate = AcalProperty.fromString(exDate.toRfcString() + "," + action.getStart().fmtIcal() );
 				}
 				vEvent.addProperty(exDate);
 			}
 			else if (action.getAction() ==WriteableEventInstance.ACTION_DELETE_ALL_FUTURE) {
 				AcalRepeatRuleParser parsedRule = AcalRepeatRuleParser.parseRepeatRule(action.getRRule());
 				AcalDateTime until = action.getStart().clone();
 				until.addSeconds(-1);
 				parsedRule.setUntil(until);
 				String rrule = parsedRule.toString();
 				action.setRepetition(rrule);
 				vEvent.removeProperties( new PropertyName[] {PropertyName.RRULE} );
 				vEvent.addProperty(new AcalProperty("RRULE",rrule));
 			}
 			else if (action.isModifyAction()) {
 				this.applyModify(vEvent,action);
 				this.updateTimeZones(vEvent);
 			}
 
 			String ret = this.getCurrentBlob();
 			return ret;
 		}
 		catch (Exception e) {
 			Log.w(TAG,Log.getStackTraceString(e));
 			return "";
 		}
 	*/
 		return null;
 	}
 
 	/**
 	 * TODO - needs to be refactored
 	 */
 	private void applyModify(Masterable mast, EventInstance action) {
 		/**
 		//there are 3 possible modify actions:
 		if (action.getAction() == WriteableEventInstance.ACTION_MODIFY_SINGLE) {
 			// Only modify the single instance
 		}
 		else if (action.getAction() == WriteableEventInstance.ACTION_MODIFY_ALL_FUTURE) {
 			// Modify this instance, and all future instances.
 
 		}
 		else if (action.getAction() == WriteableEventInstance.ACTION_MODIFY_ALL) {
 			// Modify all instances
 
 			// First, strip any existing properties which we modify
 			mast.removeProperties( new PropertyName[] {PropertyName.DTSTART, PropertyName.DTEND, PropertyName.DURATION,
 					PropertyName.SUMMARY, PropertyName.LOCATION, PropertyName.DESCRIPTION, PropertyName.RRULE } );
 
 			AcalDateTime dtStart = action.getStart();
 			mast.addProperty( dtStart.asProperty(PropertyName.DTSTART));
 
 			AcalDateTime dtEnd = action.getEnd();
 			if ( (dtEnd.getTimeZoneId() == null && dtStart.getTimeZoneId() == null) || 
 					(dtEnd.getTimeZoneId() != null && dtEnd.getTimeZoneId().equals(dtStart.getTimeZoneId())) )
 				mast.addProperty(action.getDuration().asProperty(PropertyName.DURATION) );
 			else
 				mast.addProperty( dtEnd.asProperty( PropertyName.DTEND ) );
 
 			mast.addProperty(new AcalProperty(PropertyName.SUMMARY, action.getSummary()));
 
 			String location = action.getLocation();
 			if ( !location.equals("") )
 				mast.addProperty(new AcalProperty(PropertyName.LOCATION,location));
 
 			String description = action.getDescription();
 			if ( !description.equals("") )
 				mast.addProperty(new AcalProperty(PropertyName.DESCRIPTION,description));
 
 			String rrule = action.getRRule();
 			if ( rrule != null && !rrule.equals(""))
 				mast.addProperty(new AcalProperty(PropertyName.RRULE,rrule));
 
 			mast.updateAlarmComponents( action.getAlarms() );
 		}
 		*/
 	}
 
 	private void updateTimeZones(VEvent vEvent) {
 		HashSet<String> tzIdSet = new HashSet<String>();
 		for( PropertyName pn : PropertyName.localisableDateProperties() ) {
 			AcalProperty p = vEvent.getProperty(pn);
 			if ( p != null ) {
 				String tzId = p.getParam("TZID");
 				if ( tzId != null ) {
 					tzIdSet.add(p.getParam("TZID"));
 					if ( Constants.LOG_DEBUG )
 						Log.println(Constants.LOGD,TAG,"Found reference to timezone '"+tzId+"' in event.");
 				}
 			}
 		}
 	
 		List<VComponent> removeChildren = new ArrayList<VComponent>();
 		for (VComponent child : getChildren() ) {
 			if ( child.name.equals(VComponent.VTIMEZONE) ) {
 				String tzId = null;
 				try {
 					VTimezone vtz = (VTimezone) child;
 					tzId = vtz.getTZID();
 				}
 				catch( Exception e ) {};
 				if ( tzIdSet.contains(tzId) ) {
 					if ( Constants.LOG_DEBUG )
 						Log.println(Constants.LOGD,TAG,"Found child vtimezone for '"+tzId+"' in event.");
 					tzIdSet.remove(tzId);
 				}
 				else {
 					if ( Constants.LOG_DEBUG )
 						Log.println(Constants.LOGD,TAG,"Removing vtimezone for '"+tzId+"' from event.");
 					removeChildren.add(child);
 				}
 			}
 			else {
 				if ( Constants.LOG_DEBUG )
 					Log.println(Constants.LOGD,TAG,"Found "+child.name+" component in event.");
 			}
 		}
 		// Have to avoid the concurrent modification
 		for(VComponent child : removeChildren ) {
 			this.removeChild(child);
 		}
 	
 		for ( String tzId : tzIdSet ) {
 			VTimezone vtz;
 			try {
 				String tzBlob = VTimezone.getZoneDefinition(tzId);
 				if ( Constants.LOG_DEBUG ) {
 					Log.println(Constants.LOGD,TAG,"New timezone for '"+tzId+"'");
 					Log.println(Constants.LOGD,TAG,tzBlob);
 				}
 				vtz = (VTimezone) VComponent.createComponentFromBlob(tzBlob, null);
 				vtz.setEditable();
 				this.addChild(vtz);
 			}
 			catch ( UnrecognizedTimeZoneException e ) {
 				Log.i(TAG,"Unable to build a timezone for '"+tzId+"'");
 			}
 			
 		}
 	}
 
 
 	public void checkRepeatRule() {
 		try {
 			if (repeatRule == null) repeatRule = AcalRepeatRule.fromVCalendar(this);
 		}
 		catch ( Exception e ) {
 			Log.e(TAG,"Exception getting repeat rule from VCalendar", e);
 		}
 		hasRepeatRule = ( repeatRule != null );
 	}
 
 	private boolean appendAlarmInstancesBetween(List<AcalAlarm> alarmList, AcalDateRange rangeRequested) {
 		if ( hasRepeatRule == null && repeatRule == null ) checkRepeatRule();
 		if ( !hasRepeatRule ) return false;
 		this.repeatRule.appendAlarmInstancesBetween(alarmList, rangeRequested);
 		return true;
 	}
 
 	
 	public boolean appendCacheEventInstancesBetween(List<CacheObject> eventList, AcalDateRange rangeRequested ) {
 		try {
 			Masterable m = getMasterChild();
 			if ( m != null ) {
 				AcalProperty rProperty = m.getProperty(PropertyName.RRULE);
 				if ( rProperty == null )
 					rProperty = m.getProperty(PropertyName.RDATE);
 				if ( rProperty == null ) {
 					Log.println(Constants.LOGD,TAG, "Individual instance CacheObject being created.");
 					eventList.add(new CacheObject(m));
 					return true;
 				}
 				else {
 					Log.println(Constants.LOGD,TAG, "Building CacheObject instances from RepeatRule.");
 
 					if ( dateRange != null ) {
 						AcalDateRange intersection = rangeRequested.getIntersection(this.dateRange);
 						if ( intersection != null ) {
 							if ( hasRepeatRule == null && repeatRule == null ) checkRepeatRule();
 							if ( hasRepeatRule ) {
 								// Log.d(TAG,"Processing event: Summary="+new
 								// UnModifiableAcalEvent(thisEvent,new AcalCalendar(),
 								// new AcalCalendar()).summary);
 								this.repeatRule.appendCacheEventInstancesBetween(eventList, intersection);
 								return true;
 							}
 						}
						else {
							checkRepeatRule();
							if ( repeatRule != null ) {
								this.repeatRule.appendCacheEventInstancesBetween(eventList, rangeRequested);
								return true;
							}
 						}
 					}
 				}
 			}
 			else
 				Log.println(Constants.LOGD,TAG, "VCalendar master instance was null for "+getEffectiveType());
 					
 		}
 		catch(Exception e) {
 			if (Constants.LOG_DEBUG)Log.d(TAG,"Exception in RepeatRule handling");
 			if (Constants.LOG_DEBUG)Log.d(TAG,Log.getStackTraceString(e));
 		}
 		return false;
 	}
 
 	public Masterable getMasterChild() {
 		if (childrenSet) {
 			for (VComponent vc : this.getChildren()) {
 				if ( vc instanceof VEvent)   	return (VEvent)vc;
 				if ( vc instanceof VTodo )		return (VTodo) vc;
 				if ( vc instanceof VJournal )	return (VJournal) vc;
 			}
 		}
 		for( PartInfo childInfo : content.partInfo ) {
 			if ( childInfo.type.equals(VEVENT) ) {
 				return new VEvent(new ComponentParts(childInfo.getComponent(content.componentString)),
 						resource, this);
 			}
 			else if ( childInfo.type.equals(VTODO) ) {
 				return new VTodo(new ComponentParts(childInfo.getComponent(content.componentString)),
 						resource, this);
 			}
 			else if ( childInfo.type.equals(VJOURNAL) ) {
 				return new VJournal(new ComponentParts(childInfo.getComponent(content.componentString)),
 						resource, this);
 			}
 		}
 		return null;
 	}
 
 	public AcalDateTime getRangeEnd() {
 		if ( dateRange == null ) return null;
 		return dateRange.end;
 	}
 
 	@Override
 	public String getEffectiveType() {
 		Masterable masterInstance =  this.getMasterChild();
 		if ( masterInstance != null ) return masterInstance.getEffectiveType();
 		if ( childrenSet && getChildren().size() > 0) {
 			VComponent vc = getChildren().get(0);
 			return vc.name;
 		}
 		if ( content.partInfo.size() > 0 ) {
 			return content.partInfo.get(0).type;
 		}
 		return name;
 	}
 
 
 	public boolean masterHasOverrides() {
 		if ( masterHasOverrides == null ) {
 			int countMasterables = 0;
 			for( PartInfo childInfo : content.partInfo ) {
 				if ( childInfo.type.equals(VEVENT) || childInfo.type.equals(VTODO) || childInfo.type.equals(VJOURNAL) ) {
 					countMasterables++;
 					if ( countMasterables > 1 ) break;
 				}
 			}
 			if ( masterHasOverrides == null ) masterHasOverrides = (countMasterables > 1);
 		}
 		return masterHasOverrides;
 	}
 
 
 	public Masterable getChildFromRecurrenceId(RecurrenceId recurrenceProperty) {
		if ( masterHasOverrides() ) return this.getMasterChild();
 		try {
 			this.setPersistentOn();
 			List<Masterable> matchingChildren = new ArrayList<Masterable>();
 			for (VComponent vc: this.getChildren()) {
 				if (vc.containsPropertyKey(recurrenceProperty.getName()) && vc instanceof Masterable)
 					matchingChildren.add((Masterable) vc);
 			}
 			if (matchingChildren.isEmpty()) {
 				// Won't happen since we test for this in masterHasOverrides()
 				return this.getMasterChild();
 			}
 			Collections.sort(matchingChildren, RecurrenceId.getVComponentComparatorByRecurrenceId());
 			for (int i = matchingChildren.size()-1; i>= 0; i--) {
 				RecurrenceId cur = (RecurrenceId) matchingChildren.get(i).getProperty("RECURRENCE-ID");
 				if (cur.notAfter(recurrenceProperty)) {
 					this.setPersistentOff();
 					return matchingChildren.get(i);
 				}
 			}
 		} catch (YouMustSurroundThisMethodInTryCatchOrIllEatYouException e) {
 			Log.w(TAG,Log.getStackTraceString(e));
 		} finally {
 			this.setPersistentOff();	
 		}
 
 		return this.getMasterChild();
 	}
 
 
 	private static String checkKnownAliases( String tzId ) {
 		if ( tzId.equals(TZNAME_UTC) ) return tzId;
 		if ( tzId.equals("GMT") ) return TZNAME_UTC;
 		if ( tzId.equals("Etc/UTC") ) return TZNAME_UTC;
 		if ( tzId.equals("Etc/GMT") ) return TZNAME_UTC;
 		if ( tzId.equals("Eastern Standard Time") || tzId.equals("Eastern Daylight Time")
 				|| tzId.equals("Eastern Time (US & Canada)")
 				|| tzId.equals("GMT -0500 (Standard) / GMT -0400 (Daylight)")
 				) return "America/New_York";
 		if ( tzId.equals("Pacific Standard Time") || tzId.equals("Pacific Daylight Time") ) return "America/Los_Angeles";
 		if ( tzId.equals("Central Standard Time") || tzId.equals("Central Daylight Time") ) return "America/Chicago";
 		if ( tzId.equals("Mountain Standard Time") || tzId.equals("Mountain Daylight Time") ) return "America/Denver";
 		if ( tzId.equals("New Zealand Standard Time") || tzId.equals("New Zealand Daylight Time") ) return "Pacific/Auckland";
 		if ( tzId.equals("Chennai") ) return "Asia/Mumbai";
 		return null;
 	}
 	
 	public static String staticGetOlsonName( String tzId ) {
 		Matcher m = VCalendar.tzOlsonExtractor.matcher(tzId);
 		if ( m.matches() ) {
 			return m.group(1);
 		}
 		String aliasResult = checkKnownAliases(tzId);
 		if ( aliasResult != null ) return aliasResult;
 		Log.w(TAG,"Could not get Olson name from "+tzId, new Exception("Unrecognized Time Zone"));
 		return tzId;
 	}
 
 	
 	public String getOlsonName( String tzId ) {
 		Matcher m = VCalendar.tzOlsonExtractor.matcher(tzId);
 		if ( m.matches() ) {
 			return m.group(1);
 		}
 
 		String aliasResult = checkKnownAliases(tzId);
 		if ( aliasResult != null ) return aliasResult;
 
 		if (childrenSet) {
 			for (VComponent vc : this.getChildren()) {
 				if ( vc instanceof VTimezone ) {
 					if ( vc.getProperty("TZID").getValue() == tzId ) {
 						AcalProperty idProperty = vc.getProperty("X-MICROSOFT-CDO-TZID");
 						if ( idProperty != null && idProperty.getValue() != null ) {
 							switch( Integer.parseInt(idProperty.getValue()) ) {
 								case 0:    return("UTC");
 								case 1:    return("Europe/London");
 								case 2:    return("Europe/Lisbon");
 								case 3:    return("Europe/Paris");
 								case 4:    return("Europe/Berlin");
 								case 5:    return("Europe/Bucharest");
 								case 6:    return("Europe/Prague");
 								case 7:    return("Europe/Athens");
 								case 8:    return("America/Brasilia");
 								case 9:    return("America/Halifax");
 								case 10:   return("America/New_York");
 								case 11:   return("America/Chicago");
 								case 12:   return("America/Denver");
 								case 13:   return("America/Los_Angeles");
 								case 14:   return("America/Anchorage");
 								case 15:   return("Pacific/Honolulu");
 								case 16:   return("Pacific/Apia");
 								case 17:   return("Pacific/Auckland");
 								case 18:   return("Australia/Brisbane");
 								case 19:   return("Australia/Adelaide");
 								case 20:   return("Asia/Tokyo");
 								case 21:   return("Asia/Singapore");
 								case 22:   return("Asia/Bangkok");
 								case 23:   return("Asia/Kolkata");
 								case 24:   return("Asia/Muscat");
 								case 25:   return("Asia/Tehran");
 								case 26:   return("Asia/Baghdad");
 								case 27:   return("Asia/Jerusalem");
 								case 28:   return("America/St_Johns");
 								case 29:   return("Atlantic/Azores");
 								case 30:   return("America/Noronha");
 								case 31:   return("Africa/Casablanca");
 								case 32:   return("America/Argentina/Buenos_Aires");
 								case 33:   return("America/La_Paz");
 								case 34:   return("America/Indiana/Indianapolis");
 								case 35:   return("America/Bogota");
 								case 36:   return("America/Regina");
 								case 37:   return("America/Tegucigalpa");
 								case 38:   return("America/Phoenix");
 								case 39:   return("Pacific/Kwajalein");
 								case 40:   return("Pacific/Fiji");
 								case 41:   return("Asia/Magadan");
 								case 42:   return("Australia/Hobart");
 								case 43:   return("Pacific/Guam");
 								case 44:   return("Australia/Darwin");
 								case 45:   return("Asia/Shanghai");
 								case 46:   return("Asia/Novosibirsk");
 								case 47:   return("Asia/Karachi");
 								case 48:   return("Asia/Kabul");
 								case 49:   return("Africa/Cairo");
 								case 50:   return("Africa/Harare");
 								case 51:   return("Europe/Moscow");
 								case 53:   return("Atlantic/Cape_Verde");
 								case 54:   return("Asia/Yerevan");
 								case 55:   return("America/Panama");
 								case 56:   return("Africa/Nairobi");
 								case 58:   return("Asia/Yekaterinburg");
 								case 59:   return("Europe/Helsinki");
 								case 60:   return("America/Godthab");
 								case 61:   return("Asia/Rangoon");
 								case 62:   return("Asia/Kathmandu");
 								case 63:   return("Asia/Irkutsk");
 								case 64:   return("Asia/Krasnoyarsk");
 								case 65:   return("America/Santiago");
 								case 66:   return("Asia/Colombo");
 								case 67:   return("Pacific/Tongatapu");
 								case 68:   return("Asia/Vladivostok");
 								case 69:   return("Africa/Ndjamena");
 								case 70:   return("Asia/Yakutsk");
 								case 71:   return("Asia/Dhaka");
 								case 72:   return("Asia/Seoul");
 								case 73:   return("Australia/Perth");
 								case 74:   return("Asia/Riyadh");
 								case 75:   return("Asia/Taipei");
 								case 76:   return("Australia/Sydney");
 								
 								case 57: // null
 								case 52: // null
 								default: // null
 							}
 						}
 					}
 				}
 			}
 		}
 
 		Log.w(TAG,"Could not get Olson name from "+tzId, new Exception("Unrecognized Time Zone"));
 
 		/**
 		 * @todo: We should 
 		 */
 		return null; // We failed :-(
 	}
 
 	
 	public boolean hasAlarm() {
 		if ( this.hasAlarms != null ) return this.hasAlarms;
 		for( PartInfo childInfo : content.partInfo ) {
 			if ( childInfo.type.equals(VEVENT) ) {
 				VEvent vc = new VEvent(new ComponentParts(childInfo.getComponent(content.componentString)),
 						resource, this);
 				for( PartInfo childChildInfo : vc.content.partInfo ) {
 					if ( childChildInfo.type.equals(VALARM)) {
 						this.hasAlarms = true;
 						return true;
 					}
 				}
 			}
 			else if ( childInfo.type.equals(VTODO) ) {
 				VTodo vc = new VTodo(new ComponentParts(childInfo.getComponent(content.componentString)),
 						resource, this);
 				for( PartInfo childChildInfo : vc.content.partInfo ) {
 					if ( childChildInfo.type.equals(VALARM))  {
 						this.hasAlarms = true;
 						return true;
 					}
 				}
 			}
 			else if ( childInfo.type.equals(VALARM))  {
 				this.hasAlarms = true;
 				return true;
 			}
 		}
 		this.hasAlarms = false;
 		return false;
 	}
 
 	
 	public VCalendar(Parcel in) {
 		super(in);
 	}
 
 	public static final Parcelable.Creator<VCalendar> CREATOR = new Parcelable.Creator<VCalendar>() {
 		public VCalendar createFromParcel(Parcel in) {
 			return new VCalendar(in);
 		}
 
 		public VCalendar[] newArray(int size) {
 			return new VCalendar[size];
 		}
 	};
 
 }
