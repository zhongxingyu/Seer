 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.stuy;
 
 import edu.stuy.subsystems.*;
 import edu.stuy.util.Gamepad;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class DESiree extends IterativeRobot {
     Drivetrain drivetrain;
     Acquirer acquirer;
     Conveyor conveyor;
     Lights lights;
     Shooter shooter;
     Tilter tilter;
     Climber climber;
     
     Gamepad driverPad;
     Gamepad operatorPad;
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         drivetrain = Drivetrain.getInstance();
         acquirer = Acquirer.getInstance();
         conveyor = Conveyor.getInstance();
         lights = Lights.getInstance();
         shooter = Shooter.getInstance();
         tilter = Tilter.getInstance();
         climber = Climber.getInstance();
 
         driverPad = new Gamepad(Constants.DRIVER_PAD_PORT);
         operatorPad = new Gamepad(Constants.OPERATOR_PAD_PORT);
     }
     
     public void autonomousInit() {
         Autonomous.run();
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
     
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         drivetrain.tankDrive(driverPad);
         SmartDashboard.putNumber("Sonar distance:", drivetrain.getSonarDistance());
         SmartDashboard.putNumber("Gyro angle:", drivetrain.getAngle());
         SmartDashboard.putNumber("Accel angle:", tilter.getAbsoluteAngle());
         SmartDashboard.putNumber("Accel angle10:", tilter.getAbsoluteAngle10());
        SmartDashboard.putNumber("Accel angle100:", tilter.getAbsoluteAngle100());
         SmartDashboard.putNumber("X:", tilter.getXAcceleration());
         SmartDashboard.putNumber("Y:", tilter.getYAcceleration());
         SmartDashboard.putNumber("Z:", tilter.getZAcceleration());
     }
     
     /**
      * This function is called periodically during test mode
      */
     public void testPeriodic() {
         
     }
     
 }
