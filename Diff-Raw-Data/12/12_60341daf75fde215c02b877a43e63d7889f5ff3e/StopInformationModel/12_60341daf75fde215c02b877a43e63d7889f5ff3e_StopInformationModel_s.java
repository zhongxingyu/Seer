 package com.mbcdev.nextluas.model;
 
 import java.text.NumberFormat;
 
 import android.location.Location;
 
 public class StopInformationModel {
 
 	private String displayName;
 	
 	private String suffix;
 	
 	private Location location;
 	
 	private float distanceFromCurrent;
 	
 	private static NumberFormat numberFormat = NumberFormat.getInstance();
 	
 	static {
 		numberFormat.setMaximumFractionDigits(1);
 		numberFormat.setMinimumFractionDigits(1);
 	}
 
 	private int stopNumber;
 
 	public float getDistanceFromCurrent() {
 		return distanceFromCurrent;
 	}
 
 	public void setDistanceFromCurrent(float distanceFromCurrent) {
 		this.distanceFromCurrent = distanceFromCurrent;
 	}
 
 	public String getDisplayName() {
 		return displayName;
 	}
 
 	public void setDisplayName(String displayName) {
 		this.displayName = displayName;
 	}
 
 	public String getSuffix() {
 		return suffix;
 	}
 
 	public void setSuffix(String suffix) {
 		this.suffix = suffix;
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 	
 	public StopInformationModel(String displayName, Location location, int stopNumber) {
 		super();
 		this.displayName = displayName;
 		this.location = location;
 		this.suffix = getSuffix(displayName);
 		this.stopNumber = stopNumber;		
 	}
 		
 	public String toString() {
 		if (this.location != null) {
 			return getFormattedDistance() + this.displayName; 
 		}
 		
 		return this.displayName;
 	}
 	
 	private String getSuffix(String display) {
 		return
 			display.replaceAll(" ", "+")
 			.replaceAll("'", "%27")
			.replaceAll("á", "%26aacute%3B");
 		
 	}
 	
 	private String getFormattedDistance() {
 		
 		if (distanceFromCurrent == 0) {
 			return "";
 		}
 		
 		String space;
 
 		if (distanceFromCurrent < 10) {
 			space = "      ";
 		} else {
 			space = "    ";
 		}
 		
 		return numberFormat.format(distanceFromCurrent) + "k" + space;
 		
 		
 	}
 
 	public int getStopNumber() {
 		return stopNumber;
 	}
 
 	public void setStopNumber(int stopNumber) {
 		this.stopNumber = stopNumber;
 	}
 
 }
