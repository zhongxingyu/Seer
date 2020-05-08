 package galapagos;
 
 /**
  * The interface of the finch behaviors.
  */
 public interface Behavior extends Cloneable {
     
     /**
      * This finch's choice of action in the meeting with the argument finch.
      */
     public Action decide (Finch finch);
     
     /**
     * This finch gets information about which action a finch just met chose.
      */
     public void response (Finch finch, Action action);
     
     /**
     * A new instanse of the same behavior.
      */
     public Behavior clone();
       
     /**
      * The toString()-method must only depend on the runtime-class.
      */
     public String toString();
     
     /**
      * Indicates whether another Object is equal to this one.
      * Should return true if the Object is of the same Behavior-type as this one.
      * @param obj object to compare to.
      * @return true if obj is of the same Behavior-type as this one.
      */
     public boolean equals(Object obj);
     public int hashCode();
 }
