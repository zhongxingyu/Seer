 package core;
 
 import actuator.GRTSolenoid;
 import event.listeners.HouseMechListener;
 import java.util.Vector;
 import logger.GRTLogger;
 
 /**
  * Generic haunted house mechanism.
  *
  * @author agd
  */
 public abstract class HouseMech extends GRTLoggedProcess {
 
     /**
      * Array of solenoids for this mechanism. If this HouseMech was constructed
      * with a single solenoid and not an array, that solenoid will be the first
      * element in this solenoid array.
      *
      * Activation and deactivation mechanics should be carried out in the
      * extend() and retract() methods.
      */
     protected GRTSolenoid[] solenoids;
     private boolean extended = false;	//We start in the inactive (not extended) state.
     private Vector listeners;
 
     /**
      * Instantiates a house mechanism.
      *
      * @param name name of mechanism
      * @param sols solenoids to control
      */
     public HouseMech(String name, GRTSolenoid[] sols) {
         super(name);
         this.solenoids = sols;
     }
 
     public void addStateChangeListener(HouseMechListener l) {
         listeners.addElement(l);
     }
 
    public void removeButtonListener(HouseMechListener l) {
         listeners.removeElement(l);
     }
 
     /**
      * Instantiates a house mechanism.
      *
      * @param name name of mechanism
      * @param sol single solenoid to control
      */
     public HouseMech(String name, GRTSolenoid sol) {
         this(name, new GRTSolenoid[1]);
         solenoids[0] = sol;
     }
 
     /**
      * This method should activate the solenoid as necessary to activate the
      * mechanism.
      */
     protected abstract void extend();
 
     /**
      * This method should activate the solenoid as necessary to deactivate the
      * mechanism.
      */
     protected abstract void retract();
 
     /**
      * Extend the mechanism into its active state. Witch pops out of window, or
      * door will swing open, etc.
      */
     public final void activate() {
         GRTLogger.getLogger().logInfo("Activating solenoid " + solenoids[0]);
         this.extended = true;
         extend();
     }
 
     /**
      * Retract the mechanism into its inactive state. Witch will go back into
      * window, door will swing closed, etc.
      */
     public final void deactivate() {
         this.extended = false;
         retract();
     }
 
     /**
      * Toggles the mechanism. If it is extended, retract; and vice versa.
      */
     public void toggle() {
        if (extended) {
             deactivate();
        } else {
             activate();
        }
     }
 
     /**
      * Get the current state of the mechanism; either extended or retracted.
      *
      * @return The current state.
      */
     public boolean getCurrentState() {
         return this.extended;
     }
 }
