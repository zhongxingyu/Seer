 
 package net.loadingchunks.plugins.GuardWolf;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Handler for the /gw sample command.
  * @author Cue
  */
 public class GWCommand implements CommandExecutor {
     private final GuardWolf plugin;
 
     public GWCommand (GuardWolf plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player)) {
             return false;
         }
         Player player = (Player) sender;
 
         if (args.length == 0) {
         	player.sendMessage("GuardWolf doesn't recognise this command, type '/gw help' for a list of commands.");
         	return true;
         } else if(args[0].equalsIgnoreCase("help"))
         {
         	GWHelp(sender);
         	return true;
         } else if(args[0].equalsIgnoreCase("maintenance"))
         {
         	return GWMaintenance(sender, args);
         } else if(args[0].equalsIgnoreCase("config"))
         {
         	sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] Configuration:");
         	for ( String key : plugin.gwConfig.keySet())
         	{
         		sender.sendMessage(ChatColor.RED + key + ChatColor.WHITE + " - " + plugin.gwConfig.get(key));
         	}
         } else {
         	sender.sendMessage(ChatColor.AQUA + "DEBUG: GOT ARGS[0] OF '" + args[0] + "'");
         	sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Unrecognised command, type '/gw help' for a list of commands.");
         }
         
         return false;
     }
 
 	public boolean GWMaintenance(CommandSender sender, String[] args)
 	{
 		Player pl = (Player) sender;
 		
 		if(!this.plugin.perm.ToggleMM(pl))
 			return false;
 		
 		if(args[1].equalsIgnoreCase("on"))
 		{
 			this.plugin.maintenanceMode = true;
 			this.plugin.getServer().broadcastMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Maintenance Mode Enabled by " + pl.getDisplayName());
 			sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Maintenance Mode Enabled, kicking all players who shouldn't be here...");
 			for (Player p : this.plugin.getServer().getOnlinePlayers())
 			{
				if(!this.plugin.perm.MMAccess(pl))
 					p.kickPlayer(this.plugin.gwConfig.get("maintenance_message"));
 			}
 			return true;
 		} else if (args[1].equalsIgnoreCase("off"))
 		{
 			this.plugin.maintenanceMode = false;
 			this.plugin.getServer().broadcastMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Maintenance Mode Disabled by " + pl.getDisplayName());
 			return true;
 		} else {
 			sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "Invalid arguments supplied. Stuck? Try /gw help for help!");
 			return true;
 		}
 	}
     
     public void GWHelp(CommandSender sender)
     {
     	Player pl = (Player) sender;
     	if(this.plugin.perm.canBan(pl))
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "/ban (name) [length] [reason]" + ChatColor.WHITE + " - Ban a player for a certain length of time (or permanently)");
     	if(this.plugin.perm.canUnban(pl))
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "/unban (name)" + ChatColor.WHITE + " - Unban a player.");
     	if(this.plugin.perm.canBanList(pl))
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "/banlist [page] [name]" + ChatColor.WHITE + " - List all bans a player has received.");
     	if(this.plugin.perm.ToggleMM(pl))
     		sender.sendMessage(ChatColor.DARK_AQUA + "[GUARDWOLF] " + ChatColor.RED + "/gw maintenance on/off" + ChatColor.WHITE + " - Enables/Disables Maintenance Mode.");
     }
 }
