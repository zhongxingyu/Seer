 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  *
  * @author alex
  */
 public class MaxPulseLaunchMotor extends CommandBase {
     
     public MaxPulseLaunchMotor() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(shooterLauncher);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         shooterLauncher.set(-SmartDashboard.getNumber("Launcher Pulse Power", 1.0)/100);
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
