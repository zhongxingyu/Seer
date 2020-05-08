 package statespace;
 
 import environment.Coordinates;
 import environment.Environment;
 import environment.ProbableTransitions;
 import environment.Util;
 import environment.Environment.action;
 import policy.Policy;
 import state.CompleteState;
 import state.ReducedState;
 import state.State;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 import action.PreyAction;
 
 /**
  * For this StateSpace we make the assumption that the predator stays in a fixed position and
  * only the prey moves.
  */
 public class ReducedStateSpace extends StateSpace implements Iterable<State>, Iterator<State> {
     private int iter_pos;
     private ReducedState[][] states;
 
     public ReducedStateSpace() {
         this.states = new ReducedState[Util.DIM][Util.DIM];
         this.initStates();
     }
 
     @Override
     protected void initStates() {
         for (int i = 0; i < Util.DIM; i++) {
             for (int j = 0; j < Util.DIM; j++) {
                 Coordinates preyC = new Coordinates(i, j);
                 this.states[i][j] = new ReducedState(preyC); // predCoordinates fixed to (0,0)
             }
         }
     }
 
     @Override
     public ReducedState getState(Coordinates preyC, Coordinates predC) {
     	// FIXME: this function may be removed!!!!!
         return this.states[preyC.getX()][preyC.getY()];
     }
 
     public ReducedState getState(Coordinates preyC) {
         return this.states[preyC.getX()][preyC.getY()];
     }
 
     @Override
     public Environment.action getTransitionAction(State s, State s_prime) {
         ReducedState rs = (ReducedState) s;
         ReducedState rs_prime = (ReducedState) s_prime;
         return rs.getTransitionAction(rs_prime);
     }
 
     @Override
     public void initializeStateValues(double d) {
         for (State s : this) { s.setStateValue(d); }
     }
 
     @Override
     public void printActions(Policy policy) {
         // TODO
     }
 
     @Override
     public State getNextState(State s, Environment.action a) {
         Coordinates predNew = s.getPredatorCoordinates();
         predNew = predNew.getShifted(a.getOpposite());
         return this.getState(s.getPreyCoordinates(), predNew);
     }
 
     @Override
     public ProbableTransitions getProbableTransitions(State s, Environment.action a) {
         ProbableTransitions ret = new ProbableTransitions();
 
         // if the action will be undertaken, then the predator
         // will have a new position, BUT since we are in a system
         // in which the relative position predator-prey is relevant
         // and the predator is considered to stay always in (0,0)
         // then we need to consider the prey as virtually moving, even if it's not.
         // This virtual move is in the opposite direction of the actual predator move
         Coordinates preyNewPos = s.getPreyCoordinates().getShifted(a.getOpposite());
 
         // afterwards, there is the *actual* move of the prey
         // where could the prey be going?
         for(Environment.action act : Environment.action.values()) {
             PreyAction tmp = new PreyAction();
             double p = tmp.getActionProbability(act);
             Coordinates preyPossiblePos = preyNewPos.getShifted(act);
             ret.add(this.getState(preyPossiblePos), p);
         }
         return ret;
     }
 
     /******************************* Iterator Related ************************************/
     @Override
     public Iterator iterator() {
         this.resetIterator();
         return this;
     }
 
     private void resetIterator() { this.iter_pos = 0; }
 
     @Override
     public boolean hasNext() { return this.iter_pos < (Math.pow(Util.DIM, 2)); }
 
     @Override
     public ReducedState next() {
         if(this.hasNext()){
             int tmp = this.iter_pos;
             int j = tmp % Util.DIM;
             tmp = tmp / Util.DIM;
             int i = tmp % Util.DIM;
             this.iter_pos++;
             return this.states[i][j];
         }
         throw new NoSuchElementException();
     }
 
     @Override
     public void remove() { }
     /******************************* Iterator Related ************************************/
 
 	@Override
 	public ArrayList<State> getNeighbors(State s) {
 		ArrayList<State> ret = new ArrayList<State>();
 		for (action a : action.values()) {
 			ret.add(this.getNextState(s, a));
 		}
 		return ret;
 	}
 }
