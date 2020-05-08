 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 
 /**
  *
  * @author RoboHawks
  */
 public class drive {
     Team3373 testing = new Team3373();
     Joystick drivestick = new Joystick(1);
     RobotDrive drive = new RobotDrive(1,2);
    boolean DRIVE_A = drivestick.getRawButton(1);
     double DRIVE_LSTICK = drivestick.getRawAxis(2);
         
     public void Driving(){
     if  (DRIVE_LSTICK >= -1 & XBOX_LSTICK <= 0 ){
        
     }  
  }
 }
