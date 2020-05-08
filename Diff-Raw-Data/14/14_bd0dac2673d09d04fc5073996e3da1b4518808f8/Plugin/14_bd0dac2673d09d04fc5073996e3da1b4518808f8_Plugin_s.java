 /*
  * This file is part of GhostBuster.
  *
  * GhostBuster is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GhostBuster is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with GhostBuster.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * Author: Robert 'Bobby' Zenz
  * Website: http://www.bonsaimind.org
  * GitHub: https://github.com/RobertZenz/org.bonsaimind.bukkitplugins/tree/master/Plugin
  * E-Mail: bobby@bonsaimind.org
  */
 package org.bonsaimind.bukkitplugins.ghostbuster;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 /**
  *
  * @author Robert 'Bobby' Zenz
  */
 public class Plugin extends JavaPlugin {
 
 	private Server server = null;
 	private BukkitScheduler scheduler = null;
 	private PlayerLoginListener playerListener = new PlayerLoginListener(this);
 	private EntityDeathListener entityListener = new EntityDeathListener(this);
 	private Winston winston;
 
 	public void onDisable() {
 		winston.save();
 		winston = null;
 
 		playerListener = null;
 		entityListener = null;
 
 		server = null;
 	}
 
 	public void onEnable() {
 		server = getServer();
 		scheduler = server.getScheduler();
 
 		PluginManager pm = server.getPluginManager();
 		pm.registerEvent(Type.PLAYER_LOGIN, playerListener, Priority.Highest, this);
 		pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Monitor, this);
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled.");
 
 		winston = new Winston("./plugins/GhostBuster/");
 		winston.load();
 
 		setCommand();
 	}
 
 	protected void banPlayer(Player player) {
 		if (!winston.isExcepted(player.getName())) {
 			if (!winston.getFreeSlotsMode() || server.getOnlinePlayers().length >= server.getMaxPlayers()) {
 				// Now ban the bastard!
 				winston.banPlayer(player.getName());
 
 				// Now kick the player
 				final Player finalizedPlayer = player;
 				scheduler.scheduleAsyncDelayedTask(this, new Runnable() {
 
 					public void run() {
 						finalizedPlayer.kickPlayer(prepareMessage(winston.getDeathMessage(), winston.getBanExpiration(finalizedPlayer.getName())));
 					}
 				}, 4);
 			}
 		}
 	}
 
 	protected void checkPlayer(PlayerLoginEvent event) {
 		// Check if the player is allowed to join
 		if (winston.isBanned(event.getPlayer().getName())) {
 			// Nope, it's not...
 			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, prepareMessage(winston.getStillDeadMessage(), winston.getBanExpiration(event.getPlayer().getName())));
 		}
 	}
 
 	/**
 	 * Replace %h and %m.
 	 * @param message The message.
 	 * @param timeLeft Time left, in minutes!
 	 * @return
 	 */
 	private String prepareMessage(String message, long timeLeft) {
 		message = message.replace("%h", Long.toString(timeLeft / 60));
 		message = message.replace("%m", Long.toString(timeLeft % 60));
 
 		return message;
 
 	}
 
 	private void setCommand() {
 		getCommand("ghostbuster").setExecutor(new CommandExecutor() {
 
 			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 				if (!sender.isOp()) {
 					sender.sendMessage("I'm sorry, Dave. I'm afraid I can't do that.");
 					return true;
 				}
 
 				if (args.length == 0) {
 					return false;
 				}
 
 				for (int idx = 0; idx < args.length; idx++) {
 					String arg = args[idx];
 
 					if (arg.equalsIgnoreCase("ban")) {
						if (idx >= args.length) {
 							return false;
 						}
 
 						if (winston.banPlayer(args[idx + 1])) {
 							sender.sendMessage("Done.");
 						} else {
 							sender.sendMessage("That player is on the exception list.");
 						}
 					} else if (arg.equalsIgnoreCase("except")) {
						if (idx >= args.length) {
 							return false;
 						}
 
 						if (winston.addException(args[idx + 1])) {
 							sender.sendMessage("Done.");
 						} else {
 							sender.sendMessage("That player is already on the exception list.");
 						}
 					} else if (arg.equalsIgnoreCase("info")) {
						if (idx >= args.length) {
 							return false;
 						}
 
 						if (winston.isExcepted(args[idx + 1])) {
 							sender.sendMessage("That player is on the exception list.");
 						} else {
 							long expires = winston.getBanExpiration(args[idx + 1]);
 							if (expires > 0) {
 								sender.sendMessage(prepareMessage("Banned for another %h hours and %m minutes.", expires));
 							} else {
 								sender.sendMessage("That player is not banned.");
 							}
 						}
 					} else if (arg.equalsIgnoreCase("reload")) {
 						winston.reload();
 						sender.sendMessage("Done.");
 					} else if (arg.equalsIgnoreCase("save")) {
 						winston.save();
 						sender.sendMessage("Done.");
 					} else if (arg.equalsIgnoreCase("unban")) {
						if (idx >= args.length) {
 							return false;
 						}
 
 						if (winston.unbanPlayer(args[idx + 1])) {
 							sender.sendMessage("Done.");
 						} else {
 							sender.sendMessage("That player is not banned.");
 						}
 					} else if (arg.equalsIgnoreCase("unban_all")) {
 						winston.unbanAll();
 						sender.sendMessage("Done.");
 					} else if (arg.equalsIgnoreCase("unexcept")) {
						if (idx >= args.length) {
 							return false;
 						}
 
 						if (winston.removeException(args[idx + 1])) {
 							sender.sendMessage("Done.");
 						} else {
 							sender.sendMessage("That player is not on the exception list.");
 						}
 					}
 				}
 
 				return true;
 			}
 		});
 	}
 }
