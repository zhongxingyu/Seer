 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.team811.subsystems;
 
 /**
  *
  * @author david
  */
 public class Climber extends SubSystem {
 
     public void init() {
     }
 
     public void run() {
         double x1 = d.xbox1.getRawAxis(CLIMB_BIG_ANGLE_AXIS);
         double y1 = d.xbox1.getRawAxis(CLIMB_BIG_CLIMBER_AXIS);
 
         double x2 = d.xbox1.getRawAxis(CLIMB_SMALL_ANGLE_AXIS);
         double y2 = d.xbox1.getRawAxis(CLIMB_SMALL_CLIMBER_AXIS);
         if (Math.abs(x1) < .26) {
             x1 = 0;
         }
        if (Math.abs(y1) < CLIMB_ERROR) {
             y1 = 0;
         }
        if (Math.abs(x2) < CLIMB_ERROR) {
             x2 = 0;
         }
        if (Math.abs(y2) < CLIMB_ERROR) {
             y2 = 0;
         }
 
         d.bigclimber.set(y1);
         d.smallclimber.set(y2);
 
         d.bigangle.set(x1);
         d.smallangle.set(x2);
     }
 
     public void disable() {
     }
 }
