 /**
  * Prototype.java
  *
  * A template for an Agent
  * 
  * @author Agent Team
  * 
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.entity;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 
 import edu.wheaton.simulator.datastructure.Grid;
 
 public class Prototype extends GridEntity {
 
 	/**
 	 * The list of all AgentIDs that follow this Prototype
 	 */
 	private List<AgentID> children;
 
 	/**
 	 * HashMap of Prototypes with associated names
 	 */
 	private static Map<String, Prototype> prototypes = new HashMap<String, Prototype>();
 
 	/**
 	 * The list of all triggers/events associated with this prototype.
 	 */
 	private List<Trigger> triggers;
 
 	private String name;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param n
 	 *            The name of this prototype
 	 */
 	public Prototype(String n) {
 		super();
 		name = n;
 		children = new ArrayList<AgentID>();
 		triggers = new ArrayList<Trigger>();
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param c
 	 *            The color of this prototype (passed to super constructor)
 	 * @param n
 	 *            The name of this prototype
 	 */
 	public Prototype(Color c, String n) {
 		super(c);
 		name = n;
 		children = new ArrayList<AgentID>();
 		triggers = new ArrayList<Trigger>();
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param c
 	 *            The color of this prototype (passed to super constructor)
 	 * @param d
 	 *            The bitmask of this prototype (passed to super constructor)
 	 * @param n
 	 *            The name of this prototype
 	 */
 	public Prototype(Color c, byte[] d, String n) {
 		super(c, d);
 		name = n;
 		triggers = new ArrayList<Trigger>();
 		children = new ArrayList<AgentID>();
 	}
 
 	/**
 	 * Adds a Prototype to the HashMap
 	 * 
 	 * @param n
 	 */
 	public static void addPrototype(Prototype p) {
 			prototypes.put(p.getName(), p);
 	}
 
 	/**
 	 * Returns the Prototype that corresponds to the given string.
 	 * 
 	 * @param n
 	 * @return
 	 */
 	public static Prototype getPrototype(String n) {
 		return prototypes.get(n);
 	}
 
 	/**
 	 * Returns and removes a Prototype from the HashMap
 	 * 
 	 * @param n
 	 *            name of the Prototype to remove
 	 * @return The deleted Prototype
 	 */
 	public static Prototype removePrototype(String n) {
 		return prototypes.remove(n);
 	}
 
 	/**
 	 * Clears all the prototypes
 	 */
 	public static void clearPrototypes() {
 		prototypes.clear();
 	}
 	
 	/**
 	 * Gets a Set of the prototype names
 	 * 
 	 * @return
 	 */
 	public static List<String> prototypeNames() {
 		List<String> keySet = Lists.newArrayList(prototypes.keySet());
 		Collections.sort(keySet);
 		return keySet;
 	}
 
 	/**
 	 * Provides all of the Prototypes in the HashMap
 	 * 
 	 * @return Immutable set of Prototypes
 	 */
 	public static ImmutableSet<Prototype> getPrototypes() {
 		ImmutableSet.Builder<Prototype> toReturn = new ImmutableSet.Builder<Prototype>();
 		for (String current : prototypes.keySet()) {
 			toReturn.add(getPrototype(current));
 		}
 		return toReturn.build();
 	}
 
 	/**
 	 * Provides all of the string trigger names that the prototype with the
 	 * given name contains.
 	 * 
 	 * @param prototypeName
 	 * @return Immutable set of trigger names
 	 */
 	public static ImmutableSet<String> getTriggerNames(String prototypeName) {
 		ImmutableSet.Builder<String> toReturn = new ImmutableSet.Builder<String>();
 		for (String currentName : prototypes.keySet()) {
 			Prototype currentPrototype = prototypes.get(currentName);
 			if (currentPrototype.getName().equals(prototypeName)) {
 				for (Trigger currentTrigger : currentPrototype.triggers)
 					toReturn.add(currentTrigger.getName());
 				return toReturn.build();
 			}
 		}
 		return toReturn.build();
 	}
 
 	/**
 	 * Changes the name of a prototype without resetting its children.
 	 * 
 	 * @param oldName
 	 * @param newName
 	 */
 	public void setPrototypeName(String oldName, String newName) {
 		Prototype p = getPrototype(oldName);
 		name = newName;
 		addPrototype(p);
 		removePrototype(oldName);
 	}
 
 	/**
 	 * Does a deep clone of this prototype and returns it as an Agent.
 	 * 
 	 * @return An Agent following this prototype
 	 */
 	public Agent createAgent(Grid grid) {
 		Agent clone = new Agent(grid, this, getColor(), getDesign());
 
 		// copy all fields
 		clone.getFieldMap().putAll(this.getFieldMap());
 
 		// copying all triggers is implicitly done in the Agent constructor, so
 		// no need here.
 
 		children.add(clone.getID());
 		return clone;
 	}
 
 	/**
 	 * Adds the given trigger to this prototype
 	 * 
 	 * @param trigger
 	 */
 	public void addTrigger(Trigger trigger) {
 		triggers.add(trigger);
 		Collections.sort(triggers);
 	}
 
 	public void replaceTrigger(String name, Trigger trigger) {
 		for (Trigger t : triggers) {
 			if (t.getName().equals(name))
 				triggers.remove(t);
 		}
 		triggers.add(trigger);
 	}
 
 	/**
 	 * Removes a trigger with the given priority
 	 * 
 	 * @param priority
 	 *            The priority of the given trigger to remove.
 	 */
 	public void removeTrigger(int priority) {
 		triggers.remove(triggers.get(priority));
 		Collections.sort(triggers);
 	}
 
 	/**
 	 * Removes a trigger with the given name from both this Prototype
 	 * 
 	 * @param name
 	 */
 	public void removeTrigger(String name) {
 		for (int i = 0; i < triggers.size(); i++)
 			if (triggers.get(i).getName().equals(name))
 				triggers.remove(i);
 		Collections.sort(triggers);
 	}
 
 	/**
 	 * Updates the trigger(s) with the given name
 	 * 
 	 * @param name
 	 */
 	public void updateTrigger(String name, Trigger newT) {
 		for (int i = 0; i < triggers.size(); i++)
 			if (triggers.get(i).getName().equals(name))
 				triggers.set(i, newT);
 		Collections.sort(triggers);
 	}
 
 	/**
 	 * Tells if this prototype has a Trigger corresponding to the name given.
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public boolean hasTrigger(String name) {
 		for (Trigger current : triggers)
 			if (current.getName().equals(name))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Returns the list of triggers for this Prototype
 	 */
 	public List<Trigger> getTriggers() {
 		return triggers;
 	}
 
 	/**
 	 * Provides a number of this children this prototype currently has.
 	 * 
 	 * @return The size of the children List
 	 */
 	public int childPopulation() {
 		return children.size();
 	}
 
 	/**
 	 * Gives the AgentID objects for all of the Agents that use this specific
 	 * prototype.
 	 * 
 	 * @return An ImmutableSet of AgentIDs.
 	 */
 	public ImmutableSet<AgentID> childIDs() {
 		ImmutableSet.Builder<AgentID> builder = new ImmutableSet.Builder<AgentID>();
 		for (AgentID current : children) {
 			builder.add(current);
 		}
 		return builder.build();
 	}
 
 	/**
 	 * Provides the name of this Prototype
 	 * 
 	 * @return
 	 */
 	public String getName() {
 		return name;
 	}
 
 }
