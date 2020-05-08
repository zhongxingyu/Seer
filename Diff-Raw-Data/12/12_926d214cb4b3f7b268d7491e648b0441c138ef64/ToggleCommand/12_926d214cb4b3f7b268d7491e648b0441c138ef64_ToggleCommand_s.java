 package musician101.emergencywhitelist.commands;
 
 import musician101.emergencywhitelist.EmergencyWhitelist;
 import musician101.emergencywhitelist.lib.Constants;
 import musician101.emergencywhitelist.util.RunKickMethod;
 
import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 /**
  * Runs the necessary code for the '/ewl toggle' command.
  * 
  * @author Musician101
  */
 public class ToggleCommand 
 {
 	/**
	 * @param plugin Reference to the plugin's main class.
 	 * @param sender The one who sent the command.
 	 * @param enabled Represents the 'enabled' config option.
 	 */
 	public ToggleCommand(EmergencyWhitelist plugin, CommandSender sender, boolean enabled)
 	{
 		if (!sender.hasPermission(Constants.PERMISSION_TOGGLE))
 			sender.sendMessage(Constants.NO_PERMISSION);
 		else
 		{
			if (enabled)
				new RunKickMethod(plugin, enabled);
			else if (!enabled)
				sender.sendMessage(ChatColor.GOLD + "[EmergencyWhitelist] Whitelist disabled.");
			
 			enabled = !enabled;
 			plugin.getConfig().set("enabled", enabled);
 			plugin.saveConfig();
 			plugin.reloadConfig();
 		}
 	}
 }
