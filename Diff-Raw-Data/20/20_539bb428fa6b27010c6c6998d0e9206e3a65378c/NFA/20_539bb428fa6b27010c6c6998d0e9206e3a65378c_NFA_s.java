 package util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 
 public class NFA {
     // Use this for epsilon transitions-- hashmaps allow a null key.
     public static final Character EPSILON = null;
 
     // we need two keys: a State and an input Character.
     private Map<State, Map<Character, Set<State>>> transitionTable;
     private State startState;
 
 
     public NFA() { 
         this.transitionTable = new HashMap<State, Map<Character, Set<State>>>();
     }
     public NFA(final Map<State, Map<Character, Set<State>>> transitionTable, final State startState) {
         this.transitionTable = transitionTable;
         this.startState = startState;
     }
 
     // Unlike DFA.doTransition, this doesn't have any side-effects:
     // It doesn't alter the current state or anything.
     public Set<State> getTransition(final State initialState, final Character input) {
         if (!this.transitionTable.containsKey(initialState) || 
             !this.transitionTable.get(initialState).containsKey(input)) {
             return new HashSet<State>();
         }
         return this.transitionTable.get(initialState).get(input);
     }
 
     public State getStartState() {
         return startState;
     }
 
     // null checking necessary
     public Set<State> getEpsilonClosure(final State state) {
         Queue<State> statesToVisit = new LinkedList<State>();
         statesToVisit.add(state);
         HashSet<State> epsilonClosure = new HashSet<State>();
         while (!statesToVisit.isEmpty()) {
             State currentState = statesToVisit.poll();
             epsilonClosure.add(currentState);
             for (State nextState : getTransition(currentState, EPSILON)) {
                 if (!statesToVisit.contains(nextState) && !epsilonClosure.contains(nextState)) {
                     statesToVisit.add(nextState);
                 }
             }
         } 
         return epsilonClosure;
 
     }
     
 
     
     
     public Set<Character> getAllowableCharacters() {
     	
     	Set<Character> charSet = new HashSet<Character>();
     	for (Map<Character,Set<State>> map : transitionTable.values()) {
     		charSet.addAll(map.keySet());
     	}
     	return charSet;
     }
     
     public Set<State> getAllStates() {
     	return transitionTable.keySet();
     }
     
     public Character getEpsilon() {
     	return EPSILON;
     }
     public NFA withStartState(State startState) {
         this.startState = startState;
         return this;
     }
 
     public NFA withTransition(State original, Character input, State transition) {
         if (!transitionTable.containsKey(original)) {
             transitionTable.put(original, new HashMap<Character, Set<State>>());
         }
         if (!transitionTable.get(original).containsKey(input)) {
             transitionTable.get(original).put(input, new HashSet<State>());
         }
 
         transitionTable.get(original).get(input).add(transition);
         
         if (!transitionTable.containsKey(transition)) {
             transitionTable.put(transition, new HashMap<Character, Set<State>>());
         }
             
         return this;
     }
 
         
     public static NFA acceptCharacter(char c) {
         State start = new State();
         State end = new State(true);
 
         NFA nfa = new NFA();
         nfa.startState = start;
         nfa.withTransition(start, c, end);
         return nfa;
     }
 
    @SuppressWarnings("unchecked")
     public void replace(State oldState, State newState) {
        Map<State, Map<Character, Set<State>>> clone = 
            (Map<State, Map<Character, Set<State>>>) ((HashMap<State, Map<Character, Set<State>>>) transitionTable).clone();
         for (State inState : clone.keySet()) {
             for (Entry<Character, Set<State>> trans : clone.get(inState).entrySet()) {
                 for (State outState : trans.getValue()) {
                     if (outState.equals(oldState)) {
                         transitionTable.get(inState).get(trans.getKey()).remove(oldState);
                         transitionTable.get(inState).get(trans.getKey()).add(newState);
                     }
                 }
             }
 
             if (inState.equals(oldState)) {
                 transitionTable.put(newState, transitionTable.get(inState));
                 transitionTable.remove(inState);
             }
         }
     }
     public Set<State> getAcceptStates() {
         Set<State> acceptStates = new HashSet<State>();
         for (State state : transitionTable.keySet()) {
             if (state.isAccepting()) {
                 acceptStates.add(state);
             }
         }
         return acceptStates;
     }       
     public NFA withTransitions(Map<State, Map<Character, Set<State>>> transitions) {
         for (State orig : transitions.keySet()) {
             for (Character input : transitions.get(orig).keySet()) {
                 for (State fin : transitions.get(orig).get(input)) {
                     this.withTransition(new State(orig), input, new State(fin));
                 }
             }
         }
         return this;
     }
 
     public static NFA union(NFA one, NFA two) {
         if (two == null) {
             return one;
         }
         if (one == null) {
             return two;
         }
         NFA union = new NFA().withTransitions(one.transitionTable)
                              .withTransitions(two.transitionTable);
         union.startState = new State();
         union.withTransition(union.startState, EPSILON, one.startState);
         union.withTransition(union.startState, EPSILON, two.startState);
         return union;
     }
 
     public static NFA concatenate(NFA one, NFA two) {
         if (two == null) {
             return one;
         }
         if (one == null) {
             return two;
         }
         NFA concat = new NFA().withTransitions(one.transitionTable)
                               .withTransitions(two.transitionTable);
         concat.startState = one.startState;
         for (State oldAccept : one.getAcceptStates()) {
             State newState = new State(oldAccept);
             newState.setAccepting(false);
             concat.replace(oldAccept, newState);
             concat.withTransition(newState, EPSILON, two.startState);
         }
         return concat;
     }
 
     public static NFA kleeneStar(NFA one) {
         if (one == null) {
             return null;
         }
         NFA star = new NFA().withTransitions(one.transitionTable);
         star.startState = new State(true);
         star.withTransition(star.startState, EPSILON, one.startState);
         for (State accept : one.getAcceptStates()) {
             star.withTransition(accept, EPSILON, one.startState);
         }
         return star;
     }
 
     public static NFA kleenePlus(NFA one) {
         return concatenate(one, kleeneStar(one));
     }
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("<NFA\n");
 
         List<State> inputStates = new ArrayList<State>();
         inputStates.addAll(transitionTable.keySet());
         Collections.sort(inputStates);
         for (State input : inputStates) {
             sb.append(input.getName());
             if (input.isAccepting()) {
                 sb.append("*");
             }
             if (input.equals(startState)) {
                 sb.append("(S)");
             }
             sb.append(" {");
             for (Entry<Character, Set<State>> transition : transitionTable.get(input).entrySet()) {
                 if (transition.getKey() == EPSILON) {
                     sb.append("epsilon");
                 } else {
                     sb.append(transition.getKey());
                 }
                 sb.append(": {");
                 for (State s : transition.getValue()) {
                     sb.append(s.getName());
                     if (s.isAccepting()) {
                         sb.append("*");
                     }
                     if (s.equals(startState)) {
                         sb.append("(S)");
                     }
                     sb.append(", ");
                 }
                 sb.append("}");
             }
             sb.append("}\n");
         }
         sb.append(">");
         return sb.toString();
     }
 
 
}
