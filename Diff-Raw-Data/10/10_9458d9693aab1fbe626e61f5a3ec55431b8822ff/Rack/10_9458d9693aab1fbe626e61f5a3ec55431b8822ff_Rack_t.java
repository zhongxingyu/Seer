 /**
  *   Copyright (C) 2010, Roger Kind Kristiansen <roger@kind-kristiansen.no>
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package no.rkkc.bysykkel.model;
 
 import com.google.android.maps.GeoPoint;
 
 public class Rack {
 	private Integer id;
 	private Boolean online;
 	private Integer emptyLocks;
 	private Integer readyBikes;
 	private String description;
 	private GeoPoint location;
 	
 	public Rack(int id, String description, Integer latitude, 
 			Integer longitude, Boolean online, 
 			Integer emptyLocks, Integer readyBikes) {
 		
 		this.id = id;
 		this.online = online;
 		this.description = description;
 		if (latitude != null && longitude != null) {
 			this.location = new GeoPoint(latitude.intValue(), 
 											longitude.intValue());
 		}
 		
 		this.emptyLocks = emptyLocks;
 		this.readyBikes = readyBikes;
 	}
 	
 	public Rack(int id, String description, Integer latitude, Integer longitude) {
 		this(id, description, latitude, longitude, null, null, null);
 	}
 	
 
 	public int getId() {
 		return id;
 	}
 
 	public boolean isOnline() {
 		return (online == null) ? false : online;
 	}
 
 	public boolean hasInfoOnEmptyLocks() {
 		return (emptyLocks == null) ? false : true;
 	}
 	
 	public int getNumberOfEmptySlots() {
 		return emptyLocks;
 	}
 	
 	public boolean hasInfoOnReadyBikes() {
 		return (readyBikes == null) ? false : true;
 	}
 
 	public int getNumberOfReadyBikes() {
 		return readyBikes;
 	}
 	
 	public boolean hasDescription() {
 		return (description == null) ? false : true;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public boolean hasLocationInfo() {
 		return (location == null) ? false : true;
 	}
 	
 	public GeoPoint getLocation() {
 		return location;
 	}
 	
	public boolean hasBikeAndSlotInfo() {
		return hasInfoOnReadyBikes() && hasInfoOnEmptyLocks();
	}
	
 	public String toString() {
 		return getId()+": "+getDescription();
 	}
 	
 }
