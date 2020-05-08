 package jack_1197.swag.common;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class FuserTileEntity extends TileEntity implements IInventory {
 	private ItemStack[] inventory = new ItemStack[3];
 	public int progress = 0;
 	public static int maxProgress = 600;
 	public int direction = 0;// 0 = infuse, 1 = defuse
 	private static int updateInterval = 5;
 	private int update = 5;
 	public boolean processing = false;
 	private int currentlyProcessing = -1;
 
 	private static ItemStack[][] fuseList = {
 			{
 					new ItemStack(Block.stone),
 					new ItemStack(SwagMod.swagDropItem),
 					new ItemStack(SwagMod.swagOreBlock) },
 			{
 					new ItemStack(Block.stone),
 					new ItemStack(SwagMod.yoloEssenceDenseItem),
 					new ItemStack(SwagMod.yoloOreBlock) },
 			{
 					new ItemStack(Item.pickaxeGold),
 					new ItemStack(SwagMod.swagDropItem, 3),
 					new ItemStack(SwagMod.swagPickaxeItem) },
 			{
 					new ItemStack(Item.axeGold),
 					new ItemStack(SwagMod.swagDropItem, 3),
 					new ItemStack(SwagMod.swagAxeItem) },
 			{
 					new ItemStack(Item.shovelGold),
 					new ItemStack(SwagMod.swagDropItem, 3),
 					new ItemStack(SwagMod.swagShovelItem) },
 			{
 					new ItemStack(Item.swordGold),
 					new ItemStack(SwagMod.swagDropItem, 3),
 					new ItemStack(SwagMod.swagSwordItem) },
 			{
 					new ItemStack(Item.hoeGold),
 					new ItemStack(SwagMod.swagDropItem, 3),
 					new ItemStack(SwagMod.swagHoeItem) },
 			{
 					new ItemStack(Item.pickaxeGold),
 					new ItemStack(SwagMod.yoloOrbItem, 1),
 					new ItemStack(SwagMod.yoloPickaxeItem) },
 			{
 					new ItemStack(Item.axeGold),
 					new ItemStack(SwagMod.yoloOrbItem, 1),
 					new ItemStack(SwagMod.yoloAxeItem) },
 			{
 					new ItemStack(Item.shovelGold),
 					new ItemStack(SwagMod.yoloOrbItem, 1),
 					new ItemStack(SwagMod.yoloShovelItem) },
 			{
 					new ItemStack(Item.swordGold),
 					new ItemStack(SwagMod.yoloOrbItem, 1),
 					new ItemStack(SwagMod.yoloSwordItem) },
 			{
 					new ItemStack(Item.hoeGold),
 					new ItemStack(SwagMod.yoloOrbItem, 1),
 					new ItemStack(SwagMod.yoloHoeItem) },
 			{
 					new ItemStack(SwagMod.swagOrbItem),
 					new ItemStack(Item.ingotIron, 1),
 					new ItemStack(SwagMod.swagIngotItem) },
			{
					new ItemStack(SwagMod.yoloDropItem),
 					new ItemStack(Item.ingotIron, 1),
 					new ItemStack(SwagMod.yoloIngotItem) }, };
 
 	private boolean canProcess() {
 
 		for (int i = 0; i < fuseList.length; i++) {
 			if (direction == 0) {
 				int acceptable = 0;
 				// need a few fors and ifs to account for items but it the other way etc
 				// could probably be done in one messy big if(a && b && c && ...)
 				// EDIT: no longer works both ways untill i can be bothered fixing an isues, patched for now
 				for (int j = 0; j < 2; j++) {
 					for (int k = 0; k < 2; k++) {
 						if (getStackInSlot(k) != null && fuseList[i][j].getItem() == getStackInSlot(k).getItem() && fuseList[i][j].stackSize <= getStackInSlot(k).stackSize) {
 							// ugly patch fix
 							if (k == j) {
 								acceptable++;
 								break;
 							}
 						}
 					}
 				}
 				if (acceptable == 2)// shouldnt be any other case that is acceptable
 				{
 					if (getStackInSlot(2) != null && getStackInSlot(2).getItem() == fuseList[i][2].getItem()
 							&& fuseList[i][2].stackSize + getStackInSlot(2).stackSize > fuseList[i][2].getMaxStackSize()) {
 						currentlyProcessing = -1;
 						return false;
 					} else if (getStackInSlot(2) != null && getStackInSlot(2).getItem() != fuseList[i][2].getItem()) {
 						currentlyProcessing = -1;
 						return false;
 					}
 					currentlyProcessing = i;
 					return true;
 				}
 			} else {
 				if (getStackInSlot(2) != null && getStackInSlot(2).getItem() == fuseList[i][2].getItem() && fuseList[i][2].stackSize <= getStackInSlot(2).stackSize) {
 					if (getStackInSlot(0) != null && getStackInSlot(0).getItem() == fuseList[i][0].getItem()
 							&& fuseList[i][0].stackSize + getStackInSlot(0).stackSize > fuseList[i][0].getMaxStackSize()) {
 						currentlyProcessing = -1;
 						return false;
 					} else if (!(getStackInSlot(0) == null || getStackInSlot(0).getItem() == fuseList[i][0].getItem() || getStackInSlot(0).stackSize == 0)) {
 						currentlyProcessing = -1;
 						return false;
 					}
 					if (getStackInSlot(1) != null && getStackInSlot(1).getItem() == fuseList[i][1].getItem()
 							&& fuseList[i][1].stackSize + getStackInSlot(1).stackSize > fuseList[i][1].getMaxStackSize()) {
 						currentlyProcessing = -1;
 						return false;
 					} else if (!(getStackInSlot(1) == null || getStackInSlot(1).getItem() == fuseList[i][1].getItem() || getStackInSlot(1).stackSize == 0)) {
 						currentlyProcessing = -1;
 						return false;
 					}
 					currentlyProcessing = i;
 					return true;
 				}
 			}
 		}
 		currentlyProcessing = -1;
 		return false;
 	}
 
 	@Override
 	public void closeChest() {
 	}
 
 	@Override
 	public ItemStack decrStackSize(int index, int amount) {
 		ItemStack stack = getStackInSlot(index);
 		if (stack != null) {
 			if (stack.stackSize <= amount) {
 				setInventorySlotContents(index, null);
 			} else {
 				stack = stack.splitStack(amount);
 				if (stack.stackSize == 0) {
 					setInventorySlotContents(index, null);
 				}
 			}
 		}
 		return stack;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public String getInvName() {
 		return "Fuser";
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return inventory.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		return inventory[slot];
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
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
 	}
 
 	void onDone() {
 		if (currentlyProcessing != -1) {
 			if (direction == 0) {
 				if (fuseList[currentlyProcessing][0].stackSize <= getStackInSlot(0).stackSize) {
 					decrStackSize(0, fuseList[currentlyProcessing][0].stackSize);
 				} else {
 					inventory[0] = null;
 				}
 				if (fuseList[currentlyProcessing][1].stackSize <= getStackInSlot(1).stackSize) {
 					decrStackSize(1, fuseList[currentlyProcessing][1].stackSize);
 				} else {
 					inventory[1] = null;
 				}
 
 				if (getStackInSlot(2) != null && getStackInSlot(2).stackSize > 0) {
 					inventory[2].stackSize = inventory[2].stackSize + fuseList[currentlyProcessing][2].stackSize;
 				} else {
 					inventory[2] = fuseList[currentlyProcessing][2];
 				}
 			} else {
 				if (getStackInSlot(0) != null && getStackInSlot(0).stackSize > 0) {
 					inventory[0].stackSize = inventory[0].stackSize + fuseList[currentlyProcessing][0].stackSize;
 				} else {
 					inventory[0] = fuseList[currentlyProcessing][0];
 				}
 				if (getStackInSlot(1) != null && getStackInSlot(1).stackSize > 0) {
 					inventory[1].stackSize = inventory[1].stackSize + fuseList[currentlyProcessing][1].stackSize;
 				} else {
 					inventory[1] = fuseList[currentlyProcessing][1];
 				}
 				if (fuseList[currentlyProcessing][2].stackSize <= getStackInSlot(2).stackSize) {
 					decrStackSize(2, fuseList[currentlyProcessing][2].stackSize);
 				} else {
 					inventory[2] = null;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void openChest() {
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound tagCompound) {
 		super.readFromNBT(tagCompound);
 		// get inventory
 		NBTTagList tagList = tagCompound.getTagList("Inventory");
 		for (int i = 0; i < tagList.tagCount(); i++) {
 			NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
 			byte slot = tag.getByte("Slot");
 			if (slot >= 0 && slot < inventory.length) {
 				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
 			}
 		}
 		// get stats
 		progress = tagCompound.getInteger("progress");
 	}
 
 	public void sendUpdatePackets() {
 		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(28);
 		DataOutputStream dataOutputStream = new DataOutputStream(byteOutputStream);
 		try {
 			dataOutputStream.writeInt(SwagMod.packetType.FUSER_UPDATE.ordinal());
 			dataOutputStream.writeInt(xCoord);
 			dataOutputStream.writeInt(yCoord);
 			dataOutputStream.writeInt(zCoord);
 			dataOutputStream.writeInt(progress);
 			dataOutputStream.writeInt(direction);
 			dataOutputStream.writeBoolean(processing);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Packet250CustomPayload packet = new Packet250CustomPayload();
 		packet.channel = "SwagMod";
 		packet.data = byteOutputStream.toByteArray();
 		packet.length = byteOutputStream.size();
 		PacketDispatcher.sendPacketToAllPlayers(packet);
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		inventory[slot] = stack;
 	}
 
 	@Override
 	public void updateEntity() {
 		if (!this.worldObj.isRemote && update == 0) {
 			update = updateInterval;
 			sendUpdatePackets();
 		} else {
 			update--;
 		}
 		if (canProcess()) {
 			if (++progress > maxProgress) {
 				progress = 0;
 				onDone();
 			}
 		} else {
 			progress = 0;
 		}
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound) {
 		NBTTagList tagList = new NBTTagList();
 		// write inventory
 		for (int i = 0; i < inventory.length; i++) {
 			ItemStack stack = inventory[i];
 			if (stack != null) {
 				NBTTagCompound tag = new NBTTagCompound();
 				tag.setByte("Slot", (byte) i);
 				stack.writeToNBT(tag);
 				tagList.appendTag(tag);
 			}
 		}
 		// write stats
 		tagCompound.setTag("Inventory", tagList);
 		tagCompound.setInteger("progress", progress);
 		tagCompound.setString("id", "fuser");
 		tagCompound.setInteger("x", this.xCoord);
 		tagCompound.setInteger("y", this.yCoord);
 		tagCompound.setInteger("z", this.zCoord);
 	}
 }
