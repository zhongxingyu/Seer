 package com.cyprias.Lifestones;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 
 import com.cyprias.Lifestones.Config.lifestoneStructure;
 import com.wimbli.WorldBorder.WorldBorder;
 
 public class Lifestones extends JavaPlugin {
 	public static String chatPrefix = "f[aLsf] ";
 	static String pluginName;
 	public Commands commands;
 	public Events events;
 	public HashMap<String, Double> playerProtections = new HashMap<String, Double>();
 	private WorldBorder wb;
 	private Logger log = Logger.getLogger("Minecraft");
 	
 	public void onLoad() {
 		pluginName = getDescription().getName();
 
 		new Config(this);
 		Config.reloadOurConfig();
 		new Attunements(getServer());
 		new Database(this);
 		Database.createTables();
 
 		this.commands = new Commands(this);
 		this.events = new Events(this);
 
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 		}
 		
 		if (Config.checkForNewVersion == true)
 			VersionChecker.retreiveVersionInfo(this, "http://dev.bukkit.org/server-mods/lifestones/files.rss");
 		
 		wb = (WorldBorder) getServer().getPluginManager().getPlugin("WorldBorder");
 		log.info(String.format("%s v%s is loaded.", pluginName, this.getDescription().getVersion()));
 	}
 
 	public static HashMap<String, String> locales = new HashMap<String, String>();
 	public void onEnable() {
 		Config.onEnable();
 		
 		loadLocales();
 		loadAliases();
 		
 		getCommand("lifestone").setExecutor(this.commands);
 		getCommand("lifestones").setExecutor(this.commands);
 		getServer().getPluginManager().registerEvents(this.events, this);
 
 		Attunements.onEnable();
 		Database.loadDatabases(Config.preferAsyncDBCalls);
 
 		log.info(String.format("%s v%s is enabled.", pluginName, this.getDescription().getVersion()));
 	}
 	public void onDisable() {
 		getCommand("lifestones").setExecutor(null);
 		events.unregisterEvents();
 	}
 
 	
 	private void loadLocales(){
 		String localeDir = getDataFolder().separator + "locales" +getDataFolder().separator;
 		
 		//Copy existing locales into plugin dir, so admin knows what's available. 
 		new YML(getResource("enUS.yml"), getDataFolder(), localeDir + "enUS.yml", true);
 		new YML(getResource("ptBR.yml"), getDataFolder(), localeDir + "ptBR.yml", true);
 		
 		//Copy any new locale strings to file on disk.
 		YML resLocale = new YML(getResource("enUS.yml"));
 		YML locale = new YML(getResource(Config.localeFile), getDataFolder(), localeDir+ Config.localeFile);
 		for (String key : resLocale.getKeys(false)) {
 			if (locale.get(key) == null){
 				info("Adding new locale " + key + " = " + resLocale.getString(key).replaceAll("(?i)&([a-k0-9])", "\u00A7$1"));
 				locale.set(key, resLocale.getString(key));
 			}
 		}
 		
 		//Load locales into our hashmap. 
 		locales.clear();
 		for (String key : locale.getKeys(false)) {
 			locales.put(key, locale.getString(key).replaceAll("(?i)&([a-k0-9])", "\u00A7$1"));// 
 		}
 	}
 	
 	private void loadAliases(){
 		YML yml = new YML(getResource("aliases.yml"),getDataFolder(), "aliases.yml");
 		
 		for (String key : yml.getKeys(false)) {
 			Events.aliases.put(key, yml.getString(key));
 		}
 		
 	}
 	
 	
 
 	
 
 	public void info(String msg) {
 		getServer().getConsoleSender().sendMessage(chatPrefix + msg);
 	}
 
 	public void debug(String msg) {
 		if (Config.debugMessages == true) {
 
 			info(ChatColor.DARK_GRAY + "[Debug] " + ChatColor.WHITE + msg);
 		}
 	}
 
 	//
 
 	public boolean hasPermission(CommandSender sender, String node) {
 		if (!(sender instanceof Player)) {
 			return true;
 		}
 		Player player = (Player) sender;
 
 		if (player.isPermissionSet(node)) // in case admin purposely set the
 											// node to false.
 			return player.hasPermission(node);
 
 		if (player.isPermissionSet(pluginName.toLowerCase() + ".*"))
 			return player.hasPermission(pluginName.toLowerCase() + ".*");
 
 		String[] temp = node.split("\\.");
 		String wildNode = temp[0];
 		for (int i = 1; i < (temp.length); i++) {
 			wildNode = wildNode + "." + temp[i];
 
 			if (player.isPermissionSet(wildNode + ".*"))
 				// plugin.info("wildNode1 " + wildNode+".*");
 				return player.hasPermission(wildNode + ".*");
 		}
 
 		return player.hasPermission(node);
 	}
 
 	public void sendMessage(CommandSender sender, String message, Boolean showConsole, Boolean sendPrefix) {
 		if (sender instanceof Player && showConsole == true) {
 			info("e" + sender.getName() + "->f" + message);
 		}
 		if (sendPrefix == true) {
 			sender.sendMessage(chatPrefix + message);
 		} else {
 			sender.sendMessage(message);
 		}
 	}
 
 	public void sendMessage(CommandSender sender, String message, Boolean showConsole) {
 		sendMessage(sender, message, showConsole, true);
 	}
 
 	public void sendMessage(CommandSender sender, String message) {
 		sendMessage(sender, message, true);
 	}
 
 	/**/
 	static public class lifestoneLoc {
 		String world;
 		int X, Y, Z;
 
 		public lifestoneLoc(String world, int X, int Y, int Z) {
 			this.world = world;
 			this.X = X;
 			this.Y = Y;
 			this.Z = Z;
 		}
 	}
 
 	public static double getUnixTime() {
 		return (System.currentTimeMillis() / 1000D);
 	}
 
 	public ArrayList<lifestoneLoc> lifestoneLocations = new ArrayList<lifestoneLoc>();
 
 	public void regsterLifestone(final lifestoneLoc lsLoc) {
 		for (int i = 0; i < lifestoneLocations.size(); i++) {
 			if (lifestoneLocations.get(i).world.equals(lsLoc.world)) {
 				if (lifestoneLocations.get(i).X == lsLoc.X && lifestoneLocations.get(i).Y == lsLoc.Y && lifestoneLocations.get(i).Z == lsLoc.Z) {
 					// info("LS already in aray...");
 					return;
 				}
 			}
 		}
 		lifestoneLocations.add(lsLoc);
 
 		debug("Registered LS at " + lsLoc.world + ", " + lsLoc.X + ", " + lsLoc.Y + ", " + lsLoc.Z);
 		// isLifestoneCache.clear();
 
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			public void run() {
 				cacheSurroundBlocks(lsLoc);
 			}
 		});
 	}
 
 	public static HashMap<Block, Block> isLifestoneCache = new HashMap<Block, Block>();
 	public static HashMap<Block, Block> isProtectedCache = new HashMap<Block, Block>();
 
 	public World getWorld(String worldName){
 		for (int i=0; i< getServer().getWorlds().size(); i++){
 			if (getServer().getWorlds().get(i).getName().equalsIgnoreCase(worldName)){
 				return getServer().getWorlds().get(i);
 			}
 			
 		}
 		
 		return null;
 	}
 	
 	private void cacheSurroundBlocks(lifestoneLoc loc) {
 		World world = getWorld(loc.world);
 		if (world != null){
 			Block cBlock = world.getBlockAt(loc.X, loc.Y, loc.Z);
 			lifestoneStructure lsBlock;
 			for (int b = 0; b < Config.structureBlocks.size(); b++) {
 				lsBlock = Config.structureBlocks.get(b);
 				isLifestoneCache.put(world.getBlockAt(loc.X + lsBlock.rX, loc.Y + lsBlock.rY, loc.Z + lsBlock.rZ), cBlock);
 				// info("caching " + (loc.X+lsBlock.rX) +", " + (loc.Y+lsBlock.rY) +
 				// ", " + (loc.Z+lsBlock.rZ));
 			}
 	
 			for (int y_iter = cBlock.getX() + Config.protectLifestoneRadius; y_iter > cBlock.getX() - Config.protectLifestoneRadius; y_iter--) {
 				for (int x_iter = cBlock.getY() + Config.protectLifestoneRadius; x_iter > cBlock.getY() - Config.protectLifestoneRadius; x_iter--) {
 					for (int z_iter = cBlock.getZ() + Config.protectLifestoneRadius; z_iter > cBlock.getZ() - Config.protectLifestoneRadius; z_iter--) {
 	
 						isProtectedCache.put(world.getBlockAt(y_iter, x_iter, z_iter), cBlock);
 						// info("protecting " + y_iter +", " +x_iter + ", " +
 						// z_iter);
 	
 					}
 				}
 			}
 		}
 	}
 
 	private void removeCachedSurroundBlocks(Player player, lifestoneLoc loc) {
 		Block cBlock = getServer().getWorld(loc.world).getBlockAt(loc.X, loc.Y, loc.Z);
 		lifestoneStructure lsBlock;
 		Block rBlock;
 		// for (int b=0; b<Config.structureBlocks.size();b++){
 		BlockPlaceEvent e;
 		for (int b = Config.structureBlocks.size() - 1; b >= 0; b--) {
 			lsBlock = Config.structureBlocks.get(b);
 			rBlock = getServer().getWorld(loc.world).getBlockAt(loc.X + lsBlock.rX, loc.Y + lsBlock.rY, loc.Z + lsBlock.rZ);
 			isLifestoneCache.remove(rBlock);
 			if (Config.setUnregisteredLifestonesToAir == true) {
 				if (Config.callBlockPlaceEvent == true){
 					e = new BlockPlaceEvent(rBlock, rBlock.getState(), cBlock, player.getItemInHand(), player, false);
 					e.getBlock().setTypeId(0);
 					player.getServer().getPluginManager().callEvent(e);
 				}
 				rBlock.setTypeId(0);
 			}
 		}
 
 		for (int y_iter = cBlock.getX() + Config.protectLifestoneRadius; y_iter > cBlock.getX() - Config.protectLifestoneRadius; y_iter--) {
 			for (int x_iter = cBlock.getY() + Config.protectLifestoneRadius; x_iter > cBlock.getY() - Config.protectLifestoneRadius; x_iter--) {
 				for (int z_iter = cBlock.getZ() + Config.protectLifestoneRadius; z_iter > cBlock.getZ() - Config.protectLifestoneRadius; z_iter--) {
 
 					isProtectedCache.remove(getServer().getWorld(loc.world).getBlockAt(y_iter, x_iter, z_iter));
 					// info("protecting " + y_iter +", " +x_iter + ", " +
 					// z_iter);
 
 				}
 			}
 		}
 	}
 
 	public void unregsterLifestone(Player player, final lifestoneLoc lsLoc) {
 		for (int i = 0; i < lifestoneLocations.size(); i++) {
 			if (lifestoneLocations.get(i).world.equals(lsLoc.world)) {
 				if (lifestoneLocations.get(i).X == lsLoc.X && lifestoneLocations.get(i).Y == lsLoc.Y && lifestoneLocations.get(i).Z == lsLoc.Z) {
 					lifestoneLocations.remove(i);
 
 					removeCachedSurroundBlocks(player, lsLoc);
 
 					return;
 				}
 			}
 		}
 
 	}
 
 	public Boolean isProtected(Block block) {
 		if (isProtectedCache.containsKey(block))
 			return true;
 
 		return false;
 	}
 
 	public Boolean isLifestone(Block block) {
 		if (isLifestoneCache.containsKey(block))
 			return true;
 
 		return false;
 	}
 
 	public Block getLifestoneCenterBlock(Block block) {
 		if (isLifestone(block)) {
 			return isLifestoneCache.get(block);
 		}
 		return null;
 	}
 
 	
 	public Location getRandomLocation(World world, int fails){
 		
 		int rad = Config.randomTPRadius;
 		double bX = 0;
 		double bZ = 0;
 		if (wb != null){
 			rad = wb.GetWorldBorder(world.getName()).getRadius();
 			bX = wb.GetWorldBorder(world.getName()).getX();
 			bZ = wb.GetWorldBorder(world.getName()).getZ();
 		}
 		
		if (fails >= 0)
 			return null;
 		
 		
 		Double rX = (bX-rad) + (Math.random() * (rad*2));
 		Double rZ = (bZ-rad) + (Math.random() * (rad*2));
 		
 		Block b = getTopBlock(world, (int) Math.round(rX), (int) Math.round(rZ));
 		
 		if (b != null){
 			Location blockLoc;
 			
 			switch (b.getTypeId()){
 			case 1:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 3, blockLoc.getBlockZ() + .5);
 			case 2:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 2, blockLoc.getBlockZ() + .5);
 			case 3:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 2, blockLoc.getBlockZ() + .5);
 			case 12:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 2, blockLoc.getBlockZ() + .5);
 			case 13:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 2, blockLoc.getBlockZ() + .5);
 			case 78:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 2, blockLoc.getBlockZ() + .5);
 				
 			case 79:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 2, blockLoc.getBlockZ() + .5);
 				
 			case 110:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 2, blockLoc.getBlockZ() + .5);
 			case 87:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 2, blockLoc.getBlockZ() + .5);
 			case 121:
 				blockLoc = b.getLocation();
 				return new Location(world, blockLoc.getBlockX() + .5, blockLoc.getBlockY() + 2, blockLoc.getBlockZ() + .5);
 				
 			default:
 				return getRandomLocation(world, fails+1);
 			}
 		}
 		
 		return null;
 	}
 	
 	public Location getRandomLocation(World world){
 		return getRandomLocation(world, 0);
 	}
 	
 	private Block getTopBlock(World world, int X, int Z){
 		Block b;
 		for (int i=255; i>0; i--){
 			b = world.getBlockAt(X, i, Z);
 			if (b.getTypeId() != 0){
 				//plugin.info("found" + b.getType() + " (" + b.getTypeId() + ")");
 				return b; //world.getBlockAt(X, i, Z);
 			}
 		}
 		return null;
 	}
 	public static boolean isInt(final String sInt) {
 		try {
 			Integer.parseInt(sInt);
 		} catch (NumberFormatException e) {
 			return false;
 		}
 		return true;
 	}
 	
 	static public String L(String key) {
 		if (locales.containsKey(key))
 			return locales.get(key).toString();
 		
 		return "MISSING LOCALE: " + ChatColor.RED + key;
 	}
 
 	static public String F(String key, Object... args) {
 		String value = L(key);
 		try {
 			if (value != null || args != null)
 				value = String.format(value, args); // arg.toString()
 		} catch (Exception e) {e.printStackTrace();
 		}
 		return value;
 	}
 	
 
 }
