 /*
  * This file is part of CBCJVM.
  * CBCJVM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * CBCJVM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with CBCJVM.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package cbccore.movement;
 
 import cbccore.InvalidValueException;
 
 /**
  * Allows you to move the MotorDriveTrain or Create through high-level,
  * consistant abstracted commands. Centimeters and radians are used as the
  * default units, although for convenience, degree versions of the methods are
  * provided. Counter-clockwise is the default direction of rotation. This class
  * implements most of the hard things, so subclassing this abstract should be
  * <b>very</b> easy. You really only need to fill in a few things like
  * directDrive.
  * 
  * @author Benjamin Woodruff
  * @see    MotorDriveTrain
  * @see    CreateDriveTrain
  */
 
 public abstract class DriveTrain {
 	
 	public abstract void moveAtCmps(double cmps);
 	
 	public abstract double getTrainWidth();
 	
 	/**
 	 * Utility to make cmps match the sign of cm.
 	 */
 	protected static double moveParser(double cm, double cmps) { //returns new cmps
 		//cmps is made to match cm's sign
 		return cm<0?-Math.abs(cmps):Math.abs(cmps);
 	}
 	
 	/**
 	 * Rotates the device a specified number of degrees Counter-Clockwise
 	 * 
 	 * @param  degrees  The desired change in degrees
 	 * @see             #rotateRadians
 	 */
 	public void rotateDegrees(double degrees, double degreesPerSecond) {
 		rotateRadians(Math.toRadians(degrees), Math.toRadians(degreesPerSecond));
 	}
 	
 	
 	/**
 	 * Rotates the device a specified number of radians Counter-Clockwise
 	 * 
 	 * @param  radians           The desired change in radians
 	 * @param  radiansPerSecond  The number of radians to rotate every second
 	 * @see                      #rotateDegrees
 	 */
 	public void rotateRadians(double radians, double radiansPerSecond) {
 		double dist = getTrainWidth()*radians*.5;
 		double speed = radiansPerSecond/radians*dist;
 		moveWheelCm(-dist, dist, -speed, speed);
 	}
 	
 	
 	/**
 	 * Moves the robot in a piece of a circle.
 	 * 
 	 * @param  degrees  The piece of the circle defined as a change in the
 	 *                      robot's rotation in degrees
 	 * @param  radius   The radius of the circle.
 	 * @param  cmps     The Centimeters-Per-Second of the center of the robot.
 	 *                     The maximum value for this is less than maxCmps
 	 * @see             #moveCurveRadians
 	 */
 	public void moveCurveDegrees(double degrees, double radius, double cmps) {
 		moveCurveRadians(Math.toRadians(degrees), radius, cmps);
 	}
 	
 	
 	/**
 	 * Moves the robot in a piece of a circle.
 	 * 
 	 * @param  radians  The piece of the circle defined as a change in the
 	 *                      robot's rotation in radians
 	 * @param  radius   The radius of the circle.
 	 * @param  cmps     The Centimeters-Per-Second of the center of the robot.
 	 *                     The maximum value for this is less than maxCmps
 	 * @see             #moveCurveDegrees
 	 */
 	public void moveCurveRadians(double radians, double radius, double cmps) {
 		double halfOffset = getTrainWidth()*radians*.5;
		double cm = radians*getTrainWidth();
 		double leftCm = cm - halfOffset;
 		double rightCm = cm + halfOffset;
 		double halfCmpsOffset = halfOffset/cmps;
 		double leftCmps = cmps - halfCmpsOffset;
 		double rightCmps = cmps + halfCmpsOffset;
 		moveWheelCm(leftCm, rightCm, leftCmps, rightCmps);
 	}
 	
 	
 	/**
 	 * Moves robot forward a certain number of centimeters at a set speed. If
 	 * you specify a negitive number for cm, the robot will move backwards that
 	 * many centimeters.
 	 * 
 	 * @param  cm     Desired distance in centimeters
 	 * @param  cmps   Desired speed in centimeters-per-second
 	 */
 	public void moveCm(double cm, double cmps) throws InvalidValueException {
 		moveWheelCm(cm, cm, cmps, cmps);
 	}
 	
 	
 	//leftCm/leftCmps must be equal to rightCm/rightCmps
 	private void moveWheelCm(double leftCm, double rightCm, double leftCmps, double rightCmps) {
 		//System.out.println("moving left: "+ leftCm + " and right " + rightCm + " for " + leftCm/leftCmps + "seconds");
 		
 		leftCmps = leftCm<0?-Math.abs(leftCmps):Math.abs(leftCmps);
 		rightCmps = rightCm<0?-Math.abs(rightCmps):Math.abs(rightCmps);
 		
 		//we have an overhead, so in a case like this, it is better not to move at all
 		if(Math.abs(leftCm) < .5) leftCm = 0.;
 		if(Math.abs(rightCm) < .5) { if(leftCm == 0) { return; } rightCm = 0.; }
 		
 		//double destCmCounter = 0.;
 		directDrive(leftCmps, rightCmps);
 		long destTime = System.currentTimeMillis()+((long)((leftCm/leftCmps)*1000.));
 		long sleepOverhead = 0l;
 		while(System.currentTimeMillis() < (destTime-sleepOverhead)) {
 			//Thread.yield();
 			long sleepTime = (destTime - System.currentTimeMillis() - sleepOverhead)>>1;
 			long sleepStart = System.currentTimeMillis();
 			try {
 				Thread.sleep(sleepTime);
 			} catch(InterruptedException e) {
 				stop();
 				return;
 			}
 			sleepOverhead = System.currentTimeMillis() - sleepStart - sleepTime;
 		}
 		while(System.currentTimeMillis() < destTime) {} //This code is questionable, may cause adverse multithreading effects
 		
 		stop();
 	}
 	
 	protected abstract void directDrive(double leftCmps, double rightCmps);
 	
 	
 	/**
 	 * Just mirrors (calls) <code>freeze()</code>
 	 * 
 	 * @see    #stop
 	 * @see    #kill    
 	 */
 	public void stop() {
 		freeze();
 	}
 	
 	/**
 	 * Should lock both the robot's motors if possible, otherwise it should call
 	 * <code>kill()</code>
 	 * 
 	 * @see    #stop
 	 * @see    #kill
 	 */
 	public abstract void freeze();
 	
 	
 	/**
 	 * Stops the robot by setting the speed to zero. If a different mechanism is
 	 * needed for stopping, you can override this method.
 	 * 
 	 * @see    #stop
 	 * @see    #freeze
 	 */
 	public void kill() {
 		directDrive(0, 0);
 	}
 	
 	/**
 	 * Should get the maximum speed in centimeters-per-second for the left wheel
 	 * of the robot.
 	 * 
 	 * @return        the speed in cmps
 	 * @see           #getRightMaxCmps
 	 */
 	protected abstract double getLeftMaxCmps();
 	
 	
 	/**
 	 * Should get the maximum speed in centimeters-per-second for the right
 	 * wheel of the robot.
 	 * 
 	 * @return        the speed in cmps
 	 * @see           #getLeftMaxCmps
 	 */
 	protected abstract double getRightMaxCmps();
 	
 	/**
 	 * Gets the maximum speed in centimeters-per-second for the robot.
 	 * 
 	 * @return        The maximum speed in centimeters-per-second for the robot.
 	 * @see           #getLeftMaxCmps
 	 * @see           #getRightMaxCmps
 	 */
 	public double getMaxCmps() {
 		return Math.min(getLeftMaxCmps(), getRightMaxCmps());
 	}
 }
