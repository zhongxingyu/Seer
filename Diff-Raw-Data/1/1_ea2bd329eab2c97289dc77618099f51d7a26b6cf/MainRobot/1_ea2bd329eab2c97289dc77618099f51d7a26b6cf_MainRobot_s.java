 package com.frc2013.rmr662.main;
 
import com.frc2013.rmr662.eastereggs.DanceMode;
 import com.frc2013.rmr662.system.generic.Robot;
 import com.frc2013.rmr662.system.generic.RobotMode;
 
 public class MainRobot extends Robot {
 
 	protected RobotMode getAutoMode() {
 		return null;// new DanceMode(); // No autonomous mode this season :(
 	}
 
 	protected RobotMode getTeleopMode() {
 		return new TeleopMode();
 	}
 
 }
