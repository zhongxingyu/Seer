 package me.armar.plugins.autorank;
 
 import net.milkbowl.vault.Vault;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.util.LinkedHashMap;
 import java.util.Timer;
 import java.util.logging.Logger;
 
 public class Autorank extends JavaPlugin {
 
     private Logger log = Logger.getLogger("Minecraft");
     private Config config;
     private DataStorage data;
     private VaultHandler vault;
     private AutorankSaveData save;
     private Leaderboard leaderboard;
     private AutorankUpdateLeaderboard leaderboardUpdate;
     private boolean debug;
     private Timer timer;
     private RankChanger changer;
 
     public void onEnable() {
 
 	// check if vault is present
 	Plugin x = this.getServer().getPluginManager().getPlugin("Vault");
 	if (x != null & x instanceof Vault) {
 	    vault = new VaultHandler(this);
 	} else {
 	    logMessage("[%s] Vault was _NOT_ found! Disabling autorank.");
 	    getPluginLoader().disablePlugin(this);
 	}
 
 	// set up general config
 	LinkedHashMap<String, Object> configDefaults = new LinkedHashMap<String, Object>();
 	String configPath = this.getDataFolder().getAbsolutePath() + File.separator + "config.yml";
 	configDefaults.put("Enabled", false);
 	configDefaults.put("Debug mode", false);
 	configDefaults.put("Message prefix", "&2");
 	configDefaults.put("Leaderboard layout", "&p | &n - &th hour(s) and &m minute(s).");
 	configDefaults.put("Essentials AFK integration", false);
 
 	String[] availableGroups = vault.getGroups();
 	for (int i = 0; i < availableGroups.length - 1; i++) {
 	    String from = availableGroups[i];
 	    String to = availableGroups[i + 1];
 
 	    configDefaults.put((i + 1) + ".from", from);
 	    configDefaults.put((i + 1) + ".to", to);
 	    configDefaults.put((i + 1) + ".required minutes played", ((i + 1) * 200));
 	    configDefaults.put((i + 1) + ".message", "&2Congratulations, you are now a " + to + ".");
 	    configDefaults.put((i + 1) + ".world", null);
 	    configDefaults.put((i + 1) + ".commands", null);
 	}
 	config = new Config(this, configPath, configDefaults, "config");
 
 	// set up player data
 	data = new DataStorage(this);
 
 	// schedule AutorankUpdate to be run
 	timer = new Timer();
 	timer.scheduleAtFixedRate(new AutorankUpdateData(this), 300000, 300000);
 
 	// schedule AutorankSave to be run
 	save = new AutorankSaveData(this);
 	getServer().getScheduler().scheduleSyncRepeatingTask(this, save, 72000, 72000);
 
 	// make leaderboard
 	leaderboard = new Leaderboard(this);
 
 	leaderboardUpdate = new AutorankUpdateLeaderboard(this);
 	getServer().getScheduler().scheduleSyncRepeatingTask(this, leaderboardUpdate, 33, 36000);
 
 	if (config.get("Debug mode") != null) {
 	    debug = (Boolean) config.get("Debug mode");
 	}
 
 	changer = new RankChanger(this);
 	Bukkit.getPluginManager().registerEvents(changer, this);
 
 	logMessage("Enabled.");
 
 	if (!(Boolean) config.get("Enabled")) {
 	    logMessage("Rank changing disabled. Please set 'enabled' to true if after you set up the ranks.");
 	}
 
 	debugMessage("Debug mode ON");
 
     }
 
     public void onDisable() {
 	getData().save();
 	logMessage("Data saved");
 	logMessage("Disabled.");
 	// stop scheduled tasks from running
 	getServer().getScheduler().cancelTasks(this);
 	timer.cancel();
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 	if (args.length < 1) {
 	    return false;
 	}
 
 	String prefix = "&2";
 	if (getConf().get("Message prefix") != null) {
 	    prefix = (String) getConf().get("Message prefix");
 	}
 
 	prefix = prefix.replaceAll("(&([a-f0-9]))", "\u00A7$2");
 
 	String noPerm = "&cYou do not have permission to use this command.";
 	boolean overridePerms = sender.hasPermission("autorank.*");
 
 	if (args[0].equalsIgnoreCase("debug")) {
 	    if (!(sender instanceof ConsoleCommandSender)) {
 		sender.sendMessage("Please execute from the console");
 		return true;
 	    }
 
 	    sender.sendMessage("----Autorank Debug----");
 
 	    sender.sendMessage("Version: " + this.getDescription().getVersion());
 
 	    final PluginManager pluginManager = getServer().getPluginManager();
 	    final Plugin vaultPlugin = pluginManager.getPlugin("Vault");
 	    sender.sendMessage("Vault version: " + vaultPlugin.getDescription().getVersion());
 
 	    if ((Boolean) config.get("Enabled") == null) {
 		sender.sendMessage("Section 'Enabled' was not found in the config, please check that you are not using a pre-1.0 config");
 	    }
 	    
 	    if ((Boolean) config.get("Enabled") == false) {
 		sender.sendMessage("Section 'Enabled' was not found in the config, please check that you are not using a pre-1.0 config");
 	    }
 	   	    
 	    Player[] onlinePlayers = getServer().getOnlinePlayers();
 	    for(Player player : onlinePlayers){
 		if (player.hasPermission("autorank.exclude") && !player.hasPermission("autorank.sf5k4fg7hu")) {
 		    sender.sendMessage("Player " + player.getName() + " has the autorank.exclude permission and will not be ranked.");
 		}
 		
 		if (player.hasPermission("autorank.timeexclude") && !player.hasPermission("autorank.sf5k4fg7hu")) {
 		    sender.sendMessage("Player " + player.getName() + " has the autorank.timeexclude permission and will not have his/her time counted.");
 		}
 	    }
 	    
 	    sender.sendMessage("Known groups are:");
 	    String[] groups = vault.getGroups();
 	    for(String group :groups){
 		   sender.sendMessage(" - " + group);
 	    }
 	    
 	    sender.sendMessage("Known settings are:");
 	    int entry = 1;
 	    while (config.get(entry + ".from") != null) {
 
 		sender.sendMessage("Entry " + entry);
 		sender.sendMessage("From: " + (String) config.get(entry + ".from"));
 		boolean existingGroup = false;
 		    for(String group :groups){
 			   if( ((String)config.get(entry + ".from") ).equals(group))
 			       existingGroup = true;
 		    }
 		    
 		    if(!existingGroup)
 			    sender.sendMessage("WARNING: The group " + config.get(entry + ".from") + " was not found in the permissions ! Please check spelling.");
 
 		
 		sender.sendMessage("To: " + (String) config.get(entry + ".to"));
 		existingGroup = false;
 		    for(String group :groups){
 			   if( ((String)config.get(entry + ".to") ).equals(group))
 			       existingGroup = true;
 		    }
 		    
 		    if(!existingGroup)
 			    sender.sendMessage("WARNING: The group " + config.get(entry + ".to") + " was not found in the permissions ! Please check spelling.");
 		
 		sender.sendMessage("World: " + (String) config.get(entry + ".world"));
 		
 		entry++;
 	    }
 
 	    sender.sendMessage("----Autorank Debug----");
 
 	    return true;
 	}
 
 	else if (args[0].equalsIgnoreCase("reload")) {
 	    if (!sender.hasPermission("autorank.reload") && !overridePerms) {
 		sender.sendMessage(noPerm);
 		return true;
 	    }
 	    getData().save();
 	    logMessage("Data saved");
 	    getConf().load();
 	    sender.sendMessage(prefix + "Autorank config reloaded");
 	    return true;
 	} else if (args[0].equalsIgnoreCase("help")) {
 	    sender.sendMessage(prefix + "Actions: check, check [name], leaderboard, set [name] [value], reload");
 	    return false;
 	} else if (args[0].equalsIgnoreCase("leaderboard")) {
 	    // check permissions
 	    if (!sender.hasPermission("autorank.leaderboard") && !overridePerms) {
 		sender.sendMessage(noPerm);
 		return true;
 	    }
 	    leaderboard.display(sender, prefix);
 	    return true;
 	} else if (args[0].equalsIgnoreCase("check") && args.length == 1) {
 	    if (!sender.hasPermission("autorank.check") && !overridePerms) {
 		sender.sendMessage(noPerm);
 		return true;
 	    }
 	    if (sender instanceof ConsoleCommandSender) {
 		sender.sendMessage("Cannot check for console.");
 		return true;
 	    }
 	    Integer time = (Integer) data.get(sender.getName().toLowerCase());
 	    if (time == null) {
 		sender.sendMessage(prefix + "No time registered. Please check again in 5 minutes.");
 	    } else {
 		String[] info = getRankInfo((Player) sender);
 		// 0 - current rank
 		// 1 - next rank
 		// 2 - in world
 		// 3 - time to next rank
 		sender.sendMessage(prefix + "You are a " + info[0] + " and have played for " + time / 60 + " hours and " + time % 60
 			+ " minutes.");
 
 		if (info[1] != null && info[3] != null) {
 		    if ((Boolean) config.get("Enabled") == false) {
 			sender.sendMessage(prefix + "Automatic rank changes are disabled");
 			return true;
 		    }
 		    int reqMins = Integer.parseInt(info[3]);
 
 		    if (reqMins > 0) {
 			sender.sendMessage(prefix + "You will be ranked up to " + info[1] + " after " + info[3] + " more minutes.");
 		    } else {
 			sender.sendMessage(prefix + "You will now be ranked up.");
 			changer.CheckRank((Player) sender);
 		    }
 
 		}
 		if (info[2] != null) {
 		    sender.sendMessage(prefix + "This is specific to the world you are currently in. (" + info[2] + ")");
 		}
 	    }
 	    return true;
 	}
 
 	else if (args.length < 2) {
 	    return false;
 	}
 
 	// set variables
 	String playerName = args[1].toLowerCase();
 	Player player = Bukkit.getPlayer(playerName);
 	// handle CHECK OTHERS command
 	if (args[0].equalsIgnoreCase("check")) {
 	    // check permissions
 	    if (!sender.hasPermission("autorank.checkothers") && !overridePerms) {
 		sender.sendMessage(noPerm);
 		return true;
 	    }
 
 	    Integer time = (Integer) data.get(playerName);
 	    if (time == null) {
 		sender.sendMessage(prefix + "No time registered yet, try again later.");
 	    } else {
 		sender.sendMessage(prefix + playerName + " has played for " + time / 60 + " hours and " + time % 60 + " minutes.");
 
 		if (player != null) {
 		    String[] info = getRankInfo(player);
 		    // 0 - current rank
 		    // 1 - next rank
 		    // 2 - in world
 		    // 3 - time to next rank
 		    sender.sendMessage(prefix + playerName + " is a " + info[0] + ".");
 		    if (info[1] != null && info[3] != null) {
 			sender.sendMessage(prefix + "He/she will be ranked up to " + info[1] + " after " + info[3] + " more minutes.");
 		    }
 		    if (info[2] != null) {
 			sender.sendMessage(prefix + "This is specific to the world he/she is currently in. (" + info[2] + ")");
 		    }
 		} else {
 		    sender.sendMessage(prefix + "This player is offline, cannot check future rank");
 		}
 	    }
 	    return true;
 	}
 	// handle SET command
 	else if (args[0].equalsIgnoreCase("set")) {
 	    // check permissions
 	    if (!sender.hasPermission("autorank.set") && !overridePerms) {
 		sender.sendMessage(noPerm);
 		return true;
 	    }
 	    try {
 		int value = Integer.parseInt(args[2]);
 		data.set(playerName, (Integer) value);
 		sender.sendMessage(prefix + playerName + "'s playtime has been set to " + value);
 		return true;
 	    } catch (Exception e) {
 		sender.sendMessage(prefix + "Cannot set to that.");
 		return true;
 	    }
 	} else if (args[0].equalsIgnoreCase("add")) {
 	    // check permissions
 	    if (!sender.hasPermission("autorank.set") && !overridePerms) {
 		sender.sendMessage(noPerm);
 		return true;
 	    }
 	    Integer current = (Integer) data.get(playerName);
 	    if (current == null) {
 		sender.sendMessage("Unknown player.");
 		return true;
 	    }
 	    try {
 		int value = Integer.parseInt(args[2]);
 		value = value + current;
 		data.set(playerName, (Integer) value);
 		sender.sendMessage(prefix + playerName + "'s playtime has been set to " + value);
 		return true;
 	    } catch (Exception e) {
 		sender.sendMessage(prefix + "Cannot set to that.");
 		return true;
 	    }
 	} else if (args[0].equalsIgnoreCase("rem")) {
 	    // check permissions
 	    if (!sender.hasPermission("autorank.set") && !overridePerms) {
 		sender.sendMessage(noPerm);
 		return true;
 	    }
 	    Integer current = (Integer) data.get(playerName);
 	    if (current == null) {
 		sender.sendMessage("Unknown player.");
 		return true;
 	    }
 	    try {
 		int value = Integer.parseInt(args[2]);
 		if (value > current) {
 		    current = value;
 		}
 		value = value - current;
 		data.set(playerName, (Integer) value);
 		sender.sendMessage(prefix + playerName + "'s playtime has been set to " + value);
 		return true;
 	    } catch (Exception e) {
 		sender.sendMessage(prefix + "Cannot set to that.");
 		return true;
 	    }
 	}
 
 	return false;
     }
 
     public void logMessage(String msg) {
 	PluginDescriptionFile pdFile = this.getDescription();
 	this.log.info(pdFile.getName() + " " + pdFile.getVersion() + " : " + msg);
     }
 
     public void debugMessage(String msg) {
 	if (debug) {
 	    this.log.info("Autorank debug: " + msg);
 	}
     }
 
     public boolean getDebug() {
 	return debug;
     }
 
     public Config getConf() {// name is weird because getConfig is used by
 			     // JavaPlugin
 	return config;
     }
 
     public DataStorage getData() {
 	return data;
     }
 
     public VaultHandler getVault() {
 	return vault;
     }
 
     public Leaderboard getLeaderboard() {
 	return leaderboard;
     }
 
     public String[] getRankInfo(Player player) {
 	// returns:
 	// 0 - current rank
 	// 1 - next rank
 	// 2 - in world
 	// 3 - time to next rank
 	String[] res = new String[4];
 
 	String playerName = player.getName().toLowerCase();
 	String world = player.getWorld().getName();
 
 	String[] Playergroups = vault.getGroups(player, world);
 	if (Playergroups.length == 0) {
 	    Playergroups = new String[] { "default" };
 	}
 
 	res[0] = "";
 	for (String group : Playergroups) {
 	    if (!res[0].equals(""))
 		res[0] += ", ";
 	    res[0] += group;
 	}
 
 	boolean found = false;
 	int entry = 1;
 
 	for (int i = 0; !found && i < Playergroups.length; i++) {
 
 	    entry = 1;
 	    found = false;
 	    while (config.get(entry + ".from") != null && !found) {
 
		if (Playergroups[i].equals((String) config.get(entry + ".from")) && (config.get(entry + ".world") == null)
			|| world.equals((String) config.get(entry + ".world"))) {
 		    found = true;
 		} else {
 		    entry++;
 		}
 	    }
 
 	}
 
 	if (found == true) {
 	    res[1] = (String) config.get(entry + ".to");
 	    res[2] = (String) config.get(entry + ".world");
 	}
 
 	Integer timePlayed = (Integer) data.get(playerName);
 
 	if (timePlayed != null && config.get(entry + ".required minutes played") != null)
 	    res[3] = ((Integer) ((Integer) config.get(entry + ".required minutes played") - timePlayed)).toString();
 	;
 
 	return res;
     }
 }
