 /**
  * DriveForward: An autonomous stage that drives forward a fixed distance.
  */
 
 package org.usfirst.frc348.auton;
 
 import org.usfirst.frc348.JagBot;
 
 import edu.wpi.first.wpilibj.SmartDashboard;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 
 public class DriveForward implements Stage {
     protected JagBot bot;
     protected boolean error = false;
     protected double distance, goodEnough, gain;
     protected double leftStart, rightStart, leftDist, rightDist;
     
     public DriveForward(JagBot bot, double feet, double goodEnough, double gain) {
 	this.bot = bot;
 	this.distance = feet;
 	this.goodEnough = goodEnough;
 	this.gain = gain;
     }
     
     public void enter() {
 	try {
 	    leftStart = bot.dt.getLeftDist();
 	    rightStart = bot.dt.getRightDist();
 	} catch (CANTimeoutException e) {
 	    e.printStackTrace();
 	    error = true;
 	}
     }
     
     public void periodic() {
 	try {
 	    leftDist = bot.dt.getLeftDist() - leftStart;
 	    rightDist = bot.dt.getRightDist() - rightStart;
 	    
 	    SmartDashboard.log(leftDist, "Left Distance");
 	    SmartDashboard.log(rightDist, "Right Distance");
 
 	    double left = gain * ((distance - leftDist) / distance);
 	    double right = gain * ((distance - rightDist) / distance);
	    double out = (left + right) / 2;
 	    
	    bot.dt.drive(out, out);
 	} catch (CANTimeoutException e) { 
 	    e.printStackTrace();
 	    error = true;
 	}
     }
     
     public void exit() {
 	// No cleaning up
     }
     
     public boolean isDone() {
 	return ((leftDist + rightDist) / 2) > goodEnough;
     }
     
     public boolean isError() {
 	return error;
     }
     
 }
