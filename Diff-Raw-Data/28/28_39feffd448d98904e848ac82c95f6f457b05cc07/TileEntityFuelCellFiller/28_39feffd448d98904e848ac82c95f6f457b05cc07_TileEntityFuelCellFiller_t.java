 package mal.carbonization.tileentity;
 
 import java.util.Random;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import mal.carbonization.carbonization;
 import mal.carbonization.items.ItemStructureBlock;
 import mal.api.*;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public class TileEntityFuelCellFiller extends TileEntity implements IInventory, net.minecraft.inventory.ISidedInventory{
 
 	public int craftingCooldown = 0;
 	public int processTime = carbonization.MAXAUTOCRAFTTIME;
 	private Random furnaceRand = new Random();
 	public double speedUpgrade;
 	public ItemStack[] inputStacks = new ItemStack[4];
 	public ItemStack outputStack;
 	public ItemStack[] upgradeStacks = new ItemStack[3];
 	
 	@Override
 	public void updateEntity()
 	{
 		super.updateEntity();
 		
 		//re-sort input stacks
 		sortInput();
 		
 		//manage the first input slot correctly
 		int value = getInputValue();
 		boolean flag = false;
 		if(isOutputValid(value))
 			flag = addOutputValue(value);
 		
 		if(flag)
 		{
 			craftingCooldown = processTime;
 		}
 		
 		//manage the upgrade slots
 		manageUpgrade();
 	}
 	
 	private void manageUpgrade()
 	{
 		if(upgradeStacks == null)
 			return;
 		
 		double value = 0;
 		
 		for(int i = 0; i < upgradeStacks.length; i++)
 		{
 			if(upgradeStacks[i] != null)
 			{
 				if(upgradeStacks[i].itemID == carbonization.itemStructureBlock.itemID)
 				{
 					if(upgradeStacks[i].getItemDamage() < 1000)
 					{
 						double[] d = ItemStructureBlock.getTier(upgradeStacks[i].getItemDamage());
 						value += (d[0]+d[1])/6*Math.sqrt(upgradeStacks[i].stackSize);
 					}
 				}
 			}
 		}
 		
 		speedUpgrade = value;
 		calculateProcessTime();
 	}
 	
 	public void activate(World world, int x, int y, int z,
 			EntityPlayer player) {
 		//System.out.println("Component Tiers: " + componentTiers[0] + ", " + componentTiers[1] +"; queue capacity: " + queue.maxJobs + "; activated: " + properlyActivated);
 		player.openGui(carbonization.instance, 4, world, x, y, z);
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound nbt)
 	{
 		super.writeToNBT(nbt);
 
 		nbt.setInteger("craftingCooldown", craftingCooldown);
 		nbt.setInteger("processTime", processTime);
 		nbt.setDouble("speedUpgrade", speedUpgrade);
 
 		NBTTagList input = new NBTTagList();
 
 		for (int i = 0; i < this.inputStacks.length; ++i)
 		{
 			if (this.inputStacks[i] != null)
 			{
 				NBTTagCompound var4 = new NBTTagCompound();
 				var4.setByte("Slot", (byte)i);
 				this.inputStacks[i].writeToNBT(var4);
 				input.appendTag(var4);
 			}
 		}
 		nbt.setTag("inputItems", input);
 
 		NBTTagList output = new NBTTagList();
 
 		for (int i = 0; i < 1; ++i)
 		{
 			if (this.outputStack != null)
 			{
 				NBTTagCompound var4 = new NBTTagCompound();
 				var4.setByte("Slot", (byte)i);
 				this.outputStack.writeToNBT(var4);
 				output.appendTag(var4);
 			}
 		}
 		nbt.setTag("outputItems", output);
 
 		NBTTagList upgrade = new NBTTagList();
 
 		for (int i = 0; i < this.upgradeStacks.length; ++i)
 		{
 			if (this.upgradeStacks[i] != null)
 			{
 				NBTTagCompound var4 = new NBTTagCompound();
 				var4.setByte("Slot", (byte)i);
 				this.upgradeStacks[i].writeToNBT(var4);
 				upgrade.appendTag(var4);
 			}
 		}
 		nbt.setTag("upgradeItems", upgrade);
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbt)
 	{
 		super.readFromNBT(nbt);
 
 		craftingCooldown = nbt.getInteger("craftingCooldown");
 		processTime = nbt.getInteger("processTime");
 		speedUpgrade = nbt.getDouble("speedUpgrade");
 
 		NBTTagList input = nbt.getTagList("inputItems");
 		for (int i = 0; i < input.tagCount(); ++i)
 		{
 			NBTTagCompound var4 = (NBTTagCompound)input.tagAt(i);
 			byte var5 = var4.getByte("Slot");
 
 			if (var5 >= 0 && var5 < this.inputStacks.length)
 			{
 				this.inputStacks[var5] = ItemStack.loadItemStackFromNBT(var4);
 			}
 		}
 
 		NBTTagList output = nbt.getTagList("outputItems");
 		for (int i = 0; i < output.tagCount(); ++i)
 		{
 			NBTTagCompound var4 = (NBTTagCompound)output.tagAt(i);
 			byte var5 = var4.getByte("Slot");
 
 			if (var5 >= 0 && var5 < 1)
 			{
 				this.outputStack = ItemStack.loadItemStackFromNBT(var4);
 			}
 		}
 
 		NBTTagList upgrade = nbt.getTagList("upgradeItems");
 		for (int i = 0; i < upgrade.tagCount(); ++i)
 		{
 			NBTTagCompound var4 = (NBTTagCompound)upgrade.tagAt(i);
 			byte var5 = var4.getByte("Slot");
 
 			if (var5 >= 0 && var5 < this.upgradeStacks.length)
 			{
 				this.upgradeStacks[var5] = ItemStack.loadItemStackFromNBT(var4);
 			}
 		}
 		
 		calculateProcessTime();
 	}
 
 	//build an int list of the inventory
 		public int[] buildIntList()
 		{
 			int[] list = new int[(inputStacks.length+1+upgradeStacks.length)*3+1];
 			//System.out.println("Created list of size " + list.length);
 			int pos = 0;
 			for(ItemStack is : inputStacks)
 			{
 				if(is != null)
 				{
 					//System.out.println("Putting input item: " + is.toString() + " in list");
 					list[pos++] = is.itemID;
 					list[pos++] = is.getItemDamage();
 					list[pos++] = is.stackSize;
 				}
 				else
 				{
 					//System.out.println("Putting null input item in list");
 					list[pos++] = 0;
 					list[pos++] = 0;
 					list[pos++] = 0;
 				}
 			}
 			
 			ItemStack os = outputStack;
 			{
 				if(os != null)
 				{
 					//System.out.println("Putting output item: " + os.toString() + " in list");
 					list[pos++] = os.itemID;
 					list[pos++] = os.getItemDamage();
 					list[pos++] = os.stackSize;
 				}
 				else
 				{
 					//System.out.println("Putting null output item in list");
 					list[pos++] = 0;
 					list[pos++] = 0;
 					list[pos++] = 0;
 				}
 			}
 
 			for(ItemStack us : upgradeStacks)
 			{
 				if(us != null)
 				{
 					//System.out.println("Putting output item: " + os.toString() + " in list");
 					list[pos++] = us.itemID;
 					list[pos++] = us.getItemDamage();
 					list[pos++] = us.stackSize;
 				}
 				else
 				{
 					//System.out.println("Putting null output item in list");
 					list[pos++] = 0;
 					list[pos++] = 0;
 					list[pos++] = 0;
 				}
 			}
 
 			return list;
 		}
 
 		public void recoverIntList(int[] items)
 		{
 			if(items != null)
 			{
 				int pos = 0;
 				for(int i = 0; i < this.inputStacks.length; i++)
 				{
 					if(pos+2 < items.length && items[pos+2]!=0)
 					{
 						ItemStack is = new ItemStack(items[pos], items[pos+2], items[pos+1]);
 						this.inputStacks[i]=is;
 					}
 					else
 					{
 						this.inputStacks[i] = null;
 					}
 					pos+= 3;
 				}
 				for(int i = 0; i < 1; i++)
 				{
 					if(pos+2 < items.length && items[pos+2]!=0)
 					{
 						ItemStack is = new ItemStack(items[pos], items[pos+2], items[pos+1]);
 						this.outputStack=is;
 					}
 					else
 					{
 						this.outputStack = null;
 					}
 					pos+= 3;
 				}
 				
 				for(int i = 0; i < this.upgradeStacks.length; i++)
 				{
 					if(pos+2 < items.length && items[pos+2]!=0)
 					{
 						ItemStack is = new ItemStack(items[pos], items[pos+2], items[pos+1]);
 						this.upgradeStacks[i]=is;
 					}
 					else
 					{
 						this.upgradeStacks[i] = null;
 					}
 					pos+= 3;
 				}
 			}
 		}
 
 	public void calculateProcessTime()
 	{
 		processTime = (int)Math.floor(carbonization.MAXAUTOCRAFTTIME-(carbonization.MAXAUTOCRAFTTIME-carbonization.MINAUTOCRAFTTIME)*speedUpgrade/100);
 		if(processTime < 1)
 			processTime = 1;
 	}
 	
 	//sort the input stacks
 	private void sortInput()
 	{
 		if(inputStacks == null)
 			return;
 		
 		for(int i = 0; i<3; i++)
 		{
 			if(inputStacks[i] == null)
 			{
 				if(inputStacks[i+1] != null)
 				{
 					inputStacks[i] = inputStacks[i+1].copy();
 					inputStacks[i+1] = null;
 				}
 			}
 			else if(inputStacks[i].stackSize<inputStacks[i].getMaxStackSize())
 			{
 				if(inputStacks[i+1]!=null)
 				{
 					if(inputStacks[i].itemID == inputStacks[i+1].itemID && ( (inputStacks[i].getItem().getHasSubtypes())?(inputStacks[i].getItemDamage() == inputStacks[i+1].getItemDamage()):(true)))
 					{
 						if(inputStacks[i].stackSize+inputStacks[i+1].stackSize<=inputStacks[i].getMaxStackSize())
 						{
 							inputStacks[i].stackSize += inputStacks[i+1].stackSize;
 							inputStacks[i+1] = null;
 						}
 						else
 						{
 							int value = inputStacks[i].getMaxStackSize()-inputStacks[i].stackSize;
 							inputStacks[i].stackSize = inputStacks[i].getMaxStackSize();
 							inputStacks[i+1].stackSize -= value;
 						}
 					}
 				}
 					
 			}
 		}
 	}
 	
 	private int getInputValue()
 	{
 		if(inputStacks[0]==null)
 			return 0;
 		else
 			return getItemBurnTime(inputStacks[0]);
 	}
 	
 	private boolean isOutputValid(int value)
 	{
 		if(craftingCooldown > 0)
 		{
 			craftingCooldown--;
 			return false;
 		}
 		
 		if(value <= 0)
 			return false;
 		if(outputStack == null)
 			return false;
 		if(outputStack.getItem() instanceof IFuelContainer)
 			return true;
 		return false;
 	}
 	private boolean addOutputValue(int value)
 	{
 		if(outputStack == null || !(outputStack.getItem() instanceof IFuelContainer || value <= 0))
 			return false;
 		
 		if(inputStacks[0] != null)
 		{
 			inputStacks[0].stackSize -= 1;
 			if(inputStacks[0].stackSize <= 0)
 				inputStacks[0] = null;
 		}
 		
 		return ((IFuelContainer)outputStack.getItem()).setFuel(outputStack, value, false);
 	}
 	
 	/**
 	 * Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 if the item isn't
 	 * fuel
 	 */
 	public static int getItemBurnTime(ItemStack par0ItemStack)
 	{
 		if (par0ItemStack == null)
 		{
 			return 0;
 		}
 		else
 		{
 			int var1 = par0ItemStack.getItem().itemID;
 			Item var2 = par0ItemStack.getItem();
 
 			if (par0ItemStack.getItem() instanceof ItemBlock && Block.blocksList[var1] != null)
 			{
 				Block var3 = Block.blocksList[var1];
 
 				if (var3 == Block.woodSingleSlab)
 				{
 					return 0;
 				}
 
 				if (var3.blockMaterial == Material.wood)
 				{
 					return 0;
 				}
 
 				if (var3 == Block.coalBlock)
 				{
 					return 16000;
 				}
 			}
 
 			if (var1 == Item.stick.itemID) return 0;
 			if (var1 == Item.coal.itemID) return 1600;
 			if (var1 == Item.bucketLava.itemID) return 0;
 			if (var1 == Block.sapling.blockID) return 0;
 			if (var1 == Item.blazeRod.itemID) return 0;
 			return GameRegistry.getFuelValue(par0ItemStack);
 		}
 	}
 	
 	//return the scaled percentage of the progress bar
 	@SideOnly(Side.CLIENT)
 	public int getCooldownScaled(int i)
 	{
 		//return (int)(0.2*i);
 		return (int) (craftingCooldown*i/(processTime+1));
 	}
 	
 	public void dumpInventory()
 	{
 		if(worldObj == null || worldObj.isRemote)
 			return;
 		//spit out all the inventory and convert fuel time into as much fuel as possible
 		float var10;
 		float var11;
 		float var12;
 
 		EntityPlayer player = worldObj.getClosestPlayer(xCoord, yCoord, zCoord, -1);
 
 		double ppx = 0;
 		double ppy = 0;
 		double ppz = 0;
 		if(player != null)
 		{
 			ppx = player.posX;
 			ppy = player.posY;
 			ppz = player.posZ;
 		}
 		float px = (float) (xCoord+ppx)/2;
 		float py = (float) (yCoord+ppy)/2;
 		float pz = (float) (zCoord+ppz)/2;
 		//unprocessed items
 		for(int i = 0; i<inputStacks.length; i++)
 		{
 			ItemStack item = inputStacks[i];
 
 			if (item != null)
 			{
 				//System.out.println("input index: " + i);
 				var10 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
 				var11 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
 				var12 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
 
 				EntityItem eItem = new EntityItem(worldObj, (double)(px + var10), (double)(py + var11), (double)(pz + var12), new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));
 
 				if (item.hasTagCompound())
 				{
 					eItem.getEntityItem().setTagCompound((NBTTagCompound)item.getTagCompound().copy());
 				}
 
 				float var15 = 0.05F;
 				eItem.motionX = (double)((float)this.furnaceRand.nextGaussian() * var15);
 				eItem.motionY = (double)((float)this.furnaceRand.nextGaussian() * var15 + 0.2F);
 				eItem.motionZ = (double)((float)this.furnaceRand.nextGaussian() * var15);
 				eItem.delayBeforeCanPickup = 10;
 				worldObj.spawnEntityInWorld(eItem);
 			}
 
 			inputStacks[i] = null;
 		}
 		//output inventory
 		ItemStack item2 = outputStack;
 
 		if (item2 != null)
 		{
 			//System.out.println("output index: " + j);
 			var10 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
 			var11 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
 			var12 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
 
 			EntityItem eItem = new EntityItem(worldObj, (double)(px + var10), (double)(py + var11), (double)(pz + var12), new ItemStack(item2.itemID, item2.stackSize, item2.getItemDamage()));
 
 			if (item2.hasTagCompound())
 			{
 				eItem.getEntityItem().setTagCompound((NBTTagCompound)item2.getTagCompound().copy());
 			}
 
 			float var15 = 0.05F;
 			eItem.motionX = (double)((float)this.furnaceRand.nextGaussian() * var15);
 			eItem.motionY = (double)((float)this.furnaceRand.nextGaussian() * var15 + 0.2F);
 			eItem.motionZ = (double)((float)this.furnaceRand.nextGaussian() * var15);
 			eItem.delayBeforeCanPickup = 10;
 			worldObj.spawnEntityInWorld(eItem);
 
 		}
 
 		outputStack = null;
 
 		//upgrade inventory
 		for(int j = 0; j<upgradeStacks.length; j++)
 		{
 			ItemStack item = upgradeStacks[j];
 
 			if (item != null)
 			{
 				//System.out.println("output index: " + j);
 				var10 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
 				var11 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
 				var12 = this.furnaceRand.nextFloat() * 0.8F + 0.1F;
 
 				EntityItem eItem = new EntityItem(worldObj, (double)(px + var10), (double)(py + var11), (double)(pz + var12), new ItemStack(item.itemID, item.stackSize, item.getItemDamage()));
 
 				if (item.hasTagCompound())
 				{
 					eItem.getEntityItem().setTagCompound((NBTTagCompound)item.getTagCompound().copy());
 				}
 
 				float var15 = 0.05F;
 				eItem.motionX = (double)((float)this.furnaceRand.nextGaussian() * var15);
 				eItem.motionY = (double)((float)this.furnaceRand.nextGaussian() * var15 + 0.2F);
 				eItem.motionZ = (double)((float)this.furnaceRand.nextGaussian() * var15);
 				eItem.delayBeforeCanPickup = 10;
 				worldObj.spawnEntityInWorld(eItem);
 
 			}
 
 			upgradeStacks[j] = null;
 		}
 
 	}
 	
 	@Override
 	public int[] getAccessibleSlotsFromSide(int var1) {
 		//Don't care, put and pull from every slot
 		int i[] = new int[getSizeInventory()];
 		for(int j = 0; j < i.length; j++)
 			i[j]=j;
 		return i;
 	}
 
 	@Override
 	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
 		return ((i < inputStacks.length) && getItemBurnTime(itemstack)>0);
 	}
 
 	@Override
 	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return (i>=inputStacks.length && i<getSizeInventory()-upgradeStacks.length);
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return inputStacks.length+1+upgradeStacks.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		if(slot < inputStacks.length)
 			return inputStacks[slot];
 		else if(slot < getSizeInventory()-upgradeStacks.length)
 			return outputStack;
 		else if(slot < getSizeInventory())
 			return upgradeStacks[slot-inputStacks.length-1];
 		else
 			return null;
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 		if(i>=0 && i<inputStacks.length)
 		{
 			if (inputStacks[i] != null)
 			{
 				ItemStack itemstack;
 
 				if (inputStacks[i].stackSize <= j)
 				{
 					itemstack = inputStacks[i];
 					this.inputStacks[i] = null;
 					return itemstack;
 				}
 				else
 				{
 					itemstack = this.inputStacks[i].splitStack(j);
 
 					if (this.inputStacks[i].stackSize == 0)
 					{
 						this.inputStacks[i] = null;
 					}
 
 					return itemstack;
 				}
 			}
 			else
 			{
 				return null;
 			}
 		}
 		else if(i<getSizeInventory()-upgradeStacks.length)
 		{
 			i = i-inputStacks.length;
 			if (outputStack != null)
 			{
 				ItemStack itemstack;
 
 				if (outputStack.stackSize <= j)
 				{
 					itemstack = outputStack;
 					this.outputStack = null;
 					return itemstack;
 				}
 				else
 				{
 					itemstack = this.outputStack.splitStack(j);
 
 					if (this.outputStack.stackSize == 0)
 					{
 						this.outputStack = null;
 					}
 
 					return itemstack;
 				}
 			}
 			else
 			{
 				return null;
 			}
 		}
 		else if(i<getSizeInventory())
 		{
 			i = i-inputStacks.length-1;
 			if (upgradeStacks[i] != null)
 			{
 				ItemStack itemstack;
 
 				if (upgradeStacks[i].stackSize <= j)
 				{
 					itemstack = upgradeStacks[i];
 					this.upgradeStacks[i] = null;
 					return itemstack;
 				}
 				else
 				{
 					itemstack = this.upgradeStacks[i].splitStack(j);
 
 					if (this.upgradeStacks[i].stackSize == 0)
 					{
 						this.upgradeStacks[i] = null;
 					}
 
 					return itemstack;
 				}
 			}
 			else
 			{
 				return null;
 			}
 		}		
 		else
 			return null;//outside the inventory
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		if(slot>=0 && slot < inputStacks.length)
 		{
 			if(inputStacks[slot] != null)
 			{
 				ItemStack var2 = inputStacks[slot];
 				inputStacks[slot] = null;
 				return var2;
 			}
 			return null;
 		}
 		else if(slot>= inputStacks.length && slot < this.getSizeInventory()-upgradeStacks.length)
 		{
 			if(outputStack != null)
 			{
 				ItemStack var2 = outputStack;
 				outputStack = null;
 				return var2;	
 			}
 			return null;
 		}
 		else if(slot < getSizeInventory())
 		{
 			if(upgradeStacks[slot-inputStacks.length-1] != null)
 			{
 				ItemStack var2 = upgradeStacks[slot-inputStacks.length-1];
 				upgradeStacks[slot-inputStacks.length-1] = null;
 				return var2;
 			}
 			return null;
 		}
 		else
 			return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 		if(i>=0 && i<inputStacks.length)
 		{
 			this.inputStacks[i] = itemstack;
 
 			if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
 			{
 				itemstack.stackSize = this.getInventoryStackLimit();
 			}
 		}
 		else if(i<getSizeInventory()-upgradeStacks.length)
 		{
 			this.outputStack = itemstack;
 
 			if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
 			{
 				itemstack.stackSize = this.getInventoryStackLimit();
 			}
 		}
 		else if(i<getSizeInventory())
 		{
 			this.upgradeStacks[i-inputStacks.length-1] = itemstack;
 
 			if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
 			{
 				itemstack.stackSize = this.getInventoryStackLimit();
 			}
 		}
 
 		this.onInventoryChanged();
 	}
 
 	@Override
 	public String getInvName() {
 		return "Fuel Cell Filler";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return false;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : entityplayer.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
 	}
 
 	@Override
 	public void openChest() {
 
 	}
 
 	@Override
 	public void closeChest() {
 	
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		// TODO Auto-generated method stub
 		return this.canInsertItem(i, itemstack, 0);
 	}
 
 }
 
 /*******************************************************************************
 * Copyright (c) 2014 Malorolam.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the included license, which is also
 * available at http://carbonization.wikispaces.com/License
 * 
 *********************************************************************************/
