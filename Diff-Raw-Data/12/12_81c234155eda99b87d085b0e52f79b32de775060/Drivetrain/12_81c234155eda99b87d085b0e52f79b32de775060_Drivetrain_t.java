 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import edu.stuy.Constants;
 import edu.stuy.util.Gamepad;
 import edu.wpi.first.wpilibj.Accelerometer;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Ultrasonic;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The robot drivetrain.
  * @author kevin,arfan
  */
 public class Drivetrain {
     
     private static Drivetrain instance;
     private RobotDrive drivetrain;
     private Gyro gyro;
     private Accelerometer accel;
     private Ultrasonic sonar;
     
     private Drivetrain() {
         drivetrain = new RobotDrive(Constants.DRIVETRAIN_LEFT_CHANNEL, Constants.DRIVETRAIN_RIGHT_CHANNEL);
         sonar = new Ultrasonic(Constants.SONAR_CHANNEL_PING,Constants.SONAR_CHANNEL_ECHO);
         gyro = new Gyro(Constants.GYRO_CHANNEL);
         gyro.setSensitivity(0.007);
         accel = new Accelerometer(Constants.ACCELEROMETER_CHANNEL);
         accel.setSensitivity(0);
     }
     
     public static Drivetrain getInstance() {
         if (instance == null) {
             instance = new Drivetrain();
         }
         return instance;
     }
     
     public void tankDrive(double leftValue, double rightValue) {
         drivetrain.tankDrive(leftValue, rightValue);
     }
     
     /**
      * Tank drive using a gamepad's left and right analog sticks.
      * @param gamepad Gamepad to tank drive with
      */
     public void tankDrive(Gamepad gamepad) {
         tankDrive(-gamepad.getLeftY(), -gamepad.getRightY());
     }
 
     public double getSonarDistance() {
         return sonar.getRangeInches();
     }
     
     public void putDistance() {
         SmartDashboard.putNumber("Sonar distance:", sonar.getRangeInches());
     }
 
     public double getAngle() {
         return gyro.getAngle();
     }
     
     public void startOver() {
         gyro.reset();
     }
     
     public void putAngle() {
         SmartDashboard.putNumber("Gyro angle:", gyro.getAngle());
     }
     
     public double getAcceleration() {
         return accel.getAcceleration();
     }
     
     public void putAcceleration() {
         SmartDashboard.putNumber("Accelerometer acceleration:", accel.getAcceleration());
     }
     
 }
