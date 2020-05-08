 package me.makskay.bukkit.tidy;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class PlayerListener implements Listener {
	private IssueManager issueManager;
 	
 	public PlayerListener(TidyPlugin plugin) {
		this.issueManager = plugin.getIssueManager();
 	}
 
 	@EventHandler
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		String message = event.getMessage();
 		Player player = event.getPlayer();
 		int panicLevel = 0;
 		
 		for (String word : message.split(" ")) {
			if (wordsToWatch.contains(word)) {
 				panicLevel++;
 			}
 		}
 		
 		if (message.endsWith("!")) {
 			panicLevel *= 2;
 		}
 		
		if (panicLevel > panicThreshhold) { // TODO Make panicThreshhold configurable via "sensitivity" in config.yml or something
			issueManager.registerIssue(player.getName(), message, player.getLocation());
 		}
 	}
 }
