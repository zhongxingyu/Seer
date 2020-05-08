 package nfa;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 /**
  * An NFA.
  * 
  * @author toriscope
  * 
  */
 public class NFA {
 
 	private final HashMap<String, State> states;
	private final State startState;
 
 	/**
 	 * Create an NFA.
 	 * 
 	 * @param startState
 	 *            the initial state.
 	 * @param additionalStates
 	 *            the additional states of the NFA.
 	 */
 	public NFA(final State startState, final State... additionalStates) {
 		this.startState = startState;
 		this.states = new HashMap<String, State>();
 		addState(this.startState);
 		for (State state : additionalStates) {
 			addState(state);
 		}
 	}
 
 	/**
 	 * Factory method of creating NFA
 	 * 
 	 * @param startState
 	 * @param additionalStates
 	 * @return
 	 */
	public static NFA createNFA(final State startState, final State... additionalStates) {
 		return new NFA(startState, additionalStates);
 	}
 
 	public void addState(final State... states) {
 		for (State state : states)
 			this.states.put(state.getName(), state);
 	}
 
 	public State getStartState() {
 		return this.startState;
 	}
 
 	/**
 	 * Get a state based on its formal name.
 	 * 
 	 * @param stateName
 	 *            the name of state.
 	 * @return the state with stateName.
 	 */
 	public State getState(final String stateName) {
 		if (this.states.containsKey(stateName))
			throw new RuntimeException("State " + stateName + " not found in NFA.");
 		else
 			return this.states.get(stateName);
 	}
 
 	/**
 	 * Clone the NFA and assign a new start state.
 	 * 
 	 * @param initialState
 	 * @return
 	 */
 	public NFA clone(final State initialState) {
 		NFA n = new NFA(initialState);
 		for (final Entry<String, State> stateTuple : this.states.entrySet()) {
 			n.addState(stateTuple.getValue());
 		}
 		return n;
 	}
 
 	/**
 	 * Returns whether this NFA is a valid DFA.
 	 * 
 	 * @return true if no states contain an empty transition or multiple
 	 *         same-character transitions.
 	 */
 	public boolean isDFA() {
 		for (final Entry<String, State> stateTuple : this.states.entrySet()) {
 			HashSet<Character> chars = new HashSet<Character>();
 			for (Transition state : stateTuple.getValue().getTransitions()) {
				if (state.isEmptyTransition() || chars.contains(state.getCharacter())) {
 					return false;
 				}
 				chars.add(state.getCharacter());
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		b.append("Starting: ").append(this.startState.getName()).append("\n");
 		for (Entry<String, State> s : this.states.entrySet()) {
			b.append(s.getKey()).append(": ").append(s.getValue().toString()).append("\n");
 		}
 		return b.toString();
 	}
 }
