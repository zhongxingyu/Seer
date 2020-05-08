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
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class XpContainer extends ItemStack {
 
 	/**
 	 * Maximum storage capacity of a item
 	 */
 	public static int MAX_STORAGE = 32767; // Max of a short
 	/**
 	 * Maximum possible tax
 	 */
 	public static double MAX_TAX = 99;
 
 	/**
 	 * Display name text format
 	 */
 	public static String DISPLAY_NAME_FORMAT = "of"; // remove italic + white
 
 	/**
 	 * Lore text format
 	 */
 	public static String LORE_FORMAT = "r7"; // reset format + gray
 
 	private static int itemId;
 	private static int imbuedItemId;
 	private static int maxExp;
 
 	private static String itemName;
 	private static String itemHint;
 
 	private static double xpTax;
 	private static int stackSize;
 
 	private ItemStack itemStack;
 
 	public XpContainer(ItemStack stack) {
 		super(stack);
 		this.itemStack = stack;
 
 	}
 
 
 	/**
 	 * Return true if the ItemStack has the capability of storing experience points
 	 * @return true if it can store XP, false otherwise
 	 */
 	public static boolean isAnXpContainer(ItemStack stack) {
 		int itemId = 0;
 		boolean container = false;
 
 		if (stack != null) {
 			itemId = stack.getTypeId();
 			container = itemId == getImbuedItemId() || itemId == getItemId();
 		}
 
 		return container;
 	}
 
 	/**
 	 * Return true if the ItemStack contains experience points
 	 * @return true if it contain XP, false otherwise
 	 */
 	public static boolean isAFilledXpContainer(ItemStack stack) {
 		boolean container = false;
 
 		if (stack != null) {
 			container = (stack.getTypeId() == getImbuedItemId() && stack.getDurability() > 0);
 		}
 
 		return container;
 	}
 
 	/**
 	 * Return true if the ItemStack has the capability of containing experience points
 	 * @return true if it can contain XP, false otherwise
 	 */
 	public boolean canContainXp() {
 		return getTypeId() == getImbuedItemId();
 	}
 
 	/**
 	 * Return true if the ItemStack has the capability of storing experience points
 	 * @return true if it can store XP, false otherwise
 	 */
 	public boolean canStoreXp() {
 		return getTypeId() == getItemId();
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.bukkit.inventory.ItemStack#clone()
 	 */
 	@Override
 	public XpContainer clone() {
 		return new XpContainer( new ItemStack(getItemStack()) );
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.bukkit.inventory.ItemStack#getAmount()
 	 */
 	@Override
 	public int getAmount() {
 		return getItemStack().getAmount();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.bukkit.inventory.ItemStack#setAmount(int)
 	 */
 	@Override
 	public void setAmount(int i) {
 		getItemStack().setAmount(i);
 	}
 
 	/**
 	 * Return the amount of experience points stored in the item
 	 * @param item
 	 * @return xp experience points
 	 */
 	public int getStoredXp() {
 
 		return getDurability();
 	}
 
 	/**
 	 * Set the amount of stored experience points in the itemstack to xp (mutable)
 	 * @param xp new stored experience points
 	 * @param item ItemStack getting modified
 	 */
 	public void setStoredXp(int xp) {
 		// check for overflow
 		if (xp > getmaxExp()) {
 			xp = getmaxExp();
 		}
 
 		// Change appearance
 		if (xp == 0) {
 			resetContainer();
 		} else {
 			initContainer(xp);
 		}
 
 		// Change the stored xp
 		setDurability((short) xp);
 	}
 
 	/**
 	 * @return the itemId used as the empty containers
 	 */
 	public static int getItemId() {
 		return itemId;
 	}
 
 
 	/**
 	 * @return the ItemId used as the containers
 	 */
 	public static int getImbuedItemId() {
 		return imbuedItemId;
 	}
 
 
 	/**
 	 * @return the maximum level cap of the containers
 	 */
 	public static int getmaxExp() {
 		return maxExp;
 	}
 
 
 	/**
 	 * @return the stack
 	 */
 	public ItemStack getItemStack() {
 		return itemStack;
 	}
 
 
 	/**
 	 * @return the xpTax
 	 */
 	public static double getXpTax() {
 		return xpTax;
 	}
 
 
 	/**
 	 * @return the stackSize
 	 */
 	@Override
 	public int getMaxStackSize() {
 		int max;
 		if (canContainXp() && getStoredXp() > 0) {
 			//filled gems
 			max = stackSize;
 		} else {
 			//empty gems
 			max = getType().getMaxStackSize();
 		}
 		return max;
 	}
 
 
 	/**
 	 * @return the itemName of the containers
 	 */
 	protected static String getItemName() {
 		return itemName;
 	}
 
 
 	/**
 	 * @return the itemHint
 	 */
 	protected static String getItemHint() {
 		return itemHint;
 	}
 
 
 	/**
 	 * @param imbuedItemId the imbuedItemId to set
 	 */
 	protected static void setImbuedItemId(int imbuedItemId) {
 		XpContainer.imbuedItemId = imbuedItemId;
 	}
 
 
 	/**
 	 * @param itemId the itemId to set
 	 */
 	protected static void setItemId(int itemId) {
 		XpContainer.itemId = itemId;
 	}
 
 
 	/**
 	 * @param maxExp the maximum experience points cap  to set
 	 */
 	protected static boolean setMaxExp(int maxExp) {
 		boolean result = false;
 		// check if maxLevel fits in a short (2^15 - 1)
 		if (maxExp > MAX_STORAGE) {
 			XpContainer.maxExp = MAX_STORAGE;
 		} else {
 			XpContainer.maxExp = maxExp;
 			result = true;
 		}
 		return result;
 	}
 
 
 	/**
 	 * @param itemName the itemName to set
 	 */
 	protected static void setItemName(String itemName) {
 		XpContainer.itemName = itemName;
 	}
 
 	/**
 	 * @param itemHint the itemHint to set
 	 */
 	protected static void setItemHint(String itemHint) {
 		XpContainer.itemHint = itemHint;
 	}
 
 	/**
 	 * @param xpTax the xpTax to set
 	 */
 	protected static void setXpTax(double xpTax) {
 		XpContainer.xpTax = xpTax;
 	}
 
 
 	/**
 	 * @param maxStackSize the maxStackSize to set
 	 */
 	protected static void setMaxStackSize(int maxStackSize) {
 		XpContainer.stackSize = maxStackSize;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		boolean result = false;
 
 		if (this == obj)
 			return true;
 		if (getClass() != obj.getClass())
 			return false;
 
 		XpContainer other = (XpContainer) obj;
 
 		if (other.canContainXp() == this.canContainXp()
 				&& other.canStoreXp() == this.canStoreXp()
 				&& other.getStoredXp() == this.getStoredXp()) {
 			result = true;
 		}
 
 		return result;
 	}
 
 
 	/**
 	 * Find first not full stack with the same property. Return null if nothing
 	 * found.
 	 *
 	 * @param stack {@link XpContainer} with the property looking for
 	 * @param inv inventory
 	 * @return ItemStack found
 	 */
 	public XpContainer findSimilarStack(Inventory inv) {
 		return findSimilarStack(inv, 0, inv.getSize());
 	}
 
 
 	/**
 	 * Find the first not full stack of XpContainer with the same property starting at start
 	 * and ending at the index stop.
 	 *
 	 * @param stack {@link XpContainer} with the property looking for
 	 * @param inv inventory
 	 * @param start the index to start the search
 	 * @param stop
 	 * @return the XpContainer found or null if nothing found
 	 */
 	public XpContainer findSimilarStack(Inventory inv, int start, int stop) {
 		ItemStack[] items = inv.getContents();
 		XpContainer gem = null;
 		boolean found = false;
 
 		if (stop > items.length) stop = items.length;
 
 		for (int i = start; i < stop && !found; i++) {
 			if (items[i] != null) {
 				gem = new XpContainer(items[i]);
 
 				if (gem.getAmount() < gem.getMaxStackSize() && gem.equals(this)) {
 					found = true;
 				}
 			}
 		}
 
 		return found ? gem : null;
 	}
 
 	/**
 	 * Prepare the container by changing is typeid, display name and adding a
 	 * new lore indicating the stored experience points
 	 *
 	 * @param xp experience points stored in the container
 	 */
 	private void initContainer(int xp) {
 		ItemMeta itemMeta = this.getItemMeta();
 		List<String> lores = itemMeta.getLore();
 		String lore = LORE_FORMAT + getItemHint() + " " + xp + "xp";
 
 		// Change appearance and display name
 		setTypeId(getImbuedItemId());
 		itemMeta.setDisplayName(DISPLAY_NAME_FORMAT + getItemName());
 
 		// Add description
 		if (lores != null) {
 			lores.add(lore);
 		} else {
 			// If the list does'nt exist we create it
 			lores = new LinkedList<String>();
 			lores.add(lore);
 		}
 
		itemMeta.setLore(lores);
 		this.setItemMeta(itemMeta);
 	}
 
 	/**
 	 * Reset the appearance of the container
 	 */
 	private void resetContainer() {
 		ItemMeta itemMeta = this.getItemMeta();
 
 		// Change appearance
 		setTypeId(getItemId());
 
 		// Reset hints
 		itemMeta.setDisplayName(null);
 		itemMeta.setLore(null);
 
 		this.setItemMeta(itemMeta);
 	}
 
 }
