 package com.edinarobotics.zed;
 
 import com.edinarobotics.utils.gamepad.Gamepad;
 import com.edinarobotics.zed.commands.*;
 import com.edinarobotics.zed.subsystems.Auger;
 import com.edinarobotics.zed.subsystems.Collector;
 import com.edinarobotics.zed.subsystems.Conveyor;
 import com.edinarobotics.zed.subsystems.Lifter;
 import com.edinarobotics.zed.subsystems.Shooter;
 import com.edinarobotics.zed.vision.TargetType;
 
 /**
  * Controls handles creating the {@link Gamepad} objects
  * used to control the robot as well as binding the proper Commands
  * to button actions.
  */
 public class Controls {
     private static Controls instance;
     private static final double ONE_JOYSTICK_MAGNITUDE = 1;
     
     public final Gamepad gamepad1;
     public final Gamepad gamepad2;
     
     private Controls(){
         gamepad1 = new Gamepad(1);
         //Trigger Drive
         gamepad1.RIGHT_TRIGGER.whileHeld(new SetDrivetrainStrafeCommand(ONE_JOYSTICK_MAGNITUDE, 0));
         gamepad1.RIGHT_TRIGGER.whenReleased(new SetDrivetrainStrafeCommand(0, 0));
         gamepad1.LEFT_TRIGGER.whileHeld(new SetDrivetrainStrafeCommand(ONE_JOYSTICK_MAGNITUDE, 180));
         gamepad1.LEFT_TRIGGER.whenReleased(new SetDrivetrainStrafeCommand(0, 0));
         //Lifter
         gamepad1.DPAD_UP.whenPressed(new SetLifterCommand(Lifter.LIFTER_UP));
         gamepad1.DPAD_UP.whenReleased(new SetLifterCommand(Lifter.LIFTER_STOP));
         gamepad1.DPAD_DOWN.whenPressed(new SetLifterCommand(Lifter.LIFTER_DOWN));
         gamepad1.DPAD_DOWN.whenReleased(new SetLifterCommand(Lifter.LIFTER_STOP));
         
         gamepad2 = new Gamepad(2);
         //Conveyor
         gamepad2.LEFT_TRIGGER.whenPressed(new SetConveyorCommand(Conveyor.CONVEYOR_SHOOT_IN));
         gamepad2.LEFT_TRIGGER.whenReleased(new SetConveyorCommand(Conveyor.CONVEYOR_STOP));
         gamepad2.LEFT_BUMPER.whenPressed(new SetConveyorCommand(Conveyor.CONVEYOR_SHOOT_OUT));
         gamepad2.LEFT_BUMPER.whenReleased(new SetConveyorCommand(Conveyor.CONVEYOR_STOP));
         //Shooter
         gamepad2.RIGHT_TRIGGER.whenPressed(new SetShooterCommand(Shooter.SHOOTER_ON));
         gamepad2.RIGHT_TRIGGER.whenReleased(new SetShooterCommand(Shooter.SHOOTER_OFF));
         //Augers
         gamepad2.RIGHT_BUMPER.whenPressed(new AugerRotateCommand(Auger.AugerDirection.AUGER_DOWN));
         gamepad2.MIDDLE_RIGHT.whenPressed(new AugerRotateCommand(Auger.AugerDirection.AUGER_UP));
         gamepad2.MIDDLE_LEFT.whenPressed(new SetAugerCommand(Auger.AugerDirection.AUGER_STOP));
         //Vision Tracking
        gamepad2.DIAMOND_UP.whileHeld(new FixedPointVisionTrackingCommand(FixedPointVisionTrackingCommand.PYRAMID_BACK_MIDDLE, TargetType.HIGH_GOAL));
        gamepad2.DIAMOND_DOWN.whileHeld(new FixedPointVisionTrackingCommand(FixedPointVisionTrackingCommand.PYRAMID_BACK_MIDDLE, TargetType.MIDDLE_GOAL));
         //Lifter
         gamepad2.DPAD_UP.whenPressed(new SetLifterCommand(Lifter.LIFTER_UP));
         gamepad2.DPAD_UP.whenReleased(new SetLifterCommand(Lifter.LIFTER_STOP));
         gamepad2.DPAD_DOWN.whenPressed(new SetLifterCommand(Lifter.LIFTER_DOWN));
         gamepad2.DPAD_DOWN.whenReleased(new SetLifterCommand(Lifter.LIFTER_STOP));
     }
     
     /**
      * Returns the proper instance of Controls.
      * This method creates a new Controls object the first time it is called
      * and returns that object for each subsequent call.
      * @return The current instance of Controls.
      */
     public static Controls getInstance(){
         if(instance == null){
             instance = new Controls();
         }
         return instance;
     }
 }
