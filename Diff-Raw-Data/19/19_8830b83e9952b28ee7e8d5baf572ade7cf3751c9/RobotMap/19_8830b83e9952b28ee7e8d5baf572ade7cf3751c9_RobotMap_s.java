 package edu.wpi.first.wpilibj.templates;
 
 /**
  * The RobotMap is a mapping from the ports sensors and actuators are wired into
  * to a variable name. This provides flexibility changing wiring, makes checking
  * the wiring easier and significantly reduces the number of magic numbers
  * floating around.
  */
 public class RobotMap {
     // For example to map the left and right motors, you could define the
     // following variables to use with your drivetrain subsystem.
     // public static final int leftMotor = 1;
     // public static final int rightMotor = 2;
     
     // If you are using multiple modules, make sure to define both the port
     // number and the module. For example you with a rangefinder:
     // public static final int rangefinderPort = 1;
     // public static final int rangefinderModule = 1;
 
     //////////////////////////
     //  ___         _       //
     // | _ \___ _ _| |_ ___ //
     // |  _/ _ \ '_|  _(_-< //
     // |_| \___/_|  \__/__/ //
     //                      //
     //////////////////////////
     
     // Joysticks
     public static final int LEFT_STICK_PORT = 1;
     public static final int RIGHT_STICK_PORT = 2;
     
     // Jaguars
     public static final int LEFT_MOTOR_PORT = 3;
     public static final int RIGHT_MOTOR_PORT = 4;
     
     // Victors
     public static final int FRONT_LAUNCHER_MOTOR = 9;
     public static final int REAR_LAUNCHER_MOTOR = 8;
     public static final int LAUNCHER_ADVANCER = 7;
     
     //Servos
     
     
     //Solenoids
     
     
     // Relays
     
     
     // Sensors
    

     
     // Speeds
     public static final double FRONT_LAUNCHER_SPEED = 1.0;
     public static final double REAR_LAUNCHER_SPEED = 0.8;
     public static final double FLYWHEEL_SPINUP_TIME = 2;
     
     public static final double LAUNCHER_ADVANCE_SPEED = 0.75;
     public static final double LAUNCHER_FORWARD_TIME = 1.0;
     public static final double LAUNCHER_REVERSE_TIME = 1.0;
     
     public static final double DEFAULT_DRIVING_SPEED = 0.5;
     public static final double DEFAULT_SPEED_INDEX = 0;
     public static final double SLOW_DRIVING_SPEED = 0.2;
     public static final double SLOW_SPEED_INDEX = 1;
     public static final double FAST_DRIVING_SPEED = 1.0;
     public static final double FAST_SPEED_INDEX = 2;
     
     public final static double[] SPEED_ARRAY = { RobotMap.DEFAULT_DRIVING_SPEED, 
                                                  RobotMap.SLOW_DRIVING_SPEED, 
                                                  RobotMap.FAST_DRIVING_SPEED };
     
     // Times
     
     
     // Constants
     public static final int FORWARD = 1;
     public static final int REVERSE = -1;
     public static final int STOPPED = 0;
     
     
     // Left Joystick Buttons
     
     
     
     // Right Joystick Buttons
     public static final int LAUNCHER_TOGGLE_BUTTON = 3;
     public static final int LAUNCHER_ADVANCE_BUTTON = 2;
     
     
     // Camera
     
     
 }
