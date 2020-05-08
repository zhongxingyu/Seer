 package me.makskay.bukkit.tidy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class PlayerListener implements Listener {
 	private TidyPlugin plugin;
	//private IssueManager issueManager;
 	
 	public PlayerListener(TidyPlugin plugin) {
 		this.plugin = plugin;
		//this.issueManager = plugin.getIssueManager();
 	}
 
	@EventHandler
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
