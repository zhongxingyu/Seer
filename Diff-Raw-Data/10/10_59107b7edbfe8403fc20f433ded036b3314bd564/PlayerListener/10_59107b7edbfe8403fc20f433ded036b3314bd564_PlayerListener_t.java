 package net.dmulloy2.teamsparkle.listeners;
 
 import net.dmulloy2.teamsparkle.TeamSparkle;
 import net.dmulloy2.teamsparkle.types.PlayerData;
 import net.dmulloy2.teamsparkle.util.TimeUtil;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * @author dmulloy2
  */
 
 public class PlayerListener implements Listener
 {
 	private final TeamSparkle plugin;
 	public PlayerListener(TeamSparkle plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		final Player player = event.getPlayer();
 		PlayerData data = plugin.getPlayerDataCache().getData(player.getName());
 		if (data == null)
 		{
 			plugin.debug("Creating new data file for {0}!", player.getName());
 			data = plugin.getPlayerDataCache().newData(player.getName());
 			data.setTotalSparkles(0);
 			data.setTokens(0);
 		}
 
		if (! player.hasPlayedBefore() && plugin.isSparkled(player))
 		{
 			// Wait 6 seconds to reward
 			new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					if (player != null && player.isOnline())
 					{
 						plugin.handleSparkle(player);
 					}
 				}
 			}.runTaskLater(plugin, TimeUtil.toTicks(6));
 		}
 	}
 }
