 package com.ezgliding.igc;
 
 public class Fix {
 	
 	public int time;
 	private double lat, latrd;
 	private double lon, lonrd;
 	public int pressureAlt;
 	public int gnssAlt;
 	public char validity;
 
 	public Fix(int time, double lat, double lon, int pressureAlt, int gnssAlt, char validity) {
 		this.time = time;
 		setLat(lat);
 		setLon(lon);
 		this.pressureAlt = pressureAlt;
 		this.gnssAlt = gnssAlt;
 		this.validity = validity;
 	}
 
 	public void setLat(double degrees) {
 		lat = degrees;
 		latrd = Math.toRadians(lat);
 	}
 
 	public void setLon(double degrees) {
 		lon = degrees;
 		lonrd = Math.toRadians(lon);
 	}
 
 	public double lat() { return lat; }
 	
 	public double latrd() { return latrd; }
 
 	public double lon() { return lon; }
 
 	public double lonrd() { return lonrd; }
 
 	/**
 	 * Like equal(), but ignoring the date, and usage of pressureAlt is optional.
 	 */
 	public boolean equivalent(Fix other, boolean withAlt) {
 		if (other == null) return false;
 
 		if (latrd != other.latrd || lonrd != other.lonrd)
 			return false;
 		if (withAlt && pressureAlt != other.pressureAlt)
 			return false;
 		return true;
 	}
 
 	@Override
	public int hashCode() {
		double result = 17;
		result = result * 37 + latrd();
		result = result * 37 + lonrd();
		result = result * 37 + pressureAlt;
		result = result * 37 + gnssAlt;
		result = result * 37 + validity;
		return (int)result;
	}

	@Override
 	public boolean equals(Object otherO) {
 		if (otherO == null) return false;
 
 		Fix other;
 		try {
 			other = (Fix)otherO;
 		} catch(ClassCastException e) { return false; }
 
 		if (time != other.time || latrd != other.latrd || lonrd != other.lonrd
 			|| pressureAlt != other.pressureAlt || gnssAlt != other.gnssAlt
 			|| validity != other.validity) 
 			return false;
 		
 		return true;
 	}
 
 	@Override
 	public Fix clone() {
 		return new Fix(time, lat, lon, pressureAlt, gnssAlt, validity);
 	}
 
 	@Override
 	public String toString() {
 		return "{" + time + "," + String.format("%1$,.2f",lat()) 
 			+ ":" + String.format("%1$,.2f",latrd()) + "," + String.format("%1$,.2f",lon()) 
 			+ ":" + String.format("%1$,.2f",lonrd()) + "," + pressureAlt + ":" + gnssAlt + "," + validity + "}"; 
 	}
 }
