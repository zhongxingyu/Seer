 package mods.cc.rock.tileentity;
 
 import mods.cc.rock.block.BlockOven;
 import mods.cc.rock.recipie.OvenRecipes;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraftforge.common.ForgeDirection;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class TileEntityOven extends TileCC implements ISidedInventory, net.minecraftforge.common.ISidedInventory{
 
    
     public static final int COOK_SPEED = 100;
     /**What is in the oven*/
     public ItemStack[] ovenStacks = new ItemStack[3];
     
     /** The number of ticks that the oven will keep burning */
     public int ovenBurnTime = 0;
     
     /** The number of ticks that the current item has been cooking for */
     public int ovenCookTime = 0;
     
     /**
      * The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for
      */
     public int currentItemBurnTime = 0;
     
     
 	@Override
 	public int getSizeInventory() {
 		return ovenStacks.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		return ovenStacks[i];
 	}
 
 	@Override
     public ItemStack decrStackSize(int par1, int par2)
     {
         if (this.ovenStacks[par1] != null)
         {
             ItemStack itemstack;
 
             if (this.ovenStacks[par1].stackSize <= par2)
             {
                 itemstack = this.ovenStacks[par1];
                 this.ovenStacks[par1] = null;
                 return itemstack;
             }
             else
             {
                 itemstack = this.ovenStacks[par1].splitStack(par2);
 
                 if (this.ovenStacks[par1].stackSize == 0)
                 {
                     this.ovenStacks[par1] = null;
                 }
 
                 return itemstack;
             }
         }
         else
         {
             return null;
         }
     }
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		if(ovenStacks[i] != null){
 			ItemStack itemstack = ovenStacks[i];
 			ovenStacks[i] = null;
 			return itemstack;
 		}else{
 			return null;
 		}
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 		ovenStacks[i] = itemstack;
 		
 		if(itemstack != null && itemstack.stackSize > getInventoryStackLimit()){
 			itemstack.stackSize = getInventoryStackLimit();
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		
 		return this.hasCustomName() ? this.getCustomName() : "container.oven";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		
 		return this.hasCustomName();
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		
 		return 64;
 	}
 
 	@Override
 	public void openChest() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void closeChest() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
 		
 		return true;
 	}
 
 	@Override
 	@Deprecated
 	public int getStartInventorySide(ForgeDirection side) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	@Deprecated
 	public int getSizeInventorySide(ForgeDirection side) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int[] getAccessibleSlotsFromSide(int var1) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
     public void readFromNBT(NBTTagCompound par1NBTTagCompound)
     {
         super.readFromNBT(par1NBTTagCompound);
         NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items");
         this.ovenStacks = new ItemStack[this.getSizeInventory()];
 
         for (int i = 0; i < nbttaglist.tagCount(); ++i)
         {
             NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
             byte b0 = nbttagcompound1.getByte("Slot");
 
             if (b0 >= 0 && b0 < this.ovenStacks.length)
             {
                 this.ovenStacks[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
             }
         }
 
        this.ovenBurnTime = par1NBTTagCompound.getInteger("BurnTime");
        this.ovenCookTime = par1NBTTagCompound.getInteger("CookTime");
        this.currentItemBurnTime = par1NBTTagCompound.getInteger("CurrentItemBurnTime");
 
         
     }
     /**
      * Writes a tile entity to NBT.
      */
     public void writeToNBT(NBTTagCompound par1NBTTagCompound)
     {
         super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("BurnTime", this.ovenBurnTime);
        par1NBTTagCompound.setInteger("CookTime", this.ovenCookTime);
        par1NBTTagCompound.setInteger("CurrentItemBurnTime", this.currentItemBurnTime);
         NBTTagList nbttaglist = new NBTTagList();
 
         for (int i = 0; i < this.ovenStacks.length; ++i)
         {
             if (this.ovenStacks[i] != null)
             {
                 NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                 nbttagcompound1.setByte("Slot", (byte)i);
                 this.ovenStacks[i].writeToNBT(nbttagcompound1);
                 nbttaglist.appendTag(nbttagcompound1);
             }
         }
 
         par1NBTTagCompound.setTag("Items", nbttaglist);
 
        
     }
     /**
      * Returns the number of ticks that the supplied fuel item will keep the oven burning, or 0 if the item isn't
      * fuel
      */
     public static int getItemBurnTime(ItemStack par0ItemStack){
     	return TileEntityFurnace.getItemBurnTime(par0ItemStack);
     }
     
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns an integer between 0 and the passed value representing how close the current item is to being completely
      * cooked
      */
     public int getCookProgressScaled(int par1)
     {
         return this.ovenCookTime * par1 / COOK_SPEED;
     }
     
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns an integer between 0 and the passed value representing how much burn time is left on the current fuel
      * item, where 0 means that the item is exhausted and the passed value means that the item is fresh
      */
     public int getBurnTimeRemainingScaled(int par1)
     {
         if (this.currentItemBurnTime == 0)
         {
             this.currentItemBurnTime = 200;
         }
 
         return this.ovenBurnTime * par1 / this.currentItemBurnTime;
     }
     
     /**
      * Returns true if the oven is currently burning
      */
     public boolean isBurning()
     {
         return this.ovenBurnTime > 0;
     }
     
     /**
      * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
      * ticks and creates a new spawn inside its implementation.
      */
     public void updateEntity()
     {
         boolean flag = this.ovenBurnTime > 0;
         boolean flag1 = false;
 
         if (this.ovenBurnTime > 0)
         {
             --this.ovenBurnTime;
         }
 
         if (!this.worldObj.isRemote)
         {
             if (this.ovenBurnTime == 0 && this.canSmelt())
             {
                 this.currentItemBurnTime = this.ovenBurnTime = getItemBurnTime(this.ovenStacks[1]);
 
                 if (this.ovenBurnTime > 0)
                 {
                     flag1 = true;
 
                     if (this.ovenStacks[1] != null)
                     {
                         --this.ovenStacks[1].stackSize;
 
                         if (this.ovenStacks[1].stackSize == 0)
                         {
                             this.ovenStacks[1] = this.ovenStacks[1].getItem().getContainerItemStack(ovenStacks[1]);
                         }
                     }
                 }
             }
 
             if (this.isBurning() && this.canSmelt())
             {
                 ++this.ovenCookTime;
 
                 if (this.ovenCookTime == COOK_SPEED)
                 {
                     this.ovenCookTime = 0;
                     this.smeltItem();
                     flag1 = true;
                 }
             }
             else
             {
                 this.ovenCookTime = 0;
             }
 
             if (flag != this.ovenBurnTime > 0)
             {
                 flag1 = true;
                 BlockOven.updateOvenBlockState(this.ovenBurnTime > 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
             }
         }
 
         if (flag1)
         {
             this.onInventoryChanged();
         }
     }
     
     /**
      * Returns true if the furnace can smelt an item, i.e. has a source item, destination stack isn't full, etc.
      */
     private boolean canSmelt()
     {
         if (this.ovenStacks[0] == null)
         {
             return false;
         }
         else
         {
             ItemStack itemstack = OvenRecipes.smelting().getSmeltingResult(this.ovenStacks[0]);
             if (itemstack == null) return false;
             if (this.ovenStacks[2] == null) return true;
             if (!this.ovenStacks[2].isItemEqual(itemstack)) return false;
             int result = ovenStacks[2].stackSize + itemstack.stackSize;
             return (result <= getInventoryStackLimit() && result <= itemstack.getMaxStackSize());
         }
     }
     
     public void smeltItem()
     {
         if (this.canSmelt())
         {
             ItemStack itemstack = OvenRecipes.smelting().getSmeltingResult(this.ovenStacks[0]);
 
             if (this.ovenStacks[2] == null)
             {
                 this.ovenStacks[2] = itemstack.copy();
             }
             else if (this.ovenStacks[2].isItemEqual(itemstack))
             {
             	ovenStacks[2].stackSize += itemstack.stackSize;
             }
 
             --this.ovenStacks[0].stackSize;
 
             if (this.ovenStacks[0].stackSize <= 0)
             {
                 this.ovenStacks[0] = null;
             }
         }
     }
     /**
      * Return true if item is a fuel source (getItemBurnTime() > 0).
      */
     public static boolean isItemFuel(ItemStack par0ItemStack)
     {
         return getItemBurnTime(par0ItemStack) > 0;
     }
 }
