 package me.bluejelly.main.commands;
 
import me.bluejelly.main.configs.GuildConfig;
 import me.bluejelly.main.configs.PlayerConfig;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class CmdLeave {
 	
 	public static void perform(Player player) {
 		
		if(!PlayerConfig.config.contains(player.getName())) {
 			player.sendMessage(ChatColor.RED + "You're not part of any guild!");
 			return;
 		}
 		
		int guildID = PlayerConfig.config.getInt(player.getName() + ".guildID");
 		
		if(GuildConfig.config.contains(guildID+"")) {
			if(!GuildConfig.config.get(guildID + ".owner").equals(player.getName())) {
 				PlayerConfig.config.set(player.getName(), null);
				player.sendMessage(ChatColor.GREEN + "You've left " + ChatColor.GOLD + GuildConfig.config.getString(guildID + ".name") + ".");
 				PlayerConfig.saveConfig();
 				return;
 			} else {
				player.sendMessage(ChatColor.RED + "You need to delete or transfer ownership to someone else before leaving " + ChatColor.GOLD + GuildConfig.config.getString(guildID + ".name") + ".");
 				return;
 			}
 		}
 		
 	}
 	
 }
