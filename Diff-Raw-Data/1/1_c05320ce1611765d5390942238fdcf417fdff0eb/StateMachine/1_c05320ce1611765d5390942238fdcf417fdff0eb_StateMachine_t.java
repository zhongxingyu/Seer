 package com.coalmine.jstately.machine;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import com.coalmine.jstately.graph.StateGraph;
 import com.coalmine.jstately.graph.state.CompositeState;
 import com.coalmine.jstately.graph.state.FinalState;
 import com.coalmine.jstately.graph.state.State;
 import com.coalmine.jstately.graph.transition.Transition;
 import com.coalmine.jstately.machine.listener.StateMachineEventListener;
 
 
 /** Representation of a basic state machine. */
 public class StateMachine<MachineInput,TransitionInput> {
 	protected StateGraph<TransitionInput>						stateGraph;
 	protected State<TransitionInput>							currentState;
 	protected InputAdapter<MachineInput,TransitionInput>		inputProvider;
 	protected List<StateMachineEventListener<TransitionInput>>	eventListeners = new ArrayList<StateMachineEventListener<TransitionInput>>();
 
 	private StateMachine<TransitionInput,TransitionInput>		compositeStateMachine;
 
 	public StateMachine() { }
 
 	public StateMachine(StateGraph<TransitionInput> stateGraph, InputAdapter<MachineInput,TransitionInput> inputProvider) {
 		this.stateGraph		= stateGraph;
 		this.inputProvider	= inputProvider;
 	}
 
 	/**
 	 * Initialize the machine to its start state, calling its {@link NonFinalState#onEnter()} method.
 	 * @throws IllegalStateException thrown if no start state was specified or if the machine has already been started.
 	 */
 	public void start() {
 		if(hasStarted()) {
 			throw new IllegalStateException("Machine has already started.");
 		}
 		if(stateGraph.getStartState()==null) {
 			throw new IllegalStateException("No start state specified.");
 		}
 		if(!stateGraph.getStates().contains(stateGraph.getStartState())) {
 			throw new IllegalStateException("Start state not defined in graph.");
 		}
 
 		enterState(stateGraph.getStartState());
 	}
 
 	/** @return Whether the machine has a current state. */
 	public boolean hasStarted() {
 		return currentState!=null;
 	}
 
 	/** Resets the machine's state to null without calling {@link NonFinalState#onExit()} on the current state (if there is one.) */
 	public void reset() {
 		currentState = null;
 	}
 
 	/**
 	 * Providing the machine's input to its InputAdapter, the resulting transition input(s) are
 	 * iterated over.  For each input, the machine follows the first transition that is valid
 	 * according to its {@link Transition#isValid(Object)} method.
 	 * @param input Machine input from which Transition inputs are generated to evaluate and transition on.
 	 * @return Whether any of input's subsequent Transition inputs were ignored (no valid transition was found) while evaluating.
 	 * @throws IllegalStateException thrown if no {@link InputAdapter} has been set.
 	 */
 	public boolean evaluateInput(MachineInput input) {
 		if(inputProvider==null) {
 			throw new IllegalStateException("No InputAdapter specified prior to calling evaluateInput().");
 		}
 
 		boolean inputIgnored = false;
 		inputProvider.queueInput(input);
 		while(inputProvider.hasNext()) {
 			TransitionInput transitionInput = inputProvider.next();
 
 			TransitionInput inputToEvaluate = null;
 			if(currentState instanceof CompositeState) {
 				// Delegate the evaluation of the input
 				compositeStateMachine.evaluateInput(transitionInput);
 
 				// If the composite reaches a final state, extract the state's output to evaluate on this graph/machine
 				if(compositeStateMachine.getState() instanceof FinalState) {
 					inputToEvaluate = ((FinalState<TransitionInput>)compositeStateMachine.getState()).getResult();
 				}
 			} else {
 				inputToEvaluate = transitionInput;
 			}
 
 			if(inputToEvaluate != null) {
 				Transition<TransitionInput> validTransition = getFirstValidTransitionFromCurrentState(inputToEvaluate);
 				if(validTransition==null) {
 					inputIgnored = true;
 				} else {
 					transition(validTransition,transitionInput);
 				}
 			}
 		}
 
 		return inputIgnored;
 	}
 
 	public State<TransitionInput> getSubState() {
 		return compositeStateMachine==null? null : compositeStateMachine.getState();
 	}
 
 	public Set<Transition<TransitionInput>> getValidTransitionsFromCurrentState(TransitionInput input) {
 		if(!hasStarted()) {
 			throw new IllegalStateException("Machine has not started.");
 		}
 		return stateGraph.getValidTransitionsFromTail(currentState, input);
 	}
 
 	public Transition<TransitionInput> getFirstValidTransitionFromCurrentState(TransitionInput input) {
 		if(!hasStarted()) {
 			throw new IllegalStateException("Machine has not started.");
 		}
 		return stateGraph.getFirstValidTransitionFromTail(currentState, input);
 	}
 
 	public Set<Transition<TransitionInput>> getTransitionsFromCurrentState() {
 		if(!hasStarted()) {
 			throw new IllegalStateException("Machine has not started.");
 		}
 		return stateGraph.getTransitionsFromTail(currentState);
 	}
 
 	/** @return A collection of states that could potentially be transitioned to from the current state, ignoring whether they are valid. */
 	public Set<State<TransitionInput>> getStatesFromCurrentState() {
 		if(!hasStarted()) {
 			throw new IllegalStateException("Machine has not started.");
 		}
 		return stateGraph.getStatesFromTail(currentState);
 	}
 
 	/** @return A collection of states that could be transitioned to given the provided input. */
 	public Set<State<TransitionInput>> getValidStatesFromCurrentState(TransitionInput input) {
 		if(!hasStarted()) {
 			throw new IllegalStateException("Machine has not started.");
 		}
 		return stateGraph.getValidStatesFromTail(currentState,input);
 	}
 
 	/**
 	 * Follows the given transition without checking its validity.  Calls {@link State#onExit()} on
 	 * the current state, followed by {@link Transition#onTransition()} on the given transition,
 	 * followed by {@link State#onEnter()} on the machine's updated current state.
 	 * @param transition State transition to follow.
 	 * @throws IllegalArgumentException thrown if the StateMachine has not started or the given Transition does not
 	 * originate at the machine's current state.
 	 */
 	protected void transition(Transition<TransitionInput> transition, TransitionInput input) {
 		if(!hasStarted()) {
 			throw new IllegalStateException("Machine has not started.");
 		}
 		if(transition==null || !transition.getTail().equals(currentState)) {
 			throw new IllegalArgumentException("Transition not allowed from machine's current state.");
 		}
 
 		exitState(transition.getTail());
 
 		for(StateMachineEventListener<TransitionInput> listener : eventListeners) {
 			listener.beforeTransition(transition, input);
 		}
 		transition.onTransition(input);
 		for(StateMachineEventListener<TransitionInput> listener : eventListeners) {
 			listener.afterTransition(transition, input);
 		}
 
 		enterState(transition.getHead());
 	}
 
 	private void enterState(State<TransitionInput> newState) {
 		for(StateMachineEventListener<TransitionInput> listener : eventListeners) {
 			listener.beforeStateEntered(newState);
 		}
 
 		currentState = newState;
 		currentState.onEnter();
 
 		if(currentState instanceof CompositeState) {
 			CompositeState<TransitionInput> compositeState = (CompositeState<TransitionInput>)currentState;
 			compositeStateMachine = new StateMachine<TransitionInput,TransitionInput>(compositeState.getStateGraph(), new DefaultInputAdapter<TransitionInput>());
			compositeStateMachine.setEventListeners(eventListeners);
 			compositeStateMachine.start();
 		}
 
 		for(StateMachineEventListener<TransitionInput> listener : eventListeners) {
 			listener.afterStateEntered(newState);
 		}
 	}
 
 	private void exitState(State<TransitionInput> oldState) {
 		for(StateMachineEventListener<TransitionInput> listener : eventListeners) {
 			listener.beforeStateExited(oldState);
 		}
 
 		oldState.onExit();
 
 		if(oldState instanceof CompositeState) {
 			compositeStateMachine = null;
 		}
 
 		for(StateMachineEventListener<TransitionInput> listener : eventListeners) {
 			listener.afterStateExited(oldState);
 		}
 	}
 
 
 	public StateGraph<TransitionInput> getStateGraph() {
 		return stateGraph;
 	}
 	public void setStateGraph(StateGraph<TransitionInput> stateGraph) {
 		this.stateGraph = stateGraph;
 	}
 
 	public void setInputProvider(InputAdapter<MachineInput,TransitionInput> inputProvider) {
 		this.inputProvider = inputProvider;
 	}
 	public InputAdapter<MachineInput,TransitionInput> getInputProvider() {
 		return inputProvider;
 	}
 
 	public State<TransitionInput> getState() {
 		return currentState;
 	}
 
 	/**
 	 * Sets the machine's state without calling any event methods such as {@link State#onEnter()}
 	 * or {@link State#onExit()}. This is mostly for testing.  API users should generally avoid
 	 * setting a machine's state explicitly.
 	 */
 	public void setCurrentState(State<TransitionInput> newState) {
 		this.currentState = newState;
 	}
 
 	public List<StateMachineEventListener<TransitionInput>> getEventListeners() {
 		return eventListeners;
 	}
 	public void setEventListeners(List<StateMachineEventListener<TransitionInput>> eventListeners) {
 		if(eventListeners==null) {
 			throw new IllegalArgumentException("Provided EventListener list cannot be null.");
 		}
 		this.eventListeners = eventListeners;
 	}
 	public void addEventListener(StateMachineEventListener<TransitionInput> eventListener) {
 		eventListeners.add(eventListener);
 	}
 }
 
 
 
 
