 /**
  * iPlayed - A Bukkit playtime plugin for Minecraft
  * Copyright (C) 2011 Melvin "inspireVeiN" Chien <melvin.chien@gmail.com>
  * @author inspireVeiN
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.inspireVeiN.iPlayed;
 
 import com.mini.Arguments;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.mini.Mini;
 
 
 public class iPlayed extends JavaPlugin {
 	private iPlayedPlayerListener playerListener;
 	private PluginDescriptionFile pdf;
 	static String folder = "plugins/iPlayed/";
 	public Mini timesdb;
 	Logger log = Logger.getLogger("Minecraft");
 
 	public void onDisable() {
 		timesdb.update();
 		log.info("[" + pdf.getName() + "] " + pdf.getName() + " " + pdf.getVersion() + " disabled.");
 	}
 
 	public void onEnable() {
 		playerListener = new iPlayedPlayerListener(this);
 		pdf = this.getDescription();
 		timesdb = new Mini(folder, "playtimes.db");
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Monitor, this);
 		log.info("[" + pdf.getName() + "] " + pdf.getName() + " " + pdf.getVersion() + " enabled.");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 		if (cmdLabel.equalsIgnoreCase("played")) {
 			String player;
 			if (args.length == 0)
 				player = ((Player)sender).getName();
 			else
 				player = args[0];
 
 			Arguments entry = null;
 			if (timesdb.hasIndex(player))
 				entry = timesdb.getArguments(player);
 			if (entry == null)
 				sender.sendMessage(playtimeMessage(player, false, entry));
 			else
 				sender.sendMessage(playtimeMessage(player, true, entry));
 			return true;
 		} else if (cmdLabel.equalsIgnoreCase("iPlayed")) {
 			if (sender.isOp()) {
 				if (args.length == 0) 
 					sender.sendMessage(ChatColor.GREEN + "[" + pdf.getName() + "] " + ChatColor.WHITE +
 							"Version " + ChatColor.BLUE + pdf.getVersion());
 				else if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
 					String player = args[1];
 					int amount = Integer.parseInt(args[2]);
 					Arguments entry = null;
 					if (timesdb.hasIndex(player))
 						entry = timesdb.getArguments(player);
 					if (entry == null)
 						sender.sendMessage(playtimeMessage(player, false, entry));
 					else {
 						int playtime = entry.getInteger("playtime") + amount;
 						entry.setValue("playtime", Integer.toString(playtime));
 						timesdb.update();
 						sender.sendMessage(ChatColor.GREEN + "[" + pdf.getName() + "] " + ChatColor.BLUE + 
 								amount + " minutes" + ChatColor.WHITE + " has been added to " + ChatColor.BLUE + 
 								player + ChatColor.WHITE + "'s playtime.");
 					}
 
 				}
 			} else
 				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
 			return true;
 		}
 		return false;
 	}
 
 	private String playtimeMessage (String player, boolean exists, Arguments entry) {
 		if (exists)
 			return ChatColor.GREEN + "[" + pdf.getName() + "] " + ChatColor.BLUE + player + ChatColor.WHITE + 
 			" has played for " + ChatColor.BLUE + formatTime(entry.getInteger("playtime")) + ChatColor.WHITE + 
			" and last login was on " + ChatColor.BLUE + entry.getValue("lastlogin");
 		else
 			return ChatColor.GREEN + "[" + pdf.getName() + "] " + ChatColor.BLUE + 
 			player + ChatColor.WHITE + " does not exist!";
 	}
 	private String formatTime(int playtime) {
 		int tempTime = playtime;
 		int timeDays = tempTime / 60 / 24;
 		tempTime -= timeDays * 24 * 60;
 		int timeHours = tempTime / 60;
 		tempTime -= timeHours * 60;
 		int timeMinutes = tempTime;
 
 		String stDays = "";
 		String stHours = "";
 		String stMinutes = "";
 		if (timeDays == 1)
 			stDays = " day, ";
 		else if (timeDays > 1)
 			stDays = " days, ";
 		if (timeHours == 1)
 			stHours = " hour, ";
 		else if (timeHours > 1)
 			stHours = " hours, ";
 		if (timeMinutes == 1)  // Special case for minutes
 			stMinutes = " minute"; 
 		else
 			stMinutes = " minutes";  // Will output N minutes or 0 minutes.
 
 		return (timeDays > 0 ? timeDays : "") + stDays + 
 		(timeHours > 0 ? timeHours : "") + stHours
 		+ timeMinutes + stMinutes;
 	}
 }
