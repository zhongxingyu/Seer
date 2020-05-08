 package net.amunak.bukkit.plugin_DropsToInventory;
 
 /**
  * Copyright 2013 Jiří Barouš
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * This is the main class of plugin DropsToInventory. It acts both as a plugin
  * and listener.
  *
  * @author Amunak
  */
 public class DropsToInventory extends JavaPlugin implements Listener {
 
     protected static Log log;
     protected FileConfiguration config;
     public List<String> allowedEntities;
 
     @Override
     public void onEnable() {        
         log = new Log(this);
        log.raiseFineLevel = true;
         log.fine("Plugin enabled");
         log.fine("Fine logging will be seen");
 
         this.saveDefaultConfig();
         config = this.getConfig();
         allowedEntities = config.getStringList("lists.allowedEntities");
         Common.fixEnumLists(allowedEntities);
 
         if (config.getBoolean("options.general.checkVersion")) {
             CheckVersion.check(this);
         }
 
         this.getServer().getPluginManager().registerEvents(this, this);
         if (config.getBoolean("options.blocks.allow")) {
             this.getServer().getPluginManager().registerEvents(new BlockBreakEventListener(this), this);
         }
     }
 
     @Override
     public void onDisable() {
         HandlerList.unregisterAll((JavaPlugin) this);
         log.fine("Plugin disabled");
     }
     
     private void reloadConfigurtion() {
         /**
          * We need to re-register event listeners when reloading configuration!
          */
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event){
        log.fine("Entity " + event.getEntityType() + " died by " + event.getEntity().getKiller());
         //We ignore everything not killed by a player
        if ((event.getEntity().getKiller() != null) && (event.getEntity().getKiller() instanceof Player)) {
            if (config.getBoolean("options.entities.allow") && allowedEntities.contains(event.getEntity().getType().toString())) {
                log.fine("dropping inventory of dead " + event.getEntityType() + " to " + event.getEntity().getKiller());
                 this.moveDropToInventory(event.getEntity().getKiller(), event.getDrops(), event.getDroppedExp(), event.getEntity().getLocation());
                event.setDroppedExp(0);
                event.getDrops().clear();
             }
         }
     }
 
     /**
      * moves drop and xp into player's inventory, leaving leftover on specified
      * location
      *
      * @param player the player
      * @param drop collection of drop
      * @param xp amount of xp
      * @param leftoverDropLocation the location for leftover
      */
     protected void moveDropToInventory(Player player, Collection<ItemStack> drop, Integer xp, Location leftoverDropLocation) {
         HashMap<Integer, ItemStack> leftover;
 
         if (xp != null) {
            log.fine("giving " + xp + " xp");
             player.giveExp(xp);
         }
 
         leftover = player.getInventory().addItem(drop.toArray(new ItemStack[0]));
 
         for (Map.Entry<Integer, ItemStack> entry : leftover.entrySet()) {
             player.getWorld().dropItemNaturally(leftoverDropLocation, entry.getValue());
         }
     }
     
 }
