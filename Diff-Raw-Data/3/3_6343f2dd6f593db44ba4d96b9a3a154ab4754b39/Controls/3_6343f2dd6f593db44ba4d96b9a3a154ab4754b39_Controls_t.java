 package com.edinarobotics.zed;
 
 import com.edinarobotics.utils.gamepad.Gamepad;
 import com.edinarobotics.zed.commands.*;
 import com.edinarobotics.zed.subsystems.Auger;
 import com.edinarobotics.zed.subsystems.Collector;
 import com.edinarobotics.zed.subsystems.Conveyor;
 import com.edinarobotics.zed.subsystems.Lifter;
 import com.edinarobotics.zed.subsystems.Shooter;
 
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
         //Drivetrain Direction Toggle
         gamepad1.DIAMOND_LEFT.whenPressed(new ToggleDrivetrainDirectionCommand());
         //Conveyor
         gamepad1.LEFT_TRIGGER.whenPressed(new SetConveyorCommand(Conveyor.CONVEYOR_COLLECT_IN));
         gamepad1.LEFT_TRIGGER.whenReleased(new SetConveyorCommand(Conveyor.CONVEYOR_STOP));
         gamepad1.LEFT_BUMPER.whenPressed(new SetConveyorCommand(Conveyor.CONVEYOR_COLLECT_OUT));
         gamepad1.LEFT_BUMPER.whenReleased(new SetConveyorCommand(Conveyor.CONVEYOR_STOP));
         //Collector
         gamepad1.RIGHT_TRIGGER.whenPressed(new SetCollectorCommand(Collector.CollectorDirection.COLLECTOR_IN));
         gamepad1.RIGHT_TRIGGER.whenReleased(new SetCollectorCommand(Collector.CollectorDirection.COLLECTOR_STOP));
         gamepad1.RIGHT_BUMPER.whenPressed(new SetCollectorCommand(Collector.CollectorDirection.COLLECTOR_OUT));
         gamepad1.RIGHT_BUMPER.whenReleased(new SetCollectorCommand(Collector.CollectorDirection.COLLECTOR_STOP));
         gamepad1.DIAMOND_UP.whenPressed(new SetCollectorCommand(Collector.CollectorLiftDirection.COLLECTOR_LIFT_UP));
         gamepad1.DIAMOND_UP.whenReleased(new SetCollectorCommand(Collector.CollectorLiftDirection.COLLECTOR_LIFT_STOP));
         gamepad1.DIAMOND_DOWN.whenPressed(new SetCollectorCommand(Collector.CollectorLiftDirection.COLLECTOR_LIFT_DOWN));
         gamepad1.DIAMOND_DOWN.whenReleased(new SetCollectorCommand(Collector.CollectorLiftDirection.COLLECTOR_LIFT_STOP));
         //One-Joystick Strafe
         gamepad1.DPAD_UP.whileHeld(new SetDrivetrainCommand(ONE_JOYSTICK_MAGNITUDE, 0, 0));
         gamepad1.DPAD_UP.whenReleased(new SetDrivetrainCommand(0, 0, 0));
         gamepad1.DPAD_DOWN.whileHeld(new SetDrivetrainCommand(ONE_JOYSTICK_MAGNITUDE, 180, 0));
         gamepad1.DPAD_DOWN.whenReleased(new SetDrivetrainCommand(0, 0, 0));
         gamepad1.DPAD_LEFT.whileHeld(new SetDrivetrainCommand(ONE_JOYSTICK_MAGNITUDE, -90, 0));
         gamepad1.DPAD_LEFT.whenReleased(new SetDrivetrainCommand(0, 0, 0));
         gamepad1.DPAD_RIGHT.whileHeld(new SetDrivetrainCommand(ONE_JOYSTICK_MAGNITUDE, 90, 0));
         gamepad1.DPAD_RIGHT.whenReleased(new SetDrivetrainCommand(0, 0, 0));
         
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
         gamepad2.DIAMOND_UP.whileHeld(new VisionTrackingCommand(VisionTrackingCommand.HIGH_GOAL));
         gamepad2.DIAMOND_DOWN.whileHeld(new VisionTrackingCommand(VisionTrackingCommand.MIDDLE_GOAL));
         gamepad2.DIAMOND_LEFT.whileHeld(new VisionTrackingCommand(VisionTrackingCommand.ANY_GOAL));
        gamepad2.DIAMOND_RIGHT.whileHeld(new FixedPointVisionTrackingCommand(0.0273556, -0.29411, VisionTrackingCommand.HIGH_GOAL));
         //Lifter
         gamepad2.DPAD_UP.whenPressed(new SetLifterCommand(Lifter.LifterDirection.LIFTER_UP));
         gamepad2.DPAD_UP.whenReleased(new SetLifterCommand(Lifter.LifterDirection.LIFTER_STOP));
         gamepad2.DPAD_DOWN.whenPressed(new SetLifterCommand(Lifter.LifterDirection.LIFTER_DOWN));
         gamepad2.DPAD_DOWN.whenReleased(new SetLifterCommand(Lifter.LifterDirection.LIFTER_STOP));
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
