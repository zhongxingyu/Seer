 package com.etriacraft.etriabending.suites;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 
 import com.etriacraft.etriabending.DBConnection;
 import com.etriacraft.etriabending.EtriaBending;
 import com.etriacraft.etriabending.util.Home;
 import com.etriacraft.etriabending.util.PlayerUtils;
 
 public class HomeSuite {
 	
 	public static int homescap;
 
 	public static HashMap<String, List<Home>> homesDb = new HashMap();
 
 	// Methods 
 	public static void Homes() {
 		ResultSet results = DBConnection.sql.readQuery("SELECT * FROM `player_homes`;");
 		int i = 0;
 		try {
 			while (results.next()) {
 				String owner = results.getString("owner"), name = results.getString("name"), world = results.getString("world");
 				World w = Bukkit.getWorld(world);
 				if (w == null) continue;
 				int x = results.getInt("x"), y = results.getInt("y"), z = results.getInt("z");
 				float pitch = results.getFloat("pitch"), yaw = results.getFloat("yaw");
 				if (!homesDb.containsKey(owner)) homesDb.put(owner, new ArrayList());
 				homesDb.get(owner).add(new Home(w, x, y, z, pitch, yaw, owner, name));
 				i++;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		EtriaBending.log.info("Successfully loaded " + i + " homes.");
 	}
 
 	static boolean homeExist(String player, String homename) {
 		if (!homesDb.containsKey(player)) return false;
 		for(Home home : homesDb.get(player)) {
 			if (home.getName().equals(homename))
 				return true;
 		}
 		return false;
 	}
 
 	static Home getPlayerHome(String player, String homename) {
 		for (Home home : homesDb.get(player)) {
 			if (home.getName().equals(homename))
 				return home;
 		}
 		return null;
 	}
 
 	static void removePlayerHome(String player, String homename) {
 		for (Home home : homesDb.get(player)) {
 			if (home.getName().equals(homename)) {
 				homesDb.get(player).remove(home);
 				return;
 			}
 		}
 	}
 
 	static int getPlayerHomeLimit(CommandSender s) {
 	     if (s.isOp()) return homescap;
 	     int cap = 0;
 	     for (int i = 0; i <= homescap; i++) {
 	     if (s.hasPermission("ec.sethome.limit." + i)) cap = i;
 	     }
 	     return cap;
 	    }
 	
 //	static int getPlayerHomeLimit(CommandSender s) {
 //	     if (s.isOp()) return Config.GLOBAL_MAX_HOMES;
 //	     int cap = 0;
 //	     for (int i = 0; i <= Config.GLOBAL_MAX_HOMES; i++) {
 //	     if (s.hasPermission("ec.sethome.limit." + i)) cap = i;
 //	     }
 //	     return cap;
 //	    }
 
 	EtriaBending plugin;
 
 	public HomeSuite(EtriaBending instance) {
 		this.plugin = instance;
 		init();
 	}
 
 	private void init() {
 		PluginCommand delhome = plugin.getCommand("delhome");
 		PluginCommand home = plugin.getCommand("home");
 		PluginCommand listhomes = plugin.getCommand("listhomes");
 		PluginCommand sethome = plugin.getCommand("sethome");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.home")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					if (!(s instanceof Player)) return false;
 
 					String homename = "home", player = s.getName();
 					if (args.length >= 1) {
 						if (!s.hasPermission("eb.home.named")) {
 							s.sendMessage("cYou don't have permission to do that!");
 							return true;
 						} if (args[0].contains(":")) {
 							if (!s.hasPermission("ec.home.other")) {
 								s.sendMessage("cYou don't have permission to do that!");
 								return true;
 							}
 							String[] homeinf = args[0].split(":");
 							player = homeinf[0];
 							homename = homeinf[1].toLowerCase();
 						} else {
 							homename = args[0].toLowerCase();
 						}
 					}
 
 					if (!homeExist(player, homename)) {
 						s.sendMessage("cThat home doesn't exist!");
 						return true;
 					}
 
 					if (PlayerUtils.teleport((Player) s, getPlayerHome(player, homename).getLocation()))
 						s.sendMessage("aGoing to:e " + homename);
 					return true;
 				}
 				return true;
 			}
 		}; home.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.delhome")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					if (!(s instanceof Player)) return false;
 
 					String homename = "home", player = s.getName();
 					if (args.length >= 1) {
 						if (args[0].contains(":")) {
 							if (!s.hasPermission("eb.delhome.others")) {
 								s.sendMessage("cYou don't have permission to do that!");
 								return true;
 							}
 							String[] homeinf = args[0].split(":");
 							player = homeinf[0];
 							homename = homeinf[1].toLowerCase();
 						} else {
 							if (!(s instanceof Player)) return false;
 							homename = args[0].toLowerCase();
 						}
 					} else {
 						if (!(s instanceof Player)) return false;
 					}
 
 					if (homeExist(player, homename)) {
 						DBConnection.sql.modifyQuery("DELETE FROM `player_homes` WHERE `owner` = '"+player+"' AND `name` = '"+homename+"';");
 						removePlayerHome(player, homename);
 						s.sendMessage("cDeleting homee " + homename);
 					} else {
 						s.sendMessage("cThat home doesn't exist");
 					}
 					return true;
 				} return true;
 			}
 		}; delhome.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.listhomes")) {
 					s.sendMessage("cYou don't have permission to do that!");
 					return true;
 				} else {
 					String player = s.getName();
 					if (args.length >= 1) {
 						if (!s.hasPermission("eb.listhomes.other")) {
 							s.sendMessage("cYou don't have permission to do that!");
 							return true;
 						}
 						player = args[0];
 					}
 
 					if (homesDb.containsKey(player) || homesDb.get(player).isEmpty()) {
 						s.sendMessage("cNo homes found.");
 						return true;
 					}
 
 					String list = "";
 					for (Home home : homesDb.get(player)) {
 						if (!list.isEmpty()) list += "a,e ";
 						list += home.getName();
 					}
 
 					s.sendMessage("aHomes:e " + list);
 					s.sendMessage("e" + homesDb.get(player).size() + " ahomes in total.");
 					return true;
 				}
 			}
 		}; listhomes.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.sethome")) {
 					s.sendMessage("cYou don't have permission to do that!");
 					return true;
 				} else {
 					if (!(s instanceof Player)) return false;
 
 					String homename = "home", player = s.getName();
 					if (args.length >= 1) {
 						if (!s.hasPermission("eb.sethome.named")) {
 							s.sendMessage("cYou don't have permission to do that!");
 							return true;
 						}
 					}
 
					if (!homesDb.containsKey(player)) homesDb.put(player,  new ArrayList());
 
 					final int limit = getPlayerHomeLimit(s);
 					if (homesDb.get(player) != null && homesDb.get(player).size() >= limit) {
 						s.sendMessage("cYou have already reached your limit of e" + limit + " chomes.");
 						return true;
 					}
 
 					Player p = (Player) s;
 					if (!homeExist(player, homename)) {
 						DBConnection.sql.modifyQuery("INSERT INTO `player_homes` (`owner`, `name`, `world`, `x`, `y`,`z`, `pitch`, `yaw`) VALUES "
 								+ "('"+p.getName()+ "', '"+homename+"', '"+p.getLocation().getWorld().getName()+"', "+p.getLocation().getX()+", "+p.getLocation().getY()+", "+p.getLocation().getZ()+", "+p.getLocation().getPitch()+", "+p.getLocation().getYaw()+");");
 						s.sendMessage("aYou have set homee " + homename);
 					} else {
 						DBConnection.sql.modifyQuery("UPDATE `player_homes` SET `world` = '"+p.getLocation().getWorld().getName()+"', `x` = "+p.getLocation().getX()+", `y` = "+p.getLocation().getY()+", `z` = "+p.getLocation().getZ()+", `pitch` = "+p.getLocation().getPitch()+", `yaw` = "+p.getLocation().getYaw()+" WHERE"
 								+ " `owner` = '"+player+"' AND `name` = '"+homename+"';");
 						s.sendMessage("aUpdated the location ofe " + homename);
 						homesDb.get(player).remove(getPlayerHome(player, homename));
 					}
 
 					Home home = new Home(p.getLocation(), player, homename);
 					homesDb.get(player).add(home);
 					return true;
 				}
 			}
 		}; sethome.setExecutor(exe);
 	}
 }
