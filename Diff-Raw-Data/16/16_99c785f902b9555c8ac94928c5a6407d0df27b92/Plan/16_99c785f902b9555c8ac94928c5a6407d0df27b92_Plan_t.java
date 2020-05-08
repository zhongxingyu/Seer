 /*
  * @copyright Copyright 2012 Andrew Brown. All rights reserved.
  * @license GNU/GPL.
  */
 package aima.core.search.nondeterministic;
 
 import java.util.LinkedList;
 
 /**
  * Represents a solution plan for an AND-OR search; according to page 135, the
  * plan must be "a subtree that (1) has a goal node at every leaf, (2) specifies
  * one action at each of its OR nodes, and (3) includes every outcome branch at 
  * each of its AND nodes." As demonstrated on page 136, this subtree is
  * implemented as a linked list where every OR node is an action object--
  * satisfying (2)--and every AND node is an if-then-else chain--satisfying (3).
  * @author Andrew Brown
  */
 public class Plan {
 
    LinkedList<Object> steps = new LinkedList<Object>();
 
     /**
      * Empty constructor
      */
     public Plan() {
         // do nothing
     }
 
     /**
      * Constructor
      * @param step 
      */
     public Plan(Object step) {
         this.steps.offerLast(step);
     }
 
     /**
      * Returns whether the plan contains the given step
      * @param step
      * @return 
      */
     public boolean contains(Object step) {
         return this.steps.contains(step);
     }
 
     /**
      * Appends a step to the plan and returns itself
      * @param step 
      * @return
      */
     public Plan append(Object step) {
         this.steps.offerLast(step);
         return this;
     }
 
     /**
      * Prepends a step to the plan and returns itself
      * @param step
      * @return 
      */
     public Plan prepend(Object step) {
         this.steps.offerFirst(step);
         return this;
     }
 
     /**
      * Returns the string representation of this plan
      * @return 
      */
     public String toString() {
         StringBuilder s = new StringBuilder();
         s.append("[");
         int count = 0;
         int size = this.steps.size();
         for (Object step : this.steps) {
             s.append(step);
             if (count < size) {
                 s.append(", ");
             }
             count++;
         }
         s.append("]");
         return s.toString();
     }
 }
