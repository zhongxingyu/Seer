 package com.jaxelson;
 
 import java.awt.Graphics2D;
 
 import robocode.AdvancedRobot;
 import robocode.ScannedRobotEvent;
 import java.io.Serializable;
 
 public class EnemyBot implements Serializable{
 	private static final long serialVersionUID = -6633270333555835298L;
 	
 	// Bot Info
 	private String _name;
 	private double _bearing;
 	private double _bearingRadians;
 	private double _distance;
 	private double _energy;
 	private double _heading;
 	private double _headingRadians;
 	private double _oldHeading;
 	private double _oldHeadingRadians;
 	private double _velocity;
 	private long _time;
 	private int _priority;
 	
 	private ExtendedPoint2D _location;
 	
 	/** The robot that created this EnemyBot */
 	private AdvancedRobot _robot;
 	
 	// Misc Info
 	public int _numUpdates;
 
 	public EnemyBot(ScannedRobotEvent e, AdvancedRobot robot) {
 		this.setRobot(robot);
     	this.update(e);
 	}
 
 	/**
 	 * Update the information about this enemy
 	 * @param e ScannedRobotEvent information to use to update
 	 */
 	public void update(ScannedRobotEvent e) {
 		_numUpdates++;
 		savePreviousValues();
 		
		String enemyName = BotUtility.fixName(e.getName());
		
		this.setName(enemyName);
     	this.setBearing(e.getBearing());
     	this.setBearingRadians(e.getBearingRadians());
     	this.setDistance(e.getDistance());
     	this.setEnergy(e.getEnergy());
     	this.setHeading(e.getHeading());
     	this.setHeadingRadians(e.getHeadingRadians());
     	this.setVelocity(e.getVelocity());
     	this.setTime(e.getTime());
     	this.setPriority(e.getPriority());
     	
 		double angle = _robot.getHeadingRadians() + e.getBearingRadians();
 		ExtendedPoint2D enemyLocation = new ExtendedPoint2D((_robot.getX() + Math.sin(angle) * e.getDistance()),
 				(_robot.getY() + Math.cos(angle) * e.getDistance()));
     	this.setLocation(enemyLocation);
 	}
 	
 	/**
 	 * Saves previous values from last scan
 	 */
 	private void savePreviousValues() {
 		setOldHeading(_heading);
 		setOldHeadingRadians(_headingRadians);
 	}
 
 	/**
 	 * Returns time since this robot was last seen
 	 * @param currentTime This is only required until I can figure out a workaround
 	 * @return the time since this robot was last seen
 	 */
 	public long timeSinceSeen() {
 		//Doesn't compile
 		//System.out.println(StatusEvent.getStatus().getTime());
 		return _robot.getTime() - this.getTime();
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
 		System.out.println("Location: " + this.getLocation());
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
 		string.append(" Location: " + this.getLocation());
 		
 		return string.toString();
 	}
 	
 	public double getX() {
 		return _location.getX();
 	}
 	
 	public double getY() {
 		return _location.getY();
 	}
 	
 	/***************************/
 	/* Getters and Setters     */
 	/***************************/
 
 	public String getName() {
 		return _name;
 	}
 
 	public void setName(String name) {
 		this._name = BotUtility.fixName(name);
 	}
 
 	public void setBearing(double bearing) {
 		this._bearing = bearing;
 	}
 
	// This function returns the angle in radians
 	public double getBearing() {
		ExtendedPoint2D temp = new ExtendedPoint2D(_robot.getX(),_robot.getY());
		return temp.angleTo(_location);
 	}
 	
 	/**
 	 * @return the _oldHeading
 	 */
 	public double getOldHeading() {
 		return _oldHeading;
 	}
 
 	/**
 	 * @param oldHeading the _oldHeading to set
 	 */
 	public void setOldHeading(double oldHeading) {
 		_oldHeading = oldHeading;
 	}
 
 
 	public void setBearingRadians(double bearingRadians) {
 		this._bearingRadians = bearingRadians;
 	}
 
 	public double getBearingRadians() {
 		return _bearingRadians;
 	}
 	
 	/**
 	 * @return the _oldHeadingRadians
 	 */
 	public double getOldHeadingRadians() {
 		return _oldHeadingRadians;
 	}
 
 	/**
 	 * @param oldHeadingRadians the _oldHeadingRadians to set
 	 */
 	public void setOldHeadingRadians(double oldHeadingRadians) {
 		_oldHeadingRadians = oldHeadingRadians;
 	}
 
 
 	public void setDistance(double distance) {
 		this._distance = distance;
 	}
 
 	public double getDistance() {
 		return _distance;
 	}
 
 	public void setEnergy(double energy) {
 		this._energy = energy;
 	}
 
 	public double getEnergy() {
 		return _energy;
 	}
 
 	public void setHeading(double heading) {
 		this._heading = heading;
 	}
 
 	public double getHeading() {
 		return _heading;
 	}
 
 	public void setHeadingRadians(double headingRadians) {
 		this._headingRadians = headingRadians;
 	}
 
 	public double getHeadingRadians() {
 		return _headingRadians;
 	}
 
 	public void setVelocity(double velocity) {
 		this._velocity = velocity;
 	}
 
 	public double getVelocity() {
 		return _velocity;
 	}
 
 	public void setTime(long time) {
 		this._time = time;
 	}
 
 	public long getTime() {
 		return _time;
 	}
 
 	public void setPriority(int priority) {
 		this._priority = priority;
 	}
 
 	public int getPriority() {
 		return _priority;
 	}
 	
 	public void setLocation(ExtendedPoint2D location) {
 		this._location = location;
 	}
 	
 	public ExtendedPoint2D getLocation() {
 		return _location;
 	}
 
 	/**
 	 * @return the robot
 	 */
 	public AdvancedRobot getRobot() {
 		return _robot;
 	}
 
 	/**
 	 * @param robot the robot to set
 	 */
 	public void setRobot(AdvancedRobot robot) {
 		this._robot = robot;
 	}
 	
 	public int getNumUpdates() {
 		return _numUpdates;
 	}
 
 	public void paintTrackingRectangle(AdvancedRobot robot, Graphics2D g) {
 	     int x = (int)getX();
 	     int y = (int)getY();
 	     
 	     // Draw a line from our robot to the scanned robot
 	     g.drawLine(x, y, (int)robot.getX(), (int)robot.getY());
 	 
 	     // Draw a filled square on top of the scanned robot that covers it
 	     g.fillRect(x - 20, y - 20, 40, 40);
 	}
 
 }
