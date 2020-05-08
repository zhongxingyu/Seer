 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.buzzrobotics.commands;
 
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  *
  * @author buzz5
  */
 public class AutoMode1 extends CommandBase {
     int m_autoPeriodicLoops;
     public AutoMode1() {
         // Use requires() here to declare subsystem dependencies
         // eg. requires(chassis);
         requires(drivetrain);
         requires(light);
     }
 
     // Called just before this Command runs the first time
     protected void initialize() {
         m_autoPeriodicLoops = 0;
     }
 
     // Called repeatedly when this Command is scheduled to run
     protected void execute(){
         m_autoPeriodicLoops = m_autoPeriodicLoops + 1;
         if (m_autoPeriodicLoops == 1) {
         drivetrain.drive(1,0);
         }
         
         if (m_autoPeriodicLoops == 2 * 20) {
            drivetrain.drive(-1,2);
         }
         if (m_autoPeriodicLoops == 3 * 20) {
             light.on();
         }
         if (m_autoPeriodicLoops == 5 * 20) {
             light.off();
         }
         if (m_autoPeriodicLoops == 6 * 20) {
             drivetrain.drive(0,0);
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
