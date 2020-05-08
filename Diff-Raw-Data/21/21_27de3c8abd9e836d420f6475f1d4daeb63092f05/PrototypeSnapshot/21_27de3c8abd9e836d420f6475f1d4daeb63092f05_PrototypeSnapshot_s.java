 package edu.wheaton.simulator.statistics;
 
 import java.awt.Color;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 
 import edu.wheaton.simulator.entity.AgentID;
 
 /**
  * Represents a category of agent at a particular moment in time.
  * 
  * @author Daniel Gill, Akon Ngoh
  */
 public class PrototypeSnapshot {
 	
 	/**
 	 * The name of this category of agent.
 	 */
 	public final String categoryName;
 
 	/**
 	 * The default field values for agents of this category.
 	 */
 	public final ImmutableMap<String, FieldSnapshot> defaultFields;
 
 	/**
 	 * The children of this Prototype at this point in time. 
 	 */
 	public final ImmutableSet<AgentID> children;
 	
 	/**
 	 * This Prototope's Triggers
 	 */
 	public final Set<TriggerSnapshot> triggers; 
 
 	/**
 	 * The present population of this category of Agent. 
 	 */
 	public final int population;
 	
 	/**
 	 * The color the agents are displayed
 	 */
 	public final Color color; 
 	
 	/**
 	 * A bitmap of the design for displaying the agents 
 	 */
 	public final byte[] design;
 
 	/**
 	 * Constructor. 
 	 * @param categoryName The name of this prototype. 
 	 * @param id The PrototypeID of this prototype. 
 	 * @param fields The default fields for this prototype. 
 	 * @param population The population of this prototype. 
 	 * @param poulation The number of this prototype's children. 
 	 */
 	public PrototypeSnapshot(String categoryName,
 			ImmutableMap<String, FieldSnapshot> fields, int population,
 			ImmutableSet<AgentID> children, Set<TriggerSnapshot> triggers, 
 			Color color, byte[] design) {
 		this.categoryName = categoryName; 
 		this.defaultFields = fields; 
 		this.children = children;
 		this.population = children.size(); 
 		this.triggers = triggers; 
 		this.color = color; 
 		this.design = design; 
 	}
 	
 	/**
 	 * Produce a string serializing this object
 	 * @return a String containing all of the data in this snapshot
 	 * 
 	 * Format: (Stuff in parentheses is just notes - not actually there)
 	 * -----------------------------------------------------------------
 	 * PrototypeSnapshot
 	 * Dog (categoryName)
 	 * Color (color)
 	 * Bitmask (design)
 	 * DefaultFields: FieldSnapshot Name Value
 	 * DefaultFields: FieldSnapshot Name Value
 	 * #
 	 * TODO: Add the triggers to serialization
 	 */
 	public String serialize(){
 		String s = "PrototypeSnapshot";
 		s += "\n" + categoryName; 
 		s += "\n" + color.getRed() + "~" + color.getGreen() + "~" + color.getBlue(); 
 		s += "\n" + displayByteArray(design); 
 		
 		//Serialize the defaultFields map
 		for (Entry<String, FieldSnapshot> entry : defaultFields.entrySet()) {
 			s += "\n" + entry.getValue().serialize();
 		}
 		
 		//Serialize the Triggers
 		for(TriggerSnapshot trigger : triggers){
 			s += "\n" + trigger.serialize(); 
 		}
 		
 		return s; 
 	}
 	
 	private static String displayByteArray(byte[] array){
 		String ret = ""; 
 		
 		for(byte b : array)
			ret += b; 
 		
 		return ret; 
 	}
 
 }
