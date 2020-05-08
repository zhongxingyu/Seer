 	package com.alta189.minemail.command;
 
 	
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
 				Boolean payed = false;
 				//iConomy Support\\
 				
 				if (!this.plugin.isFree(player)) {
 					if (this.plugin.config.settingsFile.iConomyEnabled && this.plugin.iConomy != null) {
 						if (this.plugin.addons.iConomyManager.hasAccount(player) && this.plugin.addons.iConomyManager.hasValidAccount(player)) {
 							if (this.plugin.addons.iConomyManager.hasEnough(this.plugin.config.settingsFile.costReceive, this.plugin.addons.iConomyManager.getHoldings(player.getName()))) {
 								this.plugin.addons.iConomyManager.subtract(this.plugin.config.settingsFile.costReceive, this.plugin.addons.iConomyManager.getHoldings(player.getName()));
 								payed = true;
 							} else {
 								this.plugin.addons.msgFormat.formatAndSend("<error>You do not have enough in your iConomy Account", player);
 							}
 						} else {
							this.plugin.addons.msgFormat.formatAndSend("<error>You do not have a valid iConomy Account", player);
 						}
 					}
 					
 				}
 				if (payed) {
 					this.plugin.addons.msgFormat.formatAndSend("<header>MineMail <c1>- " + this.plugin.addons.iConomyManager.formatAmount(this.plugin.config.settingsFile.costReceive) + " was subtracted from your account", player);
 				}
 				plugin.mmServer.getMail(player);
 			} else {
 				this.plugin.addons.msgFormat.formatAndSend("<header>MineMail - <c1>No Messages", player);
 			}
 			
 		}
 		
 		public void write(Player player, Command cmd, String commandLabel, String[] args) {
 			String receiver = args[1].toLowerCase();
 			String message = "";
 			Integer count = 3;
 			Boolean formatErrors = false;
 			Boolean payed = false;
 
 			while (count <= args.length) {
 				if (count == 3) {
 					message = args[count - 1];
 				} else {
 					message = message + " " + args[count - 1];
 				}
 				count = count + 1;
 			}
 			
 			//iConomy Support\\
 			
 			if (!this.plugin.isFree(player)) {
 				if (this.plugin.config.settingsFile.iConomyEnabled && this.plugin.iConomy != null) {
 					if (this.plugin.addons.iConomyManager.hasAccount(player) && this.plugin.addons.iConomyManager.hasValidAccount(player)) {
 						if (this.plugin.addons.iConomyManager.hasEnough(this.plugin.config.settingsFile.costSend, this.plugin.addons.iConomyManager.getHoldings(player.getName()))) {
 							this.plugin.addons.iConomyManager.subtract(this.plugin.config.settingsFile.costSend, this.plugin.addons.iConomyManager.getHoldings(player.getName()));
 						} else {
 							this.plugin.addons.msgFormat.formatAndSend("<error>You do not have enough in your iConomy Account", player);
 						}
 					} else {
 						this.plugin.addons.msgFormat.formatAndSend("<error>You do not have a valid iConomy Account", player);
 					}
 				}
 				
 			}
 			
 			if (this.plugin.addons.formatSQL.check(message)) {
 				message = this.plugin.addons.formatSQL.fix(message);
 				formatErrors = true;
 			}
 				plugin.mmServer.sendMail(player.getName(), receiver, message);
 				
 				if (formatErrors) {
 					this.plugin.addons.msgFormat.formatAndSend("<c1>Your message has been sent, <error>but there were format errors.", player);
 					this.plugin.addons.msgFormat.formatAndSend("<error>Use /mail format to see what characters are not allowed", player);
 					if (payed) {
 						this.plugin.addons.msgFormat.formatAndSend("<header>MineMail <c1>- " + this.plugin.addons.iConomyManager.formatAmount(this.plugin.config.settingsFile.costSend) + " was subtracted from your account", player);
 					}
 				} else {
 					this.plugin.addons.msgFormat.formatAndSend("<c1>Your message has been sent", player);
 					if (payed) {
 						this.plugin.addons.msgFormat.formatAndSend("<header>MineMail <c1>- " + this.plugin.addons.iConomyManager.formatAmount(this.plugin.config.settingsFile.costSend) + " was subtracted from your account", player);
 					}
 				}
 				plugin.notifyReceiver(receiver); 
 			
 		}
 		
 		public void wipe(Player player, Command cmd, String commandLabel, String[] args) {
 			try {
 				if (plugin.isAdmin(player, "wipe")){
 					if(plugin.dbManage.checkTable("mails")){
 						if (!plugin.ScheduledWipe) plugin.mmServer.ScheduleWipe();
 						this.plugin.addons.msgFormat.formatAndSend("<header>The database will be wiped in 1 minute!", player);
 					} else {
 						this.plugin.addons.msgFormat.formatAndSend("<error>Could not wipe database.", player);
 					}
 				} else {
 					this.plugin.addons.msgFormat.formatAndSend("<error>You do not have permission to use this command!", player);
 				}
 			} catch (Exception ex) {
 				plugin.log.severe(plugin.logPrefix + "Error at command delete: " + ex);
 			}
 		}
 		
 		public void help(Player player, Command cmd, String commandLabel, String[] args) {
 			this.plugin.addons.msgFormat.formatAndSend("<header>---   MineMail Help   ---", player);
 			this.plugin.addons.msgFormat.formatAndSend("<c1>/mail write [player] [message] <help>- Send a message", player);
 			this.plugin.addons.msgFormat.formatAndSend("<c1>/mail read <help>- Read your messages", player);
 			this.plugin.addons.msgFormat.formatAndSend("<c1>/mail format <help>- Shows characters that will be removed from your msg", player);
 			if (plugin.isAdmin(player, "paper")) {
 				this.plugin.addons.msgFormat.formatAndSend("<c1>/mail paper <help>- Toggles reading mail by clicking with paper in hand.", player);
 			}
 			this.admin(player, cmd, commandLabel, args);
 		}
 		
 		public void reload(Player player, Command cmd, String commandLabel, String[] args) {
 			if (plugin.isAdmin(player, "reload")) {
 				this.plugin.addons.msgFormat.formatAndSend("<header>---   MineMail Reloading   ---", player);
 				//Reload Database\\
 				plugin.dbManage.close();
 				plugin.log.info("[MineMail] Database Closed.");
 				plugin.dbManage.initialize();
 				plugin.log.info("[MineMail] Database Loaded.");
 				this.plugin.addons.msgFormat.formatAndSend("<c1>Database has been reloaded", player);
 				
 				//Reload Settings\\
 				plugin.log.info("[MineMail] Settings Reloading.");
 				plugin.config.initialize();
 				plugin.log.info("[MineMail] Settings Reloaded.");
 				this.plugin.addons.msgFormat.formatAndSend("<c1>Settings have been reloaded", player);
 				this.plugin.addons.msgFormat.formatAndSend("<header>---   MineMail Reloaded   ---", player);
 				
 			} else {
 				this.plugin.addons.msgFormat.formatAndSend("<error>You do not have permission to use this command!", player);
 			}
 		}
 		
 		public void admin(Player player, Command cmd, String commandLabel, String[] args) {
 			if (plugin.isAdmin(player, "wipe|reload")) {
 				this.plugin.addons.msgFormat.formatAndSend("<c1>/mail reload <help>- Reload Mail System and settings!", player);
 				this.plugin.addons.msgFormat.formatAndSend("<c1>/mail wipe <help>- Wipes the database", player);
 			} else if (args[0].equalsIgnoreCase("help")) {
 				this.plugin.addons.msgFormat.formatAndSend("<error>You do not have permission to use this command!", player);
 			}
 		}
 		
 		public void formatHelp(Player player, Command cmd, String commandLabel, String[] args) {
 			//@#%^&*()-+=[]{}|\/":';<>~`
 			this.plugin.addons.msgFormat.formatAndSend("<c1>Characters that will be removed from message <help>- @#%^&*()-+=[]{}|\\/\":';<>~`", player);
 		}
 		
 		public void paper(Player player, Command cmd, String commandLabel, String[] args) {
 			if (plugin.isAdmin(player, "paper")) {
 				Boolean read = this.plugin.addons.managePaper.toggleReader(player.getName().toLowerCase());
 				if (read) {
 					this.plugin.addons.msgFormat.formatAndSend("<header>MineMail - <c1>Paper read is enabled", player);
 				} else {
 					this.plugin.addons.msgFormat.formatAndSend("<header>MineMail - <c1>Paper read is disabled", player);
 				}
 			} else {
 				this.plugin.addons.msgFormat.formatAndSend("<error>You do not have permission to use this command!", player);
 			}
 		}
 	}
