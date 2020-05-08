 package me.bluejelly.main.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.bluejelly.main.configs.*;
 import me.bluejelly.main.getters.GuildPlayer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class CmdCreate {
 	
 	private static List<Player> list1 = new ArrayList<Player>();
 	private static List<String> list2 = new ArrayList<String>();
 	
 	private static String guildName = null;
 	
 	public static void perform(Player player, String[] args) {
 		
 		if(args.length <= 1) {
 			player.sendMessage("Usage: /[g|guild] create [name]");
 			return;
 		}
 		
 		if(GuildPlayer.isInGuild(player.getName())) {
 			player.sendMessage(ChatColor.RED + "You're already in an guild!");
 			return;
 		}
 		
 		if(!GuildConfig.config.contains(""+args[1])) {
 			GuildConfig.config.set(args[1] + ".owner", player.getName());
 			GuildConfig.config.set(args[1] + ".description", "Default guild description :]");
 			GuildConfig.config.set(args[1] + ".level", 1);
 			GuildConfig.config.set(args[1] + ".open", false);
 			GuildConfig.config.set(args[1] + ".peaceful", false);
 			GuildConfig.config.set(args[1] + ".money", 0.0);
 			GuildConfig.config.set(args[1] + ".invites", list1);
 			GuildConfig.config.set(args[1] + ".relations", list2);
 			GuildConfig.saveConfig();
 		}
 		
 		if(guildName != null) {
 			PlayerConfig.config.set(player.getName()+".isInGuild", true);
 			PlayerConfig.config.set(player.getName()+".guildName", args[1]);
 			PlayerConfig.config.set(player.getName()+".role", "OWNER");
 			PlayerConfig.config.set(player.getName()+".title", "");
 			PlayerConfig.config.set(player.getName()+".chatmode", "PUBLIC");
 			PlayerConfig.config.set(player.getName()+".honor", 0.0);
 			PlayerConfig.config.set(player.getName()+".reputation", 0.0);
 			PlayerConfig.saveConfig();
 		} else {
 			player.sendMessage(ChatColor.RED + "CREATION FAILED. CONTACT ADMIN/OWNER!");
 		}
 		
 		player.sendMessage("Guild \"" + ChatColor.GOLD + args[1] + ChatColor.WHITE + "\" created with leader: " + ChatColor.DARK_RED + player.getName() + ChatColor.WHITE + ".");
 		return;
 		
 	}
 	
 }
