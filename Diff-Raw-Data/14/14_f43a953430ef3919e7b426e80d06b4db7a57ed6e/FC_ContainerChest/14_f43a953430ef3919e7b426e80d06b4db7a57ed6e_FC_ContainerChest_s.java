 package shadow.mods.metallurgy.precious;
 
 import net.minecraft.src.Container;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.Slot;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.EntityPlayer;
 
 public class FC_ContainerChest extends Container
 {
     private IInventory lowerChestInventory;
     private int numRows;
     private int numCols;
 
     public FC_ContainerChest(IInventory playerInv, IInventory chestInv)
     {
         this.lowerChestInventory = chestInv;
         this.numRows = ((FC_TileEntityChest)chestInv).getNumRows();
         this.numCols = ((FC_TileEntityChest)chestInv).getNumCols();
         chestInv.openChest();
         int var3 = (this.numRows - 4) * 18;
         int currRow;
         int currCol;
 
         int modifier = 0;
         
         if(numCols == 12)
         {
        	modifier = -27;
         }
         if(numCols == 10)
         {
        	modifier = -9;
         }
         
         int i = 0;
         for (currRow = 0; currRow < this.numRows; ++currRow)
         {
             for (currCol = 0; currCol < this.numCols; ++currCol)
             {
                this.addSlotToContainer(new Slot(chestInv, currCol + currRow * numCols, 8 + currCol * 18 + modifier, 18 + currRow * 18));
             }
         }
 
         for (currRow = 0; currRow < 3; ++currRow)
         {
             for (currCol = 0; currCol < 9; ++currCol)
             {
                this.addSlotToContainer(new Slot(playerInv, currCol + currRow * 9 + 9, 8 + currCol * 18, 94 + currRow * 18 + var3));
             }
         }
 
         for (currRow = 0; currRow < 9; ++currRow)
         {
            this.addSlotToContainer(new Slot(playerInv, currRow, 8 + currRow * 18, 152 + var3));
         }
     }
 
     public boolean canInteractWith(EntityPlayer par1EntityPlayer)
     {
         return this.lowerChestInventory.isUseableByPlayer(par1EntityPlayer);
     }
 
     /**
      * Called to transfer a stack from one inventory to the other eg. when shift clicking.
      */
     public ItemStack transferStackInSlot(int par1)
     {
         ItemStack var2 = null;
         Slot var3 = (Slot)this.inventorySlots.get(par1);
 
         if (var3 != null && var3.getHasStack())
         {
             ItemStack var4 = var3.getStack();
             var2 = var4.copy();
 
             if (par1 < this.numRows * this.numCols)
             {
                 if (!this.mergeItemStack(var4, this.numRows * this.numCols, this.inventorySlots.size(), true))
                 {
                     return null;
                 }
             }
             else if (!this.mergeItemStack(var4, 0, this.numRows * this.numCols, false))
             {
                 return null;
             }
 
             if (var4.stackSize == 0)
             {
                 var3.putStack((ItemStack)null);
             }
             else
             {
                 var3.onSlotChanged();
             }
         }
 
         return var2;
     }
 
     /**
      * Callback for when the crafting gui is closed.
      */
     public void onCraftGuiClosed(EntityPlayer par1EntityPlayer)
     {
         super.onCraftGuiClosed(par1EntityPlayer);
         this.lowerChestInventory.closeChest();
     }
 }
