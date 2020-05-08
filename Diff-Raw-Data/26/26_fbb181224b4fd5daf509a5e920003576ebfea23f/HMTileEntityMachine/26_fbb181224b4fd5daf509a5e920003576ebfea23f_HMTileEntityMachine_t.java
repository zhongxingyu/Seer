 
 package hawksmachinery.tileentity;
 
 import hawksmachinery.api.HMRecipes.HMEnumProcessing;
 import hawksmachinery.api.HMRepairInterfaces.IHMRepairable;
 import hawksmachinery.api.HMRepairInterfaces.IHMSapper;
 import java.util.Random;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.INetworkManager;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagList;
 import net.minecraft.src.Packet;
 import net.minecraft.src.Packet250CustomPayload;
 import net.minecraft.src.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 import universalelectricity.core.electricity.ElectricInfo;
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
 public abstract class HMTileEntityMachine extends TileEntityElectricityReceiver implements ISidedInventory, IRotatable, IPacketReceiver, IHMRepairable
 {
 	public int ELECTRICITY_REQUIRED;
 	
 	public int TICKS_REQUIRED;
 	
 	private ForgeDirection facingDirection = ForgeDirection.DOWN;
 	
 	public double electricityStored;
 	
 	public int workTicks;
 	
 	public ItemStack[] containingItems;
 	
 	public int ELECTRICITY_LIMIT;
 	
 	public boolean isOpen;
 	
 	public double voltage;
 	
 	public HMEnumProcessing machineEnum;
 	
 	public int machineHP;
 	
 	private ItemStack sapper;
 	
 	protected boolean isProcessor;
 	
 	protected boolean canSendPackets = true;
 	
 	@Override
 	public boolean canReceiveFromSide(ForgeDirection side)
 	{
 		return side == ForgeDirection.getOrientation(this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord)).getOpposite() || side == ForgeDirection.DOWN;
 	}
 	
 	@Override
 	public void onReceive(TileEntity tileEntity, double amps, double voltage, ForgeDirection side)
 	{
 		if (voltage > this.getVoltage())
 		{
 			this.explodeMachine(5.0F);
 		}
 		
 		this.electricityStored += ElectricInfo.getJoules(amps, voltage);
 		
 	}
 	
 	@Override
 	public void updateEntity()
 	{
 		this.facingDirection = ForgeDirection.getOrientation(this.getBlockMetadata());
 		
 		if (!this.worldObj.isRemote)
 		{
 			this.electricityStored = Math.min(this.electricityStored, this.ELECTRICITY_LIMIT);
 			this.electricityStored = Math.max(this.electricityStored, 0);
 			this.machineHP = Math.min(this.machineHP, this.getMaxHP());
 			this.machineHP = Math.max(this.machineHP, 0);
 			
 			if (this.worldObj.getTotalWorldTime() % 3L == 0L && this.canSendPackets)
 			{
 				PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, Vector3.get(this), 8);
 				
 			}
 			
 			if (this.isBeingSapped())
 			{
 				((IHMSapper)this.sapper.getItem()).sapperTick(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.sapper);
 				
 			}
 			
 		}
 		
 	}
 	
 	@Override
 	public void onInventoryChanged()
 	{
 		
 	}
 	
 	@Override
 	public double getVoltage()
 	{
 		return voltage;
 	}
 	
 	protected void explodeMachine(float strength)
 	{
 		this.worldObj.createExplosion((Entity)null, this.xCoord, this.yCoord, this.zCoord, strength, true);
 	}
 	
 	@Override
 	public Packet getDescriptionPacket()
 	{
 		if (this.isProcessor)
 		{
 			return PacketManager.getPacket("HawksMachinery", this, this.workTicks, this.electricityStored, this.machineHP);
 		}
 		
 		return PacketManager.getPacket("HawksMachinery", this, this.electricityStored, this.machineHP);
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
 			if (this.isProcessor)
 			{
 				this.workTicks = dataStream.readInt();
 				
 			}
 			
 			this.electricityStored = dataStream.readDouble();
 			this.machineHP = dataStream.readInt();
 			
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
 	public ItemStack getStackInSlot(int var1)
 	{
 		return this.containingItems != null ? this.containingItems[var1] : null;
 	}
 	
 	@Override
 	public ItemStack decrStackSize(int var1, int var2)
 	{
 		if (this.containingItems[var1] != null)
 		{
 			ItemStack var3;
 			
 			if (this.containingItems[var1].stackSize <= var2)
 			{
 				var3 = this.containingItems[var1];
 				this.containingItems[var1] = null;
 				return var3;
 			}
 			else
 			{
 				var3 = this.containingItems[var1].splitStack(var2);
 				
 				if (this.containingItems[var1].stackSize == 0)
 				{
 					this.containingItems[var1] = null;
 				}
 				
 				return var3;
 			}
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	@Override
 	public ItemStack getStackInSlotOnClosing(int var1)
 	{
 		if (this.containingItems[var1] != null)
 		{
 			ItemStack var2 = this.containingItems[var1];
 			this.containingItems[var1] = null;
 			return var2;
 		}
 		else
 		{
 			return null;
 		}
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
 	public void setInventorySlotContents(int var1, ItemStack var2)
 	{
 		this.containingItems[var1] = var2;
 		
 		if (var2 != null && var2.stackSize > this.getInventoryStackLimit())
 		{
 			var2.stackSize = this.getInventoryStackLimit();
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
 		return true;
 	}
 	
 	@Override
 	public void openChest()
 	{
 		this.isOpen = true;
 		
 	}
 	
 	@Override
 	public void closeChest()
 	{
 		this.isOpen = false;
 		
 	}
 	
 	@Override
 	public ForgeDirection getDirection()
 	{
 		return this.facingDirection;
 	}
 	
 	@Override
 	public void setDirection(ForgeDirection facingDirection)
 	{
 		this.facingDirection = facingDirection;
 		
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound NBTTag)
 	{
 		super.readFromNBT(NBTTag);
 		this.machineHP = NBTTag.getInteger("MachineHP");
 		this.electricityStored = NBTTag.getDouble("electricityStored");
 		if (this.isProcessor) this.workTicks = NBTTag.getInteger("workTicks");
 		
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
 		return 20;
 	}
 	
 	public int getHP()
 	{
 		return this.machineHP;
 	}
 	
 	@Override
 	public double wattRequest()
 	{
 		return this.electricityStored == this.ELECTRICITY_LIMIT ? 0 : this.ELECTRICITY_REQUIRED * 2;
 	}
 	
 }
