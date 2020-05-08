 package net.amunak.bukkit.plugin_DropsToInventory;
 
 /**
  *
  * @author Amunak
  */
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class DropsToInventory extends JavaPlugin implements Listener {
 
     protected static Log log;
     protected FileConfiguration config;
     public List<String> filter;
 
     @Override
     public void onEnable() {
         log = new Log(this);
         log.fine("Plugin enabled");
 
         this.saveDefaultConfig();
         config = this.getConfig();
         filter = config.getStringList("filter");
         for (String string : filter) {
             string = string.toUpperCase();
         }
 
         if (config.getBoolean("options.checkVersion")) {
             CheckVersion.check(this);
         }
 
         this.getServer().getPluginManager().registerEvents(this, this);
     }
 
     @Override
     public void onDisable() {
         HandlerList.unregisterAll((JavaPlugin) this);
         log.fine("Plugin disabled");
     }
 
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
     public void onBlockBreakEvent(BlockBreakEvent event) {
             if (config.get("options.filterMode").equals("blacklist")) {
                 if (!filter.contains(event.getBlock().getType().toString())) {
                     this.moveToInventory(event);
                 }
             } else if (config.get("options.filterMode").equals("whitelist")) {
                 if (filter.contains(event.getBlock().getType().toString())) {
                     this.moveToInventory(event);
                 }
             }
     }
 
     private void moveToInventory(BlockBreakEvent event) {
         HashMap<Integer, ItemStack> leftover;
         Player player;
 
         player = event.getPlayer();
 
         event.setCancelled(true);
         event.getBlock().setType(Material.AIR);
 
        leftover = player.getInventory().addItem(event.getBlock().getDrops(player.getItemInHand()).toArray(new ItemStack[0]));
         
         for (Map.Entry<Integer, ItemStack> entry : leftover.entrySet()) {
             event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), entry.getValue());
         }
     }
 }
