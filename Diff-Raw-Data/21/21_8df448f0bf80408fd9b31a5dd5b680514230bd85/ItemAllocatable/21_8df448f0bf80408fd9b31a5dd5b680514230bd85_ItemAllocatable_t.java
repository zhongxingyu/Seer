 package com.em.allocator.item;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
 
 public abstract class ItemAllocatable {
 
 	// The ItemStack containing the Item
 	private ItemStack theItemStack;
 
 	
 	public ItemStack getTheItemStack() {
 		return theItemStack;
 	}
 
 	protected ItemAllocatable(ItemStack theItemStack) {
 		this.theItemStack = theItemStack;
 	}
 	
 	public Material getType() {
 		if (theItemStack == null)  {
 			return null;
 		}
 		return theItemStack.getType();
 	}

	public MaterialData getData() {
		if (theItemStack == null)  {
			return null;
		}
		return theItemStack.getData();
	}


 	public int getAmount() {
 		if (theItemStack == null)  {
 			return -1;
 		}
 		return theItemStack.getAmount();
 	}
 	
 	public abstract void remove();
 
 
 	public String toString() {
 		StringBuffer sb= new StringBuffer();
 		
 		sb.append(this.theItemStack.getType())
 			.append(" ")
 			.append(this.getClass().getName());
 		
 		return sb.toString();
 	}
 
 }
 
