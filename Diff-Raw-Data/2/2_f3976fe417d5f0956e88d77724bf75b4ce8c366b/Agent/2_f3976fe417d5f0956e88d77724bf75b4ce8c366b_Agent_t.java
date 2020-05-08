 /**
  * Agent.java
  *
  * Agents model actors in the simulation's Grid.
  * 
  * @author Daniel Davenport, Grant Hensel, Elliot Penson, and Simon Swenson
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.entity;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import edu.wheaton.simulator.datastructure.Field;
 import edu.wheaton.simulator.simulation.Grid;
 
 public class Agent extends GridEntity {
 
 	private final AgentID id;
 	
 	/**
 	 * The list of all triggers/events associated with this agent.
 	 */
 	private List<Trigger> triggers;
 	
 	/*
 	 * Prototype of the agent
 	 */
 	private Prototype prototype;
 
 	/**
 	 * Constructor.
 	 * 
 	 * Makes an agent with the default gridEntity color
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 */
 	public Agent(Grid g, Prototype prototype) {
 		super(g);
 		triggers = new ArrayList<Trigger>();
 		id = new AgentID();
 		this.prototype = prototype;
 	}
 	/**
 	 * Constructor.
 	 * Makes an agent with a solid color
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 * @param c
 	 *            The color of this agent (passed to super constructor)
 	 */
 	public Agent(Grid g, Prototype prototype, Color c) {
 		super(g, c);
 		triggers = new ArrayList<Trigger>();
 		id = new AgentID();
 		this.prototype = prototype;
 	}
 	/**
 	 * Constructor.
 	 * Makes an agent with custom color and color map
 	 * @param g
 	 *            The grid (passed to super constructor)
 	 * @param c
 	 *            The color of this agent (passed to super constructor)
 	 * @param d
 	 *            The design for this agent (passed to super constructor)
 	 */
 	public Agent(Grid g, Prototype prototype, Color c, byte[] d) {
 		super(g, c, d);
 		triggers = new ArrayList<Trigger>();
 		id = new AgentID();
 		this.prototype = prototype;
 	}
 
 	/**
 	 * Causes this Agent to perform 1 action. The first trigger with valid
 	 * conditions will fire.
 	 *
 	 * @throws Exception
 	 */
 	public void act(GridEntity local, GridEntity global) {
 		try {
 			for (Trigger t : triggers)
				t.evaluate(this);
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 	}
 
 	/**
 	 * Removes this Agent from the environment's list.
 	 */
 	public void die() {
 		getGrid().removeAgent(this);
 	}
 
 	/**
 	 * Adds to the Agent's list of triggers
 	 * 
 	 * @param trigger
 	 *            The trigger to add
 	 */
 	public void addTrigger(Trigger trigger) {
 		triggers.add(trigger);
 		Collections.sort(triggers);
 	}
 
 	/**
 	 * Removes a trigger with the given priority (index in array list)
 	 * 
 	 * @param priority
 	 *            The priority of the given trigger to remove.
 	 */
 	public void removeTrigger(int priority) {
 		triggers.remove(triggers.get(priority));
 		Collections.sort(triggers);
 	}
 
 	/**
 	 * Gets the current x position of this agent
 	 * 
 	 * @return x
 	 */
 	public int getPosX() {
 		return getField("x").getIntValue();
 	}
 
 	/**
 	 * Gets the current y position of this agent
 	 * 
 	 * @return y
 	 */
 	public int getPosY() {
 		return getField("y").getIntValue();
 	}
 
 	/**
 	 * Sets the Agent's new position
 	 * 
 	 * @param x
 	 * @param y
 	 */
 	public void setPos(int x, int y) {
 		updateField("x", x);
 		updateField("y", y);
 	}
 	
 	public AgentID getAgentID(){
 		return id;
 	}
 	public Prototype getPrototype(){
 		return prototype;
 	}
 }
