 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy;
 
 /**
  * Port numbers and constants.
  * @author kevin
  */
 public class Constants {
     /* USB PORTS */
     public static final int DRIVER_PAD_PORT = 1;
     public static final int OPERATOR_PAD_PORT = 2;
     
     /* PWM OUTPUTS */
     public static final int DRIVETRAIN_LEFT_CHANNEL = 1;
     public static final int DRIVETRAIN_RIGHT_CHANNEL = 2;
     public static final int ACQUIRER_CHANNEL = 5;
     public static final int SHOOTER_CHANNEL = 6;
     public static final int TILTER_CHANNEL = 8;
     public static final int CONVEYOR_CHANNEL = 9;
     public static final int WENCH_CHANNEL = 10;
    
     /* RELAY OUTPUTS */
     public static final int COMPRESSOR_RELAY_CHANNEL = 1;
     public static final int CAMERA_AND_DIRECTION_RELAY_CHANNEL = 2;
     public static final int SIGNAL_LIGHT_RELAY_CHANNEL = 3;
     
     /* GPIO */
     public static final int DRIVE_ENCODER_LEFT_A = 1;
     public static final int DRIVE_ENCODER_LEFT_B = 2;
     public static final int DRIVE_ENCODER_RIGHT_A = 3;
     public static final int DRIVE_ENCODER_RIGHT_B = 4;
     public static final int TILT_ENCODER_A = 5;
     public static final int TILT_ENCODER_B = 6;
     public static final int UPPER_CONVEYOR_SENSOR = 9;
     public static final int LOWER_CONVEYOR_SENSOR = 10;
     public static final int PRESSURE_SWITCH_CHANNEL = 11;
     
     /* I2C */
     public static final int ACCELEROMETER_CHANNEL = 1;
      
     /* ANALOG INPUTS */
     public static final int GYRO_CHANNEL = 1;
     
     /* SOLDENOID */
     public static final int SHOOTER_PLUNGER_OUT = 1;
     public static final int SHOOTER_PLUNGER_IN = 2;
     
     /* SHOOTING ANGLES */
     public static final int DUMMY_ANGLE = 69;
     
     /* DRIVETRAIN PID VALUES */
     public static final double PVAL_D = .01;
     public static final double IVAL_D = .01;
     public static final double DVAL_D = .01;
     
     private static final double ENCODER_PULSE_PER_REV = 360; // From encoder
    private static final double GEAR_RATIO = 42/39; // Wheels turn 39 times every time gear turns 42
     private static final double WHEEL_RADIUS = 2.0; // inches of the wheel radius
     public static final double ENCODER_DISTANCE_PER_PULSE = 1 / (ENCODER_PULSE_PER_REV * GEAR_RATIO / ( 2 * Math.PI * WHEEL_RADIUS));
     
     /* TILTER CONSTANTS */
     public static final double DISTANCE_TO_LEADSCREW_BASE = 10;
     public static final double SHOOTER_DISTANCE_TO_LEADSCREW = 16;
     public static final double LEADSCREW_HEIGHT = 5;
     public static final double TILTER_INCHES_PER_REV = 0.1;
     public static final double TILTER_DISTANCE_PER_PULSE = TILTER_INCHES_PER_REV / ENCODER_PULSE_PER_REV;
     
     /* The DESarray */
     public static final char DES[] = {'i', 'r', 'e', 'e'};
 }
