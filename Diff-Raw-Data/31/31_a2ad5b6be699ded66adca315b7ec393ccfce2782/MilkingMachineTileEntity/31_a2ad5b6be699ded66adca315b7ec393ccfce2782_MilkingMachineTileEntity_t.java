 package biotech.tileentity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import liquidmechanics.api.IColorCoded;
 import liquidmechanics.api.IReadOut;
 import liquidmechanics.api.helpers.ColorCode;
 import liquidmechanics.api.liquids.IPressure;
 import liquidmechanics.api.liquids.LiquidData;
 import liquidmechanics.api.liquids.LiquidHandler;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.passive.EntityCow;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.liquids.ITankContainer;
 import net.minecraftforge.liquids.LiquidContainerRegistry;
 import universalelectricity.core.UniversalElectricity;
 import universalelectricity.core.electricity.ElectricityNetwork;
 import universalelectricity.core.electricity.ElectricityPack;
 import universalelectricity.core.implement.IItemElectric;
 import universalelectricity.core.vector.Vector3;
 import universalelectricity.prefab.network.IPacketReceiver;
 import universalelectricity.prefab.network.PacketManager;
 import biotech.Biotech;
 
 import com.google.common.io.ByteArrayDataInput;
 
 
 public class MilkingMachineTileEntity extends BasicMachineTileEntity implements IPacketReceiver, IColorCoded, IPressure, IReadOut
 {
         private int tickCounter;
         private int scantickCounter;
         private int drainCounter;
 
         private MilkingManagerTileEntity ManagerEntity;
         protected List<EntityLiving> CowList = new ArrayList<EntityLiving>();
  
         // Watts being used per action / idle action
         public static final double WATTS_PER_TICK = 250;
         public static final double WATTS_PER_IDLE_TICK = 25;
  
         // Time idle after a tick
         public static final int IDLE_TIME_AFTER_ACTION = 60;
         public static final int IDLE_TIME_NO_ACTION = 30;
  
         // Watts being used per pump action
         public static final double WATTS_PER_PUMP_ACTION = 50;
  
         // How much power is stored?
         private double electricityStored = 0;
         private double electricityMaxStored = 5000;
  
         private boolean isMilking = false;
  
         // Is the machine currently powered, and did it change?
         public boolean prevIsPowered, isPowered = false;
  
         // Amount of milliBuckets of internal storage
         private ColorCode color = ColorCode.WHITE;
         private static final int milkMaxStored = 3 * LiquidContainerRegistry.BUCKET_VOLUME;
         private int milkStored = 0;
  
         private int facing;
         private int playersUsing = 0;
         private int idleTicks;
         
         private int managerX = 0;
         private int managerY = 0;
         private int managerZ = 0;
  
         public int currentX = 0;
         public int currentZ = 0;
         public int currentY = 0;
  
         public int minX, maxX;
         public int minZ, maxZ;
  
         public MilkingMachineTileEntity()
         {
                 super();
         }
  
         public void scanForCows()
         {
                 int xminrange = xCoord - getMilkRange();
                 int xmaxrange = xCoord + getMilkRange() + 1;
                 int yminrange = yCoord - getMilkRange();
                 int ymaxrange = yCoord + getMilkRange() + 1;
                 int zminrange = zCoord - getMilkRange();
                 int zmaxrange = zCoord + getMilkRange() + 1;
  
                 List<EntityCow> scannedCowslist = worldObj.getEntitiesWithinAABB(EntityCow.class, AxisAlignedBB.getBoundingBox(xminrange, yminrange, zminrange, xmaxrange, ymaxrange, zmaxrange));
  
                 for (EntityCow Living : scannedCowslist)
                 {
                         if (!CowList.contains(Living))
                         {
                                 CowList.add(Living);
                         }
                 }
         }
         
         public void scanForManager()
         {
         	int xminrange = xCoord - getScanRange();
             int xmaxrange = xCoord + getScanRange() + 1;
             int yminrange = yCoord - getScanRange();
             int ymaxrange = yCoord + getScanRange() + 1;
             int zminrange = zCoord - getScanRange();
             int zmaxrange = zCoord + getScanRange() + 1;
             
             for (int xx = xminrange; xx <= xmaxrange; xx++)
     		{
     			for (int yy = yminrange; yy <= ymaxrange; yy++)
     			{
     				for (int zz = zminrange; zz <= zmaxrange; zz++)
     				{
     					TileEntity tileEntity = worldObj.getBlockTileEntity(xx, yy, zz);
 
     					if (tileEntity instanceof MilkingManagerTileEntity)
     					{
     						ManagerEntity = (MilkingManagerTileEntity) tileEntity;
     					}
     				}
     			}
     		}
         	
         	
         }
  
         public void milkCows()
         {
                 if (CowList.size() != 0)
                 {
                         CowList.remove(0);
                         this.milkStored += 250;
                        this.setElectricityStored(this.electricityStored -= 500);
                 }
         }
  
         public int getMilkRange()
         {
         	return 3;
         }
         
         public int getScanRange()
     	{
     		if (getStackInSlot(1) != null)
     		{
     			if (inventory[1].isItemEqual(Biotech.bioCircuitRangeUpgrade)) { return (getStackInSlot(1).stackSize + 5); }
     		}
     		return 3;
     	}
  
         @Override
         public void updateEntity()
         {
             if (!worldObj.isRemote)
             {
                     if (this.GetRedstoneSignal())
                     {
                         this.setPowered(true);
                         if (scantickCounter >= 40)
                         {
                                 scanForCows();
                                if(ManagerEntity == null)
                                {
                                	scanForManager();
                                }
                                 scantickCounter = 0;
                         }
                         if (CowList.size() != 0 && tickCounter >= 100)
                         {
                                 milkCows();
                                 isMilking = true;
                                 tickCounter = 0;
                                 this.setPowered(false);
                         }
                         System.out.println("Machine:" + milkStored);
                         tickCounter++;
                        // drainCounter++;
                         //if(drainCounter >= 60)
                        // {
                        	drainTo();
                         	//drainCounter = 0;
                        // }
                     }
                     if (tickCounter >= 150)
                     {
                             tickCounter = 0;
                     }
                     if (scantickCounter >= 100)
                     {
                             tickCounter = 0;
                     }
                     
                     this.chargeUp();
                     scantickCounter++;
                     if (this.ticks % 3 == 0 && this.playersUsing > 0)
                     {
                             PacketManager.sendPacketToClients(getDescriptionPacket(), this.worldObj, new Vector3(this), 12);
                     }
             }
             super.updateEntity();
         }
         /**
          * Use this to just drain to the pipe or liquid tank bellow your milker
          */
         public void drainTo()
         {
             TileEntity ent = worldObj.getBlockTileEntity(xCoord, yCoord-1, xCoord);
             if(ent != null && ent instanceof ITankContainer)
             {
                     int vol = LiquidContainerRegistry.BUCKET_VOLUME;
                     if(this.milkStored < vol)
                     {
                     	vol = this.milkStored;
                     }
                    int filled = ((ITankContainer) ent).fill(0, LiquidHandler.getStack(color.getLiquidData(), vol), true);
                     this.milkStored -= filled;
                     System.out.println("filled: " + filled);
             }
         }
         public boolean GetRedstoneSignal()
         {
        	if (worldObj.isBlockGettingPowered(xCoord, yCoord, zCoord) || worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) { return true; }
    		return false;
         }
  
         @Override
         public void readFromNBT(NBTTagCompound tagCompound)
         {
                 super.readFromNBT(tagCompound);
                 //vars
                 this.facing = tagCompound.getShort("facing");
                 this.isPowered = tagCompound.getBoolean("isPowered");
                 this.electricityStored = tagCompound.getDouble("electricityStored");
                 this.milkStored = tagCompound.getInteger("milkStored");
  
                 //inventory
                 NBTTagList tagList = tagCompound.getTagList("Inventory");
                 for (int i = 0; i < tagList.tagCount(); i++)
                 {
                         NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
                         byte slot = tag.getByte("Slot");
  
                         if (slot >= 0 && slot < inventory.length)
                         {
                                 inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
                         }
                 }
         }
  
         @Override
         public void writeToNBT(NBTTagCompound tagCompound)
         {
                 super.writeToNBT(tagCompound);
                 //vars
                 tagCompound.setShort("facing", (short) this.facing);
                 tagCompound.setBoolean("isPowered", this.isPowered);
                 tagCompound.setDouble("electricityStored", this.electricityStored);
                 tagCompound.setInteger("milkStored", this.milkStored);
  
                 //inventory
                 NBTTagList itemList = new NBTTagList();
                 for (int i = 0; i < inventory.length; i++)
                 {
                         ItemStack stack = inventory[i];
  
                         if (stack != null)
                         {
                                 NBTTagCompound tag = new NBTTagCompound();
                                 tag.setByte("Slot", (byte) i);
                                 stack.writeToNBT(tag);
                                 itemList.appendTag(tag);
                         }
                 }
                 tagCompound.setTag("Inventory", itemList);
         }
  
         @Override
         public String getInvName()
         {
                 return "Milking Machine";
         }
  
         @Override
         public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
         {
                 try
                 {
                         if (this.worldObj.isRemote)
                         {
                                 this.isPowered = dataStream.readBoolean();
                                 this.facing = dataStream.readInt();
                                 this.electricityStored = dataStream.readDouble();
                                 this.milkStored = dataStream.readInt();
                         }
                 }
                 catch (Exception e)
                 {
                         e.printStackTrace();
                 }
         }
  
         @Override
         public Packet getDescriptionPacket()
         {
                 return PacketManager.getPacket(Biotech.CHANNEL, this, this.isPowered, this.facing, this.electricityStored, this.milkStored);
         }
  
         public int getFacing()
         {
                 return facing;
         }
  
         public void setFacing(int facing)
         {
                 this.facing = facing;
         }
  
         public double getElectricityStored()
         {
                 return electricityStored;
         }
  
         public void setElectricityStored(double joules)
         {
                 electricityStored = Math.max(Math.min(joules, getMaxElectricity()), 0);
         }
  
         public double getMaxElectricity()
         {
                 return electricityMaxStored;
         }
         
         public int getMilkStored()
         {
                 return this.milkStored;
         }
  
         @Override
         public ColorCode getColor()
         {
                 return ColorCode.WHITE;
         }
  
         @Override
         public void setColor(Object obj)
         {
                 // leave this blank unless you plan on having it change or be changed
         }
  
         @Override
         public int presureOutput(LiquidData type, ForgeDirection dir)
         {
                 if (type.getColor() == color){ return type.getPressure();}
                
                 return 0;
         }
  
         @Override
         public boolean canPressureToo(LiquidData type, ForgeDirection dir)
         {
                 if (type.getColor() == color && dir == ForgeDirection.DOWN.getOpposite()){ return true;}
                
                 return false;
         }
 
 		@Override
 		public String getMeterReading(EntityPlayer user, ForgeDirection side)
 		{
 			return "Milk:"+this.milkStored;
 		}
 }
