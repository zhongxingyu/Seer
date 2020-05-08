 package com.delin.speedlogger.Utils;
 
 import android.location.Location;
 
 public class Logger {
 	public static String LocToStr(Location loc) {
 		return  "Latitude: " + Double.toString(loc.getLatitude()) + 
 				"\nLongitude: " + Double.toString(loc.getLongitude()) +
     			"\nProvider: " + loc.getProvider() + 
     			"\nAccuracy (m): " + loc.getAccuracy() + 
    			"\nSpeed (kph): " + Double.toString(Converter.ms2kph(loc.getSpeed())) +
     			"\nAltitude (m): " + loc.getAltitude() + 
     			"\nBearing: " + loc.getBearing() + 
     			"\nTime: " + loc.getTime();
 	}
 }
