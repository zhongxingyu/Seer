 package com.madpcgaming.mt.inventory;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ICrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.tileentity.TileEntityFurnace;
 
 import com.madpcgaming.mt.tileentitys.TileSimpleEFurnace;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ContainerSimpleEFurnace extends Container
 {
 	private TileSimpleEFurnace tile;
 	private int lastCookTime;
     private int lastBurnTime;
     private int lastItemBurnTime;
 	
 	public ContainerSimpleEFurnace(InventoryPlayer par1InventoryPlayer, TileSimpleEFurnace tile)
 	{
 		this.tile = tile;
 		
		this.addSlotToContainer(new Slot(tile, 0, 56, 17));
		this.addSlotToContainer(new Slot(tile, 1, 56, 53));
 		
 		BindPlayerInventory(par1InventoryPlayer);
 	}
 	
 	public void addCraftingToCrafters(ICrafting par1ICrafting)
     {
         super.addCraftingToCrafters(par1ICrafting);
         par1ICrafting.sendProgressBarUpdate(this, 0, this.tile.furnaceCookTime);
         par1ICrafting.sendProgressBarUpdate(this, 1, this.tile.furnaceBurnTime);
         par1ICrafting.sendProgressBarUpdate(this, 2, this.tile.currentItemBurnTime);
     }
 	
 	/**
      * Looks for changes made in the container, sends them to every listener.
      */
     public void detectAndSendChanges()
     {
         super.detectAndSendChanges();
 
         for (int i = 0; i < this.crafters.size(); ++i)
         {
             ICrafting icrafting = (ICrafting)this.crafters.get(i);
 
             if (this.lastCookTime != this.tile.furnaceCookTime)
             {
                 icrafting.sendProgressBarUpdate(this, 0, this.tile.furnaceCookTime);
             }
 
             if (this.lastBurnTime != this.tile.furnaceBurnTime)
             {
                 icrafting.sendProgressBarUpdate(this, 1, this.tile.furnaceBurnTime);
             }
 
             if (this.lastItemBurnTime != this.tile.currentItemBurnTime)
             {
                 icrafting.sendProgressBarUpdate(this, 2, this.tile.currentItemBurnTime);
             }
         }
 
         this.lastCookTime = this.tile.furnaceCookTime;
         this.lastBurnTime = this.tile.furnaceBurnTime;
         this.lastItemBurnTime = this.tile.currentItemBurnTime;
     }
 
     @SideOnly(Side.CLIENT)
     public void updateProgressBar(int par1, int par2)
     {
         if (par1 == 0)
         {
             this.tile.furnaceCookTime = par2;
         }
 
         if (par1 == 1)
         {
             this.tile.furnaceBurnTime = par2;
         }
 
         if (par1 == 2)
         {
             this.tile.currentItemBurnTime = par2;
         }
     }
 	
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer)
 	{
 		return tile.isUseableByPlayer(entityplayer);
 	}
 	
 	private void BindPlayerInventory(InventoryPlayer inventoryPlayer)
 	{
 		int i;
 
         for (i = 0; i < 3; ++i)
         {
             for (int j = 0; j < 9; ++j)
             {
                 this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
             }
         }
 
         for (i = 0; i < 9; ++i)
         {
             this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
         }
 	}
 	
 	/**
      * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
      */
     public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
     {
         ItemStack itemstack = null;
         Slot slot = (Slot)this.inventorySlots.get(par2);
 
         if (slot != null && slot.getHasStack())
         {
             ItemStack itemstack1 = slot.getStack();
             itemstack = itemstack1.copy();
 
             if (par2 == 2)
             {
                 if (!this.mergeItemStack(itemstack1, 3, 39, true))
                 {
                     return null;
                 }
 
                 slot.onSlotChange(itemstack1, itemstack);
             }
             else if (par2 != 1 && par2 != 0)
             {
                 if (FurnaceRecipes.smelting().getSmeltingResult(itemstack1) != null)
                 {
                     if (!this.mergeItemStack(itemstack1, 0, 1, false))
                     {
                         return null;
                     }
                 }
                 else if (TileEntityFurnace.isItemFuel(itemstack1))
                 {
                     if (!this.mergeItemStack(itemstack1, 1, 2, false))
                     {
                         return null;
                     }
                 }
                 else if (par2 >= 3 && par2 < 30)
                 {
                     if (!this.mergeItemStack(itemstack1, 30, 39, false))
                     {
                         return null;
                     }
                 }
                 else if (par2 >= 30 && par2 < 39 && !this.mergeItemStack(itemstack1, 3, 30, false))
                 {
                     return null;
                 }
             }
             else if (!this.mergeItemStack(itemstack1, 3, 39, false))
             {
                 return null;
             }
 
             if (itemstack1.stackSize == 0)
             {
                 slot.putStack((ItemStack)null);
             }
             else
             {
                 slot.onSlotChanged();
             }
 
             if (itemstack1.stackSize == itemstack.stackSize)
             {
                 return null;
             }
 
             slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
         }
 
         return itemstack;
     }
 }
