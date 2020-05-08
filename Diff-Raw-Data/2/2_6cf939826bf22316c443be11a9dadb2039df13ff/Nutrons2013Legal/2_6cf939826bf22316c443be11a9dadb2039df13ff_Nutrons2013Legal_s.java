 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.neu.nutrons.ultimateascent;
 
 
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.command.Scheduler;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
 import edu.neu.nutrons.ultimateascent.commands.CommandBase;
 import edu.neu.nutrons.ultimateascent.commands.auto.Autonomous;
 import edu.neu.nutrons.ultimateascent.commands.drivetrain.DTManualCmd;
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Nutrons2013Legal extends IterativeRobot {
 
     private Command autonomousCommand;
     private int autonomousMode = Autonomous.NONE;
     private Compressor comp = new Compressor(RobotMap.AIR_PRESSURE, RobotMap.COMPRESSOR_PORT);
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
        // comp.start();
         // Initialize all subsystems
         CommandBase.init();
         autonomousCommand = new Autonomous(Autonomous.THREE_DISC);
     }
 
     public void disabledPeriodic() {
         int oiAutoMode = CommandBase.oi.getAutoMode();
         if(oiAutoMode != Autonomous.NONE && oiAutoMode != autonomousMode) {
             autonomousMode = oiAutoMode;
             DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1,
                     "Auto: " + autonomousMode + "             ");
             System.out.println("Auto: " + autonomousMode);
         }
     }
 
     public void autonomousInit()
     {
         autonomousCommand = new Autonomous(autonomousMode);
         // schedule the autonomous command (example)
         autonomousCommand.start();
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic()
     {
         CommandBase.handleSubsystems();
         Scheduler.getInstance().run();
     }
 
     public void teleopInit() {
 	// This makes sure that the autonomous stops running when
         // teleop starts running. If you want the autonomous to
         // continue until interrupted by another command, remove
         // this line or comment it out.
         autonomousCommand.cancel();
         comp.start();
 
     }
     public void testInit()
     {
         comp.start();
     }
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         CommandBase.handleSubsystems();
         Scheduler.getInstance().run();
     }
 
     /**
      * This function is called periodically during test mode
      */
     public void testPeriodic() {
         LiveWindow.run();
     }
     public void disabledInit()
     {
         System.out.println("disabled");
         comp.stop();
     }
 }
