 package com.aldaviva.dreamstats.data.model;
 
 import com.aldaviva.dreamstats.data.enums.EventName;
 
 import com.google.api.services.calendar.model.Event;
 import com.google.api.services.calendar.model.EventDateTime;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 
 public class CalendarEvent {
 
 	private DateTime start;
 	private DateTime end;
 	private EventName name;
 
 	public CalendarEvent(){

 	}
 
 	public CalendarEvent(final Event googleCalendarEvent){
 		try {
 			name = EventName.valueOf(googleCalendarEvent.getSummary());
 		} catch (final IllegalArgumentException e){
 			name = EventName.Other;
 		}
 		
 		start = convertRemoteDateToDateTime(googleCalendarEvent.getStart());
 		end = convertRemoteDateToDateTime(googleCalendarEvent.getEnd());
 	}
 
 	public DateTime getStart() {
 		return start;
 	}
 	public void setStart(final DateTime start) {
 		this.start = start;
 	}
 	public DateTime getEnd() {
 		return end;
 	}
 	public void setEnd(final DateTime end) {
 		this.end = end;
 	}
 	public EventName getName() {
 		return name;
 	}
 	public void setName(final EventName name) {
 		this.name = name;
 	}
 
 	@JsonIgnore
 	public Duration getDuration(){
 		return new Duration(start, end);
 	}
 	
 	private static DateTime convertRemoteDateToDateTime(final EventDateTime googleDate) {
 		DateTime result;
 		com.google.api.client.util.DateTime googleDateTime = googleDate.getDateTime();
 		if(googleDateTime != null) {
 			result = new DateTime(googleDateTime.getValue());
 		} else { // all-day event
 			googleDateTime = googleDate.getDate();
 			result = new DateTime(googleDateTime.getValue(), DateTimeZone.forOffsetHours(googleDateTime.getTimeZoneShift())).withZoneRetainFields(DateTimeZone
 			    .getDefault());
 		}
 		return result;
 	}
 }
