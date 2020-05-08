 package edu.wpi.first.wpilibj.templates;
 
 /**
  * The RobotMap is a mapping from the ports sensors and actuators are wired into
  * to a variable name. This provides flexibility changing wiring, makes checking
  * the wiring easier and significantly reduces the number of magic numbers
  * floating around.
  */
 public class RobotMap {
     //PWM ports
     public static final int driveFrontLeft = 1;
     public static final int driveRearLeft = 2;
     public static final int driveFrontRight = 4;
     public static final int driveRearRight = 3;
     public static final int shooterLaunch = 5;
     public static final int shooterInjector = 6;
    public static final int shooterIndexer = 7; // PWM port for the servo that indexes frisbees
     public static final int shooterArticulator = 8;
     public static final int climberBelt = 10;
     public static final int climberShoulder = 9;
     
     //Analog ports
     public static final int baseGyro = 1; // Analog port of gyro on robot base
     
     //Digital ports
     public static final int shooterTopLimit = 1; // DIO port of the limit switch for the articulator at highest angle.
     public static final int shooterBottomLimit = 5; // DIO port of the limit switch for the articulator at lowest angle.
     public static final int shooterEncoderA = 3; // DIO port of the "A" channel of the quadrature encoder on the articulator.
     public static final int shooterEncoderB = 4; // DIO port of the "B" channel of the quadrature encoder on the articulator.
     public static final int injectorLimit = 2;
     
     //Solenoid ports
     public static final int shoulderLock = 1; // The solenoid that holds the cables restraining the arm.
     // If you are using multiple modules, make sure to define both the port
     // number and the module. For example you with a rangefinder:
     // public static final int rangefinderPort = 1;
     // public static final int rangefinderModule = 1;
 }
