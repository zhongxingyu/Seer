 package gaarnik.bsa.common.tileentity;
 
 import gaarnik.bsa.common.BSAMod;
 
 import gaarnik.bsa.common.block.BSABlocks;
 import gaarnik.bsa.common.block.EngElecMachBlock;
 import gaarnik.bsa.common.recipe.EngMachRecipe;
 import ic2.api.Direction;
 import ic2.api.tile.IWrenchable;
 import ic2.api.energy.event.EnergyTileUnloadEvent;
 import ic2.api.energy.tile.IEnergySink;
 import ic2.api.network.INetworkDataProvider;
 import ic2.api.network.INetworkTileEntityEventListener;
 import ic2.api.network.NetworkHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.MinecraftForge;
 
 public class EngElecMachTileEntity extends TileEntity implements ISidedInventory, IEnergySink, IWrenchable, INetworkDataProvider, INetworkTileEntityEventListener {
 	// *******************************************************************
 	public static final int MAX_ENERGY = 500;
 	public static final int MAX_PROCESS_TICKS = 50;
 
 	private static final int MAX_INPUT = 32;
 	private static final int ENERGY_CONSUME = 1;
 
 	// *******************************************************************
 	private ItemStack[] stacks = new ItemStack[5];
 
 	private int energyStored;
 
 	private int processTicks;
 	
 	private int metadata;
 
 	private boolean addedToNetwork;
 	private boolean active;
 	private boolean prevActive;
 
 	private static ArrayList<String> networkedFileds;
 
 	// *******************************************************************
 	public EngElecMachTileEntity() {
 		if(networkedFileds == null) {
 			networkedFileds = new ArrayList<String>();
 			networkedFileds.add("active");
 		}
 
 		this.addedToNetwork = false;
 		this.active = this.prevActive = false;
 
 		this.energyStored = 0;
 	}
 
 	// *******************************************************************
 	@Override
 	public void updateEntity() {
 		super.updateEntity();
 
 		if(this.addedToNetwork == false) {
 			BSAMod.proxy.addMachineToIc2Network(this);
 			this.addedToNetwork = true;
 		}
 
 		if(this.worldObj.isRemote)
 			return;
 
 		if(this.energyStored >= ENERGY_CONSUME && this.canProcess()) {
 			this.energyStored -= ENERGY_CONSUME;
 			this.processTicks++;
 
 			if(this.processTicks >= MAX_PROCESS_TICKS) {
 				this.smeltItem();
 				this.processTicks = 0;
 			}

			EngElecMachBlock.updateBlockState(true, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		}
		else {
			this.processTicks = 0;
			EngElecMachBlock.updateBlockState(false, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
 		}
 	}
 
 	@Override
 	public void invalidate() {
 		if (this.addedToNetwork) {
 			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
 			this.addedToNetwork = false;
 		}
 
 		super.invalidate();
 	}
 
 	@Override
 	public int injectEnergy(Direction directionFrom, int amount) {
 		if(amount > MAX_INPUT) {
 			if (!BSAMod.explodeMachineAt(worldObj, xCoord, yCoord, zCoord)) {
 				worldObj.createExplosion(null, xCoord, yCoord, zCoord, 2.0F, true);
 				//remove machine block (too resistant for explosion)
 				worldObj.setBlock(xCoord, yCoord, zCoord, 0);
 			}
 
 			invalidate();
 			return 0;
 		}
 
 		this.energyStored += amount;
 		int excess = 0;
 
 		if (this.energyStored > MAX_ENERGY) {
 			excess = this.energyStored - MAX_ENERGY;
 			this.energyStored = MAX_ENERGY;
 		}
 
 		return excess;
 	}
 
 	public void smeltItem() {
 		if (this.canProcess()) {
 			ItemStack stack = EngMachRecipe.smelting().getSmeltingResult(this.stacks[0]);
 
 			for(int slot=1;slot<=4;slot++) {
 				if(this.isSlotFull(slot, stack) == true)
 					continue;
 
 				boolean smelted = false;
 
 				if (this.stacks[slot] == null) {
 					this.stacks[slot] = stack.copy();
 					smelted = true;
 				}
 				else if (this.stacks[slot].isItemEqual(stack)) {
 					stacks[slot].stackSize += stack.stackSize;
 					smelted = true;
 				}
 
 				if(smelted == true) {
 					--this.stacks[0].stackSize;
 
 					if (this.stacks[0].stackSize <= 0)
 						this.stacks[0] = null;
 
 					return;
 				}
 			}
 		}
 	}
 
 	public ItemStack decrStackSize(int position, int count) {
 		if (this.stacks[position] != null) {
 			ItemStack stack;
 
 			if (this.stacks[position].stackSize <= count) {
 				stack = this.stacks[position];
 				this.stacks[position] = null;
 
 				if(position == 0) //if resource slot is empty reset processTicks
 					this.processTicks = 0; //TODO work but no gui update
 
 				return stack;
 			}
 			else {
 				stack = this.stacks[position].splitStack(count);
 
 				if (this.stacks[position].stackSize == 0)
 					this.stacks[position] = null;
 
 				return stack;
 			}
 		}
 		else
 			return null;
 	}
 
 	public ItemStack getStackInSlotOnClosing(int position) {
 		if (this.stacks[position] != null) {
 			ItemStack stack = this.stacks[position];
 			this.stacks[position] = null;
 
 			return stack;
 		}
 
 		return null;
 	}
 
 	public void setInventorySlotContents(int position, ItemStack stack) {
 		this.stacks[position] = stack;
 
 		if (stack != null && stack.stackSize > this.getInventoryStackLimit())
 			stack.stackSize = this.getInventoryStackLimit();
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 
 	public boolean isActive() {
 		return this.active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 
 		if(this.active != this.prevActive)
 			NetworkHelper.updateTileEntityField(this, "active");
 
 		this.prevActive = active;
 	}
 
 	// *******************************************************************
 	@Override
 	public void onNetworkEvent(int event) {}
 
 	@Override
 	public List<String> getNetworkedFields() {
 		return networkedFileds;
 	}
 
 	// *******************************************************************
 	@Override
 	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side) { return false; }
 
 	@Override
 	public short getFacing() { return 0; }
 
 	@Override
 	public void setFacing(short facing) {}
 
 	@Override
 	public boolean wrenchCanRemove(EntityPlayer player) {
 		return player.isSneaking();
 	}
 
 	@Override
 	public float getWrenchDropRate() { return 0.9f;	}
 
 	@Override
 	public ItemStack getWrenchDrop(EntityPlayer entityPlayer) {
 		return new ItemStack(BSABlocks.engElecMachBlock, 1);
 	}
 
 	// *******************************************************************
 	@Override
 	public void readFromNBT(NBTTagCompound tag) {
 		super.readFromNBT(tag);
 
 		NBTTagList var2 = tag.getTagList("Items1");
 		this.stacks = new ItemStack[this.getSizeInventory()];
 
 		for (int var3 = 0; var3 < var2.tagCount(); ++var3) {
 			NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
 			byte var5 = var4.getByte("Slot1");
 
 			if (var5 >= 0 && var5 < this.stacks.length)
 				this.stacks[var5] = ItemStack.loadItemStackFromNBT(var4);
 		}
 
 		this.energyStored = tag.getInteger("energyStored");
 		this.processTicks = tag.getInteger("processTicks");
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound tag) {
 		super.writeToNBT(tag);
 
 		NBTTagList var2 = new NBTTagList();
 
 		for (int var3 = 0; var3 < this.stacks.length; ++var3) {
 			if (this.stacks[var3] != null) {
 				NBTTagCompound var4 = new NBTTagCompound();
 				var4.setByte("Slot1", (byte)var3);
 				this.stacks[var3].writeToNBT(var4);
 				var2.appendTag(var4);
 			}
 		}
 
 		tag.setTag("Items1", var2);
 
 		tag.setInteger("energyStored", this.energyStored);
 		tag.setInteger("processTicks", this.processTicks);
 	}
 
 	// *******************************************************************
 	@Override
 	public int[] getAccessibleSlotsFromSide(int side) {
 		this.metadata = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
 		int[] slots = null;
 		
 		if(side == 0) { //bottom
 			slots = new int[4];
 			//output slots
 			slots[0] = 1;
 			slots[1] = 2;
 			slots[2] = 3;
 			slots[3] = 4;
 		}
 		else if(side == this.metadata) { //front
 			slots = new int[4];
 			//output slots
 			slots[0] = 1;
 			slots[1] = 2;
 			slots[2] = 3;
 			slots[3] = 4;
 		}
 		else {
 			slots = new int[1];
 			//input slot
 			slots[0] = 0;
 		}
 		
 		return slots;
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
 		switch(slot) {
 		
 		case 0:
 			if (EngMachRecipe.smelting().getSmeltingResult(itemstack) != null)
 				return true;
 			break;
 			
 		}
 		
 		return false;
 	}
 
 	@Override
 	public boolean canInsertItem(int slot, ItemStack stack, int side) {
 		if(side != this.metadata)
 			return EngMachRecipe.smelting().getSmeltingResult(stack) != null ? true: false;
 		
 		return false;
 	}
 
 	@Override
 	public boolean canExtractItem(int slot, ItemStack stack, int side) {
 		if(side != 1 && (slot == 1 || slot == 2 || slot == 3 || slot == 4))
 			return true;
 		
 		return false;
 	}
 
 	// *******************************************************************
 	private boolean canProcess() {
 		if (this.stacks[0] == null)
 			return false;
 
 		ItemStack stack = EngMachRecipe.smelting().getSmeltingResult(this.stacks[0]);
 
 		if (stack == null) return false;
 
 		for(int i=1;i<=4;i++)
 			if(this.isSlotFull(i, stack) == false)
 				return true;
 
 		return false;
 	}
 
 	private boolean isSlotFull(int slot, ItemStack stack) {
 		if (this.stacks[slot] == null) return false;
 		if (!this.stacks[slot].isItemEqual(stack)) return false;
 
 		int result = stacks[slot].stackSize + stack.stackSize;
 		return !(result <= getInventoryStackLimit() && result <= stack.getMaxStackSize());
 	}
 
 	// *******************************************************************
 	@Override
 	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
 		return true;
 	}
 
 	@Override
 	public boolean isAddedToEnergyNet() { return this.addedToNetwork; }
 
 	@Override
 	public int demandsEnergy() { return MAX_ENERGY - this.energyStored; }
 
 	@Override
 	public int getMaxSafeInput() { return MAX_INPUT; }
 
 	public int getEnergyStored() { return this.energyStored; }
 	public void setEnergyStored(int stored) { this.energyStored = stored; }
 
 	public int getProcessTicks() { return this.processTicks; }
 	public void setProcessTicks(int ticks) { this.processTicks = ticks; }
 
 	public int getStartInventorySide(ForgeDirection side) {
 		if(side == ForgeDirection.EAST)
 			return 4;
 		
 		return 0;
 	}
 
 	public int getSizeInventorySide(ForgeDirection side) { return 1; }
 
 	@Override
 	public int getSizeInventory() { return this.stacks.length; }
 
 	@Override
 	public ItemStack getStackInSlot(int slot) { return this.stacks[slot]; }
 
 	@Override
 	public String getInvName() { return "container.EngElecMach"; }
 
 	@Override
 	public int getInventoryStackLimit() { return 64; }
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer var1) { return true; }
 
 	@Override
 	public boolean isInvNameLocalized() { return false; }
 
 }
