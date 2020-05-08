 package com.aerodynelabs.habtk.atmosphere;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 
 /**
  * A collection of AtmosphereState representing the atmosphere at a fixed time.
  * Allows interpolation between the stored values and extrapolation beyond them 
  * according to the 1976 International Standard Atmosphere.
  * 
  * @author Ethan Harstad
  */
 public class AtmosphereProfile {
 	
 	protected long startTime = 0;
 	protected long endTime;
 	protected double lat;
 	protected double lon;
 	protected double resolution = 0.0;
 	
 	protected ArrayList<AtmosphereState> data;
 	protected ArrayList<AtmosphereState> roc;
 	
 	private static final double R = 8.31432;
 	private static final double MW = 0.0289644;
 	private static final double G = 9.080665;
 	
 	/**
 	 * Creates an atmosphere profile that has no associated time or location information.
 	 */
 	public AtmosphereProfile() {
 		this(0, 0);
 	}
 	
 	/**
 	 * Creates an atmosphere profile starting at the given time and ending three hours later with no associated location.
 	 * @param startTime Time in seconds
 	 */
 	public AtmosphereProfile(int startTime) {
 		this(startTime, startTime + 10800);
 	}
 	
 	/**
 	 * Creates an atmosphere profile representing the time between start and end with no associated location.
 	 * @param startTime	Start time in seconds
 	 * @param endTime	End time in seconds
 	 */
 	public AtmosphereProfile(int startTime, int endTime) {
 		this.startTime = startTime;
 		this.endTime = endTime;
 		
 		data = new ArrayList<AtmosphereState>(10);
 		roc = new ArrayList<AtmosphereState>(10);
 	}
 	
 	/**
 	 * Get number of data points in the profile.
 	 * @return
 	 */
 	public int size() {
 		return data.size();
 	}
 	
 	/**
 	 * Get the i-th data point in the profile.
 	 * @param i
 	 * @return
 	 */
 	public AtmosphereState get(int i) {
 		return data.get(i);
 	}
 	
 	/**
 	 * Associates the given location with the atmosphere profile.
 	 * @param lat Latitude in signed degrees
 	 * @param lon Longitude in signed degrees
 	 */
 	public void setCenter(double lat, double lon) {
 		this.lat = lat;
 		this.lon = lon;
 		resolution = 0.5;
 	}
 	
 	/**
 	 * Associates the given location and resolution with the profile.
 	 * @param lat
 	 * @param lon
 	 * @param resolution
 	 */
 	public void setCenter(double lat, double lon, double resolution) {
 		this.lat = lat;
 		this.lon = lon;
 		this.resolution = resolution;
 	}
 	
 	/**
 	 * Get the associated latitude.
 	 * @return
 	 */
 	public double getLat() {
 		return lat;
 	}
 	
 	/**
 	 * Get the associated longitude
 	 * @return
 	 */
 	public double getLon() {
 		return lon;
 	}
 	
 	/**
 	 * Test if the given time is within this profiles timeframe.
 	 * @param time
 	 * @return
 	 */
 	public boolean isValid(long time) {
 		if(time < startTime) return false;
 		if(time > endTime) return false;
 		return true;
 	}
 	
 	/**
 	 * Test if the given time and location are within this profiles defined values.
 	 * @param time
 	 * @param lat
 	 * @param lon
 	 * @return
 	 */
 	public boolean isValid(long time, double lat, double lon) {
 		if(time < startTime) return false;
 		if(time > endTime) return false;
 		if(resolution == 0.0) return true;
 		if(Math.abs(this.lat - lat) > resolution) return false;
 		if(Math.abs(this.lon - lon) > resolution) return false;
 		return true;
 	}
 	
 	/**
 	 * Get an iterator over the stored AtmosphereState objects.
 	 * @return
 	 */
 	public Iterator<AtmosphereState> iterator() {
 		return data.iterator();
 	}
 	
 	/**
 	 * Add a state to the profile
 	 * @param altitude
 	 * @param pressure
 	 * @param temperature
 	 * @param dewPoint
 	 * @param windDirection
 	 * @param windSpeed
 	 */
 	public void addData(double altitude, double pressure, double temperature, double dewPoint, double windDirection, double windSpeed) {
 		// Add the state
 		data.add(new AtmosphereState(altitude, pressure, temperature, dewPoint, windDirection, windSpeed));
 		// Sort with ascending altitude
 		Collections.sort(data);
 		
 		// Compute new rates of change
 		roc.clear();	// Start over
 		Iterator<AtmosphereState> itr = data.iterator();
 		AtmosphereState cur = itr.next();
 		while(itr.hasNext()) {	// Iterate over the data points
 			// Compute changes between this and previous data points
 			AtmosphereState next = itr.next();
 			double dh = next.h - cur.h;
 			double dp = next.p - cur.p;
 			double dt = next.t - cur.t;
 			double dd = next.dp - cur.dp;
 			double ds = next.ws - cur.ws;
 			double ddir = next.wd - cur.wd;
 			if(Math.abs(ddir) > 180) {	// Adjust for wind direction wrapping
 				if(ddir > 0) {
 					ddir = ddir - 360;
 				} else {
 					ddir = ddir + 360;
 				}
 			}
 			// Store the rate of change data
 			roc.add(new AtmosphereState(dh, dp/dh, dt/dh, dd/dh, ddir/dh, ds/dh));
 			// Step to next data point
 			cur = next;
 		}
 	}
 	
 	/**
 	 * Get the state of the atmosphere at the given altitude.
 	 * @param alt
 	 * @return
 	 */
 	public AtmosphereState getAtAltitude(double alt) {
 		Iterator<AtmosphereState> itr = data.iterator();
 		AtmosphereState base = itr.next();
		if(alt < base.h) return null;
 		AtmosphereState next = null;
 		int i = -1;
 		while(itr.hasNext()) {	// Iterate over data points
 			next = itr.next();
 			i++;
 			if(alt >= next.h) {	// Find highest base altitude
 				base = next;
 				continue;
 			}
 			
 			// Interpolate values
 			AtmosphereState dd = roc.get(i);
 			double dh = alt - base.h;
 			return new AtmosphereState(
 					alt,
 					dd.p * dh + base.p,
 					dd.t * dh + base.t,
 					dd.dp * dh + base.dp,
 					dd.wd * dh + base.wd,
 					dd.ws * dh + base.ws);
 		}
 
 		// Interpolation could not be found
 		// Standard atmosphere extrapolation
 		//XXX Add wind speed extrapolation
 		//XXX Add dew point extrapolation
 		double Hb = base.h;
 		double Tb = base.t + 273.15;
 		double Pb = base.p;
 		
 		if(Hb < 11000) {
 			if(alt < 11000) {
 				double T = Tb - (0.0065 * (alt - Hb));
 				double P = Pb * Math.pow((Tb - (0.0065*(alt - Hb)))/Tb, -(G * MW)/(R * -0.0065));
 				return new AtmosphereState(alt, P, T - 273.15, base.dp, base.wd, base.ws);
 			}
 			double T = Tb - (0.0065 * (11000 - Hb));
 			double P = Pb * Math.pow((Tb - (0.0065*(11000 - Hb)))/Tb, -(G * MW)/(R * -0.0065));
 			Hb = 11000;
 			Tb = T;
 			Pb = P;
 		}
 		if(Hb < 20000) {
 			if(alt < 20000) {
 				double T = Tb;
 				double P = Pb * Math.exp(-(G*MW*(alt - Hb))/(R*Tb));
 				return new AtmosphereState(alt, P, T - 273.15, base.dp, base.wd, base.ws);
 			}
 			double T = Tb;
 			double P = Pb * Math.exp(-(G*MW*(20000 - Hb))/(R*Tb));
 			Hb = 20000;
 			Tb = T;
 			Pb = P;
 		}
 		if(Hb < 32000) {
 			if(alt < 32000) {
 				double T = Tb + (0.001 * (alt - Hb));
 				double P = Pb * Math.pow((Tb + (0.001*(alt - Hb)))/Tb, -(G * MW)/(R * 0.001));
 				return new AtmosphereState(alt, P, T - 273.15, base.dp, base.wd, base.ws);
 			}
 			double T = Tb + (0.001 * (32000 - Hb));
 			double P = Pb * Math.pow((Tb + (0.001*(32000 - Hb)))/Tb, -(G * MW)/(R * 0.001));
 			Hb = 32000;
 			Tb = T;
 			Pb = P;
 		}
 		if(Hb < 47000) {
 			if(alt < 47000) {
 				double T = Tb + (0.0028 * (alt - Hb));
 				double P = Pb * Math.pow((Tb + (0.0028*(alt - Hb)))/Tb, -(G * MW)/(R * 0.0028));
 				return new AtmosphereState(alt, P, T - 273.15, base.dp, base.wd, base.ws);
 			}
 			double T = Tb + (0.0028 * (47000 - Hb));
 			double P = Pb * Math.pow((Tb + (0.0028*(47000 - Hb)))/Tb, -(G * MW)/(R * 0.0028));
 			Hb = 47000;
 			Tb = T;
 			Pb = P;
 		}
 		
 		// Could not extrapolate
 		return null;
 	}
 	
 	@SuppressWarnings("unused")
 	private double getGeopotential(double hg) {
 		double RE = 63567660;
 		return (hg * RE) / (RE + hg);
 	}
 	
 	@SuppressWarnings("unused")
 	private double getGeometric(double hp) {
 		double RE = 63567660;
 		return (hp * RE) / (RE - hp);
 	}
 	
 	/**
 	 * Get a human readable representation of this profile.
 	 */
 	public String toString() {
 		String ret = "Profile for " + lat + ", " + lon + " @ " + startTime + "\n";
 		Iterator<AtmosphereState> itr = data.iterator();
 		while(itr.hasNext()) {
 			ret += itr.next().toString() + "\n";
 		}
 		return ret;
 	}
 
 	public Date getTime() {
 		return new Date(startTime * 1000);
 	}
 
 }
