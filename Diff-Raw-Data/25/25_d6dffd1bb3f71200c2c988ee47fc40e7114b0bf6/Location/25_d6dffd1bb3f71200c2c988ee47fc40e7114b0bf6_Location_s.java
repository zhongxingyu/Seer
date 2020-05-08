 package com.android.locproof.stamp;
 
 import java.util.LinkedList;
 
 public class Location { 
 	private String _name;
 	private double _longitude;
 	private double _latitude;
	private LinkedList<String> _levels;
 
 	// create and initialize a point with given name and
 	// (latitude, longitude) specified in degrees
 	public Location(String name, double latitude, double longitude) {
 		this._name = name;
 		this._latitude  = latitude;
 		this._longitude = longitude;
 		
		_levels = new LinkedList<String>();
		// Hard coded for now
		_levels.add(name);
		_levels.add("Neighborhood");
		_levels.add("Town/City");
		_levels.add("Region/County");
		_levels.add("State");
		_levels.add("County");
 	}
 	
 	public Location(String locString) {
 		String name = locString.substring(0, locString.indexOf("("));
 		String latString = locString.substring(locString.indexOf("(")+1, locString.indexOf(","));
 		String longString = locString.substring(locString.indexOf(",")+1, locString.indexOf(")"));
 		
 		this._name = name;
 		this._latitude  = Double.valueOf(latString);
 		this._longitude = Double.valueOf(longString);
 	}
 
 	public String getLevel(int index){
 		return _levels.get(index);
 	}
 	
 	public String getFirstLevel(){
 		return this.toString();
 	}
 	
 	public int getLevelCount(){
 		return _levels.size();
 	}
 	
 	// return distance between this location and that location
 	// measured in statute miles
 	public double distanceTo(Location that) {
 		double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
 		double lat1 = Math.toRadians(this._latitude);
 		double lon1 = Math.toRadians(this._longitude);
 		double lat2 = Math.toRadians(that._latitude);
 		double lon2 = Math.toRadians(that._longitude);
 
 		// great circle distance in radians, using law of cosines formula
 		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
 				+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
 
 		// each degree on a great circle of Earth is 60 nautical miles
 		double nauticalMiles = 60 * Math.toDegrees(angle);
 		double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
 		return statuteMiles;
 	}
 
 	// return string representation of this point
 	public String toString() {
 		return _name + " (" + _latitude + ", " + _longitude + ")";
 	}
 
 }
 
