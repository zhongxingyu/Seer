 package minny.simplehealth;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class SimpleHealthPlayerListener implements Listener {
 
 	SimpleHealth plugin;
 
 	public SimpleHealthPlayerListener(SimpleHealth plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e) {
 		updateScoreboard();
 	}
 	
 	public void updateScoreboard(){
 		for (Player p : Bukkit.getOnlinePlayers()) {
 			if (!p.hasPermission("simplehealth.hidehealth")) {
 				p.setScoreboard(plugin.board);
 				p.setHealth(p.getHealth());
 			}
 		}
 	}
 }
