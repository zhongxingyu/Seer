 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.badrobots.y2012.technetium.commands;
 
 import com.badrobots.y2012.technetium.subsystems.Helios;
 
 
 /**
  *
  * @author 1014
  */
 public class Monitor extends CommandBase {
 
     private boolean topWasBlocked;
     private boolean bottomWasBlocked;
 
     public Monitor() {
         requires(sensors);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() 
     {
         topWasBlocked = false;
         bottomWasBlocked = false;
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() 
     {
        if(sensors.topChannelBlocked())
         {
             
            bottomWasBlocked = true;//This is wrong, chage when all sensors in place
             System.out.println("Blocked");
         }
         else if(bottomWasBlocked)
         {
             sensors.setNumBalls(sensors.getNumBalls() + 1);
             bottomWasBlocked = false;
             System.out.println("BallAdded : " + sensors.getNumBalls());
         }
 
         //Does not account for shooting
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return false;
     }
 
     // Called once after isFinished returns true
     protected void end() {
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
