 /**
  * This file is part of Hygienic.
 
     Hygienic is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Hygienic is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Hygienic.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.joaogldarkdeagle.hygienic.gui;
 
 import us.joaogldarkdeagle.hygienic.Hygienic;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.InventoryCraftResult;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class ContainerPolluCraft extends Container {
     public InventoryCrafting craftMatrix = new InventoryCrafting(this, 4, 4);
     public IInventory craftResult = new InventoryCraftResult();
     private World worldObj;
     private int posX;
     private int posY;
     private int posZ;
 
     public ContainerPolluCraft(InventoryPlayer par1InventoryPlayer, World par2World, int par3, int par4, int par5) {
         this.worldObj = par2World;
         this.posX = par3;
         this.posY = par4;
         this.posZ = par5;
 
         this.addSlotToContainer(new SlotPolluCraft(par1InventoryPlayer.player, this.craftMatrix, this.craftResult, 0, 121, 33));
        //this.addSlotToContainer(new SlotPolluCraft(par1InventoryPlayer.player, this.craftMatrix, this.craftResult, 0, 149, 33));
         int var6;
         int var7;
 
         // var7 + var6 * 5= no Idea... but it crash the game so dont change
         // it!;
        for (var6 = 0; var6 < 4; var6++)
            for (var7 = 0; var7 < 4; var7++)
                 this.addSlotToContainer(new Slot(this.craftMatrix, var7 + var6 * 3, 8 + var7 * 18, 6 + var6 * 18));
 
         // var7 + var6 * 9 + 2 = no Idea... but it crash the game so dont change
         // it!; 8 + var7 * 18 = size of it; 100 + var6 * 18 = y
         for (var6 = 0; var6 < 3; ++var6)
             for (var7 = 0; var7 < 9; ++var7)
                 this.addSlotToContainer(new Slot(par1InventoryPlayer, var7 + var6 * 9 + 2, 8 + var7 * 18, 98 + var6 * 18));
 
         // var6 = slotID; 8 + var6 * 18 = empty space between each slot; 156 = y
         for (var6 = 0; var6 < 9; ++var6)
             this.addSlotToContainer(new Slot(par1InventoryPlayer, var6, 8 + var6 * 18, 156));
 
         this.onCraftMatrixChanged(this.craftMatrix);
     }
 
     public void onCraftMatrixChanged(IInventory par1IInventory) {
         this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj));
     }
 
     public void onCraftGuiClosed(EntityPlayer par1EntityPlayer) {
         super.onCraftGuiClosed(par1EntityPlayer);
 
         if (!this.worldObj.isRemote) {
             for (int var2 = 0; var2 < 9; ++var2) {
                 ItemStack var3 = this.craftMatrix.getStackInSlotOnClosing(var2);
                 if (var3 != null) par1EntityPlayer.dropPlayerItem(var3);
             }
         }
     }
 
     public boolean canInteractWith(EntityPlayer par1EntityPlayer) {
         return this.worldObj.getBlockId(this.posX, this.posY, this.posZ) != Hygienic.polluCraft.blockID ? false : par1EntityPlayer.getDistanceSq((double) this.posX + 0.5D, (double) this.posY + 0.5D, (double) this.posZ + 0.5D) <= 64.0D;
     }
 
     public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
         ItemStack var3 = null;
         Slot var4 = (Slot) this.inventorySlots.get(par2);
 
         if (var4 != null && var4.getHasStack()) {
             ItemStack var5 = var4.getStack();
             var3 = var5.copy();
 
             if (par2 == 0) {
                 if (!this.mergeItemStack(var5, 10, 46, true)) return null;
                 var4.onSlotChange(var5, var3);
             } else if (par2 >= 10 && par2 < 37) {
                 if (!this.mergeItemStack(var5, 37, 46, false)) return null;
             } else if (par2 >= 37 && par2 < 46) {
                 if (!this.mergeItemStack(var5, 10, 37, false)) return null;
             } else if (!this.mergeItemStack(var5, 10, 46, false)) return null;
 
             if (var5.stackSize == 0) var4.putStack((ItemStack) null);
             else var4.onSlotChanged();
             if (var5.stackSize == var3.stackSize) return null;
             var4.onPickupFromSlot(par1EntityPlayer, var5);
         }
         return var3;
     }
 }
