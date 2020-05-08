 package com.araeosia.ArcherGames.listeners;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.araeosia.ArcherGames.utils.Archer;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class ArcherGamesKitCommandExecutor implements CommandExecutor{
 
 	public ArcherGames plugin;
 		   
 	public ArcherGamesKitCommandExecutor(ArcherGames plugin){
 		this.plugin = plugin;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("listkits")){
 			sender.sendMessage(ChatColor.GREEN + "Here are the avalible kits: ");
 			String kits = "";
 			for(String s : plugin.kits.keySet()){
 				kits += s + ", ";
 			}
 			sender.sendMessage(ChatColor.GREEN + kits);
 		}
		if(plugin.kits.containsKey(args[0])){
			Archer.getByName(sender.getName()).selectKit(args[0]);
			sender.sendMessage(ChatColor.GREEN + "Your kit has been set to " + args[0]);
			return true;
 		}
 		return false;
 	}
	
}
