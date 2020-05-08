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
 	import java.lang.Runnable;
 //* IMPORTS: BUKKIT
 	import org.bukkit.Bukkit;
 	import org.bukkit.entity.Item;
 	import org.bukkit.entity.Player;
 	import org.bukkit.inventory.ItemFactory;
 	import org.bukkit.inventory.meta.ItemMeta;
 	import org.bukkit.Location;
 	import org.bukkit.Material;
 	import org.bukkit.plugin.Plugin;
 	import org.bukkit.scheduler.BukkitScheduler;
 	import org.bukkit.util.Vector;
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class ItemMover implements Runnable {
 	private int taskId = -1;
 	private Item item;
 	private Player player;
 	private Plugin plugin;
 	private StackInventory stacker;
 	private int ticksRemaining = 3;
 	private int amount = 0;
 
 	public ItemMover(
 		Plugin plugin,
 		Item item,
 		Player player,
 		StackInventory stacker,
 		int amount)
 	{
 		if ((plugin == null) || (item == null))
 			return;
 		else if ((player == null) || (stacker == null))
 			return;
 
 		this.item	= item;
 		this.player	= player;
 		this.plugin	= plugin;
 		this.stacker	= stacker;
 		this.amount	= amount;
 
 		Material mat = item.getItemStack().getType();
 		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(mat);
 		meta.setDisplayName("MovingItem");
 		item.getItemStack().setItemMeta(meta);
 
 		start();
 	}
 
 	private void pickup() {
 		if (item.isDead() || !item.isValid())
 			return;
 
 		if (item.getItemStack().getAmount() <= amount) {
 			item.getItemStack().setAmount(0);
 			item.remove();
 		}
 		else {
 			int remaining = item.getItemStack().getAmount();
 			remaining -= amount;
 			item.getItemStack().setAmount(remaining);
 			item.getItemStack().setItemMeta(null);
 		}
 
 		stacker.pickupItems();
 	}
 
 	private void softStop() {
 		stop();
 
 		if(ticksRemaining >= 0)
 			start();
 	}
 
 	public void start() {
 		if (taskId >= 0)
 			return;
 
 		item.setPickupDelay(40);
 		BukkitScheduler scheduler = plugin.getServer().getScheduler();
 		taskId = scheduler.scheduleSyncDelayedTask(plugin, this, 1);
 	}
 
 	private void stop() {
 		if (taskId < 0)
 			return;
 
 		plugin.getServer().getScheduler().cancelTask(taskId);
 		taskId = -1;
 	}
 
 	public void run() {
 		if (item.isDead() || !item.isValid()) {
 			stop();
 			return;
 		}
 
 		Location p = player.getLocation();
 		Location i = item.getLocation();
 
 		item.getVelocity().zero();
		double x = ((p.getX() - i.getX()) / ((double) ticksRemaining));
		double py = p.getY() + 1.22D;
		double y = ((py - i.getY()) / ((double) ticksRemaining));
		double z = ((p.getZ() - i.getZ()) / ((double) ticksRemaining));
 
 		item.setVelocity(new Vector(x, y, z));
 
 		if (ticksRemaining <= 0)
 			pickup();
 
 		ticksRemaining--;
 		softStop();
 	}
}
