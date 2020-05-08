 /**
  * Copyright (c) Beliar, 2012
  * https://github.com/Beliaar/Butchery
  *
  * Butchery is distributed under the terms of the Minecraft Mod Public
  * License 1.0, or MMPL. Please check the contents of the license located in
  * https://github.com/Beliaar/Butchery/wiki/License
  */
 package butchery.common.blocks;
 
 import butchery.api.ITubWaterModifier;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagList;
 import net.minecraft.src.NetworkManager;
 import net.minecraft.src.Packet;
 import net.minecraft.src.Packet132TileEntityData;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.Vec3;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldClient;
 import net.minecraft.src.WorldServer;
 
 public class TileEntityTub extends TileEntity implements IInventory {
 
 	public int waterLevel = 0;
 	public float soakProgress = 0.0F;
 	public int currentSoakTime = 0;
 
 	private ItemStack[] tubItemStacks;
 
 	public TileEntityTub() {
 		this.tubItemStacks = new ItemStack[3];
 	}
 
 	public boolean processActivate(EntityPlayer player, World world,
 			ItemStack currentItem) {
 		if (world.isRemote) {
 			return true;
 		}
 		if (currentItem.itemID == Item.bucketWater.shiftedIndex) {
 			if (this.waterLevel < 100) {
 				if (!player.capabilities.isCreativeMode) {
 					player.inventory.setInventorySlotContents(
 							player.inventory.currentItem, new ItemStack(
 									Item.bucketEmpty));
 				}
 				this.waterLevel = 100;
 				world.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord,
 						0);
 				sendUpdateToWatchingPlayers(world, this.xCoord, this.zCoord);
 			}
 
 			return true;
 		}
 		return false;
 	}
 
 	public void fillWithRain(World world, int par2, int par3, int par4) {
 		if (world.isRemote) {
 			return;
 		}
 		if (this.waterLevel < 100) {
 			this.waterLevel += 1;
 			checkWaterLevel();
 			world.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, 0);
 			sendUpdateToWatchingPlayers(world, par2, par4);
 		}
 	}
 
 	private void sendUpdateToWatchingPlayers(World world, int par2, int par4) {
 		if (world.isRemote) {
 			return;
 		}
 		WorldServer serverWorld = (WorldServer) world;
 		for (Object player_obj : world.playerEntities) {
 			EntityPlayerMP player = (EntityPlayerMP) player_obj;
 			if (serverWorld.getPlayerManager().isPlayerWatchingChunk(player,
 					par2 >> 4, par4 >> 4)) {
 				player.serverForThisPlayer
 						.sendPacketToPlayer(getAuxillaryInfoPacket());
 			}
 		}
 	}
 
 	public void checkWaterLevel() {
 		if (this.waterLevel >= 100) {
 			this.waterLevel = 100;
 		}
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbt) {
 		super.readFromNBT(nbt);
 		NBTTagList var2 = nbt.getTagList("Items");
 		this.tubItemStacks = new ItemStack[this.getSizeInventory()];
 
 		for (int var3 = 0; var3 < var2.tagCount(); ++var3) {
 			NBTTagCompound var4 = (NBTTagCompound) var2.tagAt(var3);
 			byte var5 = var4.getByte("Slot");
 
 			if (var5 >= 0 && var5 < this.tubItemStacks.length) {
 				this.tubItemStacks[var5] = ItemStack.loadItemStackFromNBT(var4);
 			}
 		}
 		this.waterLevel = nbt.getInteger("waterLevel");
 		this.soakProgress = nbt.getFloat("soakProgress");
 		this.currentSoakTime = nbt.getInteger("currentSoakTime");
 		checkWaterLevel();
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbt) {
 		super.writeToNBT(nbt);
 		checkWaterLevel();
 		nbt.setInteger("waterLevel", this.waterLevel);
 		NBTTagList var2 = new NBTTagList();
 
 		for (int var3 = 0; var3 < this.tubItemStacks.length; ++var3) {
 			if (this.tubItemStacks[var3] != null) {
 				NBTTagCompound var4 = new NBTTagCompound();
 				var4.setByte("Slot", (byte) var3);
 				this.tubItemStacks[var3].writeToNBT(var4);
 				var2.appendTag(var4);
 			}
 		}
 
 		nbt.setTag("Items", var2);
 		nbt.setFloat("soakProgress", this.soakProgress);
 		nbt.setInteger("currentSoakTime", this.currentSoakTime);
 	}
 
 	@Override
 	public Packet getAuxillaryInfoPacket() {
 		NBTTagCompound tag = new NBTTagCompound();
 		this.writeToNBT(tag);
 		return new Packet132TileEntityData(this.xCoord, this.yCoord,
 				this.zCoord, 1, tag);
 	}
 
 	@Override
 	public void onDataPacket(NetworkManager net, Packet132TileEntityData packet) {
 		NBTTagCompound tag = packet.customParam1;
 		this.waterLevel = tag.getInteger("waterLevel");
 		this.soakProgress = tag.getFloat("soakProgress");
 		this.currentSoakTime = tag.getInteger("currentSoakTime");
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return this.tubItemStacks.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		return this.tubItemStacks[slot];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int amount) {
 		ItemStack stack = getStackInSlot(slot);
 		if (stack != null) {
 			if (stack.stackSize <= amount) {
 				setInventorySlotContents(slot, null);
 			} else {
 				stack = stack.splitStack(amount);
 				if (stack.stackSize == 0) {
 					setInventorySlotContents(amount, null);
 				}
 			}
 		}
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
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		tubItemStacks[slot] = stack;
 		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
 			stack.stackSize = getInventoryStackLimit();
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		return "butchery.container.tub";
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
 				&& player.getDistanceSq(xCoord + 0.5, yCoord + 0.5,
 						zCoord + 0.5) < 64;
 	}
 
 	@Override
 	public void openChest() {
 
 	}
 
 	@Override
 	public void closeChest() {
 	}
 
 	@SideOnly(Side.CLIENT)
 	/**
 	 * Returns an integer between 0 and the passed value representing the 
 	 * current water level.
 	 */
 	public int getWaterLevelScaled(int max) {
 		return this.waterLevel * max / 100;
 	}
 
 	@SideOnly(Side.CLIENT)
 	/**
 	 * Returns an integer between 0 and the passed value representing how close
 	 * the current item is to being completely soaked.
 	 */
 	public int getSoakProgressScaled(int max) {
 		return (int) (this.soakProgress * max);
 	}
 
 	public boolean isSoaking() {
 		return this.currentSoakTime > 0;
 	}
 
 	/**
 	 * Allows the entity to update its state. Overridden in most subclasses,
 	 * e.g. the mob spawner uses this to count ticks and creates a new spawn
 	 * inside its implementation.
 	 */
 	@Override
 	public void updateEntity() {
 
 		boolean inventoryChanged = false;
 
 		if (!canSoak()) {
 			this.currentSoakTime = 0;
 			this.soakProgress = 0;
 		}
 		if (this.currentSoakTime > 0) {
 			--this.currentSoakTime;
 			ITubWaterModifier modifier;
 			modifier = (ITubWaterModifier) this.tubItemStacks[0].getItem();
 			int ticksNeeded = modifier.getTicksNeeded(this.tubItemStacks[1]);
 			int ticksPassed = ticksNeeded - this.currentSoakTime;
 			this.soakProgress = (float) ticksPassed / (float) ticksNeeded;
			inventoryChanged = true;
 		}
 
 		if (!this.worldObj.isRemote) {
 			if (this.currentSoakTime == 0 && canSoak()) {
 				ITubWaterModifier modifier;
 				modifier = (ITubWaterModifier) this.tubItemStacks[0].getItem();
 				this.currentSoakTime = modifier
 						.getTicksNeeded(this.tubItemStacks[1]);
 				this.soakProgress = 0.0F;
 				inventoryChanged = true;
 			}
 			if (this.currentSoakTime == 1) {
 				soakItem();
 				this.soakProgress = 0.0F;
 				inventoryChanged = true;
 			}
 		}
 
 		if (inventoryChanged) {
 			this.onInventoryChanged();
 			sendUpdateToWatchingPlayers(this.worldObj, this.xCoord, this.zCoord);
 		}
 	}
 
 	/**
 	 * Returns true if an item can be soaked in the tub with the current
 	 * modifier
 	 * 
 	 */
 	public boolean canSoak() {
 		if (this.tubItemStacks[0] == null) {
 			return false;
 		}
 		if (this.tubItemStacks[1] == null) {
 			return false;
 		}
 		if (this.tubItemStacks[0].getItem() instanceof ITubWaterModifier) {
 			ITubWaterModifier modifier;
 			modifier = (ITubWaterModifier) this.tubItemStacks[0].getItem();
 			if (this.waterLevel < modifier.getWaterUsage(this.tubItemStacks[1])) {
 				return false;
 			}
 			ItemStack output = modifier.getOutput(this.tubItemStacks[1]);
 			if (output == null) {
 				return false;
 			}
 			if (this.tubItemStacks[2] == null) {
 				return true;
 			}
 			if (!this.tubItemStacks[2].isItemEqual(output)) {
 				return false;
 			}
 			int result = this.tubItemStacks[2].stackSize + output.stackSize;
 			return (result < getInventoryStackLimit() && result < output
 					.getMaxStackSize());
 		}
 		return false;
 	}
 
 	/**
 	 * Transform items from the input stack into the output items
 	 */
 	public void soakItem() {
 		if (canSoak()) {
 			ITubWaterModifier modifier;
 			modifier = (ITubWaterModifier) this.tubItemStacks[0].getItem();
 			ItemStack output = modifier.getOutput(this.tubItemStacks[1]);
 			if (this.tubItemStacks[2] == null) {
 				this.tubItemStacks[2] = output.copy();
 			} else if (this.tubItemStacks[2].isItemEqual(output)) {
 				this.tubItemStacks[2].stackSize += output.stackSize;
 			}
 			this.waterLevel -= modifier.getWaterUsage(this.tubItemStacks[1]);
 			--this.tubItemStacks[0].stackSize;
 			if (this.tubItemStacks[0].stackSize <= 0) {
 				this.tubItemStacks[0] = null;
 			}
 			this.tubItemStacks[1].stackSize -= output.stackSize;
 			if (this.tubItemStacks[1].stackSize <= 0) {
 				this.tubItemStacks[1] = null;
 			}
 		}
 	}
 }
