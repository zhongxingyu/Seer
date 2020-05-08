 package squishyFriends.entity;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import squishyFriends.SlimeData;
 import squishyFriends.SquishyFriends;
 import cpw.mods.fml.common.FMLLog;
 
 public class TileEntitySlimePen extends TileEntity implements IInventory {
 	private ItemStack core;
 	private ItemStack[] trainingItems;
 	private ItemStack appearanceItem;
 	
 	private SlimeData data;
 	
 	private static final int coreSlot = 5;
 	private static final int appearanceSlot = 6;
 
 	public TileEntitySlimePen() {
 		trainingItems = new ItemStack[5];
 	}
 	
 	public ItemStack getCore() {
 		return core;
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return 7;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		switch (slot) {
 		case coreSlot:
 			return core;
 		case appearanceSlot:
 			return appearanceItem;
 		default:
 			return trainingItems[slot];
 		}
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int amount) {
 		ItemStack stack = getStackInSlot(slot);
 		
 		// When a core is removed, pull the items out along with it.
 		if (slot == coreSlot && stack.getItem() == SquishyFriends.slimeCore) {
 			transferItemsToCore();
 			
 			for (int i = 0; i < trainingItems.length; i++) {
 				setInventorySlotContents(i, null);
 			}
 			
 			setInventorySlotContents(coreSlot, null);
 		}
 		else if (stack != null) {
 			if (stack.stackSize <= amount) {
 				setInventorySlotContents(slot, null);
 			}
 			else {
 				stack = stack.splitStack(amount);
 				
 				if (stack.stackSize == 0) {
 					setInventorySlotContents(slot, null);
 				}
 			}
 		}
 		
 		return stack;
 	}
 	
 	private void transferItemsToCore() {
 		NBTTagList items = new NBTTagList();
 		for (int i = 0; i < trainingItems.length; i++) {
 			if (trainingItems[i] != null) {
 				NBTTagCompound item = new NBTTagCompound();
 				trainingItems[i].writeToNBT(item);
 				trainingItems[i] = null;
 				item.setByte("slot", (byte) i);
				items.appendTag(item);
 			}
 		}
 		
 		core.getTagCompound().setTag("items", items);
 	}
 	
 	private void loadItemsFromCore() {
 		NBTTagList items = core.getTagCompound().getTagList("items");
 		
 		for (int i = 0; i < items.tagCount(); i++) {
 			NBTTagCompound itemTag = (NBTTagCompound) items.tagAt(i);
 			int slot = (int) itemTag.getByte("slot");
 			trainingItems[slot] = ItemStack.loadItemStackFromNBT(itemTag);
 			setInventorySlotContents(slot, trainingItems[slot]);
 		}
 	}
	
	public boolean hasTrainingItem(ItemStack stack) {
		for (int i = 0; i < trainingItems.length; i++) {
			if (trainingItems[i] != null && trainingItems[i].getItem() == stack.getItem()) {
				return true;
			}
		}
		
		return false;
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
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		switch (slot) {
 		case coreSlot:
 			if (stack != null) {
 				core = stack;
 				data = new SlimeData(stack.getTagCompound());
 				loadItemsFromCore();
 			}
 			else {
 				core = null;
 			}
 			break;
 		case appearanceSlot:
 			appearanceItem = stack;
 			break;
 		default:
 			trainingItems[slot] = stack;
 			break;
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		return "SlimePenInventory";
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 1;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return true;
 	}
 
 	@Override
 	public void openChest() {
 
 	}
 
 	@Override
 	public void closeChest() {
 		// TODO Auto-generated method stub
 
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound tag) {
 		super.readFromNBT(tag);
 		
 		core = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("core"));
 		
 		if (core != null) {
 			data = new SlimeData(core.getTagCompound());
 		}
 		
 		appearanceItem = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("appearanceItem"));
 		
 		NBTTagList items = tag.getTagList("items");
 		
 		for (int i = 0; i < items.tagCount(); i++) {
 			NBTTagCompound itemTag = (NBTTagCompound) items.tagAt(i);
 			int slot = (int) itemTag.getByte("slot");
 			trainingItems[slot] = ItemStack.loadItemStackFromNBT(itemTag);
 		}
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound tag) {
 		super.writeToNBT(tag);
 		
 		if (core != null) {
 			tag.setCompoundTag("core", core.writeToNBT(new NBTTagCompound()));
 		}
 		
 		if (appearanceItem != null) {
 			tag.setCompoundTag("appearance", appearanceItem.writeToNBT(new NBTTagCompound()));
 		}
 		
 		NBTTagList items = new NBTTagList();
 		for (int i = 0; i < trainingItems.length; i++) {
 			if (trainingItems[i] != null) {
 				NBTTagCompound item = new NBTTagCompound();
 				trainingItems[i].writeToNBT(item);
 				item.setByte("slot", (byte) i);
				items.appendTag(item);
 			}
 		}
 		
 		tag.setTag("items", items);
 	}
 
 }
