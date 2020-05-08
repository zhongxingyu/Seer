 package pgDev.bukkit.DisguiseCraft.listeners;
 
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
 import pgDev.bukkit.DisguiseCraft.injection.PlayerInvalidInteractEvent;
 
 public class DCCustomListener implements Listener {
 	final DisguiseCraft plugin;
 	
 	public DCCustomListener(final DisguiseCraft plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onDisguiseHit(PlayerInvalidInteractEvent event) {
 		if (event.getAction()) {
 			Player attacked = plugin.getPlayerFromDisguiseID(event.getTarget());
 			if (attacked != null) {
 				// Do the attack
 				((CraftPlayer) event.getPlayer()).getHandle().attack(((CraftPlayer) attacked).getHandle());
 			}
 		}
 	}
 }
