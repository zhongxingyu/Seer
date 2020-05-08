 package amrcci;
 
 import java.lang.reflect.Method;
 
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
				Method getUserMethodPlayer = ess.getClass().getDeclaredMethod("getUser", Player.class);
				getUserMethodPlayer.setAccessible(true);
				Method getUserMethodString = ess.getClass().getDeclaredMethod("getUser", String.class);
				getUserMethodString.setAccessible(true);
				User essuser = (User) getUserMethodPlayer.invoke(ess, event.getPlayer());
				User requester = (User) getUserMethodString.invoke(ess, essuser.getTeleportRequest());
 				if (requester != null && requester.isOnline())
 				{
 					PlayerCommandPreprocessEvent fakeevent = new PlayerCommandPreprocessEvent(requester.getPlayer(), "/faketpaccept");
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
