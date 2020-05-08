 package com.github.limdingwen.RealSleep;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RealSleep extends JavaPlugin implements Listener {
 	Logger log;
 	public Map<String, Float> data = new HashMap<String, Float>();
 	
 	public void onEnable() {
 		log = this.getLogger();
 		
 		getServer().getPluginManager().registerEvents(this, this);
 		checkStartupFile();
 		
 		// Start the decrease
 		
 		Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 
 			   public void run() {
 			       SleepDecreaser.decrease();
 			   }
 			}, 100L, 100L);
 		
 		log.info("RealSleep ready to get you sleepy!");
 	}
 	
 	public void checkStartupFile() {
 		try {
 			SLAPI.load("RealSleepData");
 		} catch (Exception e) {
 			log.warning("Check failed for RealSleepData. Reason: Cannot find file. Creating.");
 			
 			Map<String, Float> data = new HashMap<String, Float>();
 			
 			try {
 				SLAPI.save(data, "RealSleepData");
 			} catch (Exception e1) {
 				log.warning("Create failed for RealSleepData. Reason: cannot save file. Try reloading.");
 			}
 		}
 	}
 	
 	public void onDisable() {
 		log.info("RealSleep has finished disabling!");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("sleep")) {
 			if (args.length > 0) {
 				if (args[0].equalsIgnoreCase("check")) {
 					if (args.length > 1) {
 						Float sleep = (float) 0.0;
						Player tester = Bukkit.getServer().getPlayer(args[1]);
						if (tester != null) {
							sleep = SleepChecker.getSleep((CommandSender) tester);
						}
						else {
							sender.sendMessage(ChatColor.RED + "Cannot find " + args[1] + "! No check.");
							
							return true;
						}
 						
 						if (sleep != null) {
 							sender.sendMessage(args[1] + "'s sleep is now at: " + Float.toString(sleep) + "%");
 							
 							return true;
 						}
 						else {
 							sender.sendMessage(ChatColor.RED + "Cannot find " + args[1] + "! No check.");
 							
 							return true;
 						}
 					}
 					else {
						Float sleep = SleepChecker.getSleep(sender);
 						
 						if (sender instanceof Player) {
 							if (sleep != null) {
 								sender.sendMessage("Your sleep is now at: " + Float.toString(sleep) + "%");
 								
 								return true;
 							}
 							else {
 								sender.sendMessage(ChatColor.RED + "For some reason we cannot find your sleep. Contact the admin.");
 								
 								return true;
 							}
 						}
 						else {
 							sender.sendMessage(ChatColor.RED + "Sorry, you can only use /sleep check <Player> in the console.");
 							
 							return true;
 						}						
 					}
 				}
 				else if (args[0].equalsIgnoreCase("help")) {
 					Help.printHelp(sender);
 					
 					return true;
 				}
 				else {
 					Help.printHelp(sender);
 					
 					return true;
 				}
 
 			}
 			else {
 				Help.printHelp(sender);
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	@EventHandler
 	public void PlayerJoinEvent(PlayerJoinEvent event) {		
 		try {
 			data = (Map) SLAPI.load("RealSleepData");
 		} catch (Exception e) {
 			log.warning("Could not find RealSleepData! This is not normal! Please reload plugin (/reload) to create file!");
 			e.printStackTrace();
 			
 			return;
 		}
 		
 		if (!data.containsKey(event.getPlayer().getName())) {
 			log.info("Is this the first time " + event.getPlayer().getName() + " is joining? Personal Sleep data not found. Creating.");
 			
 			data.put(event.getPlayer().getName(), (float) 100.0);
 			
 			try {
 				SLAPI.save(data, "RealSleepData");
 			} catch (Exception e) {
 				log.warning("RealSleepData cannot be found! This should not be showing in the first place! Failed to create player data! Note: Plugin requires reload.");
 				e.printStackTrace();
 			}
 		}
 	}
 }
