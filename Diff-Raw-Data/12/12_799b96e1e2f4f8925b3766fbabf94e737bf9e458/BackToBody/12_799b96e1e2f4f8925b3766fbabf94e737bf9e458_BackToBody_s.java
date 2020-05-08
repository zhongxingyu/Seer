 package com.sparkedia.valrix.BackToBody;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.coelho.iConomy.system.Account;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class BackToBody extends JavaPlugin {
 	private BEntityListener el;
 	public Logger log;
 	public String df;
 	public String players;
 	public String pName;
 	public Property config;
 	public PermissionHandler permission = null;
 	public iConomy iconomy = null;
 
 	public String getCanonPath(String d) {
 		String c = null;
 		try {
 			c = new File(d).getCanonicalPath();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return c;
 	}
 
 	public String getCanonFile(String f) {
 		String c = null;
 		try {
 			c = new File(f).getCanonicalFile().toString();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return c;
 	}
 
 	public void onDisable() {
 		PluginDescriptionFile pdf = getDescription();
 		log.info('['+pName+"] v"+pdf.getVersion()+" has been disabled.");
 	}
 
 	public void onEnable() {
 		PluginDescriptionFile pdf = getDescription();
 		pName = pdf.getName();
 		log = getServer().getLogger();
 
 		// Set up Permissions support
 		if (getServer().getPluginManager().getPlugin("Permissions") != null) {
 			permission = ((Permissions)getServer().getPluginManager().getPlugin("Permissions")).getHandler();
 		} else {
 			log.info('['+pName+"]: Permission system not detected. Disabling Permissions support.");
 		}
 
 		// Set up iConomy support
 		if (getServer().getPluginManager().getPlugin("iConomy") != null) {
 			iconomy = ((iConomy)getServer().getPluginManager().getPlugin("iConomy"));
 		} else {
 			log.info('['+pName+"]: iConomy not found. Disabling iConomy support.");
 		}
 
 		// Set public path variables
 		df = getCanonPath(getDataFolder().toString());
 		players = getCanonPath(df+"/players");
 
 		// Initialize listeners
 		el = new BEntityListener(this);
 
 		// Register events to the listeners
 		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, el, Event.Priority.Normal, this);
 
 		// If players folder doesn't exist make it, including the BackToBody one if that doesn't exist
 		if (!(new File(players)).isDirectory()) {
 			new File(players).mkdirs();
 		}
 
 		// Then make the config if it doesn't exist, otherwise load it
 		if (!(new File(df+"/config.txt").exists())) {
 			config = new Property(getCanonFile(df+"/config.txt"), this);
 			config.setInt("cost", 0);
 			config.save();
 		} else {
 			config = new Property(getCanonFile(df+"/config.txt"), this);
 		}
 
 		final BackToBody p = this;
 		// START commands
 		getCommand("btb").setExecutor(new CommandExecutor() {
 			public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 				String cmdName = cmd.getName();
 				if (sender instanceof Player) {
 					if (cmdName.equalsIgnoreCase("btb")) {
 						Player player = ((Player)sender);
 						String name = player.getName();
 						// Permission is enabled, but they're not allowed to use the command
						if (permission == null && player.isOp() || !permission.has(player, "backtobody.btb")) {
 							Location loc = player.getLocation();
 							Property prop = new Property(getCanonFile(players+'/'+name+".loc"), p);
 							// They didn't die yet, so they can't go back to their body
 							if (!prop.keyExists("x")) {
								player.sendMessage("You either haven't died, or your corpse no longer exists.");
 								return true;
 							}
 							int cost = config.getInt("cost");
 							if (cost == 0) {
 								//return player to where they died
 								double x = prop.getDouble("x");
 								double y = prop.getDouble("y");
 								double z = prop.getDouble("z");
 								float yaw = prop.getFloat("yaw");
 								loc.setX(x);
 								loc.setY(y);
 								loc.setZ(z);
 								loc.setYaw(yaw);
 								player.teleport(loc);
 								player.sendMessage(ChatColor.DARK_RED+"Your corpse vanishes as you return to it...");
 								new File(getCanonFile(players+'/'+name+".loc")).delete();
 								return true;
 							} else {
 								if (iconomy != null) {
 									//return player to where they died
 									double x = prop.getDouble("x");
 									double y = prop.getDouble("y");
 									double z = prop.getDouble("z");
 									float yaw = prop.getFloat("yaw");
 									loc.setX(x);
 									loc.setY(y);
 									loc.setZ(z);
 									loc.setYaw(yaw);
 									player.teleport(loc);
 									Account account = iConomy.getBank().getAccount(name);
 									if (account.getBalance() > cost) {
 										account.subtract(cost);
 										player.sendMessage(ChatColor.DARK_RED+"The Reaper"+ChatColor.DARK_PURPLE+" returns you to your corpse, but for a price. You forfeit "+ChatColor.DARK_RED+cost+' '+iConomy.getBank().getCurrency()+ChatColor.DARK_PURPLE+'.');
 										new File(getCanonFile(players+'/'+name+".loc")).delete();
 										return true;
 									} else {
										player.sendMessage(ChatColor.DARK_PURPLE+"You hear an evil laugh."+ChatColor.DARK_RED+" \"You do not possess enough "+ChatColor.GREEN+iConomy.getBank().getCurrency()+ChatColor.DARK_RED+".\" The toll is "+ChatColor.GREEN+cost+' '+iConomy.getBank().getCurrency()+ChatColor.DARK_RED+'.');
 										return true;
 									}
 								} else {
 									log.severe('['+pName+"]: iConomy isn't enabled. Remove the price or enable iConomy.");
 									return true;
 								}
 							}
 						} else {
							player.sendMessage("You do not have permissions to use that command!");
 						}
 					}
 				}
 				return false;
 			}
 		});
 		// END commands
 
 		log.info('['+pName+"] v"+pdf.getVersion()+" has been enabled.");
 	}
 }
