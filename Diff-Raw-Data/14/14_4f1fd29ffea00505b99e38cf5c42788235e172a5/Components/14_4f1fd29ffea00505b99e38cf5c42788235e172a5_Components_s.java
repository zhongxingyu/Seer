 package com.edinarobotics.zed;
 
 import com.edinarobotics.zed.subsystems.Drivetrain;
 import com.edinarobotics.zed.subsystems.DrivetrainRotation;
 import com.edinarobotics.zed.subsystems.DrivetrainStrafe;
 import com.edinarobotics.zed.subsystems.Shooter;
 
 public class Components {
     private static Components instance;
     
     //Drivetrain constants
    private static final int FRONT_LEFT_DRIVE_JAGUAR = 2;
     private static final int FRONT_RIGHT_DRIVE_JAGUAR = 1;
    private static final int BACK_LEFT_DRIVE_JAGUAR = 3;
     private static final int BACK_RIGHT_DRIVE_JAGUAR = 4;
     
     //Shooter constants
     private static final int SHOOTER_JAGUAR_FIRST = 1;
     private static final int SHOOTER_JAGUAR_SECOND = 2;
     private static final int SHOOTER_ANGLING_JAGUAR = 3;
     
     //Subsystem objects
     public final Drivetrain drivetrain;
     public final DrivetrainStrafe drivetrainStrafe;
     public final DrivetrainRotation drivetrainRotation;
     public final Shooter shooter;
     
     /**
      * Private constructor for the Components singleton. This constructor
      * is only called once and handles creating all the robot subsystems.
      */
     private Components(){
         drivetrain = new Drivetrain(FRONT_LEFT_DRIVE_JAGUAR, FRONT_RIGHT_DRIVE_JAGUAR,
                 BACK_LEFT_DRIVE_JAGUAR, BACK_RIGHT_DRIVE_JAGUAR);
         drivetrainStrafe = new DrivetrainStrafe(drivetrain);
         drivetrainRotation = new DrivetrainRotation(drivetrain);
         shooter = new Shooter(SHOOTER_JAGUAR_FIRST, SHOOTER_JAGUAR_SECOND, SHOOTER_ANGLING_JAGUAR);
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
