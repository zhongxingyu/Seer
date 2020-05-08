 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 package org.usfirst.frc1148;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import org.usfirst.frc1148.interfaces.RobotModule;
 import org.usfirst.frc1148.modules.AutoDriveModule;
 import org.usfirst.frc1148.modules.AutonomousModule;
 import org.usfirst.frc1148.modules.ClimberModule;
 import org.usfirst.frc1148.modules.DrawbridgeModule;
 import org.usfirst.frc1148.modules.JoyStickInputModule;
 import org.usfirst.frc1148.modules.RobotDriver;
 
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Watchdog;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class HWRobot extends IterativeRobot {
 
     private Hashtable modules;
 
     public HWRobot() {
         Watchdog.getInstance().feed();
         System.out.println("Constructing HWRobot...");
         modules = new Hashtable();
 
         System.out.println("Constructing modules...");
         // modules.put("motorTester", new MotorMoveTest(this));
         
         modules.put("robotDriver", new RobotDriver(this));
         modules.put("drawbridge", new DrawbridgeModule());
         modules.put("climber", new ClimberModule((DrawbridgeModule)modules.get("drawbridge")));
         modules.put("autodrive", new AutoDriveModule((RobotDriver) modules.get("robotDriver")));
         modules.put("joystick", new JoyStickInputModule(this, (AutoDriveModule) modules.get("autodrive"), (RobotDriver) modules.get("robotDriver"), (DrawbridgeModule) modules.get("drawbridge"), (ClimberModule) modules.get("climber")));
        modules.put("autonomous", new AutonomousModule((RobotDriver) modules.get("robotDriver"), (DrawbridgeModule) modules.get("drawbridge")));
         System.out.println("HWRobot construction done...");
         Watchdog.getInstance().feed();
     }
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         Watchdog.getInstance().feed();
         System.out.println("Initializing HWRobot...");
 
         Enumeration e = modules.elements();
         RobotModule value;
         while (e.hasMoreElements()) {
             value = (RobotModule) e.nextElement();
             value.initModule();
         }
 
         System.out.println("HWRobot initialization done...");
     }
 
     /**
      * Init the autonomous mode.
      */
     public void autonomousInit() {
         Watchdog.getInstance().feed();
         Enumeration e = modules.elements();
         RobotModule value;
         while (e.hasMoreElements()) {
             value = (RobotModule) e.nextElement();
             value.activateModule();
         }
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         Watchdog.getInstance().feed();
         Enumeration e = modules.elements();
         RobotModule value;
         while (e.hasMoreElements()) {
             value = (RobotModule) e.nextElement();
             value.updateTick(1);
         }
     }
 
     public void disabledInit() {
         Watchdog.getInstance().feed();
         Enumeration e = modules.elements();
         RobotModule value;
         while (e.hasMoreElements()) {
             value = (RobotModule) e.nextElement();
             value.deactivateModule();
         }
     }
 
     public void disabledPeriodic() {
         Watchdog.getInstance().feed();
         /*
          Enumeration e = modules.elements();
          RobotModule value;
          while(e.hasMoreElements()){
          value = (RobotModule) e.nextElement();
          value.deactivateModule();
          }*/
     }
 
     public void teleopInit() {
         Watchdog.getInstance().feed();
         Enumeration e = modules.elements();
         RobotModule value;
         while (e.hasMoreElements()) {
             value = (RobotModule) e.nextElement();
             value.activateModule();
         }
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         Watchdog.getInstance().feed();
 
         Enumeration e = modules.elements();
         RobotModule value;
         while (e.hasMoreElements()) {
             value = (RobotModule) e.nextElement();
             value.updateTick(2);
             Watchdog.getInstance().feed();
         }
         Watchdog.getInstance().feed();
     }
 
     public void disabledTeleop() {
         Watchdog.getInstance().feed();
         /*
          Enumeration e = modules.elements();
          RobotModule value;
          while(e.hasMoreElements()){
          value = (RobotModule) e.nextElement();
          value.deactivateModule();
          }*/
         Watchdog.getInstance().feed();
     }
 
     /**
      * This function is called periodically during test mode
      */
     public void testPeriodic() {
         Watchdog.getInstance().feed();
 
         Enumeration e = modules.elements();
         RobotModule value;
         while (e.hasMoreElements()) {
             value = (RobotModule) e.nextElement();
             value.updateTick(3);
             Watchdog.getInstance().feed();
         }
     }
 
     public void disabledTest() {
         Watchdog.getInstance().feed();
         /*
          Enumeration e = modules.elements();
          RobotModule value;
          while(e.hasMoreElements()){
          value = (RobotModule) e.nextElement();
          value.deactivateModule();
          }*/
     }
 }
