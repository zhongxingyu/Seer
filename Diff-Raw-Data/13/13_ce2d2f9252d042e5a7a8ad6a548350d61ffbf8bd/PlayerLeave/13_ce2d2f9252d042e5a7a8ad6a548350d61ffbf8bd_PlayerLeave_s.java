 package pro.homiecraft;
 
import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import pro.homiecraft.Commands.resources.toggleMap;
 
 public class PlayerLeave implements Listener {
 	@EventHandler
 	public void onPlayerLeave(PlayerQuitEvent event){
		Player player = event.getPlayer();
 		try{
			toggleMap.ee.remove(player.getName());
 		}catch(Exception e){
 			
 		}
 	}
 }
