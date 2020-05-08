 /*
  * Copyright (C) 2013 mewin<mewin001@hotmail.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.mewin.WGKeepInvFlags;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import de.mewin.util.SavingValue;
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class WGKIFlagListener implements Listener {
     private WorldGuardPlugin wgPlugin;
     private WGKeepInventoryFlagsPlugin plugin;
     @SavingValue
     public Map<String, ItemStack[]> inventories, armors;
     
     public WGKIFlagListener(WGKeepInventoryFlagsPlugin plugin, WorldGuardPlugin wgPlugin)
     {
         this.plugin = plugin;
         this.wgPlugin = wgPlugin;
         
         this.inventories = new HashMap<String, ItemStack[]>();
         this.armors = new HashMap<String, ItemStack[]>();
     }
     
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent e)
     {
         if(wgPlugin.getGlobalRegionManager().allows(WGKeepInventoryFlagsPlugin.KEEP_INVENTORY_FLAG, e.getEntity().getLocation(), wgPlugin.wrapPlayer(e.getEntity())))
         {
             inventories.put(e.getEntity().getName(), e.getEntity().getInventory().getContents());
             armors.put(e.getEntity().getName(), e.getEntity().getInventory().getArmorContents());
             e.getDrops().clear();
         }
         
         if(wgPlugin.getGlobalRegionManager().allows(WGKeepInventoryFlagsPlugin.KEEP_LEVEL_FLAG, e.getEntity().getLocation(), wgPlugin.wrapPlayer(e.getEntity())))
         {
             e.setKeepLevel(true);
             e.setDroppedExp(0);
         }
     }
     
     @EventHandler
     public void onPlayerRespawn(PlayerRespawnEvent e)
     {
         if (inventories.containsKey(e.getPlayer().getName()))
         {
             e.getPlayer().getInventory().setContents(inventories.remove(e.getPlayer().getName()));
         }
         
         if (armors.containsKey(e.getPlayer().getName()))
         {
             e.getPlayer().getInventory().setArmorContents(armors.remove(e.getPlayer().getName()));
         }
     }
 }
