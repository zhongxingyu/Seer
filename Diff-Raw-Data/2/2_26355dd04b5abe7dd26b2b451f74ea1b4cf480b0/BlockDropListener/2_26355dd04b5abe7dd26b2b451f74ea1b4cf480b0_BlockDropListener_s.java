 package com.meggawatts.MeggaChat;
 
 import java.util.List;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class BlockDropListener implements Listener {
 
     Block blok;
     Player executor;
 
    @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockBreak(BlockBreakEvent event) {
         blok = event.getBlock();
         executor = event.getPlayer();
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void removeent(ItemSpawnEvent event) {
         Block spawnloc = event.getLocation().getBlock();
         if (spawnloc.equals(blok) && executor.hasPermission("meggachat.noharvest")) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void noThrow(PlayerDropItemEvent event) {
         Player player = event.getPlayer();
         Item drop = event.getItemDrop();
         if (player.hasPermission("meggachat.nothrow")) {
             drop.remove();
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void noDropOnDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         List<ItemStack> drops = event.getDrops();
         if (player.hasPermission("meggachat.nodeathdrop")) {
             for (ItemStack drop : drops) {
                 drop.setAmount(0);
             }
         }
     }
 }
