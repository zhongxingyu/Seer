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
 import edu.wpi.first.wpilibj.camera.*;
 import edu.wpi.first.wpilibj.image.*;
 import edu.wpi.first.wpilibj.AnalogChannel;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class RobotTemplate extends SimpleRobot {
 
     AxisCamera camera;
     Dashboard board;
     DriverStationLCD LCD;
     //RobotDrive robot;
     Joystick left;
     Joystick right;
     Jaguar flipper;
     Victor shooter1, shooter2;
     Victor leadScrew;
     Jaguar motor1, motor2, motor3, motor4;
     Timer timer;
     AnalogChannel ReedSwitch;
     double startTime, timed;
     double delayStartTime;
     double delayFlipperTimer;
     double driveMagnitude, driveAngle;
     double x, y, rotation, xVal, yVal, rotationVal;
     double riseTime;
     boolean flipperOn, leadUp, leadDown, shooterOn, timedUp, timedDown;
     ColorImage fromCamera;
     BinaryImage filteredImage, threshholdImage, filteredSmallImage, convexHullImage;
 
     boolean tracker;
     //robot initialization stuff here
 
     public void robotInit() {
         board = DriverStation.getInstance().getDashboardPackerLow();
         
         //used to send the debug data to the display
         LCD = DriverStationLCD.getInstance();
         timer = new Timer();
         //joystick initialization 
         right = new Joystick(2);
         left = new Joystick(1);
         //motor initialization
         motor1 = new Jaguar(1);
         motor2 = new Jaguar(2);
         motor3 = new Jaguar(3);
         motor4 = new Jaguar(4);
         leadScrew = new Victor(8);
         flipper = new Jaguar(7);
         shooter2 = new Victor(6);
         shooter1 = new Victor(5);
         ReedSwitch = new AnalogChannel(1);
         //camera initialization
         /*camera = AxisCamera.getInstance();
          camera.writeCompression(30);
          camera.writeMaxFPS(30);
          camera.writeResolution(AxisCamera.ResolutionT.k320x240);*/
         // autonomous delay time in ms
         delayStartTime = 2000;
        delayFlipperTimer = 5000;
         //initial value for leadscrew rise time
         riseTime = 0;
     }
 
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public void autonomous() {
         this.safetyOff();
        timer.start();
         shooter1.setSafetyEnabled(true);
         shooter2.setSafetyEnabled(true);
         flipper.setSafetyEnabled(true);
         startTime = timer.get();
        System.out.println(startTime);
         //delayStartTime = 0;//how to get value from Dashboard?
         while (this.isAutonomous() && this.isEnabled()) {
           // System.out.println("sum = "+(startTime + delayStartTime + delayFlipperTimer) );
            //System.out.println("Cur time = "+timer.get());
             if (startTime + delayStartTime > timer.get()) {
                 shooter1.set(1);
                 shooter2.set(1);
                double temp = timer.get();
                System.out.println(temp);
                if ((startTime + 3) <temp ) {
                    System.out.println("i am in");
                     flipper.set(-1);
                 }
             } else {
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
 
         this.safetyOff();
         double time = 0;
         while (this.isOperatorControl() && this.isEnabled()) {
 
 
             x = left.getX();
             y = left.getY();
             rotation = right.getX();
             flipperOn = left.getTrigger();
             leadUp = left.getRawButton(3);
             leadDown = left.getRawButton(2);
             timedUp = left.getRawButton(8);
             timedDown = left.getRawButton(9);
             shooterOn = right.getTrigger();
 
 
             //drive control
             xVal = filter(x, 0.1);
             yVal = filter(y, 0.1);
             rotationVal = (Math.abs(rotation) < 0.1) ? 0 : rotation * 0.6;
 
             double[] MotorValues = mecanumMotorValue(xVal, yVal, rotationVal);
             //double[] MotorValues = tankMotorValue(yVal, right.getY());
             // double[] MotorValues = tankMotorValue2(yVal,xVal);
             motor1.set(MotorValues[0]);
             motor2.set(-MotorValues[1]);
             motor3.set(MotorValues[2]);
             motor4.set(-MotorValues[3]);
 
 
             //flipper control 
             if (flipperOn) {
                 tracker=false;
                 int count=0;
                 boolean fin=false;
                 flipper.set(-1);
                 while (fin == false) {
 
                     if (count > 10) {
                         break;
                     }
                     if (ReedSwitch.getVoltage() > 0.15) {
                         System.out.println(ReedSwitch.getVoltage());
                         tracker = true;
                         count++;
 
                     } else {
                         if (tracker == true) {
                             tracker = false;
                             fin = true;
                         }
 
                     }
 
                 }
                 Timer.delay(0.2);
                 flipper.set(0);
 
             } else {
                 flipper.set(0);
             }
             //shooter control
             if (shooterOn) {
                 shooter1.set(1);
                 shooter2.set(1);
                 System.out.println("Shooter on");
             } else {
                 shooter1.set(0);
                 shooter2.set(0);
             }
             //lead screw control 
             double leadScrewValue = 0;
             if (leadUp) {
                 leadScrewValue = 1;
                 System.out.println("Lead Screw up");
 
             } else {
                 if (leadDown) {
                     leadScrewValue = -1;
                     System.out.println("Lead Screw down");
                 } else {
                     if (timedUp && timed < 5000) {
                         time = timer.get();
                         leadScrewValue = 1;
                         timed = timed + (timer.get() - time);
                         System.out.println("Lead Screw timed up");
                     } else {
                         if (timedDown && timed > 0) {
                             time = timer.get();
                             leadScrewValue = -1;
                             timed = timed - (timer.get() - time);
                             System.out.println("Lead Screw timed down");
                         } else {
                             leadScrew.set(0);
 
                         }
                     }
                 }
             }
             leadScrew.set(leadScrewValue);
 
             Timer.delay(0.01);
         }
     }
 
     /**
      * This function is called once each time the robot enters test mode.
      */
     public void test() {
         //does nothing
         //blah blah testing
     }
 
     //diabled state
     public void disabled() {
         //this.safetyOff();
         while (this.isDisabled()) {
             Timer.delay(0.01);
         }
     }
 
     public void safetyOff() {
         //robot.setSafetyEnabled(false);
         shooter1.setSafetyEnabled(false);
         shooter2.setSafetyEnabled(false);
         flipper.setSafetyEnabled(false);
         leadScrew.setSafetyEnabled(false);
     }
 
     public void safetyOn() {
         //robot.setSafetyEnabled(true);
         shooter1.setSafetyEnabled(true);
         shooter2.setSafetyEnabled(true);
         flipper.setSafetyEnabled(true);
         leadScrew.setSafetyEnabled(true);
     }
 
     public double filter(double value, double range) {
         double filtered = (Math.abs(value) < 0.1) ? 0 : value * value;
         if (value >= 0) {//give back the sign 
             filtered = filtered;
         } else {
             filtered = -filtered;
         }
         return filtered;
 
     }
 
     public double[] mecanumMotorValue(double x, double y, double rotation) {
         double[] motorValues = new double[4];
         double front_left = -y + rotation + x;
         double front_right = -y - rotation - x;
         double rear_left = -y + rotation - x;
         double rear_right = -y - rotation + x;
         double max = Math.abs(front_left);
         if (Math.abs(front_right) > max) {
             max = Math.abs(front_right);
         }
         if (Math.abs(rear_left) > max) {
             max = Math.abs(rear_left);
         }
         if (Math.abs(rear_right) > max) {
             max = Math.abs(rear_right);
         }
         if (max > 1) {
             front_left /= max;
             front_right /= max;
             rear_left /= max;
             rear_right /= max;
         }
         motorValues[0] = front_left;
         motorValues[1] = front_right;
         motorValues[2] = rear_left;
         motorValues[3] = rear_right;
         return motorValues;
     }
 
     public double[] tankMotorValue(double axis1, double axis2) {
         double[] motorValues = new double[4];
         motorValues[0] = motorValues[1] = 0;
         motorValues[2] = -0.5 * axis1;
         motorValues[3] = -0.5 * axis2;
         return motorValues;
     }
 
     public double[] tankMotorValue2(double axisY, double axisX) {
         double[] motorValues = new double[4];
         double leftValue = axisY + axisX * 2, rightValue = axisY - axisX * 2;
         if (Math.abs(leftValue) >= 1 || Math.abs(rightValue) >= 1) {
             double maxValue = (Math.abs(leftValue) >= Math.abs(rightValue)) ? Math.abs(leftValue) : Math.abs(rightValue);
             leftValue /= maxValue;
             rightValue /= maxValue;
         }
         motorValues[0] = 0;
         motorValues[2] = -0.5 * leftValue;
         motorValues[1] = 0;
         motorValues[3] = -0.5 * rightValue;
         return motorValues;
     }
 }
