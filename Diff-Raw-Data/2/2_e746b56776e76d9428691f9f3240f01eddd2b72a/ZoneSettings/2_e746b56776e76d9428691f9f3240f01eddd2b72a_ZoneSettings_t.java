 package btwmod.protectedzones;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import btwmods.Util;
 import btwmods.io.Settings;
 import btwmods.util.Area;
 import btwmods.util.Cube;
 
 public class ZoneSettings {
 	
 	public enum PERMISSION { ON, WHITELIST, OFF };
 	
 	public final String name;
 	public final int dimension;
 	private final List<Area<ZoneSettings>> _areas = new ArrayList<Area<ZoneSettings>>();
 	public final List<Area<ZoneSettings>> areas = Collections.unmodifiableList(_areas);
 	
 	public PERMISSION protectEdits = PERMISSION.OFF;
 	public PERMISSION allowDoors = PERMISSION.ON;
 	public PERMISSION allowContainers = PERMISSION.OFF;
 	public boolean allowOps = false;
 	
 	public PERMISSION protectEntities = PERMISSION.OFF;
 	public boolean allowMooshroom = false;
 	public boolean allowVillagers = false;
 	
 	public boolean protectExplosions = false;
 	public boolean protectBurning = false;
 	
 	public boolean sendDebugMessages = false;
 	
 	private final Set<String> whitelist = new HashSet<String>();
 	
 	private ProtectedZones protectedZones = null;
 	
 	public static final String[] settings = {
 		"protectEdits",
 		"allowDoors",
 		"allowContainers",
 		"allowOps",
 		
 		"protectEntities",
 		"allowMooshroom",
 		"allowVillagers",
 		
 		"protectExplosions",
 		"protectBurning"
 	};
 	
 	public ZoneSettings(String name, int dimension) throws IllegalArgumentException {
 		if (!isValidName(name))
 			throw new IllegalArgumentException("name");
 		
 		if (Util.getWorldNameFromDimension(dimension) == null)
 			throw new IllegalArgumentException("dimension");
 		
 		this.name = name;
 		this.dimension = dimension;
 	}
 	
 	public ZoneSettings(Settings settings) throws IllegalArgumentException {
 		name = settings.get("name");
 		dimension = settings.getInt("dimension", 0);
 		
 		if (!isValidName(name))
 			throw new IllegalArgumentException("name");
 		
 		if (Util.getWorldNameFromDimension(dimension) == null)
 			throw new IllegalArgumentException("dimension");
 		
 		if (settings.isBoolean("isCube")) {
 			boolean isCube = settings.getBoolean("isCube", false);
 			
 			int x1 = settings.getInt("x1", 0);
 			int y1 = settings.getInt("y1", 0);
 			int z1 = settings.getInt("z1", 0);
 			
 			int x2 = settings.getInt("x2", 0);
 			int y2 = settings.getInt("y2", 0);
 			int z2 = settings.getInt("z2", 0);
 			
 			if (isCube)
 				addCube(x1, y1, z1, x2, y2, z2);
 			else
 				addArea(x1, z1, x2, z2);
 		}
 
 		int areaCount = settings.getInt("areaCount", 0);
 		for (int i = 1; i <= areaCount; i++) {
 			boolean isCube = settings.getBoolean("area" + i + "_isCube", false);
 			
 			int x1 = settings.getInt("area" + i + "_x1", 0);
 			int y1 = settings.getInt("area" + i + "_y1", 0);
 			int z1 = settings.getInt("area" + i + "_z1", 0);
 			
 			int x2 = settings.getInt("area" + i + "_x2", 0);
 			int y2 = settings.getInt("area" + i + "_y2", 0);
 			int z2 = settings.getInt("area" + i + "_z2", 0);
 			
 			if (isCube)
 				addCube(x1, y1, z1, x2, y2, z2);
 			else
 				addArea(x1, z1, x2, z2);
 		}
 		
 		// Old style protectBlocks
 		if (settings.getBoolean("protectBlocks", false)) {
 			protectEdits = PERMISSION.ON;
 			protectExplosions = true;
 			protectBurning = true;
 		}
 		
 		protectEdits = settings.getEnum(PERMISSION.class, "protectEdits", protectEdits);
 		allowOps = settings.getBoolean("allowOps", allowOps);
 		allowDoors = settings.getEnum(PERMISSION.class, "allowDoors", allowDoors);
 		allowContainers = settings.getEnum(PERMISSION.class, "allowContainers", allowContainers);
 		
 		protectEntities = settings.getEnum(PERMISSION.class, "protectEntities", protectEntities);
 		allowMooshroom = settings.getBoolean("allowMooshroom", allowMooshroom);
 		allowVillagers = settings.getBoolean("allowVillagers", allowVillagers);
 		
 		protectExplosions = settings.getBoolean("protectExplosions", protectExplosions);
 		protectBurning = settings.getBoolean("protectBurning", protectBurning);
 		
 		String players = settings.get("allowedPlayers");
 		if (players != null) {
 			this.whitelist.addAll(Arrays.asList(players.toLowerCase().split(";")));
 		}
 	}
 	
 	public void setProtectedZones(ProtectedZones protectedZones) {
		if (protectedZones != null && this.protectedZones != null)
 			throw new IllegalStateException();
 		
 		this.protectedZones = protectedZones;
 	}
 	
 	public Area<ZoneSettings> addArea(int x1, int z1, int x2, int z2) {
 		int tmp;
 		
 		if (x1 > x2) {
 			tmp = x2;
 			x2 = x1;
 			x1 = tmp;
 		}
 		
 		if (z1 > z2) {
 			tmp = z2;
 			z2 = z1;
 			z1 = tmp;
 		}
 		
 		Area<ZoneSettings> newArea = new Area<ZoneSettings>(x1, z1, x2, z2, this);
 
 		return addArea(newArea) ? newArea : null;
 	}
 	
 	public Cube<ZoneSettings> addCube(int x1, int y1, int z1, int x2, int y2, int z2) {
 		int tmp;
 		
 		if (x1 > x2) {
 			tmp = x2;
 			x2 = x1;
 			x1 = tmp;
 		}
 		
 		if (z1 > z2) {
 			tmp = z2;
 			z2 = z1;
 			z1 = tmp;
 		}
 		
 		if (y1 > y2) {
 			tmp = y2;
 			y2 = y1;
 			y1 = tmp;
 		}
 		
 		Cube<ZoneSettings> newCube = new Cube<ZoneSettings>(x1, y1, z1, x2, y2, z2, this);
 
 		return addArea(newCube) ? newCube : null;
 	}
 	
 	private boolean addArea(Area area) {
 		if (!_areas.contains(area) && _areas.add(area)) {
 			
 			if (protectedZones != null)
 				protectedZones.add(area);
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean removeArea(int index) {
 		Area area = null;
 		if (index >= 0 && index < _areas.size() && (area = _areas.remove(index)) != null) {
 			
 			if (protectedZones != null)
 				protectedZones.remove(area);
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean setSetting(String name, String value) {
 		if (name.equalsIgnoreCase("protectEdits") && Settings.isEnumValue(PERMISSION.class, value.toUpperCase())) {
 			protectEdits = Settings.getEnumValue(PERMISSION.class, value.toUpperCase(), protectEdits);
 		}
 		else if (name.equalsIgnoreCase("allowDoors") && Settings.isEnumValue(PERMISSION.class, value.toUpperCase())) {
 			allowDoors = Settings.getEnumValue(PERMISSION.class, value.toUpperCase(), allowDoors);
 		}
 		else if (name.equalsIgnoreCase("allowOps") && Settings.isBooleanValue(value)) {
 			allowOps = Settings.getBooleanValue(value, allowOps);
 		}
 		else if (name.equalsIgnoreCase("allowContainers") && Settings.isEnumValue(PERMISSION.class, value.toUpperCase())) {
 			allowContainers = Settings.getEnumValue(PERMISSION.class, value.toUpperCase(), allowContainers);
 		}
 		
 		else if (name.equalsIgnoreCase("protectEntities") && Settings.isEnumValue(PERMISSION.class, value)) {
 			protectEntities = Settings.getEnumValue(PERMISSION.class, value, protectEntities);
 		}
 		else if (name.equalsIgnoreCase("allowMooshroom") && Settings.isBooleanValue(value)) {
 			allowMooshroom = Settings.getBooleanValue(value, allowMooshroom);
 		}
 		else if (name.equalsIgnoreCase("allowVillagers") && Settings.isBooleanValue(value)) {
 			allowVillagers = Settings.getBooleanValue(value, allowVillagers);
 		}
 		
 		else if (name.equalsIgnoreCase("protectBurning") && Settings.isBooleanValue(value)) {
 			protectBurning = Settings.getBooleanValue(value, protectBurning);
 		}
 		
 		else if (name.equalsIgnoreCase("protectExplosions") && Settings.isBooleanValue(value)) {
 			protectExplosions = Settings.getBooleanValue(value, protectExplosions);
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
 		
 		strings.add("protectEdits(" + protectEdits.toString().toLowerCase() + ")");
 		strings.add("allowOps(" + (allowOps ? "on" : "off") + ")");
 		strings.add("allowDoors(" + (allowDoors.toString().toLowerCase()) + ")");
 		strings.add("allowContainers(" + (allowContainers.toString().toLowerCase()) + ")");
 		
 		strings.add("protectEntities(" + (protectEntities.toString().toLowerCase()) + ")");
 		strings.add("allowMooshroom(" + (allowMooshroom ? "on" : "off") + ")");
 		strings.add("allowVillagers(" + (allowVillagers ? "on" : "off") + ")");
 		
 		strings.add("protectBurning(" + (protectBurning ? "on" : "off") + ")");
 		strings.add("protectExplosions(" + (protectExplosions ? "on" : "off") + ")");
 		
 		strings.add("whitelist(" + whitelist.toString() + ")");
 		
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
 		return whitelist.add(username.trim().toLowerCase());
 	}
 	
 	public boolean revokePlayer(String username) {
 		return whitelist.remove(username.trim().toLowerCase());
 	}
 	
 	public boolean isPlayerWhitelisted(String username) {
 		return whitelist.contains(username.trim().toLowerCase());
 	}
 	
 	public boolean isPlayerAllowed(String username, PERMISSION permission) {
 		if (permission == PERMISSION.WHITELIST)
 			return isPlayerWhitelisted(username);
 		
 		return permission == PERMISSION.ON;
 	}
 	
 	public static boolean isValidName(String name) {
 		return name != null && name.matches("^[A-Za-z0-9_\\-]{1,25}$");
 	}
 	
 	public void saveToSettings(Settings settings, String section) {
 		settings.set(section, "name", name);
 		
 		settings.setInt(section, "dimension", dimension);
 		
 		settings.setInt(section, "areaCount", areas.size());
 		for (int i = 1; i <= areas.size(); i++) {
 			Area area = areas.get(i - 1);
 			settings.setInt(section, "area" + i + "_x1", area.x1);
 			settings.setInt(section, "area" + i + "_z1", area.z1);
 			settings.setInt(section, "area" + i + "_x2", area.x2);
 			settings.setInt(section, "area" + i + "_z2", area.z2);
 			
 			if (area instanceof Cube) {
 				Cube cube = (Cube)area;
 				settings.setBoolean(section, "area" + i + "_isCube", true);
 				settings.setInt(section, "area" + i + "_y1", cube.y1);
 				settings.setInt(section, "area" + i + "_y2", cube.y2);
 			}
 			else {
 				settings.removeKey(section, "area" + i + "_isCube");
 				settings.removeKey(section, "area" + i + "_y1");
 				settings.removeKey(section, "area" + i + "_y2");
 			}
 		}
 		
 		settings.set(section, "protectEdits", protectEdits.toString());
 		settings.setBoolean(section, "allowOps", allowOps);
 		settings.set(section, "allowDoors", allowDoors.toString());
 		settings.set(section, "allowContainers", allowContainers.toString());
 		
 		settings.set(section, "protectEntities", protectEntities.toString());
 		settings.setBoolean(section, "allowMooshroom", allowMooshroom);
 		settings.setBoolean(section, "allowVillagers", allowVillagers);
 		
 		settings.setBoolean(section, "protectBurning", protectBurning);
 		settings.setBoolean(section, "protectExplosions", protectExplosions);
 		
 		StringBuilder sb = new StringBuilder();
 		for (String player : whitelist) {
 			if (sb.length() > 0) sb.append(";");
 			sb.append(player);
 		}
 		settings.set(section, "allowedPlayers", sb.toString());
 	}
 
 	public List<String> settingsAsList() {
 		ArrayList<String> list = new ArrayList<String>();
 		
 		list.add("protectEdits(" + protectEdits.toString().toLowerCase() + ")");
 		list.add("allowOps(" + (allowOps ? "on" : "off") + ")");
 		list.add("allowDoors(" + allowDoors.toString().toLowerCase() + ")");
 		list.add("allowContainers(" + allowContainers.toString().toLowerCase() + ")");
 		
 		list.add("protectEntities(" + (protectEntities.toString().toLowerCase()) + ")");
 		list.add("allowMooshroom(" + (allowMooshroom ? "on" : "off") + ")");
 		list.add("allowVillagers(" + (allowVillagers ? "on" : "off") + ")");
 		
 		list.add("protectBurning(" + (protectBurning ? "on" : "off") + ")");
 		list.add("protectExplosions(" + (protectExplosions ? "on" : "off") + ")");
 		
 		if (sendDebugMessages)
 			list.add("debug(on)");
 		
 		return list;
 	}
 
 	public List<String> playersAsList() {
 		return Arrays.asList(whitelist.toArray(new String[whitelist.size()]));
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + dimension;
 		result = prime * result + ((name == null) ? 0 : name.toLowerCase().hashCode());
 		return result;
 	}
 }
