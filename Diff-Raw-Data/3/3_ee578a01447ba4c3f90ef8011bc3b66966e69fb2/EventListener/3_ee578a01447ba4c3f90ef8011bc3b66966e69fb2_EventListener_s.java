 package com.ftwinston.Killer;
 
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.world.WorldInitEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class EventListener implements Listener
 {
     public static Killer plugin;
     
     public EventListener(Killer instance)
 	{
 		plugin = instance;
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onWorldInit(final WorldInitEvent event)
     {
     	if ( plugin.stagingWorldIsServerDefault && plugin.worldManager.stagingWorld == null )
     	{
     		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
     			public void run()
     			{
     				plugin.worldManager.createStagingWorld(Settings.stagingWorldName);
 					plugin.worldManager.deleteWorlds(null, event.getWorld());
     			}
     		}, 1);
     	}
     }
 
     // when you die a spectator, be made able to fly again when you respawn
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerRespawn(PlayerRespawnEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 		
 		if ( plugin.getGameState().usesGameWorlds && plugin.worldManager.mainWorld != null )
 			event.setRespawnLocation(plugin.getGameMode().getSpawnLocation(event.getPlayer()));
 		else
 			event.setRespawnLocation(plugin.stagingWorldManager.getStagingWorldSpawnPoint());
 	
     	if(PlayerManager.instance.isSpectator(event.getPlayer().getName()))
     	{
     		final String playerName = event.getPlayer().getName();
     		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
     			public void run()
     			{
     				Player player = plugin.getServer().getPlayerExact(playerName);
     				if ( player != null )
     				{
     					boolean alive = plugin.getGameMode().isAllowedToRespawn(player);
     					plugin.playerManager.setAlive(player, alive);
     					if ( alive )
     						player.setCompassTarget(plugin.playerManager.getCompassTarget(player));
     				}
     			}
     		});
     	}
     }
     
     // spectators moving between worlds
     @EventHandler(priority = EventPriority.LOWEST)
     public void OnPlayerChangedWorld(PlayerChangedWorldEvent event)
     {
 		boolean wasInKiller = plugin.isGameWorld(event.getFrom());
 		boolean nowInKiller = plugin.isGameWorld(event.getPlayer().getWorld());
 		
 		if ( wasInKiller )
 		{
 			if ( nowInKiller )
 			{
 				Player player = event.getPlayer();
 				if(PlayerManager.instance.isSpectator(player.getName()))
 					PlayerManager.instance.setAlive(player, false);
 				else
 					player.setCompassTarget(plugin.playerManager.getCompassTarget(player));
 			}
 			else
 			{
 				playerQuit(event.getPlayer(), false);
 				plugin.playerManager.previousLocations.remove(event.getPlayer().getName()); // they left Killer, so forget where they should be put on leaving
 			}
 		}
 		else if ( nowInKiller )
 			playerJoined(event.getPlayer());
 		
 		if ( event.getPlayer().getWorld() == plugin.worldManager.stagingWorld )
 			plugin.playerManager.teleport(event.getPlayer(), plugin.stagingWorldManager.getStagingWorldSpawnPoint());
     }
     
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerPortal(PlayerPortalEvent event)
 	{// we're kinda doing the dirty work in making nether portals work, here
 		World fromWorld = event.getFrom().getWorld();
 		World toWorld;
 		double blockRatio;
 		
 		if ( fromWorld == plugin.worldManager.mainWorld )
 		{
 			toWorld = plugin.worldManager.netherWorld;
 			blockRatio = 0.125;
 		}
 		else if ( fromWorld == plugin.worldManager.netherWorld )
 		{
 			toWorld = plugin.worldManager.mainWorld;
 			blockRatio = 8;
 		}
 		else
 			return;
 		
 		Location playerLoc = event.getPlayer().getLocation();
 		event.setTo(new Location(toWorld, (playerLoc.getX() * blockRatio), playerLoc.getY(), (playerLoc.getZ() * blockRatio), playerLoc.getYaw(), playerLoc.getPitch()));
 	}
 	
     // prevent spectators picking up anything
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerPickupItem(PlayerPickupItemEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 
     	if(PlayerManager.instance.isSpectator(event.getPlayer().getName()))
     		event.setCancelled(true);
     }
     
     // prevent spectators breaking anything, prevent anyone breaking protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void onBlockBreak(BlockBreakEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 
     	if ( PlayerManager.instance.isSpectator(event.getPlayer().getName()) || plugin.worldManager.isProtectedLocation(event.getBlock().getLocation()) )
     		event.setCancelled(true);
     }
     
     // prevent anyone placing blocks on protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void onBlockPlace(BlockPlaceEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 
     	if ( PlayerManager.instance.isSpectator(event.getPlayer().getName())
     		|| plugin.getGameMode().isLocationProtected(event.getBlock().getLocation())
     		|| event.getBlock().getLocation().getWorld() == plugin.worldManager.stagingWorld )
     		event.setCancelled(true);
     }
     
     // prevent lava/water from flowing onto protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void BlockFromTo(BlockFromToEvent event)
     {
 		if ( !plugin.isGameWorld(event.getToBlock().getLocation().getWorld()) )
 			return;
 		
         if ( plugin.getGameMode().isLocationProtected(event.getToBlock().getLocation()) )
             event.setCancelled(true);
     }
     
 	// prevent pistons pushing things into/out of protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void onBlockPistonExtend(BlockPistonExtendEvent event)
     {
 		if ( !plugin.isGameWorld(event.getBlock().getLocation().getWorld()) )
 			return;
 		
     	if ( plugin.getGameMode().isLocationProtected(event.getBlock().getLocation()) )
     		event.setCancelled(true);
     }
     
 	// prevent explosions from damaging protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void onEntityExplode(EntityExplodeEvent event)
     {
 		if ( !plugin.isGameWorld(event.getEntity().getWorld()) )
 			return;
 		
     	List<Block> blocks = event.blockList();
     	for ( int i=0; i<blocks.size(); i++ )
     		if ( plugin.worldManager.isProtectedLocation(blocks.get(i).getLocation()) )
     		{
     			blocks.remove(i);
     			i--;
     		}
     	
     	if ( event.getEntity().getWorld() == plugin.worldManager.stagingWorld )
     	{
     		plugin.stagingWorldManager.stagingWorldMonsterKilled();
     		event.setYield(0);
     	}
     }
     
 	// switching between spectator items
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerItemSwitch(PlayerItemHeldEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 		
     	if ( plugin.playerManager.isSpectator(event.getPlayer().getName()) )
     	{
     		ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
     		
     		if ( item == null )
     			plugin.playerManager.setFollowTarget(event.getPlayer(), null);
     		else if ( item.getType() == Settings.teleportModeItem )
     		{
     			event.getPlayer().sendMessage("Free look mode: left click to teleport " + ChatColor.YELLOW + "to" + ChatColor.RESET + " where you're looking, right click to teleport " + ChatColor.YELLOW + "through" + ChatColor.RESET + " through what you're looking");
     			plugin.playerManager.setFollowTarget(event.getPlayer(), null);
     		}
     		else if ( item.getType() == Settings.followModeItem )
     		{
     			event.getPlayer().sendMessage("Follow mode: click to cycle target");
     			String target = plugin.playerManager.getNearestFollowTarget(event.getPlayer());
     			plugin.playerManager.setFollowTarget(event.getPlayer(), target);
 				plugin.playerManager.checkFollowTarget(event.getPlayer(), target);
     		}
     		else
     			plugin.playerManager.setFollowTarget(event.getPlayer(), null);
     	}
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerInteract(PlayerInteractEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 		
 		if ( plugin.getGameState().canChangeGameSetup && event.getPlayer().getWorld() == plugin.worldManager.stagingWorld
 		  && event.getClickedBlock() != null && (event.getClickedBlock().getType() == Material.STONE_BUTTON || event.getClickedBlock().getType() == Material.STONE_PLATE) )
 		{
 			plugin.stagingWorldManager.setupButtonClicked(event.getClickedBlock().getLocation().getBlockX(), event.getClickedBlock().getLocation().getBlockZ(), event.getPlayer());
 			return;
 		}
 		
     	// spectators can't interact with anything, but they do use clicking to handle their spectator stuff
     	String playerName = event.getPlayer().getName();
     	if ( plugin.playerManager.isSpectator(playerName) )
     	{
     		event.setCancelled(true);
     		Material held = event.getPlayer().getItemInHand().getType();
     		
     		if ( held == Settings.teleportModeItem )
     		{
 				if ( event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK )
     				plugin.playerManager.doSpectatorTeleport(event.getPlayer(), false);
     			else if ( event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK )
     				plugin.playerManager.doSpectatorTeleport(event.getPlayer(), true);
     		}
     		else if ( held == Settings.followModeItem )
     		{
         		PlayerManager.Info info = plugin.playerManager.getInfo(playerName); 
         		
     			if ( event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK )
         		{
     				String target = plugin.playerManager.getNextFollowTarget(event.getPlayer(), info.target, true);
     				plugin.playerManager.setFollowTarget(event.getPlayer(), target);
     				plugin.playerManager.checkFollowTarget(event.getPlayer(), target);
     				event.getPlayer().sendMessage("Following " + info.target);
         		}
     			else if ( event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK )
         		{
     				String target = plugin.playerManager.getNextFollowTarget(event.getPlayer(), info.target, false);
     				plugin.playerManager.setFollowTarget(event.getPlayer(), target);
     				plugin.playerManager.checkFollowTarget(event.getPlayer(), target);
     				event.getPlayer().sendMessage("Following " + info.target);
         		}
     		}
     		
     		return;
     	}
     	
 		// eyes of ender can be made to seek out nether fortresses
     	if ( plugin.isEnderEyeRecipeEnabled() && event.getPlayer().getWorld() == plugin.worldManager.netherWorld && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.EYE_OF_ENDER && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) )
     	{
 			if ( !plugin.worldManager.seekNearestNetherFortress(event.getPlayer()) )
 				event.getPlayer().sendMessage("No nether fortresses nearby");
 			else
 				event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);				
     		
     		event.setCancelled(true);
     		return;
     	}
 
     	if(event.isCancelled())
     		return;
 
 	  	if(event.getClickedBlock().getType() == Material.STONE_PLATE && plugin.getGameMode().isOnPlinth(event.getClickedBlock().getLocation()))
 			plugin.getGameMode().playerActivatedPlinth(event.getPlayer());
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onItemDrop(PlayerDropItemEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 		
     	// spectators can't drop items
     	if ( event.getPlayer().getWorld() == plugin.worldManager.stagingWorld || plugin.playerManager.isSpectator(event.getPlayer().getName()) )
     		event.setCancelled(true);
     	
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onInventoryClick(InventoryClickEvent event)
     {
     	if ( !plugin.isGameWorld(event.getWhoClicked().getWorld()) )
 			return;
     	
     	Player player = (Player)event.getWhoClicked();
     	if ( player == null )
     		return;
     	
     	// spectators can't rearrange their inventory ... is that a bit mean?
     	if ( plugin.playerManager.isSpectator(player.getName()) )
     		event.setCancelled(true);
     }
     
 	// spectators can't deal or receive damage
     @EventHandler(priority = EventPriority.LOWEST)
     public void onEntityDamage(EntityDamageEvent event)
     {
 		if ( !plugin.isGameWorld(event.getEntity().getWorld()) )
 			return;
 		
         if ( event instanceof EntityDamageByEntityEvent )
         {
         	Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
         	if ( damager != null && damager instanceof Player )
         	{
         		if ( PlayerManager.instance.isSpectator(((Player)damager).getName()))
         			event.setCancelled(true);
         	}
         }
         if ( event.isCancelled() || event.getEntity() == null || !(event.getEntity() instanceof Player))
         	return;
         
         Player victim = (Player)event.getEntity();
         
 		if(PlayerManager.instance.isSpectator(victim.getName()))
     		event.setCancelled(true);
 	}
     
 	// can't empty buckets onto protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 		
 		Block affected = event.getBlockClicked().getRelative(event.getBlockFace());
 		if ( plugin.getGameMode().isLocationProtected(affected.getLocation()) )
 			event.setCancelled(true);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onCraftItem(CraftItemEvent event)
     {
     	// killer recipes can only be crafter in killer worlds, or we could screw up the rest of the server
     	if ( !plugin.isGameWorld(event.getWhoClicked().getWorld()) )
     	{
     		if ( 	(plugin.isDispenserRecipeEnabled() && plugin.isDispenserRecipe(event.getRecipe()))
     			 || (plugin.isEnderEyeRecipeEnabled() && plugin.isEnderEyeRecipe(event.getRecipe()))
     			 || (plugin.isMonsterEggRecipeEnabled() && plugin.isMonsterEggRecipe(event.getRecipe()))
     			)
     		{
     			event.setCancelled(true);
     		}
     	}
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onEntityTarget(EntityTargetEvent event)
     {
 		if ( !plugin.isGameWorld(event.getEntity().getWorld()) )
 			return;
 		
 		// monsters shouldn't target spectators
     	if( event.getTarget() != null && event.getTarget() instanceof Player && PlayerManager.instance.isSpectator(((Player)event.getTarget()).getName()))
     		event.setCancelled(true);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 		
     	if ( plugin.voteManager.isInVote() )
     	{
     		if ( event.getMessage().equalsIgnoreCase("Y") && plugin.voteManager.doVote(event.getPlayer(), true) )
     		{
     			event.setMessage(ChatColor.GREEN + "Y");
     			return;
     		}
     		else if ( event.getMessage().equalsIgnoreCase("N") && plugin.voteManager.doVote(event.getPlayer(), false) )
     		{
     			event.setMessage(ChatColor.RED + "N");
     			return;
     		}
     	}
     	
     	// don't mess with spectator chat if they're in the vote setup conversation
     	if ( event.getPlayer().isConversing() )
     		return;
     	
     	if ( plugin.getGameState() == Killer.GameState.finished || !PlayerManager.instance.isSpectator(event.getPlayer().getName()))
 		{// colored player names shouldn't produce colored messages ... spectator chat isn't special when the game is in the "finished" state.
 			event.setMessage(ChatColor.RESET + event.getMessage());
     		return;
 		}
 
     	// mark spectator chat, and hide it from non-spectators
     	event.setMessage(ChatColor.YELLOW + "[Spec] " + ChatColor.RESET + event.getMessage());
     	
     	for (Player recipient : new HashSet<Player>(event.getRecipients()))
     		if ( recipient != null && recipient.isOnline() && !PlayerManager.instance.isSpectator(recipient.getName()))
     			event.getRecipients().remove(recipient);
     }
 	
     @EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerJoin(PlayerJoinEvent event)
     {
     	if ( event.getPlayer().getWorld() == plugin.worldManager.stagingWorld )
     	{
     		final String playerName = event.getPlayer().getName();
     		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
     			public void run()
     			{
     				Player player = plugin.getServer().getPlayerExact(playerName);
     				if ( player != null )
     					if ( plugin.getGameState().usesGameWorlds && plugin.worldManager.mainWorld != null )
     						plugin.playerManager.teleport(player, plugin.getGameMode().getSpawnLocation(player));
     					else
     						plugin.playerManager.teleport(player, plugin.stagingWorldManager.getStagingWorldSpawnPoint());
     			}
     		});
     	}
     	
 		if ( plugin.isGameWorld(event.getPlayer().getWorld()) )
 			playerJoined(event.getPlayer());
 	}
 	
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerQuit(PlayerQuitEvent event)
     {
     	if ( event.getPlayer().getWorld() == plugin.worldManager.stagingWorld )
     		plugin.stagingWorldManager.stagingWorldPlayerKilled();
     	else if ( plugin.isGameWorld(event.getPlayer().getWorld()) )
 			playerQuit(event.getPlayer(), true);
 	}
 	
 	private void playerJoined(Player player)
 	{
 		// if I log into the staging world (cos I logged out there), move me back to the main world's spawn and clear me out
 		if ( player.getWorld() == plugin.worldManager.stagingWorld && plugin.getGameState().usesGameWorlds && plugin.worldManager.mainWorld != null )
 		{
 			player.getInventory().clear();
 			player.setTotalExperience(0);
 			plugin.playerManager.teleport(player, plugin.worldManager.mainWorld.getSpawnLocation());
 		}
 		
     	plugin.playerManager.playerJoined(player);
     }
     
 	private void playerQuit(Player player, boolean actuallyLeftServer)
 	{
 		if ( actuallyLeftServer ) // the quit message should be sent to the scoreboard of anyone who this player was invisible to
 			for ( Player online : plugin.getOnlinePlayers() )
 				if ( !online.canSee(player) )
 					plugin.playerManager.sendForScoreboard(online, player, false);
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedDeathEffect(player.getName(), true), 600);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onEntityDeath(EntityDeathEvent event)
     {
     	if ( !plugin.isGameWorld(event.getEntity().getWorld()) )
     		return;
     	
     	if ( event.getEntity().getWorld() == plugin.worldManager.stagingWorld )
 		{
 			event.getDrops().clear();
     		event.setDroppedExp(0);
 
         	if ( event instanceof PlayerDeathEvent )
         	{
         		plugin.stagingWorldManager.stagingWorldPlayerKilled();
         		
         		final Player player = (Player)event.getEntity();
         		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 					@Override
 					public void run() {
 		        		plugin.playerManager.forceRespawn(player);
 					}
 				}, 30);
         	}
         	else
         		plugin.stagingWorldManager.stagingWorldMonsterKilled(); // entity killed ... if its a monster in arena mode in the staging world
         	
     		return;
     	}
     	
     	PlayerDeathEvent pEvent = (PlayerDeathEvent)event;
 		
     	Player player = pEvent.getEntity();
 		if ( player == null )
 			return;
 		
 		if ( plugin.getGameMode().useDiscreetDeathMessages() )
 			pEvent.setDeathMessage(ChatColor.RED + player.getName() + " died");	
 		
 		// the only reason this is delayed is to avoid banning the player before they properly die, if we're banning players on death
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedDeathEffect(player.getName(), false), 10);
 	}
     
     class DelayedDeathEffect implements Runnable
     {
     	String name;
 		boolean checkDisconnected;
     	public DelayedDeathEffect(String playerName, boolean disconnect)
 		{
 			name = playerName;
 			checkDisconnected = disconnect;
 		}
     	
     	public void run()
     	{
     		OfflinePlayer player = Bukkit.getServer().getPlayerExact(name);
     		if ( player == null )
     			player = Bukkit.getServer().getOfflinePlayer(name);
     		
 			if ( checkDisconnected )
 			{
 				if ( player != null && player.isOnline() )
 					return; // player has reconnected, so don't do anything
 				
 				if ( plugin.playerManager.isAlive(name) )
 					plugin.statsManager.playerQuit();
 			}
     		plugin.playerManager.playerKilled(player);
     	}
     }
 }
