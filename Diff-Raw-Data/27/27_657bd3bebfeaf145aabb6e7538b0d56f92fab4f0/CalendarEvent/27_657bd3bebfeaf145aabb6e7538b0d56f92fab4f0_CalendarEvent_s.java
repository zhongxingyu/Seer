 package com.aldaviva.dreamstats.data.model;
 
 import com.aldaviva.dreamstats.data.enums.EventName;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
import com.google.api.services.calendar.model.Event;

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
		start = new DateTime(googleCalendarEvent.getStart().getDateTime().getValue());
		end = new DateTime(googleCalendarEvent.getEnd().getDateTime().getValue());
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
 }
