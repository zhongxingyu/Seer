 package com.webkonsept.bukkit.simplechestlock;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class SCL extends JavaPlugin {
 	private Logger log = Logger.getLogger("Minecraft");
 	private PermissionHandler Permissions;
 	protected boolean usePermissions;
 	protected boolean verbose = false;
 	protected boolean lockpair = true;
 	protected Material key = Material.STICK;
 	protected Material comboKey = Material.BONE;
 	protected boolean openMessage = true;
 	protected boolean usePermissionsWhitelist = false;
 	
 	protected Server server = null;
 	
 	// Intended to hold the material in question and a boolean of weather or not it's double-lockable (like a double chest)
 	public HashMap<Material,Boolean> lockable = new HashMap<Material,Boolean>();
 	
 	// Intended to hold the materials of items/blocked that can also be activated by left-click
 	public HashSet<Material> leftLocked = new HashSet<Material>();
 	
 	private SCLPlayerListener 	playerListener 	= new SCLPlayerListener(this);
 	private SCLBlockListener 	blockListener 	= new SCLBlockListener(this);
 	private SCLEntityListener 	entityListener 	= new SCLEntityListener(this);
 	protected SCLList			chests			= new SCLList(this);
 	
 	@Override
 	public void onDisable() {
 		chests.save("Chests.txt");
 		this.out("Disabled!");
 		getServer().getScheduler().cancelTasks(this);
 	}
 
 	@Override
 	public void onEnable() {
 		setupLockables();
 		loadConfig();
 		usePermissions = setupPermissions();
 		server = getServer();
 		chests.load("Chests.txt");
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_INTERACT,playerListener,Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK,blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE,blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_EXPLODE,entityListener,Priority.Normal,this);
 		server.getScheduler().scheduleSyncRepeatingTask(this,chests, 6000, 6000);
 		this.out("Enabled!");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		
 		if ( ! this.isEnabled() ) return false;
 		
 		boolean success = false;
 		boolean isPlayer = false;
 		Player player = null;
 		
 		if (sender instanceof Player){
 			isPlayer = true;
 			player = (Player)sender;
 		}
 
 		if (command.getName().equalsIgnoreCase("scl")){
 			if (args.length == 0){
 				success = false;  // Automagically prints the usage.
 			}
 			else if (args.length >= 1){
 				if (args[0].equalsIgnoreCase("reload")){
 					success = true;  // This is a valid command.
 					if ( !isPlayer  || this.permit(player, "simplechestlock.command.reload")){
 						this.loadConfig();
 						String saveFile = "Chests.txt";
 						if (args.length == 2){
 							saveFile = args[1];
 						}
 						chests.load(saveFile);
 						sender.sendMessage(ChatColor.GREEN+"Successfully reloaded configuration and locks from "+saveFile);
 						
 					}
 					else {
 						sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Sorry, permission denied!");
 					}
 				}
 				else if (args[0].equalsIgnoreCase("save")){
 					success = true;
 					if (!isPlayer || this.permit(player,"simplechestlock.command.save")){
 						String saveFile = "Chests.txt";
 						if (args.length == 2){
 							saveFile = args[1];
 						}
 						chests.save(saveFile);
 						sender.sendMessage(ChatColor.GREEN+"Saved locks to "+saveFile);
 					}
 					else {
 						sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Sorry, permission denied!");
 					}
 				}
 				else if (args[0].equalsIgnoreCase("status")){
 					success = true;
 					if (!isPlayer || this.permit(player,"simplechestlock.command.status")){
 						HashMap<String,Integer> ownership = new HashMap<String,Integer>();
 						int total = 0;
 						for (SCLItem item : chests.list.values()){
 							Integer owned = ownership.get(item.getOwner());
 							total++;
 							if (owned == null){
 								ownership.put(item.getOwner(),1);
 							}
 							else {
 								ownership.put(item.getOwner(),++owned);
 							}
 						}
 						
 						for (String playerName : ownership.keySet()){
 							Integer owned = ownership.get(playerName);
 							if (owned == null){
 								owned = 0;
 							}
 							sender.sendMessage(playerName+": "+owned);
 						}
 						sender.sendMessage("Total: "+total);
 					}
 				}
				else if (args[0].equalsIgnoreCase("list")){
					success = true;
					if (!isPlayer || this.permit(player, "simplechestlock.command.list")){
						for (SCLItem item : chests.list.values()){
							sender.sendMessage(item.getLocation().toString());
						}
					}
				}
 			}
 		}
 		else {
 			sender.sendMessage(ChatColor.RED+"Command is deprecated, try /scl");
 		}
 		return success;
 	}
 	public boolean permit(Player player,String[] permissions){ 
 		
 		if (player == null) return false;
 		if (permissions == null) return false;
 		String playerName = player.getName();
 		boolean permit = false;
 		for (String permission : permissions){
 			if (usePermissions){
 				permit = Permissions.permission(player, permission);
 			}
 			else {
 				permit = player.hasPermission(permission);
 			}
 			if (permit){
 				babble("Permission granted: "+playerName+"->"+permission);
 				break;
 			}
 			else {
 				babble("Permission denied: "+playerName+"->"+permission);
 			}
 		}
 		return permit;
 		
 	}
 	public boolean permit(Player player,String permission){
 		return permit(player,new String[]{permission});
 	}
 	private boolean setupPermissions() {
 		boolean crap = false;
 		
 		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 		if (test != null){
 			crap = true;
 			this.Permissions = ((Permissions)test).getHandler();
 		}
 		
 		return crap;
 	}
 	public void out(String message) {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
 	}
 	public void crap(String message){
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.severe("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
 	}
 	public void babble(String message){
 		if (!this.verbose){ return; }
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " VERBOSE] " + message);
 	}
 	public String plural(int number) {
 		if (number == 1){
 			return "";
 		}
 		else {
 			return "s";
 		}
 	}
 	private void setupLockables() {
 		lockable.clear();
 		// DEFAULT VALUES
 		lockable.put(Material.CHEST,true);
 		//NOTE:  If double locking is enabled for furnaces, remember that a furnace and a burning furnace are NOT the same material!
 		// That means that double locking, which tests the neighboring blocks for .equals() on the material, won't work if one is burning. 
 		lockable.put(Material.FURNACE,false);
 		lockable.put(Material.BURNING_FURNACE,false);
 		lockable.put(Material.DISPENSER,false);
 		lockable.put(Material.JUKEBOX,false);
 		
 		// Levers, buttons and doors are special:  You can activate them with a left click.
 		// Hence, we have to lock interaction via LMB as well, making them "leftLocked"
 		lockable.put(Material.WOODEN_DOOR, true);
 		lockable.put(Material.LEVER,false);
 		lockable.put(Material.STONE_BUTTON,false);
 		lockable.put(Material.TRAP_DOOR, false);
 		leftLocked.add(Material.STONE_BUTTON);
 		leftLocked.add(Material.LEVER);
 		leftLocked.add(Material.WOODEN_DOOR);
 		leftLocked.add(Material.TRAP_DOOR);
 		
 		
 		// And now:  Pressure plates!
 		lockable.put(Material.STONE_PLATE,false);
 		lockable.put(Material.WOOD_PLATE,false);
 
 	}
 	public boolean canLock (Block block){
 		if (block == null) return false;
 		Material material = block.getType();
 		return lockable.containsKey(material);
 	}
 	public boolean canDoubleLock (Block block){
 		if (block == null) return false;
 		Material material = block.getType();
 		if (lockable.containsKey(material)){
 			return lockable.get(material);
 		}
 		else {
 			return false;
 		}
 	}
 	public void loadConfig() {
 		File configFile = new File(this.getDataFolder(),"settings.yml");
 		File configDir = this.getDataFolder();
 		Configuration config = new Configuration(configFile);
 		
 		config.load();
 		verbose = config.getBoolean("verbose", false);
 		Integer keyInt = config.getInt("key",280); // Stick
 		Integer comboKeyInt = config.getInt("comboKey",352); // Bone
 		
 		lockpair = config.getBoolean("lockpair", true);
 		usePermissionsWhitelist = config.getBoolean("usePermissionsWhitelist",false);
 		openMessage = config.getBoolean("openMessage", true);
 		key = Material.getMaterial(keyInt);
 		comboKey = Material.getMaterial(comboKeyInt);
 		if (key == null){
 			key = Material.STICK;
 			this.crap("OY!  Material ID "+keyInt+" is not a real material.  Falling back to using STICK (ID 280) for the key.");
 		}
 		if (comboKey == null){
 			comboKey = Material.BONE;
 			this.crap("OY!  Materail ID "+comboKeyInt+" is not a real material. Falling back to using BONE (ID 352) for the combo key.");
 		}
 		if (!configFile.exists()){
 			if (!configDir.exists()){
 				configDir.mkdir();
 			}
 			try {
 				configFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 				this.crap("IOError while creating config file: "+e.getMessage());
 			}
 			config.save();
 		}
 	}
 }
