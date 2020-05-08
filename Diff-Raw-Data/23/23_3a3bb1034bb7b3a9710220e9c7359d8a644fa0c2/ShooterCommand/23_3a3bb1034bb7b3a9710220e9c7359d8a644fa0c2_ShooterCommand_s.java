 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.usfirst.frc2022.commands;
 
 import org.usfirst.frc2022.Joysticks.Attack3;
import org.usfirst.frc2022.subsystems.ShooterInjector;
 
 /**
  *
  * @author Emma
  */
 public class ShooterCommand extends CommandBase {
     
    ShooterInjector shooterInjector;
     Attack3 attack3;
     
     public ShooterCommand() {
         // Use requires() here to declare subsystem dependencies
         requires(shooterInjector);
         attack3 = oi.getAttack3();
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         shooterInjector.setShooter(0);//stops motor by setting PID loop to 0
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute() {
         shooterInjector.setShooter(attack3.getThrottle());
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
