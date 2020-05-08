 
 package hawksmachinery.common.tileentity;
 
 import hawksmachinery.common.api.HMRecipes.HMEnumProcessing;
 import hawksmachinery.common.api.HMRepairInterfaces.IHMSappable;
 import hawksmachinery.common.api.HMRepairInterfaces.IHMSapper;
 import hawksmachinery.common.api.IHMMachine;
 import hawksmachinery.common.api.helpers.HMVector;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.Random;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 import universalelectricity.core.UniversalElectricity;
 import universalelectricity.core.electricity.ElectricityConnections;
 import universalelectricity.core.electricity.ElectricityNetwork;
 import universalelectricity.core.electricity.ElectricityPack;
 import universalelectricity.core.implement.IConductor;
 import universalelectricity.core.vector.Vector3;
 import universalelectricity.prefab.implement.IRotatable;
 import universalelectricity.prefab.network.IPacketReceiver;
 import universalelectricity.prefab.network.PacketManager;
 import universalelectricity.prefab.tile.TileEntityElectricityReceiver;
 import com.google.common.io.ByteArrayDataInput;
 
 /**
  * 
  * Extend this if you'd like to make a machine slightly faster.
  * 
  * @author Elusivehawk
  */
 public abstract class HMTileEntityMachine extends TileEntityElectricityReceiver implements IInventory, ISidedInventory, IRotatable, IPacketReceiver, IHMSappable, IHMMachine
 {
 	public int ELECTRICITY_REQUIRED;
 	
 	public int TICKS_REQUIRED;
 	
 	private ForgeDirection facingDirection = ForgeDirection.UNKNOWN;
 	
 	public double electricityStored;
 	
 	public int workTicks;
 	
 	public ItemStack[] containingItems;
 	
 	public int ELECTRICITY_LIMIT;
 	
 	public int playersLookingIn = 0;
 	
 	public double VOLTAGE;
 	
 	public HMEnumProcessing machineEnum;
 	
 	protected int machineHP;
 	
 	protected ItemStack sapper;
 	
 	protected boolean isProcessor;
 	
 	protected boolean canSendPackets = true;
 	
 	protected int maxHP = 20;
 	
 	protected HMVector backsideVec;
 	
 	protected HMVector selfVec;
 	
 	public boolean canRotate;
 	
 	private ArrayList<ForgeDirection> directionList = new ArrayList<ForgeDirection>();
 	
 	public HMTileEntityMachine()
 	{
 		
 	}
 	
 	public ForgeDirection getDefaultCableDirection()
 	{
 		return ForgeDirection.DOWN;
 	}
 	
 	@Override
 	public void initiate()
 	{
 		this.selfVec = new HMVector(this);
 		if (!this.canRotate) ElectricityConnections.registerConnector(this, EnumSet.of(this.getDefaultCableDirection()));
 		
 	}
 	
 	@Override
 	public void updateEntity()
 	{
 		super.updateEntity();
 		
 		if (!this.directionList.isEmpty() && this.canRotate)
 		{
 			this.facingDirection = this.directionList.get(0);
 			this.directionList.remove(0);
 			ElectricityConnections.unregisterConnector(this);
 			ElectricityConnections.registerConnector(this, EnumSet.of(this.getDefaultCableDirection(), this.facingDirection.getOpposite()));
 			
 			if (this.backsideVec == null)
 			{
 				this.backsideVec = new HMVector(this);
 				
 			}
 			
 			this.backsideVec = this.backsideVec.reset(this).modifyFromDir(this.facingDirection.getOpposite());
 			
 			this.selfVec.markBlockForRenderUpdate();
 			this.selfVec.updateNeighboringBlocks();
 			
 		}
 		
 		TileEntity inputCable = this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord - 1, this.zCoord);
 		ElectricityNetwork network = null;
 		boolean usedBacksideVec = false;
 		
 		if (inputCable == null && this.backsideVec != null)
 		{
 			inputCable = this.backsideVec.getTileEntity();
 			usedBacksideVec = true;
 			
 		}
 		
 		if (inputCable != null)
 		{
 			if (inputCable instanceof IConductor)
 			{
 				network = ElectricityNetwork.getNetworkFromTileEntity(inputCable, usedBacksideVec ? this.facingDirection.getOpposite() : this.getDefaultCableDirection());
 				
 			}
 			
 		}
 		
 		if (network != null && !this.isDisabled())
 		{
 			if (this.electricityStored < this.ELECTRICITY_LIMIT)
 			{
 				network.startRequesting(this, (this.ELECTRICITY_REQUIRED * 2) / this.VOLTAGE, this.VOLTAGE);
 				ElectricityPack pack = network.consumeElectricity(this);
 				this.electricityStored += Math.max(Math.min(this.electricityStored + pack.getWatts(), this.ELECTRICITY_REQUIRED), 0);
 				
 				if (UniversalElectricity.isVoltageSensitive && pack.voltage > this.VOLTAGE)
 				{
 					this.explodeMachine(2.0F);
 					
 				}
 				
 			}
 			else
 			{
 				network.stopRequesting(this);
 				
 			}
 			
 		}
 		
 		if (!this.isDisabled())
 		{
 			this.electricityStored = Math.min(this.electricityStored, this.ELECTRICITY_LIMIT);
 			this.electricityStored = Math.max(this.electricityStored, 0);
 			
 		}
 		
 		this.machineHP = Math.min(this.machineHP, this.getMaxHP());
 		this.machineHP = Math.max(this.machineHP, 0);
 		
 		if (!this.worldObj.isRemote && this.worldObj.getTotalWorldTime() % 3L == 0L && this.canSendPackets)
 		{
 			PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 8);
 			
 		}
 		
 	}
 	
 	@Override
 	protected void whileDisable()
 	{
 		if (this.isBeingSapped())
 		{
 			((IHMSapper)this.sapper.getItem()).sapperTick(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.sapper);
 			
 		}
 		
 		//TODO Add machine network computer... thingy.
 		
 	}
 	
 	@Override
 	public void onInventoryChanged()
 	{
 		
 	}
 	
 	@Override
	public double getVoltage(Object... data)
 	{
 		return this.VOLTAGE;
 	}
 	
 	protected void explodeMachine(float strength)
 	{
 		this.worldObj.createExplosion((Entity)null, this.xCoord, this.yCoord, this.zCoord, strength, true);
 		
 	}
 	
 	@Override
 	public Packet getDescriptionPacket()
 	{
 		if (this.isProcessor && this.playersLookingIn > 0) return PacketManager.getPacket("HawksMachinery", this, this.workTicks, this.electricityStored, this.machineHP, this.facingDirection.ordinal());
 		return PacketManager.getPacket("HawksMachinery", this, this.electricityStored, this.machineHP, this.facingDirection.ordinal());
 	}
 	
 	@Override
 	public String getInvName()
 	{
 		return null;
 	}
 	
 	@Override
 	public void handlePacketData(INetworkManager network, int type, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
 	{
 		try
 		{
 			if (this.isProcessor && this.playersLookingIn > 0)
 			{
 				this.workTicks = dataStream.readInt();
 				
 			}
 			
 			this.electricityStored = dataStream.readDouble();
 			this.machineHP = dataStream.readInt();
 			
 			ForgeDirection newDir = ForgeDirection.getOrientation(dataStream.readInt());
 			
 			if (newDir != this.facingDirection)
 			{
 				this.setDirection(newDir);
 				
 			}
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			
 		}
 		
 	}
 	
 	@Override
 	public int getSizeInventory()
 	{
 		return this.containingItems != null ? this.containingItems.length : 0;
 	}
 	
 	@Override
 	public ItemStack getStackInSlot(int slot)
 	{
 		return this.containingItems != null ? this.containingItems[slot] : null;
 	}
 	
 	@Override
 	public ItemStack decrStackSize(int slot, int quantity)
 	{
 		if (this.containingItems[slot] != null)
 		{
 			ItemStack oldItem;
 			
 			if (this.containingItems[slot].stackSize <= quantity)
 			{
 				oldItem = this.containingItems[slot];
 				this.containingItems[slot] = null;
 				return oldItem;
 			}
 			else
 			{
 				oldItem = this.containingItems[slot].splitStack(quantity);
 				
 				if (this.containingItems[slot].stackSize == 0)
 				{
 					this.containingItems[slot] = null;
 					
 				}
 				
 				return oldItem;
 			}
 			
 		}
 		else
 		{
 			return null;
 		}
 		
 	}
 	
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot)
 	{
 		if (this.containingItems[slot] != null)
 		{
 			ItemStack oldItem = this.containingItems[slot];
 			this.containingItems[slot] = null;
 			return oldItem;
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public int getStartInventorySide(ForgeDirection side)
 	{
 		return 0;
 	}
 	
 	@Override
 	public int getSizeInventorySide(ForgeDirection side)
 	{
 		return 0;
 	}
 	
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack item)
 	{
 		this.containingItems[slot] = item;
 		
 		if (item != null)
 		{
 			if (item.stackSize > this.getInventoryStackLimit())
 			{
 				item.stackSize = this.getInventoryStackLimit();
 				
 			}
 			
 		}
 		
 	}
 	
 	@Override
 	public int getInventoryStackLimit()
 	{
 		return 64;
 	}
 	
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player)
 	{
 		return !player.isSneaking();
 	}
 	
 	@Override
 	public void openChest()
 	{
 		++this.playersLookingIn;
 		
 	}
 	
 	@Override
 	public void closeChest()
 	{
 		--this.playersLookingIn;
 		
 	}
 	
 	@Override
 	public ForgeDirection getDirection()
 	{
 		return this.facingDirection;
 	}
 	
 	@Override
 	public void setDirection(ForgeDirection dir)
 	{
 		if (this.canRotate)
 		{
 			this.directionList.add(dir);
 			
 		}
 		
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound NBTTag)
 	{
 		super.readFromNBT(NBTTag);
 		this.machineHP = NBTTag.getInteger("MachineHP");
 		this.electricityStored = NBTTag.getDouble("electricityStored");
 		if (this.isProcessor) this.workTicks = NBTTag.getInteger("workTicks");
 		if (NBTTag.hasKey("electricityLimit")) this.ELECTRICITY_LIMIT = NBTTag.getInteger("electricityLimit");
 		if (NBTTag.hasKey("ticksNeeded")) this.TICKS_REQUIRED = NBTTag.getInteger("ticksNeeded");
 		if (NBTTag.hasKey("maxMachineHP")) this.maxHP = NBTTag.getInteger("maxMachineHP");
 		if (NBTTag.hasKey("facingDirection")) this.setDirection(ForgeDirection.getOrientation(NBTTag.getInteger("facingDirection")));
 		
 		if (NBTTag.hasKey("Sapper"))
 		{
 			this.sapper = ItemStack.loadItemStackFromNBT(NBTTag.getCompoundTag("Sapper"));
 			
 		}
 		
 		if (this.containingItems != null)
 		{
 			NBTTagList tagList = NBTTag.getTagList("Items");
 			this.containingItems = new ItemStack[this.getSizeInventory()];
 			for (int counter = 0; counter < tagList.tagCount(); ++counter)
 			{
 				NBTTagCompound newTag = (NBTTagCompound)tagList.tagAt(counter);
 				byte slot = newTag.getByte("Slot");
 				if (slot >= 0 && slot < this.containingItems.length)
 				{
 					this.containingItems[slot] = ItemStack.loadItemStackFromNBT(newTag);
 					
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound NBTTag)
 	{
 		super.writeToNBT(NBTTag);
 		NBTTag.setInteger("MachineHP", this.machineHP);
 		NBTTag.setDouble("electricityStored", this.electricityStored);
 		if (this.isProcessor) NBTTag.setInteger("workTicks", this.workTicks);
 		NBTTag.setInteger("electricityLimit", this.ELECTRICITY_LIMIT);
 		NBTTag.setInteger("ticksNeeded", this.TICKS_REQUIRED);
 		NBTTag.setInteger("maxMachineHP", this.maxHP);
 		NBTTag.setInteger("facingDirection", this.facingDirection.ordinal());
 		
 		if (this.sapper != null) NBTTag.setCompoundTag("Sapper", this.sapper.writeToNBT(new NBTTagCompound()));
 		
 		if (this.containingItems != null)
 		{
 			NBTTagList tagList = new NBTTagList();
 			
 			for (int counter = 0; counter < this.containingItems.length; ++counter)
 			{
 				if (this.containingItems[counter] != null)
 				{
 					NBTTagCompound newTag = new NBTTagCompound();
 					newTag.setByte("Slot", (byte)counter);
 					this.containingItems[counter].writeToNBT(newTag);
 					tagList.appendTag(newTag);
 				}
 				
 			}
 			
 			NBTTag.setTag("Items", tagList);
 			
 		}
 		
 	}
 	
 	public void randomlyDamageSelf()
 	{
 		if (new Random().nextInt(10) == 6)
 		{
 			--this.machineHP;
 			
 		}
 		
 	}
 	
 	public boolean canWork()
 	{
 		return this.isProcessor && !this.isDisabled() && this.workTicks > 0;
 	}
 	
 	public boolean attemptToRepair(int repairAmount)
 	{
 		if (this.machineHP != this.getMaxHP() && !this.isBeingSapped())
 		{
 			this.machineHP += repairAmount;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean setSapper(ItemStack sapper)
 	{
 		if (this.sapper == null)
 		{
 			this.sapper = sapper;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean attemptToUnSap(EntityPlayer player)
 	{
 		if (this.isBeingSapped())
 		{
 			int randomDigit = new Random().nextInt(((IHMSapper)this.sapper.getItem()).getRemovalValue(this.sapper, player));
 			
 			if (randomDigit == ((IHMSapper)this.sapper.getItem()).getRemovalValue(this.sapper, player) / 2)
 			{
 				((IHMSapper)this.sapper.getItem()).onRemoved(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
 				this.sapper = null;
 				return true;
 			}
 			
 		}
 		
 		return false;
 	}
 	
 	public boolean isBeingSapped()
 	{
 		return this.sapper != null;
 	}
 	
 	@Override
 	public boolean isDisabled()
 	{
 		return (this.isBeingSapped() || (this.machineHP == 0 && this.getMaxHP() > 0)) || this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
 	}
 	
 	public int getMaxHP()
 	{
 		return this.maxHP;
 	}
 	
 	public int getHP()
 	{
 		return this.machineHP;
 	}
 	
 	public double getElectricity()
 	{
 		return this.electricityStored;
 	}
 	
 	public double getMaxElectricity()
 	{
 		return this.ELECTRICITY_LIMIT;
 	}
 	
 }
