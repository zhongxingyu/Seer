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
 package org.jaggy.bukkit.ample.cmds;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.jaggy.bukkit.ample.Ample;
 import org.jaggy.bukkit.ample.config.Config;
 import org.jaggy.bukkit.ample.db.DB;
 
 public class CmdQList implements CommandExecutor {
 
 	private Ample plugin = new Ample();
 	private DB db;
 	private Config config;
 	
 	public CmdQList(Ample instance) {
 		plugin = instance;
 		db = plugin.getDB();
 		config = plugin.getDConfig();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		if(sender instanceof Player) {
 			Player player = (Player) sender;
 			if( player.hasPermission("ample.list") ) {
 			listQuestions(sender, args);	
 			} else plugin.Error(player, "You do not have permissions to use this command.");
 		} else {
 			listQuestions(sender, args);
 		}
 		return true;
 	}
 
 	private void listQuestions(CommandSender sender, String[] args) {
 		ResultSet result = null;
 		try {
 			Integer.parseInt(args[0]);
 			result = db.query("SELECT * FROM "+config.getDbPrefix()+"Responses WHERE id = '"+args[0]+"';");
 		} catch (Exception e) {
 				if(args.length == 0) {
 					result = db.query("SELECT * FROM "+config.getDbPrefix()+"Responses WHERE keyphrase LIKE '%'  ESCAPE \"|\";");
 				
 				}
 		}
 		if(result != null) {
 			try {
 				int i = 0;
 				while (result.next()) {
 					String id = result.getString("id");
 					String question = result.getString("keyphrase");
 					String response = result.getString("response");
 					ChatColor color;
 					if(i == 1) {
 						color = ChatColor.AQUA;
 						i = 0;
 					} else {
 						color = ChatColor.YELLOW;
 						i = 1;
 					}
 					sender.sendMessage(color+"["+id+"] Q:"+db.unescape(question));
 					sender.sendMessage(color+"    A:"+db.unescape(response));
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 				plugin.Msg(sender, "DB error: "+e);
 			}
 		} else plugin.Msg(sender, "No results found.");
 		
 	}
 
 }
