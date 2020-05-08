 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.Joystick;
 
 /**
  *
  * @author alex
  */
 public class TeleopDriveCommand extends CommandBase {
 
     int n = 0;
 
     public TeleopDriveCommand() {
         requires(drive);
     }
 
     protected void initialize() {
     }
 
     protected void execute() {
         if (++n % 10 == 0) {
            System.out.println("x,y,theta: " + oi.drivex.get() + " " + oi.drivey.get() + " " + oi.driveTheta.get());
         }
 //        drive.driveRel(oi.drivex.get(), oi.drivey.get(), oi.driveTheta.get());
     }
 
     protected boolean isFinished() {
         return false;
     }
 
     protected void end() {
     }
 
     protected void interrupted() {
     }
 }
