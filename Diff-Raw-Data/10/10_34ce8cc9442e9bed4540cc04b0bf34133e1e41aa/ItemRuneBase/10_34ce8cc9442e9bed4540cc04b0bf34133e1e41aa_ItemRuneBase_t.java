 package com.xetosphere.arcane.item;
 
 import com.xetosphere.arcane.ArchaniCommutatio;
import com.xetosphere.arcane.lib.Strings;
 
 public class ItemRuneBase extends ItemARC {
 
 	public ItemRuneBase(int id) {
 
 		super(id);
 		setCreativeTab(ArchaniCommutatio.tabARC);
		setUnlocalizedName(Strings.RESOURCE_PREFIX + Strings.RUNE_BASE_NAME);
 		setMaxStackSize(64);
 	}
 
 }
