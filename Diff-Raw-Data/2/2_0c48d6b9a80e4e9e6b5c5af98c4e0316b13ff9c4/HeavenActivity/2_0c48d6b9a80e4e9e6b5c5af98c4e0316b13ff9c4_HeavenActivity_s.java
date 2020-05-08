 package net.blockheaven.kaipr.heavenactivity;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.iConomy.*;
 import com.iConomy.system.Holdings;
 import com.nijiko.permissions.PermissionHandler;
 
 
 public class HeavenActivity extends JavaPlugin {
 	/**
      * Logger for messages.
      */
     protected static final Logger logger = Logger.getLogger("Minecraft.HeavenActivity");
     
     /**
      * Configuration
      */
     public HeavenActivityConfig config;
     
     /**
      * Permission handler
      */
     public static PermissionHandler Permissions;
     
     /**
      * iConomy hook
      */
     public static iConomy iConomy;
     
     /**
      * Stores the sequences of players activities <sequence, <playerName, activity>>
      */
     public Map<Integer, Map<String, Double>> playersActivities = new HashMap<Integer, Map<String, Double>>();
     
     /**
      * Sequence update timer
      */
     public static Timer updateTimer = null;
     
     /**
      * The current sequence
      */
     public int currentSequence = 0;
     
     /**
      * Tracking
      */
     public Double chatPointsGiven;
     public Double chatCharPointsGiven;
     public Double commandPointsGiven;
     public Double commandCharPointsGiven;
     public Double movePointsGiven;
     public Double blockPlacePointsGiven;
     public Double blockBreakPointsGiven;
     
     /**
      * Called when plugin gets enabled, initialize all the stuff we need
      */
     public void onEnable() {
         
     	logger.info(getDescription().getName() + " "
                 + getDescription().getVersion() + " enabled.");
         
         config = new HeavenActivityConfig(this);
         
         startUpdateTimer();
         
         PlayerListener playerListener = new HeavenActivityPlayerListener(this);
         BlockListener blockListener = new HeavenActivityBlockListener(this);
         ServerListener serverListener = new HeavenActivityServerListener(this);
 
         PluginManager pm = getServer().getPluginManager();
         if (config.moveTracking)
             pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
         if (config.commandTracking || config.logCommands)
             pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Monitor, this);
         if (config.chatTracking)
             pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Monitor, this);
         if (config.blockTracking) {
             pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
             pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Monitor, this);
         }
         pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
         
     }
     
     /**
      * Called when the plugin gets disabled, disable timers and save stats
      */
     public void onDisable() {
     	config.reloadAndSave();
     	stopUpdateTimer();
     }
     
     /**
      * Command handling
      */
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
     	
     	if (!(sender instanceof Player) && args.length == 0) {
             sender.sendMessage(ChatColor.RED + "[Activity] Activity is only tracked for players!");
     		return false;
         }
     	
     	if (args.length == 0) {
     		int activity = getActivity((Player) sender);
     		sendMessage(sender, "Your current activity is: " + activityColor(activity) + activity + "%");
     	} else if (args[0].compareToIgnoreCase("list") == 0 || args[0].compareToIgnoreCase("listall") == 0) {
     		if (hasPermission(sender, "activity.view.list", true)) {
     			StringBuilder res = new StringBuilder();
     			for (Player player : getServer().getOnlinePlayers()) {
     			    int activity = getActivity(player);
     			    res.append(activityColor(activity) + player.getName() + " " + activity + "%");
     			    res.append(ChatColor.GRAY + ", ");
     		    }
     		    sendMessage(sender, res.substring(0, res.length() - 2));
     		} else {
     			sendMessage(sender, ChatColor.RED + "You have no permission to see a list of online players' activity.");
     		}
     	} else if (args[0].compareToIgnoreCase("admin") == 0 && hasPermission(sender, "activity.admin", false)) {
     	    if (args.length == 1) {
     	    	sendMessage(sender, ChatColor.RED + "/activity admin <reload|stats|resetstats>");
     	    } else if (args[1].compareToIgnoreCase("reload") == 0) {
         		config.reloadAndSave();
         		config.load();
         		stopUpdateTimer();
         		startUpdateTimer();
         		sendMessage(sender, ChatColor.GREEN + "Reloaded");
         	} else if (args[1].compareToIgnoreCase("stats") == 0) {
         		sendMessage(sender, ChatColor.YELLOW + "Statistic " + ChatColor.DARK_GRAY + "--------------------");
         		sendMessage(sender, "Chat: " + ChatColor.WHITE 
         				+ this.chatPointsGiven.intValue());
         		sendMessage(sender, "Chat char: " + ChatColor.WHITE 
         				+ this.chatCharPointsGiven.intValue());
         		sendMessage(sender, "Command: " + ChatColor.WHITE
         				+ this.commandPointsGiven.intValue());
         		sendMessage(sender, "Command char: " + ChatColor.WHITE
         				+ this.commandCharPointsGiven.intValue());
         		sendMessage(sender, "Move: " + ChatColor.WHITE
         				+ this.movePointsGiven.intValue());
         		sendMessage(sender, "Block place: " + ChatColor.WHITE
         				+ this.blockPlacePointsGiven.intValue());
         		sendMessage(sender, "Block break: " + ChatColor.WHITE
         				+ this.blockBreakPointsGiven.intValue());
         	} else if (args[1].compareToIgnoreCase("resetstats") == 0) {
         		this.chatPointsGiven = 0.0;
         		this.chatCharPointsGiven = 0.0;
         		this.commandPointsGiven = 0.0;
         		this.commandCharPointsGiven = 0.0;
         		this.movePointsGiven = 0.0;
         		this.blockPlacePointsGiven = 0.0;
         		this.blockBreakPointsGiven = 0.0;
         		sendMessage(sender, ChatColor.RED + "Stats reseted");
         	}
     	} else if (args.length == 1) {
     		if (hasPermission(sender, "activity.view.other", true)) {
     		   String playerName = matchSinglePlayer(sender, args[0]).getName();
     		   int activity = getActivity(playerName);
     		   sendMessage(sender, "Current activity of " + playerName + ": " + activityColor(activity) + activity + "%");
     		} else {
     			sendMessage(sender, ChatColor.RED + "You have no permission to see other's activity.");
     		}
     	}
     		
     	return true;
     	
     }
     
     /**
      * Checks permission for a CommandSender, OP defaults to true
      * 
      * @param player
      * @param node
      * @param noPermissionsReturn return value if permissions plugin isn't active
      * @return
      */
     public boolean hasPermission(CommandSender sender, String node, boolean noPermissionsReturn) {
     	return hasPermission((Player)sender, node, noPermissionsReturn);
     }
     
     /**
      * Checks permission for a Player, OP defaults to true
      * 
      * @param player
      * @param node
      * @param noPermissionsReturn return value if permissions plugin isn't active
      * @return
      */
     public boolean hasPermission(Player player, String node, boolean noPermissionsReturn) {
     	if (player.isOp())
     		return true;
     	
         if (Permissions != null) {
         	return Permissions.has(player, node);
         } else {
         	return noPermissionsReturn;
         }
     }
     
     /**
      * Returns the individual multiplier for a player
      * 
      * @param player
      * @param which
      * @return
      */
     public double getMultiplier(Player player, String which) {
         if (Permissions == null)
         	return 1.0;
         
     	final double multiplier = Permissions.getPermissionDouble(
     			player.getWorld().getName(), player.getName(), "activity.multiplier." + which);
         if (multiplier == -1.0) {
         	return 1.0;
         } else {
         	return multiplier;
         }
     }
     
     /**
      * Adds given amount of activity to the given playerName
      * 
      * @param playerName
      * @param activity
      */
     public void addActivity(String playerName, Double activity) {
     	
     	activity = config.pointMultiplier * activity;
     	playerName = playerName.toLowerCase();
     	if (playersActivities.get(this.currentSequence).containsKey(playerName)) {
     		activity += playersActivities.get(this.currentSequence).get(playerName);
     	}
     	
     	playersActivities.get(this.currentSequence).put(playerName, activity);
     	
     }
     
     /**
      * Calculates and returns activity of a given Player
      * 
      * @param player
      * @return
      */
     public int getActivity(Player player) {
     	return getActivity(player.getName());
     }
     
     /**
      * Calculates and returns activity of given playerName
      * 
      * @param playerName
      * @return
      */
     public int getActivity(String playerName) {
     	
     	playerName = playerName.toLowerCase();
     	
     	Iterator<Map<String, Double>> iterator = playersActivities.values().iterator();
     	
     	Double rawActivity = 0.0;
     	while (iterator.hasNext()) {
     		Map<String, Double> playersActivity = iterator.next();
     		if (playersActivity.containsKey(playerName)) {
     			rawActivity += playersActivity.get(playerName);
     		}
     	}
     	
     	int activity = (int)(rawActivity / playersActivities.size());
     	if (activity > 100) activity = 100;
     	
     	return activity;
     
     }
     
     /**
      * Sends a prefixed message to given CommandSender
      * 
      * @param sender
      * @param message
      */
     public void sendMessage(CommandSender sender, String message) {
     	sender.sendMessage(ChatColor.DARK_GRAY + "[Activity] " + ChatColor.GRAY + message);
     }
     
     /**
      * Sends a prefixed message to given Player
      * 
      * @param player
      * @param message
      */
     public void sendMessage(Player player, String message) {
     	player.sendMessage(ChatColor.DARK_GRAY + "[Activity] " + ChatColor.GRAY + message);
     }
     
     /**
      * Match a single online player which name contains filter
      * 
      * @param sender
      * @param filter
      * @return
      */
     public Player matchSinglePlayer(CommandSender sender, String filter) {
     	
     	filter = filter.toLowerCase();
     	for (Player player : getServer().getOnlinePlayers()) {
     		if (player.getName().toLowerCase().contains(filter)) {
     			return player;
     		}
     	}
     	
     	sender.sendMessage(ChatColor.RED + "No matching player found, matching yourself.");
     	return (Player) sender;
     	
     }
     
     /**
      * Initializes and starts the update timer
      */
     protected void startUpdateTimer() {
     	
     	updateTimer = new Timer();
         updateTimer.scheduleAtFixedRate(new TimerTask() {
             
         	public void run() {
         		
                 // Give players info
                 if (currentSequence % config.notificationSequence == 0) {
                 	for (Player player : getServer().getOnlinePlayers()) {
                     	int activity = getActivity(player.getName());
                 		sendMessage(player, "Your current activity is: " 
                 				+ activityColor(activity) + activity + "%");
                     }
                 }
                 
                 // Handle income
                 if (currentSequence % config.incomeSequence == 0 && config.incomeEnabled) {
                 	handleOnlineIncome();
                 }
                 
                 int nextSequence;
         		if (currentSequence == config.maxSequences) {
         			nextSequence = 1;
         		} else {
         			nextSequence = currentSequence + 1;
         		}
                 
                 playersActivities.put(nextSequence, new HashMap<String, Double>());
                 currentSequence = nextSequence;
             }
         	
         }, 0, (config.sequenceInterval * 1000L));
         
         logger.info("[HeavenActivity] Update timer started");
         
     }
     
     /**
      * Stops the update timer
      */
     protected void stopUpdateTimer() {
     	updateTimer.cancel();
     	logger.info("[HeavenActivity] Update timer stopped");
     }
 
     /**
      * Gives income to online players
      */
     @SuppressWarnings("static-access")
     protected void handleOnlineIncome() {
     	
     	if (playersActivities.size() == 0)
     		return;
     	
     	if (iConomy == null) {
     		logger.warning("[HeavenActivity] Want to give income, but iConomy isn't active! Skipping...");
     		return;
     	}
     	
     	for (Player player : getServer().getOnlinePlayers()) {
         	final int activity = getActivity(player);
         	if ((int)activity >= config.incomeMinActivity) {
 				Holdings balance = iConomy.getAccount(player.getName()).getHoldings();
                 
 				Double amount = config.incomeBaseValue 
                   + (((double)(activity - config.incomeTargetActivity) / (double)config.incomeActivityModifier) * config.incomeBaseValue)
                   + (balance.balance() * config.incomeBalanceMultiplier);
                 
 				if (amount > 0.0 || config.incomeAllowNegative) {
 				    balance.add(amount);
                 
                     sendMessage(player, "You got " + activityColor(activity) + iConomy.format(amount) 
                 		+ ChatColor.GRAY + " income for being " 
                 		+ activityColor(activity) + activity + "% " + ChatColor.GRAY + "active.");
                     sendMessage(player, "Your Balance is now: " + ChatColor.WHITE 
                 		+ iConomy.format(balance.balance()));
                     
                    return;
 				}
         	}
         	
         	sendMessage(player, ChatColor.RED + "You were too lazy, no income for you this time!");
         }
     	
     }
     
     protected ChatColor activityColor(int activity) {
     	
     	if (activity > 75) {
     		return ChatColor.GREEN;
     	} else if (activity < 25) {
     		return ChatColor.RED;
     	} else {
     		return ChatColor.YELLOW;
     	}
     	
     }
 
 }
