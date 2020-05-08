 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Dashboard;
 import edu.wpi.first.wpilibj.DriverStation;
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class RobotTemplate extends SimpleRobot {
     
     Dashboard board = DriverStation.getInstance().getDashboardPackerLow();
     RobotDrive robot = new RobotDrive(1,2,3,4);
     Joystick left = new Joystick(1);
     Joystick right = new Joystick(2);
     Jaguar shooter1 = new Jaguar(5);
     Jaguar shooter2 = new Jaguar(6);
     Jaguar flipper = new Jaguar(7);
     Victor leadScrew = new Victor(8);
     Timer timer = new Timer();
     double startTime, timed ;
     double delayStartTime = 2000;// unit of millisecond
     double delayFlipperTimer = 2000;// unit of millisecond
     double x,y,rotation;
     double rise_time=0;
     boolean flipperOn, leadUp,leadDown,shooterOn, timedUp, timedDown;
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public void autonomous() {
         this.safetyOff();
         shooter1.setSafetyEnabled(true);
         shooter2.setSafetyEnabled(true);
         flipper.setSafetyEnabled(true);
         startTime = timer.get();
         
         //delayStartTime = 0;//how to get value from Dashboard?
         while(this.isAutonomous()&&this.isEnabled()){
             if(startTime+delayStartTime>timer.get())
             {
                 shooter1.set(1);
                 shooter2.set(1);
                 if(startTime+delayStartTime+delayFlipperTimer>timer.get()){
                     flipper.set(-1);
                 }
             }else{
                 shooter1.set(0);
                 shooter2.set(0);
                 flipper.set(0);
             }
             Timer.delay(0.01);
             
         }
     }
     /**
      * This function is called once each time the robot enters operator control.
      */
     public void operatorControl() {
         this.safetyOn();
         double time;
         while(this.isOperatorControl()&&this.isEnabled()){
             x = left.getX();
             y = left.getY();
             rotation = right.getX();
             flipperOn = left.getTrigger();
             leadUp = left.getRawButton(3);
             leadDown = left.getRawButton(2);
            timedUp = left.getRawButton(8);
            timedDown = left.getRawButton(9);
             shooterOn= right.getTrigger();
             if(flipperOn){
                 flipper.set(-1);
             }else{
                 flipper.set(0);
             }
             if(shooterOn){
                 shooter1.set(1);
                 shooter2.set(1);
             }else{
                 shooter1.set(0);
                 shooter2.set(0);
             }
             if(leadUp){
                 leadScrew.set(1);
             }else{
                 if(leadDown){
                     leadScrew.set(-1);
                 }else{
                     leadScrew.set(0);
                 }
             }
             if(timedUp && timed<5000){
                 time=timer.get();
                 leadScrew.set(1);
                 timed = timed +(timer.get()-time);
             }else{
                 if(timedDown && timed>0){
                     time=timer.get();
                     leadScrew.set(-1);
                     timed = timed -(timer.get()-time);
                 }else{
                     leadScrew.set(0);
                 }
             }
             double X_val,Y_val,Rotation_val;
             X_val = (Math.abs(x)<0.05)? 0:x*x;
             Y_val = (Math.abs(y)<0.05)? 0:y*y;
             Rotation_val = (Math.abs(rotation)<0.05)? 0:rotation*0.6;
             
             robot.mecanumDrive_Cartesian(X_val, Y_val, Rotation_val, 0);
             
             Timer.delay(0.01);
         }
     }
     
     /**
      * This function is called once each time the robot enters test mode.
      */
     public void test() {
     
     }
     public void safetyOff(){
         robot.setSafetyEnabled(false);
         shooter1.setSafetyEnabled(false);
         shooter2.setSafetyEnabled(false);
         flipper.setSafetyEnabled(false);
         leadScrew.setSafetyEnabled(false);
     }
     public void safetyOn(){
         robot.setSafetyEnabled(true);
         shooter1.setSafetyEnabled(true);
         shooter2.setSafetyEnabled(true);
         flipper.setSafetyEnabled(true);
         leadScrew.setSafetyEnabled(true);
     }
 }
