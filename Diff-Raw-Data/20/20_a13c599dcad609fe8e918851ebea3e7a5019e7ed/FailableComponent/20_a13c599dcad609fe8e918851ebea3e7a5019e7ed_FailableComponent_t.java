 package eel.seprphase2.Simulator;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
import eel.seprphase2.GameOverException;
 import eel.seprphase2.Utilities.Percentage;
 
 /**
  * Base Class for all components which can fail.
  * 
  * FailableComponents maintain their state of repair and their wear.
  * 
  * Wear is accumulated by the template method stepWear; particular
  * FailableComponents should implement an appropriate calculateWearDelta
  * function to provide the wear characteristic of that component.
  * 
  * Components which can fail but which do not suffer from wear
  * (such as the reactor) can provide a calculateWearDelta function which
  * always returns a Percentage of zero.
  * 
  * @author Marius Dumetrescu
  */
 public abstract class FailableComponent {
 
     @JsonProperty
     private boolean hasFailed;      //The state of the component
     @JsonProperty
     private Percentage wear;                //Current wear level - capped at 100%
 
     /**
      * Constructor for the FailableComponent. Sets default percentage to 0 and a normal FailureState
      */
     public FailableComponent() {
         //Initialize to a normal state
         hasFailed = false;
         wear = new Percentage(0);
     }
 
     public boolean hasFailed() {
         return hasFailed;
     }
 
     public void fail() {
         hasFailed = true;
         stepWear();
     }
     
    public void addWear(Percentage damage) throws GameOverException {        
         
         if ((wear.points() + damage.points()) < 100) {
             wear = wear.plus(damage);
         } else {
             wear = new Percentage(100);     //Cap at 100%
            if (this instanceof Reactor) {
                throw new GameOverException(); // Ends the game if reactor reaches max wear
            }
         }
     }
 
     public void repair() throws CannotRepairException {
        if (this instanceof Reactor) {
             throw new CannotRepairException("This component cannot be repaired");
         }
 
         hasFailed = false;
         Percentage repair = new Percentage(10);
         if ((wear.points() - repair.points()) > 0) {
             wear = wear.minus(repair);
         } else {
             wear = new Percentage(0);     //Cap at 0%
         }        
     }
 
     /**
      * CalculateWearDelta must be overridden by any child classes. It should be used to calculate a minute change in
      * wear level for the component to be added onto it. This is normally calculated within a method's step() routine.
      *
      * @return Percentage. The number of percentage points to add to the current wear level.
      */
     protected abstract Percentage calculateWearDelta();
 
     /**
      * Returns the component's current wear level as a percentage.
      *
      * @return The component's wear level.
      */
     public Percentage wear() {
         return wear;
     }
 
     /**
      * stepWear will increase the wear of the component from a delta value. The value will be capped at 100 and 0
      *
      * @param delta The number of percentage points to increase the FailableComponent's wear level by between 0 and 100
      */
     public void stepWear() {
         Percentage wearDelta = calculateWearDelta();
         if ((wear.points() + wearDelta.points()) < 100) {
             wear = wear.plus(wearDelta);
         } else {
             wear = new Percentage(100);     //Cap at 100%
         }
        
 
     }
 }
