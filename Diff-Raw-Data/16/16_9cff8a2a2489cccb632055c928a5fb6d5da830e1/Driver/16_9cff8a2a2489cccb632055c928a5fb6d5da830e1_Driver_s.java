 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.northwestrobotics.frc2013;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.RobotDrive.MotorType;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.SpeedController;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Talon;
 import edu.wpi.first.wpilibj.Victor;
 
 /**
  * @author AgentOrange
  * @author soggy.potato
  * @author RG3
  */
 class Driver {
     /**
      * Joystick responsible for the user control of driving.
      * @author soggy.potato
      */
     private Joystick moveStick;
     /**
      * The object called for arcade drive
      *
      * @author soggy.potato
      */
     private RobotDrive robotDrive;
     
     // MOTORS
    private SpeedController frontLeftController = new Talon(RobotConstants.Drive.FRONT_LEFT_MOTOR);
     private SpeedController backRightController = new Talon(RobotConstants.Drive.BACK_RIGHT_MOTOR);
     
     private SpeedController frontRightController = new Victor(RobotConstants.Drive.FRONT_RIGHT_MOTOR);
     private SpeedController backLeftController = new Victor(RobotConstants.Drive.BACK_LEFT_MOTOR);
 
     /**
      * @author soggy.potato
      * @author AgentOrange
      * @author RG3
      */
     public Driver() {
         moveStick = new Joystick(RobotConstants.Drive.MOVE_CONTROLLER);
         //TODO: Change variable type to SpeedController
         initializeRobotDrive();
     }
 
     public void drive() {
         move();
     }
 
     
     /**
      * Move the robot motors in accordance with the user's input.
      *
      * @author AgentOrange
      */
     private void move() {
         robotDrive.arcadeDrive(moveStick);
     }
 
     
 
     private void initializeRobotDrive() {
         robotDrive = new RobotDrive(frontLeftController, backLeftController, frontRightController, backRightController);
         robotDrive.setInvertedMotor(MotorType.kRearRight, false);
         robotDrive.setMaxOutput(RobotConstants.Drive.MAX_MOTOR_SPEED);
     }
 }
