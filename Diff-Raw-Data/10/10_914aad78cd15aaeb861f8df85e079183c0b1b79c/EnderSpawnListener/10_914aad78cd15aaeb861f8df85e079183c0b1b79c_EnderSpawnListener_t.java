 /*
  * Copyright (c) 2012 Sean Porter <glitchkey@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.enderspawn;
 
 //* IMPORTS: JDK/JRE
 	import java.sql.Timestamp;
 	import java.util.ArrayList;
 	import java.util.Date;
 	import java.util.List;
 //* IMPORTS: BUKKIT
	import org.bukkit.block.Block;
 	import org.bukkit.block.BlockState;
 	import org.bukkit.Chunk;
 	import org.bukkit.entity.EnderDragon;
 	import org.bukkit.entity.Entity;
 	import org.bukkit.entity.LivingEntity;
 	import org.bukkit.entity.Player;
 	import org.bukkit.event.block.BlockFromToEvent;
 	import org.bukkit.event.entity.EntityCreatePortalEvent;
 	import org.bukkit.event.entity.EntityDeathEvent;
 	import org.bukkit.event.entity.EntityExplodeEvent;
 	import org.bukkit.event.player.PlayerChangedWorldEvent;
 	import org.bukkit.event.player.PlayerJoinEvent;
 	import org.bukkit.event.EventHandler;
 	import org.bukkit.event.EventPriority;
 	import org.bukkit.event.Listener;
 	import org.bukkit.event.world.ChunkUnloadEvent;
 	import org.bukkit.inventory.ItemStack;
 	import org.bukkit.Location;
 	import org.bukkit.plugin.PluginManager;
 	import org.bukkit.PortalType;
 	import org.bukkit.World;
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class EnderSpawnListener implements Listener {
 	private EnderSpawn plugin;
 
 	public EnderSpawnListener(EnderSpawn plugin) {
 		this.plugin = plugin;
 	}
 
 	public void register() {
 		PluginManager manager;
 
 		manager = plugin.getServer().getPluginManager();
 		manager.registerEvents(this, plugin);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onChunkUnload(ChunkUnloadEvent event) {
 		World world = event.getWorld();
 		String worldName = world.getName().toUpperCase().toLowerCase();
 
 		if (!plugin.config.worlds.containsKey(worldName))
 			return;
 
 		Chunk chunk = event.getChunk();
 		Entity[] entities = chunk.getEntities();
 
 		for (Entity entity : entities) {
 			if (entity == null)
 				continue;
 
 			if (!(entity instanceof EnderDragon))
 				continue;
 
 			EntityDeathEvent newEvent;
 			newEvent = new EntityDeathEvent((LivingEntity) entity, new ArrayList());
 			plugin.getServer().getPluginManager().callEvent(newEvent);
 			entity.remove();
 		}
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onDragonEggTeleport(BlockFromToEvent event) {
 		if (event.getBlock().getType().getId() != 122)
 			return;
 
 		if (plugin.config.teleportEgg)
 			return;
 
 		event.setCancelled(true);
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEntityExplode(EntityExplodeEvent event) {
 		if (!(event.getEntity() instanceof EnderDragon))
 			return;
 
 		if (plugin.config.destroyBlocks)
 			return;
 
 		event.blockList().clear();
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEntityCreatePortal(EntityCreatePortalEvent event) {
 		Entity entity = event.getEntity();
 
 		if (!(entity instanceof EnderDragon))
 			return;
 
 		List<BlockState> blocks = new ArrayList(event.getBlocks());
 
 		for (BlockState block : event.getBlocks()) {
 			if (block.getType().getId() == 122 && !plugin.config.spawnEgg)
 				blocks.remove(block);
 
 			if (plugin.config.spawnPortal)
 				continue;
 
 			if (block.getType().getId() == 7 || block.getType().getId() == 119)
 				blocks.remove(block);
 			else if (block.getType().getId() == 0 || block.getType().getId() == 50)
 				blocks.remove(block);
 			else if (block.getType().getId() == 122 && plugin.config.spawnEgg) {
 				blocks.remove(block);
 
 				Location location = entity.getLocation();
 				ItemStack item = new ItemStack(block.getType());
 
 				entity.getWorld().dropItemNaturally(location, item);
 			}
 		}
 
 		if (blocks.size() != event.getBlocks().size()) {
 			event.setCancelled(true);
 
 			LivingEntity newEntity = (LivingEntity) entity;
 			PortalType type = event.getPortalType();
 			EntityCreatePortalEvent newEvent;
 			newEvent = new EntityCreatePortalEvent(newEntity, blocks, type);
 
 			plugin.getServer().getPluginManager().callEvent(newEvent);

			if (!newEvent.isCancelled()) {
				for(BlockState blockState : blocks) {
					int id		= blockState.getTypeId();
					byte data	= blockState.getRawData();
					blockState.getBlock().setTypeIdAndData(id, data, false);
				}
			}
 		}
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEntityDeath(EntityDeathEvent event) {
 		Entity entity = event.getEntity();
 
 		if (!(entity instanceof EnderDragon))
 			return;
 
 		int droppedEXP = event.getDroppedExp();		
 		event.setDroppedExp(0);
 
 		if (droppedEXP <= 0)
 			return;
 
 		String worldName = entity.getWorld().getName().toUpperCase().toLowerCase();
 		plugin.data.lastDeath.put(worldName, new Timestamp(new Date().getTime()));
 		plugin.saveData();
 		plugin.spawner.start();
 
 		if (plugin.config.useCustomExp)
 			droppedEXP = plugin.config.customExp;
 
 		List<Player> players = entity.getWorld().getPlayers();
 
 		Location enderDragonLocation = entity.getLocation();
 
 		double enderX = enderDragonLocation.getX();
 		double enderY = enderDragonLocation.getY();
 		double enderZ = enderDragonLocation.getZ();
 
 		for (Player player : players) {
 			Location playerLocation = player.getLocation();
 
 			double playerX = playerLocation.getX();
 			double playerY = playerLocation.getY();
 			double playerZ = playerLocation.getZ();
 
 			double squareX = Math.pow((enderX - playerX), 2);
 			double squareY = Math.pow((enderY - playerY), 2);
 			double squareZ = Math.pow((enderZ - playerZ), 2);
 
 			double distance = Math.sqrt(squareX + squareY + squareZ);
 
 			if (distance > plugin.config.expMaxDistance)
 				continue;
 
 			String playerName = player.getName().toUpperCase().toLowerCase();
 
 			if (plugin.data.bannedPlayers.containsKey(playerName))
 				continue;
 
 			Timestamp time = plugin.data.players.get(playerName);
 
 			long requiredTime = new Date().getTime();
 			requiredTime -= plugin.config.expResetMinutes * 60000;
 
 			if (time != null && (time.getTime() > requiredTime))
 				continue;
 
 			if (!(plugin.hasPermission(player, "enderspawn.exp", false)))
 				continue;
 
 			player.giveExp(droppedEXP);
 
 			if (plugin.hasPermission(player, "enderspawn.unlimitedexp", false))
 				continue;
 
 			Timestamp now = new Timestamp(new Date().getTime());
 			plugin.data.players.put(playerName, now);
 		}
 
 		plugin.saveData();
 	}
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
 		World world = event.getPlayer().getWorld();
 		String worldName = world.getName().toUpperCase().toLowerCase();
 
 		if (!plugin.config.worlds.containsKey(worldName))
 			return;
 
 		plugin.spawner.start();
 		plugin.showStatus(event.getPlayer(), null);
 	}
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		World world = event.getPlayer().getWorld();
 		String worldName = world.getName().toUpperCase().toLowerCase();
 
 		if (!plugin.config.worlds.containsKey(worldName))
 			return;
 
 		plugin.spawner.start();
 		plugin.showStatus(event.getPlayer(), null);
 	}
 }
