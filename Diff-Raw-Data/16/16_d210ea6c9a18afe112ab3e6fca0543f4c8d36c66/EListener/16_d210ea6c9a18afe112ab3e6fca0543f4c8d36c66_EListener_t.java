 package net.sradonia.bukkit.antibuild;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
 
 public class EListener implements Listener {
 	private final AntiBuild plugin;
 	private final MessageSender message;
 
 	public EListener(AntiBuild instance, MessageSender message) {
 		this.plugin = instance;
 		this.message = message;
 	}
 
 	@EventHandler
	public void onPaintingPlace(HangingPlaceEvent event) {
 		final Player player = event.getPlayer();
 		if (!plugin.canBuild(player)) {
 			event.setCancelled(true);
 			if (message != null)
 				message.sendMessage(player);
 		}
 	}
 
 	@EventHandler
	public void onPaintingBreak(HangingBreakEvent event) {
		if (event instanceof HangingBreakByEntityEvent) {
			final Entity remover = ((HangingBreakByEntityEvent) event).getRemover();
 			if (remover instanceof Player) {
 				if (!plugin.canBuild((Player) remover)) {
 					event.setCancelled(true);
 					if (message != null)
 						message.sendMessage((Player) remover);
 				}
 			}
 		}
 	}
 }
