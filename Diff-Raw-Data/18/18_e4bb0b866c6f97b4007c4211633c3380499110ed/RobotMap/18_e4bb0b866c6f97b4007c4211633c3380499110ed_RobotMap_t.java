 package org.usfirst.frc2022;
 
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
     //public static final LiveWindow liveWindow = new LiveWindow();
     /**
      * PWM Port Numbers for the Digital Sidecar Used for Jaguars, Victors,
      * Talons, and Servos
      */
     // Jaguars
     private static final int jagUpperLeft = 1;
    private static final int jagUpperRight = 3;
    private static final int jagLowerLeft = 2;
    private static final int jagLowerRight = 4;
     public static final int[] portsJaguar = {jagUpperLeft, jagUpperRight, jagLowerLeft, jagLowerRight};
     public static final int shooterJaguar = 9;
     public static final int handlingsolenoid = 5;
     
     //Sync group for CAN Jaguars
     public static final byte syncGroup = 0;
     
     // Servos
     //public static final int rotateServo = 6;
     //public static final int pitchServo = 7;
     public static final int rotate_motor = 6;
     public static final int pitch_motor = 7;
     
     
     // Analog Sensors (Cherry, Ultrasound, etc.)
     public static final int cherry_rotate = 1;
     public static final int cherry_pitch = 2;
     
     
     // Digital I/O Ports (Encoders and Limit Switches)
     public static final int[] shooterEndcoder = {3, 4};
     public static final int[] myEncoder = {9, 10};
     public static final int[] DriveEncoder1 = {5,6};
     public static final int[] DriveEncoder2 = {7,8};
    
     // Enumeration of Sweet Spots
     public static final int behindPyramid = 0;
     public static final int rightOfPyramid = 1;
     public static final int feederSpot = 2;
     
     
     // Digital Relay Ports (Spike Relays)
     public static final int pickupPortNum = 2;
     public static final int[] pickupPorts = {1,2};
     public static final int shooterSpike = 3;
     public static final int handlingSpike = 4;
 }
