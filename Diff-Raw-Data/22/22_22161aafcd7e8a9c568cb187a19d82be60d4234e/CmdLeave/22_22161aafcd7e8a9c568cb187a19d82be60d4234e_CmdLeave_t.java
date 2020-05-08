 package me.bluejelly.main.commands;
 
 import me.bluejelly.main.configs.PlayerConfig;
import me.bluejelly.main.getters.Guild;
import me.bluejelly.main.getters.GuildPlayer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class CmdLeave {
 	
 	public static void perform(Player player) {
 		
		if(!GuildPlayer.isInGuild(player.getName())) {
 			player.sendMessage(ChatColor.RED + "You're not part of any guild!");
 			return;
 		}
 		
		String guildName = GuildPlayer.getGuildName(player.getName());
		String guildOwner = Guild.getOwner(guildName);
 		
		if(Guild.exists(guildName+"")) {
			if(!guildName.equals(guildOwner)) {
 				PlayerConfig.config.set(player.getName(), null);
				player.sendMessage(ChatColor.GREEN + "You've left " + ChatColor.GOLD + guildName + ".");
 				PlayerConfig.saveConfig();
 				return;
 			} else {
				player.sendMessage(ChatColor.RED + "You need to delete or transfer ownership to someone else before leaving " + ChatColor.GOLD + guildName + ".");
 				return;
 			}
 		}
 		
 	}
 	
 }
