 package com.cole2sworld.colebans;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import com.nijiko.permissions.PermissionHandler;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.cole2sworld.colebans.commands.CBCommand;
 import com.cole2sworld.colebans.commands.CommandHandler;
 import com.cole2sworld.colebans.framework.GlobalConf;
 import com.cole2sworld.colebans.framework.PlayerOfflineException;
 import com.cole2sworld.colebans.handlers.BanHandler;
 
 /**
  * The main class for ColeBans.
  * 
  * @since v1 Apricot
  */
 public final class ColeBansPlugin extends JavaPlugin {
 	/**
 	 * Are we in debug mode? (If this is turned on when compiled, it means the
 	 * build is a debug build)
 	 */
 	public static boolean		debug	= false;
 	/**
 	 * The Minecraft log.
 	 */
 	public static final Logger	LOG		= Logger.getLogger("Minecraft");
 	public static final String	PREFIX	= "[ColeBans] ";
 	
 	public static void debug(final String msg) {
 		if (debug) {
 			String caller = "??";
 			try {
 				throw new Exception("Getting caller");
 			} catch (final Exception e) {
 				try {
 					caller = Class.forName(e.getStackTrace()[1].getClassName()).getSimpleName();
 				} catch (final ClassNotFoundException e1) {
 					// we don't really care
 				}
 			}
 			System.out.println(PREFIX + "[DEBUG] [" + caller + "] " + msg);
 		}
 	}
 	
 	public static Map<String, String> getBanHandlerInitArgs() {
 		final Map<String, String> data = new HashMap<String, String>(15);
 		data.put("username", GlobalConf.get("mysql.user").asString());
 		data.put("password", GlobalConf.get("mysql.pass").asString());
 		data.put("host", GlobalConf.get("mysql.host").asString());
 		data.put("prefix", GlobalConf.get("mysql.prefix").asString());
 		data.put("db", GlobalConf.get("mysql.db").asString());
 		data.put("yaml", GlobalConf.get("yaml.fileName").asString());
 		data.put("apiKey", GlobalConf.get("mcbans.apiKey").asString());
 		return data;
 	}
 	
 	/**
 	 * Called when something really bad happens.
 	 */
 	protected static void onFatal(final String reason) throws Exception {
 		throw new Exception(reason);
 	}
 	
 	/**
 	 * The Permissions 3/2 (or bridge) that we will use for permissions.
 	 */
	public PermissionHandler		permissionsHandler	= null;
 	/**
 	 * The instance of Main, for accessing non-static methods.
 	 */
	public static ColeBansPlugin	instance;
 	
	public IPLogManager				ipLog;
 	/**
 	 * The server that ColeBans got on startup.
 	 */
	public Server					server;
 	
 	/**
 	 * The banhandler that will be used for all actions.
 	 */
	public BanHandler				banHandler;
 	
 	/**
 	 * @param player
 	 *            Player to check (name)
 	 * @param permissionNode
 	 *            Node to check
 	 * @return If there is a permissionsHandler, whether or not the given player
 	 *         has the node. If there isn't, if the player is an operator.
 	 */
 	public boolean hasPermission(final Player player, final String permissionNode) {
 		if (permissionsHandler == null)
 			return player.hasPermission(new Permission(permissionNode)) || player.isOp();
 		return permissionsHandler.has(player, permissionNode);
 	}
 	
 	/**
 	 * Kicks a player out of the game, with a fancy effect if enabled.
 	 * 
 	 * @param player
 	 *            The player to kick (name)
 	 * @param reason
 	 *            The reason for the kick (shown to the victim)
 	 * @throws PlayerOfflineException
 	 *             If the player is offline
 	 */
 	public void kickPlayer(final String player, final String reason) throws PlayerOfflineException {
 		final Player playerObj = server.getPlayer(player);
 		if ((playerObj != null) && playerObj.isOnline()) {
 			if (GlobalConf.get("fancyEffects").asBoolean()) {
 				final World world = playerObj.getWorld();
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 1);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 2);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 3);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 4);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 5);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 6);
 			}
 			if (reason != null) {
 				playerObj.kickPlayer(ChatColor.valueOf(GlobalConf.get("kickColor").asString())
 						+ "KICKED: " + reason);
 			} else {
 				playerObj.kickPlayer(ChatColor.valueOf(GlobalConf.get("kickColor").asString())
 						+ "Kicked!");
 			}
 			if (GlobalConf.get("announceBansAndKicks").asBoolean() && (reason != null)) {
 				server.broadcastMessage(ChatColor.valueOf(GlobalConf.get("kickColor").asString())
 						+ player + " was kicked! [" + reason + "]");
 			} else if (GlobalConf.get("announceBansAndKicks").asBoolean()) {
 				server.broadcastMessage(ChatColor.valueOf(GlobalConf.get("kickColor").asString())
 						+ player + " was kicked!");
 			}
 		} else
 			throw new PlayerOfflineException(player + " is offline!");
 	}
 	
 	/**
 	 * Manages the dynamic command handler and the static command handler.
 	 */
 	@Override
 	public boolean onCommand(final CommandSender sender, final Command cmd, final String cmdLabel,
 			final String[] args) {
 		debug("Executing command " + cmdLabel);
 		if (cmdLabel.equalsIgnoreCase("cb") || cmdLabel.equalsIgnoreCase("colebans")) {
 			debug("It's /cb");
 			if (args.length < 1) return false;
 			try {
 				String cmdName = args[0].substring(1);
 				debug("cmdName = " + cmdName);
 				final Character firstChar = args[0].charAt(0);
 				debug("firstChar = " + firstChar);
 				cmdName = Character.toUpperCase(firstChar) + cmdName.toLowerCase(Locale.ENGLISH);
 				debug("cmdName = " + cmdName);
 				final Object rawObject = Class.forName(
 						"com.cole2sworld.colebans.commands." + cmdName).newInstance();
 				debug("rawObject = " + rawObject.getClass().getSimpleName());
 				if (rawObject instanceof CBCommand) {
 					debug("rawObject is a CBCommand");
 					final CBCommand cmdObj = (CBCommand) rawObject;
 					final Vector<String> newArgs = new Vector<String>(args.length);
 					for (int i = 1; i < args.length; i++) {
 						newArgs.add(args[i]);
 					}
 					final String error = cmdObj.run(newArgs.toArray(new String[newArgs.size()]),
 							sender);
 					if (error != null) {
 						sender.sendMessage(error);
 					}
 					return true;
 				}
 			} catch (final ClassNotFoundException e) {
 				debug("ClassNotFoundException (invalid subcommand)");
 			} catch (final InstantiationException e) {
 				debug("InstantiationException (???)");
 			} catch (final IllegalAccessException e) {
 				debug("IllegalAccessException (non-public class)");
 			} catch (final Exception e) {
 				return true;
 			}
 		} else {
 			debug("Requires static handling. Passing to CommandHandler");
 			return CommandHandler.onCommand(sender, cmd, cmdLabel, args);
 		}
 		sender.sendMessage(ChatColor.RED + "Invalid sub-command.");
 		return true;
 	}
 	
 	/**
 	 * Called when the plugin is disabled.
 	 */
 	@Override
 	public void onDisable() {
 		if (banHandler != null) {
 			banHandler.onDisable();
 		}
 		GlobalConf.save();
 		System.out.println(PREFIX + "Disabled.");
 	}
 	
 	/**
 	 * Registers events, gets the config, pulls the banhandler, and all that
 	 * good stuff you need to do when initializing.
 	 */
 	@Override
 	public void onEnable() {
		instance = this;
 		try {
 			System.out.println(PREFIX + "Initalizing...");
 			server = getServer();
 			final PluginManager pm = server.getPluginManager();
 			System.out.println(PREFIX + "Loading config and ban handler...");
 			long oldtime = System.currentTimeMillis();
 			GlobalConf.load();
 			IPLogManager.initalize();
 			if (debug) {
 				LOG.warning(PREFIX + "Using a debug build. Expect many messages");
 			}
 			try {
 				banHandler = Util.lookupHandler(GlobalConf.get("banHandler").asString());
 			} catch (final ClassNotFoundException e) {
 				onFatal("Non-existant ban handler given in config file");
 			} catch (final SecurityException e) {
 				onFatal("Somehow, a SecurityException occurred. Plugin conflict?");
 			} catch (final NoSuchMethodException e) {
 				onFatal("Bad ban handler given in config file! (NSME)");
 			} catch (final IllegalArgumentException e) {
 				onFatal("Bad ban handler given in config file! (IAE1)");
 			} catch (final IllegalAccessException e) {
 				onFatal("Bad ban handler given in config file! (IAE2)");
 			} catch (final InvocationTargetException e) {
 				onFatal("Bad ban handler given in config file! (ITE)");
 			} catch (final NullPointerException e) {
 				onFatal("Bad ban handler given in config file! (NPE)");
 			} catch (final ClassCastException e) {
 				onFatal("Bad ban handler given in config file! (CCE)");
 			}
 			ActionLogManager.initialize();
 			long newtime = System.currentTimeMillis();
 			System.out.println(PREFIX + "Done. Took " + (newtime - oldtime) + " ms.");
 			System.out.println(PREFIX + "Registering events...");
 			oldtime = System.currentTimeMillis();
 			pm.registerEvents(new EventListener(), this);
 			pm.registerEvents(new BanhammerListener(), this);
 			pm.registerEvents(new RestrictionListener(), this);
 			pm.registerEvents(new IPLogManager(), this);
 			newtime = System.currentTimeMillis();
 			System.out.println(PREFIX + "Done. Took " + (newtime - oldtime) + " ms.");
 		} catch (final Exception e) {
 			// TMTRAINER effect
 			// http://bulbapedia.bulbagarden.net/wiki/TMTRAINER_effect
 			LOG.severe(PREFIX + "▄▒□◊▲□TMTRAINER◙░░▓▄'s TM55 is frozen solid!");
 			LOG.severe(PREFIX + "▪▪▪▪ is hurt by the burn!");
 			LOG.severe(PREFIX + "▪▪▪▪ fainted!");
 			LOG.severe(PREFIX + "Aborting operation: " + e.getMessage());
 			setEnabled(false);
 		}
 	}
 	
 }
