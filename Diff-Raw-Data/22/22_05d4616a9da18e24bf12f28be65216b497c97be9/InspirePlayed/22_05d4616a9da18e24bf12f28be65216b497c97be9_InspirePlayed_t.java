 /**
  * InspirePlayed - A Bukkit playtime plugin for Minecraft
  * Copyright (C) 2011 Melvin "VeiN" Chien <melvin.chien@gmail.com>
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
 
 package com.gmail.melvinchien.InspirePlayed;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 public class InspirePlayed extends JavaPlugin{
 	private InspirePlayedPlayerListener playerListener;
 	private PluginDescriptionFile pdf;
 	public HashMap<String, InspireTime> mapTimes;
 	static String mainDirectory = "plugins/InspirePlayed/";
 	static File timesFile = new File(mainDirectory + "playtimes.txt");
 	Logger log = Logger.getLogger("Minecraft");
 
 
 	public void onDisable() {
 		saveData();
 		log.info("[" + pdf.getName() + "] " + pdf.getName() + " " + 
 				pdf.getVersion() + " is disabled.");
 	}
 
 	public void onEnable() {
 		playerListener = new InspirePlayedPlayerListener(this);
 		pdf = this.getDescription();
 		mapTimes = new HashMap<String, InspireTime>();
 
 		// Create directory and file if they do not already exist
 		new File(mainDirectory).mkdir();
 		if (!timesFile.exists()) {
 			try {
 				timesFile.createNewFile();
 			} catch (IOException e) {e.printStackTrace();}
 		}
 		else
 			loadData();
 
 		// Register events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Normal, this);
 
 		log.info("[" + pdf.getName() + "] " + pdf.getName() + " " + 
 				pdf.getVersion() + " is enabled.");
 	}
 
 	public void saveData() {
 		try {
 			FileWriter fileOut = new FileWriter(timesFile);
 			BufferedWriter out = new BufferedWriter (fileOut);
 			out.write(pdf.getVersion() + "\n");
 			Iterator<Map.Entry<String, InspireTime>> mapIt = mapTimes.entrySet().iterator();
 			while (mapIt.hasNext()) {
 				Map.Entry<String, InspireTime> entry = (Map.Entry<String, InspireTime>)mapIt.next();
 				InspireTime it = entry.getValue();
 				out.write(entry.getKey() + ";" + it.playtime + ";" + it.lastactive + "\n");
 			}
 			out.close();
 			log.info("[" + pdf.getName() + "] " + "Data successfully saved.");
 		} catch (IOException e) {e.printStackTrace();}
 	}
 
 	public void loadData() {
 		try {
 			FileReader fileIn = new FileReader(timesFile);
 			BufferedReader in = new BufferedReader(fileIn);
 			String inLine = in.readLine();
 			// Check for old version of playtimes.txt
 			if (inLine.equals(pdf.getVersion()))
 				while (in.ready()) {
 					inLine = in.readLine();
 					String[] inText = inLine.split(";");
 					long playtime = Long.parseLong(inText[1]);
					String lastactive = "0000000000";
					if (inText.length == 3)
						lastactive = inText[2];
 					InspireTime it = new InspireTime(playtime, lastactive);
 					mapTimes.put(inText[0], it);
 				}
 			else
 				while (in.ready()) {
 					inLine = in.readLine();
 					StringTokenizer st = new StringTokenizer(inLine, "=");
 					String player = st.nextToken();
 					long playtime = Long.parseLong(st.nextToken());
					InspireTime it = new InspireTime(playtime, "0000000000");
 					mapTimes.put(player, it);
 				}
 			in.close();
 			log.info("[" + pdf.getName() + "] " + "Data successfully loaded.");
 		} catch (IOException e) {e.printStackTrace();}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("played")) {
 			String player;
 			boolean online = false;
 			if (args.length == 0) {
 				player = ((Player)sender).getName();
 				online = true;
 			} else if (args[0].equalsIgnoreCase("help")) {
 				return false;
 			} else {
 				player = args[0];
 			}
 			Iterator<Map.Entry<String, InspireTime>> mapIt = mapTimes.entrySet().iterator();
 			while (mapIt.hasNext()) {
 				Map.Entry<String, InspireTime> entry = (Map.Entry<String, InspireTime>)mapIt.next();
 				if (entry.getKey().equalsIgnoreCase(player)) {
 					player = entry.getKey();
 					InspireTime it = entry.getValue();
 					if (online)
 						it.updatePlaytime();
 					sender.sendMessage(ChatColor.GREEN + "[IP] " + ChatColor.RED + 
 							player + ChatColor.WHITE + " has played for " + 
 							ChatColor.GREEN + it.getPlaytime());
 					sender.sendMessage(ChatColor.GREEN + "[IP] " + ChatColor.RED + 
 							player + ChatColor.WHITE +  "'s last activity was on " + 
 							ChatColor.GREEN + it.getLastActive());
 					return true;
 				}
 			}
 			sender.sendMessage(ChatColor.GREEN + "[IP] " + ChatColor.RED + 
 					player + ChatColor.WHITE + " does not exist!");
 			return true;
 		}
 		return false;
 	}
 }
