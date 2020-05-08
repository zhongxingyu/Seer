 package com.github.LeoVerto.Fact;
 
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Fact extends JavaPlugin {
 public void onEnable(){ 
 	getLogger().info("Enabled Fact by Leo Verto");  
 }
      
 public void onDisable(){ 
 	getLogger().info("Disabled Fact"); 
 }
 
 public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){	
     Player player = (Player) sender;
 	if ((sender instanceof Player)) {
 	if(cmd.getName().equalsIgnoreCase("fact")){
 		String message = "";
 		for (int i = 0; i < args.length; i++) {
			message = (message + args[i]);
 		}
 		player.sendMessage("Fact> "+ message);
 		return true;
 	} 
 	return false; 
 	} else {
            sender.sendMessage(ChatColor.RED + "You must be a player!");
            return false;
         }
 }
 
 }
