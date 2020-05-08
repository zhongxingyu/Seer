 package org.usfirst.frc2022;
 
 import org.usfirst.frc2022.commands.AutoAimCommand;
 
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import org.usfirst.frc2022.Joysticks.Attack3;
 import org.usfirst.frc2022.Joysticks.Xbox360;
 import org.usfirst.frc2022.commands.HandlingCommand;
 import org.usfirst.frc2022.commands.InjectionCommand;
 import org.usfirst.frc2022.commands.SweetSpotCommand;
 
 public class OI {
 
     // Joystick Declarations
     // @see org.usfirst.frc2022.Joysticks
     private Xbox360 xbawks;
     private Attack3 attack;
     
     // Joystick Button Declarations
     // Map these buttons to commands
     private JoystickButton targetTrackerButton;
     private JoystickButton injectorButton;
     private JoystickButton behindPyramidSweetSpotButton;
     private JoystickButton rightSweetSpotButton;
     private JoystickButton feederSweetSpotButton;
     private JoystickButton mrAttackTheesButton4Handling;
     private boolean Pressed;
 
     
     public OI() {
         
         // Initialize Joysticks with port numbers
         xbawks = new Xbox360(1);
         attack = new Attack3(2);
         
         // Initialize all Joystick Buttons
         injectorButton = attack.GetButton(1);
        behindPyramidSweetSpotButton = attack.GetButton(3);
        rightSweetSpotButton = attack.GetButton(4);
        feederSweetSpotButton = attack.GetButton(5);
        targetTrackerButton = attack.GetButton(2);
         mrAttackTheesButton4Handling = attack.GetButton(10);
         Pressed = false;
         
         /**
          * Map buttons to commands.
          * whileHeld() executes a command and then calls Interrupted() when
          * button is released.
          * 
          * whenPressed() executes a command once after button is pressed and
          * released
          */
        targetTrackerButton.whileHeld(new AutoAimCommand());
         injectorButton.whileHeld(new InjectionCommand());
         behindPyramidSweetSpotButton.whileHeld(new SweetSpotCommand(0));
         rightSweetSpotButton.whileHeld(new SweetSpotCommand(1));
         feederSweetSpotButton.whileHeld(new SweetSpotCommand(2));
         mrAttackTheesButton4Handling.whileHeld(new HandlingCommand());
     }
 
     // Getter functions for all Joysticks
     public Xbox360 getXbawks() {
         return xbawks;
     }
     
     public Attack3 getAttack3() {
         return attack;
     }
     
     public boolean getAutoAimState(){
         return targetTrackerButton.get();
     }
 }
