 package com.slidellrobotics.reboundrumble.commands;
 
 import edu.wpi.first.wpilibj.command.Command;
 import com.slidellrobotics.reboundrumble.OI;
 import com.slidellrobotics.reboundrumble.RobotMap;
 import com.slidellrobotics.reboundrumble.subsystems.Camera;
 import com.slidellrobotics.reboundrumble.subsystems.DriveTrain;
 import com.slidellrobotics.reboundrumble.subsystems.FeedBelt;
 import com.slidellrobotics.reboundrumble.subsystems.LazySusan;
 import com.slidellrobotics.reboundrumble.subsystems.LoadPiston;
 import com.slidellrobotics.reboundrumble.subsystems.PositioningAccelerometer;
 import com.slidellrobotics.reboundrumble.subsystems.FireMotors;
 import com.slidellrobotics.reboundrumble.subsystems.Transmission;
 
 /**
  * The base for all commands. All atomic commands should subclass CommandBase.
  * CommandBase stores creates and stores each control system. To access a
  * subsystem elsewhere in your code in your code use CommandBase.exampleSubsystem
  * @author Author
  */
 public abstract class CommandBase extends Command {
 
     public static OI oi;
     // Create a single static instance of all of your subsystems
     public static DriveTrain driveTrain = new DriveTrain();
     public static Transmission transmission = new Transmission();
     public static FireMotors leftShootingMotors = new FireMotors(RobotMap.leftFireEncoder, RobotMap.leftShootingMotor);
     public static FireMotors rightShootingMotors = new FireMotors(RobotMap.rightFireEncoder, RobotMap.rightShootingMotor);
     public static FeedBelt feedBelt = new FeedBelt();
     public static LoadPiston loadPiston = new LoadPiston();
     public static PositioningAccelerometer pAccelerometer = new PositioningAccelerometer();
     public static LazySusan lazySusan = new LazySusan();
     public static Camera camera = new Camera();
 
     public static void init() {
         // This MUST be here. If the OI creates Commands (which it very likely
         // will), constructing it during the construction of CommandBase (from
         // which commands extend), subsystems are not guaranteed to be
         // yet. Thus, their requires() statements may grab null pointers. Bad
         // news. Don't move it.
         oi = new OI();
 
         // Show what command your subsystem is running on the TheDash
         
     }
 
     public CommandBase(String name) {
         super(name);
     }
 
     public CommandBase() {
         super();
     }
 }
