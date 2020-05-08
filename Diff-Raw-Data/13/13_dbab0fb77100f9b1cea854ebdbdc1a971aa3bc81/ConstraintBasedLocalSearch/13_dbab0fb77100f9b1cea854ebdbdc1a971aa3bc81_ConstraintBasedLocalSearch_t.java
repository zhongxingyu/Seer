 package gps.solver;
 
 import gps.Range;
 import gps.statemanager.LocalStateManager;
 import java.util.ArrayList;
 
 public abstract class ConstraintBasedLocalSearch {
     
     /* PROPERTIES */
     
     protected LocalStateManager   stateManager;                     // state manager      
     protected ArrayList<Integer>  state;    // variables
     protected Range               values    = new Range(0, 0);      // values range
     
     protected int step;
     // TODO remove values
     
     /* METHODS */
    public abstract ArrayList<Integer> solve(int maxIterations);                                        
     
     /**
      * Cosntructor
      */
     /*public ConstraintBasedLocalSearch(LocalStateManager stateManager, int varCount, Range values) {
     	this.stateManager = stateManager;
         this.values       = values;
         for(int i = 0; i < varCount; i++) {
             variables.add(0);
         }
     }*/
     
     /**
      * 
      * @param stateManager
      */
     public ConstraintBasedLocalSearch(LocalStateManager stateManager) {
     	this.stateManager = stateManager;
     	reset();
     }
     
     /**
      * Reset function, need to be called before each running solve
      */
     public void reset() {
     	step = 0;
     }
     
     public int getStep() {
     	return step;
     }
     
     /**
      * solve problem
      */
     public /*abstract*/ ArrayList<Integer> solve() {// TODO abstract
     	return new ArrayList<Integer>();
     }
     
     public double evaluateState(ArrayList<Integer> state) {
     	return stateManager.evaluateState(state);
     }
     
 }
