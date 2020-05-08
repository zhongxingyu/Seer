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
 
 import java.sql.SQLException;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.jaggy.bukkit.ample.Ample;
 import org.jaggy.bukkit.ample.config.Config;
 import org.jaggy.bukkit.ample.db.DB;
 
 public class CmdQuestion implements CommandExecutor {
 	
 	private Ample plugin = new Ample();
 	private DB db;
 	private Config config;
 	
 	public CmdQuestion(Ample instance) {
 		plugin = instance;
 		db = plugin.getDB();
 		config = plugin.getDConfig();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 
 		String question = "";
 
 		for(int i = 0; i < args.length; i++) {
 			question += args[i];
 			question += " ";
 		}
 		question = question.trim();
 		if(sender instanceof Player){
 			Player player = (Player) sender;
 			if( player.hasPermission("ample.edit") ) {
 				try {
 					addQuestion(sender, question);
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					db.Error("DB error:" +e);
 				}
 
 			} else plugin.Error(player, "You do not have permissions to use this command.");
 		} else {
 			try {
 				addQuestion(sender, question);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				db.Error("DB error:" +e);
 			}
 		}
 		return true;
 	}
 
 	private void addQuestion(CommandSender sender, String question) throws SQLException {
 		if(question.length() >= 3) {
 
 		if(db.query("INSERT INTO "+config.getDbPrefix()+"Responses (keyphrase,response) VALUES ('"+db.escape_quotes(question.toLowerCase())+"','');") != null) 
 			plugin.Msg(sender, "Db error: Failed to add the question.");
 		else
 			plugin.Msg(sender, "Question ID: "+db.lastID());
		} else plugin.Msg(sender, "Question keyphrase is to short, it has to be 4 characters or greater.");
 	}
 	
 }
