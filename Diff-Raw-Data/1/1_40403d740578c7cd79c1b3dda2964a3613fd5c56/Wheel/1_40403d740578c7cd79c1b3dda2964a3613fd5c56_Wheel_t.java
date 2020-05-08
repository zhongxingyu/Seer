 package cbccore.movement;
 /*
  *      Wheel.java
  *      
  *      Version 0.9 r0000
  *      
  *      Copyright 2009 PiPeep
  *      Ariel Lawson
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
 
 import cbccore.motors.Motor;
 import cbccore.InvalidValueException;
 
 public class Wheel extends Motor {
 	
 	protected double _efficiency;
 	private double _maxRps;
 	private double _maxCmps;
 	protected double _circumference;
 	private int _currentTps;
 	private int _port;
 	
 	public Wheel(int port, double circumference, double efficiency) {
 		super(port);
 		_port = port;
 		_circumference = circumference;
 		_efficiency = efficiency;
 		_maxRps = 1000./MotorDriveTrain.ticksPerRotation*_efficiency;
 		_maxCmps = _maxRps*circumference();
 	}
 	
 	public double maxRps() {
 		return _maxRps;
 	}
 	
 	public double maxCmps() {
 		return _maxCmps;
 	}
 	
 	public int currentTps() {
 	    return _currentTps;
 	}
 	
 	public double currentRps() {
 		return _currentTps/MotorDriveTrain.ticksPerRotation;
 	}
 	
 	public double circumference() {
 		return _circumference;
 	}
 	
 	protected void checkTpsRange(int tps) throws InvalidValueException {
 		System.out.println(""+tps);
 		if(Math.abs(tps) > (_maxRps*MotorDriveTrain.ticksPerRotation)) {
 			System.out.println("" + tps + ", " + _maxRps*MotorDriveTrain.ticksPerRotation);
 			throw new InvalidValueException();
 		}
 	}
 	
 	public void moveAtTps(int tps) throws InvalidValueException {
 		checkTpsRange(tps);
 		_currentTps = tps;
 		super.moveAtVelocity((int)(tps/_efficiency));
 	}
 	
 	public void moveAtRps(double rps) throws InvalidValueException {
 		moveAtTps((int)(rps*MotorDriveTrain.ticksPerRotation));
 	}
 	
 	public void moveAtCmps(double cmps) throws InvalidValueException {
 		moveAtRps(cmps/circumference());
 	}
 	
 	//calling this does not guarentee anything, but will attempt to move within a certain accuracy
 	//uses recursive algorithm for fine tuning
 	/*public void moveCm(double cm, double cmps, boolean fineTune) throws InvalidValueException {
 		System.out.println("moveCm has been called - port #"+_port);
 		//cmps is made to match cm's sign
 		cmps = cm<0?-Math.abs(cmps):Math.abs(cmps);
 		
 		//we have an overhead, so in a case like this, it is better not to move at all
 		if(Math.abs(cm) < .5) return;
 		
 		double destCmCounter = 0.;
 		if(fineTune) {
 			destCmCounter = getCmCounter()+cm;
 		}
 		moveAtCmps(cmps);
 		long destTime = System.currentTimeMillis()+((long)((cm/cmps)*1000.));
 		long sleepOverhead = 0;
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
 		//while(System.currentTimeMillis() < destTime) {} //This code is questionable, may cause adverse multithreading effects
 		
 		//refine, and fineTune if necicary. Fine tuning moves slower.
 		if(fineTune) { moveCm(destCmCounter-getCmCounter(), (Math.abs(cmps)/2)<1.5?1.5:(cmps/2) , fineTune); }
 		moveAtTps(0);
 	}*/
 	
 	public int getTickCounter() {
 		return getPositionCounter();
 	}
 	
 	public double getWheelRotationCounter() {
 		return ((double)getTickCounter())/MotorDriveTrain.ticksPerRotation;
 	}
 	
 	public double getCmCounter() {
 		return getWheelRotationCounter()*circumference();
 	}
 	
 	public int moveAtVelocity(int tps) {
 		moveAtTps(tps);
		return 0;
 	}
 }
