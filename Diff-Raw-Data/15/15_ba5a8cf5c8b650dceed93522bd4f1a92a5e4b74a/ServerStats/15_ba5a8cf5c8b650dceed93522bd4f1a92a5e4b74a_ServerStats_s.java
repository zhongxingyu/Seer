 package me.rosswalker.serverstats;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /* @name ServerStats
  * @author uberKat, DicaxDorcas
  * @description Central class for the ServerStats plugin.
  */
 
 
 public class ServerStats extends JavaPlugin {
 
 	Messages messages;
 	
 	/* @name onCommand()
 	 * @description Handles commands for ServerStats.
 	 * @param1 CommandSender
 	 * @param2 Command
 	 * @param3 String
 	 * @param4 String[]
 	 * 
 	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("ss")) {
 				Player player = (Player)sender;
 				//Stats Command
 			if (args[0].equalsIgnoreCase("stats")) {
 				if (player.hasPermission("stats.stats")) {
 					int totalPlayers = (getServer().getOfflinePlayers().length 
 										+ getServer().getOnlinePlayers().length);
 					int onlinePlayers = getServer().getOnlinePlayers().length;
 					int maxSlots = getServer().getMaxPlayers();
 					String serverName = getServer().getName();
 					String serverIP = getServer().getIp();
 					int serverPort = getServer().getPort();
 					int serverOPs = getServer().getOperators().size();
 					String bukkitVersion = getServer().getBukkitVersion();
 						
 					player.sendMessage(ChatColor.BLUE + "----------ServerStats----------");
 					player.sendMessage(ChatColor.GRAY + "  There are " + ChatColor.DARK_GRAY + onlinePlayers + ChatColor.GRAY + " out of " + ChatColor.DARK_GRAY + maxSlots + ChatColor.GRAY + " players online.");
 					player.sendMessage(ChatColor.DARK_GRAY + "  " + serverName + ChatColor.GRAY + " is running on IP " + ChatColor.DARK_GRAY + serverIP + ChatColor.GRAY + " with port " + ChatColor.DARK_GRAY + serverPort + ChatColor.GRAY + ".");
 					player.sendMessage(ChatColor.DARK_GRAY + "  " + serverName + ChatColor.GRAY + " has Bukkit version " + ChatColor.DARK_GRAY + bukkitVersion + ChatColor.GRAY + ".");
 					player.sendMessage(ChatColor.GRAY + "  There are currently " + ChatColor.DARK_GRAY + serverOPs + ChatColor.GRAY + " players with 'OP' status.");
 					player.sendMessage(ChatColor.GRAY + "  A total of " + ChatColor.DARK_GRAY + totalPlayers + ChatColor.GRAY + " players have joined " + ChatColor.DARK_GRAY + serverName);
 					player.sendMessage(ChatColor.GRAY + "  Maximum Memory: " + ChatColor.DARK_GRAY + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + ChatColor.GRAY + " MB");
 					player.sendMessage(ChatColor.GRAY + "  Total Memory: " + ChatColor.DARK_GRAY + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + ChatColor.GRAY + " MB");
 					player.sendMessage(ChatColor.GRAY + "  Free Memory: " + ChatColor.DARK_GRAY + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + ChatColor.GRAY + " MB");
 					player.sendMessage(ChatColor.BLUE + "--------------------------------");
 					return true;
 				} else {
					messages.getError(0, player);
 				}
 			}
 				
 				//Player Command
 				
 			if (args[0].equalsIgnoreCase("player")) {
 				try {
 					if (args.length < 2) {
 						if (player.hasPermission("stats.player")) {
 							player.sendMessage(ChatColor.BLUE + "----------ServerStats----------");
 							player.sendMessage(ChatColor.GRAY + "  Hello, " + ChatColor.DARK_BLUE + player.getName() + "!");
 							player.sendMessage(ChatColor.GRAY + "  You are playing in gamemode '" + ChatColor.DARK_GRAY + player.getGameMode() + ChatColor.GRAY + ".'");
 							player.sendMessage(ChatColor.GRAY + "  You are playing on IP " + ChatColor.DARK_GRAY + player.getAddress() + ChatColor.GRAY + ".");
 							player.sendMessage(ChatColor.GRAY + "  You have " + ChatColor.DARK_GRAY + player.getHealth() + ChatColor.GRAY + " out of " + ChatColor.DARK_GRAY + player.getMaxHealth() + ChatColor.GRAY + " health.");
 							player.sendMessage(ChatColor.BLUE + "--------------------------------");
 							return true;
 						} else {
							messages.getError(0, player);
 						}
 					}
 					
 				Player otherPlayer = player.getServer().getPlayer(args[1]);
 				
 					if (player.hasPermission("stats.player.lookup")) {
 						player.sendMessage(ChatColor.BLUE + "----------ServerStats----------");
 					    player.sendMessage(ChatColor.GRAY + "  " + ChatColor.DARK_BLUE + otherPlayer.getName() + "'s stats:");
 						player.sendMessage(ChatColor.GRAY + "  OP: " + ChatColor.DARK_GRAY + otherPlayer.isOp());
 						player.sendMessage(ChatColor.GRAY + "  Online: " + ChatColor.DARK_GRAY + otherPlayer.isOnline());
 						player.sendMessage(ChatColor.GRAY + "  Gamemode: " + ChatColor.DARK_GRAY + otherPlayer.getGameMode() + ChatColor.GRAY);
 						player.sendMessage(ChatColor.GRAY + "  IP: " + ChatColor.DARK_GRAY + otherPlayer.getAddress());
 						player.sendMessage(ChatColor.GRAY + "  Health: " + ChatColor.DARK_GRAY + otherPlayer.getHealth());
 						player.sendMessage(ChatColor.GRAY + "  Location: " + ChatColor.DARK_GRAY + "(" + otherPlayer.getWorld().getName() + ", " + Math.round(otherPlayer.getLocation().getX()) + ", " + Math.round(otherPlayer.getLocation().getY()) + ", " + Math.round(otherPlayer.getLocation().getZ()) + ")");
 						player.sendMessage(ChatColor.BLUE + "--------------------------------");
 						return true;
 					} else {
						messages.getError(0, player);
 					}
 				} catch (Exception ex) {
					messages.getError(1, player);
 					}
 			}
 			
 		}
 		return false;
 				
 	}
 
 	
 	/* @name onEnable()
 	 * @description Ran when the plugin is enabled.
 	 * 
 	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
 	 */
     public void onEnable() {
     	this.getLogger().info("Plugin enabled.");
 	}
 	
     
     /* @name onDisable()
 	 * @description Ran when the plugin is disabled.
 	 * 
 	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
 	 */
 	public void onDisable() { 
 		this.getLogger().info("Plugin disabled.");
 	}	
 
 }
 
