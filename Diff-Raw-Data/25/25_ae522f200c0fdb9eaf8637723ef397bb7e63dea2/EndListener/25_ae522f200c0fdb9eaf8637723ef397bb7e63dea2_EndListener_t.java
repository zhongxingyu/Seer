 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.listener
  * Created: 2013/01/12 19:19:24
  */
 package net.syamn.sakuracmd.listener;
 
 import net.syamn.sakuracmd.SakuraCmd;
 import net.syamn.sakuracmd.manager.Worlds;
import net.syamn.sakuracmd.worker.AFKWorker;
 import net.syamn.utils.Util;
 
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World.Environment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityCreatePortalEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 
 /**
  * EndListener (EndListener.java)
  * @author syam(syamn)
  */
 public class EndListener implements Listener{
     private SakuraCmd plugin;
     public EndListener (final SakuraCmd plugin){
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDeath(final EntityDeathEvent event) {
         if (event.getEntity().getType() == EntityType.ENDER_DRAGON && event.getEntity().getKiller() != null && event.getEntity().getWorld().getName().equals(Worlds.main_end)) {
             final int normal_end_DragonExp = 7000;
 
             event.setDroppedExp(normal_end_DragonExp);
             Util.broadcastMessage("&6" + event.getEntity().getKiller().getName() + " &bさんがドラゴンを倒しました！");
             Util.broadcastMessage("&aエンドワールドは6時間後に自動再生成されます！", false);
             Util.worldcastMessage(event.getEntity().getWorld(), "&aメインワールドに戻るには&f /spawn &aコマンドを使ってください！");
 
             //Actions.log("End.log", "Player " + event.getEntity().getKiller().getName() + " killed the EnderDragon at world " + event.getEntity().getWorld().getName());
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDamage(final EntityDamageEvent event) {
         final Entity ent = event.getEntity();
 
         if (ent.getWorld().getName().equals(Worlds.main_end) && (ent.getType() == EntityType.ENDER_DRAGON || ent.getType() == EntityType.COMPLEX_PART)) {
             event.setDamage(event.getDamage() / 2); // ドラゴンHP x2
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
         if (!event.getEntity().getWorld().getName().equals(Worlds.main_end)){
             return;
         }
 
         if (event.getEntity() instanceof Player){
             event.setDamage(event.getDamage() + 6);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onEntityCreatePortal(final EntityCreatePortalEvent event) {
         if (!event.getEntityType().equals(EntityType.ENDER_DRAGON)) {
             return;
         }
         event.setCancelled(true);
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onChunkUnload(final ChunkUnloadEvent event) {
         if (event.getWorld().getEnvironment() != Environment.THE_END) {
             return;
         }
 
         final Chunk chunk = event.getChunk();
         for (final Entity entity : chunk.getEntities()) {
             if (entity.getType() == EntityType.ENDER_DRAGON || entity.getType() == EntityType.COMPLEX_PART) {
                 event.setCancelled(true);
                 return;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onItemSpawn(final ItemSpawnEvent event) {
         final Item item = event.getEntity();
         if (item.getWorld().getEnvironment().equals(Environment.THE_END) && item.getItemStack().getType() == Material.DRAGON_EGG) {
             event.setCancelled(true);
         }
     }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event){
        final Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BED_BLOCK && 
                player.getWorld().getEnvironment().equals(Environment.THE_END)) {
            event.setCancelled(true);
        }
    }
 }
