 package cbccore.movement;
 /*
  *      MotorDriveTrain.java
  *      
  *      Version 0.9.1 r0002
  *      
  *      Copyright 2009 PiPeep
  *      Dr. Sakuya Ph. D
  *      Blinded.Light
  *      
  *      This program is free software; you can redistribute it and/or modify
  *      it under the terms of the GNU General Public License as published by
  *      the Free Software Foundation; either version 2 of the License, or
  *      (at your option) any later version.
  *      
  *      This program is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU General Public License for more details.
  *      
  *      You should have received a copy of the GNU General Public License
  *      along with this program; if not, write to the Free Software
  *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  *      MA 02110-1301, USA.
  */
 
 //no references to cbccore.low in here please.
 //If you feel you must, it should be done in wheel
 
 import cbccore.InvalidValueException;
 
 public class MotorDriveTrain extends DriveTrain {
 	
 	public static final int ticksPerRotation = 1100;
 	
 	protected Wheel _leftWheel;
 	protected Wheel _rightWheel;
 	private double _maxRps;
 	private double _maxCmps;
 	private double _trainWidth;
 	
 	public MotorDriveTrain(Wheel leftWheel, Wheel rightWheel, double trainWidth) {
 		_leftWheel = leftWheel;
 		_rightWheel = rightWheel;
 		_maxRps = Math.min(_leftWheel.maxRps(), _rightWheel.maxRps());
 		_maxCmps = Math.min(_leftWheel.maxCmps(), _rightWheel.maxCmps());
 		_trainWidth = trainWidth;
 	}
 	
 	public void moveAtVelocity(int tps) throws InvalidValueException {
 		moveAtTps(tps);
 	}
 	
 	public void moveAtTps(int tps) throws InvalidValueException {
 		_leftWheel.moveAtTps(tps);
 		_rightWheel.moveAtTps(tps);
 	}
 	
 	public void moveAtRps(double rps) throws InvalidValueException {
 		_leftWheel.moveAtRps(rps);
 		_rightWheel.moveAtRps(rps);
 	}
 	
 	public void moveAtCmps(double cmps) throws InvalidValueException {
 		_leftWheel.moveAtCmps(cmps);
 		_rightWheel.moveAtCmps(cmps);
 	}
 	
 	//Clockwise
 	public void rotateDegrees(double degrees, double degreesPerSecond) {
 		rotateRadians(Math.toRadians(degrees), Math.toRadians(degreesPerSecond));
 	}
 	
 	//Clockwise
 	public void rotateRadians(double radians, double radiansPerSecond) {
 		double dist = (_trainWidth*Math.PI)*(radians/(2*Math.PI));//, simplified
 		double speed = radiansPerSecond/radians*dist;//, simplified
 		moveWheelCm(dist, -dist, speed, -speed);
 	}
 	
 	//TODO: we need a function to move in a curve (w00t!)
 	
 	public void moveCurveRadians(double radians, double radius, double cmps) {
 		double leftCm = radians/(2*Math.PI) * radius*Math.PI + _trainWidth*2*radians/(2*Math.PI);
 		double rightCm = radians/(2*Math.PI) * radius*Math.PI - _trainWidth*2*radians/(2*Math.PI);
 		double leftCmps = cmps + radians/(2*Math.PI) * _trainWidth*2.;
 		double rightCmps = cmps - radians/(2*Math.PI) * _trainWidth*2.;
 		moveWheelCm(leftCm, rightCm, leftCmps, rightCmps);
 	}
 	
 	//remember, interruption is non-instantanious
 	public void moveCm(double cm, double cmps) throws InvalidValueException {
 		moveWheelCm(cm, cm, cmps, cmps);
 	}
 	
 	//leftCm/leftCmps must be equal to rightCm/rightCmps
 	private void moveWheelCm(double leftCm, double rightCm, double leftCmps, double rightCmps) {
 		System.out.println("moving left: "+ leftCm + " and right " + rightCm + " for " + leftCm/leftCmps + "seconds");
 		
 		leftCmps = leftCm<0?-Math.abs(leftCmps):Math.abs(leftCmps);
 		rightCmps = rightCm<0?-Math.abs(rightCmps):Math.abs(rightCmps);
 		
 		//we have an overhead, so in a case like this, it is better not to move at all
 		if(Math.abs(leftCm) < .5) leftCm = 0.;
 		if(Math.abs(rightCm) < .5) { if(leftCm == 0) { return; } rightCm = 0.; }
 		
		double destCmCounter = 0.;
 		_leftWheel.moveAtCmps(leftCmps);
 		_rightWheel.moveAtCmps(rightCmps);
 		long destTime = System.currentTimeMillis()+((long)((leftCm/leftCmps)*1000.));
 		long sleepOverhead = 0l;
 		while(System.currentTimeMillis() < (destTime-sleepOverhead)) {
 			//Thread.yield();
 			long sleepTime = (destTime - System.currentTimeMillis() - sleepOverhead)/2;
 			long sleepStart = System.currentTimeMillis();
 			try {
 				Thread.sleep(sleepTime);
 			} catch(InterruptedException e) {
 				moveAtTps(0);
 				return;
 			}
 			sleepOverhead = System.currentTimeMillis() - sleepStart - sleepTime;
 		}
 		while(System.currentTimeMillis() < destTime) {} //This code is questionable, may cause adverse multithreading effects
 		
 		kill();
 	}
 	
 	/*//Helper thread
 		private class MoveCmHelperThread implements Runnable {
 			private double _cm, _cmps;
 			private boolean _fineTune;
 			private Wheel _wheel;
 			
 			public MoveCmHelperThread(double cm, double cmps, boolean fineTune, Wheel wheel) {
 				_cm = cm; _cmps = cmps; _fineTune = fineTune; _wheel = wheel;
 			}
 			
 			public void run() {
 				_wheel.moveCm(_cm, _cmps, _fineTune);
 			}
 		}
 	//End Helper thread*/
 	
 	public void stop() {
 		freeze();
 	}
 	
 	public void freeze() {
 		_leftWheel.freeze();
 		_rightWheel.freeze();
 	}
 	
 	public void kill() {
 		_leftWheel.moveAtTps(0);
 		_rightWheel.moveAtTps(0);
 	}
 	
 	public double maxRps() {
 		return _maxRps;
 	}
 	
 	public double maxRadiansPS() {
 		return maxCmps() * _trainWidth /2./Math.PI;
 	}
 	
 	public double maxCmps() {
 		return _maxCmps;
 	}
 }
