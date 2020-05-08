 package com.qzx.au.core;
 
 import net.minecraft.block.Block;
 import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 
 // fake pattern block
 // - ghosted item
 // - accepts a single item, but does not decrement stack
 // - item can not be removed
 //		- clicking clears item
 // - stone filter image
 public class SlotBlockCamo extends SlotPattern {
 	public SlotBlockCamo(IInventory inventory, int slot, int x, int y){
 		super(inventory, slot, x, y, 1);
 //		this.setFilterItemStack(new ItemStack(Block.stone));
 		this.setTooltip("block camo");
 		if(slot != -1) Debug.error("SlotBlockCamo needs slotIndex == -1");
 	}
 
 	@Override
 	public boolean isItemValid(ItemStack itemstack){
		if(!(itemstack.getItem() instanceof ItemBlock) || itemstack.itemID >= Block.blocksList.length) return false;
		return (Block.blocksList[itemstack.itemID] != null);
 	}
 
 	@Override
 	public ItemStack getStack(){
 		TileEntityAU te = (TileEntityAU)this.inventory;
 		return (te.canCamo() ? te.getCamoBlock() : null);
 	}
 
 	@Override
     public void putStack(ItemStack itemstack){
 		TileEntityAU te = (TileEntityAU)this.inventory;
 		if(!te.worldObj.isRemote && te.canCamo())
 			te.setCamoBlock(itemstack);
 	}
 }
