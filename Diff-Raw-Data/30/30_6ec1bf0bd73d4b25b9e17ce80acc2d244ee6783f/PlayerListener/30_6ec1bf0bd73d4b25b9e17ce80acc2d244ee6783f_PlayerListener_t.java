 package me.makskay.bukkit.tidy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
 
 public class PlayerListener implements Listener {
 	private TidyPlugin plugin;
	private IssueManager issueManager;
 	
 	public PlayerListener(TidyPlugin plugin) {
 		this.plugin = plugin;
		this.issueManager = plugin.getIssueManager();
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPermission("tidy.staff")) {
			return;
		}
		
		int count = 0;
		for (IssueReport issue : issueManager.getCachedIssues()) {
			if (issue.isOpen()) {
				count++;
			}
		}
		
		if (count != 0) {
			event.getPlayer().sendMessage(ChatColor.GRAY + "There are currently " + ChatColor.GREEN + count + 
					ChatColor.GRAY + " unresolved issues");
		}
 	}
 
	@EventHandler (ignoreCancelled = true)
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		String message = event.getMessage();
 		Player player = event.getPlayer();
 		int panicLevel = 0;
 		
 		for (String word : message.split(" ")) {
 			if (plugin.configYml.getConfig().getStringList("PanicWords").contains(word)) {
 				panicLevel++;
 			}
 		}
 		
 		if (message.endsWith("!")) {
 			panicLevel *= 2;
 		}
 		
 		if (panicLevel > plugin.configYml.getConfig().getInt("PanicTolerance")) {
 			player.sendMessage(ChatColor.GRAY + "It looks like you might be having a problem.");
 			player.sendMessage(ChatColor.GRAY + "Consider using /helpme to alert server staff.");
 			//issueManager.registerIssue(player.getName(), message, player.getLocation());
 		}
 	}
 }
