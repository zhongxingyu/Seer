 package com.skyost.auth;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.skyost.auth.listeners.Listeners;
 import com.skyost.auth.tasks.SessionsTask;
 import com.skyost.auth.tasks.UpdateTask;
 import com.skyost.auth.utils.PasswordUtils;
 import com.skyost.auth.utils.Metrics;
 
 public class AuthPlugin extends JavaPlugin {
 	
 	public ConfigFile config;
 	public MessagesFile messages;
 	public PasswordUtils pwdutils;
 	
 	public HashMap<String, String> sessions = new HashMap<String, String>();
 	public HashMap<String, Integer> tried = new HashMap<String, Integer>();
 	
 	@Override
 	public void onEnable() {
 		try {
 			config = new ConfigFile(this);
 			config.init();
 			messages = new MessagesFile(this);
 			messages.init();
 			pwdutils = new PasswordUtils();
 			config.PluginFile = this.getFile().toString();
 			config.save();
 			startMetrics();
 			this.getServer().getPluginManager().registerEvents(new Listeners(this), this);
			this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new UpdateTask(this), 0, 8640000);
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public void startMetrics() {
 		try {
 		    Metrics metrics = new Metrics(this);
     		metrics.createGraph("EncryptGraph").addPlotter(new Metrics.Plotter("Encrypting password") {	
     			@Override
     			public int getValue() {	
     				return 1;
     			}
     			
     			@Override
     			public String getColumnName() {
     				if(config.EncryptPassword) {
     					return "Yes";
     				}
     				else {
     					return "No";
     				}
     			}
     		});
 		    metrics.start();
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public boolean isLogged(Player player) {
 		if(sessions.get(player.getName()) != null) {
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		Player player = null;
 		String playername = null;
 		
 		if(sender instanceof Player) {
 			player = (Player) sender;
 			playername = player.getName();
 		}
 		else {
 			sender.sendMessage(ChatColor.RED + "[Skyauth] Please do this from the game !");
 			return true;
 		}
 		
 		if(cmd.getName().equalsIgnoreCase("login")) {
 			if(args.length == 1) {
 				if(!isLogged(player)) {
 					if(config.Accounts.get(playername) != null) {
 						String truepassword = config.Accounts.get(playername);
 						if(config.EncryptPassword) {
 							truepassword = pwdutils.decrypt(truepassword);
 						}
 						if(truepassword.equals(args[0])) {
 							sessions.put(playername, player.getAddress().getHostString());
 							player.sendMessage(messages.Messages_4);
 							this.getServer().getScheduler().runTaskLaterAsynchronously(this, new SessionsTask(this, player), 1000 * config.SessionLength);
 						}
 						else {
 							if(tried.get(playername) == null) {
 								tried.put(playername, 1);
 							}
 							else {
 								tried.put(playername, tried.get(playername) + 1);
 							}
 							if(tried.get(playername) == config.MaxTry) {
 								player.kickPlayer(messages.Messages_11);
 							}
 							player.sendMessage(messages.Messages_3);
 						}
 					}
 					else {
 						player.sendMessage(messages.Messages_12);
 					}
 				}
 				else {
 					player.sendMessage(messages.Messages_2);
 				}
 			}
 			else {
 				return false;
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("register")) {
 			try {
 				if(args.length == 2) {
 					if(!isLogged(player)) {
 						if(config.Accounts.get(playername) == null) {
 							if(args[0].equals(args[1])) {
 								if(config.EncryptPassword) {
 									config.Accounts.put(playername, pwdutils.encrypt(args[0]));
 								}
 								else {
 									config.Accounts.put(playername, args[0]);
 								}
 								int code = new Random().nextInt(99999999);
 								config.Codes.put(playername, code);
 								config.save();
 								String message = messages.Messages_7.replaceAll("/code/", "" + code);
 								player.sendMessage(message);
 							}
 							else {
 								player.sendMessage(messages.Messages_6);
 							}
 						}
 						else {
 							player.sendMessage(messages.Messages_5);
 						}
 					}
 					else {
 						player.sendMessage(messages.Messages_2);
 					}
 				}
 				else {
 					return false;
 				}
 			}
 			catch(Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 		if(cmd.getName().equalsIgnoreCase("change")) {
 			try {
 				if(args.length == 3) {
 					if(config.Accounts.get(playername) != null) {
 						if(args[1].equals(args[2])) {
 							if(config.Codes.get(playername) == Integer.parseInt(args[0])) {
 								if(config.EncryptPassword) {
 									config.Accounts.put(playername, pwdutils.encrypt(args[1]));
 								}
 								else {
 									config.Accounts.put(playername, args[1]);
 								}
 								sessions.remove(playername);
 								config.save();
 								player.sendMessage(messages.Messages_10);
 							}
 							else {
 								player.sendMessage(messages.Messages_9);
 							}
 						}
 						else {
 							player.sendMessage(messages.Messages_6);
 						}
 					}
 					else {
 						player.sendMessage(messages.Messages_12);
 					}
 				}
 				else {
 					return false;
 				}
 			}
 			catch(Exception ex) {
 				player.sendMessage(messages.Messages_9);
 			}
 		}
 		return true;
 	}
 }
