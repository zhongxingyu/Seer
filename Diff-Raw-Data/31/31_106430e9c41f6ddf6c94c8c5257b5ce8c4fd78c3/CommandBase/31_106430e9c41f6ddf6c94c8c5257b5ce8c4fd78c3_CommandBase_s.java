 package edu.wpi.first.wpilibj.templates.commands;
 
 import edu.wpi.first.wpilibj.command.Command;
 import edu.wpi.first.wpilibj.templates.OI;
 import edu.wpi.first.wpilibj.templates.subsystems.*;
 
 /**
  * The base for all commands. All atomic commands should subclass CommandBase.
  * CommandBase stores creates and stores each control system.
  */
 public abstract class CommandBase extends Command {
 
     /**
      * SubSystems.
      */
    public static ShooterMotors shooterMotors = new ShooterMotors();
    public static ShooterSolenoid shooterSolenoid = new ShooterSolenoid();
    public static PressureSwitch pressureSwitch = new PressureSwitch();
     public static Compressor compressor = new Compressor();
     public static Camera mainCamera = new Camera();
     public static Climber climber = new Climber();
     public static GroundDrive groundDrive = new GroundDrive();
     public static RunClimber runClimber = new RunClimber();
     public static ClimberLimitSwitch climberLimitSwitch = new ClimberLimitSwitch();
     public static TestMotors testMotors = null;// new TestMotors(); I think we have GroundDrive already doing this.
     public static OI oi;
 
     public static void init() {
         // This MUST be here. If the OI creates Commands (which it very likely
         // will), constructing it during the construction of CommandBase (from
         // which commands extend), subsystems are not guaranteed to be
         // yet. Thus, their requires() statements may grab null pointers. Bad
         // news. Don't move it.
         oi = new OI();
     }
 
     public CommandBase(String name) {
         super(name);
     }
 
     public CommandBase() {
         super();
     }
 }
