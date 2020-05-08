 /*
  * Copyright 2013 Michael McKnight. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and contributors and should not be interpreted as representing official policies,
  * either expressed or implied, of anybody else.
  */
 
 package com.forgenz.mobmanager.listeners;
 
 
 import org.bukkit.GameMode;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.forgenz.mobmanager.Config;
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.world.MMChunk;
 import com.forgenz.mobmanager.world.MMLayer;
 import com.forgenz.mobmanager.world.MMWorld;
 
 public class PlayerListener implements Listener
 {
 
 	public PlayerListener()
 	{
 	}
 	
 	/**
 	 * Updates the player counts within each chunk </br>
 	 * @param toChunk The chunk the player is moving into
 	 * @param toY The height of the player inside the to chunk
 	 * @param fromChunk The chunk the player moved from
 	 * @param fromY The height the player was at when they moved
 	 */
 	public void updateChunkPlayerCount(final MMChunk toChunk, final int toY, final MMChunk fromChunk, final int fromY)
 	{		
 		if (fromChunk != null)
 		{
 			fromChunk.playerLeft();
 			
 			for (MMLayer layer : fromChunk.getLayersAt(fromY))
 				layer.playerLeft();
 		}
 		
 		if (toChunk != null)
 		{
 			toChunk.playerEntered();
 			
 			for (MMLayer layer : toChunk.getLayersAt(toY))
 				layer.playerEntered();
 		}
 	}
 	
 	// NOTE: May not use this
 	@SuppressWarnings("unused")
 	private boolean checkSpecialCase(final PlayerMoveEvent event)
 	{
 		return event.getFrom() == null || event.getTo() == null;
 	}
 	
 	// NOTE: May not use this
 	@SuppressWarnings("unused")
 	private void handleSpecialCase(final PlayerMoveEvent event)
 	{
 		if (event.getTo() == null && event.getPlayer() != null)
 		{
 			P.p.getServer().getScheduler().runTaskLater(P.p, new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					MMWorld toWorld = P.worlds.get(event.getPlayer().getWorld().getName());
 					
 					if (toWorld == null)
 						return;
 					
 					MMChunk toChunk = toWorld.getChunk(event.getPlayer().getLocation().getChunk());
 					
 					if (toChunk == null)
 						return;
 					
 					updateChunkPlayerCount(toChunk, event.getPlayer().getLocation().getBlockY(), null, 0);
 				}
 				
 			}, 1L);
 		}
 		
 		if (event.getFrom() == null && event.getPlayer() != null)
 		{
 			MMWorld fromWorld = P.worlds.get(event.getPlayer().getWorld().getName());
 			
 			if (fromWorld == null)
 				return;
 			
 			MMChunk fromChunk = fromWorld.getChunk(event.getPlayer().getLocation().getChunk());
 			
 			if (fromChunk == null)
 				return;
 			
 			updateChunkPlayerCount(null, 0, fromChunk, event.getPlayer().getLocation().getBlockY());
 		}
 	}
 	
 	// Listener methods
 	
 	/**
 	 * Removes a player from chunks/layers if they change to creative mode</br>
 	 * Else it adds them back</br>
 	 * Does not do anything if ignoreCreativePlayers is disabled
 	 */
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onGamemodeChange(PlayerGameModeChangeEvent event)
 	{
 		// If ignoreCreativePlayers is disabled we do nothing
 		if (!Config.ignoreCreativePlayers)
 			return;
 		
 		// Fetch the world the player is in
 		MMWorld world = P.worlds.get(event.getPlayer().getWorld().getName());
 		
 		if (world == null)
 			return;
 		
 		// Fetch the chunk the player is  in
 		MMChunk chunk = world.getChunk(event.getPlayer().getLocation().getChunk());
 		
 		if (chunk == null)
 			return;
 		
 		// If the player is now in creative mod remove them from the chunk/layers they are in
 		if (event.getNewGameMode() == GameMode.CREATIVE)
 		{
 			updateChunkPlayerCount(null, 0, chunk, event.getPlayer().getLocation().getBlockY());
 		}
 		// Otherwise add them into the chunk/layers they are in
 		else
 		{
 			updateChunkPlayerCount(chunk, event.getPlayer().getLocation().getBlockY(), null, 0);
 		}
 		
 				
 	}
 	
 	/**
 	 * Updates player counts when a player joins the server
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(final PlayerJoinEvent event)
 	{
 		// Check if we can ignore creative players and the player is in creative mode
 		if (Config.ignoreCreativePlayers && event.getPlayer().getGameMode() == GameMode.CREATIVE)
 			return;
 		
 		// Fetch the world the player logged into
 		final MMWorld world = P.worlds.get(event.getPlayer().getLocation().getWorld().getName());
 
 		// If the world is inactive do nothing
 		if (world == null)
 			return;
 
 		// Fetch the chunk the player logged into
 		final MMChunk chunk = world.getChunk(event.getPlayer().getLocation().getChunk());
 
 		updateChunkPlayerCount(chunk, event.getPlayer().getLocation().getBlockY(), null, 0);
 	}
 	
 	/**
 	 * Updates player counts when a player quits the server
 	 */
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(final PlayerQuitEvent event)
 	{
 		// Check if we can ignore creative players and the player is in creative mode
 		if (Config.ignoreCreativePlayers && event.getPlayer().getGameMode() == GameMode.CREATIVE)
 			return;
 		
 		// Fetch the world the player quit from
 		final MMWorld world = P.worlds.get(event.getPlayer().getLocation().getWorld().getName());
 
 		// If the world is inactive do nothing
 		if (world == null)
 			return;
 
 		// Fetch the chunk the player quit from
 		final MMChunk chunk = world.getChunk(event.getPlayer().getLocation().getChunk());
 
 		updateChunkPlayerCount(null, 0, chunk, event.getPlayer().getLocation().getBlockY());
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerRespawn(final PlayerRespawnEvent event)
 	{
 		// Check if we can ignore creative players and the player is in creative mode
 		if (Config.ignoreCreativePlayers && event.getPlayer().getGameMode() == GameMode.CREATIVE)
 			return;
 		
 		// Fetch the world the player spawned in
 		final MMWorld world = P.worlds.get(event.getRespawnLocation().getWorld().getName());
 		
 		// If the world is inactive do nothing
 		if (world == null)
 			return;
 		
 		// Fetch the chunk the player spawned in
 		final MMChunk chunk = world.getChunk(event.getRespawnLocation().getChunk());
 		
 		updateChunkPlayerCount(chunk, event.getRespawnLocation().getBlockY(), null, 0);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerDeath(final PlayerDeathEvent event)
 	{
 		// Check if we can ignore creative players and the player is in creative mode
 		if (Config.ignoreCreativePlayers && event.getEntity().getGameMode() == GameMode.CREATIVE)
 			return;
 		
 		// Fetch the world the player died in
 		final MMWorld world = P.worlds.get(event.getEntity().getLocation().getWorld().getName());
 		
 		// If the world is inactive do nothing
 		if (world == null)
 			return;
 		
 		// Fetch the chunk the player died in
 		final MMChunk chunk = world.getChunk(event.getEntity().getLocation().getChunk());
 		
 		updateChunkPlayerCount(null, 0, chunk, event.getEntity().getLocation().getBlockY());
 	}
 
 	/**
 	 * Updates player counts when players move between chunks or move up or down
 	 */
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerMove(final PlayerMoveEvent event)
 	{
 		// Check if we can ignore creative players and the player is in creative mode
 		if (Config.ignoreCreativePlayers && event.getPlayer().getGameMode() == GameMode.CREATIVE)
 			return;
 		
 		// First we make sure there is actually any point in doing anything
 
 		// Check if the player is moving between chunks
 		if (event.getTo().getBlockX() >> 3 != event.getFrom().getBlockX() >> 3 || event.getTo().getBlockZ() >> 3 != event.getFrom().getBlockZ() >> 3)
 		{
 			// Fetch the world the player is moving in
 			final MMWorld world = P.worlds.get(event.getTo().getWorld().getName());
 
 			// If the world is not active ignore the movement
 			if (world == null)
 			{
 				return;
 			}
 
 			// Fetch the chunks the player is moving between
 			final MMChunk fromChunk = world.getChunk(event.getFrom().getChunk());
 			final MMChunk toChunk = world.getChunk(event.getTo().getChunk());
 			
 			// Update chunks player count
 			updateChunkPlayerCount(toChunk, event.getTo().getBlockY(), fromChunk, event.getFrom().getBlockY());
 		}
		// Check if the player is only moving up or down and away from a layer boundary
		else if (event.getTo().getBlockY() != event.getFrom().getBlockY() && Config.layerBoundaries.contains(event.getFrom().getBlockY()))
 		{
 			// Fetch the world the player is moving in
 			final MMWorld world = P.worlds.get(event.getTo().getWorld().getName());
 
 			// If the world is not active ignore the movement
 			if (world == null)
 			{
 				return;
 			}
 
 			// Fetch the chunk the player is moving in
 			final MMChunk chunk = world.getChunk(event.getTo().getChunk());
 
 			updateChunkPlayerCount(chunk, event.getTo().getBlockY(), chunk, event.getFrom().getBlockY());
 		}
 	}
 	
 	/**
 	 * Updates player counts when players teleport between worlds </br>
 	 * If a player teleports within a world it passes the event to <i>onPlayerMove</i>
 	 */
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerTeleport(final PlayerTeleportEvent event)
 	{
 		// Check if we can ignore creative players and the player is in creative mode
 		if (Config.ignoreCreativePlayers && event.getPlayer().getGameMode() == GameMode.CREATIVE)
 			return;
 		
 		// Check if the worlds differ
 		if (!event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName()))
 		{
 			// Fetch each world
 			MMWorld toWorld = P.worlds.get(event.getTo().getWorld().getName());
 			MMWorld fromWorld = P.worlds.get(event.getFrom().getWorld().getName());
 			
 			// Fetch each chunk
 			MMChunk toChunk = toWorld != null ? toWorld.getChunk(event.getTo().getChunk()) : null;
 			MMChunk fromChunk = fromWorld != null ? fromWorld.getChunk(event.getFrom().getChunk()) : null;
 			
 			// Update player counts
 			updateChunkPlayerCount(toChunk, event.getTo().getBlockY(), fromChunk, event.getFrom().getBlockY());
 		}
 		else
 		{
 			onPlayerMove(event);
 		}
 	}
 	
 	/**
 	 * Updates player counts when players use portals</br>
 	 */
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerPortalEvent(final PlayerPortalEvent event)
 	{
 		// Check if we can ignore creative players and the player is in creative mode
 		if (Config.ignoreCreativePlayers && event.getPlayer().getGameMode() == GameMode.CREATIVE)
 			return;
 		
 		final MMWorld fromWorld = P.worlds.get(event.getFrom().getWorld().getName());
 
 		// Fetch each chunk
 		MMChunk fromChunk = fromWorld != null ? fromWorld.getChunk(event.getFrom().getChunk()) : null;
 
 		// Update player counts
 		updateChunkPlayerCount(null, 0, fromChunk, event.getFrom().getBlockY());
 		
 		// Portal events don't give the correct Y value for the to location
 		// Because of this I check the location of the player after the portal event has been processed
 		P.p.getServer().getScheduler().runTaskLater(P.p, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				MMWorld toWorld;
 
 				if (event.getPlayer().getWorld() == event.getFrom().getWorld())
 					toWorld = fromWorld;
 				else
 					toWorld = P.worlds.get(event.getPlayer().getWorld().getName());
 
 				if (toWorld == null)
 					return;
 
 				MMChunk toChunk = toWorld.getChunk(event.getPlayer().getLocation().getChunk());
 
 				if (toChunk == null)
 					return;
 
 				updateChunkPlayerCount(toChunk, event.getPlayer().getLocation().getBlockY(), null, 0);
 			}
 
 		}, 0L);
 	}
 }
