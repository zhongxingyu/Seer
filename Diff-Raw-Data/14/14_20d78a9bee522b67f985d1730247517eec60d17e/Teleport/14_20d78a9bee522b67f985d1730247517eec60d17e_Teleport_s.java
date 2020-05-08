 package iggy.Teleport;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Teleport extends JavaPlugin {
 	public static Teleport plugin;
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public final TeleportPlayerListener playerListen = new TeleportPlayerListener(this);
 	public Map<Player, Date> lastJump = new HashMap<Player, Date>();
 	
 	public Map<String,Location> cityTeleports = new HashMap<String,Location>();
 	public Map<Location,String> cityActivators = new HashMap<Location,String>();
 	public Map<String,List<String>> playerActivations = new HashMap<String,List<String>>();
 	
 	
 	PluginDescriptionFile pdFile;
 	String pluginName;
 	String pluginTitle;
 	
 	public void loadCities () {
 		cityTeleports.clear();
 		cityActivators.clear();
 		//Load all the city names
 		Set <String> cities = this.getConfig().getConfigurationSection("city").getKeys(false);
 		
 		if (cities == null) {
 			this.logger.severe(pluginTitle+" Failed to read the Configuration File");
 			return;
 		}
 		
 		// iterate through the cities and get their data
 		Iterator<String> it = cities.iterator();
 		while (it.hasNext()) {
 	        String cityName = it.next();
 	        
 	        /// possible version 2 ///
 	        //String warp = this.getConfig().getString("city."+cityName+".warp");
 	        //String activator = this.getConfig().getString("city."+cityName+".activator");
 	        
 	        
 	        // LOAD TELEPORT
 	        World warpWorld = this.getServer().getWorld(this.getConfig().getString("city."+cityName+".warp.world"));
 	        
 	        if (warpWorld == null) {
 	        	this.logger.severe("["+pluginName+"] Failed to find the world for "+cityName+"'s warp on the server");
 	        	continue;
 	        }
 	        
 	        Double warpX = this.getConfig().getDouble("city."+cityName+".warp.x");
	        Double warpY = this.getConfig().getDouble("city."+cityName+".warp.z");
 	        Double warpZ = this.getConfig().getDouble("city."+cityName+".warp.z");
 	        
 	        float warpYaw = Float.parseFloat(this.getConfig().getString("city."+cityName+".warp.yaw"));
 	        float warpPitch = 0;
 	        
 	        //LOAD ACTIVATOR
 	        
 	        World activatorWorld = this.getServer().getWorld(this.getConfig().getString("city."+cityName+".activator.world"));
 	        
 	        if (activatorWorld == null){
 	        	this.logger.severe(pluginTitle+" Failed to find the world for "+cityName+"'s activator on the server");
 	        	continue;
 	        }
 	        
 	        Double activatorX = this.getConfig().getDouble("city."+cityName+".activator.x");
 	        Double activatorY = this.getConfig().getDouble("city."+cityName+".activator.y");
 	        Double activatorZ = this.getConfig().getDouble("city."+cityName+".activator.z");
 
 	        // Put both on the lists
 	        cityTeleports.put(cityName, new Location(warpWorld,warpX,warpY,warpZ,warpYaw,warpPitch));
 	        cityActivators.put(new Location(activatorWorld,activatorX,activatorY,activatorZ), cityName);
 	        
 	        
 	    }
 		this.logger.info(pluginTitle+" Loaded \033[0;32m" + String.valueOf(cities.size()) + "\033[0m Cities \033[0;35m"+cities.toString() + "\033[0m");
 	}
 	public void saveCities() {
 		// clear the current listing of cities
 		this.getConfig().set("city","");
 		
 		// add the new cities warp
 		Iterator<Entry<String, Location>> teleportIterator =  cityTeleports.entrySet().iterator();
 		while (teleportIterator.hasNext()) {
 			Entry<String,Location> pairs = (Entry<String,Location>)teleportIterator.next();
 			
 			String cityName = pairs.getKey();
 			
 			logger.info("Saving warps for :"+cityName);
 			
 			
 			// Set warp
 			this.getConfig().set("city."+cityName+".warp.world", pairs.getValue().getWorld().getName());
 			this.getConfig().set("city."+cityName+".warp.x", pairs.getValue().getX());
 			this.getConfig().set("city."+cityName+".warp.y", pairs.getValue().getY());
 			this.getConfig().set("city."+cityName+".warp.z", pairs.getValue().getZ());
 			this.getConfig().set("city."+cityName+".warp.yaw", pairs.getValue().getYaw());
 	    }
 		
 		// add the new cities activators
 		Iterator<Entry<Location, String>> activatorIterator = this.cityActivators.entrySet().iterator();
 		while (activatorIterator.hasNext()) {
 			Entry<Location, String> pairs = (Entry<Location,String>)activatorIterator.next();
 			
 			String cityName = pairs.getValue();
 			
 			logger.info("Saving activators for :"+cityName);
 			
 			// Set Activator
 			this.getConfig().set("city."+cityName+".activator.world", pairs.getKey().getWorld().getName());
 			this.getConfig().set("city."+cityName+".activator.x", pairs.getKey().getX());
 			this.getConfig().set("city."+cityName+".activator.y", pairs.getKey().getY());
 			this.getConfig().set("city."+cityName+".activator.z", pairs.getKey().getZ());
 			
 		}
 		
 	}
 	
 	public void addCity(String city, Location warp, Location activator) {
 		cityActivators.put(activator, city);
 		cityTeleports.put(city, warp);
 		saveCities();
 		this.saveConfig();
 		this.logger.info(pluginTitle + " "+city+" was created");
 	}
 	
 	public void loadActivations() {
 		playerActivations.clear();
 		
 		Set <String> players = this.getConfig().getConfigurationSection("player").getKeys(false);
 		
 		if (players == null) {
 			this.logger.severe(pluginTitle + " Failed to read the Configuration File");
 		}
 		
 		// iterate through the players
 		Iterator<String> playerIterator = players.iterator();
 		while (playerIterator.hasNext()){
 			String playerName = playerIterator.next();
 			
 			List<String> activations = this.getConfig().getStringList("player."+playerName);
 			
 			if (activations == null) {
 				this.logger.severe(pluginTitle + " \033[0;31mFailed to read the Configuration File\033[0m");
 			}
 			
 			playerActivations.put(playerName, activations);
 		}
 		this.logger.info(pluginTitle+" Loaded \033[0;32m" + String.valueOf(players.size()) + "\033[0m Players \033[0;35m"+players.toString() + "\033[0m");
 	}
 	public void saveActivations() {
 		this.getConfig().set("player", "");
 		
 		Iterator<Entry<String, List<String>>> it = playerActivations.entrySet().iterator();
 		
 		if (it == null){
 			this.logger.severe(pluginTitle + "\033[0;32mFailed to save configuration file \033[0m(playerActivations iterator is null)");
 			return;
 		}
 		
 		while(it.hasNext()) {
 			Entry<String,List<String>> pairs = (Entry<String,List<String>>) it.next();
 			
 			String playerName = pairs.getKey();
 			
 			logger.info(pluginTitle+"Saving player activations for \033[0;32m"+playerName+"\033[0m");
 			
 			this.getConfig().set("player."+playerName,pairs.getValue());
 		}
 	}
 	/******************************* ADD ACTIVATIONS ******************************\
 	| This adds the city to the player's activated cities list. If the player      |
 	| already has the city activated then the function returns false and does      |
 	| nothing, on success it returns true                                           |
 	\******************************************************************************/
 	public Boolean addActivation (String player, String city) {
 		List<String> activations = playerActivations.get(player);
 		
 		if (activations == null) {
 			activations = new ArrayList<String>();
 		}
 		
 		if(activations.contains(city)){
 			return false;
 		}
 		
 		activations.add(city);
 		
 		playerActivations.put(player, activations);
 		
 		saveActivations();
 		this.saveConfig();
 		this.logger.info(pluginTitle + player + " activated " + city);
 		return true;
 	}
 	
 	
 	@Override
 	public void onEnable() {
 		pdFile = this.getDescription();
 		pluginName = pdFile.getName();
 		pluginTitle = "[\033[0;36m"+pluginName+"\033[0m]";
 		
 		Bukkit.getServer().getPluginManager().registerEvents(playerListen, this);
 		
 		loadCities();
 		loadActivations();
 		
 		this.logger.info(pluginTitle+ " version " + pdFile.getVersion() +" is enabled");
 	}
 	@Override
 	public void onDisable() {
 		saveCities();
 		saveActivations();
 		this.saveConfig();
 		this.logger.info(pluginTitle + " version " + pdFile.getVersion() +" is disabled");
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		
 		if (player == null) {
 			this.logger.info("This command can only be run by a player");
 			return false;
 		}
 		
 		//World world = player.getWorld();
 		
 		if (commandLabel.equalsIgnoreCase("createwarp")){
 			if (player.isOp() || (player.hasPermission("teleport.createwarp"))){
 					
 			}
 			else {
 				player.sendMessage("Ask an admin to create this warp for you");
 			}
 		}
 		if (commandLabel.equalsIgnoreCase("warp")){
 			if ( args.length == 1) {
 				if (playerActivations.get(player.getName()).contains(args[0])) {
 					//attempt to teleport the player
 					warp(player,args[0]);
 					player.sendMessage("begining warp");
 				} else {
 					player.sendMessage("You have not visited this warp, or this warp does not exist");
 				}
 			}
 			else {
 				player.sendMessage("correct usage is /warp <location>");
 			}
 			
 		}
 		return false;
 	}
 	// queue for teleporters
 	public Queue<Player>   teleportingPlayerQueue = new LinkedList<Player>();
 	public Queue<Location> teleportingDestinationQueue = new LinkedList<Location>();
 	// hash table for waiting teleporters
 	public Map<Player,Location> teleportingPlayers = new HashMap<Player,Location>();
 	public Map<Player,Date> lastWarpTime = new HashMap<Player,Date>();
 	//
 	public void warp (Player player, String cityname){
 		Date nowdate = new Date();
 		Date thendate = lastWarpTime.put(player, nowdate);
 		
 		
 		
 		// prevent rapid jumping or quick jumping
 		long nowtime = nowdate.getTime();
		long thentime = thendate.getTime();
 			
 		if ((nowtime - thentime) < 6000) {
 			player.sendMessage("You must wait to teleport");
 			return;
 		}
 		
 		teleportingPlayerQueue.offer(player);
 		teleportingDestinationQueue.offer(cityTeleports.get(cityname));
 		teleportingPlayers.put(player, player.getLocation());
 		
 		
 		
		
 		// need to continue this function later
 		//trick the client to displaying a warp animation by creating a portal under the player
 		player.sendBlockChange(player.getLocation(), Material.PORTAL, (byte) 0);
 		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				Player player = teleportingPlayerQueue.poll();
 				Location location = teleportingDestinationQueue.poll();
 				
 				if (teleportingPlayers.containsKey(player))
 				
 				player.sendBlockChange(player.getLocation(), player.getWorld().getBlockTypeIdAt(player.getLocation()), (byte) 0);
 				
 				player.teleport(location);
 			}
 		}, 120L);
 	}
 }
