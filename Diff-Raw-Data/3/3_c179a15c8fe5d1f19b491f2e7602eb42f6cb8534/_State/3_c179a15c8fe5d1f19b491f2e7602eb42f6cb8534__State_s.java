 package language;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import utilities.ErrorUtilities;
 import utilities.StringUtilities;
 
 /**
  * This is an information holder class.
  * 
  * @author Trironk Kiatkungwanglai
  */
 public class _State {
 
 	/**
 	 * This string serves as a reader-friendly name. The naming convention is
 	 * "State" followed by the State's identifier (e.g. State1, State2, State3). 
 	 */
 	public String name;
 
 	/**
 	 * This identifier just serves as a way to easily name new States
 	 */
 	public int id;
 
 	/**
 	 * This HashMap enables performant traversal of a Table, enabling retrieval
 	 * of destination states in constant time.
 	 */
 	public Map<Character, Set<_State>> charToStateSetMap;
 
 	/**
 	 * This HashMap enables performant generation of reader-friendly string
 	 * representations of this State.
 	 */
 	public Map<_State, Set<Character>> stateToCharSetMap;
 
 	/**
 	 * This Set contains all states to which this state can epsilon transition
 	 * into.
 	 */
 	public Set<_State> epsilonTransitions;
 
 	/**
 	 * Blank constructor.
 	 */
 	public _State(int identifier) {
 		this.name = "State" + identifier;
 		this.id = identifier;
 		this.charToStateSetMap = new HashMap<Character, Set<_State>>();
 		this.stateToCharSetMap = new HashMap<_State, Set<Character>>();
 		this.epsilonTransitions = new HashSet<_State>();
 	}
 
 	/**
 	 * This method returns the State that a character would transition into, or
 	 * null if there is no defined map destination state.
 	 */
 	public Set<_State> getDestinations() {
 		Set<_State> result = new HashSet<_State>();
 		return getDestinations(result);
 	}
 	
 	private Set<_State> getDestinations(Set<_State> result)	{
 		// Include all State objects that any epsilon transition states can
 		// transition into as well.
 		for (_State equivalentState : this.epsilonTransitions) {
 			if (result.contains(equivalentState)) {
 				continue;
 			}
 			result.add(equivalentState);
 			result.addAll(equivalentState.getDestinations(result));
 		}
 		
 		for (_State s : stateToCharSetMap.keySet()) {
 			result.add(s);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * This method returns the set of characters that can make this state
 	 * transition into a given state.
 	 */
 	public Set<Character> getCharactersToDestinations(_State destination) {
 		return getCharactersToDestination(destination, new HashSet<_State>());
 	}
 	
 	private Set<Character> getCharactersToDestination(_State destination,
 			Set<_State> visited) {
 		Set<Character> result = new HashSet<Character>();
 		
 		// Include all State objects that any epsilon transition states can
 		// transition into as well.
 		for (_State equivalentState : this.epsilonTransitions) {
 			if (visited.contains(equivalentState)) {
 				continue;
 			}
 			visited.add(equivalentState);
 			result.addAll(equivalentState.getCharactersToDestination(
 					destination, visited));
 		}
 		
 		if (stateToCharSetMap.containsKey(destination)) {
 			result.addAll(stateToCharSetMap.get(destination));
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * This method returns the set of states that this state can transition
 	 * into from a given character.
 	 */
 	public Set<_State> getDestinationsFromCharacter(Character transition) {
 		return getDestinationsFromCharacter(transition, new HashSet<_State>());
 	}
 	
 	private Set<_State> getDestinationsFromCharacter(Character transition,
 			Set<_State> visited) {
 		Set<_State> result = new HashSet<_State>();
 		
 		// Include all State objects that any epsilon transition states can
 		// transition into as well.
 		for (_State equivalentState : this.epsilonTransitions) {
 			if (result.contains(equivalentState)) {
 				continue;
 			}
 			result.add(equivalentState);
 			visited.add(equivalentState);
 			result.addAll(equivalentState.getDestinationsFromCharacter(
 					transition, visited));
 		}
 		
 		if (charToStateSetMap.containsKey(transition)) {
 			for (_State s : charToStateSetMap.get(transition)) {
 				result.add(s);
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * This method returns a set of states that this state can transition into
 	 * with an epsilon.
 	 */
 	public Set<_State> getEpsilonStates() {
 		return getEpsilonStates(new HashSet<_State>());
 	}
 	
 	private Set<_State> getEpsilonStates(Set<_State> result) {
 		result.add(this);
 		for (_State epsilonState : this.epsilonTransitions) {
 			if (result.contains(epsilonState)) {
 				continue;
 			}
 			result.addAll(epsilonState.getEpsilonStates(result));
 		}
 		return result;
 	}
 	
 	/**
 	 * This method handles the bookkeeping for the insertion of a transition.
 	 */
 	public void addTransition(Character c, _State destination) {
 
 		// Duplicate checks
 		if (stateToCharSetMap.containsKey(destination) &&
 				stateToCharSetMap.get(destination).contains(c))
 		{
 						ErrorUtilities.throwError(c + " already exists in " +
 								destination.name + ".");
 			return;
 		}
 
 		if (charToStateSetMap.containsKey(c) == false) {
 			HashSet<_State> emptySet = new HashSet<_State>();
 			charToStateSetMap.put(c, emptySet);
 		} else {
 			ErrorUtilities.throwError(c + " already exists.");
 		}
 		charToStateSetMap.get(c).add(destination);
 
 		if (stateToCharSetMap.containsKey(destination) == false) {
 			HashSet<Character> emptySet = new HashSet<Character>();
 			stateToCharSetMap.put(destination, emptySet);
 		}
 		stateToCharSetMap.get(destination).add(c);
 	}
 	
 	/**
 	 * This method handles the bookkeeping for the deletion of a transition.
 	 */
 	public void removeTransition(char c, _State destination) {
 		// Existence checks.
 		if (stateToCharSetMap.containsKey(destination) == false ||
 				stateToCharSetMap.get(destination).contains(c) == false)
 		{
 			ErrorUtilities.throwError("There does not exist a transition from "
 					+ c + "to " + destination.name);
 			return;
 		}
 		
 		// Perform the actual removal.
 		stateToCharSetMap.get(destination).remove(c);
 		charToStateSetMap.get(c).remove(destination);
 		
 		// Handle deleting the only character or only destination.
 		if (stateToCharSetMap.get(destination).isEmpty()) {
 			stateToCharSetMap.remove(destination);
 		}
 		if (charToStateSetMap.get(c).isEmpty()) {
 			charToStateSetMap.remove(c);
 		}
 	}
 	
 	/**
 	 * This method allows for the mapping of a set of characters to a
 	 * destination.
 	 */
	public void addTransitions(Set<Character> characterSet, _State destination) {
 		for (Character c : characterSet) {
 			if (c == null) {
 				String errorMsg =
 						"Tried to add \"null\" as part of a Set<Character>.";
 				ErrorUtilities.throwError(errorMsg);
 			}
 			addTransition(c, destination);
 		}
 	}
 
 	/**
 	 * Overriding the default toString().
 	 */
 	public String toString() {
 		String result = new String();
 		
 		result += name + '\n';
 		
 		for (_State s : stateToCharSetMap.keySet()) {
 			result += "  " + s.name + ": " +
 					StringUtilities.escaped(
 							this.stateToCharSetMap.get(s).toString()) + "\n"; 
 		}
 		
 		if (this.epsilonTransitions.isEmpty() == false) {
 			result += "  Epsilon Transitions: ";
 			for (_State epsilonTransition : this.epsilonTransitions) {
 				result += epsilonTransition.name + ' ';
 			}
 		}
 	
 		return result;
 	}
 	
 	public boolean equals(Object rhs) {
 		if (rhs == null)
 			return false;
 		if (this.getClass() != rhs.getClass())
 			return false;
 		_State o = (_State)rhs;
 		return o.id == this.id;
 	}
 	
 	public int hashcode() {
 		return this.id;
 	}
 }
