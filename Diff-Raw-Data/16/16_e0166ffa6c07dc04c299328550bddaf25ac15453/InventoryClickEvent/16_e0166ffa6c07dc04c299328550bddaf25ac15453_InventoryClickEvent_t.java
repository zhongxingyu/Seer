 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
package de.indiplex.moreevents.event;
 
 import de.indiplex.moreevents.IMNetHandler.InventorySlotType;
 import org.bukkit.event.HandlerList;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author Webteam
  */
 public class InventoryClickEvent extends InventoryEvent {
     private InventorySlotType inventorySlotType;
     private ItemStack itemStack;
     private static final HandlerList handlers = new HandlerList();
 
     public InventoryClickEvent(Inventory inventory, InventorySlotType inventorySlotType, ItemStack itemStack) {
         super(inventory);
         this.inventorySlotType = inventorySlotType;
         this.itemStack = itemStack;
     }
 
     public InventorySlotType getInventorySlotType() {
         return inventorySlotType;
     }
 
     public ItemStack getItemStack() {
         return itemStack;
     }
     
     @Override
     public HandlerList getHandlers() {
         return handlers;
     }
  
     public static HandlerList getHandlerList() {
         return handlers;
     }
 
     @Override
     public String toString() {
         return "ist: "+inventorySlotType.toString()+"; it: "+itemStack.toString()+"; inv: "+inventory.toString();
     }
     
 }
