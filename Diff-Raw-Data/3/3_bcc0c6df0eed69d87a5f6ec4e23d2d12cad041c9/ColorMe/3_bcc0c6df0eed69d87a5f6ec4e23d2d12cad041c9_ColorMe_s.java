 package com.sparkedia.valrix.ColorMe;
 
 import java.io.File;
 import java.util.LinkedHashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.craftbukkit.TextWrapper;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.coelho.iConomy.system.Account;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class ColorMe extends JavaPlugin {
 	private ColorPlayerListener pListener;
 	protected Logger log;
 	public Property colors;
 	public String pName;
 	public File df;
 	private Property config; // need at least one config option for non-OP use
 	private PermissionHandler permission;
 	private boolean iconomy = false;
 	
 	public void onDisable() {
 		log.info('['+pName+"] v"+getDescription().getVersion()+" has been disabled.");
 	}
 
 	public void onEnable() {
 		log = getServer().getLogger();
 		
 		pName = getDescription().getName();
 		df = getDataFolder();
 		
 		if (!df.isDirectory()) {
 			df.mkdir();
 		}
 
 		colors = new Property(df+"/players.color", "color", this);
 
 		//Does the config exist, if not then make a new blank one
 		if (!(new File(df+"/config.txt").exists())) {
 			config = new Property(df+"/config.txt", "color", this);
 			config.setBoolean("OP", true); //OP only by default
 			config.setDouble("cost", 0);
 			config.save();
 		} else {
 			config = new Property(df+"/config.txt", "config", this);
 			// Check if they have the updated prefix property file, otherwise update it to new format
 			if (!getDescription().getVersion().equalsIgnoreCase(config.getString(pName+"Version"))) {
 				LinkedHashMap<String, Object> tmp = new LinkedHashMap<String, Object>();
 				config.remove(pName+"Version");
 				config.remove(pName+"Type");
 				for (String key : config.getKeys()) {
 					// Reformat each player
 					tmp.put(key, config.getString(key));
 				}
 				config.rebuild(tmp);
 			}
 		}
 
 		if (getServer().getPluginManager().getPlugin("Permissions") != null) {
 			permission = ((Permissions)getServer().getPluginManager().getPlugin("Permissions")).getHandler();
 		} else {
 			log.info('['+pName+"]: Permission system not detected. Defaulting to OP permissions.");
 		}
 		
 		if (getServer().getPluginManager().getPlugin("iConomy") != null) {
 			iconomy = true;
 		} else {
 			log.info('['+pName+"]: iConomy not detected. Disabling iConomy support.");
 		}
 		
 		pListener = new ColorPlayerListener(this);
 		
 		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, pListener, Event.Priority.Highest, this);
 		
 		log.info('['+pName+"] v"+getDescription().getVersion()+" has been enabled.");
 		
 		// /color <color/name> [name] (name is optional since you can color your own name)
 		getCommand("colorme").setExecutor(new CommandExecutor() {
 			public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 				if (sender instanceof Player) {
 					Player player = ((Player)sender);
 					// Sender has permissions for /prefix or (sender is an OP or OP=false)
 					if (permission != null) {
 						if (args.length == 1) {
 							if (args[0].equalsIgnoreCase("list") && permission.has(player, "colorme.list")) {
 								// Display a list of colors for the user
 								player.sendMessage("Color List:");
 								String color;
 								String msg = "";
 								for (int i = 0; i < ChatColor.values().length; i++) {
 									color = ChatColor.getByCode(i).name();
 									if (msg.length() == 0) {
 										msg = ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
 										continue;
 									}
 									msg += (i == ChatColor.values().length-1) ? ChatColor.valueOf(color)+color.toLowerCase().replace("_", "") : ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
 									TextWrapper.wrapText(msg);
 								}
 								player.sendMessage(msg);
 								return true;
 							} else if ((hasColor(args[0]) && (permission.has(player, "colorme.remove"))) || args[0].equalsIgnoreCase(player.getName().toLowerCase())) {
 								// Only people with permission to remove another's color or the color owner can remove
 								removeColor(args[0]);
 								return true;
 							} else {
 								if (permission.has(player, "colorme.self")) {
 									// Set a color for the user calling the command if they have permission
 									setColor(player.getName(), args[0]);
 									if (iconomy) {
 										double cost = config.getDouble("cost");
 										Account acct = iConomy.getBank().getAccount(player.getName());
 										if (cost > 0 && acct.getBalance()-cost > 0) {
 											acct.subtract(cost);
 											player.sendMessage(ChatColor.RED.toString()+cost+' '+iConomy.getBank().getCurrency()+" has been charged to your account.");
 										}
 									}
 									return true;
 								}
 								player.sendMessage("You don't have permission to color your own name.");
 								return true;
 							}
 						} else if (args.length == 2) {
 							// /colorme <name> <color>
 							if ((hasColor(args[0]) && (permission.has(player, "colorme.other"))) || (args[0].equalsIgnoreCase(player.getName().toLowerCase()) && permission.has(player, "colorme.self"))) {
 								// Name exists. They have permission to set another's color or can set own.
 								setColor(args[0], args[1]);
 								if (iconomy && args[0].equalsIgnoreCase(player.getName())) {
 									double cost = config.getDouble("cost");
 									Account acct = iConomy.getBank().getAccount(player.getName());
 									if (cost > 0 && acct.getBalance()-cost > 0) {
 										acct.subtract(cost);
 										player.sendMessage(ChatColor.RED.toString()+cost+' '+iConomy.getBank().getCurrency()+" has been charged to your account.");
 									}
 								}
 								return true;
 							}
 							return true;
 						}
 					} else if (player.isOp() || !config.getBoolean("OP")) {
 						// Permissions isn't enabled
 						if (args.length == 1) {
 							if (args[0].equalsIgnoreCase("list")) {
 								// Display a list of colors for the user
 								player.sendMessage("Color List:");
 								String color;
 								String msg = "";
 								for (int i = 0; i < ChatColor.values().length; i++) {
 									color = ChatColor.getByCode(i).name();
 									if (msg.length() == 0) {
 										msg = ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
 										continue;
 									}
 									msg += (i == ChatColor.values().length-1) ? ChatColor.valueOf(color)+color.toLowerCase().replace("_", "") : ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
 									TextWrapper.wrapText(msg);
 								}
 								player.sendMessage(msg);
 								return true;
 							} else if (hasColor(args[0]) && (player.isOp() || !config.getBoolean("OP"))) {
 								// Only people with permission to remove another's color or the color owner can remove
 								if (args[0].equalsIgnoreCase(player.getName().toLowerCase())) {
 									removeColor(args[0]);
 								}
 								return true;
 							} else if (!hasColor(args[0])) {
 								// Don't let them set the color equal to their own name
 								if (args[0].equalsIgnoreCase(player.getName().toLowerCase())) return true;
 								// If not trying to remove a color they don't already have...
 								if (player.isOp() || !config.getBoolean("OP")) {
 									// Set a color for the user calling the command if they have permission
 									setColor(player.getName(), args[0]);
 									if (iconomy) {
 										double cost = config.getDouble("cost");
 										Account acct = iConomy.getBank().getAccount(player.getName());
 										if (cost > 0 && acct.getBalance()-cost > 0) {
 											acct.subtract(cost);
 											player.sendMessage(ChatColor.RED.toString()+cost+' '+iConomy.getBank().getCurrency()+ChatColor.DARK_PURPLE+" has been charged to your account.");
 										}
 									}
 									return true;
 								}
 								player.sendMessage("You don't have permission to set your name color.");
 								return true;
 							}
 						} else if (args.length == 2) {
 							// /colorme <name> <color>
 							if (hasColor(args[0]) && (player.isOp() || (!config.getBoolean("OP") && args[0].equalsIgnoreCase(player.getName().toLowerCase())))) {
 								// Name exists. Are OP or is allowed to set own color
 								setColor(args[0], args[1]);
 								if (iconomy && args[0].equalsIgnoreCase(player.getName())) {
 									double cost = config.getDouble("cost");
 									Account acct = iConomy.getBank().getAccount(player.getName());
 									if (cost > 0 && acct.getBalance()-cost > 0) {
 										acct.subtract(cost);
 										player.sendMessage(ChatColor.RED.toString()+cost+' '+iConomy.getBank().getCurrency()+ChatColor.DARK_PURPLE+" has been charged to your account.");
 									}
 								}
 								return true;
 							}
 							return true;
 						}
 					}
 				} else if (sender instanceof ConsoleCommandSender) {
 					// /colorme <name> [color]
 					if (args.length == 1) {
 						if (args[0].equalsIgnoreCase("list")) {
 							// Display a list of colors for the user
 							sender.sendMessage("Color List:");
 							String color;
 							String msg = "";
 							for (int i = 0; i < ChatColor.values().length; i++) {
 								color = ChatColor.getByCode(i).name();
 								if (msg.length() == 0) {
 									msg = ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
 									continue;
 								}
 								msg += (i == ChatColor.values().length-1) ? ChatColor.valueOf(color)+color.toLowerCase().replace("_", "") : ChatColor.valueOf(color)+color.toLowerCase().replace("_", "")+' ';
 								TextWrapper.wrapText(msg);
 							}
 							sender.sendMessage(msg);
 							return true;
 						} else {
 							if (hasColor(args[0])) {
 								removeColor(args[0]);
 								sender.sendMessage("Removed color from "+args[0]);
 							} else {
 								sender.sendMessage(args[0]+" doesn't have a colored name.");
 							}
 							return true;
 						}
 					} else if (args.length == 2) {
 						setColor(args[0], args[1]);
 						sender.sendMessage("Colored "+args[0]+"\'s name "+args[1]);
 						return true;
 					}
 				}
 				return false;
 			}
 		});
 	}
 	
 	public String getColor(String name) {
 		name = name.toLowerCase();
 		if (colors.keyExists(name)) {
 			return colors.getString(name);
 		}
 		return "";
 	}
 	
 	public boolean setColor(String name, String color) {
 		name = name.toLowerCase();
 		String col;
 		for (int i = 0; i <= 15; i++) {
 			col = ChatColor.getByCode(i).name().toLowerCase().replace("_", "");
 			if (color.equalsIgnoreCase(col)) {
 				colors.setString(name, color);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean hasColor(String name) {
 		name = name.toLowerCase();
 		if (!colors.getString(name).isEmpty()) {
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean removeColor(String name) {
 		name = name.toLowerCase();
 		if (colors.keyExists(name)) {
 			colors.remove(name);
 			return true;
 		}
 		return false;
 	}
 }
