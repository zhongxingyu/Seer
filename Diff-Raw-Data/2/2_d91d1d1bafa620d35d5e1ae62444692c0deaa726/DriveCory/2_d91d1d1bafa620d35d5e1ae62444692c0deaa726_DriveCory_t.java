 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package modules.Cory;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.Talon;
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  *
  * @author spinerc
  */
 public class DriveCory extends GyroCory {
     
     //Declaring Motor controlers
     Talon fRight = new Talon(1);
     Talon fLeft = new Talon(2);
     Talon bRight = new Talon(3);
     Talon bLeft = new Talon(4);
     
     //Declaring Joysticks
     Joystick right = new Joystick(1);
     Joystick left = new Joystick(2);
     
    int delay = 1;
    
     
     protected void iteration() {
      
         //getting joystick values
         double rightvalue = right.getY();
         double leftvalue = left.getY();
             
         //setting speed
         fRight.set(-1 * rightvalue);
         fLeft.set(leftvalue);
         bRight.set(-1 * rightvalue);
         bLeft.set(leftvalue);
         
         boolean tRight = right.getRawButton(2);
                     boolean tLeft = right.getRawButton(3);
 
                     bLeft.set(leftvalue);
                     fLeft.set(leftvalue);
                     bRight.set(-1 * rightvalue);
                     fRight.set(-1 * rightvalue);
                     
                     
                     
                     if(tLeft) {
                         double less = angle + 315;
                         if (less >= 360){
                             less -= 360;
                         }
                         
                         while (angle < less){
                         leftvalue = -1;
                         rightvalue = 1;   
                         
                         bLeft.set(leftvalue);
                         fLeft.set(leftvalue);
                         bRight.set(-1 * rightvalue);
                         fRight.set(-1 * rightvalue);   
                         }
                         
                         Timer.delay(delay);
                         if (tLeft) {
                             leftvalue = -0.5;
                             rightvalue = 0.5;
                         
                             bLeft.set(leftvalue);
                             fLeft.set(leftvalue);
                             bRight.set(-1 * rightvalue);
                             fRight.set(-1 * rightvalue);
                         }
                     }
 
                     if(tRight) {
                         double less1 = angle + 45;
                         if (less1 >= 360){
                             less1 -= 360;
                         }
                         
                         while (angle < less1){
                         leftvalue = 1;
                         rightvalue = -1;
                         
                         bLeft.set(leftvalue);
                         fLeft.set(leftvalue);
                         bRight.set(-1 * rightvalue);
                         fRight.set(-1 * rightvalue);
                         }
                         
                         Timer.delay(delay);
                         if (tRight) {
                             leftvalue = 0.5;
                             rightvalue = -0.5;
 
                             bLeft.set(leftvalue);
                             fLeft.set(leftvalue);
                             bRight.set(-1 * rightvalue);
                             fRight.set(-1 * rightvalue);
                         }
                     }
    }
     protected void reset() {
          System.out.println("Shutting down...");
    }
 }
