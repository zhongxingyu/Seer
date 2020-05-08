 package com.brandonmabey.telepad;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Core extends JavaPlugin implements Listener{
 
 	private HashMap<String, PlayerHomes> homeMap;
 	private HashMap<String, Integer> lastUseMap;
 	int currentTick = 0;
 
 	private static final String HOMES_SAVE_FILE_NAME = "telepad.sav";
	private static final int TIME_BETWEEN_USES = 600; //seconds
	public static final int TIME_BETWEEN_SAVES = 1200; //Saves every 5 minutes
 	private static final String TELEPAD_SAVE_FILE_NAME = "teleport.sav";
 	
 	private Location lastSelected = null;
 	private Teleporter lastTeleporter = null;
 	
 	
 	private HashMap<String, Teleporter> teleporterMap;
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("home")) {
 			return onHomeCommand(sender, command, label, args);
 		} else if (command.getName().equalsIgnoreCase("sethome")) {
 			return onSetHomeCommand(sender, command, label, args);
 		} else if (command.getName().equalsIgnoreCase("homelist")) {
 			return onListHomeCommand(sender, command, label, args);
 		} else if (command.getName().equalsIgnoreCase("homedel")) {
 			return onHomeDelCommand(sender, command, label, args);
 		} else if (command.getName().equalsIgnoreCase("telepadname")) {
 			return onTelepadNameCommand(sender, command, label, args);
 		} else if (command.getName().equalsIgnoreCase("telepadlist")) {
 			return onTelepadListCommand(sender, command, label, args);
 		} else if (command.getName().equalsIgnoreCase("telepaddel")) {
 			return onTelepadDelCommand(sender, command, label, args);
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean onTelepadDelCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (sender instanceof Player) {
 			Player p = (Player) sender;
 			if (!p.isOp()) {
 				sender.sendMessage(ChatColor.GRAY + "You do not have permission to do that.");
 				return true;
 			}
 		}
 		
 		if (args.length != 1) {
 			sender.sendMessage(ChatColor.GRAY + "Incorrect number of arguments for command.");
 			return false;
 		}
 		
 		if (!teleporterMap.containsKey(args[0])) {
 			sender.sendMessage(ChatColor.GRAY + "No telepad with name " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + ".");
 			return true;
 		}
 		
 		teleporterMap.remove(args[0]);
 		sender.sendMessage(ChatColor.GRAY + "Telepad with name " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + " deleted.");
 		this.getLogger().info("Player " + sender.getName() + " has removed telepad " + args[0] + ".");
 		return true;
 	}
 
 	private boolean onTelepadListCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length > 0) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("Must be a player to use the nearby argument!");
 				return true;
 			}
 			Player p = (Player) sender;
 			
 			if (!p.isOp()) {
 				sender.sendMessage("You do not have permission to do that.");
 				return true;
 			}
 			
 			if (args[0].equalsIgnoreCase("nearby")) {
 				if (teleporterMap == null || teleporterMap.size() == 0) {
 					sender.sendMessage(ChatColor.GRAY + "No telepad with name " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + ".");
 					return true;
 				}
 				Location loc = p.getLocation();
 				Object[] obj = getClosestTelepad(loc);
 				
 				if (obj == null) {
 					sender.sendMessage(ChatColor.RED + "An unkown error occured, Sorry.");
 					this.getLogger().severe("Could not find any teleporters. Data may be lost when shutting down the server.");
 					return true;
 				}
 				
 				Teleporter tp = (Teleporter) obj[0];
 				String name = (String) obj[1];
 				
 				int dist = getDistanceBetweenCoords(tp.getEnterX(), tp.getEnterY(), tp.getEnterZ(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
 				String messages[] = new String[] {
 						ChatColor.GRAY + "The closest telepad " + ChatColor.YELLOW + "\""+ name + "\"" + ChatColor.GRAY + " is " + ChatColor.YELLOW + dist + "m" + ChatColor.GRAY + " away.",
 						ChatColor.GRAY + "The coordinates for entry are " + ChatColor.YELLOW + "(" + tp.getEnterX() + ", " + tp.getEnterY() + ", " + tp.getEnterZ() + ")" + ChatColor.GRAY + ".",
 						ChatColor.GRAY + "The coordinates for exit are " + ChatColor.YELLOW + "(" + tp.getSendX() + ", " + tp.getSendY() + ", " + tp.getSendZ() + ")" + ChatColor.GRAY + "."
 						};
 				
 				sender.sendMessage(messages);
 				return true;
 				
 			} else {
 				sender.sendMessage("Incorrect arguments. Please check your syntax.");
 				return false;
 			}
 		}
 		
 		if (teleporterMap == null || teleporterMap.size() == 0) {
 			sender.sendMessage(ChatColor.GRAY + "No telepads linked yet. Please create some telepads before using this command.");
 		}
 		
 		for (String telepadName : teleporterMap.keySet()) {
 			Teleporter tp = teleporterMap.get(telepadName);
 			sender.sendMessage(ChatColor.YELLOW + telepadName + ChatColor.GRAY + " with entry coordinates " + ChatColor.YELLOW + "(" + tp.getEnterX() + ", " + tp.getEnterY() + ", " + tp.getEnterZ() + ")" + ChatColor.GRAY + ".");
 		}
 		
 		return true;
 	}
 
 	private Object[] getClosestTelepad(Location location) {
 		
 		int closestDistance = Integer.MAX_VALUE;
 		Teleporter closestTP = null;
 		String closestString = null;
 		for (String telepadName : teleporterMap.keySet()) {
 			Teleporter tp = teleporterMap.get(telepadName);
 			
 			int newDistance = getDistanceBetweenCoords(tp.getEnterX(), tp.getEnterY(), tp.getEnterZ(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
 			
 			if (newDistance < closestDistance) {
 				closestDistance = newDistance;
 				closestTP = tp;
 				closestString = telepadName;
 			}
 		}
 		
 		if (closestTP == null || closestString == null) {
 			return null;
 		} else {
 			return new Object[] { closestTP, closestString };
 		}
 		
 	}
 	
 	private int getDistanceBetweenCoords(int x1, int y1, int z1, int x2, int y2, int z2) {
 		return (int)Math.sqrt( ((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)) + ((z1 - z2) * (z1 - z2)) );
 	}
 
 	private boolean onTelepadNameCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length != 1) {
 			return false;
 		}
 		
 		if (findTelepadWithName(args[0])) {
 			sender.sendMessage(ChatColor.GRAY + "Telepad already created with same name.");
 			return true;
 		}
 		
 		if (sender instanceof Player) {
 			Player p = (Player) sender;
 			if (!p.isOp()) {
 				p.sendMessage(ChatColor.GRAY + "You do not have permission to do that.");
 				return true;
 			}
 			lastTeleporter.setSendYaw(p.getLocation().getYaw());
 		}
 		
 		addTelepadWithName(args[0], lastTeleporter);
 		sender.sendMessage(ChatColor.GRAY + "Telepad created with name: " + ChatColor.YELLOW + args[0]);
 		this.getLogger().info("Created teleporter. Name: " + args[0] + " enterX: " + lastTeleporter.getEnterX() + " enterY: " + lastTeleporter.getEnterY() + " enterZ: "
 				+ lastTeleporter.getEnterZ() + " exitX: " + lastTeleporter.getSendX() + " exitY: " + lastTeleporter.getSendY() + " exitZ: " + lastTeleporter.getSendZ());
 		lastTeleporter = null;
 		return true;
 	}
 	
 	private void addTelepadWithName(String name, Teleporter tele) {
 		teleporterMap.put(name, tele);
 	}
 
 	private boolean findTelepadWithName(String string) {
 		return teleporterMap.containsKey(string);
 	}
 	
 	private boolean onHomeDelCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length != 1) {
 			sender.sendMessage(ChatColor.GRAY + "Please supply the name of the house you with to delete.");
 			return false;
 		}
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("Can not use command from console becuase console can not own homes.");
 			return false;
 		}
 		
 		Player p = (Player) sender;
 		
 		PlayerHomes ph = getPlayerHomes(p.getName());
 		
 		if (ph.getHome(args[0]) == null) {
 			sender.sendMessage(ChatColor.GRAY + "No home created with that name.");
 			return true;
 		}
 		
 		ph.removeHome(args[0]);
 		
 		sender.sendMessage(ChatColor.GRAY + "Deleted home called " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + ".");
 		this.getLogger().info("Player " + p.getDisplayName() + " has deleted their home.");
 		return true;
 	}
 
 	private boolean onListHomeCommand(CommandSender sender, Command command, String label, String[] args) {
 		
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("Command can only be used as a player, not from the console");
 			return false;
 		}
 		
 		PlayerHomes playerHomes = getPlayerHomes(sender.getName());
 		sender.sendMessage(playerHomes.getHomeStringNames());
 		
 		return true;
 	}
 
 	private boolean onSetHomeCommand(CommandSender sender, Command command, String label, String[] args) {
 		Player p;
 		if (!(sender instanceof Player)) {
 			this.getLogger().warning("Can not set and use homes from console.");
 			return false;
 		} else {
 			p = (Player) sender;
 		}
 		PlayerHomes ph = getPlayerHomes(sender.getName());
 		
 		String homeName;
 		if (args.length == 0) {
 			homeName = "";
 			sender.sendMessage(ChatColor.GRAY + "Setting default home!");
 		} else {
 			homeName = args[0];
 			sender.sendMessage(ChatColor.GRAY + "Setting home called: " + homeName);
 		}
 		
 		ph.addHomeForName(homeName, new Home(p.getWorld().getName(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()));
 		return true;
 	}
 	
 	private PlayerHomes getPlayerHomes(String s) {
 		if (homeMap.containsKey(s)) {
 			return homeMap.get(s);
 		} else {
 			this.getLogger().info("Creating homes mapping for " + s);
 			homeMap.put(s, new PlayerHomes(null, this.getLogger()));
 			return homeMap.get(s);
 		}
 	}
 	
 	private int getLastPlayerCommand(String player) {
 		if (lastUseMap.containsKey(player)) {
 			return lastUseMap.get(player);
 		} else {
 			lastUseMap.put(player, 0);
 			return -TIME_BETWEEN_USES;
 		}
 	}
 	
 	private void setLastPlayerCommand(String player) {
 		lastUseMap.put(player, this.currentTick);
 	}
 
 	private boolean onHomeCommand(CommandSender sender, Command command,String label, String[] args) {
 		Player p;
 		if (!(sender instanceof Player)) {
 			this.getLogger().warning("Console can not use /home command.");
 			return false;
 		}
 		p = (Player)sender;
 		PlayerHomes ph = getPlayerHomes(sender.getName());
 		
 		
		if (p.getHealth() != p.getMaxHealth()) {
			sender.sendMessage(ChatColor.GRAY + "Can not use /home when damaged!");
			return true;
		}
		if (p.getFoodLevel() < 10) {
			sender.sendMessage(ChatColor.GRAY + "Can not use /home when hunger is under half!");
		}
		
 		String homeName;
 		if (args.length == 0) {
 			homeName = "";
 		} else {
 			homeName = args[0];
 		}
 		
 		Home location = ph.getHome(homeName);
 		if (location == null) {
 			sender.sendMessage("No home set for specified name");
 			return true;
 		}
 		if (homeName != "") {
			sender.sendMessage(ChatColor.GRAY + "Going to your home called: " + ChatColor.YELLOW + homeName);
 		} else {
 			sender.sendMessage(ChatColor.GRAY + "Going to your default home.");
 		}
 		
 		int lastUse = getLastPlayerCommand(sender.getName());
 		if (lastUse > this.currentTick - TIME_BETWEEN_USES) {
			sender.sendMessage(ChatColor.GRAY + "You can not use /home for another " + ChatColor.YELLOW + (lastUse - (this.currentTick - TIME_BETWEEN_USES)) + ChatColor.GRAY + " seconds.");
 			return true;
 		}
 		
 		if (!location.verifyWorld(p.getWorld())) {
 			sender.sendMessage(ChatColor.GRAY + "This home is for a different world. You may not use this home here.");
 		}
 		
 		this.getLogger().info(sender.getName() + " is teleporting to their home at " + location.getX() + ", " + location.getY() + ", " + location.getZ());
 		p.teleport(new Location(p.getWorld(), location.getX(), location.getY(), location.getZ()));
 		setLastPlayerCommand(sender.getName());
 		return true;
 	}
 
 	@Override
 	public void onDisable() {
 		this.getLogger().info("Telepad disabled");
 	
 		
 		File homeFile = getDataFile(HOMES_SAVE_FILE_NAME, false);
 		File telepadFile = getDataFile(TELEPAD_SAVE_FILE_NAME, false);
 		try {
 			TelepadSave.saveHomes(homeFile, homeMap);
 			TelepadSave.saveTelepads(telepadFile, teleporterMap);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onEnable() {
 		this.getLogger().info("Telepad enabled");
 		
 		
 		File f = getDataFile(HOMES_SAVE_FILE_NAME, true);
 		if (f == null) {
 			this.homeMap = new HashMap<String, PlayerHomes>(0);
 		} else {
 			this.homeMap = TelepadLoad.loadHomes(f, this.getLogger());
 		}
 		
 		f = getDataFile(TELEPAD_SAVE_FILE_NAME, true);
 		if (f == null) {
 			this.teleporterMap = new HashMap<String, Teleporter>(0);
 		} else {
 			this.teleporterMap = TelepadLoad.loadTelepads(f, this.getLogger());
 		}
 		
 		this.lastUseMap = new HashMap<String, Integer>(0);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TickUpdater(this), 20, 20);
 		this.getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	public static void main(String args[]) {
 		
 	}
 	
 	/**
 	 * Initialise the data directory for this plugin.
 	 *
 	 * @return true if the directory has been created or already exists.
 	 */
 	private boolean createDataDirectory() {
 	    File file = this.getDataFolder();
 	    if (!file.isDirectory()){
 	        if (!file.mkdirs()) {
 	            // failed to create the non existent directory, so failed
 	            return false;
 	        }
 	    }
 	    return true;
 	}
 	 
 	/**
 	 * Retrieve a File description of a data file for your plugin.
 	 * This file will be looked for in the data directory of your plugin, wherever that is.
 	 * There is no need to specify the data directory in the filename such as "plugin/datafile.dat"
 	 * Instead, specify only "datafile.dat"
 	 *
 	 * @param filename The name of the file to retrieve.
 	 * @param mustAlreadyExist True if the file must already exist on the filesystem.
 	 *
 	 * @return A File descriptor to the specified data file, or null if there were any issues.
 	 */
 	private File getDataFile(String filename, boolean mustAlreadyExist) {
 	    if (createDataDirectory()) {
 	        File file = new File(this.getDataFolder(), filename);
 	        if (mustAlreadyExist) {
 	            if (file.exists()) {
 	                return file;
 	            }
 	        } else {
 	            return file;
 	        }
 	    }
 	    return null;
 	}
 
 	public void resetTicks() {
 		lastUseMap = new HashMap<String, Integer>(0);
 	}
 
 	public void runSave() {
 		
 		this.getLogger().info("Autosaving telepad data.");
 		
 		File f = getDataFile(HOMES_SAVE_FILE_NAME, false);
 		File telepadF = getDataFile(TELEPAD_SAVE_FILE_NAME, false);
 		
 		try {
 			TelepadSave.saveHomes(f, homeMap);
 			TelepadSave.saveTelepads(telepadF, teleporterMap);
 		} catch (Exception e) {
 			e.printStackTrace();
 			this.getLogger().warning("!!! AUTOSAVE FAILED !!!");
 			this.getLogger().warning("Player home locations may not carry over to next server reboot.");
 			return;
 		}
 		this.getLogger().info("Autosave complete.");
 	}
 	
 	@EventHandler
 	public void onBlockClick(PlayerInteractEvent e) {
 		Player p = e.getPlayer();
 		if (e.getItem() == null || e.getItem().getTypeId() == 0) {
 			return;
 		}
 		
 		if (!e.getItem().getType().equals(Material.WOOD_HOE)) {
 			return;
 		}
 		if (!p.isOp()) {
 			return;
 		}
 		
 		
 		e.setCancelled(true);
 		
 		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
 			Location blockLocation = e.getClickedBlock().getLocation();
 			lastSelected = new Location(blockLocation.getWorld(), blockLocation.getX(), blockLocation.getY(), blockLocation.getZ());
 			p.sendMessage(ChatColor.GRAY + "First location set as " + ChatColor.YELLOW + "(" + blockLocation.getBlockX() + ", " + blockLocation.getBlockY() + ", " + blockLocation.getBlockZ() + ")" + ChatColor.GRAY + ".");
 			return;
 		} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK){
 			if (lastSelected == null) {
 				p.sendMessage(ChatColor.GRAY + "Please have an op select a location with left click of a wood hoe before selecting exit location");
 				return;
 			}
 			Location blockLocation = e.getClickedBlock().getLocation();
 			Location secondSelected = new Location(blockLocation.getWorld(), blockLocation.getX(), blockLocation.getY(), blockLocation.getZ());
 			
 			lastTeleporter = new Teleporter(lastSelected, secondSelected, p.getWorld().getName());
 			
 			String messages[] = new String[] {
 					ChatColor.GRAY + "Second location set as " + ChatColor.YELLOW + "(" + blockLocation.getBlockX() + ", " + blockLocation.getBlockY() + ", " + blockLocation.getBlockZ() + ")" + ChatColor.YELLOW + ".",
 					ChatColor.GRAY + "Please type " + ChatColor.WHITE + "/telepadname [name]" + ChatColor.GRAY + " to create the telepad, or change the second location."
 					};
 			p.sendMessage(messages);
 			return;
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerSneak(PlayerToggleSneakEvent e) {
 		if (!e.isSneaking()) { return; }
 		
 		Teleporter tp = getTeleporterForLocationInRadius(e.getPlayer().getLocation(), 3);
 		if (tp == null) { return; }
 		
 		Player p = e.getPlayer();
 		
 		if (!tp.validateWorld(p.getWorld()));
 		
 		p.sendMessage("Teleporting through teleporter.");
 		Location tpLocation = new Location(p.getWorld(), ((float)(tp.getSendX())) + 0.5F, ((float)(tp.getSendY())) + 1.5F, ((float)(tp.getSendZ())) + 0.5F, tp.getSendYaw(), 0F );
 		p.teleport(tpLocation);
 	}
 
 	private Teleporter getTeleporterForLocationInRadius(Location location, int radius) {
 		for (String name : teleporterMap.keySet()) {
 			Teleporter t = teleporterMap.get(name);
 			if (Math.abs(location.getX() - t.getEnterX()) <= radius && Math.abs(location.getY() - (t.getEnterY() + 1)) <= radius && Math.abs(location.getZ() - t.getEnterZ()) <= radius) {
 				return t;
 			}
 		}
 		
 		return null;
 	}
 	
 	
 
 }
