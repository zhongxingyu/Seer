 package com.dumptruckman.bartersigns.listener;
 
 import com.dumptruckman.bartersigns.BarterSignsPlugin;
 import com.dumptruckman.bartersigns.config.ConfigPath;
 import com.dumptruckman.bartersigns.sign.BarterSign;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author dumptruckman
  */
 public class BarterSignsEntityListener implements Listener {
 
     private BarterSignsPlugin plugin;
 
     public BarterSignsEntityListener(BarterSignsPlugin plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityExplode(EntityExplodeEvent event) {
         if (event.isCancelled()) return;
 
         List<Block> blocks = event.blockList();
         List<BarterSign> barterSigns = new ArrayList<BarterSign>();
         for (Block block : blocks) {
             if (block.getState() instanceof Sign) {
                 if (BarterSign.exists(plugin, block)) {
                    barterSigns.add(plugin.signManager.getBarterSignFromBlock(block));
                     if (plugin.config.getConfig().getBoolean(ConfigPath.SIGN_INDESTRUCTIBLE.getPath(),
                             (Boolean) ConfigPath.SIGN_INDESTRUCTIBLE.getDefault())) {
                         event.setCancelled(true);
                         return;
                     }
                 }
             }
         }
 
         if (plugin.config.getConfig().getBoolean(ConfigPath.SIGN_DROPS_ITEMS.getPath(),
                 (Boolean) ConfigPath.SIGN_DROPS_ITEMS.getDefault())) {
             for (BarterSign sign : barterSigns) {
                 sign.drop();
             }
         }
     }
 }
