 package org.melonbrew.fee.commands;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.melonbrew.fee.Fee;
 import org.melonbrew.fee.Phrase;
 
 public class YesCommand implements CommandExecutor {
 	private final Fee plugin;
 	
 	public YesCommand(Fee plugin){
 		this.plugin = plugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (!(sender instanceof CommandSender)){
 			sender.sendMessage(Phrase.YOU_ARE_NOT_A_PLAYER.parseWithPrefix());
 			
 			return true;
 		}
 		
 		Player player = (Player) sender;
 		
 		if (!plugin.containsPlayer(player)){
 			sender.sendMessage(Phrase.NO_PENDING_COMMAND.parseWithPrefix());
 			
 			return true;
 		}
 		
 		String command = plugin.getCommand(player);
 		
 		if (plugin.getKeyMoney(command) == -1){
 			plugin.removeCommand(player);
 			
 			sender.sendMessage(Phrase.NO_PENDING_COMMAND.parseWithPrefix());
 			
 			return true;
 		}
 		
 		plugin.removeCommand(player);
 		
 		player.chat(command);
 		
 		return true;
 	}
 
 }
