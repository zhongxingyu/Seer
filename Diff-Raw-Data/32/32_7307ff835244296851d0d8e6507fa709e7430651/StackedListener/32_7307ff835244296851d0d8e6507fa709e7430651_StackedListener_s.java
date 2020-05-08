 /*
  * Copyright (c) 2012-2013 Sean Porter <glitchkey@gmail.com>
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
 
 package org.stacked;
 
 //* IMPORTS: JDK/JRE
 	//* NOT NEEDED
 //* IMPORTS: BUKKIT
 	import org.bukkit.Bukkit;
 	import org.bukkit.entity.Item;
 	import org.bukkit.entity.Player;
 	import org.bukkit.event.EventHandler;
 	import org.bukkit.event.EventPriority;
 	import org.bukkit.event.Listener;
 	import org.bukkit.event.player.PlayerPickupItemEvent;
 	import org.bukkit.inventory.ItemStack;
 	import org.bukkit.Location;
 	import org.bukkit.plugin.Plugin;
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class StackedListener implements Listener {
 	Plugin plugin;
 
 	public StackedListener(Plugin plugin) {
 		Bukkit.getPluginManager().registerEvents(this, plugin);
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		if((Stacked.config == null) || !Stacked.config.automatic)
 			return;
 		else if (event == null)
 			return;
 		else if (event instanceof StackedPlayerPickupItemEvent)
 			return;
 		else if (event.getItem() == null)
 			return;
 		else if(event.getPlayer() == null)
 			return;
 		else if (event.getItem().getItemStack() == null)
 			return;
 
 		Player p = event.getPlayer();
 
 		if(!p.hasPermission("stacked.stack.automatic"))
 			return;
 
 		ItemStack item = event.getItem().getItemStack();
 		ItemStack copy = item.clone();
 
 		StackInventory stacker = new StackInventory(plugin, p, copy);
 		
 		boolean ready = stacker.stack();
 		int rem = stacker.remaining;
 
 		if (copy.getAmount() == item.getAmount())
 			return;
 		else if (rem == item.getAmount())
 			return;
 
 		event.setCancelled(true);
 
 		Item drop = event.getItem();
 		int taken = item.getAmount() - rem;
 
 		if (taken <= 0)
 			return;
 
 		StackedPlayerPickupItemEvent replacement;
 		replacement = new StackedPlayerPickupItemEvent(p, drop, rem);
 
 		Bukkit.getPluginManager().callEvent(replacement);
 
 		if (replacement.isCancelled())
 			return;
 
 		if (item.getAmount() - taken > 0) {
 			if(drop.isDead() || !drop.isValid())
 				return;
 
 			Location loc = drop.getLocation();
 			ItemStack copy2 = item.clone();
 			copy2.setAmount(item.getAmount() - taken);
 			drop.remove();
 			Item i = loc.getWorld().dropItem(loc, copy2);
 			i.teleport(loc);
 			i.setVelocity(i.getVelocity().zero());
 			stacker.pickupItems();
 			return;
 		}
 
 		if(drop.isDead() || !drop.isValid())
 			return;
 		else if((item.getAmount() - taken) != rem)
 			return;
 
 		new ItemMover(plugin, drop, p, stacker, taken);
 	}
}
