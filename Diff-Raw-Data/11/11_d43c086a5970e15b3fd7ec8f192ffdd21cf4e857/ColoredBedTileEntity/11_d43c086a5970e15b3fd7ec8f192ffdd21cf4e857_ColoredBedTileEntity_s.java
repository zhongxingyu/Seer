 package loecraftpack.blocks.te;
 
 import loecraftpack.logic.handlers.ColoredBedHandler;
 import loecraftpack.packethandling.PacketHelper;
 import loecraftpack.packethandling.PacketIds;
 import net.minecraft.block.BlockBed;
 import net.minecraft.block.BlockDirectional;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class ColoredBedTileEntity extends TileEntity
 {
 	public int id = 0;
 	public int pairID = -1;
 	public int pairSide = 0;//1 for right, -1 for left
 	public String pairName = "";
 	
 	public ColoredBedTileEntity()
 	{}
 	
 	public ColoredBedTileEntity(int id)
 	{
 		this.id = id;
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound nbt)
     {
 		super.readFromNBT(nbt);
 		id = nbt.getInteger("bedId");
 		pairID = nbt.getInteger("pairId");
 		findPairData();
     }
 	
 	@Override
 	public void writeToNBT(NBTTagCompound nbt)
     {		
 		super.writeToNBT(nbt);
 		nbt.setInteger("bedId", id);
 		nbt.setInteger("pairId", pairID);
     }
 	
 	@Override
 	public Packet getDescriptionPacket()
     {
 		NBTTagCompound nbt = new NBTTagCompound();
 		writeToNBT(nbt);
 		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
     }
 	
 	@Override
 	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
 	{
 		readFromNBT(packet.customParam1);
 	}
 	
 	
 	
 	public static TileEntity locateAdjacentTile(World world, int x, int y, int z, int dir)
 	{
         if (dir > 3)
         	dir -= 4;
         else if (dir < 0)
         	dir += 4;
 		return world.getBlockTileEntity(x + BlockBed.footBlockToHeadBlockMap[dir][0], y, z + BlockBed.footBlockToHeadBlockMap[dir][1]);
 	}
 	
 	public static void finishTileCreation(World world, int xFoot, int yFoot, int zFoot, int xHead, int yHead, int zHead)
 	{
 		//TODO Compact separate checks into one,   Maybe
 	    TileEntity te = world.getBlockTileEntity(xFoot, yFoot, zFoot);
 	    if (te != null && te instanceof ColoredBedTileEntity)
 	    	((ColoredBedTileEntity)te).updatePairData();
 	    te = world.getBlockTileEntity(xHead, yHead, zHead);
 	    if (te != null && te instanceof ColoredBedTileEntity)
 	    	((ColoredBedTileEntity)te).updatePairData();
 	}
 	
 	public static void finishTileRemoval(World world, int x, int y, int z, int meta)
 	{
 		//DO NOT compact this
 		int dir = BlockBed.getDirection(meta);
 		
 		TileEntity te = ColoredBedTileEntity.locateAdjacentTile(world, x, y, z, dir + 1);
 		if (te != null && te instanceof ColoredBedTileEntity)
 	    	((ColoredBedTileEntity)te).updatePairData();
 		
 		te = ColoredBedTileEntity.locateAdjacentTile(world, x, y, z, dir - 1);
 		if (te != null && te instanceof ColoredBedTileEntity)
 	    	((ColoredBedTileEntity)te).updatePairData();
 	}
 	
 	
 	
 	public void updatePairData()
 	{
 		if (worldObj == null)
 			return;
 		updatePairNameLogic();
 		findPairData();
 	}
 	
 	public void findPairData()
 	{
 		if(pairID != -1)
 		{
 			pairName = ColoredBedHandler.getPairName(pairID);
 			pairSide = -ColoredBedHandler.findPairDirection(pairID, id);
 		}
 		else
 		{
 			pairName = "";
 			pairSide = 0;
			tellClientOfChange();//bug fix: client update on block break
 		}
 	}
 	
 	private void updatePairNameLogic()
 	{
 		if (worldObj == null)//how did this bug occur?
 			return;
 		
         int dir = BlockDirectional.getDirection(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
 		
 		if(this.pairID != -1)
 		{
 			/**do the following if assigned already**/
 			
 			int dirPre = ColoredBedHandler.findPairDirection(this.pairID, id);// -1: left , 0: null , 1: right
 			if(dirPre != 0)
 			{
 				//check if it's still there
 				int pairID = checkSideForPossiblePairID(dirPre, dir);
 				
 	        	if( pairID == this.pairID )
 	        		return; //partner still exists - nothing changes
 	        	
 	        	//Attempt to rebond to target
 	        	if(attemptToBond(dirPre, dir))return;
 	        	
 		        //no partner found at intended location - check other side for new partner
 		        if(dirPre == 1)
 		        	dirPre = -1;
 		        else
 		        	dirPre = 1;
 		        //Attempt to bond on other side
 		        if(attemptToBond(dirPre, dir))return;
 		        
 		        //no pairs available - reset pairName to ""
 		        this.pairID = -1;
 			}
 			
 		}
 		else
 		{
 			/**do the following if not assigned**/
 			
 			//try to bind to the right
 			if(attemptToBond(1, dir))return;
 	        
 			//try to bond to the left
 			if(attemptToBond(-1, dir))return;
 		}
 		return;
 	}
 
 	private int checkSideForPossiblePairID(int side, int dir)
 	{
 		int pairID = -1;
         TileEntity te = locateAdjacentTile(worldObj, xCoord, yCoord, zCoord, dir + side);
         if (te !=null && te instanceof ColoredBedTileEntity)
         {
         	if (side == 1) pairID = ColoredBedHandler.getPairID(id, ((ColoredBedTileEntity)te).id);
         	if (side == -1) pairID = ColoredBedHandler.getPairID(((ColoredBedTileEntity)te).id, id);
         }
         return pairID;
 	}
 	
 	private boolean attemptToBond(int side, int dir)
 	{
 		boolean head = BlockBed.isBlockHeadOfBed(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
 		int pairID = -1;
 		int dirT = side + dir;
         if (dirT > 3)
         	dirT -= 4;
         else if (dirT < 0)
         	dirT += 4;
         TileEntity te = worldObj.getBlockTileEntity(xCoord + BlockBed.footBlockToHeadBlockMap[dirT][0], yCoord, zCoord + BlockBed.footBlockToHeadBlockMap[dirT][1]);
         if (te !=null && te instanceof ColoredBedTileEntity)
         {
         	ColoredBedTileEntity cte = (ColoredBedTileEntity)te;
         	
         	if (side == 1) pairID = ColoredBedHandler.getPairID(id, cte.id);
         	if (side == -1) pairID = ColoredBedHandler.getPairID(cte.id, id);
         	  /*valid pairing*/   /*available*/                    /*Facing the right direction*/                     /*correct part of bed*/
         	if(pairID != -1 && (cte).pairID == -1 && BlockBed.getDirection(cte.getBlockMetadata()) == dir  &&  head == BlockBed.isBlockHeadOfBed(cte.getBlockMetadata()))
         	{
         		//new partner - make sure this new partner is aware of the change
                 this.pairID = pairID;
                 setAdjacentBedPair(dirT, pairID);
         		return true;
         	}
         }
         return false;
 	}
 	
 	private void setAdjacentBedPair(int dir, int newPairID)
 	{
 		TileEntity te = locateAdjacentTile(worldObj, xCoord, yCoord, zCoord, dir);
 		if (te != null && te instanceof ColoredBedTileEntity)
 		{
 			((ColoredBedTileEntity)te).pairID = newPairID;
         	((ColoredBedTileEntity)te).findPairData();
         	((ColoredBedTileEntity)te).tellClientOfChange();
 		}
 	}
 	
 	//used to tell the client that, the bed has had data changed by the adjacent bed
 	private void tellClientOfChange()
 	{
 		if (worldObj != null && !worldObj.isRemote)
 		{
 			PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 64.0D, worldObj.provider.dimensionId,
 					                               PacketHelper.Make("loecraftpack", PacketIds.bedUpdate,
 					                                                 xCoord, yCoord, zCoord,
 					                                                 pairID, pairSide));
 		}
 	}
 	
 	
 }
