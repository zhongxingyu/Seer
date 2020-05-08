 package doc.dynamictanks.tileentity;
 
 import java.util.Set;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.ChunkCoordIntPair;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeChunkManager;
 import net.minecraftforge.common.ForgeChunkManager.Ticket;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidContainerRegistry;
 import net.minecraftforge.fluids.FluidStack;
 import net.minecraftforge.fluids.FluidTank;
 import net.minecraftforge.fluids.FluidTankInfo;
 import net.minecraftforge.fluids.IFluidHandler;
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerHandler;
 import buildcraft.api.power.PowerHandler.PowerReceiver;
 import buildcraft.api.power.PowerHandler.Type;
 
 import com.google.common.collect.Sets;
 
 import dan200.computer.api.IComputerAccess;
 import dan200.computer.api.ILuaContext;
 import dan200.computer.api.IPeripheral;
 import doc.dynamictanks.DynamicTanks;
 import doc.dynamictanks.Utils.BlockUtils;
 import doc.dynamictanks.Utils.ItemUtils;
 import doc.dynamictanks.Utils.MiscUtils;
 import doc.dynamictanks.Utils.PacketUtil;
 import doc.dynamictanks.Utils.ParticleUtils;
 import doc.dynamictanks.Utils.TEUtil;
 import doc.dynamictanks.block.BlockManager;
 import doc.dynamictanks.block.blockEventIds;
 import doc.dynamictanks.common.ModConfig;
 import doc.dynamictanks.items.ItemManager;
 
 
 public class TileEntityMultiTankCore extends TileEntity implements IFluidHandler, IPowerReceptor, IPeripheral, IInventory {
 
 	private Ticket chunkTicket;
 	
 	protected int ticksMax = 20;
 	private int currentTick = 0;
 	private int fillTicks = 1000;
 	public int initalizeDelay = 10;
 	public int energyStored = -1;
 
 	TileEntityMultiTankCore tileEntityCore;
 
 	public ForgeDirection lastFilledDirection = ForgeDirection.UNKNOWN;
 	
 	public static boolean[] autoOutput = { false, false, false, false, false, false }; //TOP FRONT BOTTOM BACK LEFT RIGHT
 
 	public FluidTank tank;
 
 	public PowerHandler powerProvider;
 
 	public int renderOffset;
 	public int capacity = 10;
 	public int connectingTanks = 1;
 	public int selectPostion = 0;
 	public int needed = 0;
 	public int side0 = -1, side1 = -1, side2 = -1, side3 = -1, side4 = -1, side5 = -1;
 	public int meta0 = 0, meta1 = 0, meta2 = 0, meta3 = 0, meta4 = 0, meta5 = 0;
 	public int dyeIndex = -1;
 	public int storedPotionId = -1;
 
 	public float totalTankHardness = 10;
 	public float scalarMultiplier = 1.00f;
     public float oldScalarMultiplier = 1.00f;
 
 	public boolean isDraining = false;
 
 	private ItemStack[] inventory;
 
 	public TileEntityMultiTankCore()
 	{
 		inventory = new ItemStack[2];
 
 		powerProvider = new PowerHandler(this, Type.MACHINE);
 		powerProvider.configure(1, 200, 25, 1000);
 
 		tank = new FluidTank(10 * FluidContainerRegistry.BUCKET_VOLUME);
 		totalTankHardness = 0;
 
 	}
 
 	public TileEntityMultiTankCore getCore()
 	{
 		if(tileEntityCore == null)
 			tileEntityCore = this;
 		return tileEntityCore;
 	}
 
 	@Override
 	public int fill (ForgeDirection from, FluidStack resource, boolean doFill)
 	{		
 		if (resource == null) {
 			return 0;
 		}
 
 		lastFilledDirection = from;
 		
 		resource = resource.copy();
 		int totalUsed = 0;
 		TileEntityMultiTankCore tankToFill = getBottomTank();
 
 		FluidStack liquid = tankToFill.tank.getFluid();
 		if (liquid != null && liquid.amount > 0 && !liquid.isFluidEqual(resource)) {
 			return 0;
 		}
 
 		while (tankToFill != null && resource.amount > 0) {
 			int used = tankToFill.tank.fill(resource, doFill);
 			resource.amount -= used;
 			if (used > 0) {
 				renderOffset = resource.amount;
 				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 			}
 
 			totalUsed += used;
 			tankToFill = getTankAbove(tankToFill);
 		}
 		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 		/*TEUtil.spawnLiquids(resource, TEUtil.getBottomTankCount(this) + 1, worldObj, TEUtil.getTopTank(this).xCoord + 1.0D, TEUtil.getTopTank(this).yCoord + 0.5D, TEUtil.getTopTank(this).zCoord + 0.5D, 50, from);
 		TEUtil.spawnLiquids(resource, TEUtil.getBottomTankCount(this) + 1, worldObj, TEUtil.getTopTank(this).xCoord, TEUtil.getTopTank(this).yCoord + 0.5D, TEUtil.getTopTank(this).zCoord + 0.5D, 50, from);*/
 		/*if (from == ForgeDirection.WEST)
 			ParticleUtils.spawnLiquids(resource, TEUtil.getBottomTankCount(this) + 1, worldObj, TEUtil.getTopTank(this).xCoord + 1, TEUtil.getTopTank(this).yCoord + 0.5D, TEUtil.getTopTank(this).zCoord + 0.5D, 65, from);
 		else if (from == ForgeDirection.EAST)
 			ParticleUtils.spawnLiquids(resource, TEUtil.getBottomTankCount(this) + 1, worldObj, TEUtil.getTopTank(this).xCoord, TEUtil.getTopTank(this).yCoord + 0.5D, TEUtil.getTopTank(this).zCoord + 0.5D, 65, from);*/
 		fillTicks = 0;
 		return totalUsed;
 	}
 
 
 	@Override
 	public FluidStack drain (ForgeDirection from, int maxDrain, boolean doDrain)
 	{
 		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 		return getTopTank().tank.drain(maxDrain, doDrain);
 	}
 
 	@Override
 	public FluidStack drain (ForgeDirection from, FluidStack resource, boolean doDrain)
 	{
 		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 		return getTopTank().tank.drain(resource.amount, doDrain);
 	}
 
 	@Override
 	public boolean canFill(ForgeDirection from, Fluid fluid) {
 		return true;
 	}
 
 	@Override
 	public boolean canDrain(ForgeDirection from, Fluid fluid) {
 		return true;
 	}
 
 	@Override
 	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
 		return new FluidTankInfo[] { tank.getInfo() };
 	}
 
 	public float getLiquidAmountScaled()
 	{
 		if (tank.getFluid() != null && tank.getFluid().amount > 0) {
 			if (tank.getCapacity() < tank.getFluid().amount) {
 				return worldObj.getBlockId(xCoord, yCoord + 1, zCoord) != 0 ? (float) (tank.getCapacity() - renderOffset) / (float) (tank.getCapacity() * 1.00F) : (float) (tank.getCapacity() - renderOffset) / (float) (tank.getCapacity() * 1.01F);
 			} 
 			return worldObj.getBlockId(xCoord, yCoord + 1, zCoord) != 0 ? (float) (tank.getFluid().amount - renderOffset) / (float) (tank.getCapacity() * 1.00F) : (float) (tank.getFluid().amount - renderOffset) / (float) (tank.getCapacity() * 1.01F);
 		}
 		return 0.0f;
 	}
 	
 	public float getLiquidAmountScaledForItem()
 	{
 		if (tank.getFluid() != null && tank.getFluid().amount > 0) {
 			return (float) (tank.getFluid().amount - renderOffset) / (float) (tank.getCapacity() * 1.00F);
 		}
 		return 0.0f;
 	}
 
 	public int getLiquidAmountScaledForGUI()
 	{
 		double f = (((tank.getFluid().amount * 0.01) / (tank.getCapacity() * 0.01)) * 65.0D);
 		if (f > 65) {
 			return 65;
 		}
 		return (int) (f % 2 != 0 ? f - 1 : f);
 	}
 
 	public int getLiquidAmountScaledForComparator()
 	{
 		double f = 0;
 		if (tank.getFluid() != null) f = (((tank.getFluid().amount * 0.01) / (tank.getCapacity() * 0.01)) * 15.0D);
 		if (f > 15) {
 			return 15;
 		}
 		return (int) f;
 	}
 
 	public boolean containsLiquid ()
 	{
 		return tank.getFluid() != null;
 	}
 
 	public int getBrightness ()
 	{
 		if (containsLiquid())
 		{
 			int id = tank.getFluid().getFluid().getBlockID();
			if (id < 4096)
 			{
 				return Block.lightValue[id];
 			}
 		}
 		return 0;
 	}
 
 	@Override
 	public void readFromNBT (NBTTagCompound tags)
 	{
 		super.readFromNBT(tags);
 		readCustomNBT(tags);
 	}
 
 	@Override
 	public void writeToNBT (NBTTagCompound tags)
 	{
 		super.writeToNBT(tags);
 		writeCustomNBT(tags);
 	}
 
 	public void readCustomNBT (NBTTagCompound tags)
 	{
 		if (tags.getBoolean("hasLiquid"))
 			tank.setFluid(new FluidStack(tags.getInteger("itemID"), tags.getInteger("amount")));
 		else
 			tank.setFluid(null);
 		connectingTanks = tags.getInteger("connectingTanks");
 		capacity = tags.getInteger("capacity");
 		tank.setCapacity(capacity);
 		side0 = tags.getInteger("side0");
 		side1 = tags.getInteger("side1");
 		side2 = tags.getInteger("side2");
 		side3 = tags.getInteger("side3");
 		side4 = tags.getInteger("side4");
 		side5 = tags.getInteger("side5");
 		meta0 = tags.getInteger("meta0");
 		meta1 = tags.getInteger("meta1");
 		meta2 = tags.getInteger("meta2");
 		meta3 = tags.getInteger("meta3");
 		meta4 = tags.getInteger("meta4");
 		meta5 = tags.getInteger("meta5");
 		needed = tags.getInteger("needed");
 		selectPostion = tags.getInteger("multiplier");
 		dyeIndex = tags.getInteger("dyeColor");
 		storedPotionId = tags.getInteger("potionID");
 
 		powerProvider.readFromNBT(tags);
 		powerProvider.configure(1, 200, 100, 1000);
 
 		isDraining = tags.getBoolean("isDraining");
 
 		scalarMultiplier = tags.getFloat("scalar");
 
 		for (int i = 0; i < 5; i++) {
 			autoOutput[i] = tags.getBoolean("output" + i);
 		}
 		
 		NBTTagList tagList = tags.getTagList("Inventory");
 		for (int i = 0; i < tagList.tagCount(); i++) {
 			NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
 			byte slot = tag.getByte("Slot");
 			if (slot >= 0 && slot < inventory.length) {
 				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
 			}
 		}
 
 	}
 
 	public void writeCustomNBT (NBTTagCompound tags)
 	{
 		FluidStack liquid = tank.getFluid();
 		tags.setBoolean("hasLiquid", liquid != null);
 		tags.setInteger("connectingTanks", connectingTanks);
 		tags.setInteger("capacity", tank.getCapacity());
 		if (liquid != null)
 		{
 			tags.setInteger("itemID", liquid.fluidID);
 			tags.setInteger("amount", liquid.amount);
 			//tags.setInteger("itemMeta", liquid.itemMeta);
 		}
 		tags.setInteger("side0", side0);
 		tags.setInteger("side1", side1);
 		tags.setInteger("side2", side2);
 		tags.setInteger("side3", side3);
 		tags.setInteger("side4", side4);
 		tags.setInteger("side5", side5);
 		tags.setInteger("meta0", meta0);
 		tags.setInteger("meta1", meta1);
 		tags.setInteger("meta2", meta2);
 		tags.setInteger("meta3", meta3);
 		tags.setInteger("meta4", meta4);
 		tags.setInteger("meta5", meta5);
 		tags.setInteger("needed", needed);
 		tags.setInteger("multiplier", selectPostion);
 		tags.setInteger("dyeColor", dyeIndex);
 		tags.setInteger("potionID", storedPotionId);
 
 		powerProvider.writeToNBT(tags);
 
 		for (int i = 0; i < 5; i++) {
 			tags.setBoolean("output" + i, autoOutput[i]);
 		}
 		
 		tags.setBoolean("isDraining", isDraining);
 
 		//tags.setFloat("totalHardness", totalTankHardness);
 		tags.setFloat("scalar", scalarMultiplier);
 
 		NBTTagList itemList = new NBTTagList();
 		for (int i = 0; i < inventory.length; i++) {
 			ItemStack stack = inventory[i];
 			if (stack != null) {
 				NBTTagCompound tag = new NBTTagCompound();
 				tag.setByte("Slot", (byte) i);
 				stack.writeToNBT(tag);
 				itemList.appendTag(tag);
 			}
 		}
 		tags.setTag("Inventory", itemList);
 	}
 
 	/* Packets */
 	@Override
 	public Packet getDescriptionPacket ()
 	{
 		NBTTagCompound tag = new NBTTagCompound();
 		writeCustomNBT(tag);
 		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
 	}
 
 	@Override
 	public void onDataPacket (INetworkManager net, Packet132TileEntityData packet) {
 		readCustomNBT(packet.customParam1);
 		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
 	}
 
 
 	/* Updating */
 	public boolean canUpdate ()
 	{
 		return true;
 	}
 
 	@Override
 	public void updateEntity ()
 	{
 		if (initalizeDelay < 60)
 			initalizeDelay++;		
 		
 		if (currentTick > ticksMax)
 			currentTick = 0;
 		else currentTick++;
 		
 		if (fillTicks < 5)
 			fillTicks++;
 		
 		/*if (fillTicks < 5) {
 			if (lastFilledDirection == ForgeDirection.WEST)
 				ParticleUtils.spawnLiquids(tank.getFluid(), TEUtil.getBottomTankCount(this) + 1, worldObj, TEUtil.getTopTank(this).xCoord + 1, TEUtil.getTopTank(this).yCoord + 0.5D, TEUtil.getTopTank(this).zCoord + 0.5D, 65, lastFilledDirection);
 			else if (lastFilledDirection == ForgeDirection.EAST)
 				ParticleUtils.spawnLiquids(tank.getFluid(), TEUtil.getBottomTankCount(this) + 1, worldObj, TEUtil.getTopTank(this).xCoord, TEUtil.getTopTank(this).yCoord + 0.5D, TEUtil.getTopTank(this).zCoord + 0.5D, 65, lastFilledDirection);
 			else if (lastFilledDirection == ForgeDirection.UNKNOWN)
 				ParticleUtils.spawnLiquids(tank.getFluid(), TEUtil.getBottomTankCount(this) + 1, worldObj, TEUtil.getTopTank(this).xCoord, TEUtil.getTopTank(this).yCoord + 1D, TEUtil.getTopTank(this).zCoord + 0.5D, 65, lastFilledDirection);
 		}*/
 		
 		if (worldObj.isRemote && TEUtil.getBottomTankLiquid(this) != null && TEUtil.getBottomTankLiquid(this).getFluid() == DynamicTanks.fluidPotion && ModConfig.BooleanVars.particles && side1 == -1) ParticleUtils.spawnCustomParticle("coloredSwirl", worldObj, xCoord, yCoord, zCoord, 45);
 
 		if (getStackInSlot(0) != null && getStackInSlot(0).itemID == ItemManager.linkCard.itemID && getStackInSlot(0).stackTagCompound == null) {
 			ItemStack applyInfo = getStackInSlot(0);
 			if (applyInfo.stackTagCompound == null) {
 				applyInfo.setTagCompound(new NBTTagCompound());
 				applyInfo.stackTagCompound.setInteger("X", xCoord);
 				applyInfo.stackTagCompound.setInteger("Y", yCoord);
 				applyInfo.stackTagCompound.setInteger("Z", zCoord);
 			}
 			setInventorySlotContents(0, null);
 			setInventorySlotContents(0, applyInfo);
 		}
 
 		if (getStackInSlot(1) != null && scalarMultiplier != ItemUtils.chipsetMultiplier(getStackInSlot(1).getItemDamage())) {
 			scalarMultiplier = ItemUtils.chipsetMultiplier(getStackInSlot(1).getItemDamage());
 			TEUtil.resizeTank(this);
 		}
 		
 		if (getStackInSlot(1) != null && getStackInSlot(1).itemID == ItemManager.chipSet.itemID && getStackInSlot(1).getItemDamage() == 7 && currentTick == ticksMax) {
 			if (MiscUtils.atLeastOneTrue(autoOutput)) {
 				int toDistribute = FluidContainerRegistry.BUCKET_VOLUME; // MiscUtils.countTrue(autoOutput);
 				if (tank.getFluid() == null)
 					return;
 
 				FluidStack toAdd = new FluidStack(this.tank.getFluid(), toDistribute);
 				if (autoOutput[0] && tank.getFluid().amount - toDistribute >= 0) MiscUtils.getBlockTop(toAdd, this, worldObj, xCoord, yCoord, zCoord);
 				if (autoOutput[1] && tank.getFluid().amount - toDistribute >= 0) MiscUtils.getBlockNorth(toAdd, this, worldObj, xCoord, yCoord, zCoord);
 				if (autoOutput[2] && tank.getFluid().amount - toDistribute >= 0) MiscUtils.getBlockBottom(toAdd, this, worldObj, xCoord, yCoord, zCoord);
 				if (autoOutput[3] && tank.getFluid().amount - toDistribute >= 0) MiscUtils.getBlockSouth(toAdd, this, worldObj, xCoord, yCoord, zCoord);
 				if (autoOutput[4] && tank.getFluid().amount - toDistribute >= 0) MiscUtils.getBlockWest(toAdd, this, worldObj, xCoord, yCoord, zCoord);
 				if (autoOutput[5] && tank.getFluid().amount - toDistribute >= 0) MiscUtils.getBlockEast(toAdd, this, worldObj, xCoord, yCoord, zCoord);
 			}
 		}
 
 		if (getStackInSlot(1) == null && scalarMultiplier != 1.00f) {
 			scalarMultiplier = 1.00f;
 			TEUtil.resizeTank(this);
 		}
 
 		if (tank.getFluid() == null && storedPotionId != -1)
 			storedPotionId = -1;
 
 		if (renderOffset > 0)
 		{
 			renderOffset -= 6;
 			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
 		}
 
 		if (tank.getFluid() != null) {
 			moveLiquidBelow();
 		}
 
 		if (tank != null && tank.getFluid() != null && initalizeDelay >= 40) {
 			int newSize = tank.getFluid().amount > tank.getCapacity() ? tank.getFluid().amount - tank.getCapacity() : 0;
 			tank.drain(newSize, true);
 		}
 		
 		if (!worldObj.isRemote) {
 			energyStored = (int) powerProvider.getEnergyStored();
 			
 			worldObj.addBlockEvent(xCoord, yCoord, zCoord, BlockManager.tankCore.blockID, blockEventIds.energyStoredId, energyStored);
 		}
 
 		if (worldObj.isRemote) {
 			return;
 		}
 
 
 		doWork(powerProvider);
 
 
 		/*if (powerProvider.getEnergyStored() >= 30 && isDraining == true && selectPostion == 0) {
 			powerProvider.useEnergy(25, 25, true);
 		} else if (powerProvider.getEnergyStored() >= 30 && isDraining == true && selectPostion == 1) {
 			powerProvider.useEnergy(50, 50, true);
 		} else if (powerProvider.getEnergyStored() >= 30 && isDraining == true && selectPostion == 2) {
 			powerProvider.useEnergy(100, 100, true);
 		}
 
 		else if (isDraining == true) {
 			PacketUtil.sendPacketWithInt(PacketUtil.draining, 0, this.xCoord, this.yCoord, this.zCoord); //disable drain
 			PacketUtil.sendPacketWithInt(PacketUtil.decreaseSize, sizeSelection(selectPostion), this.xCoord, this.yCoord, this.zCoord); //decrease size
 			updateMe(worldObj);
 		}*/
 
 		BlockUtils.checkAndUpdateComparator(worldObj, xCoord, yCoord, zCoord);
 	}
 
 	@Override
 	public boolean receiveClientEvent(int id, int value) {
 		if (id == blockEventIds.energyStoredId) {
 			energyStored = value;
 		}		
 		return true;
 	}
 	
 	/*
 	 * Helpers
 	 */
 
 	public void increaseSize(float increase) {
 		//float capacity = tank.getCapacity() / FluidContainerRegistry.BUCKET_VOLUME;
 		tank.setCapacity((int) ((tank.getCapacity() * increase)/* * FluidContainerRegistry.BUCKET_VOLUME*/));
 	}
 
 	public void decreaseSize(float decrease) {
 		//float capacity = tank.getCapacity() / FluidContainerRegistry.BUCKET_VOLUME;
 		tank.setCapacity((int) ((tank.getCapacity() / decrease)/* * FluidContainerRegistry.BUCKET_VOLUME*/));
 	}
 
 	public float sizeSelection(int selectedIndex) {
 		switch(selectedIndex) {
 		case 0: return 1.3F;
 		case 1: return 1.5F;
 		case 2: return 2.0F;
 		default: return 0.0F;
 		}
 	}
 
 	public TileEntityMultiTankCore getTopTank() {
 
 		TileEntityMultiTankCore lastTank = this;
 
 		while (true) {
 			TileEntityMultiTankCore above = getTankAbove(lastTank);
 			if (above != null && above.tank.getFluid() != null) {
 				lastTank = above;
 			} else {
 				break;
 			}
 		}
 
 		return lastTank;
 	}
 
 	public TileEntityMultiTankCore getBottomTank() {
 
 		TileEntityMultiTankCore lastTank = this;
 
 		while (true) {
 			TileEntityMultiTankCore below = getTankBelow(lastTank);
 			if (below != null) {
 				lastTank = below;
 			} else {
 				break;
 			}
 		}
 
 		return lastTank;
 	}
 
 	public static TileEntityMultiTankCore getTankBelow(TileEntityMultiTankCore tile) { 
 		TileEntity below = tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord - 1, tile.zCoord);
 		if (below instanceof TileEntityMultiTankCore) {
 			return (TileEntityMultiTankCore) below;
 		} else if (below instanceof TileEntityMultiTankSub) {
 			return ((TileEntityMultiTankSub) tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord - 1, tile.zCoord)).getCore();
 		} 
 		return null;
 	}
 
 	public static TileEntityMultiTankCore getTankAbove(TileEntityMultiTankCore tile) {
 		TileEntity above = tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord + 1, tile.zCoord);
 		if (above instanceof TileEntityMultiTankCore) {
 			return (TileEntityMultiTankCore) above;
 		} else if (above instanceof TileEntityMultiTankSub) {
 			return ((TileEntityMultiTankSub) tile.worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord + 1, tile.zCoord)).getCore();
 		} 
 		return null;
 	}
 
 	public void moveLiquidBelow() {
 		TileEntityMultiTankCore below = getTankBelow(this);
 		if (below == null) {
 			return;
 		}
 
 		int used = below.tank.fill(tank.getFluid(), true);
 		if (used > 0) {
 			tank.drain(used, true);
 		}
 	}
 
 	public void updateMe(World world) {
 		world.markBlockForUpdate(xCoord, yCoord, zCoord);
 	}
 
 	@Override
 	public PowerReceiver getPowerReceiver(ForgeDirection side) {
 		return powerProvider.getPowerReceiver();
 	}
 
 	@Override
 	public void doWork(PowerHandler workProvider) {
 		if (powerProvider.getEnergyStored() >= 30 && isDraining == true && selectPostion == 0) {
 			powerProvider.useEnergy(25, 25, true);
 			return;
 		} else if (powerProvider.getEnergyStored() >= 30 && isDraining == true && selectPostion == 1) {
 			powerProvider.useEnergy(50, 50, true);
 			return;
 		} else if (powerProvider.getEnergyStored() >= 30 && isDraining == true && selectPostion == 2) {
 			powerProvider.useEnergy(100, 100, true);
 			return;
 		}
 		else if (isDraining == true) {
 			PacketUtil.sendPacketWithInt(PacketUtil.draining, 0, this.xCoord, this.yCoord, this.zCoord); //disable drain
 			PacketUtil.sendPacketWithInt(PacketUtil.decreaseSize, sizeSelection(selectPostion), this.xCoord, this.yCoord, this.zCoord); //decrease size
 			updateMe(worldObj);
 		}
 	}
 
 	@Override
 	public World getWorld() {
 		return this.worldObj;
 	}
 
 	@Override
 	public String getType() {
 		return "tank_Node";
 	}
 
 	@Override
 	public String[] getMethodNames() {
 		return new String[] { "getFluid", "getAmount", "getCapacity", "getNeighbors", "getEnergy", "setCamoBlock", "setCamoBlockWithMeta" };
 	}
 
 	@Override
 	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
 			int method, Object[] arguments) throws Exception {
 		switch (method) {
 		case 0:
 			return new Object[] { this.tank.getFluid().getFluid().getName() };
 		case 1: 
 			return new Object[] { TEUtil.getTotalAmount(this) };
 		case 2: 
 			return new Object[] { TEUtil.getTotalCapacity(this) };
 		case 3:
 			return new Object[] { this.connectingTanks };
 		case 4:
 			return new Object[] { powerProvider.getEnergyStored() };
 		case 5: 
 			if (arguments[0] instanceof Double && ((Double) arguments[0]).intValue() < 4096 && Block.blocksList[((Double) arguments[0]).intValue()] != null) {
 				this.side1 = ((Double) arguments[0]).intValue();
 				PacketUtil.sendPacketWithInt(PacketUtil.camo, ((Double) arguments[0]).intValue(), xCoord, yCoord, zCoord);
 				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 				return new Object[] { true };
 			} else 
 				return new Object[] { false };
 		case 6:
 			if ((arguments[0] instanceof Double && arguments[1] instanceof Double) && ((Double) arguments[0]).intValue() < 4096 && Block.blocksList[((Double) arguments[0]).intValue()] != null) {
 				this.side1 = ((Double) arguments[0]).intValue();
 				this.meta1 = ((Double) arguments[1]).intValue();
 				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 				return new Object[] { true };
 			} else 
 				return new Object[] { false };
 		} 
 		return new Object[] { "Wrong" };
 	}
 
 	@Override
 	public boolean canAttachToSide(int side) {
 		return true;
 	}
 
 	@Override
 	public void attach(IComputerAccess computer) {
 
 	}
 
 	@Override
 	public void detach(IComputerAccess computer) {
 		// TODO Auto-generated method stub
 
 	}
 
 	// GUI
 	@Override
 	public int getSizeInventory() {
 		return inventory.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		return inventory[slot];
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		inventory[slot] = stack;
 		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
 			stack.stackSize = getInventoryStackLimit();
 		}
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int amt) {
 		ItemStack stack = getStackInSlot(slot);
 		if (stack != null) {
 			if (stack.stackSize <= amt) {
 				setInventorySlotContents(slot, null);
 			} else {
 				stack = stack.splitStack(amt);
 				if (stack.stackSize == 0) {
 					setInventorySlotContents(slot, null);
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
 	public int getInventoryStackLimit() {
 		return 1;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this &&
 				player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 
 
 	@Override
 	public String getInvName() {
 		return "dynamictanks.MultiTank";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return false;
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		if (i == 1)
 			return false;
 		return true;
 	}
 
 	//Chunk Stuff
 	@Override
 	public void invalidate() {		
 		ForgeChunkManager.releaseTicket(chunkTicket);
 		super.invalidate();		
 	}
 
 	private void setBoundaries() {
 		if (chunkTicket == null)
 			chunkTicket = ForgeChunkManager.requestTicket(DynamicTanks.instance, worldObj, ForgeChunkManager.Type.NORMAL);
 
 		if (chunkTicket == null)
 			return;
 
 		chunkTicket.getModData().setInteger("CoreX", xCoord);
 		chunkTicket.getModData().setInteger("CoreY", yCoord);
 		chunkTicket.getModData().setInteger("CoreZ", zCoord);
 
 		ForgeChunkManager.forceChunk(chunkTicket, new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4));
 
 		forceChunkLoading(chunkTicket);
 	}
 
 	public void forceChunkLoading(Ticket ticket) {
 		if (chunkTicket == null)
 			chunkTicket = ticket;
 
 		Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
 		ChunkCoordIntPair quarryChunk = new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4);
 		chunks.add(quarryChunk);
 		ForgeChunkManager.forceChunk(ticket, quarryChunk);
 
 		for (int chunkX = 0 >> 4; chunkX <= 5 >> 4; chunkX++) {
 			for (int chunkZ = 0 >> 4; chunkZ <= 5 >> 4; chunkZ++) {
 				ChunkCoordIntPair chunk = new ChunkCoordIntPair(chunkX, chunkZ);
 				ForgeChunkManager.forceChunk(ticket, chunk);
 				chunks.add(chunk);
 			}
 		}
 	}
 }
