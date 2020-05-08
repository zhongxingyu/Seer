 package policy;
 
 import action.LearnedAction;
 import action.PossibleActions;
 import environment.Environment.action;
 import state.State;
 
 import java.util.HashMap;
 
 public abstract class Policy {
 	
 	protected HashMap<State, PossibleActions> stateActionMapping;
 	
 	public Policy(){
 		this.stateActionMapping = new HashMap<State, PossibleActions>();
 	}
 	
 	public action getAction(State s) {
         PossibleActions ac = this.stateActionMapping.get(s);
 		return ac.getAction();
 	}
 	
 	public String getActionString(State s) {
 		switch (this.stateActionMapping.get(s).getAction()) {
 			case NORTH: return "^";
 			case SOUTH:	return "V";
 			case EAST:	return ">";
 			case WEST:	return "<";
 			case WAIT:	return "-";
 			default:	return "?";			
 		}
 	}
 	
 	public double getActionProbability(State s, action a) {
 		return this.stateActionMapping.get(s).getActionProbability(a);
 	}
 	
 	/**
 	 * For State s, set action probability of Action a to 1.0 and all the rest to 0.
 	 * (Deterministic policy) 
 	 */
 	public void setUniqueAction(State s, action a) {
 		if (a == null) { return; }
 		if(!this.stateActionMapping.containsKey(s)){
 			this.stateActionMapping.put(s, new LearnedAction());
 		}
 		this.stateActionMapping.get(s).setAllActionProbabilitiesTo(0.0);
 		this.stateActionMapping.get(s).setActionProbability(a, 1.0);
 	}
 
 	public void initializeStateValues(double d) {
		for (State state : this.stateActionMapping.keySet()) {
 			state.setStateValue(d);
		}
 	}
 }
