 package com.cole2sworld.ColeBans;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.server.ServerListener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.cole2sworld.ColeBans.framework.MethodNotSupportedException;
 import com.cole2sworld.ColeBans.framework.PlayerAlreadyBannedException;
 import com.cole2sworld.ColeBans.framework.PlayerNotBannedException;
 import com.cole2sworld.ColeBans.framework.PlayerOfflineException;
 import com.cole2sworld.ColeBans.handlers.BanData;
 import com.cole2sworld.ColeBans.handlers.BanHandler;
 import com.cole2sworld.ColeBans.handlers.MySQLBanHandler;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 
 public class Main extends JavaPlugin {
     public PermissionHandler permissionsHandler = null;
 	public static Server server;
 	public static FileConfiguration conf;
 	public static ConfigurationSection settings;
 	public static boolean allowTempBans = true;
 	public static Material banHammer = Material.BLAZE_ROD;
 	public static String banMessage = "You are banned for %reason!";
 	public static String tempBanMessage = "You are tempbanned! %time seconds remaining!";
 	public static boolean allowBanhammer = true;
 	public static boolean fancyEffects = true;
 	public static String banColor = "DARK_RED";
 	public static String kickColor = "YELLOW";
 	public static String tempBanColor = "RED";
 	public static BanHandler banHandler;
 	public static boolean announceBansAndKicks = true;
 	public static boolean mcbansNodes = false;
 	public static String logPrefix = "[ColeBans] ";
 	public static Main instance;
 	public static String banHandlerConf = "MySQL";
 	public static class sql {
 		public static ConfigurationSection section;
 		public static String user = "minecraft";
 		public static String pass = "password";
 		public static String host = "localhost";
 		public static String port = "3306";
 		public static String db = "minecraft";
 		public static String prefix = "cb_";
 	}
 
 	public Main() {
 		instance = this;
 	}
 	
 	@Override
 	public void onDisable() {
 		banHandler.onDisable();
 		System.out.println(logPrefix+"Disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		System.out.println(logPrefix+"Initalizing...");
 		server = getServer();
 		PluginManager pm = server.getPluginManager();
 		System.out.println(logPrefix+"Loading config & ban handler...");
 		long oldtime = System.currentTimeMillis();
 		conf = getConfig();
 		loadConfig();
 		banHandler = new MySQLBanHandler(sql.user, sql.pass, sql.host, sql.port, logPrefix, sql.db);
 		long newtime = System.currentTimeMillis();
 		System.out.println(logPrefix+"Done. Took "+(newtime-oldtime)+" ms.");
 		System.out.println(logPrefix+"Registering events...");
 		oldtime = System.currentTimeMillis();
 		pm.registerEvent(Type.PLAYER_PRELOGIN, new CBPlayerListener(), Priority.Highest, this);
 		pm.registerEvent(Type.PLUGIN_ENABLE, new ServerListener() {
 		    public void onPluginEnable(PluginEnableEvent event) {
 		    	if (Main.instance.permissionsHandler == null) {
 		    		Plugin permissions = Main.instance.getServer().getPluginManager().getPlugin("Permissions");
 		    		if (permissions != null) {
 		    			if (permissions.isEnabled() & permissions.getClass().getName().equals("com.nijikokun.bukkit.Permissions.Permissions")) {
 		    				Main.instance.permissionsHandler = ((Permissions)permissions).getHandler();
 		    				System.out.println(logPrefix+"Hooked into Nijikokun-like permissions.");
 		    			}
 		    		}
 		    	}
 		    }
 		}, Priority.Monitor, this);
 		newtime = System.currentTimeMillis();
 		System.out.println(logPrefix+"Done. Took "+(newtime-oldtime)+" ms.");
 	}
 	
 	public void loadConfig() {
		File confFile = new File("./plugins/ColeBans/config.yml");
 		try {
 			if (confFile.exists()) {
 				conf.load(confFile);
 				settings = conf.getConfigurationSection("settings");
 				allowTempBans = settings.getBoolean("allowTempBans");
 				banHammer = Material.getMaterial(settings.getString("banHammer"));
 				banMessage = settings.getString("banMessage");
 				tempBanMessage = settings.getString("tempBanMessage");
 				allowBanhammer = settings.getBoolean("allowBanhammer");
 				fancyEffects = settings.getBoolean("fancyEffects");
 				banColor = settings.getString("banColor");
 				kickColor = settings.getString("kickColor");
 				tempBanColor = settings.getString("tempBanColor");
 				announceBansAndKicks = settings.getBoolean("announceBansAndKicks");
 				mcbansNodes = settings.getBoolean("mcbansNodes");
 				logPrefix = settings.getString("logPrefix")+" ";
 				sql.section = settings.getConfigurationSection("mysql");
 				sql.user = sql.section.getString("user");
 				sql.pass = sql.section.getString("pass");
 				sql.host = sql.section.getString("host");
 				sql.port = sql.section.getString("port");
 				sql.db = sql.section.getString("db");
 				sql.prefix = sql.section.getString("prefix");
 			}
 			else {
 				confFile.mkdirs();
 				confFile.createNewFile();
 				if (confFile.canWrite()) {
 					System.out.println("[ColeBans] No config file exists, generating.");
 					FileOutputStream fos = new FileOutputStream(confFile);
 					String defaultConfig = "settings:\n"+
 							"banHammer: BLAZE_ROD\n"+
 							"allowBanhammer: true\n"+
 							"allowTempBans: true\n"+
 							"banMessage: You are banned for %reason!\n"+
 							"tempBanMessage: You are tempbanned! %time minute%plural remaining!\n"+
 							"fancyEffects: true\n"+
 							"banColor: DARK_RED\n"+
 							"kickColor: YELLOW\n"+
 							"tempBanColor: RED\n"+
 							"announceBansAndKicks: true\n"+
 							"mcbansNodes: true\n"+
 							"logPrefix: [ColeBans]\n"+
 							"#banHandler can be MySQL, MCBans, YAML, or JSON.\n"+
 							"banHandler: MySQL\n"+
 							"mysql:\n"+
 							"    user: root\n"+
 							"    pass: pass\n"+
 							"    host: localhost\n"+
 							"    port: 3306\n"+
 							"    db: minecraft\n"+
 							"    prefix: cb_\n"+
 							"mcbans:\n"+
 							"    ###### THIS LINE IS VERY VERY IMPORTANT IF YOU CHOSE MCBANS FOR THE BAN HANDLER ######\n"+
 							"    apiKey:\n"+
 							"yaml:\n"+
 							"    fileName: banlist.yml\n"+
 							"json:\n"+
 							"    fileName: banlist.json";
 					fos.write(defaultConfig.getBytes());
 					loadConfig();
 					return;
 				}
 				else {
 					Logger.getLogger("Minecraft").severe("[ColeBans] COULD NOT LOAD WORKING CONFIG FILE. Aborting operation.");
 					this.setEnabled(false);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 		String error = "An unspecified error has occured while running this command.";
 		boolean canBan = false;
 		boolean canTempBan = false;
 		boolean canUnBan = false;
 		boolean canKick = false;
 		boolean canLookup = false;
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			if (mcbansNodes) {
 				canBan = hasPermission(player, "mcbans.ban") | hasPermission(player, "mcbans.ban.local") | hasPermission(player, "mcbans.ban.global");
 				canTempBan = hasPermission(player, "mcbans.ban.temp");
 				canUnBan = hasPermission(player, "mcbans.unban");
 				canLookup = hasPermission(player, "mcbans.lookup");
 				canKick = hasPermission(player, "mcbans.kick");
 			}
 			else {
 				canBan = hasPermission(player, "colebans.ban");
 				canTempBan = hasPermission(player, "colebans.tempban");
 				canUnBan = hasPermission(player, "colebans.unban");
 				canLookup = hasPermission(player, "colebans.lookup") | hasPermission(player, "colebans.check");
 				canKick = hasPermission(player, "colebans.kick");
 			}
 		}
 		else {
 			canBan = true;
 			canTempBan = true;
 			canUnBan = true;
 			canLookup = true;
 			canKick = true;
 		}
 		if (cmdLabel.equalsIgnoreCase("ban")) {
 			if (canBan) {
 				if (args.length < 2) error = ChatColor.RED+"You must specify a player and reason.";
 				else {
 					String victim = args[0];
 					StringBuilder reasonBuilder = new StringBuilder();
 					reasonBuilder.append(args[1]);
 					for (int i = 2; i<args.length; i++) {
 						reasonBuilder.append(" ");
 						reasonBuilder.append(args[i]);
 					}
 					String reason = reasonBuilder.toString();
 					try {
 						banHandler.banPlayer(victim, reason);
 						if (announceBansAndKicks) server.broadcastMessage(ChatColor.valueOf(banColor)+victim+" was banned! ["+reason+"]");
 						return true;
 					} catch (PlayerAlreadyBannedException e) {
 						error = ChatColor.DARK_RED+victim+" is already banned!";
 					}
 				}
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
 		else if (cmdLabel.equalsIgnoreCase("tempban")) {
 			if (canTempBan) {
 				if (args.length < 2) error = ChatColor.RED+"You must specify a player and time (in minutes).";
 				else if (args.length > 2) error = ChatColor.RED+"Too many arguments. Usage: /tempban <player> <minutes>";
 				else {
 					String victim = args[0];
 					try {
 						Long time = new Long(args[1]);
 						if (time > 2880) {
 							error = ChatColor.RED+"You cannot temp ban for more than 2 days!";
 						}
 						else {
 							try {
 								banHandler.tempBanPlayer(victim, time);
 								if (announceBansAndKicks) server.broadcastMessage(ChatColor.valueOf(tempBanColor)+victim+" was temporarily banned! ["+time+" minute"+getPlural(time)+"]");
 								return true;
 							} catch (PlayerAlreadyBannedException e) {
 								error = ChatColor.DARK_RED+victim+" is already banned!";
 							} catch (MethodNotSupportedException e) {
 								error = ChatColor.DARK_RED+"Temporary bans are disabled!";
 							}
 						}
 					}
 					catch (NumberFormatException e) {
 						error = ChatColor.RED+"Expected number for minutes, got String (or number too high)";
 					}
 				}
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
 		else if (cmdLabel.equalsIgnoreCase("unban")) {
 			if (canUnBan) {
 				if (args.length < 1) error = ChatColor.RED+"You must specify a player";
 				else if (args.length > 1) error = ChatColor.RED+"Too many arguments. Usage: /unban <player>";
 				else {
 					String victim = args[0];
 					try {
 						banHandler.unbanPlayer(victim);
 						if (announceBansAndKicks) server.broadcastMessage(ChatColor.GREEN+victim+" was unbanned!");
 						return true;
 					} catch (PlayerNotBannedException e) {
 						error = ChatColor.DARK_RED+victim+" is not banned!";
 					}
 				}
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
 		else if (cmdLabel.equalsIgnoreCase("lookup") | cmdLabel.equalsIgnoreCase("check")) {
 			if (canLookup) {
 				if (args.length < 1) error = ChatColor.RED+"You must specify a player";
 				else if (args.length > 1) error = ChatColor.RED+"Too many arguments. Usage: /lookup <player>";
 				else {
 					String victim = args[0];
 					BanData bd = banHandler.getBanData(victim);
 					if (bd.getType() == BanHandler.Type.PERMANENT) {
 						sender.sendMessage(ChatColor.RED+"-- "+ChatColor.DARK_RED+victim+ChatColor.RED+" --");
 						sender.sendMessage(ChatColor.RED+"Ban Type: "+ChatColor.DARK_RED+"Permanent");
 						sender.sendMessage(ChatColor.RED+"Reason: "+ChatColor.DARK_RED+bd.getReason());
 					}
 					else if (bd.getType() == BanHandler.Type.TEMPORARY) {
 						sender.sendMessage(ChatColor.YELLOW+"-- "+ChatColor.GOLD+victim+ChatColor.YELLOW+" --");
 						sender.sendMessage(ChatColor.YELLOW+"Ban Type: "+ChatColor.GOLD+"Temporary");
 						long timeRemaining = bd.getTime()-System.currentTimeMillis();
 						timeRemaining /= 1000;
 						timeRemaining /= 60;
 						sender.sendMessage(ChatColor.YELLOW+"Time Remaining (Minutes): "+ChatColor.GOLD+timeRemaining);
 					}
 					else if (bd.getType() == BanHandler.Type.NOT_BANNED) {
 						sender.sendMessage(ChatColor.AQUA+"-- "+ChatColor.DARK_AQUA+victim+ChatColor.AQUA+" --");
 						sender.sendMessage(ChatColor.AQUA+"Ban Type: "+ChatColor.DARK_AQUA+"Not Banned");
 					}
 					return true;
 				}
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
 		else if (cmdLabel.equalsIgnoreCase("kick")) {
 			if (canKick) {
 				if (args.length < 2) error = ChatColor.RED+"You must specify a player and reason.";
 				else {
 					String victim = args[0];
 					StringBuilder reasonBuilder = new StringBuilder();
 					reasonBuilder.append(args[1]);
 					for (int i = 2; i<args.length; i++) {
 						reasonBuilder.append(" ");
 						reasonBuilder.append(args[i]);
 					}
 					String reason = reasonBuilder.toString();
 					try {
 						kickPlayer(victim, reason);
 						if (announceBansAndKicks) server.broadcastMessage(ChatColor.valueOf(kickColor)+victim+" was kicked! ["+reason+"]");
 					} catch (PlayerOfflineException e) {
 						error = ChatColor.DARK_RED+victim+" is not online!";
 					}
 				}
 			}
 			else error = ChatColor.RED+"You do not have permission to use the "+cmdLabel+" command.";
 		}
 		sender.sendMessage(error);
 		return true;
 	}
 	
 	public void kickPlayer(String player, String reason) throws PlayerOfflineException {
 		Player playerObj = server.getPlayer(player);
 		if (playerObj != null) {
 			if (fancyEffects) {
 				World world = playerObj.getWorld();
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 1);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 2);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 3);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 4);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 5);
 				world.playEffect(playerObj.getLocation(), Effect.SMOKE, 6);
 			}
 			playerObj.kickPlayer(ChatColor.valueOf(kickColor)+"KICKED: "+reason);
 			if (announceBansAndKicks) server.broadcastMessage(ChatColor.valueOf(kickColor)+player+" was kicked! ["+reason+"]");
 		}
 		else throw new PlayerOfflineException(player+" is offline!");
 	}
 	public static String getPlural(long check) {
 		if (check < 0) return "s";
 		else if (check == 0) return "s";
 		else if (check > 1) return "s";
 		else return "";
 	}
     public boolean hasPermission(Player player, String permissionNode)
     {
     	if (permissionsHandler == null) return player.isOp();
         return permissionsHandler.has(player, permissionNode);
     }
 
 }
