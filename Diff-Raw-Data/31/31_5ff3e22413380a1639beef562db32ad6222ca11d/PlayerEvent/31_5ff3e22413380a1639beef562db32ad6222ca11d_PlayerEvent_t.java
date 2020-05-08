 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 /**
  * 
  */
 package com.andune.minecraft.commonlib.server.bukkit.events;
 
 
 import com.andune.minecraft.commonlib.server.api.Player;
 import com.andune.minecraft.commonlib.server.bukkit.BukkitFactory;
 import com.google.inject.Inject;
 
 /** Base class that represents an event about a player.
  * 
  * @author andune
  *
  */
 public abstract class PlayerEvent {
     protected final org.bukkit.event.player.PlayerEvent bukkitPlayerEvent;
     protected final BukkitFactory bukkitFactory;
    private Player player;
     
     @Inject
     public PlayerEvent(org.bukkit.event.player.PlayerEvent bukkitPlayerEvent, BukkitFactory bukkitFactory) {
         this.bukkitPlayerEvent = bukkitPlayerEvent;
         this.bukkitFactory = bukkitFactory;
     }
 
     @Inject
     public PlayerEvent(org.bukkit.entity.Player player, BukkitFactory bukkitFactory) {
         this.bukkitFactory = bukkitFactory;
         this.player = bukkitFactory.newBukkitPlayer(player);
         this.bukkitPlayerEvent = null;
     }
 
     public Player getPlayer() {
         if( player == null )
             player = bukkitFactory.newBukkitPlayer(bukkitPlayerEvent.getPlayer());
         
         return player;
     }
 }
