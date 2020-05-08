 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package com.badrobots.y2012.technetium;
 
 
 import com.badrobots.y2012.technetium.commands.PolarMechanumDrive;
 import com.badrobots.y2012.technetium.commands.TankDrive;
 import com.badrobots.y2012.technetium.commands.MechanumDrive;
 import com.badrobots.y2012.technetium.commands.CommandBase;
 import com.badrobots.y2012.technetium.subsystems.DriveTrain;
 import com.badrobots.y2012.technetium.buttons.TankDriveTrigger;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Watchdog;
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.command.Scheduler;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import com.badrobots.y2012.technetium.buttons.MechanumDriveTrigger;
//import com.badrobots.y2012.technetium.buttons.ResetGyro;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Technetium extends IterativeRobot {
 
     Command firstCommand;
     Button mecanumDriveTrigger, tankDriveTrigger, resetGyro;
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit()
     {
         // Initialize all subsystems
         CommandBase.init();
         // instantiate the command used for the autonomous period
         //Initializes triggers
         mecanumDriveTrigger = new MechanumDriveTrigger();
         tankDriveTrigger = new TankDriveTrigger();
        //resetGyro = new ResetGyro();
     }
 
     public void autonomousInit()
     {
 
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic()
     {
         
     }
 
     public void teleopInit()
     {
         //Scheduler.getInstance().add(firstCommand);
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() 
     {
         Watchdog.getInstance().feed();
         Scheduler.getInstance().run();
 
         //Polls the buttons to see if they are active, if they are, it adds the
         //command to the Scheduler.
         if (mecanumDriveTrigger.get())        
             Scheduler.getInstance().add(new MechanumDrive());
 
         else if (tankDriveTrigger.get())
             Scheduler.getInstance().add(new TankDrive());
         
         else if (OI.rightJoystick.getRawButton(2))
             Scheduler.getInstance().add(new PolarMechanumDrive());
 
         resetGyro.get();
         
         //Puts the current command being run by DriveTrain into the SmartDashboard
         SmartDashboard.putData(DriveTrain.getInstance().getCurrentCommand());
         
         SmartDashboard.putString(ERRORS_TO_DRIVERSTATION_PROP, "Test String");
 
 
     }
 }
