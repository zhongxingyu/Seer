 package edu.wpi.first.wpilibj.templates;
 
 /**
  * The RobotMap is a mapping from the ports sensors and actuators are wired into
  * to a variable name. This provides flexibility changing wiring, makes checking
  * the wiring easier and significantly reduces the number of magic numbers
  * floating around.
  * @author 2399 Programmers
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
     
     //motors:
     public static int feedMotor = 5;
     public static int loadMotor = 3;
     public static int leftFront = 6;
     public static int leftRear = 3;
     public static int rightFront = 5;
     public static int rightRear = 9;
     public static int pitchMotor = 2;
     public static int yawMotor = 8;
     public static int SmackMotor = 2;
     public static int shootMotor = 4;
     public static int shootMotor2 = 1;
     
     public static final int leftEncoderA = 1;
     public static final int leftEncoderB = 2;
     public static final int rightEncoderA = 3;
     public static final int rightEncoderB = 7;
    public static final int pitchEncoder = 14;
     public static final int yawEncoderA = 15;
     public static final int yawEncoderB = 16;
    public static final int smackDownSwitch = 7;
     public static final int liftUpSwitch = 8;
     
    public static final int topSensor = 54;
    public static final int middleSensor = 55;
    public static final int bottomSensor = 56;
     
     
 }
