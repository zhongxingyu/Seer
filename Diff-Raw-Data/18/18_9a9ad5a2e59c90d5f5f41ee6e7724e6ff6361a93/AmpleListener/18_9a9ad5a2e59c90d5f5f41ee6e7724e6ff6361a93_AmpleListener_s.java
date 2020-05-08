 /**
  * Ample Chat Bot is a chat bot plugin for Craft Bukkit Servers
  *   Copyright (C) 2012  matthewl
 
  *  This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
 
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
 
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jaggy.bukkit.ample;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandException;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
 import org.jaggy.bukkit.ample.config.Config;
 import org.jaggy.bukkit.ample.db.DB;
 
 @SuppressWarnings("unused")
 public class AmpleListener implements Listener {
 	private Ample plugin;
 	private static Config config;
 	private DB db;
 	private String message;
 	AmpleListener(Ample instance) {
 		plugin = instance;
 		config = plugin.getDConfig();
 		db = plugin.getDB();
 	}
 
 	
 	@EventHandler(priority = EventPriority.LOWEST)
	void onChat(final PlayerChatEvent event) {
 		if( event.getPlayer().hasPermission("ample.invoke") ) {
 			message = ChatColor.stripColor(event.getMessage()).toLowerCase();
 			if(message.length() >= 3) {
 				ResultSet result = db.query("SELECT * FROM "+config.getDbPrefix()+"Responses ORDER BY keyphrase DESC;");
 				if(result != null) {
 					TreeMap<Double,TreeMap<Integer,String>> rank = new TreeMap<Double,TreeMap<Integer,String>>();
 					try {
 						while(result.next()) {
 							String response = result.getString("keyphrase").toLowerCase();
 							double reslength = response.length();
 							double msglength = message.length();
 							double rel;
 							if(reslength >= msglength) rel = ((msglength/reslength)*100);
 							else rel = ((reslength/msglength)*100);
 							String[] mary = message.split(" ");
 							double count = 0;
 							for(int i=0;i < mary.length;i++) {
 								if(response.contains(mary[i])) count ++;
 							}
 							double wordrel;
 							if (count <= mary.length) wordrel = ((count/mary.length)*100);
 							else wordrel = ((mary.length/count)*100);
 							double avgrel = ((wordrel+rel)/2);
 							TreeMap<Integer,String> temp = new TreeMap<Integer,String>();
 							temp.put(result.getInt("id"), result.getString("response"));
 							rank.put(avgrel, temp);
 
 						}
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					plugin.loger("test "+rank.lastEntry().getValue()); //error here
 					Entry<Double, TreeMap<Integer, String>> highest = rank.lastEntry();
 
 					TreeMap<Integer, String> value = highest.getValue();
 					if(highest.getKey() > config.getAllowable()) {
 						try {
 							execute(value, event);
 						} catch (SQLException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * @param value
 	 * @param event
 	 * @throws SQLException 
 	 */
	private void execute(TreeMap<Integer, String> value, final PlayerChatEvent event) throws SQLException {
 		final String response = value.firstEntry().getValue();
 		int id = value.firstEntry().getKey();
 		db.query("INSERT INTO "+config.getDbPrefix()+"Usage (player,dtime,question) " +
 				"VALUES ( '"+event.getPlayer().getName()+"', "+db.currentEpoch()+", "+id+");");
 		ResultSet rs = db.query("SELECT COUNT(dtime) FROM "+config.getDbPrefix()+"Usage WHERE question = "+id+" AND dtime < "+db.currentEpoch()+" AND dtime > "+(db.currentEpoch() - config.getAbuseRatio()[1])+";");
 		int v = rs.getInt(1);
 		int c = config.getAbuseRatio()[0];
 		if(v <= c) {
 			String[] newline = response.split(";");
 			for(int a=0;a < newline.length;a++) {
 				final String line = newline[a];
 				String fresponse = formatChat(setDisplay(config.getDisplay(), line, config.getBotName()), event);
 				final String fmsg = fresponse;
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						if(line.length() > 4 && line.toLowerCase().substring(0, 4).equals("cmd:")) {
 							String cmd = db.unescape(line.toLowerCase().substring(4));
 							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),formatChat(cmd.trim(),event));
 						} else if(line.length() > 5 && line.toLowerCase().substring(0, 5).equals("pcmd:")) {
 							String cmd = db.unescape(line.toLowerCase().substring(5));
 							Bukkit.getServer().dispatchCommand(event.getPlayer(),formatChat(cmd.trim(),event));
 						} else if(line.length() > 3 && line.toLowerCase().substring(0, 3).equals("pm:")) {
 							plugin.loger("pm to "+event.getPlayer().getDisplayName()+": "+line.substring(3));
 							event.getPlayer().sendMessage(formatChat(setDisplay(config.getDisplay(),db.unescape(line.substring(3)), config.getBotName()), event));
 						} else if(line.length() > 5 && line.toLowerCase().substring(0, 5).equals("chat:")) {
 							event.getPlayer().chat(formatChat(setDisplay(config.getDisplay(),db.unescape(line.substring(5)), config.getBotName()), event));
 						} else {
 							plugin.getServer().broadcastMessage(db.unescape(fmsg));
 						}
 					} catch (CommandException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}, config.getMsgDelay());
 			}
 		} else {
 			if(config.getAbuseAction().equalsIgnoreCase("kick")) event.getPlayer().kickPlayer(config.getAbuseKick());
 		}
 	}
 
 	public String setDisplay(String display, String message, String botname) {
 		String str = display.replaceAll("%botname", botname);	
 		return str.replaceAll("%message", message);
 	}
	public static String formatChat(String chat, PlayerChatEvent event)
 	{
 		//format chat
 		chat = ChatColor.translateAlternateColorCodes('&',chat);
 
 		//set wildcards
 		chat = chat.replaceAll("%player", event.getPlayer().getName());
 		
 		//numerical wildcards
 		String[] words =ChatColor.stripColor(event.getMessage()).split(" ");
 		for(int i=0;i < words.length; i++) chat = chat.replace("%"+(i+1), words[i]);
 
 		return chat;
 	}
 
 }
