 package org.usfirst.frc3946.UltimateAscent;
 
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
     
     //Joysticks
     public static final int xboxController = 1;
     public static final int leftJoystick = 2;
     public static final int rightJoystick = 3;
     public static final int cypress = 4;
     
     //Buttons
    //public static final int Button = XboxController.ButtonType.kX.value;
    public static final int fireDisk = XboxController.ButtonType.kL.value;
     
     //Motors
     public static final int leftJaguar = 1;
     public static final int rightJaguar = 2;
     
     //Relays (Spikes)
     //public static final int liftSpike = 1;
     
     //Solenoids
     public static final int firePistonRetract = 1;
     public static final int firePistonExtend = 2;
    
     //Digital
     
     //Analog
     
     
     
 }
