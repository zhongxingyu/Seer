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
	 * @throws InterruptedException if the thread was interrupted while
	 * waiting for another thread to unlock the mutex on a necessary object
 	 */
	public double getHeading() throws InterruptedException;
 	
 	/**
 	 * Get the acceleration of the robot along its longitudinal (front-back) axis
 	 * in meters per second squared. This value may come from an accelerometer
 	 * or from drivetrain encoders.
 	 * @return The robot's acceleration.
	 * @throws InterruptedException if the thread was interrupted while
	 * waiting for another thread to unlock the mutex on a necessary object
 	 */
	public double getAcceleration() throws InterruptedException;
 
 }
