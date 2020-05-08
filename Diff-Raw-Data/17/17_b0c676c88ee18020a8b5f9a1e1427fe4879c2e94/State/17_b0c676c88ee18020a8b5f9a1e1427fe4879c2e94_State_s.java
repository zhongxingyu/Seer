 package edu.gatech.cs3240.lexer;
 
 import java.util.ArrayList;
 /*
  * Represents a single state in a NFA
  * Specifies if it is an accept state
  */
 public class State{
 	
 	private final int EMPTY = 95;
 	private boolean accept = false;
 	
 	/*
 	 * Array of array lists that store states
 	 * The ith array list contains states that are transitioned to on char = i+32
 	 * The 96th array list contains states that are transitioned to on empty strings
 	 */
 	private ArrayList<State> transitions[] = new ArrayList[96];	
 	
 	/*
 	 * Make a clean state with the specified accept value
 	 */
 	public State(boolean accept){
 		for(int i = 0; i < transitions.length; i++ ){
 			transitions[i] = new ArrayList<State>();
 		}
 		this.accept = accept;
 	}
 	
 	/*
 	 * Makes a state with transitions to the given state on the given chars
 	 */
 	public State(ArrayList<Integer> chars, State state){
 		for(int i = 0; i < transitions.length; i++ ){
 			transitions[i] = new ArrayList<State>();
 		}
 		for(int i :chars){
 			transitions[i-32].add(state);
 		}
 	}
 	
 	/*
 	 * Add a transition on the empty string to the given state
 	 */
 	public void onEmpty(State to){
 		transitions[EMPTY].add(to);
 	}
 	
 	/*
 	 * Change the accept value
 	 */
 	public void setAccept(boolean accept){
 		this.accept = accept;
 	}
 
 }
