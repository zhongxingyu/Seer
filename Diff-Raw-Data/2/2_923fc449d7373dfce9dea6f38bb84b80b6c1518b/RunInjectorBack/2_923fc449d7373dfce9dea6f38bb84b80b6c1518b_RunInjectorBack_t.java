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
 public class RunInjectorBack extends CommandBase {
     
     public RunInjectorBack() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(shooterInjector);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
        shooterInjector.setPwr(-SmartDashboard.getNumber("Injector Return Power", -50)/100);
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() {
         return shooterInjector.getLimit();
     }
 
     // Called once after isFinished returns true
     protected void end() {
         shooterInjector.setPwr(0.0);
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
