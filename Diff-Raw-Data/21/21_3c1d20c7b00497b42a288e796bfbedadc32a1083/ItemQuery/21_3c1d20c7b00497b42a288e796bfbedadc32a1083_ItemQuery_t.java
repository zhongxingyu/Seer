 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp.lookup;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.NullArgumentException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 
 import com.comphenix.xp.parser.Utility;
 import com.google.common.collect.Lists;
 
 /**
  * Generic immutable representation of an item/block query.
  * 
  * @author Kristian
  */
 public class ItemQuery implements Query {
 
 	private List<Integer> itemID;
 	private List<Integer> durability;
 	private List<Boolean> playerCreated;
 
 	/**
 	 * Universal query. Matches everything.
 	 * @return The universal query.
 	 */
 	public static ItemQuery fromAny() {
 		return fromAny(null, null, null);
 	}	
 	
 	/**
 	 * Creates a query from the given material and data.
 	 * @param material - material to create from.
 	 * @return The created query.
 	 */
 	public static ItemQuery fromAny(Material material) {
 		return fromAny(material != null ? material.getId() : null, null);
 	}
 	
 	/**
 	 * Creates a query from the given material and data.
 	 * @param material - material to create from.
 	 * @param durability - durability to create from.
 	 * @return The created query.
 	 */
 	public static ItemQuery fromAny(Material material, Integer durability) {
 		return fromAny(material != null ? material.getId() : null, durability);
 	}
 	
 	/**
 	 * Creates a query from the given material and data, where NULL represents any value.
 	 * @param itemID - ID to create from, or NULL to indicate every ID.
 	 * @param durability - durability to create from, or NULL to indicate every durability.
 	 * @return The created query.
 	 */
 	public static ItemQuery fromAny(Integer itemID, Integer durability) {
 		return fromAny(itemID, durability, null);
 	}
 	
 	/**
 	 * Creates a query from the given material and data, where NULL represents any value.
 	 * @param itemID - ID to create from, or NULL to indicate every ID.
 	 * @param durability - durability to create from, or NULL to indicate every durability.
 	 * @param playerCreated - whether or not the block was created/placed by a player.
 	 * @return The created query.
 	 */
 	public static ItemQuery fromAny(Integer itemID, Integer durability, Boolean playerCreated) {
 		return new ItemQuery(
 				Utility.getElementList(itemID), 
 				Utility.getElementList(durability),
 				Utility.getElementList(playerCreated)
 		);
 	}
 	
 	/**
 	 * Extracts the item type and durability. Note that the item count property is ignored.
 	 * @param stack - item type.
 	 * @return The created query.
 	 * @throws NullArgumentException if the stack is null.
 	 */
 	public static ItemQuery fromAny(ItemStack stack) {
 		
 		if (stack == null)
 			throw new NullArgumentException("stack");
 		
 		return fromAny(stack.getTypeId(), (int) stack.getDurability(), null);
 	}
 	
 	/**
 	 * Creates a query from a given world block.
 	 * @param block - block to create from.
 	 * @return The created query.
 	 */
 	public static ItemQuery fromAny(Block block) {
 		return fromAny(block.getTypeId(), (int) block.getData(), null);
 	}
 	
 	/**
 	 * Creates a query from a given world block.
 	 * @param block - block to create from.
 	 * @return The created query.
 	 */
 	public static ItemQuery fromExact(Block block) {
 		return fromExact(block.getTypeId(), (int) block.getData());
 	}
 	
 	/**
 	 * Extracts the item type and durability. Note that the item count property is ignored.
 	 * @param stack - item type.
 	 * @return The created query.
 	 * @throws NullArgumentException if the stack is null.
 	 */
 	public static ItemQuery fromExact(ItemStack stack) {
 		
 		if (stack == null)
 			throw new NullArgumentException("stack");
 		
 		return fromExact(stack.getTypeId(), (int) stack.getDurability());
 	}
 	
 	/**
 	 * Creates a query from the given ID and durability. NULL is used to ONLY match universal queries.
 	 * @param itemID - ID to match, or NULL to match queries without IDs.
 	 * @param durability - durability to match, or NULL to match queries without durabilities.
 	 * @return The created query.
 	 */
 	public static ItemQuery fromExact(Integer itemID, Integer durability) {
 		return fromExact(itemID, durability, null);
 	}
 	
 	/**
 	 * Creates a query from the given ID and durability. NULL is used to ONLY match universal queries.
 	 * @param itemID - ID to match, or NULL to match queries without IDs.
 	 * @param durability - durability to match, or NULL to match queries without durabilities.
 	 * @return The created query.
 	 */
 	public static ItemQuery fromExact(Integer itemID, Integer durability, Boolean playerCreated) {
 		return new ItemQuery(
 				Lists.newArrayList(itemID), 
 				Lists.newArrayList(durability),
 				Lists.newArrayList(playerCreated)
 		);
 	}
 	
 	/**
 	 * Constructs a query with the given IDs and durabilities.
 	 * @param itemID - list of IDs.
 	 * @param durability - list of durabilities.
 	 */
 	public ItemQuery(List<Integer> itemID, List<Integer> durability) {
 		this(itemID, durability, Utility.getElementList((Boolean) null));
 	}
 
 	
 	/**
 	 * Constructs a query with the given IDs and durabilities.
 	 * @param itemID - list of IDs.
 	 * @param durability - list of durabilities.
 	 * @param playerCreated - option specifying whether or not the block was placed by a player.
 	 */
 	public ItemQuery(List<Integer> itemID, List<Integer> durability, List<Boolean> playerCreated) {
 		this.itemID = itemID;
 		this.durability = durability;
 		this.playerCreated = playerCreated;
 	}
 
 	public List<Integer> getItemID() {
 		return itemID;
 	}
 
 	public List<Integer> getDurability() {
 		return durability;
 	}
 	
 	public List<Boolean> getPlayerCreated() {
 		return playerCreated;
 	}
 
 	public boolean hasItemID() {
 		return itemID != null && !itemID.isEmpty();
 	}
 	
 	public boolean hasDurability() {
 		return durability != null && !durability.isEmpty();
 	}
 	
 	public boolean hasPlayerCreated() {
 		return playerCreated != null && !playerCreated.isEmpty();
 	}
 	
 	@Override
 	public boolean match(Query other) {
 
 		if (other instanceof ItemQuery) {
 			ItemQuery query = (ItemQuery) other;
 			
 			// Make sure the current query is the superset of the given
 			return QueryMatching.matchParameter(itemID, query.itemID) &&
 				   QueryMatching.matchParameter(durability, query.durability) &&
 				   QueryMatching.matchParameter(playerCreated, query.playerCreated);
 		}
 		
 		// Query must be of the same type
 		return false;
 	}
 	
 	/**
 	 * Determines if the current query matches the given item. 
 	 * @param id - id of item, or NULL for every ID.
 	 * @param durability - durability of item, or NULL for every durability.
 	 * @return TRUE if the current query matches the given item.
 	 */
 	public boolean match(Integer id, Integer durability, Boolean playerCreated) {
 		return match(ItemQuery.fromAny(id, durability, playerCreated));
 	}
 	
 	/**
 	 * Determines if the current query matches the given item. 
 	 * @param id - id of item, or NULL for every ID.
 	 * @param durability - durability of item, or NULL for every durability.
 	 * @return TRUE if the current query matches the given item.
 	 */
 	public boolean match(Integer id, Integer durability) {
 		return match(ItemQuery.fromAny(id, durability));
 	}
 	
 	/**
 	 * Determines if the current query matches the given item.
 	 * @param item - item to test, or NULL for every possible item.
 	 * @return TRUE if the current query matches the given item.
 	 */
 	public boolean match(Material item) {
 		if (item == null)
 			return true;
 		else
 			return match(ItemQuery.fromAny(item.getId(), null));
 	}
 	
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 31).
 	            append(itemID).
 	            append(durability).
 	            append(playerCreated).
 	            toHashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
             return false;
         if (obj == this)
             return true;
         if (obj.getClass() != getClass())
             return false;
 
         ItemQuery other = (ItemQuery) obj;
         return new EqualsBuilder().
             append(itemID, other.itemID).
             append(durability, other.durability).
             append(playerCreated, other.playerCreated).
             isEquals();
 	}
 	
 	// We're only interested in the String representation
 	private List<Object> getMaterials() {
 		
 		List<Object> materials = new ArrayList<Object>();
 	
 		if (itemID == null)
 			return materials;
 		
 		// Map each integer to its corresponding material
 		for (Integer id : itemID) {
 			if (id != null) {
 			
 				Material material = Material.getMaterial(id);
 				
 				if (material != null) 
 					materials.add(material);
 				else
 					materials.add(id);
 			}
 		}
 	
 		return materials;
 	}
 
 	/**
 	 * Converts the given query to an item stack if possible.
 	 * @return The converted item stack, or NULL if not possible.
 	 */
 	public ItemStack toItemStack(int amount) {
 
 		int data = 0;
 		
 		if (containsSingleNonNull(itemID)) {
 			
 			// We'll treat no durability as zero
 			if (containsSingleNonNull(durability)) {
 				data = durability.get(0);
 			} else if (durability.isEmpty()) {
 				data = 0;
 			} else {
 				return null;
 			}
 			
 			// Create the item stack
 			return new ItemStack(itemID.get(0), amount, (short) data);
 		}
 		
 		return null;
 	}
 	
 	private boolean containsSingleNonNull(List<Integer> list) {
 		return list != null && list.size() == 1 && !list.contains(null);
 	}
 	
 	@Override
 	public String toString() {
 		
 		String itemsText = StringUtils.join(getMaterials(), ", ");
 		String durabilityText = StringUtils.join(durability, ", ");
 		String playerText = Utility.formatBoolean("player", playerCreated);
 		
 		if (hasPlayerCreated())
 			return String.format("%s|%s|%s", itemsText, durabilityText, playerText);
 		else if (hasDurability())
 			return String.format("%s|%s", itemsText, durabilityText);
 		else
 			return String.format("%s", itemsText);
 	}
 		
 	@Override
 	public Types getQueryType() {
 		return Types.Items;
 	}
 	
 	/**
 	 * Determines if the given item stack is non-empty.
 	 * @param stack - item stack to test.
 	 * @return TRUE if it is non-null and non-empty, FALSE otherwise.
 	 */
 	public static boolean hasItems(ItemStack stack) {
 		return stack != null && stack.getAmount() > 0;
 	}
 }
