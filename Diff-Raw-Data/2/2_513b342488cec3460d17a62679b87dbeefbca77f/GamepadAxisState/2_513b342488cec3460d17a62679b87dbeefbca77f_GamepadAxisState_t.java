 package com.edinarobotics.utils.gamepad;
 
 import com.edinarobotics.utils.math.Vector2;
 import com.sun.squawk.util.MathUtils;
 
 /**
  * This class represents the state of all joystick axes of a Gamepad.
  */
 public class GamepadAxisState {
     private Vector2 left, right;
     
     /**
      * Constructs a new GamepadAxisState storing the given joystick states.
      * @param left The state of the gamepad's left joystick as a Vector2.
      * @param right The state of the gamepad's right joystick as a Vector2.
      */
     public GamepadAxisState(Vector2 left, Vector2 right){
         this.left = left;
         this.right = right;
     }
     
     /**
      * Returns the Vector2 representing the state of the gamepad's left
      * joystick.
      * @return The Vector2 representing the state of the gamepad's left
      * joystick.
      */
     public Vector2 getLeftJoystick(){
         return left;
     }
     
     /**
      * Returns the Vector2 representing the state of the gamepad's right
      * joystick.
      * @return The Vector2 representing the state of the gamepad's right
      * joystick.
      */
     public Vector2 getRightJoystick(){
         return right;
     }
     
     /**
      * Returns the current magnitude of the Vector2 representing the state of 
      * the gamepad's left joystick.
      * @return the current magnitude of the Vector2 representing the state of 
      * the gamepad's left joystick.
      */
     public double getLeftMagnitude() {
         return getLeftJoystick().magnitude();
     }
     
     /**
      * Returns the current magnitude of the Vector2 representing the state of 
      * the gamepad's right joystick.
      * @return the current magnitude of the Vector2 representing the state of 
      * the gamepad's right joystick.
      */
     public double getRightMagnitude() {
         return getRightJoystick().magnitude();
     }
     
     /**
      * Returns the current direction of the Vector2 representing the state of
      * the gamepad's left joystick in degrees (-180 to 180). This return value
      * is suitable to be passed into RobotDrive.
      * @return the current direction of the Vector2 representing the state of
      * the gamepad's left joystick in degrees (-180 to 180).
      */
     public double getLeftDirection() {
         return Math.toDegrees(MathUtils.atan2(left.getY(), left.getX()));
     }
     
     /**
      * Returns the current direction of the Vector2 representing the state of
      * the gamepad's right joystick in degrees (-180 to 180). This return value
      * is suitable to be passed into RobotDrive.
      * @return the current direction of the Vector2 representing the state of
     * the gamepad's right joystick in degrees (-180 to 180).
      */
     public double getRightDirection() {
         return Math.toDegrees(MathUtils.atan2(right.getY(), right.getX()));
     }
 }
