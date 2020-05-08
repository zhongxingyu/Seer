 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.northwestrobotics.frc2013;
 
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 
 import edu.wpi.first.wpilibj.RobotDrive.MotorType;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.SpeedController;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Talon;
 import edu.wpi.first.wpilibj.Victor;
 /*
  * Driver
  * ======
  */
 
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
     private Joystick moveStick = new Joystick(RobotConstants.Drive.MOVE_CONTROLLER);
     /**
      * The object called for arcade drive
      *
      * @author soggy.potato
      */
     private RobotDrive robotDrive;
     /*
      * Motors
      * ------
      */
     private SpeedController frontLeftController = new Talon(RobotConstants.Drive.FRONT_LEFT_MOTOR); // TODO: Invert 
     private SpeedController backRightController = new Talon(RobotConstants.Drive.BACK_RIGHT_MOTOR);
     private SpeedController frontRightController = new Victor(RobotConstants.Drive.FRONT_RIGHT_MOTOR);
     private SpeedController backLeftController = new Victor(RobotConstants.Drive.BACK_LEFT_MOTOR);
 
     public Driver() {
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
         DriverStationLCD test = DriverStationLCD.getInstance();
         test.println(DriverStationLCD.Line.kUser3, 1, String.valueOf(moveStick.getAxis(Joystick.AxisType.kX)));
         test.updateLCD();
 
 
     }
 
     public void test() {
         if (moveStick.getRawButton(RobotConstants.TestDrive.ACTIVATE_FRONT_LEFT_MOTOR_BUTTON)) {
             frontLeftController.set(1);
         } else {
             frontLeftController.set(0);
         }
 
         if (moveStick.getRawButton(RobotConstants.TestDrive.ACTIVATE_FRONT_RIGHT_MOTOR_BUTTON)) {
             frontRightController.set(1);
         } else {
             frontRightController.set(0);
         }
 
         if (moveStick.getRawButton(RobotConstants.TestDrive.ACTIVATE_BACK_LEFT_MOTOR_BUTTON)) {
             backLeftController.set(1);
         } else {
             backLeftController.set(0);
         }
 
         if (moveStick.getRawButton(RobotConstants.TestDrive.ACTIVATE_BACK_RIGHT_MOTOR_BUTTON)) {
             backRightController.set(1);
         } else {
             backRightController.set(0);
         }
     }
 
     private void initializeRobotDrive() {
         robotDrive = new RobotDrive(frontRightController, backRightController, frontLeftController, backLeftController);
//        robotDrive.setInvertedMotor(MotorType.kRearRight, true);// original
//        robotDrive.setInvertedMotor(MotorType.kFrontRight, true);
//        robotDrive.setInvertedMotor(MotorType.kRearLeft, true);
//        robotDrive.setInvertedMotor(MotorType.kFrontLeft, true);// original
         robotDrive.setMaxOutput(RobotConstants.Drive.MAX_MOTOR_SPEED);
 
     }
 }
