 package com.censoredsoftware.camcorder;
 
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import com.censoredsoftware.core.improve.ListedCommand;
 import com.google.common.collect.Sets;
 
 public class CamcorderCommand extends ListedCommand
 {
 	@Override
 	public Set<String> getCommands()
 	{
 		return Sets.newHashSet("camcorder");
 	}
 
 	@Override
 	public boolean processCommand(CommandSender sender, Command command, String[] args)
 	{
 		if(!(sender instanceof Player)) return false;
 		Player player = (Player) sender;
 		if(args.length == 2 && args[0].equalsIgnoreCase("start"))
 		{
 			if(CamcorderRecord.recording == null)
 			{
 				CamcorderRecord.recording = player;
 				CamcorderRecord.name = args[1];
 				player.sendMessage(ChatColor.RED + "Recording...");
 				return true;
 			}
 			player.sendMessage(ChatColor.DARK_GREEN + "The camcorder is currently in use, please wait.");
 			return true;
 		}
 		if(args.length == 1 && args[0].equalsIgnoreCase("stop") && CamcorderRecord.recording.equals(player))
 		{
			CamcorderRecord.end(CamcorderRecord.startTime);
 			CamcorderRecord.recording = null;
 			player.sendMessage(ChatColor.RED + "Stopped recording.");
 			return true;
 		}
 		if(args.length == 1 && args[0].equalsIgnoreCase("list") && CamcorderPlugin.plugin.getConfig().get(player.getName()) != null)
 		{
 			ConfigurationSection data = CamcorderPlugin.plugin.getConfig().getConfigurationSection(player.getName());
 			for(Object recording : data.getValues(false).keySet())
 				player.sendMessage(recording.toString());
 			return true;
 		}
 		if(args.length == 2 && args[0].equalsIgnoreCase("play"))
 		{
 			if(CamcorderPlayback.playback == null)
 			{
 				player.teleport(getStartLocation(player, args[1]));
 				CamcorderPlayback.start = getStartLocation(player, args[1]);
 				CamcorderPlayback.name = args[1];
 				CamcorderPlayback.endTime = getEndTime(player, args[1]);
 				CamcorderPlayback.startPlaybackTime = System.currentTimeMillis();
 				CamcorderPlayback.playback = player;
 				player.sendMessage(ChatColor.RED + "Playing... " + getEndTime(player, args[1]));
 				return true;
 			}
 			player.sendMessage(ChatColor.DARK_GREEN + "The playback is currently in use, please wait.");
 			return true;
 		}
 		return false;
 	}
 
 	private static Location getStartLocation(Player player, String name)
 	{
 		String prefix = player.getName() + "." + name + ".start.";
 		double X = CamcorderPlugin.plugin.getConfig().getDouble(prefix + "X");
 		double Y = CamcorderPlugin.plugin.getConfig().getDouble(prefix + "Y");
 		double Z = CamcorderPlugin.plugin.getConfig().getDouble(prefix + "Z");
 		float pitch = CamcorderPlugin.plugin.getConfig().getInt(prefix + "pitch");
 		float yaw = CamcorderPlugin.plugin.getConfig().getInt(prefix + "yaw");
 		return new Location(player.getWorld(), X, Y, Z, yaw, pitch);
 	}
 
 	private static long getEndTime(Player player, String name)
 	{
 		return CamcorderPlugin.plugin.getConfig().getLong(player.getName() + "." + name + ".end");
 	}
 }
