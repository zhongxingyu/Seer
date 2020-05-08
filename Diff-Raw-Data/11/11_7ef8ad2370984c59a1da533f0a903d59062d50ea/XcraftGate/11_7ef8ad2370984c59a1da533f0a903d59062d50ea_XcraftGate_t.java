 package de.xcraft.engelier.XcraftGate;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.Yaml;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import de.xcraft.engelier.XcraftGate.XcraftGateWorld.Weather;
 import de.xcraft.engelier.XcraftGate.Commands.*;
 
 public class XcraftGate extends JavaPlugin {
 	private final XcraftGatePluginListener pluginListener = new XcraftGatePluginListener(this);
 	private final XcraftGatePlayerListener playerListener = new XcraftGatePlayerListener(this);
 	private final XcraftGateCreatureListener creatureListener = new XcraftGateCreatureListener(this);
 	private final XcraftGateEntityListener entityListener = new XcraftGateEntityListener(this);
 	private final XcraftGateWeatherListener weatherListener = new XcraftGateWeatherListener(this);
 
 	public PermissionHandler permissions = null;
 
 	public Map<String, XcraftGateWorld> worlds = new HashMap<String, XcraftGateWorld>();
 	public Map<String, XcraftGateGate> gates = new HashMap<String, XcraftGateGate>();
 	public Map<String, String> gateLocations = new HashMap<String, String>();
 	public Map<String, Location> justTeleported = new HashMap<String, Location>();
 	public Map<String, Location> justTeleportedFrom = new HashMap<String, Location>();
 	public Map<String, Integer> creatureCounter = new HashMap<String, Integer>();
 
 	public final Logger log = Logger.getLogger("Minecraft");
 
 	class RunCreatureLimit implements Runnable {
 		public void run() {
 			for(Map.Entry<String, XcraftGateWorld> thisWorld: worlds.entrySet()) {
 				thisWorld.getValue().checkCreatureLimit();
 			}
 		}
 	}
 	
 	class RunTimeFrozen implements Runnable {
 		public void run() {
 			for (Map.Entry<String, XcraftGateWorld> thisWorld: worlds.entrySet()) {
 				if (thisWorld.getValue().timeFrozen) {
 					thisWorld.getValue().resetFrozenTime();
 				}
 			}
 		}
 	}
 	
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		saveGates();
 		saveWorlds();
 	}
 
 	public void onEnable() {
 		PluginManager pm = this.getServer().getPluginManager();
 
 		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.CREATURE_SPAWN, creatureListener,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener,
 				Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener,
 				Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.WEATHER_CHANGE, weatherListener,
 				Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_REGAIN_HEALTH, entityListener,
 				Event.Priority.Normal, this);
 
 		Plugin permissionsCheck = pm.getPlugin("Permissions");
 		if (permissionsCheck != null && permissionsCheck.isEnabled()) {
 			permissions = ((Permissions) permissionsCheck).getHandler();
 			log.info(getNameBrackets() + "hooked into Permissions "
 					+ permissionsCheck.getDescription().getVersion());
 		}
 
 		log.info(getNameBrackets() + "by Engelier loaded.");
 
 		loadWorlds();
 		loadGates();
 
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new RunCreatureLimit(), 600, 600);
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new RunTimeFrozen(), 200, 200);
 		try {
 			getCommand("gate").setExecutor(new CommandGate(this));
 			getCommand("gworld").setExecutor(new CommandWorld(this));
 		} catch (Exception ex) {
 			log.warning(getNameBrackets() + "getCommand().setExecutor() failed! Seems I got enabled by another plugin. Nag the bukkit team about this!");
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd,	String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("gate")) {
 			getCommand("gate").setExecutor(new CommandGate(this));
 			getCommand("gate").execute(sender, commandLabel, args);
 			return true;
 		} else if (cmd.getName().equalsIgnoreCase("gworld")) {
 			getCommand("gworld").setExecutor(new CommandWorld(this));						
 			getCommand("gworld").execute(sender, commandLabel, args);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public Boolean hasOpPermission(Player player, String permission) {
 		if (permissions != null) {
 			return permissions.has(player, permission);
 		} else {
 			return player.isOp();
 		}
 	}
 
 	public String getLocationString(Location location) {
 		if (location.getWorld() != null) {
 			return location.getWorld().getName() + ","
 					+ Math.floor(location.getX()) + ","
 					+ Math.floor(location.getY()) + ","
 					+ Math.floor(location.getZ());
 		} else {
 			return null;
 		}
 	}
 
 	public void createGate(Location location, String name) {
		XcraftGateGate newGate = new XcraftGateGate(this, name, getSaneLocation(location));
 		gates.put(name, newGate);
 		gateLocations.put(getLocationString(location), name);
 		saveGates();
 	}
 
 	public void createGateLink(String source, String destination) {
 		gates.get(source).gateTarget = destination;
 		saveGates();
 	}
 
 	public void createGateLoop(String gate1, String gate2) {
 		createGateLink(gate1, gate2);
 		createGateLink(gate2, gate1);
 	}
 
 	public void removeGateLink(String gate) {
 		gates.get(gate).gateTarget = null;
 		saveGates();
 	}
 
 	public void removeGateLoop(String gate1, String gate2) {
 		removeGateLink(gate1);
 		removeGateLink(gate2);
 	}
	
	public Location getSaneLocation(Location loc) {
		double x = Math.floor(loc.getX()) + 0.5;
		double y = loc.getY();
		double z = Math.floor(loc.getZ()) + 0.5;
		
		return new Location(loc.getWorld(), x, y, z, loc.getPitch(), loc.getYaw());
	}
 
 	public String getNameBrackets() {
 		return "[" + this.getDescription().getFullName() + "] ";
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadWorlds() {
 		File configFile = new File(getDataFolder(), "worlds.yml");
 	
 		if (!configFile.exists()) {
 			getDataFolder().mkdir();
 			getDataFolder().setWritable(true);
 			getDataFolder().setExecutable(true);
 
 			try {
 				configFile.createNewFile();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				return;
 			}
 		}
 
 		try {
 			Yaml yaml = new Yaml();
 			Map<String, Object> worldsYaml = (Map<String, Object>) yaml.load(new FileInputStream(configFile));
 			
 			if (worldsYaml == null) {
 				for (World thisWorld: getServer().getWorlds()) {
 					XcraftGateWorld newWorld = new XcraftGateWorld(this);
 					newWorld.load(thisWorld.getName(), thisWorld.getEnvironment());
 					worlds.put(thisWorld.getName(), newWorld);
 				}
 				return;
 			}
 			
 			if (worldsYaml.get("worlds") != null)
 				worldsYaml = (Map<String, Object>) worldsYaml.get("worlds");
 			
 			for (Map.Entry<String, Object> thisWorld : worldsYaml.entrySet()) {
 				String worldName = thisWorld.getKey();
 				Map<String, Object> worldData = (Map<String, Object>) thisWorld.getValue();
 
 				String env = (String) worldData.get("type");
 				
 				XcraftGateWorld newWorld = new XcraftGateWorld(this);
 				for(Environment thisEnv: World.Environment.values()) {
 					if (thisEnv.toString().equalsIgnoreCase(env)) {
 						newWorld.load(worldName, thisEnv);
 					}
 				}
 				
 				if (newWorld.name == null) newWorld.load(worldName, World.Environment.NORMAL);
 
 				newWorld.setBorder((Integer)worldData.get("border"));
 				newWorld.setAllowPvP((Boolean)worldData.get("allowPvP"));
 				newWorld.setAllowAnimals((Boolean)worldData.get("allowAnimals"));
 				newWorld.setAllowMonsters((Boolean)worldData.get("allowMonsters"));
 				newWorld.setCreatureLimit(castInt(worldData.get("creatureLimit")));
 				newWorld.setAllowWeatherChange((Boolean)worldData.get("allowWeatherChange"));
 				newWorld.setTimeFrozen((Boolean)worldData.get("timeFrozen"));
 				newWorld.setDayTime(castInt(worldData.get("setTime")));
 				newWorld.setSuppressHealthRegain((Boolean)worldData.get("suppressHealthRegain"));
 				
 				worlds.put(worldName, newWorld);
 
 				String weather = (String) worldData.get("setWeather");
 				for(Weather thisWeather: XcraftGateWorld.Weather.values()) {
 					if (thisWeather.toString().equalsIgnoreCase(weather)) {
 						newWorld.setWeather(thisWeather);
 					}
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void saveWorlds() {
 		File configFile = new File(getDataFolder(), "worlds.yml");
 		
 		Map<String, Object> toDump = new HashMap<String, Object>();
 
 		for (Map.Entry<String, XcraftGateWorld> thisWorld : worlds.entrySet()) {
 			toDump.put(thisWorld.getKey(), thisWorld.getValue().toMap());
 		}
 
 		Yaml yaml = new Yaml();
 		String dump = yaml.dump(toDump);
 
 		try {
 			FileOutputStream fh = new FileOutputStream(configFile);
 			new PrintStream(fh).println(dump);
 			fh.flush();
 			fh.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void loadGates() {
 		File configFile = new File(getDataFolder(), "gates.yml");
 		int counter = 0;
 
 		if (!configFile.exists()) {
 			log.warning(getNameBrackets()
 					+ "gates file not found. Create some gates!");
 			return;
 		}
 
 		try {
 			Yaml yaml = new Yaml();
 			Map<String, Object> gatesYaml = (Map<String, Object>) yaml
 					.load(new FileInputStream(configFile));
 			for (Map.Entry<String, Object> thisGate : gatesYaml.entrySet()) {
 				String gateName = thisGate.getKey();
 				Map<String, Object> gateData = (Map<String, Object>) thisGate
 						.getValue();
 
 				if (getServer().getWorld((String) gateData.get("world")) == null) {
 					log.severe(getNameBrackets() + "gate " + gateName
 							+ " found in unkown world " + gateData.get("world")
 							+ ". REMOVED!");
 					continue;
 				}
 
 				Location gateLocation = new Location(getServer().getWorld(
 						(String) gateData.get("world")),
 						(Double) gateData.get("locX"),
 						(Double) gateData.get("locY"),
 						(Double) gateData.get("locZ"),
 						((Double) gateData.get("locYaw")).floatValue(),
 						((Double) gateData.get("locP")).floatValue());
 
 				XcraftGateGate newGate = new XcraftGateGate(this, gateName,
 						gateLocation);
 				if (gateData.get("target") != null)
 					newGate.gateTarget = (String) gateData.get("target");
 
 				gates.put(gateName, newGate);
 				gateLocations.put(getLocationString(gateLocation), gateName);
 				counter++;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 
 		for (Map.Entry<String, XcraftGateGate> thisGate : gates.entrySet()) {
 			if (thisGate.getValue().gateTarget != null && gates.get(thisGate.getValue().gateTarget) == null) {
 				log.severe(getNameBrackets() + "gate " + thisGate.getKey()
 						+ " has an invalid destination. Destination removed.");
 				thisGate.getValue().gateTarget = null;
 			}
 		}
 
 		log.info(getNameBrackets() + "loaded " + counter + " gates");
 	}
 
 	public void saveGates() {
 		File configFile = new File(getDataFolder(), "gates.yml");
 
 		if (!configFile.exists()) {
 			try {
 				configFile.createNewFile();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				return;
 			}
 		}
 
 		Map<String, Object> toDump = new HashMap<String, Object>();
 
 		for (Map.Entry<String, XcraftGateGate> thisGate : gates.entrySet()) {
 			String gateName = thisGate.getKey();
 
 			Map<String, Object> values = new HashMap<String, Object>();
 			Location location = gates.get(gateName).gateLocation;
 
 			values.put("world", location.getWorld().getName());
 			values.put("locX", location.getX());
 			values.put("locY", location.getY());
 			values.put("locZ", location.getZ());
 			values.put("locP", location.getPitch());
 			values.put("locYaw", location.getYaw());
 			values.put("target", gates.get(gateName).gateTarget);
 
 			toDump.put(gateName, values);
 		}
 
 		Yaml yaml = new Yaml();
 		String dump = yaml.dump(toDump);
 
 		try {
 			FileOutputStream fh = new FileOutputStream(configFile);
 			new PrintStream(fh).println(dump);
 			fh.flush();
 			fh.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	private static Integer castInt(Object o) {
 		if (o == null) {
 			return 0;
 		} else if (o instanceof Byte) {
 			return (int)(Byte)o;
 		} else if (o instanceof Integer) {
 			return (Integer)o;
 		} else if (o instanceof Double) {
 			return (int)(double)(Double)o;
 		} else if (o instanceof Float) {
 			return (int)(float)(Float)o;
 		} else if (o instanceof Long) {
 			return (int)(long)(Long)o;
 		} else {
 			return 0;
 		}
 	}
 }
