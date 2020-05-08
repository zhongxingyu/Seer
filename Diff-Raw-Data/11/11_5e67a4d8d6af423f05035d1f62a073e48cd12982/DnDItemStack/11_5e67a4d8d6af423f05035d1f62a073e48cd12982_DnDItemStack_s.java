 package com.gildorymrp.api.plugin.equipment;
 
 import java.util.List;
 
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Represents a D&D item
  * @author Lucariatias
  *
  */
 public interface DnDItemStack {
 	
 	/**
	 * Gets the name of this item
 	 * It may be custom, or it may be the name of the item in D&D
 	 * 
 	 * @return the name of the item
 	 */
 	public String getName();
 	
 	/**
	 * Sets the name of this item
 	 * 
 	 * @param name the name to set
 	 */
 	public void setName(String name);
 	
 	/**
	 * Gets the lore of this item
 	 * This may include dice rolls, description, etc.
 	 * 
	 * @return
 	 */
 	public List<String> getLore();
 	
 	/**
 	 * Completely wipes the lore and sets it to something new
 	 * 
 	 * @param lore the lore to set
 	 */
 	public void setLore(List<String> lore);
 	
 	/**
 	 * Adds lore below the existing lore
 	 * 
 	 * @param lore the lore to add
 	 */
 	public void addLore(String lore);
 	
 	/**
 	 * Gets the type of item this would be in D&D
 	 * 
 	 * @return the D&D type
 	 */
 	public DnDMaterial getDnDType();
 	
 	/**
 	 * Sets the type of item this would be in D&D
 	 * 
 	 * @param type the D&D type to set
 	 */
 	public void setDnDType(DnDMaterial type);
 	
 	/**
 	 * Gets the weight in lb
 	 * 
 	 * @return the weight in lb
 	 */
 	public int getWeight();
 	
 	/**
 	 * Gets the amount of items
 	 * 
 	 * @return the amount of items
 	 */
 	public int getAmount();
 	
 	/**
 	 * Sets the amount of items
 	 * 
 	 * @param amount the amount to set
 	 */
 	public void setAmount(int amount);
 	
 	/**
 	 * Converts the D&D ItemStack into a Bukkit ItemStack
 	 * 
 	 * @return the converted itemstack
 	 */
 	public ItemStack toItemStack();
 	
 	/**
 	 * Attempts to convert the given Bukkit ItemStack into this D&D ItemStack
 	 * 
 	 * @param itemStack the bukkit itemstack to convert from
 	 */
 	public void fromItemStack(ItemStack itemStack);
 
 }
