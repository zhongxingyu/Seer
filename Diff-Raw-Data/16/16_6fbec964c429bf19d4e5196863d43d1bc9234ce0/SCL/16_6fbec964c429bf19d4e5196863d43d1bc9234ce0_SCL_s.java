 package com.webkonsept.bukkit.simplechestlock;
 
 import java.io.File;
 import java.io.FileNotFoundException;
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
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.webkonsept.bukkit.simplechestlock.listener.SCLBlockListener;
 import com.webkonsept.bukkit.simplechestlock.listener.SCLEntityListener;
 import com.webkonsept.bukkit.simplechestlock.listener.SCLPlayerListener;
 import com.webkonsept.bukkit.simplechestlock.listener.SCLWorldListener;
 import com.webkonsept.bukkit.simplechestlock.locks.SCLItem;
 import com.webkonsept.bukkit.simplechestlock.locks.SCLList;
 import com.webkonsept.bukkit.simplechestlock.locks.TrustHandler;
 
 public class SCL extends JavaPlugin {
 	private Logger log = Logger.getLogger("Minecraft");
 	protected boolean verbose = false;
 	public boolean lockpair = true;
 	public Material key = Material.STICK;
 	public Material comboKey = Material.BONE;
 	public boolean openMessage = true;
 	public boolean usePermissionsWhitelist = false;
 	public boolean lockedChestsSuck = false;
 	public int suckRange = 3;
 	public TrustHandler trustHandler;
 	
 	public Messaging messaging = new Messaging(this,3000);
 	
 	protected Server server = null;
 	
 	// Intended to hold the material in question and a boolean of weather or not it's double-lockable (like a double chest)
 	public HashMap<Material,Boolean> lockable = new HashMap<Material,Boolean>();
 	
 	// Intended to hold the materials of items/blocked that can also be activated by left-click
 	public HashSet<Material> leftLocked = new HashSet<Material>();
 	
 	// Holding the valid locations for a multi-lockable block
 	public HashSet<Material> lockIncludeVertical = new HashSet<Material>();
 	
 	// Okay for the "sucks items" feature (Item containers only plx!)
 	public HashSet<Material> canSuck = new HashSet<Material>();
 	
 	// The "Lock as" feature!
 	public HashMap<String,String> locksAs = new HashMap<String,String>();
 	
 	private final SCLPlayerListener 	playerListener 	= new SCLPlayerListener(this);
 	private final SCLBlockListener 	blockListener 	= new SCLBlockListener(this);
 	private final SCLEntityListener 	entityListener 	= new SCLEntityListener(this);
 	private final SCLWorldListener	worldListener	= new SCLWorldListener(this);
 	public SCLList			chests			= new SCLList(this);
 	
 	@Override
 	public void onDisable() {
 		chests.save("Chests.txt");
 		this.out("Disabled!");
 		getServer().getScheduler().cancelTasks(this);
 	}
 
 	@Override
 	public void onEnable() {
 		setupLockables();
 		trustHandler = new TrustHandler(this);
 		loadConfig();
 		server = getServer();
 		chests.load("Chests.txt");
 		PluginManager pm = getServer().getPluginManager();
 		
 		pm.registerEvents(playerListener,this);
 		pm.registerEvents(blockListener,this);
 		pm.registerEvents(entityListener,this);
 		pm.registerEvents(worldListener,this);
 		/* OLD!
 		pm.registerEvent(Event.Type.PLAYER_INTERACT,playerListener,Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_BREAK,blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE,blockListener, Priority.Normal, this);
 		pm.registerEvent(Event.Type.ENTITY_EXPLODE,entityListener,Priority.Normal,this);
 		pm.registerEvent(Event.Type.WORLD_LOAD, worldListener,Priority.Normal, this);
 		*/
 		if (lockedChestsSuck){
 			server.getScheduler().scheduleSyncRepeatingTask(this,chests, 100, 100);
 		}
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
 						try {
 							getConfig().load(new File(getDataFolder(),"config.yml"));
 							this.loadConfig();
 						} catch (FileNotFoundException e) {
 							crap("Configuration file went away!");
 						} catch (IOException e) {
 							e.printStackTrace();
 							crap("IOException while reading the config file!  "+e.getMessage());
 						} catch (InvalidConfigurationException e) {
 							e.printStackTrace();
 							crap("Looks like you suck at YAML.  Try again.");
 						}
 						String saveFile = "Chests.txt";
 						if (args.length == 2){
 							saveFile = args[1];
 						}
 						chests.load(saveFile);
 						server.getScheduler().cancelTasks(this);
 						if (lockedChestsSuck){
 							server.getScheduler().scheduleSyncRepeatingTask(this,chests, 100, 100);
 						}
 						sender.sendMessage(ChatColor.GREEN+"Successfully reloaded configuration and locks from "+saveFile);
 						
 					}
 					else {
 						sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Sorry, permission denied!");
 					}
 				}
 				else if (args[0].equalsIgnoreCase("as")){
 					success = true;
 					if (args.length == 2){
 						if (!isPlayer){
 							sender.sendMessage("Sorry mr. Console, you can't lock as anyone.  How will you swing the stick?");
 						}
 						else if (this.permit(player, "simplechestlock.command.as")){
 							locksAs.put(player.getName(),args[1]);
 							sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Locking chests for "+args[1]);
 						}
 						else {
 							sender.sendMessage(ChatColor.RED+"[SimpleChestLock] Sorry, permission denied!");
 						}
 					}
 					else if (args.length == 1){
 						if (locksAs.containsKey(player.getName())){
 							locksAs.remove(player.getName());
 						}
 						sender.sendMessage(ChatColor.GREEN+"[SimpleChestLock] Locking chests for yourself");
 					}
 					else if (args.length > 2){
 						sender.sendMessage(ChatColor.YELLOW+"[SimpleChestLock] Argument amount mismatch.  /scl as <name>");
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
 				else if (args[0].equalsIgnoreCase("trust")){
 				    if (isPlayer){
 				        if (this.permit(player,"simplechestlock.command.trust")){
 				            trustHandler.parseCommand(player,args);
 				        }
 				    }
 				    success = true;
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
 			permit = player.hasPermission(permission);
 			if (permit){
 				babble("Permission granted: "+playerName+"->("+permission+")");
 				break;
 			}
 			else {
 				babble("Permission denied: "+playerName+"->("+permission+")");
 			}
 		}
 		return permit;
 		
 	}
 	public boolean permit(Player player,String permission){
 		return permit(player,new String[]{permission});
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
 		lockable.put(Material.DISPENSER,false);
 		lockable.put(Material.JUKEBOX,false);
 		lockable.put(Material.ENCHANTMENT_TABLE,false);
 		lockable.put(Material.BREWING_STAND,false);
 		
 		//NOTE:  If double locking is enabled for furnaces, remember that a furnace and a burning furnace are NOT the same material!
 		// That means that double locking, which tests the neighboring blocks for .equals() on the material, won't work if one is burning. 
 		lockable.put(Material.FURNACE,false);
 		lockable.put(Material.BURNING_FURNACE,false);
 		
 		// Levers, buttons and doors are special:  You can activate them with a left click.
 		// Hence, we have to lock interaction via LMB as well, making them "leftLocked"
 		
 		lockable.put(Material.LEVER,false);
 		leftLocked.add(Material.LEVER);
 		lockable.put(Material.STONE_BUTTON,false);
 		leftLocked.add(Material.STONE_BUTTON);
 		lockable.put(Material.TRAP_DOOR, false);
 		leftLocked.add(Material.TRAP_DOOR);
 		
 		// WTH, this doesn't seem to work?!
 		lockable.put(Material.FENCE_GATE, false);
 		leftLocked.add(Material.FENCE_GATE);
 		
 		// And now:  Pressure plates!
 		lockable.put(Material.STONE_PLATE,false);
 		lockable.put(Material.WOOD_PLATE,false);
 		
 		// Doors are lockable, leftLocked AND vertically speaking TWO blocks.
 		// This makes them rather complex to lock...
 		lockable.put(Material.WOODEN_DOOR, true);
 		leftLocked.add(Material.WOODEN_DOOR);
 		lockIncludeVertical.add(Material.WOODEN_DOOR);
 		
 		
 		// Some types will "suck" in items, if enabled
 		canSuck.add(Material.CHEST);
 		canSuck.add(Material.DISPENSER);
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
 		File configFile = new File(this.getDataFolder(),"config.yml");
 		File oldConfigFile = new File(this.getDataFolder(),"settings.yml");
 		getConfig().options().copyDefaults(true);
 		getConfig().addDefaults(new HashMap<String,Object>(){
 			private static final long serialVersionUID = 1L;// So shut up, Java.
 			{
 				put("verbose",false);
 				put("key",280);
 				put("comboKey",352);
				put("lockpair",false);
 				put("usePermissionsWhitelist",false);
 				put("lockedChestsSuck",false);
 				put("suckRange",3);
 			}
 		});
 		
 		if (oldConfigFile.exists()){
 			out("Old configuration file found, attempting to move to one!");
 			try {
 				getConfig().load(oldConfigFile);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 				crap("Uh, old config file went away.");
 			} catch (IOException e) {
 				e.printStackTrace();
 				crap("Permissions issues on old config file, perhaps?  Whatever, it's gone.");
 			} catch (InvalidConfigurationException e) {
 				e.printStackTrace();
 				crap("Old config file isn't valid, and will be clobbered.");
 			}
 			oldConfigFile.delete();
 		}
 		verbose = getConfig().getBoolean("verbose", false);
 		Integer keyInt = getConfig().getInt("key",280); // Stick
 		Integer comboKeyInt = getConfig().getInt("comboKey",352); // Bone
 		lockpair = getConfig().getBoolean("lockpair", true);
 		usePermissionsWhitelist = getConfig().getBoolean("usePermissionsWhitelist",false);
 		openMessage = getConfig().getBoolean("openMessage", true);
 		lockedChestsSuck = getConfig().getBoolean("lockedChestsSuck",false);
 		suckRange = getConfig().getInt("suckRange",3);
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
 			try {
 				getConfig().save(configFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 				this.crap("IOError while creating config file: "+e.getMessage());
 			}
 		}
 		if (trustHandler != null){
 		    trustHandler.loadFromConfig();
 		}
 	}
 }
