 package com.edinarobotics.utils.gamepad;
 
 import com.edinarobotics.utils.gamepad.gamepadfilters.GamepadFilterSet;
 import com.edinarobotics.utils.math.Vector2;
 
 /**
  * Implements a Gamepad that filters all of its joystick axis values through
  * a given GamepadFilterSet.
  */
 public class FilteredGamepad extends Gamepad{
     GamepadFilterSet filters;
     
     /**
      * Constructs a new FilteredGamepad that will send the axis results
      * of the gamepad on the given port through the given GamepadFilterSet.
      * @param port The port of the gamepad that is to be wrapped by this
      * FilteredGamepad.
      * @param filterSet The GamepadFilterSet through which all joystick
      * values are to be sent.
      */
     public FilteredGamepad(int port, GamepadFilterSet filterSet){
         super(port);
     }
     
     /**
      * Returns the state of the left joystick as a Vector2.
      * This vector 2 contains the state of the x- and y- axis of the joystick.
      * @return A Vector2 representing the state of the left joystick after
      * being filtered by the given GamepadFilterSet.
      */
     public Vector2 getLeftJoystick(){
         return filters.filter(super.getGamepadAxisState()).getLeftJoystick();
     }
     
     /**
      * Returns the state of the right joystick as a Vector2.
      * This vector 2 contains the state of the x- and y- axis of the joystick.
      * @return A Vector2 representing the state of the right joystick after
      * being filtered by the given GamepadFilterSet.
      */
     public Vector2 getRightJoystick(){
         return filters.filter(super.getGamepadAxisState()).getRightJoystick();
     }
     
     /**
      * Returns the state of the gamepad's joysticks together in a
      * GamepadAxisState. The values in this object have been filtered
      * by the given GamepadFilterSet.
      * @return A GamepadAxisState object containing the states of all the
      * joystick axes on this Gamepad.
      */
     public GamepadAxisState getAxisState(){
         return filters.filter(super.getGamepadAxisState());
     }
     
     /**
      * Returns the current value of the x-axis of the left joystick. <br/>
      * A value of {@code -1} indicates that the joystick is fully left.<br/>
      * A value of {@code 1} indicates that the joystick is fully right.
      * @return The current value of the x-axis of the left joystick after
      * being sent through the given GamepadFilterSet.
      */
     public double getLeftX(){
         return getLeftJoystick().getX();
     }
     
     /**
      * Returns the current value of the y-axis of the left joystick. <br/>
      * A value of {@code -1} indicates that the joystick is fully down.<br/>
      * A value of {@code 1} indicates that the joystick is fully up.
      * @return The current value of the y-axis of the left joystick after
      * being sent through the given GamepadFilterSet.
      */
     public double getLeftY(){
         return getLeftJoystick().getY();
     }
     
     /**
      * Returns the current value of the x-axis of the right joystick. <br/>
      * A value of {@code -1} indicates that the joystick is fully left.<br/>
      * A value of {@code 1} indicates that the joystick is fully right.
      * @return The current value of the x-axis of the right joystick after
      * being sent through the given GamepadFilterSet.
      */
     public double getRightX(){
         return getRightJoystick().getX();
     }
     
     /**
      * Returns the current value of the y-axis of the right joystick. <br/>
      * A value of {@code -1} indicates that the joystick is fully down.<br/>
      * A value of {@code 1} indicates that the joystick is fully up.
      * @return The current value of the y-axis of the right joystick after
      * being sent through the given GamepadFilterSet.
      */
     public double getRightY(){
         return getRightJoystick().getY();
     }
 }
