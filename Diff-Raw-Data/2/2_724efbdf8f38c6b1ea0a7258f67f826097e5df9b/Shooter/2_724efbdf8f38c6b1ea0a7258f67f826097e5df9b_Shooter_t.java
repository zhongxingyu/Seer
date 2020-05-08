 package edu.wpi.first.wpilibj.templates;                    // Needs to be reset:
                                                             // THREE_SECONDS;  solenoid, talon, and joystick buttons need to be changed
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.Talon;
 
public class Shooter implements IRobot 
 {
     
     /* Created by Josh Kavner
      * IN THIS CLASS, ALL METHODS NEED:
      * 
      * import edu.wpi.first.wpilibj.Talon;
      * import edu.wpi.first.wpilibj.Solenoid;
      * import edu.wpi.first.wpilibj.Joystick;
      * import edu.wpi.first.wpilibj.SimpleRobot;
      * import edu.wpi.first.wpilibj.Timer;
      * 
      * Joystick rightStick = new Joystick(2);                Right Joystick; button (see below at (.getRawButton(1/2)))
      * Talon shooterMotor = new Talon(5);                    Talon's 1-4 are for drive train
      * Solenoid shooterControlPiston = new Solenoid(1);      Piston: 1
      * Talon shooterControlMotor = new Talon (6);            Either this motor or the piston; depends on what we need 
      *                                                          to push the frisbee into the shooter                                        
      */
     
     private final int THREE_SECONDS = 300;
     private final int JOYSTICK_BUTTON_ONE = 1;
     private final int JOYSTICK_BUTTON_TWO = 2;
     
     Talon shooterMotorLeft = new Talon(5);
     Talon shooterMotorRight = new Talon(6);
     Solenoid shooterControlPiston = new Solenoid(1);
     Talon shooterControlMotor = new Talon(7);
     
     private int motorPulse = 1;
     private boolean motorState = true;
     private int countTimer = 1;
     private boolean pushMotorState = false;
     private boolean pushPistonState = false;
             
     public void SetStraightShooter() {
         if (rightStick.getRawButton(JOYSTICK_BUTTON_ONE) == true && motorState == true)        // if the button is pressed once,                                i=     controller button     a=
         {                                                                                      // and if arbitrary integer "motorState" is equal to true                           
             motorPulse++;                                                                      // "motorPulse" increases by one and "motorState" is set to 0    1           deactivated      activated
             motorState = false;                                                                //                                                               2           activated        activated
         }                                                                                      //                                                               2           activated        deactivated
         if (rightStick.getRawButton(JOYSTICK_BUTTON_ONE) == false)                             // if the button is let go,                                      2           deactivated      activated
         {                                                                                      //                                                               3...        activated        activated
             motorState = true;                                                                 // "motorState" is set back to true to allow for the motor to change again.
         }
         if ((motorPulse % 2) == 0)                                                             // if "motorState" is even,
         {
             shooterMotorLeft.set(1.0);                                                         // both motors run
             shooterMotorRight.set(1.0);                                                   
         }                                                                                      // ^ We need to use a double for the motor set value
         else                                                                                   // if "motorState" is odd,
         {
             shooterMotorLeft.set(0.0);                                                         // both motors stop running
             shooterMotorRight.set(0.0);        
         }                                                                                      // *end shooter code* *start piston code*
     }
 
     public void pushPiston() {                                                                 // *start shooter code*   
         if (rightStick.getRawButton(JOYSTICK_BUTTON_TWO) == true || pushPistonState == true)   // if the button is pressed    -or-   "pushPistonState" = true :                                   
         {                                                                                                                  
             shooterControlPiston.set(true);                                                    // piston is pushed out
             countTimer++;                                                                      // "countTimer" increases by 1
             pushPistonState = true;                                                            // "pushPistonState" = true  , so it can continue to loop even if the button is not pushed
             if (countTimer == THREE_SECONDS) {                                                 // if "countTimer" = 300:                 
                 shooterControlPiston.set(false);                                               //    reset: piston is pulled back in
                 pushPistonState = false;                                                       //    reset: "pushPistonState" = false
                 countTimer = 1;                                                                //    reset: "countTimer" restarts back to 1
             }
         }
     }
 
     public void pushMotor() {
         if (rightStick.getRawButton(JOYSTICK_BUTTON_TWO) == true || pushMotorState == true)    // if the button is pressed    -or-   "pushPistonState" = true :                                
         {                                                                                              
             shooterControlMotor.set(1.0);                                                      // motor is turned on
             countTimer++;                                                                      // "countTimer" increases by 1
             pushMotorState = true;                                                             // "pushPistonState" = true  , so it can continue to loop even if the button is not pushed
             if (countTimer == THREE_SECONDS) {                                                 // if "countTimer" = 300:                
                 shooterControlMotor.set(0.0);                                                  //      reset: motor is turned off
                 pushMotorState = false;                                                        //      reset: "pushPistonState" = false
                 countTimer = 1;                                                                //      reset: "countTimer" restarts back to 1
             }
         }
     }
 }
