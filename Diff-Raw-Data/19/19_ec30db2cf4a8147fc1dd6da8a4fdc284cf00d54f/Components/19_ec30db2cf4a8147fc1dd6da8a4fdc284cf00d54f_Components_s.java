 package com.edinarobotics.zed;
 
 import com.edinarobotics.zed.subsystems.Auger;
 import com.edinarobotics.zed.subsystems.Collector;
 import com.edinarobotics.zed.subsystems.Conveyor;
 import com.edinarobotics.zed.subsystems.Drivetrain;
 import com.edinarobotics.zed.subsystems.DrivetrainRotation;
 import com.edinarobotics.zed.subsystems.DrivetrainStrafe;
 import com.edinarobotics.zed.subsystems.Lifter;
 import com.edinarobotics.zed.subsystems.Shooter;
 
 public class Components {
     private static Components instance;
     
     //Digital Input Constants
     //Collector Constants
    private static final int COLLECTOR_UPPER_LIMIT_SWITCH = 1;
    private static final int COLLECTOR_LOWER_LIMIT_SWITCH = 2;
     //Auger Constants
    private static final int AUGER_ROTATION_SWITCH = 3;
     //Drivetrain Encoder Constants
     private static final int FRONT_LEFT_A = 4;
     private static final int FRONT_LEFT_B = 5;
     private static final int FRONT_RIGHT_A = 6;
     private static final int FRONT_RIGHT_B = 7;
     private static final int BACK_LEFT_A = 8;
     private static final int BACK_LEFT_B = 9;
     private static final int BACK_RIGHT_A = 10;
     private static final int BACK_RIGHT_B = 11;
     
     //Analog Input Constants
     //Lifter Constansts
     private static final int LIFTER_STRING_POT = 1;
     //Drivetrain Constants
     private static final int GYRO = 2;
 
     //PWM constants
     //Drivetrain constants
    private static final int FRONT_LEFT_DRIVE_JAGUAR = 3;
    private static final int FRONT_RIGHT_DRIVE_JAGUAR = 2;
    private static final int BACK_LEFT_DRIVE_JAGUAR = 4;
     private static final int BACK_RIGHT_DRIVE_JAGUAR = 1;
     //Conveyor constants
     private static final int CONVEYOR_VICTOR = 9;
     
     //CAN constants
     //Shooter constants
     private static final int SHOOTER_JAGUAR_FIRST = 2;
     private static final int SHOOTER_JAGUAR_SECOND = 1;
     
     //Relay constants
     //Auger constants
     private static final int AUGER_SPIKE = 5;
     //Lifter constants
     private static final int LIFTER_SPIKE = 3;
     //Collector constants
     private static final int COLLECTOR_LEFT_STAR_SPIKE = 1;
     private static final int COLLECTOR_RIGHT_STAR_SPIKE = 2;
     private static final int COLLECTOR_LIFTER_SPIKE = 4;
     
     //Subsystem objects
     public final Drivetrain drivetrain;
     public final DrivetrainStrafe drivetrainStrafe;
     public final DrivetrainRotation drivetrainRotation;
     public final Shooter shooter;
     public final Lifter lifter;
     public final Collector collector;
     public final Conveyor conveyor;
     public final Auger auger;
     
     /**
      * Private constructor for the Components singleton. This constructor
      * is only called once and handles creating all the robot subsystems.
      */
     private Components(){
         drivetrain = new Drivetrain(FRONT_LEFT_DRIVE_JAGUAR, FRONT_RIGHT_DRIVE_JAGUAR,
                 BACK_LEFT_DRIVE_JAGUAR, BACK_RIGHT_DRIVE_JAGUAR,
                 FRONT_LEFT_A, FRONT_LEFT_B, FRONT_RIGHT_A, FRONT_RIGHT_B,
                 BACK_LEFT_A, BACK_LEFT_B, BACK_RIGHT_A, BACK_RIGHT_B);
         drivetrainStrafe = new DrivetrainStrafe(drivetrain);
         drivetrainRotation = new DrivetrainRotation(drivetrain, GYRO);
         shooter = new Shooter(SHOOTER_JAGUAR_FIRST, SHOOTER_JAGUAR_SECOND);
        lifter = new Lifter(LIFTER_SPIKE, LIFTER_STRING_POT);
         collector = new Collector(COLLECTOR_LEFT_STAR_SPIKE, COLLECTOR_RIGHT_STAR_SPIKE,
                 COLLECTOR_LIFTER_SPIKE, COLLECTOR_UPPER_LIMIT_SWITCH, COLLECTOR_LOWER_LIMIT_SWITCH);
         conveyor = new Conveyor(CONVEYOR_VICTOR);
         auger = new Auger(AUGER_SPIKE, AUGER_ROTATION_SWITCH);
     }
     
     /**
      * Returns a new instance of {@link Components}, creating one if null.
      * @return {@link Components}
      */
     public static Components getInstance() {
         if(instance == null){
             instance = new Components();
         }
         return instance;
     }
     
 }
