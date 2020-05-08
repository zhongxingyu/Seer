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
        
        if(!ir.getTopIRSensor() && !ir.getMiddleIRSensor() && !ir.getBottomIRSensor()){ //If there are three balls already then don't pick up more
        ballfeeder.stop();
        rollerarm.turnOnRollers(0);
        }
     }
 
     // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return true;
     }

     // Called once after isFinished returns true
     protected void end() {
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
