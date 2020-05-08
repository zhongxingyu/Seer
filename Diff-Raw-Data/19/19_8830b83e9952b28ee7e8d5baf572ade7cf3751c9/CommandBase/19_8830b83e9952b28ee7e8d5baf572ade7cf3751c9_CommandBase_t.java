 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import edu.wpi.first.wpilibj.templates.OI;
import edu.wpi.first.wpilibj.templates.subsystems.Climber;
 import edu.wpi.first.wpilibj.templates.subsystems.DriveTrain;
 import edu.wpi.first.wpilibj.templates.subsystems.Launcher;
 
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
     public static Launcher launcher = new Launcher();
    public static Climber climber = new Climber();
 
     public static void init() {
         // This MUST be here. If the OI creates Commands (which it very likely
         // will), constructing it during the construction of CommandBase (from
         // which commands extend), subsystems are not guaranteed to be
         // yet. Thus, their requires() statements may grab null pointers. Bad
         // news. Don't move it.
         oi = new OI();
 
         // Show what command your subsystem is running on the SmartDashboard
         SmartDashboard.putData(driveTrain);
     }
 
     public CommandBase(String name) {
         super(name);
     }
 
     public CommandBase() {
         super();
     }
 }
