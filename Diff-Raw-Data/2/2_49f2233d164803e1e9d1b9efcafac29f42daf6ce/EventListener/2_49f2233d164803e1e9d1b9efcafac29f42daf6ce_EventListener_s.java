 package com.ftwinston.Killer;
 
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
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
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
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
 
 import com.ftwinston.Killer.Game.GameState;
 
 class EventListener implements Listener
 {
     public static Killer plugin;
     
     public EventListener(Killer instance)
 	{
 		plugin = instance;
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onWorldInit(final WorldInitEvent event)
     {
     	if ( plugin.stagingWorldIsServerDefault && plugin.stagingWorld == null )
     	{
     		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
     			public void run()
     			{
     				if ( GameMode.gameModes.size() == 0 )
 					{
 						plugin.warnNoGameModes();
 						return;
 					}
     				if ( WorldOption.worldOptions.size() == 0 )
 					{
 						plugin.warnNoWorldOptions();
 						return;
 					}
     				plugin.worldManager.createStagingWorld(Settings.stagingWorldName);
 					plugin.worldManager.deleteWorlds(null, event.getWorld());
 					plugin.craftBukkit.accountForDefaultWorldDeletion(plugin.stagingWorld);
     			}
     		}, 1);
     	}
     }
 
     // when you die a spectator, be made able to fly again when you respawn
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerRespawn(PlayerRespawnEvent event)
     {
     	World world = event.getPlayer().getWorld();
     	if ( world == plugin.stagingWorld )
     	{
     		event.setRespawnLocation(plugin.stagingWorldManager.getStagingWorldSpawnPoint());
     		return;
     	}
     	
     	final Game game = plugin.getGameForWorld(world);
     	if ( game == null )
     		return;
     	
     	final String playerName = event.getPlayer().getName();
     	
 		if ( game.getGameState().usesGameWorlds && game.getWorlds().size() > 0 )
 			event.setRespawnLocation(game.getGameMode().getSpawnLocation(event.getPlayer()));
 		else
 		{
 			event.setRespawnLocation(plugin.stagingWorldManager.getStagingWorldSpawnPoint());
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
     			public void run()
     			{
     				Player player = plugin.getServer().getPlayerExact(playerName);
     				if ( player != null )
     					plugin.playerManager.giveStagingWorldInstructionBook(player);
     			}
     		});
 		}
 	
     	if(PlayerManager.instance.isSpectator(event.getPlayer().getName()))
     	{
     		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
     			public void run()
     			{
     				Player player = plugin.getServer().getPlayerExact(playerName);
     				if ( player != null )
     				{
     					boolean alive = game.getGameMode().isAllowedToRespawn(player);
     					plugin.playerManager.setAlive(player, alive);
     					if ( alive )
     						player.setCompassTarget(plugin.playerManager.getCompassTarget(game, player));
     				}
     			}
     		});
     	}
     }
     
     // spectators moving between worlds
     @EventHandler(priority = EventPriority.LOWEST)
     public void OnPlayerChangedWorld(PlayerChangedWorldEvent event)
     {
     	Game fromGame = plugin.getGameForWorld(event.getFrom());
     	Game toGame = plugin.getGameForWorld(event.getPlayer().getWorld());
     	
     	boolean wasInKiller = fromGame != null;
     	boolean nowInKiller = toGame != null;
     	
 		if ( wasInKiller )
 		{
 			if ( nowInKiller )
 			{
 				Player player = event.getPlayer();
 				if(PlayerManager.instance.isSpectator(player.getName()))
 					PlayerManager.instance.setAlive(player, false);
 				else
 					player.setCompassTarget(plugin.playerManager.getCompassTarget(fromGame, player));
 			}
 			else
 			{
 				playerQuit(fromGame, event.getPlayer(), false);
 				plugin.playerManager.previousLocations.remove(event.getPlayer().getName()); // they left Killer, so forget where they should be put on leaving
 			}
 		}
 		else if ( nowInKiller )
 			plugin.playerManager.playerJoined(event.getPlayer());
 		
 		if ( event.getPlayer().getWorld() == plugin.stagingWorld )
 			plugin.playerManager.putPlayerInStagingWorld(event.getPlayer());
     }
     
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerPortal(PlayerPortalEvent event)
 	{
 		Game game = plugin.getGameForWorld(event.getFrom().getWorld());
 		if ( game == null )
 			return;
 		
 		PortalHelper helper = new PortalHelper(event.getPortalTravelAgent());
 		event.setCancelled(true); // we're going to handle implementing the portalling ourselves
 		
 		game.getGameMode().handlePortal(event.getCause(), event.getFrom(), helper); // see? I told you
 		helper.performTeleport(event.getCause(), event.getPlayer());
 	}
 	
     // prevent spectators picking up anything
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerPickupItem(PlayerPickupItemEvent event)
     {
     	Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
 		if ( game == null )
 			return;
 
     	if(PlayerManager.instance.isSpectator(event.getPlayer().getName()))
     		event.setCancelled(true);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onItemSpawn(ItemSpawnEvent event)
     {
     	if ( event.getLocation().getWorld() == plugin.stagingWorld )
     		event.setCancelled(true);
     }
     
     // prevent spectators breaking anything, prevent anyone breaking protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void onBlockBreak(BlockBreakEvent event)
     {
     	World world = event.getPlayer().getWorld();
     	Game game = plugin.getGameForWorld(world);
     	
     	if ( game == null && world != plugin.stagingWorld ) 
 			return;
 
    	if ( PlayerManager.instance.isSpectator(event.getPlayer().getName()) || plugin.worldManager.isProtectedLocation(game, event.getBlock().getLocation()) )
     		event.setCancelled(true);
     }
     
     // prevent anyone placing blocks on protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void onBlockPlace(BlockPlaceEvent event)
     {
     	World world = event.getPlayer().getWorld();
     	if ( world == plugin.stagingWorld )
     	{
     		event.setCancelled(true);
     		return;
     	}
     	
     	Game game = plugin.getGameForWorld(world);
     	if ( game == null ) 
 			return;
     	
     	if ( plugin.worldManager.isProtectedLocation(game, event.getBlock().getLocation()) || PlayerManager.instance.isSpectator(event.getPlayer().getName()))
     		event.setCancelled(true);
     }
     
     // prevent lava/water from flowing onto protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void BlockFromTo(BlockFromToEvent event)
     {
     	Game game = plugin.getGameForWorld(event.getBlock().getWorld());
     	if ( game == null ) 
 			return;
     	
         if ( plugin.worldManager.isProtectedLocation(game, event.getBlock().getLocation()) )
             event.setCancelled(true);
     }
     
 	// prevent pistons pushing things into/out of protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void onBlockPistonExtend(BlockPistonExtendEvent event)
     {
     	Game game = plugin.getGameForWorld(event.getBlock().getWorld());
     	if ( game == null ) 
 			return;
     	
     	if ( plugin.worldManager.isProtectedLocation(game, event.getBlock().getLocation()) )
     		event.setCancelled(true);
     }
     
 	// prevent explosions from damaging protected locations
     @EventHandler(priority = EventPriority.LOWEST)
     public void onEntityExplode(EntityExplodeEvent event)
     {
     	World world = event.getEntity().getWorld();
     	Game game = plugin.getGameForWorld(world);
     	if ( game == null && world != plugin.stagingWorld ) 
 			return;
     	
     	List<Block> blocks = event.blockList();
     	for ( int i=0; i<blocks.size(); i++ )
     		if ( plugin.worldManager.isProtectedLocation(game, blocks.get(i).getLocation()) )
     		{
     			blocks.remove(i);
     			i--;
     		}
     	
     	if ( world == plugin.stagingWorld )
     	{
     		plugin.arenaManager.monsterKilled();
     		event.setYield(0);
     	}
     }
     
 	// switching between spectator items
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerItemSwitch(PlayerItemHeldEvent event)
     {
     	World world = event.getPlayer().getWorld();
     	Game game = plugin.getGameForWorld(world);
     	if ( game == null ) 
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
     			String target = plugin.playerManager.getNearestFollowTarget(game, event.getPlayer());
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
     	Game game = plugin.getGameForPlayer(event.getPlayer());
     	
     	if ( event.getPlayer().getWorld() == plugin.stagingWorld && event.getClickedBlock() != null )
     		if ( event.getAction() == Action.PHYSICAL && (event.getClickedBlock().getType() == Material.TRIPWIRE || event.getClickedBlock().getType() == Material.STONE_PLATE) )
     		{
     			plugin.stagingWorldManager.playerInteracted(game, event.getClickedBlock().getLocation().getBlockX(),  event.getClickedBlock().getLocation().getBlockY(), event.getClickedBlock().getLocation().getBlockZ(), event.getPlayer());
 				return;
     		}
 	    	else if ( (game == null || game.getGameState().canChangeGameSetup) && event.getClickedBlock().getType() == Material.STONE_BUTTON )
 			{
 				plugin.stagingWorldManager.setupButtonClicked(game, event.getClickedBlock().getLocation().getBlockX(), event.getClickedBlock().getLocation().getBlockZ(), event.getPlayer());
 				return;
 			}
 		
     	if ( game == null ) 
 			return;
     	
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
     				String target = plugin.playerManager.getNextFollowTarget(game, event.getPlayer(), info.target, true);
     				plugin.playerManager.setFollowTarget(event.getPlayer(), target);
     				plugin.playerManager.checkFollowTarget(event.getPlayer(), target);
     				event.getPlayer().sendMessage("Following " + info.target);
         		}
     			else if ( event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK )
         		{
     				String target = plugin.playerManager.getNextFollowTarget(game, event.getPlayer(), info.target, false);
     				plugin.playerManager.setFollowTarget(event.getPlayer(), target);
     				plugin.playerManager.checkFollowTarget(event.getPlayer(), target);
     				event.getPlayer().sendMessage("Following " + info.target);
         		}
     		}
     		
     		return;
     	}
 
     	// prevent spectators from interfering with other players' block placement
     	if ( !event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null )
     	{
     		Block b = event.getClickedBlock().getRelative(event.getBlockFace());
     		double minX = b.getX() - 1, maxX = b.getX() + 2,
     			   minY = b.getY() - 2, maxY = b.getY() + 1,
     			   minZ = b.getZ() - 1, maxZ = b.getZ() + 2;
     		
     		List<Player> spectators = game.getGameMode().getOnlinePlayers(new PlayerFilter().notAlive().world(b.getWorld()).exclude(event.getPlayer()));
     		for ( Player spectator : spectators )
     		{
     			Location loc = spectator.getLocation();
     			if ( loc.getX() >= minX && loc.getX() <= maxX
 	    				&& loc.getY() >= minY && loc.getY() <= maxY
 						&& loc.getZ() >= minZ && loc.getZ() <= maxZ )
     				spectator.teleport(spectator.getLocation().add(0, 3, 0)); // just teleport them upwards, out of the way of this block place
     		}
     	}
     	
 		// eyes of ender can be made to seek out nether fortresses
     	if ( game.isEnderEyeRecipeEnabled() && event.getPlayer().getWorld().getEnvironment() == Environment.NETHER && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.EYE_OF_ENDER && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) )
     	{
 			Location target = plugin.craftBukkit.findNearestNetherFortress(event.getPlayer().getLocation());
 			if ( target == null )
 				event.getPlayer().sendMessage("No nether fortresses nearby");
 			else
 			{
 				plugin.craftBukkit.createFlyingEnderEye(event.getPlayer(), target);
 				event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);				
 			}
     		
     		event.setCancelled(true);
     		return;
     	}
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onItemDrop(PlayerDropItemEvent event)
     {
     	World world = event.getPlayer().getWorld();
     	Game game = plugin.getGameForWorld(world);
     	if ( game == null && world != plugin.stagingWorld ) 
 			return;
     	
     	// spectators can't drop items
     	if ( world == plugin.stagingWorld || plugin.playerManager.isSpectator(event.getPlayer().getName()) )
     		event.setCancelled(true);
     	
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onInventoryClick(InventoryClickEvent event)
     {
     	Player player = (Player)event.getWhoClicked();
     	if ( player == null )
     		return;
 
     	World world = player.getWorld();
     	Game game = plugin.getGameForWorld(world);
     	if ( game == null ) 
 			return;
     	
     	// spectators can't rearrange their inventory ... is that a bit mean?
     	if ( plugin.playerManager.isSpectator(player.getName()) )
     		event.setCancelled(true);
     }
     
 	// spectators can't deal or receive damage
     @EventHandler(priority = EventPriority.LOWEST)
     public void onEntityDamage(EntityDamageEvent event)
     {
     	Game game = plugin.getGameForWorld(event.getEntity().getWorld());
     	if ( game == null ) 
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
     	Game game = plugin.getGameForWorld(event.getPlayer().getWorld());
     	if ( game == null ) 
 			return;
     	
 		Block affected = event.getBlockClicked().getRelative(event.getBlockFace());
 		if ( plugin.worldManager.isProtectedLocation(game, affected.getLocation()) )
 			event.setCancelled(true);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onCraftItem(CraftItemEvent event)
     {
     	Game game = plugin.getGameForWorld(event.getWhoClicked().getWorld());
     	if ( game == null )
     	{// killer recipes can only be crafter in killer worlds, or we could screw up the rest of the server
     		if ( plugin.isDispenserRecipe(event.getRecipe()) || plugin.isEnderEyeRecipe(event.getRecipe()) || plugin.isMonsterEggRecipe(event.getRecipe()) )
        			event.setCancelled(true);
     	}
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onCreatureSpawn(CreatureSpawnEvent event)
     {
     	if ( event.getLocation().getWorld() == plugin.stagingWorld && event.getSpawnReason() == SpawnReason.NATURAL )
     		event.setCancelled(true);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onEntityTarget(EntityTargetEvent event)
     {
     	World world = event.getEntity().getWorld();
     	Game game = plugin.getGameForWorld(world);
     	if ( game == null ) 
 			return;
     	
 		// monsters shouldn't target spectators
     	if( event.getTarget() != null && event.getTarget() instanceof Player && PlayerManager.instance.isSpectator(((Player)event.getTarget()).getName()))
     		event.setCancelled(true);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerChat(AsyncPlayerChatEvent event)
     {
     	World world = event.getPlayer().getWorld();
     	Game game = plugin.getGameForWorld(world);
     	if ( game == null && world != plugin.stagingWorld ) 
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
     	
     	if ( game == null || game.getGameState() == GameState.finished || !PlayerManager.instance.isSpectator(event.getPlayer().getName()))
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
     	World world = event.getPlayer().getWorld();
     	if ( world == plugin.stagingWorld )
     	{
     		final String playerName = event.getPlayer().getName();
     		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
     			public void run()
     			{
     				Player player = plugin.getServer().getPlayerExact(playerName);
     				if ( player != null )
 					{
 						plugin.playerManager.putPlayerInStagingWorld(player);
 						plugin.stagingWorldManager.updateGameInfoSigns(plugin.getGameForPlayer(player));
 					}
     			}
     		});
     	}
     	else
     	{
 			Game game = plugin.getGameForWorld(world);
 			if ( game != null )
 			{
 				plugin.playerManager.playerJoined(event.getPlayer());
 				plugin.stagingWorldManager.updateGameInfoSigns(game);
 			}
     	}
     		
 	}
 	
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerQuit(PlayerQuitEvent event)
     {
     	World world = event.getPlayer().getWorld();
     	if ( world == plugin.stagingWorld )
 		{
     		plugin.arenaManager.playerKilled();
 			plugin.stagingWorldManager.updateGameInfoSigns(plugin.getGameForPlayer(event.getPlayer()));
 		}
     	else
     	{
     		Game game = plugin.getGameForWorld(world);
    		 	if ( game != null )
     			playerQuit(game, event.getPlayer(), true);
     	}
 	}
 	
 	private void playerQuit(Game game, Player player, boolean actuallyLeftServer)
 	{
 		plugin.stagingWorldManager.updateGameInfoSigns(game);
 		if ( actuallyLeftServer ) // the quit message should be sent to the scoreboard of anyone who this player was invisible to
 			for ( Player online : game.getOnlinePlayers() )
 				if ( !online.canSee(player) )
 					plugin.craftBukkit.sendForScoreboard(online, player, false);
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedDeathEffect(game, player.getName(), true), 600);
     }
     
     @EventHandler(priority = EventPriority.LOWEST)
     public void onEntityDeath(EntityDeathEvent event)
     {
     	if ( event.getEntity().getWorld() == plugin.stagingWorld )
 		{
 			event.getDrops().clear();
     		event.setDroppedExp(0);
 
         	if ( event instanceof PlayerDeathEvent )
         	{
         		plugin.arenaManager.playerKilled();
         		((PlayerDeathEvent) event).setDeathMessage(((PlayerDeathEvent) event).getDeathMessage().replace("hit the ground too hard", "fell out of the world"));
         		
         		final Player player = (Player)event.getEntity();
         		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 					@Override
 					public void run() {
 		        		plugin.craftBukkit.forceRespawn(player);
 					}
 				}, 30);
         	}
         	else
         		plugin.arenaManager.monsterKilled(); // entity killed ... if its a monster in arena mode in the staging world
         	
     		return;
     	}
     	
     	if ( !(event instanceof PlayerDeathEvent) )
     		return;
     	
     	Game game = plugin.getGameForWorld(event.getEntity().getWorld());
 	 	if ( game == null )
 	 		return;
     	
     	PlayerDeathEvent pEvent = (PlayerDeathEvent)event;
 		
     	Player player = pEvent.getEntity();
 		if ( player == null )
 			return;
 		
 		if ( game.getGameMode().useDiscreetDeathMessages() )
 			pEvent.setDeathMessage(ChatColor.RED + player.getName() + " died");	
 		
 		// the only reason this is delayed is to avoid banning the player before they properly die, if we're banning players on death
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedDeathEffect(game, player.getName(), false), 10);
 	}
     
     class DelayedDeathEffect implements Runnable
     {
     	Game game;
     	String name;
 		boolean checkDisconnected;
     	public DelayedDeathEffect(Game game, String playerName, boolean disconnect)
 		{
     		this.game = game;
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
 					plugin.statsManager.playerQuit(game.getNumber());
 			}
     		plugin.playerManager.playerKilled(game, player);
     	}
     }
 }
