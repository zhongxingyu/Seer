 package com.comphenix.xp.lookup;
 
 /**
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
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Generic immutable representation of an item/block query.
  * 
  * @author Kristian
  */
 public class ItemQuery implements Query {
 	
 	private Integer itemID;
 	private Integer durability;
 
 	/**
 	 * Universal query.
 	 */
 	public ItemQuery() {
 	}
 	
 	/**
 	 * Creates a query from a given world block.
 	 * @param block - block to create from.
 	 */
 	public ItemQuery(Block block) { 
 		this(block.getTypeId(), (int) block.getData());
 	}
 	
 	/**
 	 * Extracts the item type and durability. Note that the item count property is ignored.
 	 * @param stack - item type.
 	 */
 	public ItemQuery(ItemStack stack) {
 		this(stack.getTypeId(), (int) stack.getDurability());
 	}
 	
 	/**
 	 * Creates a query from the given material.
 	 * @param material material to create from.
 	 */
 	public ItemQuery(Material material) {
		this(material.getId(), 0);
 	}
 	
 	/**
 	 * Creates a query from the given material and data.
 	 * @param material material to create from.
 	 */
 	public ItemQuery(Material material, Integer data) {
 		this(material != null ? material.getId() : null, 
 			 data);
 	}
 	
 	public ItemQuery(Integer itemID, Integer durability) {
 		this.itemID = asPositiveInt(itemID);
 		this.durability = asPositiveInt(durability);
 	}
 
 	public Integer getItemID() {
 		return itemID;
 	}
 
 	public Integer getDurability() {
 		return durability;
 	}
 
 	public boolean hasItemID() {
 		return itemID != null;
 	}
 	
 	public boolean hasDurability() {
 		return durability != null;
 	}
 	
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 31).
 	            append(itemID).
 	            append(durability).
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
             isEquals();
 	}
 
 	@Override
 	public String toString() {
 		Material material = null;
 
 		if (hasItemID())
 			material = Material.getMaterial(itemID);
 		
 		if (hasDurability())
 			return String.format("%s|%d", material == null ? itemID : material, durability);
 		else
 			return String.format("%s", material == null ? itemID : material);
 	}
 	
 	public static ItemQuery fromStack(ItemStack stack) {
 		if (stack == null)
 			return null;
 		else
 			return new ItemQuery(stack);
 	}
 
 	private Integer asPositiveInt(Integer value) {
 		if (value == null)
 			return null;
 		else
 			return value >= 0 ? value : null;
 	}
 	
 	@Override
 	public Types getQueryType() {
 		return Types.Items;
 	}
 }
