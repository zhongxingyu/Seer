 package io.github.matho97.lockdown;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitTask;
 
 public class LockdownCommandExecutor implements CommandExecutor{
 
 	private Lockdown plugin;
 	public String lockdown = ChatColor.RED + "[" + ChatColor.GOLD + "LockDown" + ChatColor.RED + "] " + ChatColor.WHITE;
 	public String notenough = lockdown + ChatColor.YELLOW + "Not enough arguments!";
 	public String toomany = lockdown + ChatColor.YELLOW + "Too many arguments!";
 	
 	public LockdownCommandExecutor(Lockdown plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if (cmd.getName().equalsIgnoreCase("lockdown")){
 			//Player target = Bukkit.getPlayerExact(args[0]);
 			if (args.length == 0){
 				sender.sendMessage(ChatColor.RED + "----------------" + ChatColor.WHITE + " Lockdown Help Page " + ChatColor.RED + "----------------");
 				sender.sendMessage("/lockdown" + ChatColor.YELLOW + " - Shows this help page.");
 				sender.sendMessage("/lockdown set <1|2>" + ChatColor.YELLOW + " - Sets the 2 warp points, 1 is the prison, 2 is when it's over.");
 				sender.sendMessage("/lockdown reload" + ChatColor.YELLOW + " - Reloads the configuration files.");
 				sender.sendMessage("/lockdown on <amount of time> <s|m>" + ChatColor.YELLOW + " - Sets the prison into lockdown mode, s = seconds, m = minutes");
 				//sender.sendMessage("");
 				return true;
 			}
 				if (args[0].equalsIgnoreCase("set")){
 					Player player = sender.getServer().getPlayer(sender.getName());
 					//if (!(args.length < 1)){
 						if(args.length == 1){
 							sender.sendMessage(notenough);
 							return false;
 						} else if (args.length == 3){
 							sender.sendMessage(toomany);
 							return false;
 						}
 						
 						if (args[1].equalsIgnoreCase("1")){
 							String x = Double.toString(player.getLocation().getX());
 							String y = Double.toString(player.getLocation().getY());
 							String z = Double.toString(player.getLocation().getZ());
 							
 							plugin.getConfig().set(plugin.location1 + ".X", x.substring(0, 3));
 							plugin.getConfig().set(plugin.location1 + ".Y", y.substring(0, 3));
 							plugin.getConfig().set(plugin.location1 + ".Z", z.substring(0, 3));
 							plugin.saveConfig();
 							sender.sendMessage(lockdown + ChatColor.RED + "Location 1 has been set at " + ChatColor.GREEN + x.substring(0, 3) + ", " + y.substring(0, 3) + ", " + z.substring(0, 3));
 							/*double x = Double.parseDouble(args[1]);
 							double y = Double.parseDouble(args[2]);
 							double z = Double.parseDouble(args[3]);
 							for(Player players : Bukkit.getOnlinePlayers()){
 								Location teleportloc = new Location(Bukkit.getWorld("world"), x, y, z);
 								
 								players.teleport(teleportloc);
 							}*/
 							return true;
 						} else if (args[1].equalsIgnoreCase("2")){
 							String x = Double.toString(player.getLocation().getX());
 							String y = Double.toString(player.getLocation().getY());
 							String z = Double.toString(player.getLocation().getZ());
 							
 							plugin.getConfig().set(plugin.location2 + ".X", x.substring(0, 3));
 							plugin.getConfig().set(plugin.location2 + ".Y", y.substring(0, 3));
 							plugin.getConfig().set(plugin.location2 + ".Z", z.substring(0, 3));
 							plugin.saveConfig();
 							sender.sendMessage(lockdown + ChatColor.RED + "Location 2 has been set at " + ChatColor.GREEN + x.substring(0, 3) + ", " + y.substring(0, 3) + ", " + z.substring(0, 3));
 							return true;
 						}
 					//}
 					//sender.sendMessage("Not enough arguments!");
 					//return false;
 				} else if (args[0].equalsIgnoreCase("reload")){
 					if (args.length == 2){
 						sender.sendMessage(toomany);
 					}
 					plugin.reloadConfig();
 					sender.sendMessage(lockdown + ChatColor.GREEN + "Config has been reloaded!");
 					return true;
 				} else if (args[0].equalsIgnoreCase("on")){
					if (args.length <= 2){
 						sender.sendMessage(notenough);
 						return false;
 					} else if (args.length == 4){
 						sender.sendMessage(toomany);
 						return false;
 					} else if (args[2] != "s"||args[2] != "m"){
 						sender.sendMessage(lockdown + "The argument '" + args[2] + "' is not accepted!");
 						sender.sendMessage(lockdown + "Use 's' for seconds and 'm' for minutes");
 						return true;
 					}
 					//sender.sendMessage("Teleporting players!");
 				    String sx = plugin.getConfig().getString(plugin.location1 + ".X");
 					String sy = plugin.getConfig().getString(plugin.location1 + ".Y");
 					String sz = plugin.getConfig().getString(plugin.location1 + ".Z");
 					
 					double x = Double.parseDouble(sx);
 					double y = Double.parseDouble(sy);
 					double z = Double.parseDouble(sz);
 					for(Player players : Bukkit.getOnlinePlayers()){
 						Location teleportloc = new Location(Bukkit.getWorld("world"), x, y, z);
 						
 						players.teleport(teleportloc);
 					}
 					Bukkit.broadcastMessage(lockdown + ChatColor.BLUE + "Prison has been put into lockdown, you will not be able to leave this area!");
 					int delay;
 					if (args[1] == null){
 						delay = 5;
 					} else {
 						delay = Integer.parseInt(args[1]);
 					}
 					
 					//@SuppressWarnings("unused")
 					//BukkitTask task = new LockdownTask(plugin).runTaskLater(plugin, sleep * 20);
 					if (args[2] == null){
 						sender.sendMessage(lockdown + "You need to choose if you want the delay in seconds or minutes! s or m.");
 						return true;
 					} else if (args[2].equalsIgnoreCase("m")){
 						sender.sendMessage(lockdown + "Server has been put in lockdown for " + delay + " minute(s).");
 						
 						@SuppressWarnings("unused")
 						BukkitTask task = new LockdownTask(plugin).runTaskLater(plugin, delay * 1200);
 						return true;
 					} else if (args[2].equalsIgnoreCase("s")){
 						sender.sendMessage(lockdown + "Server has been put in lockdown for " + delay + " second(s).");
 						
 						@SuppressWarnings("unused")
 						BukkitTask task = new LockdownTask(plugin).runTaskLater(plugin, delay * 20);
 						return true;
 					}
 				}
 				return false;
 				
 				/*if (args.length < 1) {
 			    	sender.sendMessage("Not enough arguments!");
 			    	return false;
 			    } else {
 			    
 				
 				sender.sendMessage("adwadwa");
 				return true;
 				*/
 			
 			
 		} //end of lockdown command
 		 //If this has happened the function will return true. 
 	        // If this hasn't happened the a value of false will be returned.
 		return false; 
 	}
 
 }
