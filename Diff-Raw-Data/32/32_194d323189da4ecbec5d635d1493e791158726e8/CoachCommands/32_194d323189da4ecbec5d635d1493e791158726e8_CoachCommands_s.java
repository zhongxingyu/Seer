 package org.mctourney.openhouse.commands;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import org.mctourney.autoreferee.AutoRefMatch;
 import org.mctourney.autoreferee.regions.AutoRefRegion;
 import org.mctourney.autoreferee.util.commands.AutoRefCommand;
 import org.mctourney.autoreferee.util.commands.AutoRefPermission;
 import org.mctourney.autoreferee.util.commands.CommandHandler;
 import org.mctourney.openhouse.OpenHouseAdmin;
 import org.mctourney.openhouse.RegionData;
 import org.mctourney.openhouse.util.RegionUtil;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 /**
  * @author Mustek
  * @author authorblues
  */
 public class CoachCommands implements CommandHandler
 {
 	private OpenHouseAdmin plugin;
 
 	public CoachCommands(OpenHouseAdmin plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	@AutoRefCommand(name={"coach", "tp"}, argmin=1, argmax=1,
 		description="Teleport to the named region.")
 	@AutoRefPermission(console=false, nodes={"openhouse.coach"})
 
 	public boolean teleport(CommandSender sender, AutoRefMatch match, String[] args, CommandLine options)
 	{
 		AutoRefRegion regionTo;
 		String regionToName = args[0].toUpperCase();
 
 		if (plugin.regions.containsKey(regionToName))
 			regionTo = plugin.regions.get(regionToName).region;
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "Region " + regionToName + " does not exist.");
 			return true;
 		}
 
 		((Player) sender).teleport(RegionUtil.getRegionCenter(regionTo));
 		return true;
 	}
 
 	@AutoRefCommand(name={"coach", "transfer"}, argmin=2, argmax=2, options="f",
 		description="Teleport all users within a region to another region.")
 	@AutoRefPermission(console=false, nodes={"openhouse.coach"})
 
 	public boolean transfer(CommandSender sender, AutoRefMatch match, String[] args, CommandLine options)
 	{
 		RegionData regionFrom, regionTo;
 
 		String regionFromName = args[0].toUpperCase();
 		String regionToName = args[1].toUpperCase();
 
 		if (plugin.regions.containsKey(regionFromName))
 			regionFrom = plugin.regions.get(regionFromName);
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "Region " + regionFromName + " does not exist.");
 			return true;
 		}
 
 		if (plugin.regions.containsKey(regionToName))
 			regionTo = plugin.regions.get(regionToName);
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "Region " + regionToName + " does not exist.");
 			return true;
 		}
 
 		if (regionFrom.claimant != null && !sender.getName().equals(regionFrom.claimant))
 		{
 			sender.sendMessage(regionFromName + " was claimed by " + regionFrom.claimant);
 			if (options.hasOption('f')) return true;
 		}
 
 		Location locationTo = RegionUtil.getRegionCenter(regionTo.region);
 		for (Player onPlayer : RegionUtil.getPlayersRegion(regionFrom.region))
 		{
 			onPlayer.teleport(locationTo);
 			onPlayer.sendMessage(ChatColor.GREEN + "You have been teleported by " + sender.getName() + ".");
 		}
 
 		sender.sendMessage(ChatColor.GREEN + "Players have been teleported from " +
 				regionFromName + " to " + regionToName + ".");
 
 		return true;
 	}
 
 	@AutoRefCommand(name={"coach", "regions"}, argmax=0,
 		description="List all available regions.")
 	@AutoRefPermission(console=true, nodes={"openhouse.coach"})
 
 	public boolean listRegions(CommandSender sender, AutoRefMatch match, String[] args, CommandLine options)
 	{
 		List<RegionData> regions = Lists.newArrayList(plugin.regions.values());
 		Collections.sort(regions, new Comparator<RegionData>()
 		{
 			@Override
 			public int compare(RegionData a, RegionData b)
 			{
 
 				int asize = RegionUtil.getPlayersRegion(a.region).size();
 				int bsize = RegionUtil.getPlayersRegion(b.region).size();
 				if (asize != bsize) return asize - bsize;
 
 				// alphabetical order by default
 				return a.region.getName().compareToIgnoreCase(b.region.getName());
 			}
 		});
 
 		sender.sendMessage(ChatColor.GRAY + "Available Regions:");
 		for (RegionData region : regions)
 		{
 			// get the count and color based on whether the region is full
 			int count = region.getPlayers().size();
 			ChatColor color = region.isFull() ? ChatColor.DARK_RED : ChatColor.DARK_GREEN;
 
 			sender.sendMessage(String.format("> %s (%s)",
 				region.region.getName(), "" + color + count + ChatColor.RESET));
 		}
 
 		return true;
 	}
 
 	@AutoRefCommand(name={"coach", "region"}, argmin=1, argmax=1,
 		description="Find out the number of players inside a given region.")
 	@AutoRefPermission(console=true, nodes={"openhouse.coach"})
 
 	public boolean describeRegion(CommandSender sender, AutoRefMatch match, String[] args, CommandLine options)
 	{
 		try
 		{
 			String regionName = args[0].toUpperCase();
			AutoRefRegion region = plugin.regions.get(regionName).region;
 			Set<String> names = Sets.newHashSet();
 
			for (Player onPlayer : RegionUtil.getPlayersRegion(region))
 				names.add(onPlayer.getDisplayName());
 
 			sender.sendMessage(String.format(ChatColor.GREEN + "Region %s [%d]: " + ChatColor.RESET + "%s",
 					regionName, names.size(), names.size() > 0 ? StringUtils.join(names, ", ") : "None"));
 		}
 		catch (Exception e)
 		{
 			sender.sendMessage(ChatColor.RED + "That region does not exist.");
 		}
 
 		return true;
 	}
 
 	@AutoRefCommand(name={"coach", "bring"}, argmin=1, argmax=1, options="f",
 		description="Teleport all users within a region to you.")
 	@AutoRefPermission(console=false, nodes={"openhouse.coach"})
 
 	public boolean bringPlayers(CommandSender sender, AutoRefMatch match, String[] args, CommandLine options)
 	{
 		Player player = (Player) sender;
 		RegionData regionFrom;
 
 		String regionName = args[0].toUpperCase();
 		if (plugin.regions.containsKey(regionName))
 			regionFrom = plugin.regions.get(regionName);
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "Region " + regionName + " does not exist.");
 			return true;
 		}
 
 		if (regionFrom.claimant != null && !sender.getName().equals(regionFrom.claimant))
 		{
 			sender.sendMessage("This region was claimed by " + regionFrom.claimant);
 			if (options.hasOption('f')) return true;
 		}
 
 		Location locationTo = player.getLocation();
		for (Player onPlayer : RegionUtil.getPlayersRegion(regionFrom.region))
 		{
 			onPlayer.teleport(locationTo);
 			onPlayer.sendMessage(ChatColor.GREEN + "You have been teleported by " + player.getName() + ".");
 		}
 
 		sender.sendMessage(ChatColor.GREEN + "Players have been teleported from " + regionName + ".");
 
 		regionFrom.claimant = null;
 		return true;
 	}
 
 	@AutoRefCommand(name={"coach", "claim"}, argmin=1, argmax=1, options="f",
 		description="Claim a region.")
 	@AutoRefPermission(console=false, nodes={"openhouse.coach"})
 
 	public boolean claimRegion(CommandSender sender, AutoRefMatch match, String[] args, CommandLine options)
 	{
 		Player player = (Player) sender;
 		RegionData region;
 
 		String regionName = args[0].toUpperCase();
 		if (plugin.regions.containsKey(regionName))
 			region = plugin.regions.get(regionName);
 		else
 		{
 			sender.sendMessage(ChatColor.RED + "Region " + regionName + " does not exist.");
 			return true;
 		}
 
 		if (region.claimant != null && !sender.getName().equals(region.claimant))
 		{
 			sender.sendMessage("This region was claimed by " + region.claimant);
 			if (options.hasOption('f')) return true;
 		}
 
 		region.claimant = sender.getName();
 		player.teleport(RegionUtil.getRegionCenter(region.region));
 		player.sendMessage("You have claimed " + ChatColor.GREEN + regionName);
 
 		return true;
 	}
 }
