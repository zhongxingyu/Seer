 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.command.Scheduler;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
 import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
 import edu.wpi.first.wpilibj.templates.commands.Autonomous;
 import edu.wpi.first.wpilibj.templates.commands.CommandBase;
import edu.wpi.first.wpilibj.templates.commands.HoldClimb;
 import edu.wpi.first.wpilibj.templates.commands.TeleopDriveCommand;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class RobotTemplate extends IterativeRobot {
 
     Command autonomousCommand;
     Command teleopCommand;
     SendableChooser autonomousSelect;
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         // instantiate the command used for the autonomous period
         autonomousSelect = new SendableChooser();
         autonomousCommand = new Autonomous();
         teleopCommand = new TeleopDriveCommand();
         autonomousSelect.addDefault("none", null);
         autonomousSelect.addObject("testAuto", autonomousCommand);
 
         // Initialize all subsystems
         CommandBase.init();
     }
 
     public void autonomousInit() {
         // schedule the autonomous command (example)
         Command autonomous = (Command)autonomousSelect.getSelected();
         if (autonomous != null) {
             autonomous.start();
         }
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         Scheduler.getInstance().run();
     }
 
     public void teleopInit() {
 	// This makes sure that the autonomous stops running when
         // teleop starts running. If you want the autonomous to 
         // continue until interrupted by another command, remove
         // this line or comment it out.
         autonomousCommand.cancel();
         teleopCommand.start();
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         Scheduler.getInstance().run();
     }
     public void disabledInit() {
         autonomousCommand.cancel();
         teleopCommand.cancel();
 //        new HoldClimb().start();
     }
     
 //    public void disabledPeriodic() {
 //        Scheduler.getInstance().run();
 //    }
     /**
      * This function is called periodically during test mode
      */
     public void testPeriodic() {
         LiveWindow.run();
     }
 }
