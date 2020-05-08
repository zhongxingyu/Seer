 package pixlepix.complexmachines.common;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Direction;
 import net.minecraft.util.StatCollector;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 import net.minecraftforge.common.MinecraftForge;
 import universalelectricity.core.UniversalElectricity;
 import universalelectricity.core.block.IConductor;
 import universalelectricity.core.block.IElectricityStorage;
 import universalelectricity.core.electricity.ElectricityNetworkHelper;
 import universalelectricity.core.electricity.ElectricityPack;
 import universalelectricity.core.electricity.IElectricityNetwork;
 import universalelectricity.core.item.IItemElectric;
 import universalelectricity.core.vector.Vector3;
 import universalelectricity.core.vector.VectorHelper;
 import universalelectricity.prefab.network.IPacketReceiver;
 import universalelectricity.prefab.network.PacketManager;
 import universalelectricity.prefab.tile.TileEntityElectrical;
 import universalelectricity.prefab.tile.TileEntityElectricityRunnable;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.Loader;
 public class OceanGeneratorTileEntity extends TileEntityElectrical implements IPacketReceiver, IElectricityStorage{
     public final double WATTS_PER_TICK = 5000;
     public final double TRANSFER_LIMIT = 12500;
     private double drawingTicks = 0;
     private double joulesStored = 0;
     public double powerRunning=0;
     public static double maxJoules = 2000000;
     public int ticks=1000;
     /**
      * The ItemStacks that hold the items currently being used in the wire mill;
      * 0 = battery; 1 = input; 2 = output;
      */
     
     private int playersUsing = 0;
     public int orientation;
     private int targetID = 0;
     private int targetMeta = 0;
     
     private boolean initialized;
 	private IConductor connectedElectricUnit;
     
     @Override
     public void initiate()
     {
         this.initialized = true;
         getOceanTiles();
     }
    
 
     public void updateEntity()
 	{
     	//System.out.println("Focal Points have been spawned at "+"  "+xCoord+"  "+yCoord+"  "+zCoord);
 		super.updateEntity();
 		getOceanTiles();
 		if (!this.worldObj.isRemote)
 		{
 				// Check nearby blocks and see if the conductor is full. If so, then it is connected
 				ForgeDirection outputDirection = ForgeDirection.getOrientation(this.getBlockMetadata() + 2);
 				TileEntity outputTile = VectorHelper.getConnectorFromSide(this.worldObj, new Vector3(this.xCoord, this.yCoord, this.zCoord), outputDirection);
 	
 				IElectricityNetwork network = ElectricityNetworkHelper.getNetworkFromTileEntity(outputTile, outputDirection);
 	
 				if (network != null)
 				{
 					if (network.getRequest().getWatts() > 0)
 					{
 						this.connectedElectricUnit = (IConductor) outputTile;
 					}
 					else
 					{
 						this.connectedElectricUnit = null;
 					}
 				}
 				else
 				{
 					this.connectedElectricUnit = null;
 				}
 	
 				if (!this.isDisabled())
 				{
 					
 	
 	
 					if (this.connectedElectricUnit != null)
 					{
 						
 							this.connectedElectricUnit.getNetwork().startProducing(this, (powerRunning / this.getVoltage()) / 20, this.getVoltage());
 						
 						
 					}
 				}
 
 		
 
 		}
 	}
 
     private void getOceanTiles(){
     	//System.out.println(powerRunning);
     	ticks++;
     	if(ticks>2000||powerRunning<10){
     
     		ticks=0;
     		int lowerBoundX=xCoord;
 
     		int lowerBoundY=yCoord;
 
     		int lowerBoundZ=zCoord;
     		
     		int upperBoundX=xCoord;
 
     		int upperBoundY=yCoord;
 
     		int upperBoundZ=zCoord;
     			lowerBoundX=xCoord-30;
     			
         		lowerBoundY=yCoord-30;
 
         		lowerBoundZ=zCoord-30;
 
         		upperBoundX=xCoord+30;
 
         		upperBoundY=yCoord+30;
 
         		upperBoundZ=zCoord+30;
     		
         		for(int cycleX=lowerBoundX;cycleX<upperBoundX;cycleX++){
         			for(int cycleY=lowerBoundY;cycleY<upperBoundY;cycleY++){
         				for(int cycleZ=lowerBoundZ;cycleZ<upperBoundZ;cycleZ++){
                 			if(worldObj.getBlockId(cycleX,cycleY,cycleZ)==0){
                				powerRunning+=0.0025;
                 			}
                 		}
             		}
         		}
     		
     	}
     	
     }
 
 	@Override
     public void handlePacketData(INetworkManager inputNetwork, int type, Packet250CustomPayload packet,
             EntityPlayer player, ByteArrayDataInput dataStream)
     {
         try
         {
             this.drawingTicks = dataStream.readInt();
             this.disabledTicks = dataStream.readInt();
             this.joulesStored = dataStream.readDouble();
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
     }    
 
     @Override
     public double getVoltage()
     {
         return 120;
     }
     
     
     @Override
     public double getJoules()
     {
         return this.joulesStored;
     }
     
     @Override
     public void setJoules(double joules)
     {
         this.joulesStored = joules;
     }
     
     @Override
     public double getMaxJoules()
     {
         return FillerMachineTileEntity.maxJoules;
     }   
     @Override
     public boolean canConnect(ForgeDirection direction)
     {
         return direction.ordinal() == this.getBlockMetadata() + 2;
     }
     
     
 }
