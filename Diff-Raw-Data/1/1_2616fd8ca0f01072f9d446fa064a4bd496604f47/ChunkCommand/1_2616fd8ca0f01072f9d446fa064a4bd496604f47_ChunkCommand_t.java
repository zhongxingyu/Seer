 package com.caindonaghey.commandbin.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.caindonaghey.commandbin.Phrases;
 
 public class ChunkCommand implements CommandExecutor {
 	
 	public boolean onCommand(CommandSender s, Command c, String l, String [] args) {
 		if(l.equalsIgnoreCase("chunk")) {
 			if(!(s instanceof Player)) {
 				System.out.println(Phrases.get("no-console"));
 				return true;
 			}
 			
 			Player player = (Player) s;
 			
 			if(!player.hasPermission("CommandBin.chunk")) {
 				player.sendMessage(Phrases.get("no-permission"));
 				return true;
 			}
 			
 			if(args.length != 0) {
 				player.sendMessage(Phrases.get("invalid-arguments"));
 				return false;
 			}
 			
 			player.getLocation().getChunk().load(true);
			player.getLocation().getChunk().load();
 			player.sendMessage(Phrases.get("chunk-reloaded"));
 		}
 		return true;
 	}
 
 }
