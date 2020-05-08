 package onethatwalks.satools;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.swing.JOptionPane;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.TreeType;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * SATools - Server Admin Tools
  * 
  * @author OneThatWalks
  */
 public class SATools extends JavaPlugin {
 	// Localize needed files
 	private final SAToolsPlayerListener playerListener = new SAToolsPlayerListener(
 			this);
 	private final SAToolsEntityListener entityListener = new SAToolsEntityListener(
 			this);
 	@SuppressWarnings("unused")
 	private final SAToolsBlockListener blockListener = new SAToolsBlockListener(
 			this);
 	private PluginDescriptionFile pdfFile;
 	// Grab the logger
 	public static final Logger log = Logger.getLogger("Minecraft");
 	// Init plugin info holders
 	public static double version;
 	public static String name;
 	public static ArrayList<String> authors_RAW;
 	public static String authors = "";
 	// Init other classes and their attributes
 	SAToolsGUI gui = new SAToolsGUI(this);
 	TimeWatch tw = new TimeWatch();
 	GarbageCollection gc = new GarbageCollection();
 	private boolean twAlive = true;
 	private boolean gcAlive = true;
 	static boolean runGC = false;
 	Tasker tasker = new Tasker(this);
 	long time;
 	// Update feature data
 	private String dataFile = null;
 	URL pluginInfo;
 	// Save data
 	static ArrayList<String> godsContents = new ArrayList<String>();
 	// Actual plugin variables
 	static World world;
 	static List<Player> gods = new ArrayList<Player>();
 	static ArrayList<String> godsRemoved = new ArrayList<String>();
 
 	static enum Weather {
 		CLEAR, STORM, THUNDER
 	}
 
 	public void onDisable() {
 		// Save data
 		save();
 		// Thank the user
 		System.out.println("SATools Disabled, Thanks for using SATools!");
 		// End those pesky threads
 		SAToolsGUI.piAlive = false;
 		SAToolsGUI.pi.interrupt();
 		twAlive = false;
 		tw.interrupt();
 		gcAlive = false;
 		gc.interrupt();
 	}
 
 	public void onEnable() {
 		// Grab plugin.yml file and contents
 		pdfFile = this.getDescription();
 		// Create data files
 		if (!getDataFolder().exists()) {
 			getDataFolder().mkdirs();
 			log.info("Directory created");
 		}
 		dataFile = getDataFolder().getPath() + File.separator + "SATools.gods";
 		// Check for updates
 		checkUpdate();
 		load();
 		world = getServer().getWorld(getServer().getWorlds().get(0).getName());
 		// Event Register
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener,
 				Event.Priority.Normal, this);
 		// Enable Text to be seen in server log
 		System.out.println(pdfFile.getName() + " version "
 				+ pdfFile.getVersion() + " is enabled!");
 		// Start Threads
 		gc.start();
 		runGC = true;
 		tw.start();
 		// plugin info setting
 		version = Double.parseDouble(pdfFile.getVersion());
 		name = pdfFile.getName();
 		authors_RAW = pdfFile.getAuthors();
 		for (int i = 0; i < authors_RAW.size(); i++) { // Set up author line
 			authors = authors + authors_RAW.get(i)
 					+ (authors_RAW.size() == 1 ? "" : ", ");
 		}
 		// Gui set up and start
 		gui.setTitle(name + " v" + version
 				+ (authors_RAW.isEmpty() ? "" : " - " + authors));
 		gui.setVisible(true);
 	}
 
 	/**
 	 * WIP Auto-Update method
 	 * 
 	 * Will check if you need to update version
 	 */
 	private void checkUpdate() {
 		try {
 			pluginInfo = new URL(
 					"https://raw.github.com/OneThatWalks/SATools/master/plugin.yml");
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					pluginInfo.openStream()));
 			String strLine;
 			while ((strLine = in.readLine()) != null) {
 				if (strLine.contains("version: ")) {
 					String[] tokens = strLine.split(" ");
 					String version = tokens[1];
 					if (isDouble(version) && isDouble(pdfFile.getVersion())) {
 						if (Double.parseDouble(version) > Double
 								.parseDouble(pdfFile.getVersion())) {
 							if (JOptionPane
 									.showConfirmDialog(
 											null,
 											"There is a new update! Would you like to stop the server to download?",
 											"Update Available!",
 											JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 								getServer().dispatchCommand(
 										new ConsoleCommandSender(getServer()),
 										"stop");
 							} else {
 								log.warning("You are running an older version of SATools to prevent errors you may want to update your plugin.");
 							}
 						} else {
 							JOptionPane.showMessageDialog(null,
 									"Your version is up to date!");
 						}
 					} else {
 						log.severe("Error");
 					}
 				}
 			}
 		} catch (MalformedURLException e) {
 			log.severe("Malformed URL");
 		} catch (IOException e) {
 			log.severe("Could no connect");
 		}
 
 	}
 
 	/**
 	 * Checks for a double in the text
 	 * 
 	 * @param text
 	 *            String to check
 	 * @return true is the text contains a double, otherwise false
 	 */
 	private boolean isDouble(String text) {
 		try {
 			Double.parseDouble(text);
 		} catch (NumberFormatException e) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Saves the current data [Currently only saves gods]
 	 */
 	private void save() {
 		try {
 			log.info("Saving SATools data");
 			if (!getDataFolder().exists()) {
 				getDataFolder().mkdirs();
 				log.info("Directory created");
 			}
 			if (!new File(dataFile).exists()) {
 				new File(dataFile).createNewFile();
 				log.info("Created SATools.gods file");
 			} else {
 				log.info("File found, checking database");
 				InputStream is = new FileInputStream(dataFile);
 				BufferedReader br = new BufferedReader(
 						new InputStreamReader(is));
 				String strLine;
 				// Read File Line By Line
 				while ((strLine = br.readLine()) != null) {
 					if (!gods.contains(getServer().getPlayer(strLine))
 							&& !godsRemoved.contains(getServer().getPlayer(
 									strLine))) {
						gods.add(getServer().getPlayer(strLine));
 					}
 				}
 				is.close();
 			}
 			log.info("Saving gods");
 			FileWriter fw = new FileWriter(dataFile);
 			PrintWriter pw = new PrintWriter(fw);
 			for (int i = 0; i < gods.size(); i++) {
 				if (gods.get(i) != null) {
 					log.info("Saving: " + gods.get(i).getDisplayName());
 					pw.println(gods.get(i).getDisplayName());
 				}
 			}
 			fw.close();
 			log.info("Gods saved");
 		} catch (Exception e) {// Catch exception if any
 			e.printStackTrace();
 			log.severe("But soft, what codeth in yonder program breaks");
 		}
 	}
 
 	/**
 	 * Loads the data
 	 */
 	private void load() {
 		try {
 			if (new File(dataFile).exists()) {
 				log.info("Loading gods");
 				InputStream is = new FileInputStream(dataFile);
 				BufferedReader br = new BufferedReader(
 						new InputStreamReader(is));
 				String strLine;
 				// Read File Line By Line
 				while ((strLine = br.readLine()) != null) {
 					log.info(strLine);
 					if (!gods.contains(getServer().getPlayer(strLine))) {
 						gods.add(getServer().getPlayer(strLine));
 						godsContents.add(strLine);
 						log.info(gods.toString());
 					}
 				}
 				is.close();
 				log.info("Gods loaded");
 			} else {
 				log.info("No gods were loaded, reason file not found.");
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * setWeather
 	 * 
 	 * Sets the weather for the current world
 	 * 
 	 * @param type
 	 *            Weather to set to
 	 */
 	public void setWeather(Weather type) {
 		if (type.equals(Weather.CLEAR)) {
 			world.setStorm(false);
 			world.setThundering(false);
 		} else if (type.equals(Weather.STORM)) {
 			world.setStorm(true);
 		} else if (type.equals(Weather.THUNDER)) {
 			world.setStorm(true);
 			world.setThundering(true);
 		} else {
 			log.severe("Error setting weather!!!");
 		}
 	}
 
 	/**
 	 * spawnCreature Spawns a creature and makes sure it happens using
 	 * spawnCreature in org.bukkit.world
 	 * 
 	 * @param loc
 	 *            Location to spawn (based on input)
 	 * @param type
 	 *            type of creature to spawn
 	 */
 	public static void spawnCreature(Location loc, CreatureType type) {
 		try {
 			if (world.spawnCreature(loc, type) == null) {
 				log.severe("Failed to create " + type.getName() + " at "
 						+ loc.toString());
 			} else {
 				log.info("Spawned " + type.getName() + " at " + loc.toString());
 			}
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 		}
 	}
 
 	/**
 	 * Spawn an object at the desired location and checks if it happens
 	 * 
 	 * @param location
 	 *            Location to spawn the object
 	 * @param object
 	 *            the object to spawn
 	 */
 	public static void spawnObject(Location location, String object) {
 		try {
 			if (object.trim().equalsIgnoreCase("tree")) {
 				if (world.generateTree(
 						location,
 						TreeType.values()[random(0,
 								TreeType.values().length - 1)])) {
 					log.info("Tree created successfully");
 				} else {
 					log.severe("Failed to create tree");
 				}
 			} else if (object.trim().equalsIgnoreCase("boat")) {
 				if (world.spawnBoat(location) != null) {
 					log.info("Boat created successfully");
 				} else {
 					log.severe("Failed to create boat");
 				}
 			} else if (object.trim().equalsIgnoreCase("minecart")) {
 				if (world.spawnMinecart(location) != null) {
 					log.info("Minecart created successfully");
 				} else {
 					log.severe("Failed to create boat");
 				}
 			} else if (object.trim().equalsIgnoreCase("powered minecart")) {
 				if (world.spawnPoweredMinecart(location) != null) {
 					log.info("Powered minecart created successfully");
 				} else {
 					log.severe("Failed to create powered minecart");
 				}
 			} else if (object.trim().equalsIgnoreCase("storage minecart")) {
 				if (world.spawnStorageMinecart(location) != null) {
 					log.info("Storage minecart created successfully");
 				} else {
 					log.severe("Failed to create storage minecart");
 				}
 			} else if (object.trim().equalsIgnoreCase("lightning")) {
 				if (world.strikeLightning(location) != null) {
 					log.info("Lightning created successfully");
 				} else {
 					log.severe("Failed to create lightning");
 				}
 			} else if (object.trim().equalsIgnoreCase("light post")) {
 				if (createLightPost(location)) {
 					log.info("Lightning created successfully");
 				} else {
 					log.severe("Failed to create lightning");
 				}
 			} else {
 				log.severe("Failed to spawn object " + object);
 				world.getBlockAt(location).setType(org.bukkit.Material.TNT);
 			}
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 		}
 
 	}
 
 	private static boolean createLightPost(Location loc) {
 		try {
 			Block block_1 = world.getBlockAt(loc);
 			Block block_2 = world.getBlockAt(loc.getBlockX(),
 					loc.getBlockY() + 1, loc.getBlockZ());
 			Block block_3 = world.getBlockAt(loc.getBlockX(),
 					loc.getBlockY() + 2, loc.getBlockZ());
 			block_1.setType(Material.FENCE);
 			block_2.setType(Material.FENCE);
 			block_3.setType(Material.GLOWSTONE);
 			if (block_1.getType() == Material.FENCE
 					&& block_2.getType() == Material.FENCE
 					&& block_3.getType() == Material.GLOWSTONE) {
 				return true;
 			}
 			return false;
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 			return false;
 		}
 	}
 
 	/**
 	 * Creates a random integer between the Minimum and the Maximum including
 	 * those integers
 	 * 
 	 * @param Min
 	 *            Minimum value
 	 * @param Max
 	 *            Maximum Value
 	 * @return random int between Min and Max
 	 */
 	private static int random(int Min, int Max) {
 		return Min + (int) (Math.random() * ((Max - Min) + 1));
 	}
 
 	class TimeWatch extends Thread {
 		public void run() {
 			try {
 				while (twAlive) {
 					time = world.getTime();
 					if (time >= 12000 && time <= 12100) {
 						runGC = true;
 						Thread.sleep(3000);
 					}
 					if (time >= 0 && time <= 100) {
 						runGC = true;
 						Thread.sleep(3000);
 					}
 					Thread.sleep(2500);
 				}
 			} catch (InterruptedException e) {
 				// Sleep Interrupted
 			}
 		}
 	}
 
 	class GarbageCollection extends Thread {
 		public void run() {
 			try {
 				while (gcAlive) {
 					if (runGC) {
 						Runtime r = Runtime.getRuntime();
 						log.info("Running Garbage Collection please wait...");
 						r.gc();
 						runGC = false;
 						log.info("Garbage Collection is complete.");
 					}
 					Thread.sleep(1000);
 				}
 			} catch (InterruptedException e) {
 				// Sleep Interrupted
 			}
 		}
 	}
 
 }
