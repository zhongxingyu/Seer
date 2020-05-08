 	package com.alta189.minemail.command;
 
 	import org.bukkit.ChatColor;
 	import org.bukkit.command.Command;
 	import org.bukkit.entity.Player;
 
 	import com.alta189.minemail.MineMail;
 
 	public class CommandHandler {
 		private MineMail plugin;
 		
 		public CommandHandler(MineMail instance) {
 			this.plugin = instance;
 		}
 		
 		public void read(Player player, Command cmd, String commandLabel, String[] args) {
 			if (plugin.mmServer.getUnreadCount(player.getName().toLowerCase()) >= 1) {
 				plugin.mmServer.getMail(player);
 			} else {
 				player.sendMessage(ChatColor.GREEN
 							+ "MineMail - No Messages");
 			}
 			
 		}
 		
 		public void write(Player player, Command cmd, String commandLabel, String[] args) {
 			String receiver = args[1].toLowerCase();
 			String message = "";
 			Integer count = 3;
 
 			while (count <= args.length) {
 				if (count == 3) {
 					message = args[count - 1];
 				} else {
 					message = message + " " + args[count - 1];
 				}
 				count = count + 1;
 			}
 
 				plugin.mmServer.sendMail(player.getName(), receiver, message);
 				player.sendMessage(ChatColor.GREEN
 						+ "Your message has been sent");
 				plugin.notifyReceiver(receiver); 
 			
 		}
 		
 		public void wipe(Player player, Command cmd, String commandLabel, String[] args) {
 			try {
 				if (plugin.isAdmin(player, "wipe")){
 					if(plugin.dbManage.checkTable("mails")){
 						if (!plugin.ScheduledWipe) plugin.mmServer.ScheduleWipe();
 						player.sendMessage(ChatColor.GREEN + "The database will be wiped in 1 minute!");
 					} else {
 						player.sendMessage(ChatColor.DARK_RED + "Could not wipe database.");
 					}
 				} else {
 					player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
 				}
 			} catch (Exception ex) {
 				plugin.log.severe(plugin.logPrefix + "Error at command delete: " + ex);
 			}
 		}
 		
 		public void help(Player player, Command cmd, String commandLabel, String[] args) {
 			player.sendMessage(ChatColor.GOLD
 					+ "---   MineMail   ---");
 			player.sendMessage(ChatColor.BLUE
 					+ "/mail write [player] [message]"
 					+ ChatColor.GREEN + "- Send a message");
 			player.sendMessage(ChatColor.BLUE + "/mail read"
 					+ ChatColor.GREEN + "- Read your messages");
 		}
 		
 		public void reload(Player player, Command cmd, String commandLabel, String[] args) {
 			if (plugin.isAdmin(player, "reload")) {
 				plugin.dbManage.close();
 				plugin.log.info("[MineMail] Database Closed.");
 				plugin.dbManage.initialize();
 				plugin.log.info("[MineMail] Database Loaded.");
 				player.sendMessage(ChatColor.GREEN
 						+ "[MineMail] Database has been reloaded");
 			} else {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
 			}
 		}
 		
 		public void admin(Player player, Command cmd, String commandLabel, String[] args) {
 			if (plugin.isAdmin(player, "wipe|reload")){
 				player.sendMessage(ChatColor.GOLD
 						+ "---   MineMail   ---");
 				player.sendMessage(ChatColor.RED + "/mail reload "
 						+ ChatColor.GREEN + "- Reload mailsystem");
 				player.sendMessage(ChatColor.RED + "/mail wipe "
 						+ ChatColor.GREEN + "- Wipes the database");
 				
 			} else {
 				player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
 			}
 		}
 	}

}
