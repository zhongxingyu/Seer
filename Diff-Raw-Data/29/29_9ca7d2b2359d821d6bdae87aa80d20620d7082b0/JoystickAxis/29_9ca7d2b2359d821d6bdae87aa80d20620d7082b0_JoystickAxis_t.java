 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Joystick;
 
 /**
  *
  * @author alex
  */
 public class JoystickAxis {
     private Joystick joy;
    private Joystick.AxisType axis;
 
    public JoystickAxis(Joystick joy, Joystick.AxisType axis) {
         this.joy = joy;
         this.axis = axis;
     }
     public double get() {
        return joy.getAxis(axis);
     }
 }
