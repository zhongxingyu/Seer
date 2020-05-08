 package com.barroncraft.sce;
 
 import net.sacredlabyrinth.phaed.simpleclans.Clan;
 import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class ExtensionsCommand 
 {
 	
 	SimpleClansExtensions plugin;
 	
 	public ExtensionsCommand(SimpleClansExtensions plugin)
 	{
 		this.plugin = plugin;
 	}
 	
 	public void CommandJoin(Player player, String newClanName)
 	{
 		ClanPlayer clanPlayer = plugin.manager.getCreateClanPlayer(player.getName());
 		Clan newClan = plugin.manager.getClan(newClanName);
 		Clan oldClan = clanPlayer.getClan();
 		
 		if (oldClan != null)
 		{
			if (oldClan.getName().equalsIgnoreCase(newClan.getName()))
 				player.sendMessage(ChatColor.RED + "You can't transfer to the same team");
 			else if (PlayerDifference(oldClan, newClan) < plugin.maxDifference)
 				player.sendMessage(ChatColor.RED + "You can't transfer teams unless there is a difference of " + plugin.maxDifference + " between them");
 			else
 			{
 				oldClan.removeMember(player.getName());
 				AddPlayerToClan(clanPlayer, newClan, newClanName);
 				player.sendMessage(ChatColor.BLUE + "You have been transfered to team " + newClanName);
 			}
 		} 
 		else 
 		{
 			if (PlayerDifference(newClan, plugin.maxDifference) >= plugin.maxDifference)
 				player.sendMessage(ChatColor.RED + "That team already has too many players.  Try a different one.");
 			else
 			{
 				AddPlayerToClan(clanPlayer, newClan, newClanName);
 				player.sendMessage(ChatColor.BLUE + "You have joined team " + newClanName);
 			}
 		}
 	}
 	
 	private int PlayerDifference(Clan clan, int max)
 	{
 		int returnValue = 0;
 		int count = GetOnlinePlayerCount(clan);
 		for (String name : plugin.ClanNames)
 		{
 			int clanCount = GetOnlinePlayerCount(plugin.manager.getClan(name));
 			int difference = count - clanCount;
 			if (difference >= max)
 				return difference;
 			else if (difference > returnValue)
 				returnValue = difference;
 		}
 		
 		return returnValue;
 	}
 	
 	private int PlayerDifference(Clan firstClan, Clan secondClan)
 	{
 		return GetOnlinePlayerCount(firstClan) - GetOnlinePlayerCount(secondClan);
 	}
 	
 	private void AddPlayerToClan(ClanPlayer player, Clan clan, String clanName)
 	{
 		if (clan == null)
 		{
 			plugin.manager.createClan(player.toPlayer(), clanName, clanName);
 			clan = plugin.manager.getClan(clanName);
 			Location location = plugin.spawnLocations.get(clanName);
 			if (location != null)
 				clan.setHomeLocation(location);
 		}
 		clan.addPlayerToClan(player);
 		player.toPlayer().teleport(clan.getHomeLocation());
 	}
 	
 	private int GetOnlinePlayerCount(Clan clan)
 	{
 		if (clan == null)
 			return 0;
 		
 		int count = 0;
 		for (ClanPlayer player : clan.getMembers())
 		{
 			Player onlinePlayer = player.toPlayer();
 			if (onlinePlayer != null && onlinePlayer.isOnline())
 				count++;
 		}
 		return count;
 	}
 }
