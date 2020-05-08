 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import edu.stuy.Constants;
 import edu.stuy.util.Gamepad;
 import edu.stuy.util.Sonar;
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.PIDController;
 import edu.wpi.first.wpilibj.PIDOutput;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;
 
 /**
  * The robot drivetrain.
  *
  * @author kevin,arfan
  */
 public class Drivetrain {
 
     double driveStraightSpeed = 0.8;
     private double lastTime;
     private double startTime;
     private double lastSpeedLeft;
     private double lastSpeedRight;
     
     private static Drivetrain instance;
     private RobotDrive drivetrain;
     private Gyro gyro;
     private Compressor compressor;
     PIDController forwardController;
     PIDController backwardController;
     private Encoder encoderRight;
     private Encoder encoderLeft;
 
     private Drivetrain() {
         drivetrain = new RobotDrive(Constants.DRIVETRAIN_LEFT_CHANNEL, Constants.DRIVETRAIN_RIGHT_CHANNEL);
         drivetrain.setSafetyEnabled(false);
         gyro = new Gyro(Constants.GYRO_CHANNEL);
         gyro.setSensitivity(0.007);
         gyroReset();
 
         encoderLeft = new Encoder(Constants.DRIVE_ENCODER_LEFT_A, Constants.DRIVE_ENCODER_LEFT_B);
         encoderRight = new Encoder(Constants.DRIVE_ENCODER_RIGHT_A, Constants.DRIVE_ENCODER_RIGHT_B);
         encoderLeft.setDistancePerPulse(Constants.ENCODER_DISTANCE_PER_PULSE);
         encoderRight.setDistancePerPulse(Constants.ENCODER_DISTANCE_PER_PULSE);
         encoderLeft.start();
         encoderRight.start();
 
         compressor = new Compressor(Constants.PRESSURE_SWITCH_CHANNEL, Constants.COMPRESSOR_RELAY_CHANNEL);
         compressor.start();
 
         forwardController = new PIDController(Constants.PVAL_D, Constants.IVAL_D, Constants.DVAL_D, gyro, new PIDOutput() {
             public void pidWrite(double output) {
                 drivetrain.arcadeDrive(driveStraightSpeed, output);
             }
         }, 0.005);
         forwardController.setInputRange(-360.0, 360.0);
         forwardController.setPercentTolerance(1 / 90. * 100);
         forwardController.disable();
 
         backwardController = new PIDController(Constants.PVAL_D, Constants.IVAL_D, Constants.DVAL_D, gyro, new PIDOutput() {
             public void pidWrite(double output) {
                 drivetrain.arcadeDrive(-driveStraightSpeed, output);
             }
         }, 0.005);
         backwardController.setInputRange(-360.0, 360.0);
         backwardController.setPercentTolerance(1 / 90. * 100);
         backwardController.disable();
         
         startTime = Timer.getFPGATimestamp();
         lastTime = startTime;
         lastSpeedLeft = 0;
         lastSpeedRight = 0;
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
      *
      * @param gamepad Gamepad to tank drive with
      */
     public void tankDrive(Gamepad gamepad) {
         tankDrive(gamepad.getLeftY(), gamepad.getRightY());
     }
 
     public double getAngle() {
         return gyro.getAngle();
     }
 
     public void gyroReset() {
         gyro.reset();
     }
 
     public void putAngle() {
         SmartDashboard.putNumber("Gyro angle:", gyro.getAngle());
     }
 
     public void stopCompressor() {
         compressor.stop();
     }
 
     public boolean getPressure() {
         return compressor.getPressureSwitchValue();
     }
 
     public void enableDriveStraight(boolean forward) {
         if (forward) {
             forwardController.setSetpoint(0);
             forwardController.enable();
         } else {
             backwardController.setSetpoint(0);
            forwardController.disable();
         }
     }
 
     public void disableDriveStraight() {
         forwardController.disable();
         backwardController.disable();
     }
 
     public double getLeftEnc() {
         return encoderLeft.getDistance();
     }
 
     public double getRightEnc() {
         return encoderRight.getDistance();
     }
 
     public void forwardInchesRough(int inches) {
         resetEncoders();
         double startTime = Timer.getFPGATimestamp();
         boolean fwd = inches >= 0;
         enableDriveStraight(fwd);
         while (((fwd && getAvgDistance() < inches)
                 || (!fwd && getAvgDistance() > inches))
                 && (Timer.getFPGATimestamp() - startTime) < 15.0) {
             //do nothing because driveStraight is enabled.
         }
         disableDriveStraight();
     }
 
     public void resetEncoders() {
         encoderLeft.reset();
         encoderRight.reset();
     }
 
     public double getAvgDistance() {
         return (getLeftEnc() + getRightEnc()) / 2.0;
     }
 }
