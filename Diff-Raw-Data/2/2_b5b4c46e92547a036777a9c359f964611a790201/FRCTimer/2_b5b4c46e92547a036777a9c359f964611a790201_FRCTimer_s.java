 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  *Timer for the robot
  * @author First1
  */
 public class FRCTimer {
     
     public static Timer clock;
     public static boolean bHasStarted;
     public static void initTimer(){
         bHasStarted = false;
         clock= new Timer();
         
     }
     /**
      * DelayDone.
      * checks if a certain delay is done as defined by argument 
      * if the timer has not started the method will start the time, it will stop when time has elapsed 
      * @param length
      * @return true if time has passed false if otherwise
      */
     public static boolean DelayDone(double length){
         if (bHasStarted == true){
            if (getSeconds() >= length) { 
                stop();
                return true;
                
            }
            else {
               return false;
            }
         } else {
            start();
         }
         return false;
         
     }
 
     /**
      * Start the timer
      */
     public static void start(){
         clock.start();
         bHasStarted = true ;
     }
     
     
     /**
      * Stop the timer
      */
     public static void stop(){
         clock.stop();
         bHasStarted = false;
     }
     
     /**
      * Resets the timer
      */
     public static void reset(){
         clock.reset();
     }
     
     /**
      * Gets the current time on the timer in seconds
      * @return time in seconds
      */
     public static double getSeconds(){
        return clock.get() / 1000000;
     }
     
 }
