 package org.usfirst.frc2022.Joysticks;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 
 /**
  * Class designed to interface with an Attack3 joystick. This class was designed
  * to allow better definition of the joysticks in the source code of a program
  * since now the class would be named with the joystick type.
  *
  * This class uses the Joystick class to interface with the Drivers Station to
  * get the input from the joystick.
  *
  * Note: Port of Attack3.ccp
  */
 public class Attack3 extends Joystick {
     public static int Button6 = 6;
     public static int Button7 = 7;
     /**
      * Construct an instance of a Logitech Attack3 joystick (The kind in the
      * KoP).
      *Intialize Button6 and Button 7 for controling the pickup relays
      * @param port The port on the driver station that the joystick is plugged
      * into.
      * @return
      */
     
    
     
     
     
     
     public Attack3(int port) {
 
         super(port);
 
     } //End public Attack3(int port)
 
     /**
      * Return the value of the Twist axis for this joystick This value is always
      * 0 since there is no twist axis.
      *
      * @param
      * @return 0 since there is no twist axis
      */
     float GetTwist() {
 
         return (0);	//Return 0 since there is no twist axis
 
     }//End float GetTwist()
 
     /**
      * Return the value of the Z axis for this joystick This value is always 0
      * since there is no Z axis.
      *
      * @param
      * @return 0 since there is no Z axis
      */
     float GetZ() {
 
         return (0);	//Return 0 since there is no Z axis
 
     }//End float GetZ()
 
     /**
      * Get the X value of the joystick. The right side of the axis is positive.
      *
      * @param
      * @return The current X value of the joystick between -1 and 1
      */
     float GetX() {
 
         return (float) (this.getX());
 
     }//End float GetX()
 
     /**
      * Get the Y value of the joystick. The forward part of the axis is
      * negative.
      *
      * @param
      * @return The current Y value of the joystick between -1 and 1
      */
     float GetY() {
 
         return (float) (this.getY());
 
     }//End float GetY()
 
     /**
      * Get the Throttle value of the joystick. The top part of the axis is
      * negative.
      *
      * @param
      * @return The current Throttle value of the joystick between -1 and 1
      */
     float GetThrottle() {
 
         return (float) (this.getThrottle());
 
     } //End float GetThrottle()
 
     /**
      * Get a JoystickButton for the Command Subsystem OI Class
      *
      * @param button The button as an integer
      * @return JoystickButton
      */
     
        public static boolean GetButton6Value() {
         return (Attack3.GetButton6Value());
     }
 
     /**
     * Get the current state of the Back button.
      *
      * @param
      * @return The current state of the button
      */
     public static boolean GetButton7Value() {
         return (Attack3.GetButton7Value());
     }
    
     
     
     JoystickButton GetButton(int button) {
 
         return (new JoystickButton(this, button));
 
     } //End float GetButton(int Button)
   
 }
