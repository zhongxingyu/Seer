 package com.gildorym;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
import com.earth2me.essentials.Essentials;

 public class SurnameCommand implements CommandExecutor {
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (args.length >= 1) {
 			String surname = "";
 			for (String arg : args) {
 				surname += arg + " ";
 			}
			Essentials essentials = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
			essentials.getUserMap().getUser(sender.getName()).setNickname(essentials.getUserMap().getUser(sender.getName()).getNickname() + " " + surname);
 			sender.sendMessage(ChatColor.AQUA + "Your surname has been changed to " + surname + ".");
 		}
 		return true;
 	}
 
 }
