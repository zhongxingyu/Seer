 package com.ammar.bukkit.adminonly;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.ammar.bukkit.adminonly.Perms;
 
 public class AdminChatCommand implements CommandExecutor {
 
     private static AdminChat plugin;
 
     public AdminChatCommand(AdminChat plugin) {
         this.plugin = plugin;
     }
     
 	public static void sendMessage(String msg) {	
         for (final Player plr : plugin.getServer().getOnlinePlayers()) {
             if (com.ammar.bukkit.adminonly.Perms.canRecieve(plr) || plr.isOp()) {
                 plr.sendMessage(msg);
             }	
         }
 	}
     
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     	final Player player = (Player) sender;
         if ((sender instanceof Player) && Perms.canSend((Player) sender) || player.isOp()){
        	if (args.length < 1) {
         		player.sendMessage(ChatColor.RED + "Usage: /amsg <message>");
         	}
         	else{
         		String playername = player.getName();
         		final String message = com.ammar.bukkit.adminonly.Methods.combineSplit(0, args, " ");
         		com.ammar.bukkit.adminonly.Methods.MessageBuild(message, playername);
         	}
         }
         return true;
     }
 }
