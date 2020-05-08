 package musician101.emergencywhitelist.runnables;
 
 import musician101.emergencywhitelist.EmergencyWhitelist;
 import musician101.emergencywhitelist.lib.Constants;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  * Checks player permissions and kicks those who lack them.
  * 
  * @author Musician101
  */
 public class KickPlayers implements Runnable
 {
 	EmergencyWhitelist plugin;
 	/**
 	 * @param plugin Reference the plugin's main class.
 	 */
 	public KickPlayers(EmergencyWhitelist plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void run()
 	{
 		Player[] players = Bukkit.getOnlinePlayers();
 		for (Player player : players)
 		{
 			if (!player.hasPermission(Constants.PERMISSION_WHITELIST))
				player.kickPlayer("Server whitelist has been enabled.");
 		}
 	}
 	
 }
