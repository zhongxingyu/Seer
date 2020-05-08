 
 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 /**
  *
  * @author bradmiller
  */
 public class ShootOn extends CommandBase {
     
     Timer timer;
     
     public ShootOn() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(shooter);
         
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         shooter.setSpeed(0.5);
         timer.start();
         
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         SmartDashboard.putBoolean("atSpeed",timer.get()> 1000);
         
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
