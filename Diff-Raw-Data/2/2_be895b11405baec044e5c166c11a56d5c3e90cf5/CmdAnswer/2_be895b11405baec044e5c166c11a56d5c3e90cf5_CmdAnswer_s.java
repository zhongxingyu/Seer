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
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.jaggy.bukkit.ample.Ample;
 import org.jaggy.bukkit.ample.config.Config;
 import org.jaggy.bukkit.ample.db.DB;
 import org.jaggy.bukkit.ample.utils.Misc;
 
 
 public class CmdAnswer implements CommandExecutor {
 	
 	private Ample plugin = new Ample();
 	private DB db;
 	private Config config;
 	
 	public CmdAnswer(Ample instance) {
 		plugin = instance;
 		db = plugin.getDB();
 		config = plugin.getDConfig();
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		String answer = "";
 		
 		for(int i = 1; i < args.length; i++) {
 			answer += args[i];
 			answer += " ";
 		}
 		answer = answer.trim();
 		int qid = 0;
 		if(args.length >= 2) {
 			if(Misc.isInteger(args[0])) {
 			qid = Integer.parseInt(args[0]);
 			}
 		}
 		if(sender instanceof Player) {
 			Player player = (Player) sender;
 			
 			if( player.hasPermission("ample.edit") ) {
 				try {
 					
 					if(qid != 0) {
 					setAnswer(sender, qid, answer);
 					} else {
 						plugin.Msg(sender, "Not a valid answer: /"+label+" <qid> <answer here>");
 					}
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else plugin.Error(player, "You do not have permissions to use this command.");
 		} else {
 			try {
 				if(qid != 0 && args.length >= 2) {
 				setAnswer(sender, qid, answer);
 				} else {
 					plugin.Msg(sender, "Not a valid answer: /"+label+" <qid> <answer here>");
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return true;
 	}
 	/**
 	 * Sets the answer to a question ID.
 	 * 
 	 * @param sender
 	 * @param QID
 	 * @param answer
 	 * @throws SQLException
 	 */
 	public void setAnswer(CommandSender sender, Integer QID, String answer) throws SQLException {
 			ResultSet result = db.query("SELECT * FROM "+config.getDbPrefix()+"Responses WHERE id = '"+QID+"';");
 			if(result != null) {
				db.query("UPDATE "+config.getDbPrefix()+"Responses SET response = \""+answer+"\" WHERE id = '"+QID+"';");
 				plugin.Msg(sender, "Answer was set!");
 			}
 			else {
 				plugin.Msg(sender, "Unable to find the id: "+QID);
 			}
 	}
 }
