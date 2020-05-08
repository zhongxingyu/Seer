 package org.usfirst.frc4682.Audacity;
 
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import edu.wpi.first.wpilibj.Joystick;
 import org.usfirst.frc4682.Audacity.commands.*;
 
 /**
  * This class is the glue that binds the controls on the physical operator
  * interface to the commands and command groups that allow control of the robot.
  */
 public class OI {
     public Joystick leftStick = new Joystick(1);
     public Joystick rightStick = new Joystick(2);
     public Joystick thirdStick = new Joystick(3);
 
     Button shootingButton = new JoystickButton(thirdStick, 2);
     Button firingButton = new JoystickButton(thirdStick, 1);
     
     Button firstDisable = new JoystickButton(leftStick, 11);
     Button secondDisable = new JoystickButton(rightStick, 11);
     Button thirdDisable = new JoystickButton(thirdStick, 11);
     
     Button firstDisableAlt = new JoystickButton(leftStick, 10);
     Button secondDisableAlt = new JoystickButton(rightStick, 10);
     Button thirdDisableAlt = new JoystickButton(thirdStick, 10);
     
     Button firstEnable = new JoystickButton(leftStick, 6);
     Button secondEnable = new JoystickButton(rightStick, 6);
     Button thirdEnable = new JoystickButton(thirdStick, 6);
     
     Button firstEnableAlt = new JoystickButton(leftStick, 7);
     Button secondEnableAlt = new JoystickButton(rightStick, 7);
     Button thirdEnableAlt = new JoystickButton(thirdStick, 7);
     
     
     public OI() {
         shootingButton.whenPressed(new TuneShooter());
         firingButton.whenPressed(new Feed());
         
         firstDisable.whenPressed(new StopAll());
         secondDisable.whenPressed(new StopAll());
         thirdDisable.whenPressed(new StopAll());
         
         firstDisableAlt.whenPressed(new StopAll());
         secondDisableAlt.whenPressed(new StopAll());
         thirdDisableAlt.whenPressed(new StopAll());
         
        firstEnable.whenPressed(new RestartDrive());
        secondEnable.whenPressed(new RestartDrive());
        thirdEnable.whenPressed(new RestartDrive());
         
        firstEnableAlt.whenPressed(new RestartDrive());
        secondEnableAlt.whenPressed(new RestartDrive());
        thirdEnableAlt.whenPressed(new RestartDrive());
         
     }
     
     public double getFirstThrottle() { // returns 0 < double < 1
         return getThrottle(leftStick);
     }
     
     public double getSecondThrottle() {
         return getThrottle(rightStick);
     }
     
     public double getThirdThrottle() {
         return getThrottle(thirdStick);
     }
     public double getThrottle(Joystick stick) {
         return (-(stick.getRawAxis(3) + 1)/2) + 1;
         
     }
 
 }
