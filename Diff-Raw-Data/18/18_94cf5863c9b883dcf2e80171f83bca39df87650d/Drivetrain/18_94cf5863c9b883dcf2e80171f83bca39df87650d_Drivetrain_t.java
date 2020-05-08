 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import com.sun.squawk.util.MathUtils;
 import edu.stuy.Constants;
 import edu.stuy.util.Gamepad;
 import edu.stuy.util.Sonar;
 import edu.wpi.first.wpilibj.ADXL345_I2C;
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.PIDController;
 import edu.wpi.first.wpilibj.PIDOutput;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The robot drivetrain.
  * @author kevin,arfan
  */
 public class Drivetrain {
     
     private static Drivetrain instance;
     private RobotDrive drivetrain;
     private Gyro gyro;
     private Sonar sonar;
     private Compressor compressor;
     PIDController straightController;
 
     private Drivetrain() {
         drivetrain = new RobotDrive(Constants.DRIVETRAIN_LEFT_1_CHANNEL, Constants.DRIVETRAIN_LEFT_2_CHANNEL, Constants.DRIVETRAIN_RIGHT_1_CHANNEL, Constants.DRIVETRAIN_RIGHT_2_CHANNEL);
         drivetrain.setSafetyEnabled(false);
         sonar = new Sonar(Constants.SONAR_CHANNEL,Constants.ANALOG_SUPPLY_VOLTAGE_CHANNEL);
         sonar.start();
         gyro = new Gyro(Constants.GYRO_CHANNEL);
         gyro.setSensitivity(0.007);
         startOver();
         compressor = new Compressor(Constants.PRESSURE_SWITCH_CHANNEL, Constants.COMPRESSOR_RELAY_CHANNEL);
         compressor.start();
         
        straightController = new PIDController(Constants.PVAL_D, Constants.IVAL_D, Constants.DVAL_D, gyro, new PIDOutput() {
             public void pidWrite(double output) {
               drivetrain.arcadeDrive(0, output);
             }
         }, 0.005);
         straightController.setInputRange(-360.0, 360.0);
         straightController.setPercentTolerance(1 / 90. * 100);
         straightController.disable();
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
 
     public Sonar getSonar() {
         return sonar;
     }
     
     public double getSonarDistance() {
         return sonar.getDistance();
     }
     
     public void putDistance() {
         SmartDashboard.putNumber("Sonar distance:", sonar.getDistance());
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
     
     public void stopCompressor() {
         compressor.stop();
     }
  
     public boolean getPressure() {
         return compressor.getPressureSwitchValue();
     }
     
     public void enableDriveStraight(){
            straightController.setSetpoint(0);
            straightController.enable();
     }
 
     public void disableDriveStraight(){
        straightController.disable();
     }
 }
