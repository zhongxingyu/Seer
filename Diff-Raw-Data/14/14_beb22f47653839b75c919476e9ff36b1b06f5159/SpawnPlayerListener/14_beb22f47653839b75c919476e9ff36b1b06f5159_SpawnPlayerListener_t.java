 package com.cydoniarp.amd3th.spawnr;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class SpawnPlayerListener extends PlayerListener {
 	public Spawnr plugin;
 	
 	public SpawnPlayerListener(Spawnr plugin) {
 		this.plugin = plugin;
 	}
 	
 	public void onPlayerJoin(PlayerEvent event) {
 		Player player = event.getPlayer();
		
		Spawnr.properties = new Property("plugins/Spawnr/"+player.getWorld().getId()+".spawn");
 		if (!Spawnr.properties.keyExists("x")) {
 			Location oLoc = player.getWorld().getSpawnLocation();
 			player.teleportTo(oLoc);
 			if(player.isOp()) {
 				player.sendMessage("Spawnr point needs to be set.");
 			}
 		}
 		else if (!Spawnr.users.keyExists(player.getName())) {
 			Location loc = player.getLocation();
 			loc.setX(Spawnr.properties.getDouble("x"));
 			loc.setY(Spawnr.properties.getDouble("y"));
 			loc.setZ(Spawnr.properties.getDouble("z"));
 			loc.setYaw(Spawnr.properties.getFloat("yaw"));
 			player.teleportTo(loc);
 			Spawnr.users.setBoolean(player.getName(), true);
 		}
 	}
 	
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
		Spawnr.properties = new Property("plugins/Spawnr/"+player.getWorld().getId()+".spawn");
 		if (Spawnr.properties.keyExists("x")){
 			Location loc = player.getLocation();
 			loc.setX(Spawnr.properties.getDouble("x"));
 			loc.setY(Spawnr.properties.getDouble("y"));
 			loc.setZ(Spawnr.properties.getDouble("z"));
 			loc.setYaw(Spawnr.properties.getFloat("yaw"));
 			event.setRespawnLocation(loc);
 		}
 		else {
 			Location oLoc = player.getWorld().getSpawnLocation();
 			player.teleportTo(oLoc);
 			if(player.isOp()){
 				player.sendMessage("Spawnr point needs to be set.");
 			}
 		}
 	}
 }
