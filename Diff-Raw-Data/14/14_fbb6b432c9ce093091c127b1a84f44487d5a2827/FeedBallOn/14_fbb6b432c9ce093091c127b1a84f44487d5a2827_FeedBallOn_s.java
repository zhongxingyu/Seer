 
 package edu.wpi.first.wpilibj.templates.commands;
 
 /**
  * A Command that extends CommandBase and turns the feeder motor on, therefore rotating the feeder conveyor belt
  * @author Jessie
  */
 public class FeedBallOn extends CommandBase {
     
     private int sign;
     
     /**
      * turns the PickupBall feeder on
      * requires the "feeder" subsystem
      * @param sign either -1 or 1 designates the direction of the motor/victor as positive or negative
      */
     public FeedBallOn(int sign) {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(feeder);
         this.sign = sign;
     }
 
     // Called just before this Command runs the first time
     /**
      * turns the bottommost conveyor on
      */
     protected void initialize() {
         feeder.setFeederSpeed(sign * 0.25);
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
       
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
