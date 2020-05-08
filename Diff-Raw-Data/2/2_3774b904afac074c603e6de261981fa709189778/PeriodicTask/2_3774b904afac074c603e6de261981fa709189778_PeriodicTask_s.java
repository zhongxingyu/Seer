 package org.team751.tasks;
 
 import com.sun.squawk.util.MathUtils;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * A base class for anything that should run periodically, independent of
  * the command-based system timing. This uses a java.util.Timer to schedule
  * tasks.
  * @author Sam Crow
  */
 public abstract class PeriodicTask {
     
     private Timer periodicTaskTimer = new Timer();
     
     /**
      * The time, in seconds, that should pass between times when the task
      * is run. Subclasses can override this with {@link #setTaskTime(double) }.
      */
     private double periodicTaskTime = 1;
     
     /**
      * Set the time, in seconds, to wait in between calls to
      * {@link #run() }. This should be called before {@link #start()}. Otherwise,
      * it will have no effect.
      * @param newTime The time in seconds in between processing loops
      */
     protected void setTaskTime(double newTime) {
         periodicTaskTime = newTime;
     }
     
     /**
      * Start processing.
      * {@link #run() } will be called immediately, and thereafter at an interval
      * set by {@link #setTaskTime(double) }.
      */
     public void start() {
         //start the timer
        periodicTaskTimer.scheduleAtFixedRate(periodicTask, 0, MathUtils.round(periodicTaskTime * 1000));
     }
     
     /**
      * The run method.
      * The timer will call this method.
      * Subclasses should place code in this method to be run.
      */
     protected abstract void run();
     
     
     /**
      * The task that calls {@link #run()}
      */
     private TimerTask periodicTask = new TimerTask() {
 
         public void run() {
             /*
              * Advanced Java: This is a method inside an inner class.
              * Because this class is not static, it has access to the fields
              * and methods of the instances of the outer PeriodicTask as well as
              * the inner TimerTask. We can use the <class name>.this.<method clall>
              * syntax to specify that we want to access the outer instance.
              */
             //call the run() method of the outer PeriodicTask instance
             PeriodicTask.this.run();
         }
         
     };
 }
