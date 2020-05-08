 package amrcci;
 
 import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 import com.earth2me.essentials.Essentials;
 import com.earth2me.essentials.User;
 
 public class EssentialsTPA implements Listener {
 
 	Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
 	
 	@EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true)
 	public void onEssTPA(PlayerCommandPreprocessEvent event)
 	{
 		final String[] cmds = event.getMessage().split("\\s+");
 		if (cmds[0].equalsIgnoreCase("/tpaccept"))
 		{
 			try {
 				User essuser = ess.getUser(event.getPlayer());
				Player requester = Bukkit.getPlayerExact(essuser.getTeleportRequest());
				if (requester!= null)
 				{
					PlayerCommandPreprocessEvent fakeevent = new PlayerCommandPreprocessEvent(requester, "/faketpaccept");
					Bukkit.getPluginManager().callEvent(fakeevent);
					if (fakeevent.isCancelled())
 					{
						event.setCancelled(true);
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 		}
 		
 	}
 	
 }
