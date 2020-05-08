 package rsmg.io;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 
 /**
  * This class reads and writes to the character progress file. The character
  * progress file stores which upgrade points that has been found, which upgrades
  * and levels that have been unlocked and so on.
  * 
  * @author Daniel Jonsson
  * 
  */
 public class CharacterProgress {
 
 	/**
 	 * Path to the configuration file.
 	 */
 	private static final String progressFilePath = "res/data/progress/progress.xml";
 	
 	/**
 	 * Make this class to a singleton.
 	 */
 	private static final CharacterProgress config = new CharacterProgress();
 
 	/**
 	 * Store all progress variables in one map.
 	 */
 	private Map<String, Integer> progress;
 	
 	private static final int TRUE = 1;
	private static final int FALSE = 0;
 	
 	/**
 	 * Make this class to a singleton. Reads the settings from the character
 	 * progress file and stores the values in this class.
 	 */
 	private CharacterProgress() {
 		SAXBuilder builder = new SAXBuilder();
 		try {
 			// The character progress document
 			Document document = builder.build(progressFilePath);
 			
 			// The root node in the document
 			Element rootNode = document.getRootElement();
 			
 			// Read and store the settings to the class
 			progress = new HashMap<String, Integer>();
 			progress.put("unlockedLevels", numberToInt(rootNode.getChildText("unlockedLevels")));
 			progress.put("upgradePoints", numberToInt(rootNode.getChildText("upgradePoints")));
 
 			Element weapon = rootNode.getChild("unlockedWeapons");
 			progress.put("rpg", booleanToInt(weapon.getChildText("rpg")));
 			progress.put("shotgun", booleanToInt(weapon.getChildText("shotgun")));
 			progress.put("assaultRifle", booleanToInt(weapon.getChildText("assaultRifle")));
 
 			Element upgrade = rootNode.getChild("unlockedUpgrades");
 			progress.put("dash", booleanToInt(upgrade.getChildText("dash")));
 			
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		} catch (JDOMException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * Get methods.
 	 */
 	public static int unlockedLevels() {
 		return variableNumber("unlockedLevels");
 	}
 	
 	public static int upgradePoints() {
 		return variableNumber("upgradePoints");
 	}
 
 	public static boolean rpgUnlocked() {
 		return variableIsTrue("rpg");
 	}
 
 	public static boolean shotgunUnlocked() {
 		return variableIsTrue("shotgun");
 	}
 
 	public static boolean assaultRifleUnlocked() {
 		return variableIsTrue("assaultRifle");
 	}
 	
 	public static boolean dashUnlocked() {
 		return variableIsTrue("dash");
 	}
 	
 	/*
 	 * Set methods.
 	 */
 	public static void setUnlockedLevels(int i) {
 		setVariable("unlockedLevels", 1);
 	}
 	
 	public static void setUpgradePoints(int i) {
 		setVariable("upgradePoints", i);
 	}
 	
 	public static void setRpgUnlocked(boolean unlocked) {
 		setVariable("rpg", unlocked);
 	}
 	
 	public static void setShotgunUnlocked(boolean unlocked) {
 		setVariable("shotgun", unlocked);
 	}
 	
 	public static void setAssaultRifleUnlocked(boolean unlocked) {
 		setVariable("assaultRifle", unlocked);
 	}
 	
 	public static void setDashUnlocked(boolean unlocked) {
 		setVariable("dash", unlocked);
 	}
 	
 	/**
 	 * Write the configurations to the hard drive.
 	 * Until this is run, changed settings are only available in the current
 	 * session, and lost when closing the game.
 	 */
 	public static void saveConfigFile() {
 		SAXBuilder builder = new SAXBuilder();
         try {
         	// Instance of the config document
         	Document document = builder.build(progressFilePath);
 
 			// The root node in the config document
 			Element rootNode = document.getRootElement();
 			
 			// Store the settings in the instance of the config document
 			rootNode.getChild("unlockedLevels").setText(String.valueOf(unlockedLevels()));
 			rootNode.getChild("upgradePoints").setText(String.valueOf(upgradePoints()));
 			
 			Element childNode = rootNode.getChild("unlockedWeapons");
 			childNode.getChild("rpg").setText(Boolean.toString(rpgUnlocked()));
 			childNode.getChild("shotgun").setText(Boolean.toString(shotgunUnlocked()));
 			childNode.getChild("assaultRifle").setText(Boolean.toString(assaultRifleUnlocked()));
 
 			childNode = rootNode.getChild("unlockedUpgrades");
 			childNode.getChild("dash").setText(Boolean.toString(dashUnlocked()));
         	
 			// Write the document to the progress file on the HDD
     		XMLOutputter outputter = new XMLOutputter();
             FileWriter writer = new FileWriter(progressFilePath);
 			outputter.output(document, writer);
 	        writer.close();
 	        
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		} catch (JDOMException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	
 	/**
 	 * Convert a boolean string to an int.
 	 * @param bol Boolean value as a string.
 	 * @return 1 if true. 0 if false.
 	 */
 	private static int booleanToInt(String bol) {
 		return (Boolean.parseBoolean(bol)) ? TRUE : FALSE;
 	}
 
 	/**
 	 * Convert a boolean value to an int.
 	 * @param bol Boolean variable.
 	 * @return 1 if true. 0 if false.
 	 */
 	private static int booleanToInt(boolean bol) {
 		return (bol) ? TRUE : FALSE;
 	}
 	
 	/**
 	 * Convert a string number to an int.
 	 * @param num Number as a string.
 	 * @return The number as an int.
 	 */
 	private static int numberToInt(String num) {
 		return Integer.parseInt(num);
 	}
 	
 	/**
 	 * Returns if the variable stored in the progression map is true.
 	 * @param variable The variable's name.
 	 * @return If the variable is true.
 	 */
 	private static boolean variableIsTrue(String variable) {
 		return config.progress.get(variable).equals(TRUE);
 	}
 
 	/**
 	 * Returns the variable's number.
 	 * @param variable The key's name.
 	 * @return The key's value.
 	 */
 	private static int variableNumber(String variable) {
 		return config.progress.get(variable);
 	}
 	
 	/**
 	 * Store a number in the progression map.
 	 * @param variable The variable name in the map.
 	 * @param value The value.
 	 */
 	private static void setVariable(String variable, int value) {
 		config.progress.put(variable, value);
 	}
 	
 	/**
 	 * Store a boolean value in the progression map.
 	 * @param variable The key in the map.
 	 * @param bol A boolean value.
 	 */
 	private static void setVariable(String variable, boolean bol) {
 		config.progress.put(variable, booleanToInt(bol));
 	}
 }
