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
 
 package com.mewin.wgFlyFlag;
 
 import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
 import com.mewin.util.ConfigMgr;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WGFlyFlagPlugin extends JavaPlugin
 {
     public static final StateFlag FLY_FLAG = new StateFlag("fly", false);
    public Map lastPvp;
     public int pvpTimeOut;
     private WorldGuardPlugin wgPlugin;
     private WGCustomFlagsPlugin custPlugin;
     private RegionListener regionListener;
     private PlayerListener playerListener;
     private ConfigMgr conf;
 
     public WGFlyFlagPlugin()
     {
         pvpTimeOut = -1;
     }
 
     @Override
     public void onEnable()
     {
         wgPlugin = getWGPlugin();
         custPlugin = getCustPlugin();
         if(wgPlugin == null)
         {
             getLogger().log(Level.WARNING, "This plugin requires WorldGuard. Deactivating.");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         if(custPlugin == null)
         {
             getLogger().log(Level.WARNING, "This plugin requires WG Custom Flags. Deactivating.");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         custPlugin.addCustomFlag(FLY_FLAG);
         regionListener = new RegionListener(wgPlugin, this);
         loadConfig();
         lastPvp = new HashMap();
         if(pvpTimeOut > -1)
         {
             playerListener = new PlayerListener(this);
             getServer().getPluginManager().registerEvents(playerListener, wgPlugin);
         }
         startUpdate();
     }
 
     private void startUpdate()
     {
         getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
 
             @Override
             public void run()
             {
                 regionListener.onUpdate();
             }
         }
         , 100L, 20L);
     }
 
     private WorldGuardPlugin getWGPlugin()
     {
         org.bukkit.plugin.Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
         if(plugin == null || !(plugin instanceof WorldGuardPlugin))
         {
             return null;
         }
         else
         {
             return (WorldGuardPlugin)plugin;
         }
     }
 
     private WGCustomFlagsPlugin getCustPlugin()
     {
         org.bukkit.plugin.Plugin plugin = getServer().getPluginManager().getPlugin("WGCustomFlags");
         if(plugin == null || !(plugin instanceof WGCustomFlagsPlugin))
         {
             return null;
         }
         else
         {
             return (WGCustomFlagsPlugin)plugin;
         }
     }
 
     public void loadConfig()
     {
         getConfig().addDefault("send-messages", true);
         getConfig().set("send-messages", true);
         getConfig().addDefault("messages.fly-start", "§6You can fly now.");
         getConfig().set("messages.fly-start", "§6You can fly now.");
         getConfig().addDefault("messages.fly-end", "§6You cannot fly anymore.");
         getConfig().set("messages.fly-end", "§6You cannot fly anymore.");
         getConfig().addDefault("pvp-events", true);
         getConfig().set("pvp-events", true);
         getConfig().addDefault("pvp-timeout", 10000);
         getConfig().set("pvp-timeout", 10000);
         conf = new ConfigMgr(this);
         conf.load(true);
         pvpTimeOut = getConfig().getBoolean("pvp-events") ? getConfig().getInt("pvp-timeout", 10000) : -1;
     }
 }
