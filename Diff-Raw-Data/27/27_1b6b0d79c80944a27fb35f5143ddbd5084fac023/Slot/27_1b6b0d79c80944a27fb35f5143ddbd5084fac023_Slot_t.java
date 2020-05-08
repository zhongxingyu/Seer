 package com.turt2live.antishare.inventory;
 
 import org.bukkit.inventory.ItemStack;
 
 public class Slot {
 
 	public int slot = 0;
 	public ItemStack item = null;
 
	public Slot(int slot, ItemStack item){
		this.slot = slot;
		this.item = item;
	}

 }
