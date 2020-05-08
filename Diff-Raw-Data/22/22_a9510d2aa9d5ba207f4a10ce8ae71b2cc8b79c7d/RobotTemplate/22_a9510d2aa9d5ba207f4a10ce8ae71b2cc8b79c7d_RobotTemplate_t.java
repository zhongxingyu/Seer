 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.Timer;
 
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class RobotTemplate extends SimpleRobot {
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     Joystick j1 = new Joystick(1); // Well what do you think??
     Joystick j2 = new Joystick(2);
    CANJaguar fLeft;
    CANJaguar fRight;
    CANJaguar bLeft;
    CANJaguar bRight;
     Timer timer = new Timer(); // timer
 
 
     public void autonomous()
     {
        try {
            CANJaguar fLeft = new CANJaguar(10); // motors for wheels with CAN ports as arguements
            CANJaguar fRight = new CANJaguar(4);
            CANJaguar bLeft = new CANJaguar(9);
            CANJaguar bRight = new CANJaguar(7);
        } catch (Exception e) { e.printStackTrace(); }
         System.out.print("autonomous starts");
         timer.start(); // obvious
         while (timer.get() < 5000000) // drive for 5s
         {
             System.out.print("current time"+timer.get());
            try {
                fLeft.setX(0.5); // DEPRECATED METHOD NO JUTSU
                fRight.setX(0.5);
            } catch(Exception e) { e.printStackTrace(); }
         }
         timer.stop();  // stop
     }
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     public void operatorControl() 
     {
         
         
     }
 }
