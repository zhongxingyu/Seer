 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Team522Robot extends SimpleRobot {
 
     RobotDrive drive = new RobotDrive(1, 2, 3, 4);
     Joystick leftStick = new Joystick(1);
     Joystick rightStick = new Joystick(2);
     Joystick thirdStick = new Joystick (3);
 //    Compressor air = new Compressor(1,1);
     Compressor air = new Compressor(1,1);
     Solenoid pistonDown = new Solenoid(7); 
     Solenoid pistonUp = new Solenoid(8); 
     
     public Team522Robot() {
 
     }
     
     public void autonomous() {
 //       air.start();
        getWatchdog().setEnabled(false);        
         
//        sol1.set(false);
//      sol2.set(true);
         air.start();
        Timer.delay(1.0);
//        sol2.set(true);
//        sol1.set(false);
         
        Timer.delay(5.0);
         
        pistonDown.set(false);
        pistonUp.set(true);
 //        for(int i = 0; i < 4; i++){
 //           drive.drive(0.5, 0.0);
 //            Timer.delay(2.0);
 //            drive.drive(0.0, 0.0);
 //        }
 //       drive.drive(0.0, 0.0); 
     }
     
     public void operatorControl() {
         while(isOperatorControl() && isEnabled()){
             drive.tankDrive(leftStick.getY(), rightStick.getY());
             //SmartDashboard.putString(leftStick.getX() + " , " + leftStick.getY() + " , " + leftStick.getZ() + " +" , ERRORS_TO_DRIVERSTATION_PROP);
             Timer.delay(0.005);
             
             if(thirdStick.getRawButton(1)){
                 deployDoor(true);
             }
             else if(thirdStick.getRawButton(3) || thirdStick.getRawButton(2)){
                 deployDoor(false);
             }
         }
     }
     
     public void deployDoor(boolean value){
         
         if(!air.enabled()){
             air.start();
         }
         
         if(value){
             pistonUp.set(true);        
             pistonDown.set(false);
         }
         else{
             pistonDown.set(true);
             pistonUp.set(false);
         }
     }
     
 }
