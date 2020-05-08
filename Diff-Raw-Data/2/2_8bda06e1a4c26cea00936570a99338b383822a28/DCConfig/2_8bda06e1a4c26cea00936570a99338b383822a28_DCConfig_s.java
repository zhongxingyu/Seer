 package pgDev.bukkit.DisguiseCraft;
 
 import java.awt.Color;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 
 import pgDev.bukkit.DisguiseCraft.listeners.optional.*;
 
 public class DCConfig {
 	private Properties properties;
 	private final DisguiseCraft plugin;
 	public boolean upToDate = true;
 	
 	// List of Optionals
 	ConcurrentHashMap<String, Class<?>> optionals = new ConcurrentHashMap<String, Class<?>>();
 	
 	// List of Config Options
 	public boolean disguisePVP;
 	public int pvpThreads;
 	public boolean updateNotification;
 	public int biggerCube;
 	public int massiveCube;
 	public int godzillaCube;
 	public boolean quitUndisguise;
 	public boolean bandwidthReduction;
 	public boolean movementUpdateThreading;
 	public int movementUpdateFrequency;
 	public boolean nopickupDefault;
 	public int attackInterval;
 	public boolean noTabHide;
 	
 	public DCConfig(Properties p, final DisguiseCraft plugin) {
 		properties = p;
         this.plugin = plugin;
         
         // Set optional listeners
         optionals.put("playerHeldItemChange", PlayerItemHeldListener.class);
         optionals.put("playerArmSwingAnimation", PlayerAnimationListener.class);
         optionals.put("playerSneakAnimation", PlayerToggleSneakListener.class);
         optionals.put("disguiseDamageAnimation", EntityDamageListener.class);
         optionals.put("disguiseDeathAnimation", EntityDeathListener.class);
         optionals.put("itemPickupAnimation", PlayerPickupItemListener.class);
         optionals.put("disguiseArmorUpdates", InventoryClickListener.class);
         
         // Grab values here.
         for (String key : optionals.keySet()) {
         	if (!getBoolean(key, true)) {
        		optionals.put(key, null);
         	}
         }
         disguisePVP = getBoolean("disguisePVP", true);
         pvpThreads = getInt("pvpThreads", 1);
         updateNotification = getBoolean("updateNotification", true);
         biggerCube = getInt("bigger", 20);
         massiveCube = getInt("massive", 50);
         godzillaCube = getInt("godzilla", 100);
         quitUndisguise = getBoolean("quitUndisguise", true);
         bandwidthReduction = getBoolean("bandwidthReduction", false);
         movementUpdateThreading = getBoolean("movementUpdateThreading", false);
         movementUpdateFrequency = getInt("movementUpdateFrequency", 4);
         nopickupDefault = getBoolean("nopickupDefault", false);
         attackInterval = getInt("attackInterval", 2);
         noTabHide = getBoolean("noTabHide", false);
 	}
 	
 	// Value obtaining functions down below
 	public int getInt(String label, int thedefault) {
 		String value;
         try {
         	value = getString(label);
         	return Integer.parseInt(value);
         } catch (NoSuchElementException e) {
         	return thedefault;
         }
     }
     
     public double getDouble(String label) throws NoSuchElementException {
         String value = getString(label);
         return Double.parseDouble(value);
     }
     
     public File getFile(String label) throws NoSuchElementException {
         String value = getString(label);
         return new File(value);
     }
 
     public boolean getBoolean(String label, boolean thedefault) {
     	String values;
         try {
         	values = getString(label);
         	return Boolean.valueOf(values).booleanValue();
         } catch (NoSuchElementException e) {
         	return thedefault;
         }
     }
     
     public Color getColor(String label) {
         String value = getString(label);
         Color color = Color.decode(value);
         return color;
     }
     
     public HashSet<String> getSet(String label, String thedefault) {
         String values;
         try {
         	values = getString(label);
         } catch (NoSuchElementException e) {
         	values = thedefault;
         }
         String[] tokens = values.split(",");
         HashSet<String> set = new HashSet<String>();
         for (int i = 0; i < tokens.length; i++) {
             set.add(tokens[i].trim().toLowerCase());
         }
         return set;
     }
     
     public LinkedList<String> getList(String label, String thedefault) {
     	String values;
         try {
         	values = getString(label);
         } catch (NoSuchElementException e) {
         	values = thedefault;
         }
         if(!values.equals("")) {
             String[] tokens = values.split(",");
             LinkedList<String> set = new LinkedList<String>();
             for (int i = 0; i < tokens.length; i++) {
                 set.add(tokens[i].trim().toLowerCase());
             }
             return set;
         }else {
         	return new LinkedList<String>();
         }
     }
     
     public String getString(String label) throws NoSuchElementException {
         String value = properties.getProperty(label);
         if (value == null) {
         	upToDate = false;
             throw new NoSuchElementException("Config did not contain: " + label);
         }
         return value;
     }
     
     public String getString(String label, String thedefault) {
     	String value;
     	try {
         	value = getString(label);
         } catch (NoSuchElementException e) {
         	value = thedefault;
         }
         return value;
     }
     
     public String linkedListToString(LinkedList<String> list) {
     	if(list.size() > 0) {
     		String compounded = "";
     		boolean first = true;
         	for (String value : list) {
         		if (first) {
         			compounded = value;
         			first = false;
         		} else {
         			compounded = compounded + "," + value;
         		}
         	}
         	return compounded;
     	}
     	return "";
     }
     
     
     // Config creation method
     public void createConfig() {
     	try {
     		@SuppressWarnings("static-access")
 			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(plugin.pluginConfigLocation)));
     		out.write("#\r\n");
     		out.write("# DisguiseCraft Configuration\r\n");
     		out.write("#\r\n");
     		out.write("\r\n");
     		out.write("# Disguise PVP\r\n");
     		out.write("#	In order for players to be able to attack those who\r\n");
     		out.write("#	are disguised, you must have the ProtocolLib plugin\r\n");
     		out.write("#	installed on your server:\r\n");
     		out.write("#		http://dev.bukkit.org/server-mods/protocollib/\r\n");
     		out.write("disguisePVP=" + disguisePVP + "\r\n");
     		out.write("pvpThreads=" + pvpThreads + "\r\n");
     		out.write("\r\n");
     		out.write("# Optional Event Listeners\r\n");
     		out.write("#	Any of the following can be disabled to increase\r\n");
     		out.write("#	efficiency or cater to server-specific needs.\r\n");
     		out.write("playerHeldItemChange=" + (optionals.get("playerHeldItemChange") != null ) + "\r\n");
     		out.write("playerArmSwingAnimation=" + (optionals.get("playerArmSwingAnimation") != null ) + "\r\n");
     		out.write("playerSneakAnimation=" + (optionals.get("playerSneakAnimation") != null ) + "\r\n");
     		out.write("disguiseDamageAnimation=" + (optionals.get("disguiseDamageAnimation") != null ) + "\r\n");
     		out.write("disguiseDeathAnimation=" + (optionals.get("disguiseDeathAnimation") != null ) + "\r\n");
     		out.write("itemPickupAnimation=" + (optionals.get("itemPickupAnimation") != null ) + "\r\n");
     		out.write("disguiseArmorUpdates=" + (optionals.get("disguiseArmorUpdates") != null ) + "\r\n");
     		out.write("\r\n");
     		out.write("# Update Notification\r\n");
     		out.write("#	With this set to true, the plugin will check for\r\n");
     		out.write("#	and notify those who have the \"disguisecraft.update\"\r\n");
     		out.write("#	permission of plugin updates upon joining.\r\n");
     		out.write("updateNotification=" + updateNotification + "\r\n");
     		out.write("\r\n");
     		out.write("# Unnatural Slime/MagmaCube Sizes\r\n");
     		out.write("#	Here you can set the size for the extra slime\r\n");
     		out.write("#	subtypes given. You can choose any number between\r\n");
     		out.write("#	-128 and 127, but beware that negative slime\r\n");
     		out.write("#	size values will result in upside-down slimes.\r\n");
     		out.write("#	These slime properties will also apply\r\n");
     		out.write("#	to magma cubes.\r\n");
     		out.write("bigger=" + biggerCube + "\r\n");
     		out.write("massive=" + massiveCube + "\r\n");
     		out.write("godzilla=" + godzillaCube + "\r\n");
     		out.write("\r\n");
     		out.write("# Undisguise on Quit\r\n");
     		out.write("#	With this set to true, any disguised player\r\n");
     		out.write("#	who leaves the server will be undisguised.\r\n");
     		out.write("quitUndisguise=" + quitUndisguise + "\r\n");
     		out.write("\r\n");
     		out.write("# Bandwidth Reduction\r\n");
     		out.write("#	To keep disguises perfectly in-sync with\r\n");
     		out.write("#	the wearer, DisguiseCraft uses the entity\r\n");
     		out.write("#	teleportation packet instead of the \r\n");
     		out.write("#	smaller relative motion packets that\r\n");
     		out.write("#	the Vanilla MineCraft server uses.\r\n");
     		out.write("#	Set this to true if you wish to have\r\n");
     		out.write("#	disguise movements handled in the less\r\n");
     		out.write("#	bandwidth-intensive manner.\r\n");
     		out.write("bandwidthReduction=" + bandwidthReduction + "\r\n");
     		out.write("\r\n");
     		out.write("# Disguise Movement Update Threading\r\n");
     		out.write("#	Typically, DisguiseCraft uses the PlayerMoveEvent\r\n");
     		out.write("#	in order to track disguises. However, this has\r\n");
     		out.write("#	proven to cause lag on servers with large\r\n");
     		out.write("#	numbers of disguised players. Use this option\r\n");
     		out.write("#	to place updates into separate threads. You can\r\n");
     		out.write("#	also set the frequency (in ticks) that they update\r\n");
     		out.write("#	the player disguise position.\r\n");
     		out.write("movementUpdateThreading=" + movementUpdateThreading + "\r\n");
     		out.write("movementUpdateFrequency=" + movementUpdateFrequency + "\r\n");
     		out.write("\r\n");
     		out.write("# Disguise NoPickup Default\r\n");
     		out.write("#	With this option set to true, any disguises\r\n");
     		out.write("#	created will have the \"nopickup\" property\r\n");
     		out.write("#	automatically set.\r\n");
     		out.write("#	This means that disguised players are\r\n");
     		out.write("#	automatically set to not pick up items.\r\n");
     		out.write("nopickupDefault=" + nopickupDefault + "\r\n");
     		out.write("\r\n");
     		out.write("# Disguise Attack Process Interval\r\n");
     		out.write("#	To greatly improve performance, DisguiseCraft\r\n");
     		out.write("#	processes \"attacks on disguises\" in a separate\r\n");
     		out.write("#	polling thread. This is how often (in ticks)\r\n");
     		out.write("#	that this thread will process attacks.\r\n");
     		out.write("attackInterval=" + attackInterval + "\r\n");
     		out.write("\r\n");
     		out.write("# Tab List No-Hide\r\n");
     		out.write("#	If you have ProtocolLib installed on your server,\r\n");
     		out.write("#	you can prevent players from having their name\r\n");
     		out.write("#	removed from the client tab list when they disguise.\r\n");
     		out.write("noTabHide=" + noTabHide + "\r\n");
     		out.close();
     	} catch (Exception e) {
     		DisguiseCraft.logger.log(Level.SEVERE, "There was a problem while writing config to disk", e);
     	}
     }
 }
