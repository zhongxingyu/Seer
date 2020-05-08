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
 
 import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.flags.RegionGroup;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import de.mewin.util.ValueSaver;
 import java.io.File;
 import java.util.logging.Level;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class WGKeepInventoryFlagsPlugin extends JavaPlugin {
     public static final StateFlag KEEP_INVENTORY_FLAG = new StateFlag("keep-inventory", false, RegionGroup.ALL);
     public static final StateFlag KEEP_LEVEL_FLAG = new StateFlag("keep-level", false, RegionGroup.ALL);
     
     private WGCustomFlagsPlugin custPlugin;
     private WorldGuardPlugin wgPlugin;
     private WGKIFlagListener listener;
     private ValueSaver saver;
     
     @Override
     public void onEnable()
     {
         Plugin plug = getServer().getPluginManager().getPlugin("WGCustomFlags");
         
         if (plug == null || !(plug instanceof WGCustomFlagsPlugin) || !plug.isEnabled())
         {
             getLogger().warning("Could not load WorldGuard Custom Flags Plugin, disabling");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         else
         {
             custPlugin = (WGCustomFlagsPlugin) plug;
         }
         
         plug = getServer().getPluginManager().getPlugin("WorldGuard");
         
         if (plug == null || !(plug instanceof WorldGuardPlugin) || !plug.isEnabled())
         {
             getLogger().warning("Could not load WorldGuard Plugin, disabling");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         else
         {
             wgPlugin = (WorldGuardPlugin) plug;
         }
         
         custPlugin.addCustomFlag(KEEP_INVENTORY_FLAG);
         custPlugin.addCustomFlag(KEEP_LEVEL_FLAG);
         
         listener = new WGKIFlagListener(this, wgPlugin);
         
         loadInventories();
         getServer().getPluginManager().registerEvents(listener, this);
     }
     
     @Override
     public void onDisable()
     {
         saveInventories();
     }
     
     public void saveInventories()
     {
         try
         {
             saver.save();
         }
         catch(Exception ex)
         {
             getLogger().log(Level.WARNING, "Could not save inventories: ", ex);
         }
     }
     
     public void loadInventories()
     {
         try
         {
            if (saver == null)
            {
                saver = new ValueSaver(listener, new File(this.getDataFolder(), "inventories.yml"));
            }
             saver.load();
         }
         catch(Exception ex)
         {
             getLogger().log(Level.WARNING, "Could not load inventories: ", ex);
         }
     }
 }
