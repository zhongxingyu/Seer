 package com.frc4343.robot;
 
 import com.frc4343.robot.commands.*;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.buttons.DigitalIOButton;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 
 /**
  * This class is the glue that binds the controls on the physical operator
  * interface to the commands and command groups that allow control of the robot.
  */
 public class OI {
     // Sets stick to a new joystick at JOYSTICK_PORT
     Joystick stick = new Joystick(Constants.JOYSTICK_PORT); // Instance of the current joystick.

     JoystickButton CATAPULT_WIND = new JoystickButton(stick, Constants.CATAPULT_WIND_BUTTON);
     JoystickButton CATAPULT_UNWIND = new JoystickButton(stick, Constants.CATAPULT_UNWIND_BUTTON);
     //JoystickButton SOLENOID_ONE_TOGGLE = new JoystickButton(stick, SOLENOID_ONE_BUTTON);
     JoystickButton SOLENOID_TWO_TOGGLE = new JoystickButton(stick, Constants.SOLENOID_TWO_BUTTON);
     JoystickButton BRIDGE_RAISE = new JoystickButton(stick, Constants.BRIDGE_RAISE_BUTTON);
     JoystickButton BRIDGE_LOWER = new JoystickButton(stick, Constants.BRIDGE_LOWER_BUTTON);
     JoystickButton TRIGGER_RELEASE = new JoystickButton(stick, Joystick.ButtonType.kTrigger.value);
     JoystickButton BALL_PICKUP = new JoystickButton(stick, Constants.PICKUP_BUTTON);
     JoystickButton BALL_RELEASE = new JoystickButton(stick, Constants.UNPICKUP_BUTTON);
     JoystickButton CAMERA_LIGHT = new JoystickButton(stick, Constants.CAMERA_LIGHT_BUTTON);
     JoystickButton CAMERA_TARGET = new JoystickButton(stick, Constants.CAMERA_TARGET_BUTTON);
     JoystickButton AUTO_FIRE = new JoystickButton(stick, Constants.AUTO_FIRE_BUTTON);
 
     public OI() {
         CATAPULT_WIND.whenPressed(new CatapultRotate(-1.0));
         CATAPULT_UNWIND.whenPressed(new CatapultRotate(1.0));
         //SOLENOID_ONE_TOGGLE.whenPressed(new ToggleSolenoid(Constants.SOLENOID_ONE));
         SOLENOID_TWO_TOGGLE.whenPressed(new ToggleSolenoid(Constants.SOLENOID_TWO));
         BRIDGE_RAISE.whenPressed(new BridgeRotate(Constants.BRIDGE_TIME, -0.25));
         BRIDGE_LOWER.whenPressed(new BridgeRotate(Constants.BRIDGE_TIME, 0.25));
         TRIGGER_RELEASE.whenPressed(new CatapultRelease());
         AUTO_FIRE.whenPressed(new Fire());
         BALL_PICKUP.whenPressed(new TogglePickup(1.0));
         BALL_RELEASE.whenPressed(new TogglePickup(-1.0));
         CAMERA_LIGHT.whenPressed(new ToggleLight());
         CAMERA_TARGET.whileHeld(new TargetWithCamera());
     }
 
     // Used to get the current joystick.
     public Joystick getJoystick() {
         return stick;
     }
 }
