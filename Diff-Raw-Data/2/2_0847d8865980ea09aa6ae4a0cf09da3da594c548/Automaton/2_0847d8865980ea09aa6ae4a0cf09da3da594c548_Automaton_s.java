 package de.hszg.atocc.core.util.automaton;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 public final class Automaton {
 
     public static final String EPSILON = "EPSILON";
 
     private static final String FA_STACK_SYMBOL = "Core|FA_STACK_SYMBOL";
     private static final String FA_STACK_ALPHABET = "Core|FA_STACK_ALPHABET";
     private static final String MM_FINAL_STATE = "Core|MM_FINAL_STATE";
     private static final String MM_OUTPUT_ALPHABET = "Core|MM_OUTPUT_ALPHABET";
 
     private AutomatonType type = AutomatonType.DEA;
     private Set<String> alphabet = new HashSet<>();
     private Set<String> stackAlphabet = new HashSet<>();
     
     private Set<String> outputAlphabet = new HashSet<>();
 //  TODO  private Map<String, String> outputMappings = new HashMap<>();
     
     private Set<String> states = new HashSet<>();
     private Map<String, Set<Transition>> transitions = new HashMap<>();
 
     private String initialState = "";
     private String initialStackSymbol = "";
     private Set<String> finalStates = new HashSet<>();
 
     private boolean containsEpsilonRules;
 
     public Automaton(AutomatonType automatonType) {
         type = automatonType;
     }
 
     public Automaton(Automaton other) throws InvalidStateException, InvalidTransitionException {
         type = other.getType();
         setAlphabet(other.getAlphabet());
         setStates(other.getStates());
 
         for (String s : states) {
             for (Transition transition : other.getTransitionsFrom(s)) {
                 addTransition(transition);
             }
         }
 
         initialState = other.getInitialState();
 
         for (String s : other.getFinalStates()) {
             addFinalState(s);
         }
     }
 
     public boolean containsEpsilonRules() {
         return containsEpsilonRules;
     }
 
     public AutomatonType getType() {
         return type;
     }
 
     public Set<String> getAlphabet() {
         return Collections.unmodifiableSet(alphabet);
     }
 
     public void addAlphabetItem(String alphabetCharacter) throws InvalidAlphabetCharacterException {
         if (alphabetCharacter == null || "".equals(alphabetCharacter)) {
             throw new InvalidAlphabetCharacterException(
                     "null or empty string not allowed as alphabet item");
         }
 
         alphabet.add(alphabetCharacter);
     }
 
     public void setAlphabet(Set<String> alphabetCharacters) {
         alphabet.clear();
         alphabet.addAll(alphabetCharacters);
     }
 
     public Set<String> getStackAlphabet() {
         if (isFiniteAutomaton()) {
             throw new UnsupportedOperationException(FA_STACK_ALPHABET);
         }
 
         return Collections.unmodifiableSet(stackAlphabet);
     }
 
     public void addStackAlphabetItem(String alphabetCharacter)
             throws InvalidAlphabetCharacterException {
         if (alphabetCharacter == null || "".equals(alphabetCharacter)) {
             throw new InvalidAlphabetCharacterException(
                     "null or empty string not allowed as stack alphabet item");
         }
 
         stackAlphabet.add(alphabetCharacter);
     }
 
     public void setStackAlphabet(Set<String> alphabetCharacters) {
         if (isFiniteAutomaton()) {
             throw new UnsupportedOperationException(FA_STACK_ALPHABET);
         }
 
         stackAlphabet.clear();
         stackAlphabet.addAll(alphabetCharacters);
     }
 
     public Set<String> getOutputAlphabet() {
         if (!isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_OUTPUT_ALPHABET);
         }
         
         return outputAlphabet;
     }
     
     public void setOutputAlphabet(Set<String> alphabetCharacters) {
         if (!isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_OUTPUT_ALPHABET);
         }
         
         outputAlphabet.clear();
         outputAlphabet.addAll(alphabetCharacters);
     }
 
     public Set<String> getStates() {
         return Collections.unmodifiableSet(states);
     }
 
     public SortedSet<String> getSortedStates() {
         return new TreeSet<String>(states);
     }
 
     public void addState(String state) throws InvalidStateException {
         if (state == null || "".equals(state)) {
             throw new InvalidStateException("null or empty string");
         }
 
         states.add(state);
 
         if (!transitions.containsKey(state)) {
             transitions.put(state, new HashSet<Transition>());
         }
     }
 
     public void setStates(Set<String> stateNames) throws InvalidStateException {
         states.clear();
 
         for (String state : stateNames) {
             addState(state);
         }
     }
 
     public void removeState(String state) {
         states.remove(state);
 
         final Collection<Transition> transitionsFrom = new LinkedList<>();
         transitionsFrom.addAll(getTransitionsFrom(state));
         for (Transition transition : transitionsFrom) {
             transitions.get(state).remove(transition);
         }
 
         final Collection<Transition> transitionsTo = new LinkedList<>();
         transitionsTo.addAll(getTransitionsTo(state));
         for (Transition transition : transitionsTo) {
             transitions.get(transition.getSource()).remove(transition);
         }
 
         finalStates.remove(state);
 
         if (initialState.equals(state)) {
             initialState = "";
         }
     }
 
     public Set<Transition> getTransitions() {
         final Set<Transition> allTransitions = new HashSet<>();
 
         for (Set<Transition> transitionSet : transitions.values()) {
             allTransitions.addAll(transitionSet);
         }
 
         return allTransitions;
     }
 
     public Set<Transition> getTransitionsFrom(String state) {
         return transitions.get(state);
     }
 
     public Set<Transition> getTransitionsTo(String state) {
         final Set<Transition> transitionsToState = new HashSet<>();
 
         for (String s : states) {
             for (Transition t : transitions.get(s)) {
                 if (t.getTarget().equals(state)) {
                     transitionsToState.add(t);
                 }
             }
         }
 
         return transitionsToState;
     }
 
     public Set<String> getTargetsFor(String state, String read) {
         final Set<String> targets = new HashSet<>();
 
         for (Transition transition : getTransitionsFrom(state)) {
             if (transition.getCharacterToRead().equals(read)) {
                 targets.add(transition.getTarget());
             }
         }
 
         return targets;
     }
 
     public void setTransitions(Set<Transition> transitionSet) throws InvalidTransitionException {
         for (Transition transition : transitionSet) {
             addTransition(transition);
         }
     }
 
     public void addTransition(Transition transition) throws InvalidTransitionException {
         try {
             verifyStateExists(transition.getSource());
             verifyStateExists(transition.getTarget());
             verifyAlphabetCharacterExists(transition.getCharacterToRead());
 
             transitions.get(transition.getSource()).add(transition);
 
             if (EPSILON.equals(transition.getCharacterToRead())) {
                 containsEpsilonRules = true;
             }
         } catch (final InvalidStateException | InvalidAlphabetCharacterException e) {
             throw new InvalidTransitionException(e);
         }
     }
 
     public void removeTransitions(Set<Transition> transitionsToRemove) {
         for (Transition transition : transitionsToRemove) {
             for (String state : states) {
                 if (transitions.get(state).remove(transition)) {
                     continue;
                 }
             }
         }
     }
 
     public String getInitialState() {
         return initialState;
     }
 
     public void setInitialState(String state) throws InvalidStateException {
         initialState = state;
         addState(state);
     }
 
     public String getInitialStackSymbol() {
         if (isFiniteAutomaton()) {
             throw new UnsupportedOperationException(FA_STACK_SYMBOL);
         }
 
         return initialStackSymbol;
     }
 
     public void setInitialStackSymbol(String symbol) throws InvalidAlphabetCharacterException {
         if (isFiniteAutomaton()) {
             throw new UnsupportedOperationException(FA_STACK_SYMBOL);
         }
 
         if (!stackAlphabet.contains(symbol)) {
             throw new InvalidAlphabetCharacterException(
                     "Stack alphabet does not contain this symbol: " + symbol);
         }
 
         initialStackSymbol = symbol;
     }
 
     public Set<String> getFinalStates() {
         if (isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_FINAL_STATE);
         }
 
         return finalStates;
     }
 
     public void addFinalState(String state) throws InvalidStateException {
         if (isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_FINAL_STATE);
         }
 
         if (!states.contains(state)) {
             throw new InvalidStateException(state);
         }
 
         finalStates.add(state);
     }
 
     public boolean isFinalState(String state) {
         if (isMealyOrMooreAutomaton()) {
             throw new UnsupportedOperationException(MM_FINAL_STATE);
         }
 
         return finalStates.contains(state);
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
 
         result = prime * result + alphabet.hashCode();
         result = prime * result + finalStates.hashCode();
         result = prime * result + initialState.hashCode();
         result = prime * result + states.hashCode();
         result = prime * result + transitions.hashCode();
         result = prime * result + type.hashCode();
 
         return result;
     }
 
     // CHECKSTYLE:OFF
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof Automaton)) {
             return false;
         }
 
         final Automaton other = (Automaton) obj;
 
         return type == other.type && alphabet.equals(other.alphabet)
                 && finalStates.equals(other.finalStates) && initialState.equals(other.initialState)
                 && states.equals(other.states) && transitions.equals(other.transitions);
     }
 
     // CHECKSTYLE:ON
 
     @Override
     public String toString() {
         if (isFiniteAutomaton()) {
             return finiteAutomatonToString();
         } else {
             return pushDownAutomatonToString();
         }
     }
 
     private String finiteAutomatonToString() {
         return String.format("%s = (%s, %s, %s, %s, %s)", type.name(), getSortedStates(), alphabet,
                 transitions, initialState, finalStates);
     }
 
     private String pushDownAutomatonToString() {
         return String
                 .format("%s = (%s, %s, %s, %s, %s, %s, %s)", type.name(), getSortedStates(),
                         alphabet, stackAlphabet, transitions, initialState, initialStackSymbol,
                         finalStates);
     }
 
     private void verifyStateExists(String state) throws InvalidStateException {
         if (!states.contains(state)) {
             throw new InvalidStateException(state);
         }
     }
 
     private void verifyAlphabetCharacterExists(String character)
             throws InvalidAlphabetCharacterException {
         if (EPSILON.equals(character) && isNondeterministicAutomaton()) {
             return;
         }
 
         if (!alphabet.contains(character)) {
             throw new InvalidAlphabetCharacterException(character);
         }
     }
 
     private boolean isFiniteAutomaton() {
         return type == AutomatonType.NEA || type == AutomatonType.DEA;
     }
 
     private boolean isPushDownAutomaton() {
         return type == AutomatonType.NKA || type == AutomatonType.DKA;
     }
 
     private boolean isNondeterministicAutomaton() {
         return type == AutomatonType.NEA || type == AutomatonType.NKA;
     }
 
     private boolean isMealyOrMooreAutomaton() {
         return type == AutomatonType.MEALY || type == AutomatonType.MOORE;
     }
 }
