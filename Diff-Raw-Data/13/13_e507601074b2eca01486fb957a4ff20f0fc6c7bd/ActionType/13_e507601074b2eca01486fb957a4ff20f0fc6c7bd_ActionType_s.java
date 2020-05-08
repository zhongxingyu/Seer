 package edu.brown.cs32.goingrogue.gameobjects.actions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /** An enum detailing types of action
  * Prioritizes animations so that only the highest priority action appears
  * For instance, attack animations override move animations
  * 
  * @author Dominic Adams
  * @version 1.0 4/13
  */
 public enum ActionType {
 	
	MOVE, ATTACK, PICKUP;
 	
 	static List<ActionType> priorities=new ArrayList<>();
 	
 	static int getPriority(ActionType a) {
 		if(priorities.isEmpty()) {
 			priorities.add(PICKUP);
 			priorities.add(MOVE);
 			priorities.add(ATTACK);
 		}
 		return priorities.indexOf(a);
 	}
 	
 	/** Returns the priority of this type of action
 	 * Higher priority actions should be animated in the place of lower priority ones
 	 */
 	public int getPriority() {
 		return getPriority(this);
 	}
 }
