 /*
  * Copyright (c) 2012 Sean Porter <glitchkey@gmail.com>
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge,
  * publish, distribute, sublicense, and/or sell copies of the Software,
  * and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package org.enderspawn;
 
 //* IMPORTS: JDK/JRE
 	import java.lang.Math;
 	import java.lang.String;
 	import java.sql.Timestamp;
 	import java.util.ArrayList;
 	import java.util.Date;
 	import java.util.List;
 //* IMPORTS: BUKKIT
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
 	import org.bukkit.World.Environment;
 //* IMPORTS: SPOUT
 	//* NOT NEEDED
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class EnderSpawnListener implements Listener
 {
 	private EnderSpawn plugin;
 
 	public EnderSpawnListener(EnderSpawn plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	public void register()
 	{
 		PluginManager manager;
 
 		manager = plugin.getServer().getPluginManager();
 		manager.registerEvents(this, plugin);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onChunkUnload(ChunkUnloadEvent event)
 	{
 		World world = event.getWorld();
 		if(world.getEnvironment() != World.Environment.valueOf("THE_END"))
 				return;
 
 		Chunk chunk = event.getChunk();
 		Entity[] entities = chunk.getEntities();
 
 		for(Entity entity : entities)
 		{
 			if(entity == null)
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
 	public void onDragonEggTeleport(BlockFromToEvent event)
 	{
 		if (event.getBlock().getType().getId() != 122)
 			return;
 
 		if(plugin.config.teleportEgg)
 			return;
 
 		event.setCancelled(true);
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEntityExplode(EntityExplodeEvent event)
 	{
 		if (!(event.getEntity() instanceof EnderDragon))
 			return;
 
 		if (plugin.config.destroyBlocks)
 			return;
 
 		event.blockList().clear();
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEntityCreatePortal(EntityCreatePortalEvent event)
 	{
 		Entity entity = event.getEntity();
 
 		if (!(entity instanceof EnderDragon))
 			return;
 
 		List<BlockState> blocks = new ArrayList(event.getBlocks());
 
 		for (BlockState block : event.getBlocks())
 		{
 			if(block.getType().getId() == 122 && !plugin.config.spawnEgg)
 				blocks.remove(block);
 
 			if(plugin.config.spawnPortal)
 				continue;
 
 			if(block.getType().getId() == 7 || block.getType().getId() == 119)
 				blocks.remove(block);
 			else if(block.getType().getId() == 0 || block.getType().getId() == 50)
 				blocks.remove(block);
 			else if(block.getType().getId() == 122 && plugin.config.spawnEgg)
 			{
 				blocks.remove(block);
 
 				Location location = entity.getLocation();
 				ItemStack item = new ItemStack(block.getType());
 
 				entity.getWorld().dropItemNaturally(location, item);
 			}
 		}
 
 		if(blocks.size() != event.getBlocks().size())
 		{
 			event.setCancelled(true);
 
 			LivingEntity newEntity = (LivingEntity) entity;
 			PortalType type = event.getPortalType();
 			EntityCreatePortalEvent newEvent;
 			newEvent = new EntityCreatePortalEvent(newEntity, blocks, type);
 
 			plugin.getServer().getPluginManager().callEvent(newEvent);
 		}
 		return;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEntityDeath(EntityDeathEvent event)
 	{
 		Entity entity = event.getEntity();
 
 		if (!(entity instanceof EnderDragon))
 			return;
 
		int droppedEXP = event.getDroppedExp();		
		event.setDroppedExp(0);

		if(droppedEXP <= 0)
			return;

 		plugin.config.lastDeath = new Timestamp(new Date().getTime());
 		plugin.config.save();
 		plugin.spawner.start();
 
 		if(plugin.config.useCustomExp)
 			droppedEXP = plugin.config.customExp;
 
 		List<Player> players = entity.getWorld().getPlayers();
 
 		Location enderDragonLocation = entity.getLocation();
 
 		double enderX = enderDragonLocation.getX();
 		double enderY = enderDragonLocation.getY();
 		double enderZ = enderDragonLocation.getZ();
 
 		for(Player player : players)
 		{
 			Location playerLocation = player.getLocation();
 
 			double playerX = playerLocation.getX();
 			double playerY = playerLocation.getY();
 			double playerZ = playerLocation.getZ();
 
 			double squareX = Math.pow((enderX - playerX), 2);
 			double squareY = Math.pow((enderY - playerY), 2);
 			double squareZ = Math.pow((enderZ - playerZ), 2);
 
 			double distance = Math.sqrt(squareX + squareY + squareZ);
 
 			if(distance > plugin.config.expMaxDistance)
 				continue;
 
 			String playerName = player.getName().toUpperCase().toLowerCase();
 
 			if(plugin.config.bannedPlayers.containsKey(playerName))
 				continue;
 
 			Timestamp time = plugin.config.players.get(playerName);
 
 			long requiredTime = new Date().getTime();
 			requiredTime -= plugin.config.expResetMinutes * 60000;
 
 			if(time != null && (time.getTime() > requiredTime))
 				continue;
 
 			if(!(plugin.hasPermission(player, "enderspawn.exp", false)))
 				continue;
 
 			player.giveExp(droppedEXP);
 
 			if(plugin.hasPermission(player, "enderspawn.unlimitedexp", false))
 				continue;
 
			Timestamp now = new Timestamp(new Date().getTime());
			plugin.config.players.put(playerName, now);
 		}
 
 		plugin.config.save();
 	}
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
 	{
 		Environment environment = event.getPlayer().getWorld().getEnvironment();
 
 		if(environment != World.Environment.valueOf("THE_END"))
 			return;
 
 		plugin.spawner.start();
 		plugin.showStatus(event.getPlayer(), null);
 	}
 
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		Environment environment = event.getPlayer().getWorld().getEnvironment();
 
 		if(environment != World.Environment.valueOf("THE_END"))
 			return;
 
 		plugin.spawner.start();
 		plugin.showStatus(event.getPlayer(), null);
 	}
 }
