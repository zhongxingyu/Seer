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
 import edu.wpi.first.wpilibj.templates.commands.CommandBase;
 import edu.wpi.first.wpilibj.templates.commands.*;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class RobotTemplate extends IterativeRobot {
 
     private final static boolean AUTONOMOUS_ENABLED = true;
     private Command findTarget;
     private Command joystick;
     private Command balance;
     private Load load;
     private Shoot shoot;
     private Command elevator;
     private Command shooter;
     private Command autoshoot;
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         // Initialize all subsystems
         CommandBase.init();
 
         // Setup our top-level commands
         findTarget = new FindTarget();
         joystick = new DriveJoystick();
         balance = new Balance();
         load = new Load();
         shooter = new SpinShooter();
         shoot = new Shoot();
         elevator = new ElevatorCmd();
         autoshoot = new Autoshoot();
     }
 
     public void autonomousInit() {
         // When running in competition mode, assume we start with two balls
         CommandBase.globalState.incrementBallCount();
         CommandBase.globalState.incrementBallCount();
 
         if (AUTONOMOUS_ENABLED) {
             //startIfNotRunning(findTarget);
         }
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         Scheduler.getInstance().run();
 
         if (AUTONOMOUS_ENABLED) {
             //if (CommandBase.globalState.targetVisible()) {
             cancelIfRunning(findTarget);
             runShooter();
             runElevator();
             startIfNotRunning(autoshoot);
             //}
         }
     }
 
     public void teleopInit() {
         if (AUTONOMOUS_ENABLED) {
             cancelIfRunning(findTarget);
         }
         CommandBase.globalState.setDriveEnabled(true);
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         Scheduler.getInstance().run();
 
         // Enable/disable drive
         if (CommandBase.globalState.isDriveEnabled()) {
             // Disable manual drive & balance when in autoshoot
             // (Autoshoot itself is contolled in the ball-handling section)
             if (CommandBase.globalState.isAutoshootEnabled()) {
                 cancelIfRunning(joystick);
                 cancelIfRunning(balance);
 
                 // Disable joystick when balancing
             } else if (CommandBase.globalState.isBalanceEnabled()) {
                 cancelIfRunning(joystick);
                 startIfNotRunning(balance);
 
                 // Disable balance when under manual control
             } else {
                 cancelIfRunning(balance);
                 startIfNotRunning(joystick);
             }
         } else {
             cancelIfRunning(balance);
             cancelIfRunning(joystick);
         }
 
         // Enable/disable ball handling
         if (CommandBase.globalState.isBallHandlingEnabled()) {
             // Shoot (manually or automatically
             if (CommandBase.globalState.isShootMode()
                     || CommandBase.globalState.isAutoshootEnabled()) {
                 // Enable autoshoot mode as needed
                 if (CommandBase.globalState.isAutoshootEnabled()
                         && CommandBase.globalState.ballsInControl() > 0) {
                     startIfNotRunning(autoshoot);
                 }
 
                 // Disable loading (but not until the load elevator is done)
                 if (!load.isElevatorRunning()) {
                     cancelIfRunning(load);
                 }
 
                 // Run the shooter and elevator as needed
                 runShooter();
                 runElevator();
 
             } else {
                 // Cancel all shoot-mode commands
                 cancelIfRunning(autoshoot);
                 cancelIfRunning(shooter);
                 cancelIfRunning(elevator);
 
                 // Load if we can take more balls
                 if (!CommandBase.globalState.readyToShoot()
                         && CommandBase.globalState.canLoadMoreBalls()) {
                     startIfNotRunning(load);
                 }
             }
         } else {
             cancelIfRunning(autoshoot);
             cancelIfRunning(shooter);
             cancelIfRunning(elevator);
             cancelIfRunning(load);
         }
     }
 
     // Restart the elevator anytime we have balls and no shoot-mode ball handler is running
     private void runElevator() {
         if (!shoot.isElevatorRunning() && !CommandBase.globalState.readyToShoot() && CommandBase.globalState.ballsInControl() > 0) {
             startIfNotRunning(elevator);
         }
     }
 
     // Run the shooter anytime we have balls (and the loader is done)
     private void runShooter() {
         if (!load.isElevatorRunning() && CommandBase.globalState.ballsInControl() > 0) {
             startIfNotRunning(shooter);
         }
     }
 
     // Macro for command state control
     private void cancelIfRunning(Command cmd) {
         if (cmd.isRunning()) {
             cmd.cancel();
         }
     }
 
     // Macro for command state control
     private void startIfNotRunning(Command cmd) {
         if (!cmd.isRunning()) {
             cmd.start();
         }
     }
 }
