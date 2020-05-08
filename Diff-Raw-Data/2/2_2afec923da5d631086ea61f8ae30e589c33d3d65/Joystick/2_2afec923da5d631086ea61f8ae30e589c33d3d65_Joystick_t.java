 package com.edinarobotics.utils.gamepad;
 
 import com.edinarobotics.utils.gamepad.buttons.HatSwitchButton;
 import com.edinarobotics.utils.math.Vector2;
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 
 /**
  * This class implements a simple interface for interacting with a 3-axis
  * joystick. This interface was designed with the Logitech Extreme 3D Pro
  * in mind.
  * 
  * The buttons have been given names to avoid depending on their printed labels.
  */
 public class Joystick {
     protected final edu.wpi.first.wpilibj.Joystick joystick;
     protected final int port;
     private Button trigger;
     private Button hatButtonLeftTop, hatButtonLeftBottom, 
             hatButtonRightTop, hatButtonRightBottom;
     private Button shoulderButton;
     private Button hatSwitchUp, hatSwitchDown, hatSwitchLeft, hatSwitchRight;
     private Button outerRingTop, outerRingMiddle, outerRingBottom, innerRingTop,
             innerRingMiddle, innerRingBottom;
     private static final double HAT_SWITCH_THRESHOLD = 0.9;
     
     /**
      * Constructs a new Joystick given its port number.
      * @param port The port number of the joystick to be read.
      */
     public Joystick(int port){
         this.port = port;
         this.joystick = new edu.wpi.first.wpilibj.Joystick(port);
         //Set up trigger
         trigger = new JoystickButton(joystick, 1);
         //Set up shoulder button
         shoulderButton = new JoystickButton(joystick, 2);
         //Set up top buttons
         hatButtonLeftTop = new JoystickButton(joystick, 5);
         hatButtonLeftBottom = new JoystickButton(joystick, 3);
         hatButtonRightTop = new JoystickButton(joystick, 6);
         hatButtonRightBottom = new JoystickButton(joystick, 4);
         //Set up hat switch buttons
         hatSwitchUp = new HatSwitchButton(this, HatSwitchButton.HatSwitchButtonType.UP);
         hatSwitchDown = new HatSwitchButton(this, HatSwitchButton.HatSwitchButtonType.DOWN);
         hatSwitchLeft = new HatSwitchButton(this, HatSwitchButton.HatSwitchButtonType.LEFT);
         hatSwitchRight = new HatSwitchButton(this, HatSwitchButton.HatSwitchButtonType.RIGHT);
         //Set up ring buttons
         outerRingTop = new JoystickButton(joystick, 7);
         outerRingMiddle = new JoystickButton(joystick, 9);
         outerRingBottom = new JoystickButton(joystick, 11);
         innerRingTop = new JoystickButton(joystick, 8);
         innerRingMiddle = new JoystickButton(joystick, 10);
         innerRingBottom = new JoystickButton(joystick, 12);
     }
     
     /**
      * Returns the current value of the x-axis of the joystick. <br/>
      * A value of {@code -1} indicates that the joystick is fully left.<br/>
      * A value of {@code 1} indicates that the joystick is fully right.
      * @return The current value of the x-axis of the joystick.
      */
     public double getX(){
         return joystick.getRawAxis(1);
     }
     
     /**
      * Returns the current value of the y-axis of the joystick. <br/>
      * A value of {@code -1} indicates that the joystick is fully down.<br/>
      * A value of {@code 1} indicates that the joystick is fully up.
      * @return The current value of the y-axis of the joystick.
      */
     public double getY(){
         return -1.0*joystick.getRawAxis(2);
     }
     
     /**
      * Returns the current value of the twist axis of the joystick. <br/>
      * A value of {@code -1} indicates that the joystick is twisted
      * fully left.<br/>
      * A value of {@code 1} indicates that the joystick is twisted fully right.
      * <br/>
      * A value of {@code 0} indicates that the twist axis is centered.
      * @return The current value of the twist axis of the joystick.
      */
     public double getTwist(){
         return joystick.getRawAxis(3);
     }
     
     /**
      * Returns a new Vector2 representing the state of the joystick.
      * 
      * The Vector2 contains the state of the x- and y- axes of the joystick.
      * @return A Vector2 representing the state of the joystick.
      */
     public Vector2 getJoystick(){
         return new Vector2(getX(), getY());
     }
     
     /**
      * Returns the state of the joystick axes together in a JoystickAxisState.
      * @return A JoystickAxisState object containing the states of all the
      * axes on this Joystick.
      */
     public JoystickAxisState getJoystickAxisState(){
         return new JoystickAxisState(getJoystick(), getTwist(), getThrottle());
     }
     
     /**
      * Returns the current value of the throttle axis of the joystick. <br/>
      * A value of {@code 0} indicates that the throttle lever is fully down.
      * A value of {@code 1} indicates that the throttle lever is fully up.<br/>
      * @return The current value of the throttle axis of the joystick.
      */
     public double getThrottle(){
         return (-1.0*joystick.getRawAxis(4)+1.0)*0.5;
     }
     
     /**
      * Returns the current position of the x-axis of the hat switch that 
      * is on the head of the joystick (equivalent to a gamepad's D-Pad). <br/>
      * A value of {@code -1} indicates that the hat switch is being held
      * left.<br/>
      * A value of {@code 1} indicates that the hat switch is being held
      * right.<br/>
      * A value of {@code 0} indicates that the hat switch's x-axis is centered.
      * @return The value of the hat switch's x-axis as described above.
      */
     public byte getHatSwitchX(){
         return hatSwitchToByte(joystick.getRawAxis(5));
     }
     
     /**
      * Returns the current position of the y-axis of the hat switch that 
      * is on the head of the joystick (equivalent to a gamepad's D-Pad). <br/>
      * A value of {@code -1} indicates that the hat switch is being held
      * down.<br/>
      * A value of {@code 1} indicates that the hat switch is being held
      * up.<br/>
      * A value of {@code 0} indicates that the hat switch's y-axis is centered.
      * @return The value of the hat switch's y-axis as described above.
      */
     public byte getHatSwitchY(){
         return hatSwitchToByte(-joystick.getRawAxis(6));
     }
     
     /**
      * Returns a Button object representing the trigger of the joystick. <br/>
      * The trigger is the large button on the front of the joystick. It is
      * shaped like a trigger.
      * @return A Button object for the trigger.
      */
     public Button trigger(){
         return trigger;
     }
     
     /**
      * Returns a Button object representing the shoulder button of the
      * joystick. <br/>
      * The shoulder button is on the side of the joystick stem and is generally
      * pushed by the driver's thumb.
      * @return A Button object for the joystick shoulder button.
      */
     public Button shoulderButton(){
         return shoulderButton;
     }
     
     /**
      * Returns a Button object representing the left upper hat button of the
      * joystick. <br/>
      * The hat buttons are on the head of the joystick surrounding the hat
      * switch.
      * @return A Button object for the left upper hat button.
      */
    public Button hatButtonLeftTop(){
         return hatButtonLeftTop;
     }
     
     /**
      * Returns a Button object representing the left bottom hat button of the
      * joystick. <br/>
      * The hat buttons are on the head of the joystick surrounding the hat
      * switch.
      * @return A Button object for the left bottom hat button.
      */
     public Button hatButtonLeftBottom(){
         return hatButtonLeftBottom;
     }
     
     /**
      * Returns a Button object representing the right upper hat button of the
      * joystick. <br/>
      * The hat buttons are on the head of the joystick surrounding the hat
      * switch.
      * @return A Button object for the right upper hat button.
      */
     public Button hatButtonRightTop(){
         return hatButtonRightTop;
     }
     
     /**
      * Returns a Button object representing the right bottom hat button of the
      * joystick. <br/>
      * The hat buttons are on the head of the joystick surrounding the hat
      * switch.
      * @return A Button object for the right bottom hat button.
      */
     public Button hatButtonRightBottom(){
         return hatButtonRightBottom;
     }
     
     /**
      * Returns a Button object representing the top button in the outer ring
      * of buttons on the joystick.<br/>
      * The inner and outer ring buttons are on the side of the joystick.
      * @return A Button object for top outer ring button.
      */
     public Button outerRingTop(){
         return outerRingTop;
     }
     
     /**
      * Returns a Button object representing the middle button in the outer ring
      * of buttons on the joystick.<br/>
      * The inner and outer ring buttons are on the side of the joystick.
      * @return A Button object for middle outer ring button.
      */
     public Button outerRingMiddle(){
         return outerRingMiddle;
     }
     
     /**
      * Returns a Button object representing the bottom button in the outer ring
      * of buttons on the joystick.<br/>
      * The inner and outer ring buttons are on the side of the joystick.
      * @return A Button object for bottom outer ring button.
      */
     public Button outerRingBottom(){
         return outerRingBottom;
     }
     
     /**
      * Returns a Button object representing the top button in the inner ring
      * of buttons on the joystick.<br/>
      * The inner and outer ring buttons are on the side of the joystick.
      * @return A Button object for top inner ring button.
      */
     public Button innerRingTop(){
         return innerRingTop;
     }
     
     /**
      * Returns a Button object representing the middle button in the inner ring
      * of buttons on the joystick.<br/>
      * The inner and outer ring buttons are on the side of the joystick.
      * @return A Button object for middle inner ring button.
      */
     public Button innerRingMiddle(){
         return innerRingMiddle;
     }
     
     /**
      * Returns a Button object representing the bottom button in the inner ring
      * of buttons on the joystick.<br/>
      * The inner and outer ring buttons are on the side of the joystick.
      * @return A Button object for bottom inner ring button.
      */
     public Button innerRingBottom(){
         return innerRingBottom;
     }
     
     /**
      * Returns a Button object representing the hat switch pressed up. <br/>
      * The hat switch is the mini-joystick on the head of the joystick. It
      * is similar to a gamepad's d-pad.
      * @return A Button object representing the hat switch being pressed up.
      */
     public Button hatSwitchUp(){
         return hatSwitchUp;
     }
     
     /**
      * Returns a Button object representing the hat switch pressed down. <br/>
      * The hat switch is the mini-joystick on the head of the joystick. It
      * is similar to a gamepad's d-pad.
      * @return A Button object representing the hat switch being pressed down.
      */
     public Button hatSwitchDown(){
         return hatSwitchDown;
     }
     
     /**
      * Returns a Button object representing the hat switch pressed left. <br/>
      * The hat switch is the mini-joystick on the head of the joystick. It
      * is similar to a gamepad's d-pad.
      * @return A Button object representing the hat switch being pressed left.
      */
     public Button hatSwitchLeft(){
         return hatSwitchLeft;
     }
     
     /**
      * Returns a Button object representing the hat switch pressed right. <br/>
      * The hat switch is the mini-joystick on the head of the joystick. It
      * is similar to a gamepad's d-pad.
      * @return A Button object representing the hat switch being pressed right.
      */
     public Button hatSwitchRight(){
         return hatSwitchRight;
     }
     
     /**
      * This method is used internally to convert the hat switch axis value
      * of type {@code double} to a {@code byte}. <br/>
      * @param value The axis value of the hat switch.
      * @return A {@code byte} representing the state of the hat switch based
      * on the given value.
      */
     protected byte hatSwitchToByte(double value){
         if(value >= HAT_SWITCH_THRESHOLD){
             return 1;
         }
         else if(value <= -HAT_SWITCH_THRESHOLD){
             return -1;
         }
         return 0;
     }
     
     /**
      * Returns a human-readable String form of this Joystick object.
      * @return A human-readable String representing this Joystick.
      */
     public String toString(){
         return "Joystick "+this.port;
     }
 }
