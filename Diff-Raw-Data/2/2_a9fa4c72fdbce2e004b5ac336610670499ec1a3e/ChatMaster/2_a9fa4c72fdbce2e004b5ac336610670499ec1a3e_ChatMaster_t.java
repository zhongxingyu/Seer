 package net.techmastary.plugins.chatmaster;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChatMaster extends JavaPlugin implements Listener {
 	public static boolean Muted;
 
 	@Override
 	public void onDisable() {
 		System.out.println("Disabled Silenced.");
 	}
 
 	@Override
 	public void onEnable() {
 		System.out.println("Enabled Silenced.");
 		getServer().getPluginManager().registerEvents(this, this);
 		Muted = false;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void OnPlayerChat(AsyncPlayerChatEvent event) {
		if (!event.getPlayer().hasPermission("chat.speak") && (Muted == true)) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.RED + "Global chat is currently disabled.");
 		}
 	}
 
 	@EventHandler
 	public void OnPlayerJoin(PlayerJoinEvent event) {
 		if (Muted == true) {
 			event.getPlayer().sendMessage(ChatColor.GRAY + "Global chat is currently disabled.");
 			if (event.getPlayer().hasPermission("silenced.admin")) {
 				event.getPlayer().sendMessage(ChatColor.GRAY + "You have permission to talk.");
 			}
 
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (label.equalsIgnoreCase("silence")) {
 			if (sender.hasPermission("chat.admin")) {
 				if (Muted == false) {
 					Muted = true;
 					sender.sendMessage(ChatColor.GRAY + "You silenced global chat.");
 					Bukkit.broadcastMessage(ChatColor.GRAY + "" + sender.getName() + " disabled global chat.");
 				} else {
 					Muted = false;
 					sender.sendMessage(ChatColor.GRAY + "You have resumed global chat.");
 					Bukkit.broadcastMessage(ChatColor.GRAY + "" + sender.getName() + " resumed global chat.");
 				}
 			}
 		}
 		if (label.equalsIgnoreCase("chatstatus")) {
 			if (sender.hasPermission("chat.admin") && (Muted == true)) {
 				sender.sendMessage(ChatColor.GRAY + "Global chat is currently" + ChatColor.RED + " DISABLED" + ChatColor.GRAY + ".");
 			}
 			if (sender.hasPermission("chat.admin") && (Muted == false)) {
 				sender.sendMessage(ChatColor.GRAY + "Global chat is currently" + ChatColor.GREEN + " ENABLED" + ChatColor.GRAY + ".");
 
 			}
 		}
 		if (label.equalsIgnoreCase("deafen")) {
 			if (sender.hasPermission("silenced.admin")) {
 				for (int x = 0; x < 120; x++) {
 					sender.sendMessage("");
 					if (x == 119) {
 						sender.sendMessage(ChatColor.GRAY + "You are now deafened and cannot use chat.");
 						//TODO: Make player unable to send/recieve chat
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 }
