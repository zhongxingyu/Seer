 package org.meekers.plugins.meekarena;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 /**
  *
  * @author jaredm
  */
 class MeekArenaPluginListener implements Listener {
 
     MeekArena plugin;
 
     public MeekArenaPluginListener(MeekArena plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onDeath(EntityDamageEvent event) {
         String rname = null;
 
         if (event.getEntity() instanceof Player) { // .getType() == EntityType.PLAYER) {
             Player receiver = (Player) event.getEntity();
             rname = receiver.getPlayerListName();
             String pworld = receiver.getWorld().getName();
 
 
             if ("arena".equals(pworld)) {
                 int damage = event.getDamage();
                 int healthleft = receiver.getHealth() - damage;
                 //receiver.sendMessage("Dmg recd: " + damage + ", hp left: " + healthleft);                
 
                 // Detect death blow
                 if (damage > healthleft) {
                     event.setCancelled(true);
 
                     // Send player dead message
                     Bukkit.broadcastMessage(receiver.getPlayerListName() + " died by " + event.getCause().name());
 
                     // teleport player to spawn
                     Location spawn = new Location(receiver.getWorld(), receiver.getWorld().getSpawnLocation().getX(), receiver.getWorld().getHighestBlockYAt(receiver.getWorld().getSpawnLocation()) + 2, receiver.getWorld().getSpawnLocation().getZ());
                     receiver.teleport(spawn);
 
                     // give default inventory
                     this.plugin.setInventory(receiver);
 
                     // full heal
                     this.plugin.fullHeal(receiver);
                 }
             }
         }
     }
 
     @EventHandler
     public void onQuit(PlayerQuitEvent event) {
         Player player = event.getPlayer();
         String pworld = player.getWorld().getName();
         if ("arena".equals(pworld)) {
             this.plugin.restoreState(player);
             this.plugin.fullHeal(player);
         }
     }
 
     @EventHandler
     public void onWorldLeave(PlayerChangedWorldEvent event) {
         Player player = event.getPlayer();
         String pworld = event.getFrom().getName();
 //        player.sendMessage("you just changed from: "+pworld);
         if ("arena".equals(pworld)) {
             this.plugin.restoreState(player);
             this.plugin.fullHeal(player);
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onBorderBreak(BlockBreakEvent event) {
         if ("arena".equals(event.getBlock().getLocation().getWorld().getName())) {
             Block evblock = event.getBlock();
             double blockX = evblock.getLocation().getX();
             double blockY = evblock.getLocation().getY();
             double blockZ = evblock.getLocation().getZ();
             Location arenaspawn = Bukkit.getWorld("arena").getSpawnLocation();
             double arenaX = arenaspawn.getX();
             double arenaY = arenaspawn.getY();
             double arenaZ = arenaspawn.getZ();
 
             int radius = this.plugin.getConfig().getInt("borderradius");
             if (blockX >= (arenaX + radius) || blockX <= (arenaX - radius) || blockZ >= (arenaZ + radius) || blockZ <= (arenaZ - radius) || blockY >= 255) {
                 event.setCancelled(true);
             }
         }
     }
 }
