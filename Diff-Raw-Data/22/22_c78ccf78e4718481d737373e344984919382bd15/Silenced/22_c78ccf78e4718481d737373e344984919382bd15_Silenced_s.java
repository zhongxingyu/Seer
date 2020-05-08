 package me.techmastary.plugins.silenced;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
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
					event.getPlayer().sendMessage(ChatColor.GRAY + "The chat is currently disabled.");
				}
			}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (commandLabel.equalsIgnoreCase("silence")) {
 			if (sender.hasPermission("silenced.admin")) {
 				Silence = true;
 				sender.sendMessage("You silenced chat.");
 				Bukkit.broadcastMessage(ChatColor.GRAY + "" + sender + "disabled global chat.");
 			} else {
 				sender.sendMessage(ChatColor.WHITE + "Not cool enough to execute this command.");
 			}
 		}
 		if (commandLabel.equalsIgnoreCase("resume")) {
 			if (sender.hasPermission("silenced.admin")) {
 				Silence = false;
 			} else {
 				sender.sendMessage(ChatColor.WHITE + "Not cool enough to execute this command.");
 			}
 		}
 		return true;
 	}
 }
