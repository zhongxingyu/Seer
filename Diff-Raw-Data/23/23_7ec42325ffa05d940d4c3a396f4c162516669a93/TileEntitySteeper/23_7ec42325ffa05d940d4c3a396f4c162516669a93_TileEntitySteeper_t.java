 package MMC.neocraft.tileentity;
 
 import MMC.neocraft.block.BlockSteeper;
 import MMC.neocraft.recipe.SteeperRecipes;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemHoe;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemSword;
 import net.minecraft.item.ItemTool;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraftforge.common.ForgeDirection;
 
 public class TileEntitySteeper extends NCtileentity
 {
     private ItemStack[] steeperItemStacks = new ItemStack[4];
     /** The number of ticks that the steeper will keep burning */
     public int steeperBurnTime = 0;
     /** The number of ticks that a fresh copy of the currently-steeping item would keep the steeper burning for */
     public int currentItemSteepTime = 0;
     /** The number of ticks that the current item has been steeping for */
     public int steeperCookTime = 0;
    
    public TileEntitySteeper()
    {
    	super();
    }
    
     public ForgeDirection getOrientation() { return orientation; }
     public void setOrientation(ForgeDirection orientation) { this.orientation = orientation; }
     public void setOrientation(int orientation) { this.orientation = ForgeDirection.getOrientation(orientation); }
     public int getSizeInventory() { return this.steeperItemStacks.length; }
     public ItemStack getStackInSlot(int par1) { return this.steeperItemStacks[par1]; }
     /** Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a new stack. */
     public ItemStack decrStackSize(int par1, int par2)
     {
         if (this.steeperItemStacks[par1] != null)
         {
             ItemStack itemstack;
             if (this.steeperItemStacks[par1].stackSize <= par2)
             {
                 itemstack = this.steeperItemStacks[par1];
                 this.steeperItemStacks[par1] = null;
                 return itemstack;
             }
             else
             {
                 itemstack = this.steeperItemStacks[par1].splitStack(par2);
                 if (this.steeperItemStacks[par1].stackSize == 0) { this.steeperItemStacks[par1] = null; }
                 return itemstack;
             }
         }
         else { return null; }
     }
     public ItemStack getStackInSlotOnClosing(int par1)
     {
         if (this.steeperItemStacks[par1] != null)
         {
             ItemStack itemstack = this.steeperItemStacks[par1];
             this.steeperItemStacks[par1] = null;
             return itemstack;
         }
         else { return null; }
     }
     public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
     {
         this.steeperItemStacks[par1] = par2ItemStack;
 
         if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
         {
             par2ItemStack.stackSize = this.getInventoryStackLimit();
         }
     }
     @Override public String getInvName() { return this.isInvNameLocalized() ? this.invName : "Tea Steeper"; }
     @Override public boolean isInvNameLocalized() { return this.invName != null && this.invName.length() > 0; }
     public void setInvName(String par1Str) { this.invName = par1Str; }
     
     @Override public void readFromNBT(NBTTagCompound par1NBTTagCompound)
     {
         super.readFromNBT(par1NBTTagCompound);
         NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items");
         this.steeperItemStacks = new ItemStack[this.getSizeInventory()];
 
         for (int i = 0; i < nbttaglist.tagCount(); ++i)
         {
             NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
             byte b0 = nbttagcompound1.getByte("Slot");
             if (b0 >= 0 && b0 < this.steeperItemStacks.length) { this.steeperItemStacks[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1); }
         }
 
         this.steeperBurnTime = par1NBTTagCompound.getShort("BurnTime");
         this.steeperCookTime = par1NBTTagCompound.getShort("CookTime");
         this.currentItemSteepTime = getItemSteepTime(this.steeperItemStacks[2]);
     }
     @Override public void writeToNBT(NBTTagCompound par1NBTTagCompound)
     {
         super.writeToNBT(par1NBTTagCompound);
         par1NBTTagCompound.setShort("BurnTime", (short)this.steeperBurnTime);
         par1NBTTagCompound.setShort("CookTime", (short)this.steeperCookTime);
         NBTTagList nbttaglist = new NBTTagList();
 
         for (int i = 0; i < this.steeperItemStacks.length; ++i)
         {
             if (this.steeperItemStacks[i] != null)
             {
                 NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                 nbttagcompound1.setByte("Slot", (byte)i);
                 this.steeperItemStacks[i].writeToNBT(nbttagcompound1);
                 nbttaglist.appendTag(nbttagcompound1);
             }
         }
         par1NBTTagCompound.setTag("Items", nbttaglist);
     }
     public int getInventoryStackLimit() { return 64; }
     @SideOnly(Side.CLIENT) public int getCookProgressScaled(int par1) { return this.steeperCookTime * par1 / 200; }
 
     @SideOnly(Side.CLIENT)
     public int getBurnTimeRemainingScaled(int par1)
     {
         if (this.currentItemSteepTime == 0) { this.currentItemSteepTime = 200; }
         return this.steeperBurnTime * par1 / this.currentItemSteepTime;
     }
     public boolean isBurning() { return this.steeperBurnTime > 0; }
     @Override
     public void updateEntity()
     {
         boolean wasBurning = this.steeperBurnTime > 0;
         boolean hasChanged = false;
 
         if (this.steeperBurnTime > 0) { --this.steeperBurnTime; }
         if (!this.worldObj.isRemote)
         {
             if (this.steeperBurnTime == 0 && this.canSteep())
             {
                 this.currentItemSteepTime = this.steeperBurnTime = getItemSteepTime(this.steeperItemStacks[2]);
                 if (this.steeperBurnTime > 0)
                 {
                 	hasChanged = true;
                     if (this.steeperItemStacks[2] != null)
                     {
                         --this.steeperItemStacks[2].stackSize;
                         if (this.steeperItemStacks[2].stackSize == 0)
                         {
                             this.steeperItemStacks[2] = this.steeperItemStacks[2].getItem().getContainerItemStack(steeperItemStacks[2]);
                         }
                     }
                 }
             }
 
             if (this.isBurning() && this.canSteep())
             {
                 ++this.steeperCookTime;
 
                 if (this.steeperCookTime == 200)
                 {
                     this.steeperCookTime = 0;
                     this.steepItem();
                     hasChanged = true;
                 }
             }
             else
             {
                 this.steeperCookTime = 0;
             }
 
             if (wasBurning != this.steeperBurnTime > 0)
             {
             	hasChanged = true;
                 BlockSteeper.updateSteeperBlockState(this.steeperBurnTime > 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
             }
         }
         if (hasChanged) { this.onInventoryChanged(); }
     }
     private boolean canSteep()
     {
         if (this.steeperItemStacks[0] == null || this.steeperItemStacks[1] == null) { return false; }
         else
         {
             ItemStack itemstack = SteeperRecipes.steeping().getSteepingResult(this.steeperItemStacks[0], this.steeperItemStacks[1]);
             int teaAmount = SteeperRecipes.steeping().getTeaRequired(this.steeperItemStacks[0], this.steeperItemStacks[1]);
             if(itemstack == null) return false;
             if(this.steeperItemStacks[0].stackSize < teaAmount) { return false; }
             if(this.steeperItemStacks[3] == null) return true;
             if(!this.steeperItemStacks[3].isItemEqual(itemstack)) return false;
             int result = steeperItemStacks[3].stackSize + itemstack.stackSize;
             return (result <= getInventoryStackLimit() && result <= itemstack.getMaxStackSize());
         }
     }
     public void steepItem()
     {
         if (this.canSteep())
         {
             ItemStack itemstack = SteeperRecipes.steeping().getSteepingResult(this.steeperItemStacks[0], this.steeperItemStacks[1]);
             int teaAmount = SteeperRecipes.steeping().getTeaRequired(this.steeperItemStacks[0], this.steeperItemStacks[1]);
             
             if (this.steeperItemStacks[3] == null) { this.steeperItemStacks[3] = itemstack.copy(); }
             else if (this.steeperItemStacks[3].isItemEqual(itemstack)) { steeperItemStacks[3].stackSize += itemstack.stackSize; }
 
             this.steeperItemStacks[0].stackSize -= teaAmount;
             --this.steeperItemStacks[1].stackSize;
 
             if (this.steeperItemStacks[0].stackSize <= 0) { this.steeperItemStacks[0] = null; }
             if (this.steeperItemStacks[1].stackSize <= 0) { this.steeperItemStacks[1] = this.steeperItemStacks[1].getItem().getContainerItemStack(steeperItemStacks[1]); }
         }
     }
     public static int getItemSteepTime(ItemStack fuel)
     {
         if (fuel == null) { return 0; }
         else
         {
             int i = fuel.getItem().itemID;
             Item item = fuel.getItem();
 
             if (fuel.getItem() instanceof ItemBlock && Block.blocksList[i] != null)
             {
                 Block block = Block.blocksList[i];
                 if (block == Block.woodSingleSlab) { return 150; }
                 if (block.blockMaterial == Material.wood) { return 300; }
             }
 
             if (item instanceof ItemTool && ((ItemTool) item).getToolMaterialName().equals("WOOD")) return 200;
             if (item instanceof ItemSword && ((ItemSword) item).getToolMaterialName().equals("WOOD")) return 200;
             if (item instanceof ItemHoe && ((ItemHoe) item).getMaterialName().equals("WOOD")) return 200;
             if (i == Item.stick.itemID) return 100;
             if (i == Item.coal.itemID) return 1600;
             if (i == Item.bucketLava.itemID) return 20000;
             if (i == Block.sapling.blockID) return 100;
             if (i == Item.blazeRod.itemID) return 2400;
             return GameRegistry.getFuelValue(fuel);
         }
     }
     public static boolean isItemFuel(ItemStack par0ItemStack) { return getItemSteepTime(par0ItemStack) > 0; }
     @Override public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer) { return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D; }
     @Override public void openChest() {  }
     @Override public void closeChest() {  }
     @Override public boolean isStackValidForSlot(int par1, ItemStack par2ItemStack) { return par1 == 3 ? false : (par1 == 2 ? isItemFuel(par2ItemStack) : par1 == 1 ? par2ItemStack.itemID == Item.bucketWater.itemID : true); }
     @Override public int[] getAccessibleSlotsFromSide(int par1)
     {
     	if(par1 == 0 || par1 == 1) { return par1 == 0 ? new int[]{ 2 } : new int[]{ 0 }; }
     	int right = this.orientation.ordinal() != 5 ? this.orientation.ordinal() + 1 : 0;
     	int left = this.orientation.ordinal() != 2 ? this.orientation.ordinal() - 1 : 5;
     	if(par1 == right) { return new int[]{ 3 }; }
     	if(par1 == left) { return new int[]{ 1 }; }
     	return null;
     }
     /** Returns true if automation can insert the given item in the given slot from the given side. Args: Slot, item, side */
     @Override public boolean canInsertItem(int par1, ItemStack par2ItemStack, int par3)
     {
     	if(par1 == 3) { return false; }
         return this.isStackValidForSlot(par1, par2ItemStack);
     }
 
     /**
      * Returns true if automation can extract the given item in the given slot from the given side. Args: Slot, item,
      * side
      */
     @Override public boolean canExtractItem(int par1, ItemStack par2ItemStack, int par3)
     {
         return par1 == 3;
     }
 
 	
 }
