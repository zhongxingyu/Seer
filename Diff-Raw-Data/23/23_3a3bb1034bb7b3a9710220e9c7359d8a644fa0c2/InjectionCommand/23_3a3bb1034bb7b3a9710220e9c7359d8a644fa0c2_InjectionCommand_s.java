 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc2022.commands;
 
 import org.usfirst.frc2022.Joysticks.Attack3;
import org.usfirst.frc2022.subsystems.ShooterInjector;
 
 /**
  *
  * @author Emma Sloan
  */
 public class InjectionCommand extends CommandBase {
     
    ShooterInjector shooterInjector;
     Attack3 attack3;
     
     public InjectionCommand() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(shooterInjector);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
        shooterInjector.deactivate();       
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         if (shooterInjector.getJagSpeed()>=0.01 | shooterInjector.getJagSpeed()<-0.01){
                 shooterInjector.activate();
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
         shooterInjector.deactivate();
     }
 }
