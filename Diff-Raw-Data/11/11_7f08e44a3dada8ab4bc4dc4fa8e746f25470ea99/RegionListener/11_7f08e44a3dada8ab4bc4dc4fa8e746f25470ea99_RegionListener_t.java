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
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.entity.Player;
 
 public class RegionListener
 {
 
     private WorldGuardPlugin wgPlugin;
     private WGFlyFlagPlugin plugin;
     private List allowedPlayers;
 
     public RegionListener(WorldGuardPlugin wgPlugin, WGFlyFlagPlugin plugin)
     {
         this.wgPlugin = wgPlugin;
         this.plugin = plugin;
         allowedPlayers = new ArrayList();
     }
 
     public void onUpdate()
     {
         Player arr$[] = Bukkit.getOnlinePlayers();
         int len$ = arr$.length;
         for(int i$ = 0; i$ < len$; i$++)
         {
             Player player = arr$[i$];
             if(!player.hasPermission("flyflag.ignore"))
             {
                 updateFlyFlag(player);
             }
         }
 
     }
 
     private void updateFlyFlag(Player player)
     {
         boolean allowFlight = player.getAllowFlight();
         if(player.getGameMode() != GameMode.CREATIVE)
         {
             RegionManager rm = wgPlugin.getRegionManager(player.getWorld());
             if(plugin.pvpTimeOut > -1 && plugin.lastPvp.containsKey(player))
             {
                long lastPvp = plugin.lastPvp.get(player);
                 if(System.currentTimeMillis() - lastPvp < plugin.pvpTimeOut)
                 {
                     allowFlight = false;
                 }
                 else
                 {
                     plugin.lastPvp.remove(player);
                 }
             } else if(rm != null)
             {
                 ApplicableRegionSet regions = rm.getApplicableRegions(player.getLocation());
                 allowFlight = regions.allows(WGFlyFlagPlugin.FLY_FLAG);
             }
             if(allowFlight != player.getAllowFlight())
             {
                 if(allowFlight)
                 {
                     if(plugin.getConfig().getBoolean("send-messages", false) && player.getAllowFlight() != allowFlight)
                     {
                         player.sendMessage(plugin.getConfig().getString("messages.fly-start", ChatColor.DARK_RED + "Error: message missing!").replaceAll("&", "§"));
                     }
                     player.setAllowFlight(true);
                     allowedPlayers.add(player.getName());
                 } else if(allowedPlayers.contains(player.getName()))
                 {
                     allowedPlayers.remove(player.getName());
                     if(plugin.getConfig().getBoolean("send-messages", false) && player.getAllowFlight() != allowFlight)
                     {
                         player.sendMessage(plugin.getConfig().getString("messages.fly-end", ChatColor.DARK_RED + "Error: message missing!").replaceAll("&", "§"));
                     }
                     player.setFallDistance(1.0F);
                     player.setAllowFlight(false);
                 }
             }
         }
     }
 }
