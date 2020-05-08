 
 package edu.wpi.first.wpilibj.templates.commands;
 
 /**
  *
  * @author Jessie and Emma
  */
 public class Go extends CommandBase {
     
     double ticksInitial;
     double ticksFinal;
     double distToGo;
     
     /**
      * 
      * @param dist the distance you want to go
      */
     public Go(double dist) {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         distToGo = dist;
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         System.out.println("just pressed the goTestButt");
        ticksInitial = (driveTrain.getRightEncoder() + driveTrain.getLeftEncoder()) / 2;
         //change distToGo
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         driveTrain.setSpeed(0.25, 0.25);
        ticksFinal = (driveTrain.getRightEncoder() + driveTrain.getLeftEncoder()) / 2;
         System.out.println("execute method");
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         if(ticksFinal - ticksInitial >= distToGo){
             return true;
         } else {
             return false;
         }
     }
 
     // Called once after isFinished returns true
     protected void end() {
         driveTrain.setSpeed(0, 0);
         System.out.println("end method");
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
