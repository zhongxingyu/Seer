 package me.ashconnell.Fight;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.Location;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 
 public class Fight extends JavaPlugin {
 	
 	private static final Logger log = Logger.getLogger("Minecraft");
 	public static PermissionHandler permissionHandler;
 	
 	private final FightSignListener signListener = new FightSignListener(this);
 	private final FightReadyListener readyListener = new FightReadyListener(this);
 	private final FightRespawnListener respawnListener = new FightRespawnListener(this);
 	private final FightDeathListener deathListener = new FightDeathListener(this);
 	
     public final Map<String, String> fightUsersTeam = new HashMap<String, String>();
     public final Map<String, String> fightUsersClass = new HashMap<String, String>();
     public final Map<String, String> fightClasses = new HashMap<String, String>();
     public final Map<String, Sign> fightSigns = new HashMap<String, Sign>();
     
     int redTeam = 0;
     int blueTeam = 0;
     
     boolean fightInProgress = false;
     
 	public void onEnable() {
 		
 		setupPermissions();
 		
 		// Event Registration
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, signListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, readyListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_RESPAWN, respawnListener, Event.Priority.Highest, this);
 		pm.registerEvent(Event.Type.ENTITY_DEATH, deathListener, Event.Priority.Highest, this);
 		
 		log.info("[Fight] Plugin Started. (version 1.0)");
 		
 		// Create Config if Non-Existant
 		new File("plugins/Fight").mkdir();
 		File configFile = new File("plugins/Fight/config.yml");
 		if(!configFile.exists()){
 		    try { configFile.createNewFile(); } catch(Exception e){ log.info("[Fight] Error when creating config file."); }
 		}
 		
 		// Load Classes From Config
 		Configuration config = new Configuration(configFile);
 		config.load();
 		List<String> classes;
 		classes = config.getKeys("classes");
 		log.info("[Fight] Loaded " + classes.size() + " Classes.");
 		
 		// Load Classes
 		for(int i=0; i < classes.size(); i++){
 			String className = classes.get(i);
 			fightClasses.put(className, config.getString("classes." + className + ".items", null));
 		}
 	}
 
 	public void onDisable() {
 		log.info("[Fight] Plugin Stopped.");
 		cleanSigns();
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		String[] fightCmd = args;
 		
 		if(commandLabel.equalsIgnoreCase("Fight")){
 			Player player = (Player) sender;
 			
 			// Command: /Fight
 			if(args.length < 1 && this.isSetup() && !fightInProgress && hasPermissions(player, "user")){
 				
 				// Check For Empty Inventory
 				ItemStack[] invContents = player.getInventory().getContents();
 				ItemStack[] armContents = player.getInventory().getArmorContents();
 				boolean emptyInventory = false;
 				int invNullCounter = 0;
 				int armNullCounter = 0;
 				for(int i=0; i < invContents.length; i++){
 					if(invContents[i]==null){
 						invNullCounter++;
 					}
 				}
 				for(int i=0; i < armContents.length; i++){
 					if(armContents[i].getType()==Material.AIR){
 						armNullCounter++;
 					}
 				}
 				if(invNullCounter == invContents.length && armNullCounter == armContents.length){
 					emptyInventory = true;
 				}
 				
 				if(emptyInventory == true){
 
 					// Add New Users To Map
 					if(!this.fightUsersTeam.containsKey(player.getName())){
 						this.fightUsersTeam.put(player.getName(), "none");
 					}
 					
 					// Pick Team and Teleport To Lounge
 					if(fightUsersTeam.get(player.getName()) == "none"){
 						if(blueTeam > redTeam){
 							fightUsersTeam.put(player.getName(), "red");
 							sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Welcome! You are on team " + ChatColor.RED + "<Red>");
 							redTeam++;
 							player.teleport(getCoords("redlounge"));
 						}
 						else {
 							fightUsersTeam.put(player.getName(), "blue");
 							blueTeam++;
 							sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Welcome! You are on team " + ChatColor.BLUE + "<Blue>");
 							player.teleport(getCoords("bluelounge"));
 						}
 					}
 					
 					// If In Team, Teleport To Lounge
 					else {
 						String team = fightUsersTeam.get(player.getName());
 						player.teleport(getCoords(team + "lounge"));
 					}	
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "You must have an empty inventory to join a Fight!");
 				}
 			}
 			
 			// Command: /Fight <argument>
 			if(args.length == 1){
 				
 				// Command: /Fight RedLounge
 				if(fightCmd[0].equalsIgnoreCase("redlounge") && hasPermissions(player, "admin")){
 					setCoords(player, "redlounge");
 					sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Red Lounge Set.");
 				}
 				
 				// Command: /Fight RedSpawn
 				else if(fightCmd[0].equalsIgnoreCase("redspawn") && hasPermissions(player, "admin")){
 					setCoords(player, "redspawn");
 					sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Red Spawn Set.");
 				}
 				
 				// Command: /Fight BlueLounge
 				else if(fightCmd[0].equalsIgnoreCase("bluelounge") && hasPermissions(player, "admin")){
 					setCoords(player, "bluelounge");
 					sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Blue Lounge Set.");
 				}
 				
 				// Command: /Fight BlueSpawn
 				else if(fightCmd[0].equalsIgnoreCase("bluespawn") && hasPermissions(player, "admin")){
 					setCoords(player, "bluespawn");
 					sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Blue Spawn Set.");
 				}
 				
 				// Command: /Fight Spectator
 				else if(fightCmd[0].equalsIgnoreCase("spectator") && hasPermissions(player, "admin")){
 					setCoords(player, "spectator");
 					sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Spectator Area Set.");
 				}
 				
 				// Command: /Fight Watch
 				else if(fightCmd[0].equalsIgnoreCase("watch") && this.isSetup() && hasPermissions(player, "user")){
 					
 					// Teleport To Spectator Area
 					player.teleport(getCoords("spectator"));
 					sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Welcome to the spectator's area!");
 					if(fightUsersTeam.containsKey(player.getName())){
 						if(fightUsersTeam.get(player.getName()) == "red"){
 							redTeam = redTeam - 1;
 							log.info("red:" + redTeam + " blue:" + blueTeam);
 						}
 						if(fightUsersTeam.get(player.getName()) == "blue"){
 							blueTeam = blueTeam - 1;
 							log.info("red:" + redTeam + " blue:" + blueTeam);
 						}
 						fightUsersTeam.remove(player.getName());
 						fightUsersClass.remove(player.getName());
 						cleanSigns(player.getName());
 					}
 				}
 				
 				// Command: /Fight Leave
 				else if(fightCmd[0].equalsIgnoreCase("leave") && hasPermissions(player, "user")){
 					if(fightUsersTeam.containsKey(player.getName())){
 						if(fightUsersTeam.get(player.getName()) == "red"){
 							redTeam = redTeam - 1;
 						}
 						if(fightUsersTeam.get(player.getName()) == "blue"){
 							blueTeam = blueTeam - 1;
 						}
 						player.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "You have left the fight.");
 						fightUsersTeam.remove(player.getName());
 						fightUsersClass.remove(player.getName());
 						cleanSigns(player.getName());
						player.getInventory().clear();
						clearArmorSlots(player);
						player.teleport(getCoords("spectator"));
 					}
 					else {
 						player.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "You are not in a team.");
 					}
 				}
 				
 				// Invalid Command
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Invalid Command. (503)");
 				}
 			}
 			
 			// Waypoints have not been setup
 			else if(!this.isSetup()){
 				sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "All Waypoints must be set up first.");
 			}
 			
 			// Command: /Fight <argument> <argument> cont...
 			if(args.length > 1){
 				sender.sendMessage(ChatColor.YELLOW + "[Fight] " + ChatColor.WHITE + "Invalid Command. (504)");
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	// Set Coords (to config.yml)
 	public void setCoords(Player player, String place) {
 		Location location = player.getLocation();
 		File configFile = new File("plugins/Fight/config.yml");
 		Configuration config = new Configuration(configFile);
 		config.load();
 		config.setProperty("coords." + place + ".world", location.getWorld().getName());
 		config.setProperty("coords." + place + ".x", location.getX());
 		config.setProperty("coords." + place + ".y", location.getY());
 		config.setProperty("coords." + place + ".z", location.getZ());
 		config.setProperty("coords." + place + ".yaw", location.getYaw());
 		config.setProperty("coords." + place + ".pitch", location.getPitch());
 		config.save();
 	}
 	
 	// Get Coords (from config.yml)
 	public Location getCoords(String place){
 		File configFile = new File("plugins/Fight/config.yml");
 		Configuration config = new Configuration(configFile);
 		config.load();
 		Double x = config.getDouble("coords." + place + ".x", 0);
 		Double y = config.getDouble("coords." + place + ".y", 0);
 		Double z = config.getDouble("coords." + place + ".z", 0);
 		Float yaw = new Float(config.getString("coords." + place + ".yaw"));
 		Float pitch = new Float(config.getString("coords." + place + ".pitch"));
 		World world = Bukkit.getServer().getWorld(config.getString("coords." + place + ".world"));
 		return new Location(world, x, y, z, yaw, pitch);
 	}
 	
 	// Check if all Waypoints have been set.
 	public Boolean isSetup(){
 		File configFile = new File("plugins/Fight/config.yml");
 		Configuration config = new Configuration(configFile);
 		config.load();
 		List<String> list = config.getKeys("coords");
 		if(list.size() == 5){
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	// Give Player Class Items
 	public void giveItems(Player player){
 		String playerClass = fightUsersClass.get(player.getName());
 		String rawItems = fightClasses.get(playerClass);
 		String[] items;
 		items = rawItems.split(",");
 		for(int i=0; i < items.length; i++){
 			String item = items[i];
 			String[] itemDetail = item.split(":");
 			if(itemDetail.length == 2){
 				int x = Integer.parseInt(itemDetail[0]);
 				int y = Integer.parseInt(itemDetail[1]);
 				ItemStack stack = new ItemStack (x, y);
 				player.getInventory().setItem(i, stack);
 			}
 			else{
 				int x = Integer.parseInt(itemDetail[0]);
 				ItemStack stack = new ItemStack (x, 1);
 				player.getInventory().setItem(i, stack);
 			}
 		}
 	}
 	
 	// Permissions Support
 	private void setupPermissions() {
 	      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
 	      if (Fight.permissionHandler == null) {
 	          if (permissionsPlugin != null) {
 	              Fight.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 	          } else {
 	              log.info("Permission system not detected, defaulting to OP");
 	          }
 	      }
 	  }
 	
 	// Return True If Player Has Permissions
 	private boolean hasPermissions(Player player, String type){
 		if(type == "admin"){
 			if(Fight.permissionHandler.has(player, "fight.admin")){
 				return true;
 			}
 			else { return false; }
 		}
 		else if(type == "user"){
 			if(Fight.permissionHandler.has(player, "fight.user")){
 				return true;
 			}
 			else { return false; }
 		}
 		else {
 			return false;
 		}
 	}
 	
 	// Clean Up All Signs People Have Used For Classes
 	public void cleanSigns(){
 		Set<String> set = fightSigns.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			Sign sign = fightSigns.get(o.toString());
 			sign.setLine(2, "");
 			sign.setLine(3, "");
 			sign.update();
 		}
 	}
 	
 	// Clean Up Signs Specific Player Has Used For Classes
 	public void cleanSigns(String player){
 		Set<String> set = fightSigns.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			Sign sign = fightSigns.get(o.toString());
 			if(sign.getLine(2) == player){
 				sign.setLine(2, "");
 				sign.update();
 			}
 			if(sign.getLine(3) == player){
 				sign.setLine(3, "");
 				sign.update();
 			}
 		}
 	}
 	
 	// Check If Team Has All Chosen A Class
 	public boolean teamReady(String color){
 		int members = 0;
 		int membersReady = 0;
 		Set<String> set = fightUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			if(fightUsersTeam.get(o.toString()) == color){
 				members++;
 				if(fightUsersClass.containsKey(o.toString())){
 					membersReady++;
 				}
 			}
 		}		
 		if(members == membersReady && members > 0){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	// Tell All Fight Players
 	public void tellEveryone(String msg){
 		Set<String> set = fightUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			Player z = getServer().getPlayer(o.toString());
 			z.sendMessage(ChatColor.YELLOW + "[Fight] " + msg);
 		}
 	}
 	
 	
 	// Tell Fight Team Mates
 	public void tellTeam(String color, String msg){
 		Set<String> set = fightUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			if(fightUsersTeam.get(o.toString()) == color){
 				Player z = getServer().getPlayer(o.toString());
 				z.sendMessage(ChatColor.YELLOW + "[Fight] " + msg);
 			}
 		}
 	}
 	
 	// Teleport All Fight Players To Spawn
 	public void teleportAllToSpawn(){
 		Set<String> set = fightUsersTeam.keySet();
 		Iterator<String> iter = set.iterator();
 		while(iter.hasNext()){
 			Object o = iter.next();
 			if(fightUsersTeam.get(o.toString()) == "red"){
 				Player z = getServer().getPlayer(o.toString());
 				z.teleport(getCoords("redspawn"));
 			}
 			if(fightUsersTeam.get(o.toString()) == "blue"){
 				Player z = getServer().getPlayer(o.toString());
 				z.teleport(getCoords("bluespawn"));
 			}
 		}
 	}
 	
 	public void clearArmorSlots(Player player){
 		player.getInventory().setHelmet(null);
 		player.getInventory().setBoots(null);
 		player.getInventory().setChestplate(null);
 		player.getInventory().setLeggings(null);
 	}
 }
