 package edu.nau.spring2012.cs386.DEADBEEF;
 
 import lejos.nxt.Motor;
 
 public class CheckColor implements Recipe {
 
 	public boolean execute() {
 		
 		// Code to close the claw.
 		//
 		Motor.C.rotate(80);
 		
		if ( RobotState.colorObj.getRed() > RobotState.colorObj.getRed() ) {
 			
 			return true;
 			
 		} else {
 			
 			return false;
 			
 		}
 		
 	}
 
 }
