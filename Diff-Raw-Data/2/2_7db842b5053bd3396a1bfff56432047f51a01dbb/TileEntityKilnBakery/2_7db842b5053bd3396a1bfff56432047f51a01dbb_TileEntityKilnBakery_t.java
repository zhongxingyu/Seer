 package MMC.neocraft.tileentity;
 
 import MMC.neocraft.block.KilnBakery;
 import MMC.neocraft.recipe.KilnBakeryRecipes;
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
 
 public class TileEntityKilnBakery extends NCtileentity 
 {
 	
 	 private ItemStack[] bakeryItemStacks = new ItemStack[3];
 	    /** The number of ticks that the steeper will keep burning */
 	    public int bakeryBurnTime = 0;
 	    /** The number of ticks that a fresh copy of the currently-steeping item would keep the steeper burning for */
 	    public int currentItemBakeTime = 0;
 	    /** The number of ticks that the current item has been steeping for */
 	    public int bakeryCookTime = 0;
 	    
 	    public TileEntityKilnBakery()
 	    {
 	    	this.setInvName("Bakery Kiln");
 	    }
 	    
 	    @Override public int getSizeInventory() { return this.bakeryItemStacks.length; }
 	    
 	    @Override public ItemStack getStackInSlot(int par1) { return this.bakeryItemStacks[par1]; }
 	    
 	    /** Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a new stack. */
 	    @Override public ItemStack decrStackSize(int par1, int par2)
 	    {
 	    	if (this.bakeryItemStacks[par1] != null)
 	        {
 	            ItemStack itemstack;
 	            if (this.bakeryItemStacks[par1].stackSize <= par2)
 	            {
 	                itemstack = this.bakeryItemStacks[par1];
 	                this.bakeryItemStacks[par1] = null;
 	                return itemstack;
 	            }
 	            else
 	            {
 	                itemstack = this.bakeryItemStacks[par1].splitStack(par2);
 	                if (this.bakeryItemStacks[par1].stackSize == 0) { this.bakeryItemStacks[par1] = null; }
 	                return itemstack;
 	            }
 	        }
 	        else { return null; }
 	    }
 	    
 	    @Override public ItemStack getStackInSlotOnClosing(int par1)
 	    {
 	        if (this.bakeryItemStacks[par1] != null)
 	        {
 	            ItemStack itemstack = this.bakeryItemStacks[par1];
 	            this.bakeryItemStacks[par1] = null;
 	            return itemstack;
 	        }
 	        else { return null; }
 	    }
 	    
 	   @Override public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
 	    {
 	        this.bakeryItemStacks[par1] = par2ItemStack;
 
 	        if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
 	        {
 	            par2ItemStack.stackSize = this.getInventoryStackLimit();
 	        }
 	    }
 	   
 	   @Override public void readFromNBT(NBTTagCompound par1NBTTagCompound)
 	    {
 	        super.readFromNBT(par1NBTTagCompound);
 	        NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items");
 	        this.bakeryItemStacks = new ItemStack[this.getSizeInventory()];
 
 	        for (int i = 0; i < nbttaglist.tagCount(); ++i)
 	        {
 	            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
 	            byte b0 = nbttagcompound1.getByte("Slot");
 	            if (b0 >= 0 && b0 < this.bakeryItemStacks.length) { this.bakeryItemStacks[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1); }
 	        }
 
 	        this.bakeryBurnTime = par1NBTTagCompound.getShort("BurnTime");
 	        this.bakeryCookTime = par1NBTTagCompound.getShort("CookTime");
 	        this.currentItemBakeTime = getItemBurnTime(this.bakeryItemStacks[2]);
 	    }
 	   
 	    @Override public void writeToNBT(NBTTagCompound par1NBTTagCompound)
 	    {
 	        super.writeToNBT(par1NBTTagCompound);
 	        par1NBTTagCompound.setShort("BurnTime", (short)this.bakeryBurnTime);
 	        par1NBTTagCompound.setShort("CookTime", (short)this.bakeryCookTime);
 	        NBTTagList nbttaglist = new NBTTagList();
 
 	        for (int i = 0; i < this.bakeryItemStacks.length; ++i)
 	        {
 	            if (this.bakeryItemStacks[i] != null)
 	            {
 	                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
 	                nbttagcompound1.setByte("Slot", (byte)i);
 	                this.bakeryItemStacks[i].writeToNBT(nbttagcompound1);
 	                nbttaglist.appendTag(nbttagcompound1);
 	            }
 	        }
 	        par1NBTTagCompound.setTag("Items", nbttaglist);
 	    }
 	    
 	    public int getInventoryStackLimit() { return 64; }
 	    
 	    @SideOnly(Side.CLIENT) public int getCookProgressScaled(int par1) { return this.bakeryCookTime * par1 / 200; }
 
 	    @SideOnly(Side.CLIENT)
 	    public int getBurnTimeRemainingScaled(int par1)
 	    {
 	        if (this.currentItemBakeTime == 0) { this.currentItemBakeTime = 200; }
 	        return this.bakeryBurnTime * par1 / this.currentItemBakeTime;
 	    }
 	    
 	    public boolean isBurning() { return this.bakeryBurnTime > 0; }
 	    
 	    @Override
 	    public void updateEntity()
 	    {
 	        boolean wasBurning = this.bakeryBurnTime > 0;
 	        boolean hasChanged = false;
 
 	        if (this.bakeryBurnTime > 0) { --this.bakeryBurnTime; }
 	        if (!this.worldObj.isRemote)
 	        {
 	            if (this.bakeryBurnTime == 0 && this.canBake())
 	            {
 	                this.currentItemBakeTime = this.bakeryBurnTime = getItemBurnTime(this.bakeryItemStacks[1]);
 	                if (this.bakeryBurnTime > 0)
 	                {
 	                	hasChanged = true;
 	                    if (this.bakeryItemStacks[1] != null)
 	                    {
 	                        --this.bakeryItemStacks[1].stackSize;
 	                        if (this.bakeryItemStacks[1].stackSize == 0)
 	                        {
	                            this.bakeryItemStacks[1] = this.bakeryItemStacks[1].getItem().getContainerItemStack(bakeryItemStacks[1]);
 	                        }
 	                    }
 	                }
 	            }
 
 	            if (this.isBurning() && this.canBake())
 	            {
 	                ++this.bakeryCookTime;
 
 	                if (this.bakeryCookTime == 200)
 	                {
 	                    this.bakeryCookTime = 0;
 	                    this.bakeItem();
 	                    hasChanged = true;
 	                }
 	            }
 	            else
 	            {
 	                this.bakeryCookTime = 0;
 	            }
 
 	            if (wasBurning != this.bakeryBurnTime > 0)
 	            {
 	            	hasChanged = true;
 	                KilnBakery.updateBakeryBlockState(this.bakeryBurnTime > 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
 	            }
 	        }
 	        if (hasChanged) { this.onInventoryChanged(); }
 	    }
 	    
 	    private boolean canBake()
 	    {
 	        if (this.bakeryItemStacks[0] == null) { return false; }
 	        else
 	        {
 	            ItemStack itemstack = KilnBakeryRecipes.baking().getBakingResult(this.bakeryItemStacks[0]);
 	            if(itemstack == null) return false;
 	            if(this.bakeryItemStacks[2] == null) return true;
 	            if(!this.bakeryItemStacks[2].isItemEqual(itemstack)) return false;
 	            int result = bakeryItemStacks[2].stackSize + itemstack.stackSize;
 	            return (result <= getInventoryStackLimit() && result <= itemstack.getMaxStackSize());
 	        }
 	    }
 	    
 	    public void bakeItem()
 	    {
 	        if (this.canBake())
 	        {
 	            ItemStack itemstack = KilnBakeryRecipes.baking().getBakingResult(this.bakeryItemStacks[0]);
 	            
 	            if (this.bakeryItemStacks[2] == null) { this.bakeryItemStacks[2] = itemstack.copy(); }
 	            else if (this.bakeryItemStacks[2].isItemEqual(itemstack)) { bakeryItemStacks[2].stackSize += itemstack.stackSize; }
 
 	            this.bakeryItemStacks[0].stackSize --;
 	            if (this.bakeryItemStacks[0].stackSize <= 0) { this.bakeryItemStacks[0] = null; }	        }
 	    }
 	    
 	    public static int getItemBurnTime(ItemStack fuel)
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
 	    
 	    public static boolean isItemFuel(ItemStack par0ItemStack) { return getItemBurnTime(par0ItemStack) > 0; }
 	    @Override public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer) { return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D; }
 	    @Override public void openChest() {  }
 	    @Override public void closeChest() {  }
 	    @Override public boolean isStackValidForSlot(int par1, ItemStack par2ItemStack) { return par1 == 2 ? false : (par1 == 1 ? isItemFuel(par2ItemStack) : true); }
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
