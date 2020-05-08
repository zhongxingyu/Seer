 /*
     Copyright 2008 San Jose State University
     
     This file is part of the Blackberry Cinequest client.
 
     The Blackberry Cinequest client is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     The Blackberry Cinequest client is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with the Blackberry Cinequest client.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.sjsu.cinequest.comm.cinequestitem;
 
 import net.rim.device.api.util.Persistable;
 
 /**
  * Schedule is a class that aggregates one (or more) ProgramItems, with
  * associated meta-info.
  * 
  * @author Kevin Ross (cs160_109)
  * 
  * @version 0.1
  */
 
 public class Schedule implements Persistable {
 	/**
 	 * @return the venue
 	 */
 
 	public String getVenue() {
 		return venue;
 	}
 
 	/**
 	 * @param venue
 	 *            the venue to set
 	 */
 	public void setVenue(String venue) {
 		this.venue = venue;
 	}
 
 	/**
 	 * @return the showingTime
 	 */
 	public String getStartTime() {
 		return startTime;
 	}
 
 	/**
 	 * @param showingTime
 	 *            the showingTime to set
 	 */
 	public void setStartTime(String showingTime) {
 		this.startTime = showingTime;
 	}
 
 	public void setEndTime(String endTime) {
 		this.endTime = endTime;
 	}
 
 	public String getEndTime() {
 		return endTime;
 	}
 
 	public int getItemId() {
 		return itemId;
 	}
 
 	public void setItemId(int programItemId) {
 		this.itemId = programItemId;
 	}
 
 	/**
 	 * @return true if this item needs to be retrieved with a "mobile_item"
 	 *         query
 	 */
 	public boolean isMobileItem() {
 		return mobileItem;
 	}
 
 	public void setMobileItem(boolean special) {
 		this.mobileItem = special;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		if (title == null)
 			title = "(No title)";
 		this.title = title;
 	}
 
 	/**
 	 * @return true if this is a special item that should be highlighted in a
 	 *         list of schedules
 	 */
 	public boolean isSpecialItem() {
 		return specialItem;
 	}
 
 	public void setSpecialItem(boolean specialItem) {
 		this.specialItem = specialItem;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public boolean overlaps(Schedule other) {
 		// The intersection of [s1,e1) and [s2,e2) is [max(s1,s2),min(e1,e2))
 		if (startTime == null || other.startTime == null)
 			return false;
 		if (endTime == null || other.endTime == null)
 			return false;
 		String maxStart = startTime.compareTo(other.startTime) > 0 ? startTime
 				: other.startTime;
 		String minEnd = endTime.compareTo(other.endTime) < 0 ? endTime
 				: other.endTime;
 		return maxStart.compareTo(minEnd) < 0;
 	}
 
 	private int id;
 	private String title;
 	private boolean mobileItem;
 	private boolean specialItem;
 	private int itemId;
 	private String venue;
 	private String startTime;
 	private String endTime;
 }
