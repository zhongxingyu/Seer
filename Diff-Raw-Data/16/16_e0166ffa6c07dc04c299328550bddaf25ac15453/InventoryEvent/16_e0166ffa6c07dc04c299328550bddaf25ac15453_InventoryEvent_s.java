 /*
  * InventoryManager
  * Copyright (C) 2012 IndiPlex
  * 
  * InventoryManager is free software: you can redistribute it and/or modify
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
package de.indiplex.inventorymanager.event;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 import org.bukkit.inventory.Inventory;
 
 /**
  *
  * @author IndiPlex <Cartan12@indiplex.de>
  */
 public class InventoryEvent extends Event {
     
     protected Inventory inventory;
     private static final HandlerList handlers = new HandlerList();
     
 
     public InventoryEvent(Inventory inventory) {
         this.inventory = inventory;
     }
 
     public Inventory getInventory() {
         return inventory;
     }
     
     @Override
     public HandlerList getHandlers() {
         return handlers;
     }
  
     public static HandlerList getHandlerList() {
         return handlers;
     }
     
 }
