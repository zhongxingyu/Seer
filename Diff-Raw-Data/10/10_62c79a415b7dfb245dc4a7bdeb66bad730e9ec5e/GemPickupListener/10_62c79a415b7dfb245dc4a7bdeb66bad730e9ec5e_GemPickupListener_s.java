 /**
  * Small plugin to enable the storage of experience points in an item.
  * 
  * Rewrite of the original PearlXP created by Nebual of nebtown.info in March 2012.
  * 
  * rewrite by: Marex, Zonta.
  * 
  * contact us at : plugins@makeitonthe.net
  * 
  * Copyright (C) 2012 belongs to their respective owners
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.makeitonthe.GemXp;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 public class GemPickupListener implements Listener {
 
 	public GemPickupListener(GemXp plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 
 	@EventHandler
 	public void onGemPickUp(PlayerPickupItemEvent event) {
 		Item eventItem = event.getItem();
 		ItemStack pickUpItem = eventItem.getItemStack();
 		Inventory inv;
 		XpContainer pickUpGem;
 		XpContainer similarGem;
 
 		if (XpContainer.isAnXpContainer(pickUpItem)) {
 			event.setCancelled(true);
 			pickUpGem = new XpContainer(pickUpItem);
 			inv = event.getPlayer().getInventory();
 
 			// find a stack to add on top or puts it in an empty space,
 			// otherwise let it on the ground
 			similarGem = pickUpGem.findSimilarStack(inv);
 			if (similarGem != null) {
 
				if (pickUpGem.getAmount() == 1) {
 					similarGem.setAmount(similarGem.getAmount() + 1);
 					pickupAnimation(eventItem, event.getPlayer());
 					event.getItem().remove();
 				}
 
 			} else if (inv.firstEmpty() >= 0) {
 				inv.setItem(inv.firstEmpty(), pickUpItem);
 				event.getItem().remove();
 			}
 		}
 	}
 	
 	private void pickupAnimation(Item item, Player player) {
 		Location itemLoc = item.getLocation();
 		Location playerLoc = player.getEyeLocation();
 		Vector v;
 		
 		playerLoc.setY(playerLoc.getY() - 0.5);
 		v = new Vector(playerLoc.getX() - itemLoc.getX(), 
 				playerLoc.getY() - itemLoc.getY(),
 				playerLoc.getZ() - itemLoc.getZ());
 		
 		item.setVelocity(v);
 	}
 
 }
