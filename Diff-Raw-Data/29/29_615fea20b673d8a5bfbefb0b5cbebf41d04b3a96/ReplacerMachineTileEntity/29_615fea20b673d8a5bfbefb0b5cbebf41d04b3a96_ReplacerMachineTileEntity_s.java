 package pixlepix.complexmachines.common;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityChest;
 import net.minecraft.util.Direction;
 import net.minecraft.util.StatCollector;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 import net.minecraftforge.common.MinecraftForge;
 import universalelectricity.core.UniversalElectricity;
 import universalelectricity.core.block.IElectricityStorage;
 import universalelectricity.core.electricity.ElectricityNetworkHelper;
 import universalelectricity.core.electricity.ElectricityPack;
 import universalelectricity.core.electricity.IElectricityNetwork;
 import universalelectricity.core.item.IItemElectric;
 import universalelectricity.core.vector.Vector3;
 import universalelectricity.core.vector.VectorHelper;
 import universalelectricity.prefab.network.IPacketReceiver;
 import universalelectricity.prefab.network.PacketManager;
 import universalelectricity.prefab.tile.TileEntityElectricityRunnable;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import cpw.mods.fml.common.Loader;
 public class ReplacerMachineTileEntity extends TileEntityElectricityRunnable implements IPacketReceiver, IElectricityStorage{
     public final double WATTS_PER_TICK = 5000;
     public final double TRANSFER_LIMIT = 125000;
     private int drawingTicks = 0;
     private double joulesStored = 0;
     public static double maxJoules = 5000000;
     public final int COST_ON_ORE=5000000;
     public final int COST_ON_MISS=2500;
     public int ticks;
     private ItemStack[] inventory = new ItemStack[3];
     Random rand = new Random();
     private int playersUsing = 0;
     public int orientation;
     private int targetID = 0;
     private int targetMeta = 0;
     public final int[] ORE_LIST={14,15,16,21,56,73,129,458,688,3002,3880,3970,3989}; 
     private boolean initialized;
     public boolean isOre(int id){
     	for(int i=0;i<ORE_LIST.length;i++){
     		if(ORE_LIST[i]==id){
     			return true;
     		}
     	}
     	return false;
     }
     @Override
     public void initiate()
     {
         this.initialized = true;
     } 
     
     public boolean chestsSetUp(){
     	if(worldObj.getBlockTileEntity(xCoord,yCoord-1,zCoord) instanceof TileEntityChest&&worldObj.getBlockTileEntity(xCoord,yCoord+1,zCoord) instanceof TileEntityChest){
     		return true;
     	}
     	return false;
     }
     
     public int getBlockFromChest(TileEntityChest chest){
     	
     	for(int i=0;i<chest.getSizeInventory();i++){
     		if(chest.getStackInSlot(i)!=null){
 	    		int id=chest.getStackInSlot(i).itemID;
 	    		if(Block.blocksList.length>id&&Block.blocksList[id]!=null){
 	    			return chest.getStackInSlot(i).itemID;
 	    		}
     		}
     	}
     	
     	return 0;
     	
     }
     
     public int takeBlockFromChest(TileEntityChest chest, int id){
     	
     	for(int i=0;i<chest.getSizeInventory();i++){
     		if(chest.getStackInSlot(i)!=null){
 	    		if(chest.getStackInSlot(i).itemID==id){
 	    			if(chest.getStackInSlot(i).stackSize>1){
 	    				chest.getStackInSlot(i).stackSize--;
 	    			}else{
 	    				chest.setInventorySlotContents(i, null);
 	    			}
 	    		}	
     		}
     	}
     	
     	return 0;
     	
     }
     
     @Override
     public void updateEntity()
     {   
     	//System.out.println("Recieving ticks");
         super.updateEntity();
         
         if (!this.worldObj.isRemote)
         {
             ForgeDirection inputDirection = ForgeDirection.getOrientation(this.getBlockMetadata() + 2);
             TileEntity inputTile = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), inputDirection);
             
             IElectricityNetwork inputNetwork = ElectricityNetworkHelper.getNetworkFromTileEntity(inputTile,
                     inputDirection.getOpposite());
             
             if (inputNetwork != null)
             {
                 if (this.joulesStored < ReplacerMachineTileEntity.maxJoules)
                 {
 
                     inputNetwork.startRequesting(this,
                             Math.min(this.getMaxJoules() - this.getJoules(), this.TRANSFER_LIMIT) / this.getVoltage(),
                             this.getVoltage());
                     ElectricityPack electricityPack = inputNetwork.consumeElectricity(this);
                     this.setJoules(this.joulesStored + electricityPack.getWatts());
                     
                     
                     if (UniversalElectricity.isVoltageSensitive)
                     {
                         if (electricityPack.voltage > this.getVoltage())
                         {
                             this.worldObj.createExplosion(null, this.xCoord, this.yCoord, this.zCoord, 2f, true);
                         }
                     }
                 }
                 
                 
                 if(getJoules()>10000)
                 	
                 {
                 	ticks++;
                 	if(ticks>2){
                 	ticks=0;
                	if(worldObj.getBlockTileEntity(xCoord,yCoord+1,zCoord)!=null){
                 		TileEntityChest inputChest=(TileEntityChest)worldObj.getBlockTileEntity(xCoord,yCoord+1,zCoord);
                 		int idToReplace=getBlockFromChest(inputChest);
                 		int lowerBoundX=xCoord;
 
                 		int lowerBoundY=yCoord;
 
                 		int lowerBoundZ=zCoord;
                 		
                 		int upperBoundX=xCoord;
 
                 		int upperBoundY=yCoord;
 
                 		int upperBoundZ=zCoord;
                 		
                 		lowerBoundX=xCoord-50;
                 			
 	                	lowerBoundY=yCoord-2;
 	
 	                	lowerBoundZ=zCoord-50;
 	
 	                	upperBoundX=xCoord+50;
 	
 	                	upperBoundY=yCoord+50;
 
 	                	upperBoundZ=zCoord+50;
 	                	if(idToReplace!=0){
 		                	for(int cycleX=lowerBoundX;cycleX<upperBoundX;cycleX++){
 	                			for(int cycleY=lowerBoundY;cycleY<upperBoundY;cycleY++){
 	                				for(int cycleZ=lowerBoundZ;cycleZ<upperBoundZ;cycleZ++){
 	                        			if(worldObj.getBlockId(cycleX,cycleY,cycleZ)==1){
 	                        				worldObj.setBlock(cycleX, cycleY, cycleZ, idToReplace);
 	                        				//System.out.println("X: "+cycleX+"Y: "+cycleY+"Z: "+cycleZ);
 	                        				setJoules(getJoules()-10000);
 	                        				takeBlockFromChest(inputChest,idToReplace);
 	                        				return;
 	                        			}
 	                        		}
 	                    		}
 	                		}
 	                	}
 	                	
 	                	
 	                	
 	                	
 	                	
                 	}}	
                 }
                 }
                 
             }
         
         
         
         
         
         if (!this.worldObj.isRemote)
         {
             if (this.ticks % 3 == 0 && this.playersUsing > 0)
             {
                 PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 12);
             }
         }
         
         this.joulesStored = Math.min(this.joulesStored, this.getMaxJoules());
         this.joulesStored = Math.max(this.joulesStored, 0d);
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
         return 480;
     }
     public int getDrawingTimeLeft()
     {
         return this.drawingTicks;
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
         return ReplacerMachineTileEntity.maxJoules;
     }   
     @Override
     public boolean canConnect(ForgeDirection direction)
     {
     	
         return direction.ordinal() == this.getBlockMetadata() + 2;
     }
 }
     
