 package io.metacake.core.input;
 
 import java.util.List;
 
 /**
  * What the Process Layer sees from the Input Layer.
  *
  * This interface pool is for actions received from the input system
  * and can given them to the process system at request
  * It can also accept triggers to control action creation
  * @author spencerflorence
  */
 public interface InputSystem {
     /**
      * Get all actions and Empty the pool
      * Effect: Empties the pool
      * @return The current actions
      */
    List<Action> getAndClearActions();
 
     /**
      * Empty the current pool
      */
     void clearActions();
 
     /**
      * Pass these action triggers to the event handling layer to control action creation
      * @param ts All actions triggers to be used
      */
     public void setActionTriggers(List<ActionTrigger> ts);
 }
