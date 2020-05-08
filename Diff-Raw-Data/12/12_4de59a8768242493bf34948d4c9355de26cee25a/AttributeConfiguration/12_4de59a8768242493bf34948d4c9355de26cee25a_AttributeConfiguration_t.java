 package org.andfRa.mythr.config;
 
 import java.io.IOException;
 
 import org.andfRa.mythr.MythrLogger;
 import org.andfRa.mythr.inout.Directory;
 import org.andfRa.mythr.inout.FileIO;
 import org.andfRa.mythr.player.Attribute;
 import org.andfRa.mythr.util.LinearFunction;
 import org.bukkit.craftbukkit.libs.com.google.gson.JsonParseException;
 
 public class AttributeConfiguration {
 	
 	/** Instance of the configuration. */
 	private static AttributeConfiguration config;
 	
 	
 	/** Number of available attribute points based on level. */
 	private LinearFunction attribPoints;
 	
 	/** Range for which the cost remains constant. */
	private Integer constCostRange;
 	
 	/** Attributes. */
 	private Attribute[] attributes;
 	
 
 	// CONSTRUCTION:
 	/** Fixes all missing fields. */
 	public void complete()
 	 {
 		if(attribPoints == null){
 			MythrLogger.nullField(getClass(), "attribPoints");
 			attribPoints = new LinearFunction(0.0);
 		}
 		attribPoints.complete();
 		
		if(constCostRange == null){
			MythrLogger.nullField(getClass(), "constCostRange");
			constCostRange = 6;
 		}
 		
 		if(attributes == null){
 			MythrLogger.nullField(getClass(), "attributes");
 			attributes = new Attribute[0];
 		}
 
 	 }
 	
 	
 	// VALUES:
 	/**
 	 * Gets the number of available attribute points.
 	 * 
 	 * @param level player level
 	 * @return attribute points
 	 */
 	public static int getAttribPoints(Integer level) {
 		return config.attribPoints.yIntFloor(level);
 	}
 	
 	/**
 	 * Gets the range at which the attribute point cost remains constant.
 	 * 
 	 * @return constant cost range
 	 */
 	public static Integer getCostCostRange() {
		return config.constCostRange;
 	}
 
 	/**
 	 * Calculates attribute point cost.
 	 * 
 	 * @param score score.
 	 * @return point cost
 	 */
 	public static int calcCost(int score)
 	 {
 
 		int total = 0;
 		
 		int con = getCostCostRange();
 		int max = con;
 		int cost = 1;
 		
 		for (int s = 1; s <= score; s++) {
 			
 			total+= cost;
 			
 			if(s == max){
 				max+= con;
 				cost++;
 			}
 			
 		}
 		
 		return total;
 	 }
 
 	/**
 	 * Calculates next increase attribute point cost.
 	 * 
 	 * @param score score.
 	 * @return point cost
 	 */
 	public static int calcNextCost(int score)
 	 {
 		return calcCost(score + 1) - calcCost(score);
 	 }
 
 	/**
 	 * Calculates attribute score.
 	 * 
 	 * @param points points to spend
 	 * @return attribute score
 	 */
 	public static int calcScore(int points)
 	 {
 		int score = 0;
 		
 		while(points > 0){
 			int cost = calcCost(score);
 			if(cost > points) break;
 			points-= cost;
 			score+= 1;
 		}
 		
 		return score;
 	 }
 	
 	/**
 	 * Gets attributes.
 	 * 
 	 * @return attributes
 	 */
 	public static Attribute[] getAttributes()
 	 { return config.attributes; }
 
 	/**
 	 * Get an attribute.
 	 * 
 	 * @param name attribute name
 	 * @return attribute, null if none
 	 */
 	public static Attribute getAttribute(String name)
 	 {
 		for (int i = 0; i < config.attributes.length; i++) {
 			if(config.attributes[i].getName().equals(name)) return config.attributes[i];
 		}
 		return null;
 	 }
 
 	/**
 	 * Matches attribute abbreviation.
 	 * 
 	 * @param name attribute name
 	 * @return attribute abbreviation, null if not found
 	 */
 	public static String matchAbbreviation(String name)
 	 {
 		for (int i = 0; i < config.attributes.length; i++) {
 			if(config.attributes[i].getName().equals(name)) return config.attributes[i].getAbbrev();
 		}
 		return null;
 	 }
 
 	
 	/**
 	 * Gets the attribute count.
 	 * 
 	 * @return attribute count
 	 */
 	public static int getAttribCount()
 	 {
 		return config.attributes.length;
 	 }
 	
 	/**
 	 * Gets attribute names.
 	 * 
 	 * @return attribute names
 	 */
 	public static String[] getAttrNames()
 	 {
 		String[] result = new String[config.attributes.length];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = config.attributes[i].getName();
 		}
 		return result;
 	 }
 	
 	/**
 	 * Gets attribute abbreviations.
 	 * 
 	 * @return attribute abbreviations
 	 */
 	public static String[] getAttrAbbreviations()
 	 {
 		String[] result = new String[config.attributes.length];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = config.attributes[i].getAbbrev();
 		}
 		return result;
 	 }
 	
 	
 	/**
 	 * Checks if the attribute exists.
 	 * 
 	 * @param name attribute name
 	 * @return true if exists
 	 */
 	public static boolean checkAttribute(String name)
 	 {
 		for (int i = 0; i < config.attributes.length; i++) {
 			if(config.attributes[i].getName().equalsIgnoreCase(name)) return true;
 		}
 		return false;
 	 }
 	
 	/**
 	 * Gets the attribute index.
 	 * 
 	 * @param name attribute name
 	 * @return attribute index, -1 if none
 	 */
 	public static int getAttribIndex(String name)
 	 {
 		for (int i = 0; i < config.attributes.length; i++) {
 			if(config.attributes[i].getName().equals(name)) return i;
 		}
 		
 		return -1;
 	 }
 	
 	
 	// LOAD UNLOAD:
 	/** Loads the configuration. */
 	public static void load(){
 
 
 		// Create config:
 		if(!FileIO.exists(Directory.ATTRIBUTE_CONFIG)){
 
 			try {
 				FileIO.unpackConfig(Directory.ATTRIBUTE_CONFIG);
 			}
 			catch (IOException e) {
 				MythrLogger.severe(AttributeConfiguration.class, "Failed to unpack default configuration.");
 				MythrLogger.severe(" " + e.getClass().getSimpleName() + ":" + e.getMessage());
 			}
 			
 		}
 		
 		// Read config:
 		AttributeConfiguration config;
 		try {
 			
 			config = FileIO.readConfig(Directory.ATTRIBUTE_CONFIG, AttributeConfiguration.class);
 			
 		} catch (IOException e) {
 			
 			MythrLogger.severe(AttributeConfiguration.class, "Failed to read configuration.");
 			MythrLogger.severe(" " + e.getClass().getSimpleName() + ":" + e.getMessage());
 			config = new AttributeConfiguration();
 			
 		} catch (JsonParseException e) {
 
 			MythrLogger.severe(AttributeConfiguration.class, "Failed to parse configuration.");
 			MythrLogger.severe(" " + e.getClass().getSimpleName() + ":" + e.getMessage());
 			config = new AttributeConfiguration();
 			
 		}
 		
 		// Set instance:
 		AttributeConfiguration.config = config;
 		
 		// Complete:
 		config.complete();
 		
 	}
 	
 	/** Unloads the configuration. */
 	public static void unload()
 	 {
 		AttributeConfiguration.config = null;
 	 }
 	
 }
