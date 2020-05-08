 package rsmg.io;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 /**
  * This class provides getters and setters to read and modify the character
  * progression.
  * 
  * The character progression is stored on the hard drive in the folder
  * res/data/progress. If the file progress.xml and/or the folder don't exist,
  * they are created and filled with all necessary XML nodes. The character
  * progress file stores which upgrade points that have been found, which
  * upgrades, weapons and levels that have been unlocked.
  * 
  * @author Daniel Jonsson
  * 
  */
 public class CharacterProgress {
 	
 	private static final String UNLOCKED_LEVELS = "unlockedLevels";
 	
 	private static final String UNLOCKED_WEAPONS = "unlockedWeapons";
 	private static final String PISTOL = "pistol";
 	private static final String SHOTGUN = "shotgun";
 	private static final String RPG = "rpg";
 	
 	private static final String UPGRADE_POINTS = "upgradePoints";
 	
 	private static final String UNLOCKED_UPGRADES = "unlockedUpgrades";
 	public static final String DASH = "dash";
 	public static final String DOUBLE_JUMP = "doubleJump";
 	public static final String RAPID_FIRE = "rapidFire";
 	public static final String INC_RUNNING_SPEED = "incRunningSpeed";
 	public static final String INC_SHOTGUN_SPREAD = "incShotgunSpread";
 	public static final String INC_RPG_AOE = "incRPGAoE";
 	
 	private static final int TRUE = 1;
 	private static final int FALSE = 0;
 	
 	/**
 	 * Path to the configuration file.
 	 */
 	private static final String progressFolderPath = "res/data/progress";
 	private static final String progressFilePath = progressFolderPath+"/progress.xml";
 	
 	/**
 	 * Make this class to a singleton.
 	 */
 	private static final CharacterProgress config = new CharacterProgress();
 
 	/**
 	 * Store all progress variables in one map.
 	 */
 	private Map<String, Integer> progress;
 	
 	/**
 	 * Make this class to a singleton. Reads the settings from the character
 	 * progress file and stores the values in this class.
 	 */
 	private CharacterProgress() {
 		if (!doesProgressFolderExist()) {
 			createProgressFolder();
 			createProgressFile();
 		} else if (!doesProgressFileExist()) {
 			createProgressFile();
 		}
 		progress = new HashMap<String, Integer>();
 		fillProgressMap(this);
 	}
 
 	/**
 	 * Fills the progress map in this singleton class with all relevant XML
 	 * nodes from the hard drive. By doing this we won't need to read from the
 	 * hard drive every time a get method is called.
 	 * 
 	 * @param instance
 	 *            What instance of CharacterProgress that the operations should
 	 *            be done on.
 	 */
 	private static void fillProgressMap(CharacterProgress instance) {
 		try {
 			// Clear the map
 			instance.progress.clear();
 			
 			// The character progress document
 			Document document = new SAXBuilder().build(progressFilePath);
 			
 			// The root node in the document
 			Element rootNode = document.getRootElement();
 			
 			// Read and store the settings to the class
 			instance.progress.put(UNLOCKED_LEVELS, numberToInt(rootNode.getChildText(UNLOCKED_LEVELS)));
 			instance.progress.put(UPGRADE_POINTS, numberToInt(rootNode.getChildText(UPGRADE_POINTS)));
 
 			Element weapon = rootNode.getChild(UNLOCKED_WEAPONS);
 			instance.progress.put(PISTOL, booleanToInt(weapon.getChildText(PISTOL)));
 			instance.progress.put(SHOTGUN, booleanToInt(weapon.getChildText(SHOTGUN)));
 			instance.progress.put(RPG, booleanToInt(weapon.getChildText(RPG)));
 
 			Element upgrade = rootNode.getChild(UNLOCKED_UPGRADES);
 			instance.progress.put(DASH, booleanToInt(upgrade.getChildText(DASH)));
 			instance.progress.put(DOUBLE_JUMP, booleanToInt(upgrade.getChildText(DOUBLE_JUMP)));
 			instance.progress.put(RAPID_FIRE, booleanToInt(upgrade.getChildText(RAPID_FIRE)));
 			instance.progress.put(INC_RUNNING_SPEED, booleanToInt(upgrade.getChildText(INC_RUNNING_SPEED)));
 			instance.progress.put(INC_SHOTGUN_SPREAD, booleanToInt(upgrade.getChildText(INC_SHOTGUN_SPREAD)));
 			instance.progress.put(INC_RPG_AOE, booleanToInt(upgrade.getChildText(INC_RPG_AOE)));
 			
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		} catch (JDOMException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This method will reset all character progress.
 	 */
 	public static void resetProgress() {
 		if (doesProgressFileExist()) {
 			deleteProgressFile();
 		}
 		createProgressFile();
 		fillProgressMap(config);
 	}
 	
 	/**
 	 * Check if the progress folder exists.
 	 * 
 	 * @return Returns true if the progress file exists.
 	 */
 	private static boolean doesProgressFolderExist() {
 		File file = new File(progressFolderPath);
 		return file.exists();
 	}
 	
 	/**
 	 * Create the progress folder.
 	 */
 	private static void createProgressFolder() {
 		File file = new File(progressFolderPath);
 		file.mkdir();
 	}
 	
 	/**
 	 * Check if the progress file exists.
 	 * 
 	 * @return Returns true if the progress file exists.
 	 */
 	private static boolean doesProgressFileExist() {
 		File file = new File(progressFilePath);
 		return file.exists();
 	}
 	
 	/**
 	 * Delete the progress file.
 	 */
 	private static void deleteProgressFile() {
 		File file = new File(progressFilePath);
 		file.delete();
 	}
 	
 	/**
 	 * Create a new progress file and fill it with all necessary XML code.
 	 */
 	private static void createProgressFile() {
 		try {
 			/*
 			 * Create the character progress document.
 			 */
 			// unlocked levels
 			Element unlockedLevels = new Element(UNLOCKED_LEVELS).setText("1");
 			
 			// unlocked weapons
 			Element unlockedWeapons = new Element(UNLOCKED_WEAPONS);
 			unlockedWeapons.addContent(new Element(PISTOL).setText("false"));
 			unlockedWeapons.addContent(new Element(RPG).setText("false"));
 			unlockedWeapons.addContent(new Element(SHOTGUN).setText("false"));
 			
 			// upgrade points
 			Element upgradePoints = new Element(UPGRADE_POINTS).setText("0");
 			
 			// upgrades
 			Element unlockedUpgrades = new Element(UNLOCKED_UPGRADES);
 			unlockedUpgrades.addContent(new Element(DASH).setText("false"));
 			unlockedUpgrades.addContent(new Element(DOUBLE_JUMP).setText("false"));
 			unlockedUpgrades.addContent(new Element(RAPID_FIRE).setText("false"));
 			unlockedUpgrades.addContent(new Element(INC_RUNNING_SPEED).setText("false"));
 			unlockedUpgrades.addContent(new Element(INC_SHOTGUN_SPREAD).setText("false"));
 			unlockedUpgrades.addContent(new Element(INC_RPG_AOE).setText("false"));
 			
 			// root
 			Element root = new Element("progress");
 			root.addContent(unlockedLevels);
 			root.addContent(unlockedWeapons);
 			root.addContent(upgradePoints);
 			root.addContent(unlockedUpgrades);
 			
 			// document
 			Document document = new Document(root);
 			
 			/*
 			 * Write the character progress document to the disk.
 			 */
 			XMLOutputter outputter = new XMLOutputter();
 			outputter.setFormat(Format.getPrettyFormat());
 			FileWriter writer = new FileWriter(progressFilePath);
 			outputter.output(document, writer);
 			writer.flush();
 			writer.close();
 			
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * Get methods.
 	 */
 	/**
 	 * Returns the number of unlocked levels.
 	 * @return Number of unlocked levels.
 	 */
 	public static int getUnlockedLevels() {
 		return variableNumber(UNLOCKED_LEVELS);
 	}
 	
 	/**
 	 * Returns the number of available upgrade points.
 	 * 
 	 * @return Number of available upgrade points.
 	 */
 	public static int getUpgradePoints() {
 		return variableNumber(UPGRADE_POINTS);
 	}
 
 	// weapons
 	public static boolean isPistolUnlocked() {
		return variableIsTrue(PISTOL);
 	}
 	
 	public static boolean isRPGUnlocked() {
 		return variableIsTrue(RPG);
 	}
 
 	public static boolean isShotgunUnlocked() {
 		return variableIsTrue(SHOTGUN);
 	}
 	
 	// upgrades
 	/**
 	 * Returns if the chosen upgrade is unlocked. As upgrade parameter, use any
 	 * of the upgrade constants in this class.
 	 * 
 	 * @param upgrade
 	 *            An upgrade constant available from this class.
 	 * @return If the upgrade is unlocked.
 	 */
 	public static boolean isUpgradeUnlocked(String upgrade) {
 		switch (upgrade) {
 		case DASH:
 			return isDashUnlocked();
 		case DOUBLE_JUMP:
 			return isDoubleJumpUnlocked();
 		case RAPID_FIRE:
 			return isRapidFireUnlocked();
 		case INC_RUNNING_SPEED:
 			return isIncRunningSpeedUnlocked();
 		case INC_SHOTGUN_SPREAD:
 			return isIncShotgunSpreadUnlocked();
 		case INC_RPG_AOE:
 			return isIncRPGAoEUnlocked();
 		default:
 			new IllegalArgumentException();
 			return false;
 		}
 	}
 	
 	public static boolean isDashUnlocked() {
 		return variableIsTrue(DASH);
 	}
 	
 	public static boolean isDoubleJumpUnlocked() {
 		return variableIsTrue(DOUBLE_JUMP);
 	}
 	
 	public static boolean isRapidFireUnlocked() {
 		return variableIsTrue(RAPID_FIRE);
 	}
 	
 	public static boolean isIncRunningSpeedUnlocked() {
 		return variableIsTrue(INC_RUNNING_SPEED);
 	}
 	
 	public static boolean isIncShotgunSpreadUnlocked() {
 		return variableIsTrue(INC_SHOTGUN_SPREAD);
 	}
 	
 	public static boolean isIncRPGAoEUnlocked() {
 		return variableIsTrue(INC_RPG_AOE);
 	}
 	
 	/*
 	 * Set methods.
 	 */
 	/**
 	 * Set the number of unlocked levels.
 	 * @param i Number of unlocked levels.
 	 */
 	public static void setUnlockedLevels(int i) {
 		setVariable(UNLOCKED_LEVELS, i);
 	}
 	
 	/**
 	 * Set the number of available upgrade points.
 	 * @param i Number of available upgrade points.
 	 */
 	public static void setUpgradePoints(int i) {
 		setVariable(UPGRADE_POINTS, i);
 	}
 	
 	// weapons
 	public static void setPistolUnlocked(boolean unlocked) {
 		setVariable(PISTOL, unlocked);
 	}
 	
 	public static void setRpgUnlocked(boolean unlocked) {
 		setVariable(RPG, unlocked);
 	}
 	
 	public static void setShotgunUnlocked(boolean unlocked) {
 		setVariable(SHOTGUN, unlocked);
 	}
 	
 	// upgrades
 	/**
 	 * Set if the chosen upgrade should be unlocked or not. As upgrade
 	 * parameter, use any of the upgrade constants in this class.
 	 * 
 	 * @param upgrade
 	 *            An upgrade constant available from this class.
 	 * @param upgrade
 	 *            unlocked If the upgrade should be unlocked.
 	 */
 	public static void setUpgrade(String upgrade, boolean unlocked) {
 		switch (upgrade) {
 		case DASH:
 			setDashUnlocked(unlocked);
 			break;
 		case DOUBLE_JUMP:
 			setDoubleJumpUnlocked(unlocked);
 			break;
 		case RAPID_FIRE:
 			setRapidFireUnlocked(unlocked);
 			break;
 		case INC_RUNNING_SPEED:
 			setIncRunningSpeedUnlocked(unlocked);
 			break;
 		case INC_SHOTGUN_SPREAD:
 			setIncShotgunSpreadUnlocked(unlocked);
 			break;
 		case INC_RPG_AOE:
 			setIncRPGAoEUnlocked(unlocked);
 			break;
 		default:
 			new IllegalArgumentException();
 			break;
 		}
 	}
 	
 	public static void setDashUnlocked(boolean unlocked) {
 		setVariable(DASH, unlocked);
 	}
 	
 	public static void setDoubleJumpUnlocked(boolean unlocked) {
 		setVariable(DOUBLE_JUMP, unlocked);
 	}
 	
 	public static void setRapidFireUnlocked(boolean unlocked) {
 		setVariable(RAPID_FIRE, unlocked);
 	}
 	
 	public static void setIncRunningSpeedUnlocked(boolean unlocked) {
 		setVariable(INC_RUNNING_SPEED, unlocked);
 	}
 	
 	public static void setIncShotgunSpreadUnlocked(boolean unlocked) {
 		setVariable(INC_SHOTGUN_SPREAD, unlocked);
 	}
 	
 	public static void setIncRPGAoEUnlocked(boolean unlocked) {
 		setVariable(INC_RPG_AOE, unlocked);
 	}
 	
 	/**
 	 * Write the configurations to the hard drive.
 	 * Until this is run, changed settings are only available in the current
 	 * session, and lost when closing the game.
 	 */
 	public static void saveFile() {
         try {
         	// Instance of the config document
         	Document document = new SAXBuilder().build(progressFilePath);
 
 			// The root node in the config document
 			Element rootNode = document.getRootElement();
 			
 			// Store the settings in the instance of the config document
 			rootNode.getChild(UNLOCKED_LEVELS).setText(String.valueOf(getUnlockedLevels()));
 			rootNode.getChild(UPGRADE_POINTS).setText(String.valueOf(getUpgradePoints()));
 			
 			// weapons
 			Element childNode = rootNode.getChild(UNLOCKED_WEAPONS);
 			childNode.getChild(PISTOL).setText(Boolean.toString(isPistolUnlocked()));
 			childNode.getChild(RPG).setText(Boolean.toString(isRPGUnlocked()));
 			childNode.getChild(SHOTGUN).setText(Boolean.toString(isShotgunUnlocked()));
 
 			// upgrades
 			childNode = rootNode.getChild(UNLOCKED_UPGRADES);
 			childNode.getChild(DASH).setText(Boolean.toString(isDashUnlocked()));
 			childNode.getChild(DOUBLE_JUMP).setText(Boolean.toString(isDoubleJumpUnlocked()));
 			childNode.getChild(RAPID_FIRE).setText(Boolean.toString(isRapidFireUnlocked()));
 			childNode.getChild(INC_RUNNING_SPEED).setText(Boolean.toString(isIncRunningSpeedUnlocked()));
 			childNode.getChild(INC_SHOTGUN_SPREAD).setText(Boolean.toString(isIncShotgunSpreadUnlocked()));
 			childNode.getChild(INC_RPG_AOE).setText(Boolean.toString(isIncRPGAoEUnlocked()));
         	
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
 	 * 
 	 * @param bol
 	 *            Boolean value as a string.
 	 * @return 1 if true. 0 if false.
 	 */
 	private static int booleanToInt(String bol) {
 		return (Boolean.parseBoolean(bol)) ? TRUE : FALSE;
 	}
 
 	/**
 	 * Convert a boolean value to an int.
 	 * 
 	 * @param bol
 	 *            Boolean variable.
 	 * @return 1 if true. 0 if false.
 	 */
 	private static int booleanToInt(boolean bol) {
 		return (bol) ? TRUE : FALSE;
 	}
 	
 	/**
 	 * Convert a string number to an int.
 	 * 
 	 * @param num
 	 *            Number as a string.
 	 * @return The number as an int.
 	 */
 	private static int numberToInt(String num) {
 		return Integer.parseInt(num);
 	}
 	
 	/**
 	 * Returns if the variable stored in the progression map is true.
 	 * 
 	 * @param variable
 	 *            The variable's name.
 	 * @return If the variable is true.
 	 */
 	private static boolean variableIsTrue(String variable) {
 		return config.progress.get(variable).equals(TRUE);
 	}
 
 	/**
 	 * Returns the variable's number.
 	 * 
 	 * @param variable
 	 *            The key's name.
 	 * @return The key's value.
 	 */
 	private static int variableNumber(String variable) {
 		return config.progress.get(variable);
 	}
 	
 	/**
 	 * Store a number in the progression map.
 	 * 
 	 * @param variable
 	 *            The variable name in the map.
 	 * @param value
 	 *            The value.
 	 */
 	private static void setVariable(String variable, int value) {
 		config.progress.put(variable, value);
 	}
 	
 	/**
 	 * Store a boolean value in the progression map.
 	 * 
 	 * @param variable
 	 *            The key in the map.
 	 * @param bol
 	 *            A boolean value.
 	 */
 	private static void setVariable(String variable, boolean bol) {
 		config.progress.put(variable, booleanToInt(bol));
 	}
 }
