 package com.carlgo11.automessage.main;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.plugin.java.JavaPlugin;
 	public class Main extends JavaPlugin{
 		static int random = 0;
 	public int tick = 1;
 	     String color = null;
 		 ChatColor realcolor = null;
 		 ChatColor prefixcolor = null;
 		 int limit = 0;
 		 int x = 1;
 		 int z = 1;
 		 int n = 1;
 		 int intime = 1;
 		 int time = 20;
 		 long ltime = 0;
 		 long testtime = 40;
 		public final static Logger  logger = Logger.getLogger("Minercraft");
 		public void onEnable(){
 			File config = new File(this.getDataFolder(), "config.yml");
 			if(!config.exists()){
 				this.saveDefaultConfig();
 				System.out.println("[AutoMessage] No config.yml detected, config.yml created");
 			}
 			getLogger().info(getDescription().getName() + getDescription().getVersion() + " Is Enabled!");
 	    Prefixcolors();
 		colors();
 		Main2();
 		Time();
 		
 		}
 	
 		public void onDisable(){
 			getLogger().info(getDescription().getName() + getDescription().getVersion() + " Is Disabled!");
 		}
 		
 		public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 			String prefix = getConfig().getString("Prefix");
 			if(cmd.getName().equalsIgnoreCase("reloadmsg")){
 				if(sender.hasPermission("AutoMsg.cmd.reloadmsg")){
 				this.reloadConfig();
 				Prefixcolors();
 				colors();
 				Time();
 				limit = getConfig().getInt("limit");
 				sender.sendMessage(prefixcolor + "[" + prefix + "]  " + ChatColor.GREEN + "Automessage reloaded!");
 				} else {
 					sender.sendMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + ChatColor.RED + "Error: You don't have permission.");
 				}
 			}
 				return true;
 		}
 		
 		@EventHandler
 		public void Time(){
 			intime = getConfig().getInt("time");
			time = 1;
			ltime = 1;
 			time *= intime;
 			ltime=time;
 			System.out.println("intime: " + intime);
 			System.out.println("time: " + time);
 			ltime=time;
 			System.out.println("Ltime: " + ltime);
 		}
 		
 		@EventHandler
 		public void Prefixcolors(){
 			
 			color = getConfig().getString("Prefix-Color");
 			if(color.equalsIgnoreCase("0") || color.equalsIgnoreCase("black")){
 				prefixcolor = ChatColor.BLACK;
 				Main.logger.info("converted color > prefixcolor");
 			
 			} 
 				if(color.equalsIgnoreCase("1") || color.equalsIgnoreCase("blue")){
 					prefixcolor = ChatColor.DARK_BLUE;
 					Main.logger.info("converted color > prefixcolor");
 				
 				}
 					if(color.equalsIgnoreCase("2") || color.equalsIgnoreCase("green")){
 						prefixcolor = ChatColor.DARK_GREEN;
 						Main.logger.info("converted color > prefixcolor");
 					}
 						if(color.equalsIgnoreCase("3") || color.equalsIgnoreCase("Cyan")){
 							prefixcolor = ChatColor.DARK_AQUA;
 							Main.logger.info("converted color > prefixcolor");
 						} 
 			if(color.equalsIgnoreCase("4") || color.equalsIgnoreCase("Red")){
 				prefixcolor = ChatColor.DARK_RED;
 				Main.logger.info("converted color > prefixcolor");
 			} 
 				if(color.equalsIgnoreCase("5") || color.equalsIgnoreCase("Purple")){
 					prefixcolor = ChatColor.DARK_PURPLE;
 					Main.logger.info("converted color > prefixcolor");
 				} 
 					if(color.equalsIgnoreCase("6") || color.equalsIgnoreCase("Yellow")){
 						prefixcolor = ChatColor.YELLOW;
 						Main.logger.info("converted color > prefixcolor");
 					} 
 						if(color.equalsIgnoreCase("7") || color.equalsIgnoreCase("Gray")){
 							prefixcolor = ChatColor.DARK_GRAY;
 							Main.logger.info("converted color > prefixcolor");
 						} 
 							if(color.equalsIgnoreCase("8") || color.equalsIgnoreCase("Light_Gray")){
 								prefixcolor = ChatColor.GRAY;
 								Main.logger.info("converted color > prefixcolor");
 							} 
 								if(color.equalsIgnoreCase("9") || color.equalsIgnoreCase("Light_Blue")){
 									prefixcolor = ChatColor.BLUE;
 									Main.logger.info("converted color > prefixcolor");
 								} 
 									if(color.equalsIgnoreCase("A") || color.equalsIgnoreCase("Light_GREEN")){
 										prefixcolor = ChatColor.GREEN;
 										Main.logger.info("converted color > prefixcolor");
 									}
 										if(color.equalsIgnoreCase("B") || color.equalsIgnoreCase("Light_Cyan")){
 											prefixcolor = ChatColor.AQUA;
 											Main.logger.info("converted color > prefixcolor");
 										} 
 											if(color.equalsIgnoreCase("c") || color.equalsIgnoreCase("Light_Red")){
 												prefixcolor = ChatColor.RED;
 												Main.logger.info("converted color > prefixcolor");
 											} 
 												if(color.equalsIgnoreCase("d") || color.equalsIgnoreCase("Light_Purple")){
 													prefixcolor = ChatColor.LIGHT_PURPLE;
 													Main.logger.info("converted color > prefixcolor");
 												} 
 													if(color.equalsIgnoreCase("e") || color.equalsIgnoreCase("Light_Yellow")){
 														prefixcolor = ChatColor.GOLD;
 														Main.logger.info("converted color > prefixcolor");
 													}
 													if(color.equalsIgnoreCase("f") || color.equalsIgnoreCase("White")){
 														prefixcolor = ChatColor.WHITE;
 														Main.logger.info("converted color > prefixcolor");
 													}
 		}
 		@EventHandler
 		public void colors(){
 			color = getConfig().getString("Color");
 			if(color.equalsIgnoreCase("0") || color.equalsIgnoreCase("black")){
 				realcolor = ChatColor.BLACK;
 				Main.logger.info("converted color > realcolor");
 			
 			} 
 				if(color.equalsIgnoreCase("1") || color.equalsIgnoreCase("blue")){
 					realcolor = ChatColor.DARK_BLUE;
 					Main.logger.info("converted color > realcolor");
 				
 				}
 					if(color.equalsIgnoreCase("2") || color.equalsIgnoreCase("green")){
 						realcolor = ChatColor.DARK_GREEN;
 						Main.logger.info("converted color > realcolor");
 					}
 						if(color.equalsIgnoreCase("3") || color.equalsIgnoreCase("Cyan")){
 							realcolor = ChatColor.DARK_AQUA;
 							Main.logger.info("converted color > realcolor");
 						} 
 			if(color.equalsIgnoreCase("4") || color.equalsIgnoreCase("Red")){
 				realcolor = ChatColor.DARK_RED;
 				Main.logger.info("converted color > realcolor");
 			} 
 				if(color.equalsIgnoreCase("5") || color.equalsIgnoreCase("Purple")){
 					realcolor = ChatColor.DARK_PURPLE;
 					Main.logger.info("converted color > realcolor");
 				} 
 					if(color.equalsIgnoreCase("6") || color.equalsIgnoreCase("Yellow")){
 						realcolor = ChatColor.YELLOW;
 						Main.logger.info("converted color > realcolor");
 					} 
 						if(color.equalsIgnoreCase("7") || color.equalsIgnoreCase("Gray")){
 							realcolor = ChatColor.DARK_GRAY;
 							Main.logger.info("converted color > realcolor");
 						} 
 							if(color.equalsIgnoreCase("8") || color.equalsIgnoreCase("Light_Gray")){
 								realcolor = ChatColor.GRAY;
 								Main.logger.info("converted color > realcolor");
 							} 
 								if(color.equalsIgnoreCase("9") || color.equalsIgnoreCase("Light_Blue")){
 									realcolor = ChatColor.BLUE;
 									Main.logger.info("converted color > realcolor");
 								} 
 									if(color.equalsIgnoreCase("A") || color.equalsIgnoreCase("Light_GREEN")){
 										realcolor = ChatColor.GREEN;
 										Main.logger.info("converted color > realcolor");
 									}
 										if(color.equalsIgnoreCase("B") || color.equalsIgnoreCase("Light_Cyan")){
 											realcolor = ChatColor.AQUA;
 											Main.logger.info("converted color > realcolor");
 										} 
 											if(color.equalsIgnoreCase("c") || color.equalsIgnoreCase("Light_Red")){
 												realcolor = ChatColor.RED;
 												Main.logger.info("converted color > realcolor");
 											} 
 												if(color.equalsIgnoreCase("d") || color.equalsIgnoreCase("Light_Purple")){
 													realcolor = ChatColor.LIGHT_PURPLE;
 													Main.logger.info("converted color > realcolor");
 												} 
 													if(color.equalsIgnoreCase("e") || color.equalsIgnoreCase("Light_Yellow")){
 														realcolor = ChatColor.GOLD;
 														Main.logger.info("converted color > realcolor");
 													}
 													if(color.equalsIgnoreCase("f") || color.equalsIgnoreCase("White")){
 														realcolor = ChatColor.WHITE;
 														Main.logger.info("converted color > realcolor");
 													}
 		}
 		@EventHandler
 		public void Main2(){
 			limit = getConfig().getInt("limit");
 			Main.logger.info("limit: "  + limit);
 			if(realcolor == null){
 				System.out.print("[AutoMessage] Error: Couldn't load the color fron config.yml!");
 				onDisable();
 			}
 			if(prefixcolor == null){
 				System.out.print("[AutoMessage] Error: Couldn't load the prefix-color fron config.yml!");
 				onDisable();
 			}
 			
 			Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable(){
 				public void run(){
 					if(tick == 1){
 						if(limit > 0 ){
 					Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + ChatColor.RESET + realcolor + getConfig().getString("one"));
 					tick++;
 						} else {
 							tick=1;
 						}
 					} else
 					if(tick == 2){
 						if(limit > 1){
 						Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + ChatColor.RESET + realcolor + getConfig().getString("two"));
 						tick++;
 						} else {
 							tick=1;
 						}
 					} else
 					if(tick == 3){
 						if(limit > 2){
 						Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + ChatColor.RESET + realcolor + getConfig().getString("three"));
 						tick++;
 						} else {
 							tick=1;
 						}
 					} else
 					if(tick == 4){
 						if(limit > 3){
 						Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + ChatColor.RESET + realcolor + getConfig().getString("four"));
 						tick++;
 						} else {
 							tick=1;
 						}
 					} else
 					if(tick == 5){
 						if(limit > 4){
 						Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + ChatColor.RESET + realcolor + getConfig().getString("five"));
 						tick++;
 						} else {
 							tick=1;
 						}
 					} else
 					if(tick == 6){
 						if(limit > 5){
 						Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + ChatColor.RESET + realcolor + getConfig().getString("six"));
 						tick++;
 						} else {
 								tick=1;
 							}
 						} else
 					if(tick == 7){
 						if(limit > 6){
 						Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + ChatColor.RESET + realcolor + getConfig().getString("seven"));
 						tick++;
 						} else {
 						tick=1;
 						   }
 						} else
 						
 					if(tick == 8){
 						if(limit > 7){
 						Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + ChatColor.RESET + realcolor + getConfig().getString("eight"));
 						tick++;
 						} else {
 						tick=1;
 						}
 						} else
 					if(tick == 9){
 						if(limit > 8){
 						Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + realcolor + getConfig().getString("nine"));
 						tick++;
 						} else {
 						  tick=1;
 						}
 						} else
 					if(tick == 10){
 						if(limit > 9){
 						Bukkit.broadcastMessage(prefixcolor + "[" + getConfig().getString("Prefix") + "]  " + realcolor + getConfig().getString("ten"));
 						tick = 1;
 						} else {
 							tick=1;
 						}
 						} else
 							
 							
 						{
 						if(tick != 5 || tick != 4 || tick != 3 || tick != 2 || tick != 1){
 							Main.logger.warning("[AutoMessage] " + "Error: 303  " + "tick: " + tick);
 							
 							onError();
 							
 						}
 					}
 				}
 			}, 50L, getConfig().getLong("time"));
 			
 			
 			
 		}
 		
 		
 	
 		
 		
 		public void onError(){
 			Main.logger.warning("[AutoMessage] Error acurred! Plugin Disabeled!");
 			Bukkit.getPluginManager().disablePlugin(this);
 			
 		}
 		
 	
 	}
 		
 	
 
 
