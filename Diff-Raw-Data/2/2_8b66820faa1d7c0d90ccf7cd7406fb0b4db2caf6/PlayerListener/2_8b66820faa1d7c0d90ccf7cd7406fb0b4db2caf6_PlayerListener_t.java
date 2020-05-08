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
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 public class PlayerListener implements Listener
 {
 
     private WGFlyFlagPlugin plugin;
 
     public PlayerListener(WGFlyFlagPlugin plugin)
     {
         this.plugin = plugin;
     }
 
     public void onEntityDamage(EntityDamageEvent e)
     {
         if(e.getEntity() instanceof Player)
         {
            plugin.lastPvp.put((Player) e.getEntity(), System.currentTimeMillis());
         }
     }
 }
