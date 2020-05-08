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
 	import java.util.Map;
 //* IMPORTS: BUKKIT
 	import org.bukkit.enchantments.Enchantment;
 	import org.bukkit.entity.Player;
 	import org.bukkit.inventory.ItemStack;
 	import org.bukkit.inventory.meta.EnchantmentStorageMeta;
 	import org.bukkit.Material;
 	import org.bukkit.plugin.Plugin;
 //* IMPORTS: OTHER
 	import com.sk89q.worldedit.blocks.ItemType;
 
 public class StackInventory {
 	private boolean valid = false;
 	private boolean allowPotions, ignoreDamaged, ignoreMax, sendMessage;
 	private int affected, i, index, length, max, needed;
 	private ItemStack item, item2, items[];
 	private Player player;
 	private Plugin plugin;
 
 	public StackInventory(Plugin p, Player player, boolean sendMessage) {
 		if (p == null || player == null)
 			return;
 
 		this.player = player;
 		this.plugin = p;
 		this.sendMessage = sendMessage;
 
 		String perm;
 
 		if ((Stacked.config == null) || !Stacked.config.illegitimate)
 			this.ignoreMax = false;
 		else {
 			perm = "stacked.stack.illegitimate";
			this.ignoreMax = player.hasPermission(perm);
 		}
 
 		if ((Stacked.config == null) || !Stacked.config.damaged)
 			this.ignoreDamaged = false;
 		else {
 			perm = "stacked.stack.damaged";
 			this.ignoreDamaged = player.hasPermission(perm);
 		}
 
 		if ((Stacked.config == null) || !Stacked.config.potions)
 			this.allowPotions = false;
 		else {
 			perm = "stacked.stack.potions";
 			this.allowPotions = player.hasPermission(perm);
 		}
 
 		valid = true;
 	}
 
 	private boolean compareBooks(ItemStack book1, ItemStack book2) {
 		if (book1.getType() != Material.ENCHANTED_BOOK)
 			return true;
 		if (book2.getType() != Material.ENCHANTED_BOOK)
 			return true;
 
 		EnchantmentStorageMeta meta1, meta2;
 
 		meta1 = (EnchantmentStorageMeta) book1.getItemMeta();
 		meta2 = (EnchantmentStorageMeta) book2.getItemMeta();
 
 		Map<Enchantment, Integer> map1 = meta1.getStoredEnchants();
 		Map<Enchantment, Integer> map2 = meta2.getStoredEnchants();
 
 		if (map1.equals(map2))
 			return true;
 
 		return false;
 	}
 
 	private void innerLoop() {
 		item2 = items[i];
 
 		// Avoid infinite stacks and stacks with durability
 		if (isWrongSize(item))
 			return;
 
 		// Avoid stacking different Enchanted Books together
 		if (item.getType() == Material.ENCHANTED_BOOK) {
 			if (!compareBooks(item, item2))
 				return;
 		}
 
 		// Same type?
 		if (item2.getTypeId() != item.getTypeId())
 			return;
 
 		// Blocks store their color in the damage value
 		if (ItemType.usesDamageValue(item.getTypeId()) || !ignoreDamaged)
 			if (item.getDurability() != item2.getDurability())
 				return;
 
 		// Ensure they have the same enchantments
 		if (!item.getEnchantments().equals(item2.getEnchantments()))
 			return;
 
 		// This stack won't fit in the parent stack
 		if (item2.getAmount() > needed) {
 			item.setAmount(max);
 			item2.setAmount(item2.getAmount() - needed);
 			needed = 0;
 			return;
 		// This stack will
 		} else {
 			items[i] = null;
 			item.setAmount(item.getAmount() + item2.getAmount());
 			needed = max - item.getAmount();
 		}
 
 		affected++;
 	}
 
 	private boolean isWrongSize(ItemStack item) {
 		if (item == null)
 			return true;
 		else if (item.getAmount() <= 0)
 			return true;
 		else if (!ignoreMax && item.getMaxStackSize() == 1)
 			return true;
 
 		return false;
 	}
 
 	private void outerLoop() {
 		item = items[index];
 
 		// Avoid infinite stacks
 		if (isWrongSize(item))
 			return;
 
 		max = ignoreMax ? 64 : item.getMaxStackSize();
 		// Need to add config for max size
 		//max = ignoreMax ? -1 : item.getMaxStackSize();
 		//max = (max == -1) ? ((data == null) ? 64 : data.max) : max;
 
 		if (item.getAmount() >= max)
 			return;
 
 		// Avoid stacking potions
 		if (item.getType() == Material.POTION && !allowPotions)
 			return;
 
 		process();
 	}
 
 	private void process() {
 		// Number of needed items until max
 		needed = max - item.getAmount(); 
 
 		// Find another stack of the same type
 		for (i = index + 1; i < length; i++) {
 			innerLoop();
 
 			if (needed <= 0)
 				break;
 		}
 	}
 
 	public boolean stack() {
 		if (!valid)
 			return false;
 
 		if (!player.hasPermission("stacked.stack") && sendMessage) {
 			String msg = "You don't have permission to do that!";
 			return Message.severe(plugin, player, msg);
 		}
 
 		items = player.getInventory().getContents();
 		length = items.length;
 		affected = 0;
 
 		for (index = 0; index < length; index++) {
 			outerLoop();
 		}
 
 		if (affected > 0) {
 			player.getInventory().setContents(items);
 		}
 
 		if (sendMessage) {
 			String msg = "Items compacted into stacks!";
 			return Message.info(plugin, player, msg);
 		}
 
 		return true;
 	}
 }
