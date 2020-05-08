 /**
 * Rewrite of the original PearlXP created by Nebual of nebtown.info in March 2012.
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
 package info.nebtown.PearlXP;
 
 import info.nebtown.PearlXP.PearlXP.MsgKeys;
 
 import java.util.ListIterator;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.event.block.Action;
 
 public class PearlXPListener implements Listener {
 
 	private static final ChatColor TEXT_COLOR = ChatColor.BLUE;
 	private static final ChatColor INFO_COLOR = ChatColor.AQUA;
 	private static final ChatColor ERR_COLOR = ChatColor.DARK_RED;
 
 	private static Enchantment enchantment = Enchantment.OXYGEN;
 
 	private PearlXP plugin;
 	private String itemName;
 
 	private String invFullMsg;
 	private String infoXpMsg;
 	private String infoXpEmptyMsg;
 	private String imbueXpMsg;
 	private String restoreXpMsg;
 
 	public PearlXPListener(PearlXP plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 
 		this.plugin = plugin;
 		this.itemName = plugin.getItemName();
 
 		this.invFullMsg = plugin.getMessage(MsgKeys.INVENTORY_FULL);
 		this.infoXpMsg = plugin.getMessage(MsgKeys.INFO_XP);
 		this.infoXpEmptyMsg = plugin.getMessage(MsgKeys.INFO_XP_EMPTY);
 		this.imbueXpMsg = plugin.getMessage(MsgKeys.IMBUE_XP);
 		this.restoreXpMsg = plugin.getMessage(MsgKeys.RESTORE_XP);
 
 	}
 
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		ItemStack item;
 		Action action = event.getAction();
 
 		int xp = 0;
 
 		Player player = event.getPlayer();
 		PlayerInventory inventory = player.getInventory();
 
 		if (event.hasItem()) {
 
 			item = event.getItem();
 
 			if (canStoreXp(item) && getStoredXp(item) == 0) { // The item possess no XP
 
 				if (action == Action.RIGHT_CLICK_BLOCK) {
 					// Show the amount of XP stored
 
 					event.setUseItemInHand(Result.DENY); //Don't throw the item!
 
 					// the item is empty and the player clicked "on is feet"
 					sendInfo(infoXpEmptyMsg, INFO_COLOR, player, item);
 
 				} else if (player.getTotalExperience() > 0 
 						&& (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
 					// Store some XP in the item
 
 					if (player.getTotalExperience() > plugin.getMaxLevel()) {
 						xp = plugin.getMaxLevel();
 					} else {
 						xp = player.getTotalExperience();
 					}
 
 					try {
 
 						item = storeXp(xp, item, inventory);
 						removePlayerXp(xp, player);
 
 						// Friendly message !
 						sendInfo(imbueXpMsg, player, item);
 
 						// Visual and sound effects
 						player.getWorld().playEffect(player.getEyeLocation(), Effect.ENDER_SIGNAL, 0);
 						player.playEffect(player.getEyeLocation(), Effect.EXTINGUISH, 0);
 					} catch (InventoryFullException e) {
 						sendError(invFullMsg, player, item);
 					}
 				}
 
 			} else if (canContainXp(item)) {
 
 				if (getStoredXp(item) > 0 && action == Action.RIGHT_CLICK_AIR 
 						|| action == Action.RIGHT_CLICK_BLOCK) {
 					// Show the imbued XP...
 
 					event.setUseItemInHand(Result.DENY); //Don't throw the item!
 
 					sendInfo(infoXpMsg, INFO_COLOR, player, item);
 
 
 				} else if (getStoredXp(item) > 0 
 						&& (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
 
 					try {
 						xp = getStoredXp(item);
 
 						// Remove all Stored XP
 						storeXp(0, item, inventory);
 
 						// give the player the XP
 						player.giveExp(xp);
 						sendInfo(restoreXpMsg, player, item);
 
 					} catch (InventoryFullException e) {
 						sendError(invFullMsg, player, item);
 					}
 				}
 			}
 
 		}
 
 	} //onPlayerInteract
 
 	/**
 	 * Return true if the ItemStack has the capability of containing experience points
 	 * @param stack
 	 * @return true if it can contain XP, false otherwise
 	 */
 	private boolean canContainXp(ItemStack stack) {
 		return stack.getTypeId() == plugin.getImbuedItem();
 	}
 
 	/**
 	 * Return true if the ItemStack has the capability of storing experience points
 	 * @param stack
 	 * @return true if it can store XP, false otherwise
 	 */
 	private boolean canStoreXp(ItemStack stack) {
 		return stack.getTypeId() == plugin.getItemId();
 	}
 
 	/**
 	 * Format the message to add variables values
 	 * @param msg message
 	 * @param xp item xp
 	 * @param playerXp player total xp
 	 * @return the modified string
 	 */
 	private String formatMsg(String msg, int xp, int playerXp) {
 		String[] values = { itemName,
 				String.valueOf(xp),
 				String.valueOf(playerXp) };
 
 		return formatMsg(msg, values);
 	}
 
 	/**
 	 * Format the message to add variables values
 	 * @param msg message
 	 * @param values Array of the values to display
 	 * @return the modified string
 	 */
 	private String formatMsg(String msg, String[] values) {
 		String[] keys = { "item_name", "xp", "player_xp" };
 
 		if (msg != null) {
 			for (int i = 0; i < keys.length && i < values.length; i++) {
 				msg = msg.replaceAll("\\$\\{" + keys[i] + "\\}", values[i]);
 			}
 		}
 
 		return msg;
 	}
 
 
 
 	/**
 	 * Send an error message to the player
 	 * @param msg message
 	 * @param p player
 	 */
 	private void sendError(String msg, Player p, ItemStack i ) {
 		sendInfo(msg, ERR_COLOR, p, i);
 		p.playEffect(p.getLocation(), Effect.ZOMBIE_CHEW_IRON_DOOR, 0);
 	}
 
 	/**
 	 * Send the player a information text with the default text color.
 	 * @param s message
 	 * @param p player to inform
 	 */
 	private void sendInfo(String msg, Player p, ItemStack i) {
 		if (msg != null) {
 			sendInfo(msg, TEXT_COLOR, p, i);
 		}
 	}
 
 	/**
 	 * Send the player a information text with the default text color.
 	 * @param s message
 	 * @param p player to inform
 	 */
 	private void sendInfo(String msg, ChatColor c, Player p, ItemStack i) {
 		if (msg != null) {
 			p.sendMessage(c + formatMsg(msg, getStoredXp(i), p.getTotalExperience()));
 		}
 	}
 
 	/**
 	 * Remove a number of XP from a given player
 	 * @param xp the XP to remove
 	 * @param p player
 	 */
 	private void removePlayerXp(int xp, Player p) {
 		int currentXp = p.getTotalExperience();
 
 		// Reset level to fix update bug
 		p.setTotalExperience(0);
 		p.setExp(0);
 		p.setLevel(0);
 
 		p.giveExp(currentXp - xp);
 
 	}
 
 	/**
 	 * Find biggest not full stack with the same property. Return null if nothing
 	 * found.
 	 * @param stack ItemStack with the property looking for
 	 * @param inv inventory
 	 * @return ItemStack found
 	 */
 	private ItemStack findSimilarStack(ItemStack stack, PlayerInventory inv) {
 		ListIterator<ItemStack> items = inv.iterator();
 		ItemStack item = items.next();
 		boolean found = false;
 
 		// property searched
 		int enchantLvl = getStoredXp(stack);
 		int typeId = stack.getTypeId();
 
 
 		while (items.hasNext() && !found) {
 
 			if (item != null && item.getAmount() < item.getMaxStackSize() 
 					&& getStoredXp(item) == enchantLvl
 					&& item.getTypeId() == typeId) {
 
 				found = true;
 			} else {
 				item = items.next();
 			}
 		}
 
 		return found ? item : null;
 	}
 
 	/**
 	 * Store the given amount of XP in the item. If other uncompleted stack
 	 * exists with the correct XP the method stack them together.
 	 * 
 	 * @param item ItemStack to store XP
 	 * @param xp experience points
 	 * @param inv inventory of the player
 	 */
 	private ItemStack storeXp(int xp, ItemStack item,  PlayerInventory inv) {
 		ItemStack similarStack;
 		ItemStack newItem;
 		int slot = inv.firstEmpty();
 
 		newItem = item.clone();
 		setStoredXp(xp, newItem);
 		similarStack = findSimilarStack(newItem, inv);
 
 		if (item.getAmount() == 1 && similarStack == null && slot < 0) {
 
 			setStoredXp(xp, item);
 			newItem = item;
 
 		} else { // We can unstack stuff!
 
 			if (similarStack != null) {
 				// no place to stack on top
 
 				similarStack.setAmount(similarStack.getAmount() + 1);
 
 			} else {
 
 				if (slot >= 0) {
 					// Only create one item...
 					newItem.setAmount(1);
 
 					inv.setItem(slot, newItem);
 
 				} else {
 					// The item is in a stack and cannot be unstack
 					throw new InventoryFullException();
 				}
 			}
 
 			// Remove the item used
 			if (item.getAmount() == 1) {
 				inv.remove(item);
 			} else {
 				item.setAmount(item.getAmount() - 1);
 			}
 		}
 
 		return newItem;
 
 	}
 
 	/**
 	 * Return the amount of experience points stored in the item
 	 * @param item
 	 * @return xp experience points
 	 */
 	private int getStoredXp(ItemStack item) {
 		int xp = 0;
 
 		if (item.containsEnchantment(enchantment))
 			xp = item.getEnchantmentLevel(enchantment);
 
 		return xp;
 	}
 
 	/**
 	 * Set the amount of stored experience points in the item i to xp
 	 * @param xp new stored experience points
 	 * @param item
 	 */
 	private void setStoredXp(int xp, ItemStack item) {
 		// check for overflow
 		if (xp > plugin.getMaxLevel()) {
 			xp = plugin.getMaxLevel();
 		}
 
 		if (xp == 0) {
 			item.removeEnchantment(enchantment);
 			item.setTypeId(plugin.getItemId()); // Change appearance
 		} else {
 			item.addUnsafeEnchantment(enchantment, xp);
 			item.setTypeId(plugin.getImbuedItem()); // Change appearance
 		}
 	}
 
 } //class
