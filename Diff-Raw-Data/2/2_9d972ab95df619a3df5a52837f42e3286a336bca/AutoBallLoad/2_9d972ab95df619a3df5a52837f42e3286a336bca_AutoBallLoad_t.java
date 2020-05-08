 package org.buzzrobotics.commands;
 
 /**
  * Autonomously Loads Balls.
  * @author BUZZWS1
  */
 public class AutoBallLoad extends CommandBase {
     
     public AutoBallLoad() {
         requires(ballfeeder);
         requires(rollerarm);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
             rollerarm.turnOnRollers(-1);     //Turn on feeder and rollers
             ballfeeder.driveUp();
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() { 
         if (ir.getTopIRSensor() && ir.getMiddleIRSensor() && ir.getBottomIRSensor()){
             return true;
         }else{
             return false;
         }
     }
     // Called once after isFinished returns true
     protected void end() {
         rollerarm.turnOffRollers();
        ballfeeder.stop();
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
