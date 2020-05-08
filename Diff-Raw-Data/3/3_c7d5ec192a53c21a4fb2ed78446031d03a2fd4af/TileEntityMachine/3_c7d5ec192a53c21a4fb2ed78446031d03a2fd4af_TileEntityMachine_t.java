 package ip.industrialProcessing.machines;
 
 import ip.industrialProcessing.recipes.IRecipeWorkHandler;
 import ip.industrialProcessing.recipes.RecipeWorker;
 import ip.industrialProcessing.utils.inventories.InventoryUtils;
 import ip.industrialProcessing.utils.working.IWorkHandler;
 import ip.industrialProcessing.utils.working.Worker;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 
 public abstract class TileEntityMachine extends TileEntity implements
 		ISidedInventory, IWorkHandler, IMachineSlots, IRecipeWorkHandler {
 
 	private ArrayList<MachineItemStack> itemStacks = new ArrayList<MachineItemStack>();
 	private int[][] itemStackSideSlots = new int[6][0];
 	private RecipeWorker recipeWorker;
 
 	public TileEntityMachine() {
 		this.recipeWorker = new RecipeWorker(this);
 		setWorker(this.recipeWorker);
 	}
 
 	protected Worker worker;
 
 	public Worker getWorker() {
 		return worker;
 	}
 
 	@Override
 	public void updateEntity() {
 		work();
 	}
 
 	@Override
 	public boolean canUpdate() {
 		return true;
 	}
 
 	protected void work() {
 		worker.doWork(1, this.worldObj.isRemote);
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
 		worker.writeToNBT(nbt);
 		writeInventory(this, nbt);
 	};
 
 	private void writeInventory(TileEntityMachine tileEntityMachine,
 			NBTTagCompound nbt) {
 		NBTTagList nbttaglist = new NBTTagList();
 		for (int i = 0; i < this.itemStacks.size(); ++i) {
 			MachineItemStack machineStack = this.itemStacks.get(i);
 			if (machineStack.stack != null) {
 				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
 				nbttagcompound1.setByte("Slot", (byte) i);
 				machineStack.stack.writeToNBT(nbttagcompound1);
 				nbttaglist.appendTag(nbttagcompound1);
 			}
 		}
 		nbt.setTag("Items", nbttaglist);
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
 		worker.readFromNBT(nbt);
 		readInventory(this, nbt);
 	};
 
 	private void readInventory(TileEntityMachine tileEntityMachine,
 			NBTTagCompound nbt) {
 		NBTTagList nbttaglist = nbt.getTagList("Items");
 		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
 			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist
 					.tagAt(i);
 			byte b0 = nbttagcompound1.getByte("Slot");
 
 			if (b0 >= 0 && b0 < itemStacks.size()) {
 				MachineItemStack machineStack = this.itemStacks.get(b0);
 				machineStack.stack = ItemStack
 						.loadItemStackFromNBT(nbttagcompound1);
 			}
 		}
 	}
 
 	@Override
 	public boolean removeFromSlot(int slot, int itemId, int amount) {
 		if (slotContains(slot, itemId, amount)) {
 			MachineItemStack machineStack = itemStacks.get(slot);
 			machineStack.stack.stackSize -= amount;
 			if (machineStack.stack.stackSize == 0)
 				machineStack.stack = null;
 			onInventoryChanged();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean addToSlot(int slot, int itemId, int amount) {
 		if (slotHasRoomFor(slot, itemId, amount)) {
 			MachineItemStack machineStack = itemStacks.get(slot);
 			if (machineStack.stack == null)
 				machineStack.stack = new ItemStack(itemId, amount, 0);
 			else
 				machineStack.stack.stackSize += amount;
 			onInventoryChanged();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean slotContains(int slot, int itemId, int amount) {
 		MachineItemStack machineStack = itemStacks.get(slot);
 		return machineStack != null && machineStack.stack != null
 				&& machineStack.stack.itemID == itemId
 				&& machineStack.stack.stackSize >= amount;
 	}
 
 	@Override
 	public boolean slotHasRoomFor(int slot, ItemStack stack) {
 		if (stack == null || stack.stackSize == 0)
 			return true;
 		MachineItemStack machineStack = itemStacks.get(slot);
 		return machineStack != null
 				&& (machineStack.stack == null || (machineStack.stack.itemID == stack.itemID && (machineStack.stack.stackSize
 						+ stack.stackSize < getInventoryStackLimit())));
 	}
 
 	@Override
 	public boolean slotHasRoomFor(int slot, int itemId, int amount) {
 		if (amount == 0)
 			return true;
 		MachineItemStack machineStack = itemStacks.get(slot);
 		return machineStack != null
 				&& (machineStack.stack == null || (machineStack.stack.itemID == itemId && (machineStack.stack.stackSize
 						+ amount < getInventoryStackLimit())));
 	}
 
 	protected void addStack(ItemStack stack, ForgeDirection side,
 			boolean input, boolean output) {
 		int index = itemStacks.size();
 
 		itemStacks.add(new MachineItemStack(stack,
 				new int[] { side.ordinal() }, input, output));
 
 		int sideIndex = side.ordinal();
 		int[] slots = itemStackSideSlots[sideIndex];
 		int[] newSlots = new int[slots.length + 1];
 		System.arraycopy(slots, 0, newSlots, 0, slots.length);
 		newSlots[slots.length] = index;
 		itemStackSideSlots[sideIndex] = newSlots;
 	}
 
 	protected void addStack(ItemStack stack, ForgeDirection[] sides,
 			boolean input, boolean output) {
 		int index = itemStacks.size();
 
 		int[] sideIndices = new int[sides.length];
 		for (int i = 0; i < sideIndices.length; i++)
 			sideIndices[i] = sides[i].ordinal();
 
 		itemStacks.add(new MachineItemStack(stack, sideIndices, input, output));
 
 		for (int i = 0; i < sideIndices.length; i++) {
 			int sideIndex = sideIndices[i];
 			int[] slots = itemStackSideSlots[sideIndex];
 			int[] newSlots = new int[slots.length + 1];
 			System.arraycopy(slots, 0, newSlots, 0, slots.length);
 			newSlots[slots.length] = index;
 			itemStackSideSlots[sideIndex] = newSlots;
 		}
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return itemStacks.size();
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		MachineItemStack machineStack = getMachineStack(i);
 		if (machineStack == null)
 			return null;
 		return machineStack.stack;
 	}
 
 	private MachineItemStack getMachineStack(int i) {
 		if (i < 0 || i >= this.itemStacks.size())
 			return null;
 		return this.itemStacks.get(i);
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 		MachineItemStack machineStack = getMachineStack(i);
 		if (machineStack == null)
 			return null;
 
 		ItemStack stack = machineStack.stack;
 		if (stack == null)
 			return null;
 
 		if (stack.stackSize > j) {
 			stack = stack.splitStack(j);
 
 			onInventoryChanged();
 			return stack;
 		}
 		machineStack.stack = null;
 		onInventoryChanged();
 		return stack;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		ItemStack stack = getStackInSlot(slot);
 		if (stack != null) {
 			setInventorySlotContents(slot, null);
 		}
 		return stack;
 	}
 
 	@Override
 	public void setInventorySlotContents(int slotIndex, ItemStack stack) {
 		if (slotIndex > getSizeInventory())
 			return;
 
 		MachineItemStack machineStack = getMachineStack(slotIndex);
 		if (machineStack != null) {
 			machineStack.stack = stack;
 			if (stack != null && stack.stackSize > getInventoryStackLimit()) {
 				stack.stackSize = getInventoryStackLimit();
 			}
 			onInventoryChanged();
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		return "Filter";
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
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
 				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5,
 						zCoord + 0.5) < 64;
 	}
 
 	@Override
 	public void openChest() {
 	}
 
 	@Override
 	public void closeChest() {
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		MachineItemStack stack = getMachineStack(i);
 		if (stack == null || itemstack == null)
 			return false;
 		return stack.input && isValidInput(i, itemstack.itemID);
 	}
 
 	protected abstract boolean isValidInput(int slot, int itemID);
 
 	@Override
 	public int[] getAccessibleSlotsFromSide(int var1) {
 		return itemStackSideSlots[var1];
 	}
 
 	@Override
 	public boolean canInsertItem(int slotIndex, ItemStack itemstack, int amount) {
 		MachineItemStack machineStack = getMachineStack(slotIndex);
 		if (machineStack != null && machineStack.input) {
 			return isItemValidForSlot(slotIndex, itemstack);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean canExtractItem(int slotIndex, ItemStack itemstack, int amount) {
 		MachineItemStack machineStack = getMachineStack(slotIndex);
 		if (machineStack != null && machineStack.output) {
 			return true;
 		}
 		return false;
 	}
 
 	public void setWorker(Worker recipeWorker) {
 		this.worker = recipeWorker;
 	}
 
 	public int getScaledProgress(int i) {
 		return this.worker.getProgress() * i / 100;
 	}
 
 }
