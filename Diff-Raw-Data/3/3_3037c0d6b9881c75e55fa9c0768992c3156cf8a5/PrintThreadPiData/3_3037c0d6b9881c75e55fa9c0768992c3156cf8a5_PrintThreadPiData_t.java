 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc3946.UltimateAscent.commands;
 
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  *
  * @author Gustave Michel
  */
 public class PrintThreadPiData extends CommandBase {
     private double lastTime;
     
     public PrintThreadPiData() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(threadedberryPi);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         lastTime = 0;
        if(!threadedberryPi.isConnected() || !threadedberryPi.isEnabled()) {
            threadedberryPi.startPi();
        }
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         if(threadedberryPi.getTime() > lastTime) {
             lastTime = threadedberryPi.getTime();
             SmartDashboard.putNumber("Offset", threadedberryPi.getOffset()); //Print Data to SmartDashboard
             SmartDashboard.putNumber("Distance", (double) threadedberryPi.getDistance()/1000); //Convert from Millifeet to Feet
         }
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
