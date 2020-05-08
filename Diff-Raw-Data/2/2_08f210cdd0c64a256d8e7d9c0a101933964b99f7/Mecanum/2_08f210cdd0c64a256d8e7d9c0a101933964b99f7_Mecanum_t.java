 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 //Standard for inceptus code
 package org.inceptus;
 
 //Imports
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Watchdog;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 
 //Mecanum class. We are using IterativeRobot as SimpleRobot was not working.
 public class Mecanum extends IterativeRobot {
     //Get the joysticks (keep joy* naming system so it can be adjusted later)
     Joystick joy1 = new Joystick(1);
     Joystick joy2 = new Joystick(2);
     Joystick joy3 = new Joystick(3);
     
     //Get the jaguars
     Jaguar front_right = new Jaguar(2);
     Jaguar front_left = new Jaguar(1);
     Jaguar rear_right = new Jaguar(3);
     Jaguar rear_left = new Jaguar(4);
     
     //When robot starts
     public void robotInit() {
         
     }
     
     //When robot is disabled
    public void disabledInit() {
         //Log disabled status
         DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1, "Disabled");
     }
 
     //Periodically called during autonomous
     public void autonomousPeriodic() {
 
     }
     
     //Called at the start of teleop
     public void teleopInit() {
         //Log initiation success
         DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1, "Teleop Initiated");
     }
     
     //Periodically called during teleop
     public void teleopPeriodic() {
         
     }
     
 }
