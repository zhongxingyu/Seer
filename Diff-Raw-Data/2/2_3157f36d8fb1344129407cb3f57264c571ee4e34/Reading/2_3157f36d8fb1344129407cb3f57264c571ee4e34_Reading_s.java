 package com.jamierf.powermeter;
 
 
 public class Reading {
 
 	private int sensor;
 	private double temperature;
 	private int watts;
 
 	public Reading(int sensor, double temperature, int watts) {
 		this.sensor = sensor;
 		this.temperature = temperature;
 		this.watts = watts;
 	}
 
 	public int getSensor() {
 		return sensor;
 	}
 
 	public double getTemperature() {
 		return temperature;
 	}
 
 	public int getWatts() {
 		return watts;
 	}
 
 	@Override
 	public String toString() {
		return String.format("sensor: %d, temp: %.1f, watts: %d", temperature, watts);
 	}
 }
