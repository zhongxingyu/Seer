 package dark.assembly.common.machine;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import dark.core.blocks.InvChest;
 
 public class InventoryCrate extends InvChest
 {
     public InventoryCrate(TileEntity crate)
     {
         super(crate, 512);
     }
 
     /** Clones the single stack into an inventory format for automation interaction */
     public void buildInventory(ItemStack sampleStack)
     {
        this.items = new ItemStack[this.getSizeInventory()];
         if (sampleStack != null)
         {
             ItemStack baseStack = sampleStack.copy();
             int itemsLeft = baseStack.stackSize;
 
            for (int slot = 0; slot < this.items.length; slot++)
             {
                 int stackL = Math.min(Math.min(itemsLeft, baseStack.getMaxStackSize()), this.getInventoryStackLimit());
                this.items[slot] = baseStack.copy();
                this.items[slot].stackSize = stackL;
                 itemsLeft -= stackL;
                 if (baseStack.stackSize <= 0)
                 {
                     baseStack = null;
                     break;
                 }
             }
         }
     }
 
     @Override
     public int getSizeInventory()
     {
         if (this.hostTile instanceof TileEntityCrate)
         {
             return ((TileEntityCrate) this.hostTile).getSlotCount();
         }
         return 512;
     }
 
     @Override
     public String getInvName()
     {
         return "inv.Crate";
     }
 
     @Override
     public void saveInv(NBTTagCompound nbt)
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void loadInv(NBTTagCompound nbt)
     {
         if (nbt.hasKey("Items"))
         {
             super.loadInv(nbt);
         }
 
     }
 }
