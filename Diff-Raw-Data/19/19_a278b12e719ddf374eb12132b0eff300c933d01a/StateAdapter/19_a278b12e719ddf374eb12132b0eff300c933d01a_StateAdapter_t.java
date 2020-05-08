 package com.jaredpearson.puzzlestrike.states;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.jaredpearson.puzzlestrike.Game;
 import com.jaredpearson.puzzlestrike.Player;
 import com.jaredpearson.puzzlestrike.actions.Action;
 
 public abstract class StateAdapter implements State {
 	private static final List<Action> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<Action>(0));
 	
 	/**
 	 * Gets the list of actions the specified player can perform
 	 * on the game.
 	 */
 	public List<Action> getActions(Game game, Player player) {
 		return EMPTY_LIST;
 	}
 	
 	/**
 	 * Executes the specified action within the game
 	 */
 	public void handle(Game game, Player player, Action action) {
 	}
 	
 	/**
 	 * Override this method to execute some code on entry of this state. The base
 	 * method is empty.
 	 */
 	public void onEntry(Game game) {	
 	}
 }
