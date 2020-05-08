 /*
  * Copyright (C) 2013 UniCoPA
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
  */
 package unicopa.copa.base.event;
 
 import java.util.Date;
 
 /**
  * A single occurence (fixed date) of an Event.
  * 
  * @author Felix Wiemuth
  */
 public class SingleEvent {
 
     private int singleEventID;
     private int eventID;
     private String location;
     private Date date;
     private String supervisor;
     private int durationMinutes;
 
     /**
      * Create a new SingleEvent object.
      * 
      * @param singleEventID
      * @param eventID
      * @param location
      * @param date
      * @param supervisor
      * @param durationMinutes
      */
     public SingleEvent(int singleEventID, int eventID, String location,
 	    Date date, String supervisor, int durationMinutes) {
 	this.singleEventID = singleEventID;
 	this.eventID = eventID;
 	this.location = location;
 	this.date = date;
 	this.supervisor = supervisor;
 	this.durationMinutes = durationMinutes;
     }
	
	public SingleEvent(Long date) {
	this.date = new Date(date);
    }
	
	public SingleEvent() {

    }
 
     /**
      * Get the ID of this fixed date. It is guaranteed to be unique under all
      * available SingleEvent objects in use of the system.
      * 
      * @return
      */
     public int getSingleEventID() {
 	return singleEventID;
     }
 
     /**
      * Get the ID of the event this fixed date belongs to.
      * 
      * @return
      */
     public int getEventID() {
 	return eventID;
     }
 
     /**
      * Get the location of this fixed date.
      * 
      * @return
      */
     public String getLocation() {
 	return location;
     }
 
     /**
      * Get the starting time of this fixed date.
      * 
      * @return
      */
     public Date getDate() {
 	return new Date(date.getTime());
     }
 
     /**
      * Get the name of the responsible for this fixed date. The person does not
      * have to be a user of the system and may be independent from OWNERs and
      * RIGHTHOLDERs.
      * 
      * @return
      */
     public String getSupervisor() {
 	return supervisor;
     }
 
     /**
      * Get the duration of this fixed date.
      * 
      * @return
      */
     public int getDurationMinutes() {
 	return durationMinutes;
     }
 }
