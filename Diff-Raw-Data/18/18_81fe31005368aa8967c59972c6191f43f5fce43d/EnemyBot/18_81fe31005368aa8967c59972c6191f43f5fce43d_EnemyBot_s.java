 package com.jaxelson;
 
 import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
 
 public class EnemyBot {
 	private String name;
 	private double bearing;
 	private double bearingRadians;
 	private double distance;
 	private double energy;
 	private double heading;
 	private double headingRadians;
 	private double velocity;
 	private long time;
 	private int priority;
 
 	public EnemyBot(ScannedRobotEvent e) {
     	this.update(e);
 	}
 
 	public void update(ScannedRobotEvent e) {
     	this.setName(e.getName());
     	this.setBearing(e.getBearing());
     	this.setBearingRadians(e.getBearingRadians());
     	this.setDistance(e.getDistance());
     	this.setEnergy(e.getEnergy());
     	this.setHeading(e.getHeading());
     	this.setHeadingRadians(e.getHeadingRadians());
     	this.setVelocity(e.getVelocity());
     	this.setTime(e.getTime());
     	this.setPriority(e.getPriority());
 	}
 	
 	/**
 	 * Returns time since this robot was last seen
 	 * @param currentTime This is only required until I can figure out a workaround
 	 * @return the time since this robot was last seen
 	 */
 	public long timeSinceSeen(long currentTime) {
 		//Doesn't compile
 		//System.out.println(StatusEvent.getStatus().getTime());
 		return currentTime - this.getTime();
 	}
 
 	public void printBot() {
 		System.out.println("Name: "+ this.getName());
 		System.out.println("Bearing: "+ this.getBearing());
 		System.out.println("Bearing (radians): "+ this.getBearingRadians());
 		System.out.println("Distance: "+ this.getDistance());
 		System.out.println("Energy: "+ this.getEnergy());
 		System.out.println("Heading: "+ this.getHeading());
 		System.out.println("Heading (radians)"+ this.getHeadingRadians());
 		System.out.println("Velocity: "+ this.getVelocity());
 		System.out.println("Time: "+ this.getTime());
 		System.out.println("Priority: "+ this.getPriority());
 	}
 
 	public String toString() {
 		StringBuilder string = new StringBuilder();
 		
 		string.append("Name: "+ this.getName());
 		string.append(" Bearing: "+ this.getBearing());
 		string.append(" Bearing (radians): "+ this.getBearingRadians());
 		string.append(" Distance: "+ this.getDistance());
 		string.append(" Energy: "+ this.getEnergy());
 		string.append(" Heading: "+ this.getHeading());
 		string.append(" Heading (radians)"+ this.getHeadingRadians());
 		string.append(" Velocity: "+ this.getVelocity());
 		string.append(" Time: "+ this.getTime());
 		string.append(" Priority: "+ this.getPriority());
 		
 		return string.toString();
 	}
 	
 	// Getters and Setters
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void setBearing(double bearing) {
 		this.bearing = bearing;
 	}
 
 	public double getBearing() {
 		return bearing;
 	}
 
 	public void setBearingRadians(double bearingRadians) {
 		this.bearingRadians = bearingRadians;
 	}
 
 	public double getBearingRadians() {
 		return bearingRadians;
 	}
 
 	public void setDistance(double distance) {
 		this.distance = distance;
 	}
 
 	public double getDistance() {
 		return distance;
 	}
 
 	public void setEnergy(double energy) {
 		this.energy = energy;
 	}
 
 	public double getEnergy() {
 		return energy;
 	}
 
 	public void setHeading(double heading) {
 		this.heading = heading;
 	}
 
 	public double getHeading() {
 		return heading;
 	}
 
 	public void setHeadingRadians(double headingRadians) {
 		this.headingRadians = headingRadians;
 	}
 
 	public double getHeadingRadians() {
 		return headingRadians;
 	}
 
 	public void setVelocity(double velocity) {
 		this.velocity = velocity;
 	}
 
 	public double getVelocity() {
 		return velocity;
 	}
 
 	public void setTime(long time) {
 		this.time = time;
 	}
 
 	public long getTime() {
 		return time;
 	}
 
 	public void setPriority(int priority) {
 		this.priority = priority;
 	}
 
 	public int getPriority() {
 		return priority;
 	}
 }
