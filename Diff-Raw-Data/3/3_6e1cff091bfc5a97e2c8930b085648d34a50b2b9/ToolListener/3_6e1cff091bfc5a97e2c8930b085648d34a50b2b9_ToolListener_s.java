 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of TheRock.
  * 
  * TheRock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * TheRock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with TheRock.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.therock.listener;
 
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 import de.minestar.therock.tools.Tool;
 
 public class ToolListener implements Listener {
 
     private HashMap<Integer, Tool> toolList = new HashMap<Integer, Tool>();
 
     public void addTool(Tool tool) {
         this.toolList.put(tool.getToolID(), tool);
     }
 
     public boolean isTool(int ID) {
         return (this.toolList.get(ID) != null);
     }
 
     public Tool getTool(int ID) {
         return this.toolList.get(ID);
     }
 
     private boolean onBlockInteract(final Player player, final Block block, final BlockFace blockFace, final boolean isLeftClick) {
         Tool tool = this.getTool(player.getItemInHand().getTypeId());
         if (tool != null) {
             tool.onBlockInteract(player, block, blockFace, isLeftClick);
             return true;
         }
         return false;
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onInventoryClick(InventoryClickEvent event) {
 
         // check the inventory type
         InventoryType type = event.getInventory().getType();
         if (type != InventoryType.CHEST && type != InventoryType.DISPENSER && type != InventoryType.FURNACE && type != InventoryType.BREWING && type != InventoryType.ENDER_CHEST) {
             return;
         }
 
         // get the current item
         ItemStack inCursor = event.getCursor();
 
         // fix slot < 0
         if (event.getSlot() < 0) {
             return;
         }
 
         // get the clicked item
         ItemStack inSlot = event.getView().getItem(event.getRawSlot());
 
         // get the player
         Player player = (Player) event.getWhoClicked();
 
         boolean cursorNull = (inCursor == null || inCursor.getTypeId() == Material.AIR.getId());
         boolean slotNull = (inSlot == null || inSlot.getTypeId() == Material.AIR.getId());
 
         // Cursor = null && Slot == null => nothing happens
         if (cursorNull && slotNull) {
             return;
         }
 
         if (!slotNull) {
             // do we have a tool?
             int ID = inSlot.getTypeId();
             if (this.isTool(ID)) {
                 Tool tool = this.getTool(ID);
                 if (tool.hasPermission(player)) {
                     inSlot.setAmount(0);
                     event.setCancelled(true);
                     return;
                 }
             }
         }
 
         if (!cursorNull) {
             // do we have a tool?
             int ID = inCursor.getTypeId();
             if (this.isTool(ID)) {
                 Tool tool = this.getTool(ID);
                 if (tool.hasPermission(player)) {
                     inSlot.setAmount(0);
                     event.setCancelled(true);
                     return;
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
             if (this.onBlockInteract(event.getPlayer(), event.getClickedBlock(), event.getBlockFace(), true)) {
                 event.setCancelled(true);
                 return;
             }
         } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             if (this.onBlockInteract(event.getPlayer(), event.getClickedBlock(), event.getBlockFace(), false)) {
                 event.setCancelled(true);
                 return;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onItemDrop(PlayerDropItemEvent event) {
         // event cancelled => return
         if (event.isCancelled())
             return;
 
         // do we have a tool?
         int ID = event.getItemDrop().getItemStack().getTypeId();
         if (this.isTool(ID)) {
             Tool tool = this.getTool(ID);
             if (tool.hasPermission(event.getPlayer())) {
                // event.getItemDrop().setItemStack(null);
                event.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerDeath(PlayerDeathEvent event) {
         // prevent dropping of the lookup-tool
         for (Tool tool : this.toolList.values()) {
             if (tool.hasPermission(event.getEntity())) {
                 for (int i = event.getDrops().size() - 1; i >= 0; i--) {
                     if (event.getDrops().get(i).getTypeId() == tool.getToolID()) {
                         event.getDrops().remove(i);
                     }
                 }
             }
         }
     }
 }
