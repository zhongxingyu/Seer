 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package core;
 import util.MyTalon;
 import util.Config;
 import util.Output;
 
 /**
  * @author trevor.jones
  */
 public class Drive 
/**
 * Creates objects for each talon.
 */
 {
     MyTalon leftTalonOne = new MyTalon(Config.chnDriveLeft1);
     //MyTalon leftTalonTwo = new MyTalon();
     //MyTalon leftTalonThree = new MyTalon();
     
     MyTalon rightTalonOne = new MyTalon(Config.chnDriveRight1);
     //MyTalon rightTalonTwo = new MyTalon();
     //MyTalon rightTalonThree = new MyTalon();
      
    /**
     * The function makes two speeds for the motors and calls setLeft and setRight
     * @param x     The place on the x axis of the joystick the stick is on.
     * @param y     The place on the y axis of the joystick the stick is on.
     */
     public void set(double x, double y) 
     {
         double left = y+x;
         double right = y-x;
         
         setLeft(left);
         setRight(right);
     }
    /**
     * This function sets double left (the left motor speed) to the left talons.
     * @param left  The set speed for the left motor.
     */
     private void setLeft(double left) 
     {
         leftTalonOne.set(left);
         //leftTalonTwo.set(left);
         //leftTalonThree.set(left);
     }
    /**
     * This function sets double right (the right motor speed) to the right talons.
     * @param right The set speed for the right motor.
     */
     private void setRight(double right) 
     {
         rightTalonOne.set(right);
         //rightTalonTwo.set(right);
         //rightTalonThree.set(right);
     }
 }
