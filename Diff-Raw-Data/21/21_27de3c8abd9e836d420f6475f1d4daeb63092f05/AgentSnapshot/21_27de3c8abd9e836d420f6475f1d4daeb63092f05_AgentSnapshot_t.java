 package edu.wheaton.simulator.statistics;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Map.Entry;
 
 import com.google.common.collect.ImmutableMap;
 
 import edu.wheaton.simulator.entity.AgentID;
 
 
 /**
  * A class representing all the information to track from agents in the game.
  * 
  * @author Akon, Daniel Gill
  * 
  */
 public class AgentSnapshot {
 
 	/*
 	 * The unique id of the agent for this snapshot
 	 */
 	public final AgentID id;
 	
 	/**
 	 * The present prototype for the category of this Entity.
 	 */
 	public final String prototypeName;
 	
 	/**
 	 * The color the agents are displayed
 	 */
 	public final Color color; 
 	
 	/**
 	 * A bitmap of the design for displaying the agents 
 	 */
 	public final byte[] design;
 	
 	/**
 	 * The saved fields of this entity.
 	 */
 	public final ImmutableMap<String, FieldSnapshot> fields;
 
 	/**
 	 * The point in the simulation at which this snapshot was taken.
 	 */
 	public final Integer step;
 	
 	/**
 	 * A list of this Agent's BehaviorSnapshots
 	 */
 	public final ArrayList<TriggerSnapshot> triggers;
 	
 	/**
 	 * This Agent's xPosition
 	 */
 	public final int xpos; 
 	
 	/**
 	 * This Agent's yPosition
 	 */
 	public final int ypos; 
 
 	/**
 	 * Constructor
 	 * 
 	 * @param id
 	 *            The ID of the Agent associated with this snapshot.
 	 * @param fieldsbehaviorList.add(behaveSnap)
 	 *            The current values of the fields of the Entity
 	 * @param step
 	 *            The step in the simulation associated with this snapshot.
 	 * @param prototype
 	 *            The prototype for this category of Agent.
 	 */
 	public AgentSnapshot(AgentID id, ImmutableMap<String, FieldSnapshot> fields,
 			Integer step, String prototypeName, Color color, byte[] design, 
 			ArrayList<TriggerSnapshot> triggers, int x, int y) {
 		this.id = id;
 		this.step = step;
 		this.fields = fields;
 		this.prototypeName = prototypeName;
 		this.color = color; 
 		this.design = design; 
 		this.triggers = triggers;
 		this.xpos = x; 
 		this.ypos = y; 
 	}
 
 	/**
 	 * Produce a string serializing this object
 	 * @return a String containing all of the data in this snapshot
 	 * 
 	 * Format: (Stuff in parentheses is just notes - not actually there)
 	 * -----------------------------------------------------------------
 	 * AgentSnapshot
 	 * Dog (prototypeName - a string)
 	 * Color (color)
 	 * Bitmask (design)
 	 * 2 (xPos)
 	 * 4 (yPos)
 	 * Fields: FieldSnapshot Name Value
 	 * Fields: FieldSnapshot Name Value
 	 */
 	public String serialize(){
 		String s = "AgentSnapshot";
 		s += "\n" + prototypeName;
 		s += "\n" + color.getRed() + "~" + color.getGreen() + "~" + color.getBlue(); 
 		s += "\n" + displayByteArray(design); 
 		s += "\n" + xpos; 
 		s += "\n" + ypos; 
 		
 		for (Entry<String, FieldSnapshot> entry : fields.entrySet()) {
 			s += "\n" + entry.getValue().serialize();
 		}
 		
 		return s; 
 	}
 	
 	private static String displayByteArray(byte[] array){
 		String ret = ""; 
 		
 		for(byte b : array)
			ret += b + "~"; 
 		
 		return ret; 
 	}
 }
