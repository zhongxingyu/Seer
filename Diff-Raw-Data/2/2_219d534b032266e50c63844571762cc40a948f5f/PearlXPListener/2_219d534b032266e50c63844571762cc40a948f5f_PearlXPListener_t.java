 package info.nebtown.PearlXP;
 
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
 
 	public static String ERR_MSG_FULL_INV = "Your inventory is full!";
 
 	private static final ChatColor TEXT_COLOR = ChatColor.BLUE;
 	private static final ChatColor INFO_COLOR = ChatColor.AQUA;
 	private static final ChatColor ERR_COLOR = ChatColor.DARK_RED;
 
 	private static String itemName = "pearl";
 
 	private static Enchantment enchantment = Enchantment.OXYGEN;
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		ItemStack item;
 		Action action = event.getAction();
 
 		int xp = 0;
 		String storeMsg = "Imbued this " + itemName + " with ";
 
 		Player player = event.getPlayer();
 		PlayerInventory inventory = player.getInventory();
 
 		if ( event.hasItem() && canContainXp(event.getItem()) ) {
 
 			item = event.getItem();
 
 			if (getStoredXp(item) == 0) { // The item possess no XP
 
 				if (action == Action.RIGHT_CLICK_BLOCK) {
 					// Show the amount of XP stored
 
 					event.setUseItemInHand(Result.DENY); //Don't throw the item!
 
 					// the item is empty and the player clicked "on is feet"
 					sendInfo("This " + itemName + " is empty.", INFO_COLOR, player);
 
 				} else if (player.getTotalExperience() > 0 
 						&& (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
 					// Store some XP in the item
 
 					if (player.getTotalExperience() > PearlXP.getMaxLevel()) {
 
 						xp = PearlXP.getMaxLevel();
						storeMsg +=  xp + " XP! " + (player.getTotalExperience() - xp) + " XP left!";
 					} else {
 
 						xp = player.getTotalExperience();
 						storeMsg += xp + " XP!";
 					}
 
 					try {
 
 						storeXp(xp, item, inventory);
 						removePlayerXp(xp, player);
 
 						// Friendly message !
 						sendInfo(storeMsg, player);
 
 						// Visual and sound effects
 						player.getWorld().playEffect(player.getEyeLocation(), Effect.ENDER_SIGNAL, 0);
 						player.playEffect(player.getEyeLocation(), Effect.EXTINGUISH, 0);
 					} catch (InventoryFullException e) {
 						sendError(ERR_MSG_FULL_INV, player);
 					}
 				}
 
 			} else { // Contains XP
 
 				if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
 					// Show the imbued XP...
 
 					event.setUseItemInHand(Result.DENY); //Don't throw the item!
 
 					sendInfo("This " + itemName + " is imbued with "
 							+ getStoredXp(item) + " XP!", INFO_COLOR, player);
 
 
 				} else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
 
 					try {
 						xp = getStoredXp(item);
 						
 						// Remove all Stored XP
 						storeXp(0, item, inventory);
 						
 						// give the player the XP
 						player.giveExp(xp);
 						sendInfo("Restoring " + xp + " XP! You now have " 
 								+ player.getTotalExperience() + " XP!", player);
 
 					} catch (InventoryFullException e) {
 						sendError(ERR_MSG_FULL_INV, player);
 					}
 				}
 			}
 
 		}
 
 	} //onPlayerInteract
 
 	/**
 	 * Return true if the ItemStack has the capability of storing experience points
 	 * @param stack
 	 * @return true if it can contain XP, false otherwise
 	 */
 	public boolean canContainXp(ItemStack stack) {
 
 		return stack.getTypeId() == PearlXP.getItemId();
 	}
 
 	/**
 	 * Send an error message to the player
 	 * @param msg message
 	 * @param p player
 	 */
 	private void sendError(String msg, Player p ) {
 		sendInfo(msg, ERR_COLOR, p);
 		p.playEffect(p.getLocation(), Effect.ZOMBIE_CHEW_IRON_DOOR, 0);
 	}
 
 	/**
 	 * Send the player a information text with the default text color.
 	 * @param s message
 	 * @param p player to inform
 	 */
 	private void sendInfo(String msg, Player p) {
 		sendInfo(msg, TEXT_COLOR, p);
 	}
 
 	private void sendInfo(String msg, ChatColor c, Player p) {
 		p.sendMessage(c + msg);
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
 		int enchantLvl = stack.getEnchantmentLevel(enchantment);
 		int typeId = stack.getTypeId();
 
 
 		while (items.hasNext() && !found) {
 
 			if (item != null && item.getAmount() < item.getMaxStackSize() && item.getEnchantmentLevel(enchantment) == enchantLvl
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
 	private void storeXp(int xp, ItemStack item,  PlayerInventory inv) {
 		ItemStack similarStack;
 		ItemStack newItem;
 		int slot = inv.firstEmpty();
 
 		newItem = item.clone();
 		setStoredXp(xp, newItem);
 		similarStack = findSimilarStack(newItem, inv);
 		
 		if (item.getAmount() == 1 && similarStack == null && slot < 0) {
 			
 			setStoredXp(xp, item);
 			
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
 		if (xp > PearlXP.getMaxLevel()) {
 			xp = PearlXP.getMaxLevel();
 		}
 
 		if (xp == 0) {
 			item.removeEnchantment(enchantment);
 		} else {
 			item.addUnsafeEnchantment(enchantment, xp);
 		}
 	}
 
 } //class
