 package com.wolvencraft.prison.mines.mine;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 import org.bukkit.util.Vector;
 
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 /**
  * A virtual representation of the dynamically-updated signs that are used to display information about mines
  * @author bitWolfy
  *
  */
 @SerializableAs("DisplaySign")
 public class DisplaySign implements ConfigurationSerializable  {
 	private String id;
 	private Location loc;
 	private String parent;
 	private boolean reset;
 	private boolean paid;
 	private List<String> lines;
 	private double price;
 	
 	/**
 	 * Standard constructor for new signs
 	 * @param sign Sign object
 	 */
 	public DisplaySign(Sign sign) {
 		id = generateId();
 		loc = sign.getLocation();
 		lines = new ArrayList<String>();
 		for(int i = 0; i < sign.getLines().length; i++) {
 			String line = sign.getLine(i);
 			lines.add(line);
 		}
 		String line = lines.get(0).substring(3);
 		line = line.substring(0, line.length() - 1);
 		Message.debug(line);
 		String[] data = line.split(":");
 		String temp = "";
 		for(String part : data) temp += part + ":";
 		Message.debug(temp);
 		
 		if(data.length == 1) {
 			parent = data[0];
 			reset = false;
 			paid = false;
 			price = -1;
 		} else if(data.length == 2) {
 			parent = data[0];
 			reset = false;
 			paid = false;
 			price = -1;
 			if(data[1].equalsIgnoreCase("R")) reset = true;
 			else {
 				reset = true;
 				paid = true;
 				price = Double.parseDouble(data[1]);
 			}
 		}
 		
 		Message.debug("Created a new sign: " + parent + " | " + reset);
 		saveFile();
 	}
 	
 	/**
 	 * Constructor for mines that inherit information from their parents
 	 * @param sign Sign object
 	 * @param parentSignClass DisplaySign parent
 	 */
 	public DisplaySign(Sign sign, DisplaySign parentSignClass) {
 		id = generateId();
 		loc = sign.getLocation();
 		lines = new ArrayList<String>();
 		for(int i = 0; i < sign.getLines().length; i++) {
 			String line = sign.getLine(i);
 			lines.add(line);
 		}
 		parent = parentSignClass.getParent();
 		paid = false;
 		reset = false;
 		price = -1;
 		Message.debug("Created a new sign: " + parent + " | " + reset);
 		saveFile();
 	}
 	
     /**
      * Constructor for deserialization from a map
      * @param map Map to deserialize from
      */
 	@SuppressWarnings("unchecked")
 	public DisplaySign(Map<String, Object> me) {
 		id = (String) me.get("id");
         World world = Bukkit.getWorld((String) me.get("world"));
         loc = ((Vector) me.get("loc")).toLocation(world);
         lines = (List<String>) me.get("lines");
         parent = (String) me.get("parent");
         reset = ((Boolean) me.get("reset")).booleanValue();
         paid = ((Boolean) me.get("paid")).booleanValue();
         price = ((Double) me.get("price")).doubleValue();
         Message.debug("Loaded a sign: " + parent + " | " + reset);
 	}
 	
 	/**
 	 * Serialization method for sign data storage
 	 * @return Serialization map
 	 */
     public Map<String, Object> serialize() {
         Map<String, Object> me = new HashMap<String, Object>();
         me.put("id", id);
         me.put("loc", loc.toVector());
         me.put("world", loc.getWorld().getName());
         me.put("parent", parent);
         me.put("reset", reset);
         me.put("paid", paid);
         me.put("lines", lines);
         me.put("price", price);
         return me;
     }
 
     public String getId() 			{ return id; }
     public Location getLocation() 	{ return loc; }
     public String getParent()	 	{ return parent; }
     public boolean getReset() 		{ return reset; }
     public boolean getPaid()		{ return paid; }
     public List<String> getLines() 	{ return lines; }
     public double getPrice()		{ return price; }
     
     /**
      * Updates the DisplaySign's lines with the appropriate variables
      * @return <b>true</b> if the update was successful, <b>false</b> otherwise
      */
     public boolean update() {
 		BlockState b = loc.getBlock().getState();
 		if(b instanceof Sign) {
 			Sign signBlock = (Sign) b;
 			for(int i = 0; i < lines.size(); i++) { signBlock.setLine(i, Util.parseVars(lines.get(i), Mine.get(parent))); }
 			signBlock.update();
 			return true;
 		}
 		return false;
     }
     
     /**
      * Parses through surrounding blocks in search of children to initialize. The search is executed as follows:<br /><br />
      * <b>positive Y  negative Y<br />
      * positive X  negative X<br />
      * positive Z  negative Z</b><br /><br />
      * The initialization is executed via <b>initChild()</b>
      */
     public void initChildren() {
     	Location locNearby = loc.clone();
     	locNearby.setY(loc.getBlockY() + 1);
     	initChild(locNearby, this);
     	locNearby.setY(loc.getBlockY() - 1);
     	initChild(locNearby, this);
     	locNearby.setY(loc.getBlockY());
 
     	locNearby.setX(loc.getBlockX() + 1);
     	initChild(locNearby, this);
     	locNearby.setX(loc.getBlockX() - 1);
     	initChild(locNearby, this);
     	locNearby.setX(loc.getBlockX());
     	
     	locNearby.setZ(loc.getBlockZ() + 1);
     	initChild(locNearby, this);
     	locNearby.setZ(loc.getBlockZ() - 1);
     	initChild(locNearby, this);
     	locNearby.setZ(loc.getBlockZ());
     	return;
     }
     
     /**
      * Initializes a child sign at the specified location
      * @param location Location to check
      * @param sign Parent sign
      */
     private static void initChild(Location location, DisplaySign sign) {
     	if(!exists(location)) {
     		BlockState b = location.getBlock().getState();
     		if((b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) && (b instanceof Sign)) {
 				String data = ((Sign) b).getLine(0);
 				if(data.startsWith("<M>")) {
 					Message.debug("Registering a new DisplaySign");
 					PrisonMine.addSign(new DisplaySign((Sign) b, sign));
 				}
     		}
     	}
     }
     
 	/**
 	 * Saves the sign data to file.
 	 * @return <b>true</b> if the save was successful, <b>false</b> if an error occurred
 	 */
 	public boolean saveFile() {
		File signFile = new File(new File(PrisonMine.getInstance().getDataFolder(), "signs"), id + ".yml");
         FileConfiguration signConf =  YamlConfiguration.loadConfiguration(signFile);
        signConf.set("signclass", this);
         try {
             signConf.save(signFile);
         } catch (IOException e) {
         	Message.log(Level.SEVERE, "Unable to serialize sign '" + id + "'!");
             e.printStackTrace();
             return false;
         }
         return true;
 	}
 	
 	/**
 	 * Deletes the sign data file.<br />
 	 * <b>Warning:</b> invoking this method will not remove the sign from the list of active signs
 	 * @return <b>true</b> if the deletion was successful, <b>false</b> if an error occurred
 	 */
 	public boolean deleteFile() {
 		File signFolder = new File(PrisonMine.getInstance().getDataFolder(), "signs");
 		if(!signFolder.exists() || !signFolder.isDirectory()) return false;
 		
 		File[] signFiles = signFolder.listFiles(new FileFilter() {
            public boolean accept(File file) { return file.getName().contains(".yml"); }
         });
 		
 		for(File signFile : signFiles) {
			if(signFile.getName().equals(id+ ".yml")) {
 				PrisonMine.removeSign(this);
 				return signFile.delete();
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Creates a pseudo-random ID with a 32-bit key. ID is guaranteed to be unique
 	 * @return <b>String</b> random ID
 	 */
 	private static String generateId() {
 		boolean unique = false;
 		String id = "";
 		do {
 			id = Long.toString(Math.abs(new Random().nextLong()), 32);
 			if(!exists(id)) unique = true;
 		} while (!unique);
 		return Long.toString(Math.abs(new Random().nextLong()), 32);
 	}
 	
 	/**
 	 * Checks if a DisplaySign exists at the specified location
 	 * @param loc Location to check
 	 * @return <b>true</b> if there is an initialized DisplaySign at the location, <b>false</b> otherwise
 	 */
 	public static boolean exists(Location loc) {
 		for(DisplaySign sign : PrisonMine.getLocalSigns()) { if(sign.getLocation().equals(loc)) return true; }
 		return false;
 	}
 	
 	/**
 	 * Checks if a DisplaySign with the specified ID exists
 	 * @param id ID to check
 	 * @return <b>true</b> if there is a DisplaySign with the specified, <b>false</b> otherwise
 	 */
 	public static boolean exists(String id) {
 		for(DisplaySign sign : PrisonMine.getLocalSigns()) { if(sign.getId().equals(id)) return true; }
 		return false;
 	}
 	
 	/**
 	 * Returns the DisplaySign at the following location, if it exists
 	 * @param loc Location to check
 	 * @return <b>DisplaySign</b>, if there is one at the specified location, <b>null</b> otherwise
 	 */
 	public static DisplaySign get(Location loc) {
 		for(DisplaySign sign : PrisonMine.getLocalSigns()) { if(sign.getLocation().equals(loc)) return sign; }
 		return null;
 	}
 	
 	/**
 	 * Returns the DisplaySign associated with the specified Sign object
 	 * @param sign Sign to check
 	 * @return <b>DisplaySign</b>, if one is associated with the specified Sign, <b>null</b> otherwise
 	 */
 	public static DisplaySign get(Sign sign) { return get(sign.getLocation()); }
 	
 	/**
 	 * Returns the DisplaySign with the specified ID, if there is one
 	 * @param id ID to check
 	 * @return <b>DisplaySign</b>, if there is one with the specified ID, <b>null</b> otherwise
 	 */
 	public static DisplaySign get(String id) { 
 		for(DisplaySign sign : PrisonMine.getLocalSigns()) { if(sign.getId().equals(id)) return sign; }
 		return null;
 	}
 	
 	/**
 	 * Updates all the DisplaySigns with appropriate variables
 	 */
 	public static void updateAll() {
 		for(DisplaySign sign : PrisonMine.getLocalSigns()) {
 			sign.update();
 		}
 	}
 }
