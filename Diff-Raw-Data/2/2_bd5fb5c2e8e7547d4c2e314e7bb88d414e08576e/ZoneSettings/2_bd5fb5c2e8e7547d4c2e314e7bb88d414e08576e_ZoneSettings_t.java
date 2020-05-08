 package btwmod.protectedzones;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import btwmods.io.Settings;
 import btwmods.util.Area;
 import btwmods.util.Cube;
 
 public class ZoneSettings {
 	public final String name;
 	
 	public boolean protectBlocks = false;
 	public boolean protectEntities = false;
 	public boolean allowOps = false;
 	public boolean allowDoors = true;
 	public boolean allowContainers = false;
 	public boolean allowMooshroom = false;
 	public boolean allowVillagers = false;
 	public boolean allowBurning = false;
 	public boolean sendDebugMessages = false;
 	
 	private Set<String> allowedPlayers = new HashSet<String>();
 	
 	public static final String[] settings = {
 		"protectBlocks",
 		"protectEntities",
 		"allowBurning",
 		"allowContainers",
 		"allowDoors",
 		"allowMooshroom",
 		"allowOps",
 		"allowVillagers"
 	};
 	
 	public final boolean isCube;
 	
 	public final int dimension;
 	public final int x1;
 	public final int y1;
 	public final int z1;
 	public final int x2;
 	public final int y2;
 	public final int z2;
 	
 	public ZoneSettings(String name, int dimension, int x1, int z1, int x2, int z2) {
 		this.name = name;
 		isCube = false;
 		this.x1 = x1;
 		this.y1 = 0;
 		this.z1 = z1;
 		this.x2 = x2;
 		this.y2 = 0;
 		this.z2 = z2;
 		this.dimension = dimension;
 	}
 	
 	public ZoneSettings(String name, int dimension, int x1, int y1, int z1, int x2, int y2, int z2) {
 		this.name = name;
 		isCube = true;
 		this.x1 = x1;
 		this.y1 = y1;
 		this.z1 = z1;
 		this.x2 = x2;
 		this.y2 = y2;
 		this.z2 = z2;
 		this.dimension = dimension;
 	}
 	
 	public ZoneSettings(Settings settings) {
 		name = settings.get("name");
 		isCube = settings.getBoolean("isCube", false);
 		
 		dimension = settings.getInt("dimension", 0);
 		
 		x1 = settings.getInt("x1", 0);
 		z1 = settings.getInt("z1", 0);
 		x2 = settings.getInt("x2", x1 - 1); // Hint: -1 marks this value as invalid.
 		z2 = settings.getInt("z2", z1 - 1);
 		
 		y1 = isCube ? settings.getInt("y1", 0) : 0;
 		y2 = isCube ? settings.getInt("y2", y1 - 1) : y1 - 1;
 		
 		protectBlocks = settings.getBoolean("protectBlocks", protectBlocks);
 		protectEntities = settings.getBoolean("protectEntities", protectEntities);
 		allowOps = settings.getBoolean("allowOps", allowOps);
 		allowDoors = settings.getBoolean("allowDoors", allowDoors);
 		allowContainers = settings.getBoolean("allowContainers", allowContainers);
 		allowMooshroom = settings.getBoolean("allowMooshroom", allowMooshroom);
 		allowVillagers = settings.getBoolean("allowVillagers", allowVillagers);
 		allowBurning = settings.getBoolean("allowBurning", allowBurning);
 		
 		String players = settings.get("allowedPlayers");
 		if (players != null) {
 			this.allowedPlayers.addAll(Arrays.asList(players.toLowerCase().split(";")));
 		}
 	}
 	
 	public boolean setSetting(String name, String value) {
 		if (name.equalsIgnoreCase("protectBlocks") && Settings.isBooleanValue(value)) {
 			protectBlocks = Settings.getBooleanValue(value, protectBlocks);
 		}
 		else if (name.equalsIgnoreCase("protectEntities") && Settings.isBooleanValue(value)) {
 			protectEntities = Settings.getBooleanValue(value, protectEntities);
 		}
 		else if (name.equalsIgnoreCase("allowDoors") && Settings.isBooleanValue(value)) {
 			allowDoors = Settings.getBooleanValue(value, allowDoors);
 		}
 		else if (name.equalsIgnoreCase("allowOps") && Settings.isBooleanValue(value)) {
 			allowOps = Settings.getBooleanValue(value, allowOps);
 		}
 		else if (name.equalsIgnoreCase("allowContainers") && Settings.isBooleanValue(value)) {
 			allowContainers = Settings.getBooleanValue(value, allowContainers);
 		}
 		else if (name.equalsIgnoreCase("allowMooshroom") && Settings.isBooleanValue(value)) {
 			allowMooshroom = Settings.getBooleanValue(value, allowMooshroom);
 		}
 		else if (name.equalsIgnoreCase("allowVillagers") && Settings.isBooleanValue(value)) {
 			allowVillagers = Settings.getBooleanValue(value, allowVillagers);
 		}
 		else if (name.equalsIgnoreCase("allowBurning") && Settings.isBooleanValue(value)) {
 			allowBurning = Settings.getBooleanValue(value, allowBurning);
 		}
 		else if (name.equalsIgnoreCase("debug") && Settings.isBooleanValue(value)) {
 			sendDebugMessages = Settings.getBooleanValue(value, sendDebugMessages);
 		}
 		else {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		ArrayList<String> strings = new ArrayList<String>();
 		StringBuilder sb = new StringBuilder();
 
 		strings.add(name);
 		strings.add("protectBlocks(" + (protectBlocks ? "on" : "off") + ")");
 		strings.add("protectEntities(" + (protectEntities ? "on" : "off") + ")");
 		strings.add("allowOps(" + (allowOps ? "on" : "off") + ")");
 		strings.add("allowDoors(" + (allowDoors ? "on" : "off") + ")");
 		strings.add("allowContainers(" + (allowContainers ? "on" : "off") + ")");
 		strings.add("allowMooshroom(" + (allowMooshroom ? "on" : "off") + ")");
 		strings.add("allowVillagers(" + (allowVillagers ? "on" : "off") + ")");
 		strings.add("allowBurning(" + (allowBurning ? "on" : "off") + ")");
 		strings.add("allowedPlayers(" + allowedPlayers.toString() + ")");
 		
 		for (int i = 0; i < strings.size(); i++) {
 			if (i > 0 && i == strings.size() - 1)
 				sb.append(" and ");
 			else if (i > 0)
 				sb.append(", ");
 			
 			sb.append(strings.get(i));
 		}
 			
 		return sb.toString();
 	}
 
 	public boolean grantPlayer(String username) {
 		return allowedPlayers.add(username.trim().toLowerCase());
 	}
 	
 	public boolean revokePlayer(String username) {
 		return allowedPlayers.remove(username.trim().toLowerCase());
 	}
 	
 	public boolean isPlayerAllowed(String username) {
 		return allowedPlayers.contains(username.trim().toLowerCase());
 	}
 	
 	public boolean isValid() {
 		return isValidName(name) && x1 <= x2 && z1 <= z2 && (!isCube || y1 <= y2);
 	}
 	
 	public static boolean isValidName(String name) {
 		return name != null && name.matches("^[A-Za-z0-9_\\-]{1,25}$");
 	}
 	
 	public Area<ZoneSettings> toArea() {
 		return isCube
 			? new Cube<ZoneSettings>(x1, y1, z1, x2, y2, z2, this)
 			: new Area<ZoneSettings>(x1, z1, x2, z2, this);
 	}
 	
 	public void saveToSettings(Settings settings, String section) {
 		settings.set(section, "name", name);
 		
		settings.setInt(section, "dimension", dimension);
 		
 		settings.setInt(section, "x1", x1);
 		settings.setInt(section, "z1", z1);
 		settings.setInt(section, "x2", x2);
 		settings.setInt(section, "z2", z2);
 		
 		settings.setBoolean(section, "isCube", isCube);
 		
 		settings.setBoolean(section, "protectBlocks", protectBlocks);
 		settings.setBoolean(section, "protectEntities", protectEntities);
 		settings.setBoolean(section, "allowOps", allowOps);
 		settings.setBoolean(section, "allowDoors", allowDoors);
 		settings.setBoolean(section, "allowContainers", allowContainers);
 		settings.setBoolean(section, "allowMooshroom", allowMooshroom);
 		settings.setBoolean(section, "allowVillagers", allowVillagers);
 		settings.setBoolean(section, "allowBurning", allowBurning);
 		
 		StringBuilder sb = new StringBuilder();
 		for (String player : allowedPlayers) {
 			if (sb.length() > 0) sb.append(";");
 			sb.append(player);
 		}
 		settings.set(section, "allowedPlayers", sb.toString());
 		
 		if (isCube) {
 			settings.setInt(section, "y1", y1);
 			settings.setInt(section, "y2", y2);
 		}
 		else {
 			settings.removeKey(section, "y1");
 			settings.removeKey(section, "y2");
 		}
 	}
 
 	public List<String> settingsAsList() {
 		ArrayList<String> list = new ArrayList<String>();
 		
 		list.add("protectBlocks(" + (protectBlocks ? "on" : "off") + ")");
 		list.add("protectEntities(" + (protectEntities ? "on" : "off") + ")");
 		list.add("allowOps(" + (allowOps ? "on" : "off") + ")");
 		list.add("allowDoors(" + (allowDoors ? "on" : "off") + ")");
 		list.add("allowContainers(" + (allowContainers ? "on" : "off") + ")");
 		list.add("allowMooshroom(" + (allowMooshroom ? "on" : "off") + ")");
 		list.add("allowVillagers(" + (allowVillagers ? "on" : "off") + ")");
 		list.add("allowBurning(" + (allowBurning ? "on" : "off") + ")");
 		
 		if (sendDebugMessages)
 			list.add("debug(on)");
 		
 		return list;
 	}
 
 	public List<String> playersAsList() {
 		return Arrays.asList(allowedPlayers.toArray(new String[allowedPlayers.size()]));
 	}
 }
