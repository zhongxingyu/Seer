 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package core;
 import util.MyJoystick ;
 import util.Output;
 
 /**
  * Creates and runs joystick, ejector, and drive classes. 
 *
  */
 public class Bot 
 {
     
     MyJoystick joy;
     TimerEjector eject;
     Drive drive;
     
     /**
      * Creates a joystick, ejector, and drive.
      * @param joystick 
      */
     public Bot(MyJoystick joystick)
     {
         joy = joystick;   
         eject = new TimerEjector(joy);
         drive = new Drive();
     }
     
     /**
      * Runs the ejector and the drive.
      */
     public void botRun()
     {
         eject.run();
         drive.set(joy.getX(), joy.getY());
     }
 }
