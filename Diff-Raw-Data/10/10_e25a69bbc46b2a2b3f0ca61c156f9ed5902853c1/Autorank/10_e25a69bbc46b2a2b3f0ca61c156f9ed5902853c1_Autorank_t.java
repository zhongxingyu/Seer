 package me.armar.plugins.autorank;
 
 import net.milkbowl.vault.Vault;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.util.LinkedHashMap;
 import java.util.Timer;
 import java.util.logging.Logger;
 
 public class Autorank extends JavaPlugin {
 
     private Logger log = Logger.getLogger("Minecraft");
     private String configPath;
     private String dataPath;
     private Config config;
     private Config data;
     private LinkedHashMap<String, Object> configDefaults = new LinkedHashMap<String, Object>();
     private VaultHandler vault;
     public static Permission permission = null;
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
 	this.configPath = this.getDataFolder().getAbsolutePath()
 		+ File.separator + "config.yml";
 	this.configDefaults.put("Enabled", false);
 	this.configDefaults.put("Debug mode", false);
 	this.configDefaults.put("Message prefix", "&2");
 	this.configDefaults.put("Leaderboard layout", "&n - &tm");
 	//this.configDefaults.put("Essentials AFK integration", false);
 	this.configDefaults.put("Update interval(minutes)", 5);
 	this.configDefaults.put("Leaderboard update interval(minutes)", 30);
 	this.configDefaults.put("Save interval(minutes)", 60);
 	this.configDefaults.put("1.from", "Newcomer");
 	this.configDefaults.put("1.to", "Member");
 	this.configDefaults.put("1.required minutes played", 1800);
 	this.configDefaults.put("1.message",
 		"&2Congratulations, you are now a Member.");
 	this.configDefaults.put("1.commands",
 		"say &p just got ranked up to Member !;eco give &p 50");
 	this.configDefaults.put("1.world", null);
 	this.configDefaults.put("2.from", "Member");
 	this.configDefaults.put("2.to", "Veteran");
 	this.configDefaults.put("2.required minutes played", 12000);
 	this.configDefaults.put("2.message",
 		"&2Congratulations, you are now a Veteran.");
 	this.configDefaults.put("2.world", null);
 	config = new Config(this, configPath, configDefaults, "config");
 
 	// set up player data
 	this.dataPath = this.getDataFolder().getAbsolutePath() + File.separator
 		+ "data.yml";
 	data = new Config(this, dataPath, null, "data");
 
 	// schedule AutorankUpdate to be run
 	int updateInterval = (Integer) config.get("Update interval(minutes)");
 	timer = new Timer();
 	timer.scheduleAtFixedRate(new AutorankUpdateData(this),
 		updateInterval * 1000 * 60, updateInterval * 1000 * 60);
 
 	// schedule AutorankSave to be run
 	save = new AutorankSaveData(this);
 	int saveInterval = 60 * 20 * (Integer) config
 		.get("Save interval(minutes)");
 	getServer().getScheduler().scheduleSyncRepeatingTask(this, save,
 		saveInterval + 22, saveInterval);
 	
 	// make leaderboard
 	leaderboard = new Leaderboard(this);
 
 	// schedule AutorankUpdateLeaderboard to be run
 	if (config.get("Leaderboard update interval(minutes)") == null) {
 	    config.set("Leaderboard update interval(minutes)", 30);
 	    logMessage("Updated config to include Leaderboard update interval");
 	    getConf().save();
 	}
 
 	leaderboardUpdate = new AutorankUpdateLeaderboard(this);
 	int leaderboardUpdateInterval = 60 * 20 * (Integer) config
 		.get("Leaderboard update interval(minutes)");
 	getServer().getScheduler().scheduleSyncRepeatingTask(this,
 		leaderboardUpdate, 33, leaderboardUpdateInterval);
 
 	if (config.get("Debug mode") != null) {
 	    debug = (Boolean) config.get("Debug mode");
 	}
 
 	changer = new RankChanger(this);
 	Bukkit.getPluginManager().registerEvents(changer, this);
 
 	logMessage("Enabled.");
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
 
     public boolean onCommand(CommandSender sender, Command cmd, String label,
 	    String[] args) {
 	String prefix = "&2";
 	if(getConf().get("Message prefix") != null){
 	    prefix = (String) getConf().get("Message prefix");
 	}
 	
	prefix = prefix.replaceAll("(&([a-f0-9]))", "\u00A7$2");
	
 	String noPerm = "&cYou do not have permission to use this command.";
 	boolean overridePerms = sender.hasPermission("autorank.*");
 
 	// onCommand returns true if the command was handled by the plugin
 	if (args.length == 1) {
 	    if (args[0].equalsIgnoreCase("reload")) {
 		if (!sender.hasPermission("autorank.reload") && !overridePerms) {
 		    sender.sendMessage(noPerm);
 		    return true;
 		}
 		getData().save();
 		logMessage("Data saved");
 		getConf().load();
 		sender.sendMessage(prefix + "Autorank config reloaded");
 		return true;
 	    }
 	    if (args[0].equalsIgnoreCase("help")) {
 		sender.sendMessage(prefix + "Actions: check, check [name], leaderboard, set [name] [value], reload");
 	    }
 	    if (args[0].equalsIgnoreCase("leaderboard")) {
 		// check permissions
 		if (!sender.hasPermission("autorank.leaderboard")
 			&& !overridePerms) {
 		    sender.sendMessage(noPerm);
 		    return true;
 		}
 		leaderboard.display(sender, prefix);
 		return true;
 	    }
 	    if (args[0].equalsIgnoreCase("check")) {
 		if (!sender.hasPermission("autorank.check") && !overridePerms) {
 		    sender.sendMessage(noPerm);
 		    return true;
 		}
 		if (sender instanceof ConsoleCommandSender) {
 		    sender.sendMessage("Cannot check for console.");
 		    return true;
 		}
 		Integer time = (Integer) data.get(sender.getName()
 			.toLowerCase());
 		if (time == null) {
 		    sender.sendMessage(prefix + "No time registered yet, try again later.");
 		} else {
 		    String[] info = getRankInfo((Player) sender);
 		    // 0 - current rank
 		    // 1 - next rank
 		    // 2 - in world
 		    // 3 - time to next rank
 		    sender.sendMessage(prefix + "You are a " + info[0]
 			    + " and have played for " + time / 60
 			    + " hours and " + time % 60 + " minutes.");
 		  
 		    if (info[1] != null && info[3] != null) {
 			int reqMins = Integer.parseInt(info[3]);
 			
 			if(reqMins>0){
 			sender.sendMessage(prefix + "You will be ranked up to "
 				+ info[1] + " after " + info[3]
 				+ " more minutes.");
 			}else{
 			    sender.sendMessage(prefix + "You will now be ranked up.");
 			    changer.CheckRank((Player)sender);
 			}
 			
 		    }
 		    if (info[2] != null) {
 			sender.sendMessage(prefix + "This is specific to the world you are currently in. ("+info[2]+")");
 		    }
 		}
 		return true;
 	    }
 	}
 	if (args.length > 1) {
 	    // set variables
 	    String playerName = args[1].toLowerCase();
 	    Player player = Bukkit.getPlayer(playerName);
 	    // handle CHECK OTHERS command
 	    if (args[0].equalsIgnoreCase("check")) {
 		// check permissions
 		if (!sender.hasPermission("autorank.checkothers")
 			&& !overridePerms) {
 		    sender.sendMessage(noPerm);
 		    return true;
 		}
 
 		Integer time = (Integer) data.get(playerName);
 		if (time == null) {
 		    sender.sendMessage(prefix + "No time registered yet, try again later.");
 		} else {
 		    sender.sendMessage(prefix + playerName + " has played for " + time
 			    / 60 + " hours and " + time % 60 + " minutes.");
 
 		    if (player != null) {
 			String[] info = getRankInfo(player);
 			// 0 - current rank
 			// 1 - next rank
 			// 2 - in world
 			// 3 - time to next rank
 			sender.sendMessage(prefix + playerName + " is a " + info[0]
 				+ " and has played for " + time / 60
 				+ " hours and " + time % 60 + " minutes.");
 			if (info[1] != null && info[3] != null) {
 			    sender.sendMessage(prefix + "He/she will be ranked up to "
 				    + info[1] + " after " + info[3]
 				    + " more minutes.");
 			}
 			    if (info[2] != null) {
 				sender.sendMessage(prefix + "This is specific to the world he/she is currently in. ("+info[2]+")");
 			    }
 		    }else{
 			sender.sendMessage(prefix + "This player is offline, cannot check future rank");
 		    }
 		}
 		return true;
 		// handle CHANGE command
 	    }
 	    // handle SET command
 	    if (args[0].equalsIgnoreCase("set")) {
 		// check permissions
 		if (!sender.hasPermission("autorank.set") && !overridePerms) {
 		    sender.sendMessage(noPerm);
 		    return true;
 		}
 		try {
 		    int value = Integer.parseInt(args[2]);
 		    data.set(playerName, (Integer) value);
 		    sender.sendMessage(prefix + playerName
 			    + "'s playtime has been set to " + value);
 		    return true;
 		} catch (Exception e) {
 		    sender.sendMessage(prefix + "Cannot set to that.");
 		    return true;
 		}
 	    }
 	}
 	return false;
     }
 
     public void logMessage(String msg) {
 	PluginDescriptionFile pdFile = this.getDescription();
 	this.log.info(pdFile.getName() + " " + pdFile.getVersion() + " : "
 		+ msg);
     }
 
     public void debugMessage(String msg) {
 	if (debug) {
 	    this.log.info("Autorank debug: " + msg);
 	}
     }
     
     public boolean getDebug(){
 	return debug;
     }
 
     public Config getConf() {// name is weird because getConfig is used by
 			     // JavaPlugin
 	return config;
     }
 
     public Config getData() {
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
 
 	String currentRank = vault.getRank(player, world);
 	res[0] = currentRank;
 
 	int entry = 1;
 	boolean found = false;
 	while (config.get(entry + ".from") != null && !found) {
 
 	    if (currentRank.equals((String) config.get(entry + ".from"))
 		    && (config.get(entry + ".world") == null)
 		    || world.equals((String) config.get(entry + ".world"))) {
 		found = true;
 	    } else {
 		entry++;
 	    }
 	}
 
 	if (found == true) {
 	    res[1] = (String) config.get(entry + ".to");
 	    res[2] = (String) config.get(entry + ".world");
 	}
 
 	Integer timePlayed = (Integer) data.get(playerName);
 
 	if (timePlayed != null
 		&& config.get(entry + ".required minutes played") != null)
 	    res[3] = ((Integer) ((Integer) config.get(entry
 		    + ".required minutes played") - timePlayed)).toString();
 	;
 
 	return res;
     }
 }
