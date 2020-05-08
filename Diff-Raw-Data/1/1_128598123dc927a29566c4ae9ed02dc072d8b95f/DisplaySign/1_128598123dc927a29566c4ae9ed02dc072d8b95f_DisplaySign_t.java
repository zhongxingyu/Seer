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
 
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.PrisonMine;
 import com.wolvencraft.prison.mines.util.Message;
 import com.wolvencraft.prison.mines.util.Util;
 
 @SerializableAs("DisplaySign")
 public class DisplaySign implements ConfigurationSerializable  {
 	private String id;
 	private Location loc;
 	private String parent;
 	private boolean reset;
 	private boolean paid;
 	private List<String> lines;
 	private double price;
 	
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
 		save();
 	}
 	
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
 		save();
 	}
 	
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
     
     private static void initChild(Location location, DisplaySign sign) {
     	if(!exists(location)) {
     		BlockState b = location.getBlock().getState();
     		if((b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) && (b instanceof Sign)) {
 				String data = ((Sign) b).getLine(0);
 				if(data.startsWith("<M>")) {
 					Message.debug("Registering a new DisplaySign");
 					PrisonMine.getSigns().add(new DisplaySign((Sign) b, sign));
 				}
     		}
     	}
     }
     
 	public boolean save() {
 		File signFile = new File(new File(CommandManager.getPlugin().getDataFolder(), "signs"), id + ".yml");
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
 	
 	public boolean delete() {
 		File signFolder = new File(CommandManager.getPlugin().getDataFolder(), "signs");
 		if(!signFolder.exists() || !signFolder.isDirectory()) return false;
 		
 		File[] signFiles = signFolder.listFiles(new FileFilter() {
             public boolean accept(File file) { return file.getName().contains(".yml"); }
         });
 		
 		for(File signFile : signFiles) {
 			if(signFile.getName().equals(id+ ".yml")) {
 				PrisonMine.getSigns().remove(PrisonMine.getSigns().indexOf(this));
 				return signFile.delete();
 			}
 		}
 		return false;
 	}
 	
 
 	
 	private static String generateId() { return Long.toString(Math.abs(new Random().nextLong()), 32); }
 	
 	public static boolean exists(Location loc) {
 		List<DisplaySign> signs = PrisonMine.getSigns();
 		for(DisplaySign sign : signs) { if(sign.getLocation().equals(loc)) return true; }
 		return false;
 	}
 	
 	public static DisplaySign get(Location loc) {
 		List<DisplaySign> signs = PrisonMine.getSigns();
 		for(DisplaySign sign : signs) { if(sign.getLocation().equals(loc)) return sign; }
 		return null;
 	}
 	
 	public static DisplaySign get(Sign sign) { return get(sign.getLocation()); }
 	
 	public static DisplaySign get(String id) { 
 		List<DisplaySign> signs = PrisonMine.getSigns();
 		for(DisplaySign sign : signs) { if(sign.getId().equals(id)) return sign; }
 		return null;
 	}
 	
 	public static void updateAll() {
 		for(DisplaySign sign : PrisonMine.getSigns()) {
 			if(sign.getLocation().getBlock() == null) continue;
 			BlockState b = sign.getLocation().getBlock().getState();
 			if(b instanceof Sign) {
 				Sign signBlock = (Sign) b;
 				List<String> lines = sign.getLines();
 				for(int i = 0; i < lines.size(); i++) { signBlock.setLine(i, Util.parseVars(lines.get(i), Mine.get(sign.getParent()))); }
 				signBlock.update();
 			}
 		}
 		return;
 	}
 }
