 
 package edu.wpi.first.wpilibj.templates.commands;
 
 /**
 * A Command extending CommandBase that 
 * @author Emma and Gillie
  */
 public class AutonomousAimStart extends CommandBase {
 
     public AutonomousAimStart() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(shooterYaw);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         shooterYaw.setSetpoint(-100);
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         if(shooterYaw.getSwitch() == true){
             return true;
         }else{
             return false;
         }
     }
 
     // Called once after isFinished returns true
     protected void end() {
         shooterYaw.resetEncoder();
         shooterYaw.setSetpoint(shooterYaw.getPosition());
         
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
