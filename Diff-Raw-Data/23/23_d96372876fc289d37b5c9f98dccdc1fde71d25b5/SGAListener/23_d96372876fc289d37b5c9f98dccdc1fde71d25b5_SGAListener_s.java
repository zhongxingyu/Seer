 package net.yeticraft.xxtraineexx.sgadvanced;
 
 
 import java.util.HashSet;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 
 
 public class SGAListener implements Listener{
 
 	public static SGAdvanced plugin;
 	public boolean setupPlatforms; 
 	public HashSet<SGABlockLoc> chestList;
 	public HashSet<SGABlockLoc> platformList;
 	public HashSet<SGABlockLoc> blockLog = new HashSet<SGABlockLoc>();
 	
 	/**
 	 * This is the constructor
 	 * @param plugin
 	 */
 	public SGAListener(SGAdvanced plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 		SGAListener.plugin = plugin;
 		setupPlatforms = false;
 		
 	}
 	
 	/**
 	 * The following method kicks in when the plugin is enabled.
 	 * @param event
 	 */
 	public void onPluginEnable (PluginEnableEvent event) {
 		
 		plugin.log.info(("Plugin detected: " + event.getPlugin().toString()));
 		
 	}
 	
 	/**
 	 * Monitoring this event so we can prevent block breaking
 	 * @param e
 	 */
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onBlockBreak(BlockBreakEvent e) {	
 		
 		// If plugin is disabled, exit.
 		if (!plugin.pluginEnable) return;
 		
 		// If the world is currently being set up, we should not process the block break event.
 		if (plugin.setupMode) return;
 		
 		// If world is not the survival games world, exit.
 		if (!plugin.worldName.equalsIgnoreCase(e.getBlock().getWorld().toString())) return;
 		
 		// Checking block to see if its on the break list. If so, allow break, log it, and return.
 		int blockType = e.getBlock().getTypeId();
 		if (plugin.breakableBlocks.contains(blockType)){
 		    blockLog.add(new SGABlockLoc(e.getBlock()));
 		    return;
 		}
 		
 		// Block is not on the list, cancel the break and return.
 		e.setCancelled(true);
 		return;}
 	
 	/**
 	 * Monitoring this event so we can prevent block placement
 	 * @param e
 	 */
     @EventHandler(priority = EventPriority.NORMAL)
     public void onBlockPlace(BlockPlaceEvent e) {
 	
     	// If plugin is disabled, exit.
     	if (!plugin.pluginEnable) return;
     	
 		// If the world is currently being set up, we should not process the block place event.
 		if (plugin.setupMode) return;
     	
     	// If world is not the survival games world, exit.
     	if (!plugin.worldName.equalsIgnoreCase(e.getBlock().getWorld().toString())) return;
     		
     	// Cancel the place and return
     	e.setCancelled(true);
     	return;}
     	
     /**
      * Monitoring this event so we can set up platforms and chests.
      * @param e
      */
     @EventHandler(priority = EventPriority.NORMAL)
     public void onBlockDamage(BlockDamageEvent e) {
     	
     	// If plugin is disabled, exit.
     	if (!plugin.pluginEnable){
     	    if (plugin.debug) plugin.log.info("Plugin disabled. Not processing BlockDamageEvent");
     	    return;
     	}
     	    
     	
     	// If world is not the survival games world, exit.
     	if (!plugin.worldName.equalsIgnoreCase(e.getBlock().getWorld().getName())) {
    	    if (plugin.debug) plugin.log.info("Current world: [" + e.getBlock().getWorld().toString() + "] is not SGA world: [" + plugin.worldName + "] Not processing BlockDamageEvent");
     	    return;
     	}
     	
 		// If the world is NOT currently being set up, we should NOT process the block damage event.
 		if (!plugin.setupMode){
 		    if (plugin.debug) plugin.log.info("Plugin is NOT in SETUPMODE. Not processing BlockDamageEvent");
 		    return;
 		}
     	
     	// Player is setting up platforms, and has permissions.
 		if (plugin.setPlatforms && e.getPlayer().hasPermission("sga.platforms")){
 			SGABlockLoc blockLoc = new SGABlockLoc(e.getBlock());
 			
             //TODO: This does not work... I'm comparing a new object to existing objects so it will never be true. I need to compare the contents of the object
 			if (platformList.contains(blockLoc)){
 				platformList.remove(blockLoc);
 				e.getPlayer().sendMessage(ChatColor.AQUA + "Platform removed from memory: " + e.getBlock().getLocation().toString());}
 			else{
 				platformList.add(blockLoc);	
 				e.getPlayer().sendMessage(ChatColor.AQUA + "Platform added to memory: " + e.getBlock().getLocation().toString());}
 			e.getPlayer().sendMessage(ChatColor.AQUA + "When you are finished adding platforms, run the SET command again and SAVE");
 			e.setCancelled(true);
 			return;
 		}
 		if (plugin.debug) plugin.log.info("Either plugin is not enabled for SETPLATFORMS or player is missing the PLATFORMS permission.");
         
 		
 		// If player is setting chests, has permissions, and the block is a chest.
         if (plugin.setChests && e.getPlayer().hasPermission("sga.chests")  && e.getBlock().getTypeId() == 54) {
             SGABlockLoc blockLoc = new SGABlockLoc(e.getBlock());
             
             //TODO: This does not work... I'm comparing a new object to existing objects so it will never be true. I need to compare the contents of the object
             if (chestList.contains(blockLoc)){
                 chestList.remove(blockLoc);
                 e.getPlayer().sendMessage(ChatColor.AQUA + "Chest removed from memory: " + e.getBlock().getLocation().toString());}
             else{
                 chestList.add(blockLoc);
                 e.getPlayer().sendMessage(ChatColor.AQUA + "Chest added to memory: " + e.getBlock().getLocation().toString());}
             e.getPlayer().sendMessage(ChatColor.AQUA + "When you are finished adding chests, run the SET command again and SAVE");
             e.setCancelled(true);
             return;
             
         }
         
         if (plugin.debug) plugin.log.info("Either plugin is not enabled for SETCHESTS or player is missing the SETCHESTS permission.");
     }
     
     /**
      * Monitoring this event to stop dead player from being further damaged
      * We also use this to prevent dead players from hurting others.
      * @param event
      */
     @EventHandler(priority = EventPriority.NORMAL)
     public void onEntityDamage(EntityDamageEvent event) {
         
         
         // If plugin is disabled, exit.
         if (!plugin.pluginEnable) return;
         
 		// If the world is currently being set up, we should not process the entity damage event.
 		if (plugin.setupMode) return;
 		
 	      // If world is not the survival games world, exit.
         if (!plugin.worldName.equalsIgnoreCase(event.getEntity().getWorld().toString())) return;
     	
         if (event.isCancelled() || (event.getDamage() == 0)) return;
         Entity entity = event.getEntity();
         if (!(entity instanceof Player)) return;
         Player player = (Player)entity;
         // Code to handle standard e damage
         if (!plugin.deadPlayerList.contains(player)) return;
         // Code to handle e v. e damage
         if (event instanceof EntityDamageByEntityEvent){
             EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
             Entity attacker = damageByEntityEvent.getDamager();
             if (attacker instanceof Projectile) attacker = ((Projectile)attacker).getShooter();
             if (attacker instanceof Player) return;
             Player attackerPlayer = (Player) attacker;
             if (!plugin.deadPlayerList.contains(attackerPlayer)) return;
         }
         
         // Looks like this is a dead player. Let's cancel their fire ticks, cancel event, and setdamage to 0
         if (entity.getFireTicks()>0) entity.setFireTicks(0);
         event.setDamage(0);
         event.setCancelled(true);
         
     }
     
     /**
      * Monitoring this event because we want to prevent monsters targeting dead players.
      * We also use this event to stop XP orbs from traveling to dead players
      * @param event
      */
     @EventHandler(priority = EventPriority.NORMAL)
     void onEntityTarget(EntityTargetEvent event) {
     	
         
         // If plugin is disabled, exit.
         if (!plugin.pluginEnable) return;
         
 		// If the world is currently being set up, we should not process the entity target event.
 		if (plugin.setupMode) return;
 		
 	      // If world is not the survival games world, exit.
         if (!plugin.worldName.equalsIgnoreCase(event.getEntity().getWorld().toString())) return;
     	
         if (event.isCancelled()) return;
         Entity target = event.getTarget();
         Entity entity = event.getEntity();
         if (!(target instanceof Player)) return;
         Player player = (Player)target;
         if (!plugin.deadPlayerList.contains(player)) return;
         event.setTarget(null);
         if (entity instanceof ExperienceOrb) entity.setVelocity(null);
     }
     
     /**
      * Monitoring this event because we want to prevent items picked up by dead players
      * @param event
      */
     @EventHandler(priority = EventPriority.NORMAL)
     void onItemPickUp(PlayerPickupItemEvent event) {
     	
         // If plugin is disabled, exit.
         if (!plugin.pluginEnable) return;
         
 		// If the world is currently being set up, we should not process the onItemPickup event.
 		if (plugin.setupMode) return;
 		
 	      // If world is not the survival games world, exit.
         if (!plugin.worldName.equalsIgnoreCase(event.getPlayer().getWorld().toString())) return;
     	
         if (event.isCancelled() ) return;
         Player player = event.getPlayer();
         if (!plugin.deadPlayerList.contains(player)) return;
         event.setCancelled(true);
     }
     
     /**
      * Monitoring this event because we want to cancel drops done by dead players
      * @param event
      */
     @EventHandler(priority = EventPriority.NORMAL)
     void onItemDrop(PlayerDropItemEvent event) {
     	
         // If plugin is disabled, exit.
         if (!plugin.pluginEnable) return;
         
 		// If the world is currently being set up, we should not process the onItemDrop event.
 		if (plugin.setupMode) return;
 		
 	      // If world is not the survival games world, exit.
         if (!plugin.worldName.equalsIgnoreCase(event.getPlayer().getWorld().toString())) return;
     	
         if (event.isCancelled() ) return;
         Player player = event.getPlayer();
         if (!plugin.deadPlayerList.contains(player)) return;
         event.setCancelled(true);
     }
     
     /**
      * Monitoring this event because we want to keep people on platforms when the match is starting
      * @param event
      */
     @EventHandler(priority = EventPriority.NORMAL)
     void onPlayerMove(PlayerMoveEvent event){
     	
         // If plugin is disabled, exit.
         if (!plugin.pluginEnable) return;
         
         // If world is not the survival games world, exit.
         if (!plugin.worldName.equalsIgnoreCase(event.getPlayer().getWorld().toString())) return;
         
         // If the world is currently being set up, we should not process the onItemDrop event.
         if (plugin.setupMode) return;
         
     	if (plugin.sgaEvents.platformBoobyTrap){
     	    // Looks like the platforms are booby trapped... we need to make sure no one is moving from their platform
     	    Player player = event.getPlayer();
     	    Location platformLoc = plugin.sgaEvents.playerPlatform.get(player);
     	    if (player.getLocation().distance(platformLoc) > 1){
     	        player.sendMessage(plugin.prefix + "You've left the platform before the game started.");
     		    Bukkit.getServer().getWorld(plugin.worldName).strikeLightning(player.getLocation());
     		    player.setHealth(0);
     	    }
     	}
     	
     	if (plugin.sgaEvents.deathmatch){
     	 // Looks like we are in a deathmatch, we need to make sure eveyrone stays inside the boundary
             Player player = event.getPlayer();
             if (player.getLocation().distance(plugin.spawnLoc) > plugin.deathmatchBoundary){
                 player.sendMessage(plugin.prefix + "Coward! You've wandered to far from the deathmatch.");
                 Bukkit.getServer().getWorld(plugin.worldName).strikeLightning(player.getLocation());
                 player.setHealth(0);
             }
     	}
     	
     }
     @EventHandler(priority = EventPriority.NORMAL)
     void onPlayerQuit(PlayerQuitEvent event) {
 
         Player player = event.getPlayer();
         // If plugin is disabled, exit.
         if (!plugin.pluginEnable) return;
         
         // If the world is currently being set up, we should not process the onItemDrop event.
         if (plugin.setupMode) return;
         
         // Remove disconnected player from all lists.
         if (plugin.alivePlayerList.contains(player)) plugin.alivePlayerList.remove(player);
         if (plugin.deadPlayerList.contains(player)) plugin.deadPlayerList.remove(player);
         if (plugin.playerQueue.contains(player)) plugin.playerQueue.remove(player);
         
     }
     
     
 }
