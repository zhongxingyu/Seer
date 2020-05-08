 package me.naithantu.ArenaPVP.Arena.ArenaExtras;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import me.naithantu.ArenaPVP.ArenaManager;
 import me.naithantu.ArenaPVP.ArenaPVP;
 import me.naithantu.ArenaPVP.Arena.Arena;
 import me.naithantu.ArenaPVP.Arena.ArenaPlayer;
 import me.naithantu.ArenaPVP.Events.ArenaEvents.EventRespawn;
 import me.naithantu.ArenaPVP.Util.Util;
 
 public class ArenaSpawns {
 	ArenaPVP plugin;
 	ArenaManager arenaManager;
 	Arena arena;
 	ArenaSettings settings;
 	FileConfiguration config;
 
 	HashMap<String, Integer> respawnTimers = new HashMap<String, Integer>();
 
 	BukkitScheduler scheduler = Bukkit.getScheduler();
 
 	public ArenaSpawns(ArenaPVP plugin, ArenaManager arenaManager, Arena arena, ArenaSettings settings, FileConfiguration config) {
 		this.plugin = plugin;
 		this.arenaManager = arenaManager;
 		this.arena = arena;
 		this.settings = settings;
 		this.config = config;
 	}
 
 	public Location getRespawnLocation(Player player, ArenaPlayer arenaPlayer, SpawnType spawnType) {
 		String teamID;
 		if (spawnType == SpawnType.PLAYER) {
			teamID = Integer.toString(arena.getTeams().indexOf(arenaPlayer.getTeam()));
 		} else if (spawnType == SpawnType.SPECTATOR) {
 			teamID = "spectator";
 		} else {
 			return null;
 		}
 
 		List<String> stringLocations = config.getStringList("spawns." + teamID);
 
 		int locationIndex = 0;
 		if (stringLocations.size() > 1) {
 			Random random = new Random();
 			locationIndex = random.nextInt(stringLocations.size());
 		}
 
 		Location spawnLocation = Util.getLocationFromString(stringLocations.get(locationIndex));
 		EventRespawn eventRespawn = new EventRespawn(player, spawnLocation, arenaPlayer, spawnType);
 		arena.getGamemode().onPlayerArenaRespawn(eventRespawn);
 		if (!eventRespawn.isCancelled()) {
 			return eventRespawn.getLocation();
 		}
 		return null;
 	}
 
	/*public void addRespawnTimer(final Player player, final ArenaPlayer arenaPlayer, final SpawnType spawnType) {
		String playerName = arenaPlayer.getPlayerName();
		if (respawnTimers.containsKey(playerName)) {
			scheduler.cancelTask(respawnTimers.get(playerName));
		}

		int taskID = scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				respawnTimers.remove(arenaPlayer);
				arenaPlayer.setPlayerState(ArenaPlayerState.PLAYING);
				Location location;
				if ((location = getRespawnLocation(player, arenaPlayer, spawnType)) != null) {
					Bukkit.getPlayer(arenaPlayer.getPlayerName()).teleport(location);
				}
			}
		}, settings.getRespawnTime() * 20);
		respawnTimers.put(playerName, taskID);
		arenaPlayer.setPlayerState(ArenaPlayerState.RESPAWNING);
	}*/
	
	/*public void cancelRespawns(){
		for(int id: respawnTimers.values()){
			scheduler.cancelTask(id);
		}
	}*/

 	public enum SpawnType {
 		SPECTATOR, PLAYER
 	}
 }
