 package no.runsafe.framework.server;
 
 import no.runsafe.framework.hook.HookEngine;
 import no.runsafe.framework.hook.IPlayerLookupService;
 import no.runsafe.framework.output.ChatColour;
 import no.runsafe.framework.server.inventory.RunsafeInventory;
 import no.runsafe.framework.server.inventory.RunsafeInventoryHolder;
 import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.plugin.Plugin;
 
 import java.io.File;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 public class RunsafeServer
 {
 	public static RunsafeServer Instance = null;
 
 	public RunsafeServer(Server toWrap)
 	{
 		this.server = toWrap;
 	}
 
 	public void banIP(String address)
 	{
 		this.server.banIP(address);
 	}
 
 	public int broadcastMessage(String message, String permission)
 	{
 		return this.server.broadcast(ChatColour.ToMinecraft(message), permission);
 	}
 
 	public int broadcastMessage(String message)
 	{
 		return this.server.broadcastMessage(ChatColour.ToMinecraft(message));
 	}
 
 	public void clearRecipes()
 	{
 		this.server.clearRecipes();
 	}
 
 	public boolean getAllowEnd()
 	{
 		return this.server.getAllowEnd();
 	}
 
 	public boolean getAllowFlight()
 	{
 		return this.server.getAllowFlight();
 	}
 
 	public boolean getAllowNether()
 	{
 		return this.server.getAllowNether();
 	}
 
 	public List<RunsafePlayer> getBannedPlayers()
 	{
 		ArrayList<RunsafePlayer> result = new ArrayList<RunsafePlayer>();
 		for (OfflinePlayer banned : this.server.getBannedPlayers())
 			result.add(new RunsafePlayer(banned));
 		return result;
 	}
 
 	public String getBukkitVersion()
 	{
 		return this.server.getBukkitVersion();
 	}
 
 	public Map<String, String[]> getCommandAliases()
 	{
 		return this.server.getCommandAliases();
 	}
 
 	public long getConnectionThrottle()
 	{
 		return this.server.getConnectionThrottle();
 	}
 
 	public boolean getGenerateStructures()
 	{
 		return this.server.getGenerateStructures();
 	}
 
 	public String getIp()
 	{
 		return this.server.getIp();
 	}
 
 	public Set<String> getIpBans()
 	{
 		return this.server.getIPBans();
 	}
 
 	public Logger getLogger()
 	{
 		return this.server.getLogger();
 	}
 
 	public int getMaxPlayers()
 	{
 		return this.server.getMaxPlayers();
 	}
 
 	public String getName()
 	{
 		return this.server.getName();
 	}
 
 	public RunsafePlayer getPlayer(String playerName)
 	{
 		ArrayList<String> hits = new ArrayList<String>();
 		for (IPlayerLookupService lookup : HookEngine.hookContainer.getComponents(IPlayerLookupService.class))
 			for (String hit : lookup.findPlayer(playerName))
 				if (!hits.contains(hit))
 					hits.add(hit);
 
 		if (hits.size() == 0)
 			return new RunsafePlayer(server.getOfflinePlayer(playerName));
 
 		if (hits.size() == 1)
 			return new RunsafePlayer(server.getOfflinePlayer(hits.get(0)));
 
 		return new RunsafeAmbiguousPlayer(server.getOfflinePlayer(hits.get(0)), hits);
 	}
 
 	public List<RunsafePlayer> getOfflinePlayers()
 	{
 		return ObjectWrapper.convert(server.getOfflinePlayers());
 	}
 
 	public RunsafePlayer getOnlinePlayer(RunsafePlayer context, String playerName)
 	{
 		ArrayList<String> hits = new ArrayList<String>();
 		for (RunsafePlayer player : getOnlinePlayers())
 			if (player.getName().toLowerCase().contains(playerName.toLowerCase())
 				&& (context == null || context.canSee(player)))
 				hits.add(player.getName());
 
 		if (hits.size() == 0)
 			return null;
 
 		if (hits.size() == 1)
 			return new RunsafePlayer(server.getPlayerExact(hits.get(0)));
 
 		return new RunsafeAmbiguousPlayer(server.getPlayerExact(hits.get(0)), hits);
 	}
 
 	public boolean getOnlineMode()
 	{
 		return this.server.getOnlineMode();
 	}
 
 	public List<RunsafePlayer> getOnlinePlayers()
 	{
 		return ObjectWrapper.convert((OfflinePlayer[]) server.getOnlinePlayers());
 	}
 
 	public List<RunsafePlayer> getOperators()
 	{
 		return ObjectWrapper.convert(server.getOperators()); // RunsafePlayer.convert(server.getOperators());
 	}
 
 	public RunsafePlayer getPlayerExact(String playerName)
 	{
 		return new RunsafePlayer(this.server.getPlayerExact(playerName));
 	}
 
 	public int getPort()
 	{
 		return this.server.getPort();
 	}
 
 	public String getServerId()
 	{
 		return this.server.getServerId();
 	}
 
 	public String getServerName()
 	{
 		return this.server.getServerName();
 	}
 
 	public int getSpawnRadius()
 	{
 		return this.server.getSpawnRadius();
 	}
 
 	public int getTicksPerAnimalSpawns()
 	{
 		return this.server.getTicksPerAnimalSpawns();
 	}
 
 	public int getTicksPerMonsterSpawns()
 	{
 		return this.server.getTicksPerMonsterSpawns();
 	}
 
 	public String getUpdateFolder()
 	{
 		return this.server.getUpdateFolder();
 	}
 
 	public int getViewDistance()
 	{
 		return this.server.getViewDistance();
 	}
 
 	public List<RunsafePlayer> getWhitelistedPlayers()
 	{
 		return ObjectWrapper.convert(server.getWhitelistedPlayers()); // RunsafePlayer.convert(this.server.getWhitelistedPlayers());
 	}
 
 	public RunsafeWorld getWorld(String worldName)
 	{
 		return ObjectWrapper.convert(this.server.getWorld(worldName));
 	}
 
 	public RunsafeWorld getWorld(UUID uid)
 	{
 		return ObjectWrapper.convert(this.server.getWorld(uid));
 	}
 
 	public File getWorldContainer()
 	{
 		return this.server.getWorldContainer();
 	}
 
 	public List<RunsafeWorld> getWorlds()
 	{
 		List<RunsafeWorld> returnList = new ArrayList<RunsafeWorld>();
 
 		for (World world : this.server.getWorlds())
 		{
 			returnList.add(new RunsafeWorld(world));
 		}
 
 		return returnList;
 	}
 
 	public String getWorldType()
 	{
 		return this.server.getWorldType();
 	}
 
 	public boolean hasWhitelist()
 	{
 		return this.server.hasWhitelist();
 	}
 
 	public List<RunsafePlayer> matchPlayer(String playerName)
 	{
 		List<RunsafePlayer> returnList = new ArrayList<RunsafePlayer>();
 
 		for (Player player : this.server.matchPlayer(playerName))
 		{
 			returnList.add(new RunsafePlayer(player));
 		}
 
 		return returnList;
 	}
 
 	public void reload()
 	{
 		this.server.reload();
 	}
 
 	public void reloadWhitelist()
 	{
 		this.server.reloadWhitelist();
 	}
 
 	public void resetRecipes()
 	{
 		this.server.resetRecipes();
 	}
 
 	public void savePlayers()
 	{
 		this.server.savePlayers();
 	}
 
 	public void setSpawnRadius(int radius)
 	{
 		this.server.setSpawnRadius(radius);
 	}
 
 	public void setWhitelist(boolean value)
 	{
 		this.server.setWhitelist(value);
 	}
 
 	public void shutdown()
 	{
 		this.server.shutdown();
 	}
 
 	public void unbanIp(String address)
 	{
 		this.server.unbanIP(address);
 	}
 
 	public boolean unloadWorld(String worldName, boolean save)
 	{
 		return this.server.unloadWorld(worldName, save);
 	}
 
 	public boolean unloadWorld(RunsafeWorld world, boolean save)
 	{
 		return this.unloadWorld(world.getName(), save);
 	}
 
 	public boolean useExactLoginLocation()
 	{
 		return this.server.useExactLoginLocation();
 	}
 
 	public void banPlayer(RunsafePlayer banner, RunsafePlayer player, String reason)
 	{
 		kickingPlayer.put(player.getName(), banner);
 		player.setBanned(true);
 		player.kick(reason);
 	}
 
 	public void kickPlayer(RunsafePlayer kicker, RunsafePlayer player, String reason)
 	{
 		if (kicker != null)
 			kickingPlayer.put(player.getName(), kicker);
 		player.kick(reason);
 	}
 
 	public RunsafePlayer getKicker(String playerName)
 	{
 		if (kickingPlayer.containsKey(playerName))
 		{
 			RunsafePlayer kicker = kickingPlayer.get(playerName);
 			kickingPlayer.remove(playerName);
 			return kicker;
 		}
 		return null;
 	}
 
 	public boolean someoneHasPermission(String permission)
 	{
 		for (Player player : server.getOnlinePlayers())
 			if (player.hasPermission(permission))
 				return true;
 		return false;
 	}
 
 	public List<RunsafePlayer> getPlayersWithPermission(String permission)
 	{
 		ArrayList<RunsafePlayer> results = new ArrayList<RunsafePlayer>();
 		for (Player player : server.getOnlinePlayers())
 			if (player.hasPermission(permission))
 				results.add(ObjectWrapper.convert(player));
 		return results;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends Plugin> T getPlugin(String pluginName)
 	{
 		Plugin plugin = server.getPluginManager().getPlugin(pluginName);
 		if (plugin == null)
 			return null;
 		return (T) plugin;
 	}
 
 	public RunsafeInventory createInventory(RunsafeInventoryHolder holder, int size, String name)
 	{
		if (holder == null)
			return ObjectWrapper.convert(this.server.createInventory(null, size, name));

 		return ObjectWrapper.convert(this.server.createInventory(holder.getRaw(), size, name));
 	}
 
 	private final ConcurrentHashMap<String, RunsafePlayer> kickingPlayer = new ConcurrentHashMap<String, RunsafePlayer>();
 	private final Server server;
 }
