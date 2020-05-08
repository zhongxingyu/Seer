 package me.docredstone.gc;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 @SuppressWarnings("deprecation")
 public class PlayerChatListener implements Listener {
 
 	@EventHandler
 	public void playerChat(PlayerChatEvent event) {
 		asdjkeInjectGUI injectGUI = new asdjkeInjectGUI();
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			if (!injectGUI.visibleChat.contains(p)) {
 				event.getRecipients().remove(p);
 			}
 			String playerMessage = event.getMessage();
 			Player chatPlayer = event.getPlayer();
 			injectGUI.InjectGui(playerMessage, chatPlayer, playerMessage);
 		}
 	}
 	
 }
