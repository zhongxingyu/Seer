 package com.github.mckillroy.accontrol;
 
 // import java.util.ArrayList;
 // import java.util.Arrays;
 // import java.util.Collection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.java.JavaPlugin;
 
 //import com.massivecraft.factions.Board;
 import com.massivecraft.factions.Factions;
 import com.massivecraft.factions.Faction;
 import com.minesworn.autocraft.Autocraft;
 
 /**
  * @author McYorlik
  * @version 0.1-dev
  */
 public final class P extends JavaPlugin {
 	// Plugin instance
 	/**
 	 * plugin is the single instance of the plugin P (singleton)
 	 */
 	public static P plugin;
 	public static ConfigAccessor MyConfigAccessor;
 	// Global Config Data
 
 	/**
 	 * When set to true the plugin will spit out a bunch of debug chatter at
 	 * runtime<br>
 	 * It gets configured from the config.yml
 	 */
 	public static boolean debug;
 	/**
 	 * The safetyzone is an additional space between the airships and the
 	 * forbidden zones such, that the airships can't float directly adjacent to
 	 * them.<br>
 	 * It gets configured from the config.yml
 	 */
 	public static int safetyzone;
 	/**
 	 * When PlayerFactionOK is set to true in the config.yml, the players own
 	 * faction is added to the allowed factions. This should usually not be
 	 * changed.
 	 */
 	public static boolean playerFactionOK;
 	/**
 	 * When AllAllowed is set to true in the config.yml, all factions different
 	 * from the players own faction are added to the allowed factions. This
 	 * should usually be set to false.
 	 */
 	public static boolean allAllowed;
 
 	/**
 	 * When AlliedFactionOK is set to true in the config.yml, factions allied to
 	 * the players own faction are added to the allowed factions. This defaults
 	 * to false.
 	 */
 	public static boolean alliedFactionOK;
 	/**
 	 * Holds the list of the Ids of allowed factions for airships. You'd usually
 	 * want to have Wilderness (Id=0)and maybe Warzones (Id=-2) in it. By
 	 * default Wilderness and Warzones are listed here.<br>
 	 * Gets loaded from config.yml
 	 */
 	public static List<String> allowedFactionIds;
 
 	// Globals needed by AcListener
 
 	/**
 	 * This enum hold values needed in the command preprocess listener in the
 	 * listener class
 	 */
 	public static enum aclvl2Cmds {
 		pilot, turn;
 	} // needed later for switch in command parsing of 2nd word in retrieved
 
 	/**
 	 * This is needed for parsing the commands later Commands: # <br>
 	 * showsettings: print settings # <br>
 	 * listallowed: list allowed factions # # <br>
 	 * setsafetyzone #: set size of safezone to # # <br>
 	 * addallowed #: add # as allowed faction for flight # <br>
 	 * removeallowed #: remove # as allowed faction for flight # <br>
 	 * saveconfig: save the current configuration enable: plugin //later # <br>
 	 * disable: plugin // later #
 	 * 
 	 */
 	public static enum commands {
 		setdebug, showsettings, listallowed, setsafetyzone, setallallowed, setplayerfactionok, setalliedfactionok, addallowed, removeallowed, saveconfig, reloadconfig
 	};
 
 	// Stuff
 	public static final Server SERVER = Bukkit.getServer();
 	public static final Logger LOG = SERVER.getLogger();
	public static final String CHAT_PREFIX = "AcControl: ";
 
 	// Singleton Constructor should be protected - but we wont do it here and
 	// just (must) trust the system .. :P
 	/**
 	 * Constructor for plugin singleton instance.
 	 */
 	public P() {
 		super();
 		plugin = this;
 	}
 
 	/**
 	 * Info sent to SERVER LOG with level INFO
 	 * 
 	 * @param text
 	 *            Text to LOG
 	 */
 	public static void info(String text) {
 		LOG.log(Level.INFO, P.CHAT_PREFIX.concat(" ").concat(text));
 	}
 
 	/**
 	 * Info sent to SERVER LOG with level SEVERE
 	 * 
 	 * @param text
 	 *            Text to LOG
 	 */
 	public static void severe(String text) {
 		LOG.log(Level.SEVERE, P.CHAT_PREFIX.concat(" ").concat(text));
 	}
 
 	/**
 	 * @param recipient
 	 *            Player to send chat message to
 	 * @param text
 	 *            Message text
 	 */
 	public static void msg(org.bukkit.entity.Player recipient, String text) {
 		recipient.sendMessage(P.CHAT_PREFIX.concat(" ").concat(text));
 	}
 
 	/**
 	 * @param text
 	 *            Text to broadcast in SERVER chat
 	 */
 	public static void broadcast(String text) {
 		P.SERVER.broadcastMessage(P.CHAT_PREFIX.concat(" ").concat(text));
 	}
 
 	/**
 	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
 	 *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if (cmd.getLabel().equalsIgnoreCase("accontrol")) {
 			// Do we have args? If not leave ...
 			if (args.length < 1) {
 				return false;
 			}
 			try {
 				switch (P.commands.valueOf(args[0])) {
 				case showsettings:
 					if (sender.hasPermission("accontrol.showsettings")) {
 						// showsettings
 						sender.sendMessage(CHAT_PREFIX + "Settings:");
 						sender.sendMessage(String.format(ChatColor.BLUE
 								+ "debug:" + ChatColor.GREEN + " %s", P.debug));
 						sender.sendMessage(String.format(ChatColor.BLUE
 								+ "safetyzone:" + ChatColor.GREEN + " %s",
 								P.safetyzone));
 						sender.sendMessage(String.format(ChatColor.BLUE
 								+ "allAllowed:" + ChatColor.GREEN + " %s",
 								P.allAllowed));
 						sender.sendMessage(String.format(ChatColor.BLUE
 								+ "playerFactionOK:" + ChatColor.GREEN + " %s",
 								P.playerFactionOK));
 						sender.sendMessage(String.format(ChatColor.BLUE
 								+ "alliedFactionOK:" + ChatColor.GREEN + " %s",
 								P.alliedFactionOK));
 						sender.sendMessage(String.format(ChatColor.BLUE
 								+ "allowedFactionIds:" + ChatColor.GREEN
 								+ " %s", P.allowedFactionIds));
 						// sender.sendMessage(String.format(" :%s",P.));
 						// showsettings end
 						return true;
 					}
 					sender.sendMessage(CHAT_PREFIX + ChatColor.RED
 							+ "You don't have permission for showsettings.");
 					return true;
 				case listallowed:
 					if (sender.hasPermission("accontrol.listallowed")) {
 						// listallowed
 						sender.sendMessage(ChatColor.BLUE + CHAT_PREFIX
 								+ ChatColor.AQUA
 								+ "Factions whose lands can be flight over:");
 						String myOutput = "";
 						for (String element : P.allowedFactionIds) {
 							myOutput = myOutput
 									+ Factions.i.get(element).getTag() + " ";
 						}
 						sender.sendMessage(myOutput);
 						// listallowed end
 						return true;
 					}
 					sender.sendMessage(CHAT_PREFIX + ChatColor.RED
 							+ "You don't have permission for listallowed.");
 					return true;
 				case setsafetyzone:
 					if (sender.hasPermission("accontrol.setsafetyzone")) {
 						// setsafetyzone
 						P.safetyzone = Integer.decode(args[1]);
 						sender.sendMessage(String.format(
 								"Safetyzone set to %s", P.safetyzone));
 						// setsafetyzone end
 						return true;
 					}
 					sender.sendMessage(CHAT_PREFIX + ChatColor.RED
 							+ "You don't have permission for setsafezone.");
 					return true;
 				case addallowed:
 					if (sender.hasPermission("accontrol.addallowed")) {
 						// addallowed
 						sender.sendMessage("addallowed");
 						Faction myFaction = Factions.i.getBestTagMatch(args[1]);
 						if (myFaction == null) {
 							sender.sendMessage("Faction not found - command cancelled !");
 							return true;
 						}
 						// is the faction already allowed?
 						if (P.allowedFactionIds.contains(myFaction.getId())) {
 							sender.sendMessage(String
 									.format("%s is already allowed - command cancelled.",
 											myFaction.getTag()));
 							return true;
 						}
 						// if not we can add it - if it exists
 						P.allowedFactionIds.add(myFaction.getId());
 						sender.sendMessage(String
 								.format("Added %s to list of allowed factions. Don't forget to save!",
 										myFaction.getTag()));
 						// addallowed end
 						return true;
 					}
 					sender.sendMessage(CHAT_PREFIX + ChatColor.RED
 							+ "You don't have permission for addallowed.");
 					return true;
 				case removeallowed:
 					if (sender.hasPermission("accontrol.removeallowed")) {
 						// removeallowed
 						sender.sendMessage("removeallowed");
 						Faction myFaction = Factions.i.getBestTagMatch(args[1]);
 						if (myFaction == null) {
 							sender.sendMessage("Faction not found - command cancelled !");
 							return true;
 						}
 						if (!P.allowedFactionIds.contains(myFaction.getId())) {
 							sender.sendMessage(String
 									.format("%s is not allowed already - command cancelled.",
 											myFaction.getTag()));
 							return true;
 						}
 
 						P.allowedFactionIds.removeAll(Collections
 								.singleton(myFaction.getId()));
 						P.MyConfigAccessor.getConfig().set("AllowedFactionIds",
 								P.allowedFactionIds);
 						sender.sendMessage(String
 								.format("Removed %s from list of allowed factions. Don't forget to save!",
 										myFaction.getTag()));
 
 						// removeallowed end
 						return true;
 					}
 					sender.sendMessage(CHAT_PREFIX + ChatColor.RED
 							+ "You don't have permission for removeallowed.");
 					return true;
 
 				case saveconfig:
 					if (sender.hasPermission("accontrol.saveconfig")) {
 						// / start
 						saveConfig();
 						sender.sendMessage("Configuration saved");
 						return true;
 						// / end
 					}
 					sender.sendMessage(CHAT_PREFIX + ChatColor.RED
 							+ "You don't have permission for saveconfig.");
 					return true;
 				case reloadconfig:
 					if (sender.hasPermission("accontrol.reloadconfig")) {
 						// start
 						reloadConfig();
 						sender.sendMessage("Configuration reloaded. Check with /acc showsettings");
 						return true;
 						// end
 					}
 					sender.sendMessage(CHAT_PREFIX + ChatColor.RED
 							+ "You don't have permission for reloadconfig.");
 					return true;
 				case setdebug:
 					if (sender.hasPermission("accontrol.setdebug")) {
 						// start
 						P.debug = Boolean.parseBoolean(args[1]);
 						P.MyConfigAccessor.getConfig().set("debug", P.debug);
 						sender.sendMessage(String.format(
 								"debug set to %s. Don't forget to save!",
 								P.debug));
 						return true;
 						// end
 					}
 					sender.sendMessage(CHAT_PREFIX + ChatColor.RED
 							+ "You don't have permission for setdebug.");
 					return true;
 				case setallallowed:
 					if (sender.hasPermission("accontrol.setallowed")) {
 						// start
 						P.allAllowed = Boolean.parseBoolean(args[1]);
 						P.MyConfigAccessor.getConfig().set(
 								"factions.AllAllowed", P.allAllowed);
 						sender.sendMessage(String.format(
 								"allAllowed set to %s. Don't forget to save!",
 								P.allAllowed));
 						return true;
 						// end
 					}
 					sender.sendMessage(CHAT_PREFIX + ChatColor.RED
 							+ "You don't have permission for setallowed.");
 					return true;
 				case setplayerfactionok:
 					if (sender.hasPermission("accontrol.setplayerfactionok")) {
 						// start
 						P.playerFactionOK = Boolean.parseBoolean(args[1]);
 						P.MyConfigAccessor.getConfig().set(
 								"factions.PlayerFactionOK", P.playerFactionOK);
 						sender.sendMessage(String
 								.format("playerFactionOK set to %s. Don't forget to save!",
 										P.playerFactionOK));
 						return true;
 						// end
 					}
 					sender.sendMessage(CHAT_PREFIX
 							+ ChatColor.RED
 							+ "You don't have permission for setplayerfactionok.");
 					return true;
 				case setalliedfactionok:
 					if (sender.hasPermission("accontrol.setplayerfactionok")) {
 						// start
 						P.alliedFactionOK = Boolean.parseBoolean(args[1]);
 						P.MyConfigAccessor.getConfig().set(
 								"factions.AlliedFactionOK", P.alliedFactionOK);
 						sender.sendMessage(String
 								.format("alliedFactionOK set to %s. Don't forget to save!",
 										P.alliedFactionOK));
 						return true;
 						// end
 					}
 					sender.sendMessage(CHAT_PREFIX
 							+ ChatColor.RED
 							+ "You don't have permission for setplayerfactionok.");
 					return true;
 				default:
 					return false;
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				// e.printStackTrace();
 				sender.sendMessage(CHAT_PREFIX + ChatColor.RED.toString()
 						+ "Wrong argument!");
 
 				if (P.debug) {
 					sender.sendMessage(String.format("Exception: %s",
 							e.getCause()));
 					sender.sendMessage(String.format("Exception: %s",
 							(Object[]) e.getStackTrace()));
 				}
 
 				return false;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Intializing configuration stuff
 	 */
 	@Override
 	public void reloadConfig() {
 		MyConfigAccessor.reloadConfig();
 		// If above fails silently get Config from file:
 		// Assign Globals from Config
 			P.debug = MyConfigAccessor.getConfig().getBoolean("debug");
 			P.safetyzone = MyConfigAccessor.getConfig().getInt("safetyzone");
 			P.playerFactionOK = MyConfigAccessor.getConfig().getBoolean(
 					"factions.PlayerFactionOK");
 			P.allAllowed = MyConfigAccessor.getConfig().getBoolean(
 					"factions.AllAllowed");
 			P.alliedFactionOK = MyConfigAccessor.getConfig().getBoolean(
 					"factions.AlliedFactionOK");
 			P.allowedFactionIds = MyConfigAccessor.getConfig().getStringList(
 					"factions.AllowedFactionIds");
 			return ;
 		
 	}
 
 	@Override
 	public void saveConfig() {
 
 		MyConfigAccessor.getConfig().set("factions.AllowedFactionIds",
 				P.allowedFactionIds);
 		MyConfigAccessor.getConfig().set("factions.AlliedFactionOK",
 				P.alliedFactionOK);
 		MyConfigAccessor.getConfig().set("factions.PlayerFactionOK",
 				P.playerFactionOK);
 		MyConfigAccessor.getConfig().set("factions.AllAllowed", P.allAllowed);
 		MyConfigAccessor.getConfig().set("safetyzone", P.safetyzone);
 		MyConfigAccessor.getConfig().set("debug", P.debug);
 
 		MyConfigAccessor.saveConfig();
 	}
 
 	// Autocraft plugin related functions
 	/**
 	 * @param MyPlayer
 	 *            Player to check for being pilot or not
 	 * @return true if player is piloting a ship
 	 */
 	public static boolean isPilot(Player MyPlayer) {
 		if (Autocraft.shipmanager.ships.containsKey(MyPlayer.getName())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
 	 */
 	@Override
 	public void onEnable() {
 		// initializing config
 		// Config access
 		MyConfigAccessor = new ConfigAccessor(plugin, "config.yml");
 		reloadConfig();
 		// registering Listener Class
 		getServer().getPluginManager().registerEvents(new AcListener(), this);
		info("Plugin enabled !");
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
 	 */
 	@Override
 	public void onDisable() {
 		saveConfig();
 		HandlerList.unregisterAll(this);
		info("Plugin disabled");
 	}
 
 }
