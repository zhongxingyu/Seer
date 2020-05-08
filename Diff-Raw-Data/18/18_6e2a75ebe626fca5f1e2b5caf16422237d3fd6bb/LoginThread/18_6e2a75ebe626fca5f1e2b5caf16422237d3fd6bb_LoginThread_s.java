 package org.vijos.auth.thread;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import org.vijos.auth.VijosLogin;
 import org.vijos.auth.data.Sessions;
 // import org.vijos.auth.lib.Hash;
 import org.vijos.auth.lib.ConsoleLogger;
 import org.vijos.auth.lib.API;
 
 public class LoginThread extends Thread {
 	
 	public String username;
 	public String password;
 	public VijosLogin plugin;
 	public Player player;
 	static int repeater = 0;
 	
 	public LoginThread(String username, String password, VijosLogin plugin, Player player, Boolean putLoging) {
 		this.username = username;
 		this.plugin = plugin;
 		this.player = player;
 		this.password = password;
 		
 		this.start();
 	}
 	
 	public void run() {
 		if (!this.player.isOnline())
 			return;
 		
 		if (this.getLogin()) {
 			Sessions.i().setLogin(this.player);
 			this.player.sendMessage("\u00A7a[GeoPalz]\u00A7f \u00A7bLogged in successfully.\u00A7f");
 			ConsoleLogger.i().info(player.getName() + "[" + player.getAddress().getAddress().getHostAddress() + "] logged in");
 		} else {
 			this.player.sendMessage("\u00A7a[GeoPalz]\u00A7f \u00A7cWrong username or password!\u00A7f");
 		}
 	}
 	
 	public boolean getLogin() {
 		final int line = API.i().getLogin(this.username, this.password);
 		
 		ConsoleLogger.i().info(String.valueOf(line));
 		
 		Long time = Long.parseLong(String.valueOf(line * 20));
 		
 		if (line > -1) {
 			if (Sessions.i().locations.containsKey(this.username))
 				// Let the player do things
 				player.teleport(Sessions.i().locations.get(this.username));
 			
 				// Store username & pass
 				Sessions.i().usernames.put(player.getName().toLowerCase(), this.username);
 				Sessions.i().passwords.put(player.getName().toLowerCase(), this.password);
 				
 				// Give the user an item
				if (line > 3000)
 					player.getInventory().addItem(new ItemStack(46, 10));
 				
 				// Increment time every second
 				repeater = VijosLogin.i().getServer().getScheduler().scheduleSyncRepeatingTask(VijosLogin.i(), new Runnable() {
 					int starting_time = line;
 					int played_time = 0;
 					
 					@Override
 					public void run() {
 						if (Sessions.i().usernames.containsKey(player.getName().toLowerCase())) {
 							int remaining_time = starting_time - played_time;
 							
 							// Let the player know how many seconds they have left if time remaining is a multiple of 5 minutes
 							if (remaining_time % 300 == 0 || remaining_time == starting_time)
 								player.sendMessage("You have " + String.valueOf(remaining_time) + " seconds of play time remaining.");
 							
 							// Increment played time by 1 second
 							played_time++;
 							ConsoleLogger.i().info(String.valueOf(Sessions.i().played_times.put(player.getName().toLowerCase(), played_time)));
 						} else {
 							VijosLogin.i().getServer().getScheduler().cancelTask(repeater);
 						}
 					}
 				}, 0L, 20L);
 				
 				// Kick player when their time runs out
 				VijosLogin.i().getServer().getScheduler().runTaskLaterAsynchronously(VijosLogin.i(), new Runnable() {
 					public void run() {
 						Sessions.i().delLogin(player);
 						player.kickPlayer("Your points have expired, earn more to keep playing...");
 					}
 				}, time);
 			
 			return true;
 		}
 		return false;
 	}
 }
