 package com.ftwinston.Killer;
 
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
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
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
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
     
     @EventHandler
     public void onWorldInit(WorldInitEvent event)
     {
     	if ( plugin.stagingWorldIsServerDefault && event.getWorld().getName().equalsIgnoreCase(plugin.stagingWorldName) )
     		plugin.worldManager.stagingWorldCreated(event.getWorld());
     }
     
     // when you die a spectator, be made able to fly again when you respawn
     @EventHandler
     public void onPlayerRespawn(PlayerRespawnEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 	
     	if(PlayerManager.instance.isSpectator(event.getPlayer().getName()))
     	{
     		final String playerName = event.getPlayer().getName();
     		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
     			public void run()
     			{
     				Player player = plugin.getServer().getPlayerExact(playerName);
     				if ( player != null )
     				{
     					plugin.playerManager.setAlive(player, plugin.playerManager.numKillersAssigned() == 0);
     					plugin.playerManager.checkPlayerCompassTarget(player);
     				}
     			}
     		});
     	}
     }
     
     // spectators moving between worlds
     @EventHandler
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
 					plugin.playerManager.checkPlayerCompassTarget(player);
 			}
 			else
 				playerQuit(event.getPlayer());
 		}
 		else if ( nowInKiller )
 			playerJoined(event.getPlayer());
     }
     
     // prevent spectators picking up anything
     @EventHandler
     public void onPlayerPickupItem(PlayerPickupItemEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 
     	if(PlayerManager.instance.isSpectator(event.getPlayer().getName()))
     		event.setCancelled(true);
     	else
     		plugin.getGameMode().playerPickedUpItem(event);
     }
     
     // prevent spectators breaking anything, prevent anyone breaking the plinth
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 
     	if ( PlayerManager.instance.isSpectator(event.getPlayer().getName())
     	  || event.getPlayer().getWorld() == plugin.worldManager.stagingWorld
     	  || isOnPlinth(event.getBlock().getLocation())
     	  )
     		event.setCancelled(true);
     }
     
     // prevent anyone placing blocks over the plinth
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 
     	if ( PlayerManager.instance.isSpectator(event.getPlayer().getName())
     	  || event.getPlayer().getWorld() == plugin.worldManager.stagingWorld
     	  || isOnPlinth(event.getBlock().getLocation())
     	  )
     		event.setCancelled(true);
     }
     
     // prevent lava/water from flowing onto the plinth
     @EventHandler
     public void BlockFromTo(BlockFromToEvent event)
     {
 		if ( !plugin.isGameWorld(event.getToBlock().getLocation().getWorld()) )
 			return;
 		
         if ( isOnPlinth(event.getToBlock().getLocation()) )
             event.setCancelled(true);
     }
     
     @EventHandler
     public void onBlockPistonExtend(BlockPistonExtendEvent event)
     {
 		if ( !plugin.isGameWorld(event.getBlock().getLocation().getWorld()) )
 			return;
 		
     	if ( isOnPlinth(event.getBlock().getLocation()) )
     		event.setCancelled(true);
     }
     
     @EventHandler
     public void onEntityExplode(EntityExplodeEvent event)
     {
 		if ( !plugin.isGameWorld(event.getEntity().getWorld()) )
 			return;
 		
     	List<Block> blocks = event.blockList();
 
 		// remove any plinth blocks from the list, stop them being destroyed
     	for ( int i=0; i<blocks.size(); i++ )
     		if ( isOnPlinth(blocks.get(i).getLocation()) )
     		{
     			blocks.remove(i);
     			i--;
     		}
     }
     
     @EventHandler
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
     			plugin.playerManager.setFollowTarget(event.getPlayer(), plugin.playerManager.getNearestFollowTarget(event.getPlayer()));
 				plugin.playerManager.checkFollowTarget(event.getPlayer());
     		}
     		else
     			plugin.playerManager.setFollowTarget(event.getPlayer(), null);
     	}
     }
     
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 		
 		if ( plugin.getGameState() == Killer.GameState.stagingWorldSetup && event.getPlayer().getWorld() == plugin.worldManager.stagingWorld
		  && event.getClickedBlock().getType() == Material.STONE_BUTTON )
 			plugin.worldManager.setupButtonClicked(event.getClickedBlock().getLocation().getBlockX(), event.getClickedBlock().getLocation().getBlockZ());
 		
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
     				plugin.playerManager.setFollowTarget(event.getPlayer(), plugin.playerManager.getNextFollowTarget(event.getPlayer(), info.target, true));
     				plugin.playerManager.checkFollowTarget(event.getPlayer());
     				event.getPlayer().sendMessage("Following " + info.target);
         		}
     			else if ( event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK )
         		{
     				plugin.playerManager.setFollowTarget(event.getPlayer(), plugin.playerManager.getNextFollowTarget(event.getPlayer(), info.target, false));
     				plugin.playerManager.checkFollowTarget(event.getPlayer());
     				event.getPlayer().sendMessage("Following " + info.target);
         		}
     		}
     		
     		return;
     	}
 
     	if(event.isCancelled())
     		return;
 
 	  	if(event.getClickedBlock().getType() == Material.STONE_PLATE)
 	  	{
 	        if ( isOnPlinth(event.getClickedBlock().getLocation()) )
 	        {// does the player have one of the winning items in their inventory?	        	
 	        	for ( Material material : Settings.winningItems )
 		        	if ( event.getPlayer().getInventory().contains(material) )
 					{
 		        		plugin.getGameMode().checkForEndOfGame(plugin.playerManager, event.getPlayer(), material);
 						return;
 					}
 	        }
 	  	}
     }
 
     @EventHandler
     public void onItemDrop(PlayerDropItemEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 		
     	// spectators can't drop items
     	if ( plugin.playerManager.isSpectator(event.getPlayer().getName()) )
     		event.setCancelled(true);
     }
     
     @EventHandler
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
         
         Entity attacker = null;
         Player victim = (Player)event.getEntity();
         
         if ( event instanceof EntityDamageByEntityEvent )
         {
         	Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
         	if ( damager != null )
         		if ( damager instanceof Player )
         			attacker = damager;
         		else if ( damager instanceof Arrow )
     			{
         			Arrow arrow = (Arrow)damager;
         			if ( arrow.getShooter() instanceof Player )
         				attacker = (Player)arrow.getShooter();
 				}
         }
 
     	if(PlayerManager.instance.isSpectator(victim.getName()))
     		event.setCancelled(true);
 		else if ( !plugin.getGameMode().playerDamaged(victim, attacker, event.getCause(), event.getDamage()) )
 			event.setCancelled(true);
 	}
     
     @EventHandler
     public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
     {
 		if ( !plugin.isGameWorld(event.getPlayer().getWorld()) )
 			return;
 		
 		Block affected = event.getBlockClicked().getRelative(event.getBlockFace());
 		if ( isOnPlinth(affected.getLocation()) )
 			event.setCancelled(true);
 			
 		if ( !event.isCancelled() )
 			plugin.getGameMode().playerEmptiedBucket(event);
     }
     
     @EventHandler
     public void onCraftItem(CraftItemEvent event)
     {
     	// killer recipes can only be crafter in killer worlds, or we could screw up the rest of the server
     	if ( plugin.allRecipes.contains(event.getRecipe()) && !plugin.isGameWorld(event.getWhoClicked().getWorld()) )
     		event.setCancelled(true);
     }
     
     @EventHandler
     public void onEntityTarget(EntityTargetEvent event)
     {
     	if( event.getTarget() != null && event.getTarget() instanceof Player && PlayerManager.instance.isSpectator(((Player)event.getTarget()).getName()))
     		event.setCancelled(true);
     }
     
     @EventHandler
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
     	
     	if ( !PlayerManager.instance.isSpectator(event.getPlayer().getName()))
 		{// colored player names shouldn't produce colored messages
 			event.setMessage(ChatColor.RESET + event.getMessage());
     		return;
 		}
 
     	// mark spectator chat, and hide it from non-spectators
     	event.setMessage(ChatColor.YELLOW + "[Spec] " + ChatColor.RESET + event.getMessage());
     	
     	for (Player recipient : new HashSet<Player>(event.getRecipients()))
     		if ( recipient != null && recipient.isOnline() && !PlayerManager.instance.isSpectator(recipient.getName()))
     			event.getRecipients().remove(recipient);
     }
 	
     @EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
     {
     	if ( event.getPlayer().getWorld() == plugin.worldManager.stagingWorld )
     	{// put them right where we want them. So as to avoid needing a big hole in the roof.
     		final String playerName = event.getPlayer().getName();
     		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
     			public void run()
     			{
     				Player player = plugin.getServer().getPlayerExact(playerName);
     				if ( player != null )
     					player.teleport(plugin.worldManager.getStagingWorldSpawnPoint());
     			}
     		});
     	}
     	
 		if ( plugin.isGameWorld(event.getPlayer().getWorld()) )
 			playerJoined(event.getPlayer());
 	}
 	
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event)
     {
 		if ( plugin.isGameWorld(event.getPlayer().getWorld()) )
 			playerQuit(event.getPlayer());
 	}
 	
 	private void playerJoined(Player player)
 	{
 		// if I log into the holding world (cos I logged out there), move me back to the main world's spawn and clear me out
 		if ( player.getWorld() == plugin.worldManager.stagingWorld && plugin.getGameState().usesGameWorlds && plugin.worldManager.mainWorld != null )
 		{
 			player.getInventory().clear();
 			player.setTotalExperience(0);
 			player.teleport(plugin.worldManager.mainWorld.getSpawnLocation());
 		}
 		
     	plugin.playerManager.playerJoined(player);
     }
     
 	private void playerQuit(Player player)
 	{
 		// the quit message should be sent to the scoreboard of anyone who this player was invisible to
 		for ( Player online : plugin.getOnlinePlayers() )
 			if ( !online.canSee(player) )
 				plugin.playerManager.sendForScoreboard(online, player, false);
 		
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DelayedDeathEffect(player.getName(), true), 600);
     }
     
     @EventHandler
     public void onEntityDeath(EntityDeathEvent event)
     {
     	if (!(event instanceof PlayerDeathEvent) || !plugin.isGameWorld(event.getEntity().getWorld()) )
     		return;
     	
     	PlayerDeathEvent pEvent = (PlayerDeathEvent)event;
 		
     	Player player = pEvent.getEntity();
 		if ( player == null )
 			return;
 		
 		if ( plugin.getGameMode().discreteDeathMessages() )
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
 			if ( checkDisconnected )
 			{
 				Player player = Bukkit.getServer().getPlayerExact(name);
 				if ( player != null && player.isOnline() )
 					return; // player has reconnected, so don't kill them
 				
 				if ( plugin.playerManager.numKillersAssigned() > 0 && plugin.playerManager.isAlive(name) )
 					plugin.statsManager.playerQuit();
 			}
     		plugin.playerManager.playerKilled(name);
     	}
     }
 	
 	private boolean isOnPlinth(Location loc)
 	{
 		Location plinthLoc = plugin.getPlinthLocation();
 		return  plinthLoc != null && loc.getWorld() == plinthLoc.getWorld()
 	            && loc.getX() >= plinthLoc.getBlockX() - 1
 	            && loc.getX() <= plinthLoc.getBlockX() + 1
 	            && loc.getZ() >= plinthLoc.getBlockZ() - 1
 	            && loc.getZ() <= plinthLoc.getBlockZ() + 1;
 	}
 }
