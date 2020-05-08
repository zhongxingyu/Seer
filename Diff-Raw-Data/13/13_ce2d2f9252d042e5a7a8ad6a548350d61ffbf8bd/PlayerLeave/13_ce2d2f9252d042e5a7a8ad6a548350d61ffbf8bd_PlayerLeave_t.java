 package pro.homiecraft;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import pro.homiecraft.Commands.resources.toggleMap;
 
 public class PlayerLeave implements Listener {
 	@EventHandler
 	public void onPlayerLeave(PlayerQuitEvent event){
 		try{
			toggleMap.ee.remove(event.getPlayer().getName());
 		}catch(Exception e){
 			
 		}
 	}
 }
