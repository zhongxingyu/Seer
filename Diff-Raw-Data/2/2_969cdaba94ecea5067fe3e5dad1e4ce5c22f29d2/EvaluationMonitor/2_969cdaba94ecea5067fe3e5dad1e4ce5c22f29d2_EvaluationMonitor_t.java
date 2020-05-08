 package de.unisb.cs.depend.ccs_sem.evaluators;
 
 
 /**
  * A monitor for the evaluation of expressions.
  * It is informed every time when new states or transitions are evaluated.
  *
  * @author Clemens Hammacher
  */
 public interface EvaluationMonitor {
 
     /**
      * Called when a new state is found.
      */
     void newState();
 
     /**
      * Called when new transitions are found.
      */
     void newTransitions(int count);
 
     /**
      * Called when the evaluation of the LTS is ready.
      */
     void ready();
 
     /**
      * Called when an error occured during evaluation.
      */
     void error(String errorString);
 
     /**
     * has the same effect as
      * <code>
      *   newState();
      *   newTransitions(numTransitions);
      * </code>
      */
     void newState(int numTransitions);
 
 }
