 package com.mrz.dyndns.server.warpsuite.managers;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.players.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 public class PlayerManager implements Listener
 {
 	public PlayerManager(WarpSuite plugin)
 	{
 		this.plugin = plugin;
 		
 		String dir = plugin.getDataFolder().getAbsolutePath().toString() + System.getProperty("file.separator") + "players";
 		new File(dir).mkdirs();
 		
 		players = new HashMap<String, WarpSuitePlayer>();
 		
 		for(Player player : plugin.getServer().getOnlinePlayers())
 		{
 			addPlayer(player.getName());
 		}
 	}
 	
 	private final WarpSuite plugin;
 	private final Map<String, WarpSuitePlayer> players;
 	
 	public void clearPlayers()
 	{
 		List<String> wsPlayerNames = new ArrayList<String>(players.keySet());
 		for(String wsPlayerName : wsPlayerNames)
 		{
 			removePlayer(wsPlayerName);
 		}
 		players.clear();
 	}
 	
 	public Collection<WarpSuitePlayer> getPlayers()
 	{
 		return players.values();
 	}
 	
 	public WarpSuitePlayer getWarpPlayer(String player)
 	{
 		if(players.containsKey(player))
 		{
 			return players.get(player);
 		}
 		else
 		{
			if(Bukkit.getPlayer(player) == null)
 			{
 				return null;
 			}
 			else
 			{
 				addPlayer(player);
 				return players.get(player);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		addPlayer(event.getPlayer().getName());
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
 		removePlayer(event.getPlayer().getName());
 	}
 	
 	private void addPlayer(String player)
 	{
 		players.put(player, new WarpSuitePlayer(player, plugin));
 		Util.Debug("Added player " + player);
 	}
 	
 	private void removePlayer(String player)
 	{
 		if(players.containsKey(player))
 		{
 			players.get(player).getConfig().saveCustomConfig();
 			players.get(player).disposePlayerInstance();
 			players.remove(player);
 			Util.Debug("Removed player " + player);
 		}
 	}
 	
 //	//clean up any messes that were somehow made...
 //	@Override
 //	public void run()
 //	{
 //		Util.Debug("Cleaning player list...");
 //		Set<String> scheduledForRemoval = new HashSet<String>();
 //		for(Map.Entry<String, WarpSuitePlayer> entry : players.entrySet())
 //		{
 //			if(entry.getValue().getPlayer().isOnline() == false)
 //			{
 //				scheduledForRemoval.add(entry.getKey());
 //			}
 //		}
 //		for(String toBeRemoved : scheduledForRemoval)
 //		{
 //			Util.Debug("Removing player " + toBeRemoved);
 //			players.remove(toBeRemoved);
 //		}
 //	}
 }
