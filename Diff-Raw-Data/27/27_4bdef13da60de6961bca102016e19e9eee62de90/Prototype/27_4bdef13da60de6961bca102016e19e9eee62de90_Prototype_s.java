 /**
  * Prototype.java
  *
  * A template for an Agent
  * 
  * @author Elliot Penson
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.entity;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
import edu.wheaton.simulator.datastructure.Field;
 import edu.wheaton.simulator.simulation.Grid;
 
 public class Prototype extends GridEntity {
 
 	/**
 	 * The list of all Agent children of this Prototype
 	 */
 	private List<Agent> children;
 
 	/**
 	 * The list of all triggers/events associated with this prototype.
 	 */
 	private List<Trigger> triggers;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 * @param c
 	 *            The color of this prototype (passed to super constructor)
 	 */
 	public Prototype(Grid g, Color c) {
 		super(g, c);
 		children = new ArrayList<Agent>();
 		triggers = new ArrayList<Trigger>();
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 * @param c
 	 *            The color of this prototype (passed to super constructor)
 	 * @param d
 	 * 			  The bitmask of this prototype (passed to super constructor)
 	 */
 	public Prototype(Grid g, Color c, byte[] d) {
 		super(g, c, d);
 		triggers = new ArrayList<Trigger>();
 		children = new ArrayList<Agent>();
 	}
 
 	/**
 	 * Does a deep clone of this prototype and returns it as an Agent.
 	 * @return
 	 * 			  The clone of this prototype
 	 */
 	public Agent clonePrototype() {
 		Agent toReturn = new Agent(getGrid(), getColor(), getDesign());
 		
 		// copy all fields
 		toReturn.getFieldMap().putAll(this.getFieldMap());
 
 		for (Trigger t : triggers) {
 			toReturn.addTrigger(new Trigger(t)); // copy all triggers
 		}
 		
 		return toReturn;
 	}
 
 	public void addTrigger(Trigger trigger) {
 		triggers.add(trigger);
 		Collections.sort(triggers);
 		for (Agent a : children) {
 			a.addTrigger(trigger);
 		}
 	}
 
 	/**
 	 * Removes a trigger with the given priority all children.
 	 * 
 	 * @param priority
 	 *            The priority of the given trigger to remove.
 	 */
 	public void removeTrigger(int priority) {
 		triggers.remove(triggers.get(priority));
 		Collections.sort(triggers);
 		for (Agent a : children) {
 			a.removeTrigger(priority);
 		}
 	}
 
 }
