 package com.edinarobotics.zed.commands;
 
 import com.edinarobotics.utils.gamepad.FilterSet;
 import com.edinarobotics.utils.gamepad.Gamepad;
 import com.edinarobotics.utils.gamepad.GamepadResult;
 import com.edinarobotics.utils.gamepad.filters.DeadzoneFilter;
 import com.edinarobotics.utils.gamepad.filters.ScalingFilter;
 import com.edinarobotics.zed.Components;
 import com.edinarobotics.zed.Controls;
 import com.edinarobotics.zed.subsystems.Drivetrain;
 import com.edinarobotics.zed.subsystems.DrivetrainStrafe;
 import edu.wpi.first.wpilibj.command.Command;
 
 /**
  * This {@link Command} allows a gamepad to control the {@link Drivetrain}.
  * It gets the current values from the gamepad and sends them to the drivetrain.
  */
 public class GamepadDriveStrafeCommand extends Command{
     private static final String COMMAND_NAME = "GamepadDriveStrafe";
    private Controls controls;
     Gamepad gamepad;
     FilterSet filters;
     DrivetrainStrafe drivetrainStrafe;
     
     /**
      * Construct a new GamepadDriveCommand using the given gamepad, filters
      * and drivetrain.
      * @param gamepad The Gamepad object to read for control values.
      * @param filters The set of filters to use when filtering gamepad output.
      * @param drivetrain The drivetrain object to control.
      */
     public GamepadDriveStrafeCommand(Gamepad gamepad, FilterSet filters){
         super(COMMAND_NAME);
         this.gamepad = gamepad;
         this.filters = filters;
         this.drivetrainStrafe = Components.getInstance().drivetrainStrafe;
        controls = Controls.getInstance();
         requires(drivetrainStrafe);
     }
     
     /**
      * Constructs a new GamepadDriveCommand using the given gamepad and
      * drivetrain. This command will use a default set of filters.
      * @param gamepad The Gamepad object to read for control values.
      * @param drivetrain The drivetrain object to control.
      */
     public GamepadDriveStrafeCommand(Gamepad gamepad){
         super(COMMAND_NAME);
         filters = new FilterSet();
         filters.addFilter(new DeadzoneFilter(0.15));
         filters.addFilter(new ScalingFilter());
         this.gamepad = gamepad;
         this.drivetrainStrafe = Components.getInstance().drivetrainStrafe;
        controls = Controls.getInstance();
         requires(drivetrainStrafe);
     }
 
     protected void initialize() {
     }
 
     /**
      * Submits values from the given {@code gamepad} to the given
      * {@code drivetrain}.
      */
     protected void execute() {
         GamepadResult gamepadState = filters.filter(gamepad.getJoysticks());
        if(controls.driveStateForward) {
             drivetrainStrafe.mecanumPolarStrafe(gamepadState.getLeftMagnitude(),
                     gamepadState.getLeftDirection());
         } else {
             drivetrainStrafe.mecanumPolarStrafe(gamepadState.getLeftMagnitude(),
                     gamepadState.getLeftDirection() + 180);
         }
     }
 
     protected boolean isFinished() {
         return false;
     }
 
     protected void end() {
         
     }
 
     protected void interrupted() {
         end();
     }
 }
