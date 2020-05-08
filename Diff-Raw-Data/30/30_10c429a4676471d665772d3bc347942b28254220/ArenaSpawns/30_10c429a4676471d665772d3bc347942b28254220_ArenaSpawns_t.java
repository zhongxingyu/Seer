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
			teamID = Integer.toString(arenaPlayer.getTeam().getTeamNumber());
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
 
 	public enum SpawnType {
 		SPECTATOR, PLAYER
 	}
 }
