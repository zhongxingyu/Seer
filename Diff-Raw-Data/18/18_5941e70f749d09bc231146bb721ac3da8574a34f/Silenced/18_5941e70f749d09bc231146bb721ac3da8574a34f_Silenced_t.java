 package me.techmastary.plugins.silenced;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Silenced extends JavaPlugin implements Listener {
 	public static boolean Silence;
 
 	@Override
 	public void onDisable() {
 		System.out.println("Disabled Silenced.");
 		Silence = false;
 	}
 
 	@Override
 	public void onEnable() {
 		System.out.println("Enabled Silenced.");
 	}
 
 	@EventHandler
 	public void OnPlayerChat(AsyncPlayerChatEvent event) {
 		if (!event.getPlayer().hasPermission("silenced.admin") && (Silence == true)) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.GRAY + "Global chat is currently disabled.");
		} else {
			event.setCancelled(false);
 		}
 	}
 	
 	public void OnPlayerJoin(PlayerJoinEvent event) {
 		if (!event.getPlayer().hasPermission("silenced.admin") && (Silence == true)) {
 			event.getPlayer().sendMessage(ChatColor.GRAY + "Global chat is currently disabled.");
 		}
 	}
 
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("silence")) {
 			if (sender.hasPermission("silenced.admin")) {
 				Silence = true;
				sender.sendMessage(ChatColor.GRAY + "You silenced global chat.");
				Bukkit.broadcastMessage(ChatColor.GRAY + "" + sender.getName() + "disabled global chat.");
 			} else {
 				sender.sendMessage(ChatColor.WHITE + "Not cool enough to execute this command.");
 			}
 		}
		if (label.equalsIgnoreCase("resume")) {
 			if (sender.hasPermission("silenced.admin")) {
 				Silence = false;
				sender.sendMessage(ChatColor.GRAY + "You have resumed global chat.");
				Bukkit.broadcastMessage(ChatColor.GRAY + "" + sender.getName() + "resumed global chat.");
 			} else {
 				sender.sendMessage(ChatColor.WHITE + "Not cool enough to execute this command.");
 			}
 		}
 		return true;
 	}
 }
