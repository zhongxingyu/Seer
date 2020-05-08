 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 
 public class Drive {
     
     RobotDrive mechanum = new RobotDrive(1,2,3,4);
     double speed = 0.50; //Default Speed
     //int driverPerspective = 0;
     
     public void drive(double driveLX, double driveLY, double driveRX){ 
     mechanum.setSafetyEnabled(false);
    mechanum.mecanumDrive_Cartesian(driveLX * speed, driveLY * speed, driveRX * speed, 0); //Sets the motor speeds
     
     }
     
     public void setSpeed(boolean sniper, boolean turbo){
         
         if(turbo && !sniper){
             speed = 1.00; //In turbo mode
         } else if(!turbo && sniper){
             speed = 0.50; //In sniper mode
         } else {
             speed = 0.75; //In default mode
         }
     }
    /* public void perspectiveControl(boolean a, boolean b, boolean x, boolean y){
        if (y){
            driverPerspective = 0;
        } if (a){
            driverPerspective = 1;
        } if (x){
            driverPerspective = 2;
        } if (b){
            driverPerspective = 3;
        }
     }*/
 
 }
