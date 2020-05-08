 package com.xetosphere.arcane.item;
 
 import com.xetosphere.arcane.ArchaniCommutatio;
 
 public class ItemRuneBase extends ItemARC {
 
 	public ItemRuneBase(int id) {
 
 		super(id);
 		setCreativeTab(ArchaniCommutatio.tabARC);
 		setMaxStackSize(64);
 	}
 
 }
