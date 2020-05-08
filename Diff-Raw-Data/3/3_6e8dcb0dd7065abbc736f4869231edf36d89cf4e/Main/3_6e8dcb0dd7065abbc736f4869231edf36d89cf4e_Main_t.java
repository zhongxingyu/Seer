 package com.cole2sworld.ColeBans;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.cole2sworld.ColeBans.EventListener;
 import com.cole2sworld.ColeBans.commands.CBCommand;
 import com.cole2sworld.ColeBans.commands.CommandHandler;
 import com.cole2sworld.ColeBans.framework.PlayerOfflineException;
 import com.cole2sworld.ColeBans.handlers.BanHandler;
 import com.nijiko.permissions.PermissionHandler;
 
 /**
  * The main class for ColeBans.
  *
  */
 public class Main extends JavaPlugin {
 	/**
 	 * The Permissions 3/2 (or bridge) that we will use for permissions.
 	 */
     public PermissionHandler permissionsHandler = null;
     /**
      * The instance of Main, for accessing non-static methods.
      */
 	public static Main instance;
 	/**
 	 * The server that ColeBans got on startup.
 	 */
 	public Server server;
 	/**
 	 * The banhandler that will be used for all actions.
 	 */
 	public BanHandler banHandler;
 
 	/**
 	 * Creates a new ColeBans Main class.
 	 * <i>Do not use. Only the Bukkit server implementation should instantiate Main. If you need an instance of Main, use Main.instance</i>
 	 */
 
 	public Main() {
 		instance = this;
 	}
 	
 	/**
 	 * Called when the plugin is disabled.
 	 */
 	@Override
 	public void onDisable() {
		if (banHandler != null)
			banHandler.onDisable();
 		System.out.println(GlobalConf.logPrefix+"Disabled.");
 	}
 
 	/**
 	 * Registers events, gets the config, pulls the banhandler, and all that good stuff you need to do when initializing.
 	 */
 	@Override
 	public void onEnable() {
 		System.out.println(GlobalConf.logPrefix+"Initalizing...");
 		server = getServer();
 		PluginManager pm = server.getPluginManager();
 		System.out.println(GlobalConf.logPrefix+"Loading config and ban handler...");
 		long oldtime = System.currentTimeMillis();
 		GlobalConf.conf = getConfig();
 		GlobalConf.loadConfig();
 		HashMap<String, String> data = new HashMap<String, String>(15);
 		data.put("username", GlobalConf.Sql.user);
 		data.put("password", GlobalConf.Sql.pass);
 		data.put("host", GlobalConf.Sql.host);
 		data.put("port", GlobalConf.Sql.port);
 		data.put("prefix", GlobalConf.Sql.prefix);
 		data.put("db", GlobalConf.Sql.db);
 		// Reflection :(
 		try {
 			Class<?> rawClass = Class.forName(GlobalConf.Advanced.pkg+"."+GlobalConf.banHandlerConf+GlobalConf.Advanced.suffix);
 			if (rawClass.isAssignableFrom(BanHandler.class)) {
 				Class<?>[] arguments = {Map.class};
 				banHandler = (BanHandler) rawClass.getDeclaredMethod("onEnable", arguments).invoke(null, data);
 			}
 		} catch (ClassNotFoundException e) {
 			Logger.getLogger("Minecraft").severe(GlobalConf.logPrefix+"Non-existant ban handler given in config file! Aborting operation.");
 			onFatal();
 		} catch (SecurityException e) {
 			Logger.getLogger("Minecraft").severe(GlobalConf.logPrefix+"Somehow, a SecurityException occurred. Plugin conflict? Aborting operation.");
 			onFatal();
 		} catch (NoSuchMethodException e) {
 			Logger.getLogger("Minecraft").severe(GlobalConf.logPrefix+"Bad ban handler given in config file! Aborting operation.");
 			onFatal();
 		} catch (IllegalArgumentException e) {
 			Logger.getLogger("Minecraft").severe(GlobalConf.logPrefix+"Bad ban handler given in config file! Aborting operation.");
 			onFatal();
 		} catch (IllegalAccessException e) {
 			Logger.getLogger("Minecraft").severe(GlobalConf.logPrefix+"Bad ban handler given in config file! Aborting operation.");
 			onFatal();
 		} catch (InvocationTargetException e) {
 			Logger.getLogger("Minecraft").severe(GlobalConf.logPrefix+"Bad ban handler given in config file! Aborting operation.");
 			onFatal();
 		} catch (NullPointerException e) {
 			Logger.getLogger("Minecraft").severe(GlobalConf.logPrefix+"Bad ban handler given in config file! Aborting operation.");
 			onFatal();
 		} catch (ClassCastException e) {
 			Logger.getLogger("Minecraft").severe(GlobalConf.logPrefix+"Bad ban handler given in config file! Aborting operation.");
 			onFatal();
 		}
 		long newtime = System.currentTimeMillis();
 		System.out.println(GlobalConf.logPrefix+"Done. Took "+(newtime-oldtime)+" ms.");
 		System.out.println(GlobalConf.logPrefix+"Registering events...");
 		oldtime = System.currentTimeMillis();
 		pm.registerEvents(new EventListener(), this);
 		newtime = System.currentTimeMillis();
 		System.out.println(GlobalConf.logPrefix+"Done. Took "+(newtime-oldtime)+" ms.");
 	}
 	
 	/**
 	 * Manages the dynamic command handler and the static command handler.
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 		if (cmdLabel.equalsIgnoreCase("cb")) {
 			if (args.length < 2) return false;
 			else {
 				try {
 					String cmdName = args[0].substring(1);
 					Character firstChar = args[0].charAt(1);
 					cmdName = Character.toUpperCase(firstChar)+cmdName.toLowerCase();
 					Object rawObject = Class.forName("com.cole2sworld.ColeBans.commands."+cmdName).newInstance();
 					if (rawObject instanceof CBCommand) {
 						CBCommand cmdObj = (CBCommand) rawObject;
 						Vector<String> newArgs = new Vector<String>(args.length);
 						for (int i = 1; i<args.length; i++) {
 							newArgs.add(args[i]);
 						}
 						String error = cmdObj.run(newArgs.toArray(new String[newArgs.size()]), sender);
 						if (error != null) {
 							sender.sendMessage(error);
 						}
 						return true;
 					}
 				}
 				catch (ClassNotFoundException e) {
 				} catch (InstantiationException e) {
 				} catch (IllegalAccessException e) {}
 			}
 		}
 		else {
 			return CommandHandler.onCommand(sender, cmd, cmdLabel, args);
 		}
 		sender.sendMessage(ChatColor.RED+"Invalid sub-command.");
 		return true;
 	}
 	
 	/**
 	 * Kicks a player out of the game, with a fancy effect if enabled.
 	 * @param player The player to kick (name)
 	 * @param reason The reason for the kick (shown to the victim)
 	 * @throws PlayerOfflineException If the player is offline
 	 */
 	public void kickPlayer(String player, String reason) throws PlayerOfflineException {
 		Player playerObj = server.getPlayer(player);
 		if (playerObj != null) {
 			if (GlobalConf.fancyEffects) {
 				World world = playerObj.getWorld();
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 1);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 2);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 3);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 4);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 5);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 6);
 			}
 			playerObj.kickPlayer(ChatColor.valueOf(GlobalConf.kickColor)+"KICKED: "+reason);
 			if (GlobalConf.announceBansAndKicks) server.broadcastMessage(ChatColor.valueOf(GlobalConf.kickColor)+player+" was kicked! ["+reason+"]");
 		}
 		else throw new PlayerOfflineException(player+" is offline!");
 	}
 	/**
 	 * Check if the number "check" should be pluralized in speech with an s.
 	 * Returns "s" if yes, "" if no when plural is true, "are" if yes or "is" if no when plural is false.
 	 * <br/><b>Example:</b><br/>
 	 * "There "+Main.getPlural(apples, false)+" "+apples+" apple"+Main.getPlural(apples, true)+" in the bowl."<br/>
 	 * <br/>
 	 * If 'apples' was 4, the string would be "There are 4 apples in the bowl."<br/>
 	 * If 'apples' was 1, the string would be "There is 1 apple in the bowl."<br/>
 	 * If 'apples' was 0, the string would be "There are 0 apples in the bowl."
 	 * @param check The number to check
 	 * @param plural Return s?
 	 * @return Plural
 	 */
 	public static String getPlural(long check, boolean plural) {
 		boolean isPlural = check < 0 || check == 0 || check > 1;
 		if (plural && isPlural) return "s";
 		if (plural && !isPlural) return "";
 		if (!plural && isPlural) return "are";
 		return "is";
 	}
 	/**
 	 * @param player Player to check (name)
 	 * @param permissionNode Node to check
 	 * @return If there is a permissionsHandler, whether or not the given player has the node. If there isn't, if the player is an operator.
 	 */
     public boolean hasPermission(Player player, String permissionNode)
     {
     	if (permissionsHandler == null) return player.isOp();
         return permissionsHandler.has(player, permissionNode);
     }
 
     /**
      * Called when something really bad happens.
      */
 	protected void onFatal() {
 		this.onDisable();
 		this.setEnabled(false);
 	}
 
 }
