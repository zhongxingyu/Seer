 package com.edinarobotics.utils.common;
 
 /**
  * This interface can be inherited by classes which are updatable.
  * 
  * Classes implementing this interface are likely indicating that they have
  * some internal state that must be tracked continuously. Users should attempt
 * to call the classes' {@link #update()} methods during each control loop.
  */
 public interface Updatable {
     
     /**
      * This method causes the implementing class to update its values and to
      * track the current state of the robot and the field.
      * 
      * Its meaning will vary depending on the type of the class implementing it.
      * For subsystems, this method will most likely write control values to
      * robot components to ensure that the watchdog timer does not expire. For
      * sensors, it can cause them to continue to track their values and maintain
      * their internal state.
      */
     public void update();
 }
