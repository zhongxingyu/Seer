 package org.team751.inav;
 
 /**
  * An interface for an object that can provide data for inertial navigation
  * 
  * @author Sam Crow
  * 
  */
 public interface INavDataProvider {
 
 	/**
 	 * Get the heading of the robot in degrees. This value may be greater than
 	 * 360 or less than zero.
 	 * 
 	 * @return The robot's heading
 	 */
	public double getHeading();
 	
 	/**
 	 * Get the acceleration of the robot along its longitudinal (front-back) axis
 	 * in meters per second squared. This value may come from an accelerometer
 	 * or from drivetrain encoders.
 	 * @return The robot's acceleration.
 	 */
	public double getAcceleration();
 
 }
