 package ai.hsm;
 
 import java.util.LinkedList;
 
 public class SubMachineState extends State {
 	
 	public LinkedList<State> states = new LinkedList<State>();
 	
 	public State initialState;
 	public State currentState;
 	
 	// We get states by adding ourself to our active children
 	@Override
 	public LinkedList<State> getStates() {
 		LinkedList<State> result = new LinkedList<State>();
 		result.add(this);
 		
 		if (currentState != null) {
 			result.addAll(currentState.getStates());
 		}
 		
 		return result;
 	}
 	
 	@Override
 	public UpdateResult update() {
 		UpdateResult result;
 		
 		if (initialState == null) {
			return super.update();
 		}
 		
 		
 		// if we're in no state, use the initial state
 		if (currentState == null) {
 			currentState = initialState;
 			result = new UpdateResult();
 			result.actions = currentState.getEntryAction();
 			return result;
 		}
 		
 		// try to find a transition in the current state
 		Transition triggeredTransition = null;
 		for (Transition transition : currentState.transitions) {
 			if (transition.isTriggered()) {
 				triggeredTransition = transition;
 				break;
 			}
 		}		
 		
 		// if we have found one
 		if (triggeredTransition != null) {
 			//System.out.println("Found transition");
 			result = new UpdateResult();
 			result.actions = new LinkedList<Action>();
 			result.transition = triggeredTransition;
 			result.level = triggeredTransition.getLevel();
 		} 
 		// otherwise recurse down for a result
 		else {
 			//System.out.println("going deeper");
 			return currentState.update();
 		}
 		
 		// check if the result contains a transition
 		if (result.transition != null) {
 			// Act based on its level
 			if (result.level == 0) {
 				
 				//System.out.println("Same level");
 				//It's on our level: honor it
 				State targetState = result.transition.getTargetState();
 				result.actions.addAll(currentState.getExitAction());
 				result.actions.addAll(result.transition.getAction());
 				result.actions.addAll(targetState.getEntryAction());
 				
 				// set current state
 				currentState = targetState;
 				
 				// add our normal action (we may be a state)
				result.actions.addAll(getAction());
 			} else if (result.level > 0) {
 				//System.out.println("Different level");
 				// It's destined for a higher level
 				// Exit our current state
 				result.actions.addAll(currentState.getExitAction());
 				currentState = null;
 				
 				// Decrease the number of levels to go
 				result.level--;
 			} else {
 				//System.out.println("Different level");
 				// It needs to be passed down
 				State targetState = result.transition.getTargetState();
 				SubMachineState targetMachine = targetState.parent;
 				result.actions.addAll(result.transition.getAction());
 				result.actions.addAll(targetMachine.updateDown(targetState, -result.level));
 				
 				// clear the transition, so nobody else does it
 				result.transition = null;
 			}
 		}
 		
 		// If we didn't get a transition
 		else {
 			
 			// We can simply do our normal action
 			result.actions.addAll(getAction());
 		}
 		
 		// return result
 		return result;
 	}
 	
 	// Recurses up the parent hierarchy, transitioning into 
 	// each state in turn for the given number of levels
 	private LinkedList<Action> updateDown(State state, int level) {
 		LinkedList<Action> actions;
 		// If we're not at top level, continue recursing
 		if (level > 0) {
 			// Pass ourself as the transition state to our parent
 			actions = parent.updateDown(this, level-1);
 		} 
 		
 		// Otherwise we have not actions to add to
 		else {
 			actions = new LinkedList<Action>();
 		}
 		
 		// If we have a current state, exit it
 		if (currentState != null) {
 			actions.addAll(currentState.getExitAction());
 		}
 		
 		// Move to the new state, and return all the actions
 		currentState = state;
 		
 		return null;
 	}
 	
 	public void addState(SubMachineState state) {
 		states.add(state);
 		state.parent = this;
 	}
 }
