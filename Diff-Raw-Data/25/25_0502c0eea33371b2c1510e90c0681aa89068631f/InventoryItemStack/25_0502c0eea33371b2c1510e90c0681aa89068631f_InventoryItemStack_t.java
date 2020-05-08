 package demonmodders.Crymod.Common.Inventory;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagList;
 
 public abstract class InventoryItemStack implements IInventory {
 
 	ItemStack[] stacks = new ItemStack[getSizeInventory()];
 	
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	private final ItemStack theStack;
	private final ItemStack originalStack;
 	private final EntityPlayer player;
 		
 	public InventoryItemStack(ItemStack theStack, EntityPlayer player) {
 		this.theStack = theStack;
		originalStack = theStack.copy();
 		this.player = player;
 	}
 	
 	public InventoryItemStack(ItemStack theStack) {
 		this(theStack, null);
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		return stacks[slot];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int numDecrease) {
 		ItemStack stack = getStackInSlot(slot);
 		
 		if (stack != null) {
             ItemStack returnStack;
 
             if (stack.stackSize <= numDecrease) {
                 returnStack = stack;
                 setInventorySlotContents(slot, null);
                 return returnStack;
             } else {
                 returnStack = stack.splitStack(numDecrease);
 
                 if (stack.stackSize == 0) {
                     setInventorySlotContents(slot, null);
                 }
                 
                 return returnStack;
             }
         } else {
             return null;
         }
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		return getStackInSlot(slot);
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		stacks[slot] = stack;
 		onInventoryChanged();
 	}
 	
 	@Override
 	public void onInventoryChanged() {	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		if (this.player == null) {
 			return true;
 		} else if (this.player != player) {
 			return false;
 		} else {
			return ItemStack.areItemStacksEqual(player.inventory.getCurrentItem(), originalStack);
 		}
 	}
 
 	@Override
 	public void openChest() {
 		if (theStack.stackTagCompound == null) {
 			theStack.stackTagCompound = new NBTTagCompound();
 		}
 		
 		NBTTagList slotList = theStack.stackTagCompound.getTagList("slots");
 		for (int i = 0; i < slotList.tagCount(); i++) {
 			NBTTagCompound slotCompound = (NBTTagCompound)slotList.tagAt(i);
 			int slot = slotCompound.getInteger("slot");
 			stacks[slot] = ItemStack.loadItemStackFromNBT(slotCompound);
 		}
 	}
 
 	@Override
 	public void closeChest() {
 		NBTTagList slotList = new NBTTagList();
 		for (int i = 0; i < stacks.length; i++) {
 			if (stacks[i] != null) {
 				NBTTagCompound slotCompound = new NBTTagCompound();
 				slotCompound.setInteger("slot", i);
 				stacks[i].writeToNBT(slotCompound);
 				slotList.appendTag(slotCompound);
 			}
 		}
 		theStack.stackTagCompound.setTag("slots", slotList);
 		saveItemStack();
 	}
 	
 	void saveItemStack() {
 		if (player != null && isUseableByPlayer(player)) {
 			player.inventory.setInventorySlotContents(player.inventory.currentItem, theStack);
 		}
 	}
 }
