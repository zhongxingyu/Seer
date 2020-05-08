 /**
  *  Name:    PlayerRemoveEvent.java
  *  Created: 19:22:07 - 12 jun 2013
  * 
  *  Author:  Lucas Arnstrm - LucasEmanuel @ Bukkit forums
  *  Contact: lucasarnstrom(at)gmail(dot)com
  *  
  *
  *  Copyright 2013 Lucas Arnstrm
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program. If not, see <http://www.gnu.org/licenses/>.
  *  
  *
  *
  *  Filedescription:
  *
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse.events;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 
 public class PlayerRemoveEvent extends Event {
 	
 	private static final HandlerList handlers = new HandlerList();
 	
     private Player player;
  
     public PlayerRemoveEvent(Player player) {
     	this.player = player;
     }
  
    public Player getplayer() {
         return player;
     }
  
     public HandlerList getHandlers() {
         return handlers;
     }
  
     public static HandlerList getHandlerList() {
         return handlers;
     }
 }
