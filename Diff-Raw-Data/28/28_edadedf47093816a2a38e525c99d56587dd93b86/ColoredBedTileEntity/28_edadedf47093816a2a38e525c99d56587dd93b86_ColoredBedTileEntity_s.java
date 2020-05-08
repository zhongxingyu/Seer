 package loecraftpack.blocks.te;
 
 import loecraftpack.logic.handlers.ColoredBedHandler;
 import net.minecraft.block.BlockBed;
 import net.minecraft.block.BlockDirectional;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public class ColoredBedTileEntity extends TileEntity
 {
 	public int id = 0;
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
		pairName = nbt.getString("pair");			
     }
 	
 	@Override
 	public void writeToNBT(NBTTagCompound nbt)
     {		
 		super.writeToNBT(nbt);
 		nbt.setInteger("bedId", id);
 		nbt.setString("pair", pairName);
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
 		//TODO Compact separate checks into one
 	    TileEntity te = world.getBlockTileEntity(xFoot, yFoot, zFoot);
 	    if (te != null && te instanceof ColoredBedTileEntity)
 	    	((ColoredBedTileEntity)te).updatePairName();
 	    te = world.getBlockTileEntity(xHead, yHead, zHead);
 	    if (te != null && te instanceof ColoredBedTileEntity)
 	    	((ColoredBedTileEntity)te).updatePairName();
 	}
 	
 	public static void finishTileRemoval(World world, int x, int y, int z, int meta)
 	{
 		//DO NOT compact this
 		int dir = BlockBed.getDirection(meta);
 		
 		TileEntity te = ColoredBedTileEntity.locateAdjacentTile(world, x, y, z, dir + 1);
 		if (te != null && te instanceof ColoredBedTileEntity)
 	    	((ColoredBedTileEntity)te).updatePairName();
 		
 		te = ColoredBedTileEntity.locateAdjacentTile(world, x, y, z, dir - 1);
 		if (te != null && te instanceof ColoredBedTileEntity)
 	    	((ColoredBedTileEntity)te).updatePairName();
 	}
 	
 	//is only to be called, if a bed triggers a block update.  ONLY!!!!!
 	public void updatePairName()
 	{
 		if (worldObj == null)
 			return;
 		updatePairNameLogic();
 		System.out.println("Pair (" + worldObj.isRemote + "): " + pairName);
 	}
 	
 	private void updatePairNameLogic()
 	{
 		if (worldObj == null)//how did this bug occur?
 			return;
 		
         int dir = BlockDirectional.getDirection(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
         
 		String name = ColoredBedHandler.iconNames.get(id);; //This bed's name
 		
 		if(this.pairName != "")
 		{
 			/**do the following if assigned already**/
 			
 			int dirPre = ColoredBedHandler.findPairDirection(this.pairName, name);// -1: left , 0: null , 1: right
 			if(dirPre != 0)
 			{
 				//check if it's still there
 				String pairName = checkSideForPossiblePairName(dirPre, dir, name);
 				
 	        	if(pairName == this.pairName)
         		{
 	        		//partner still exists - nothing changes
	        		System.out.println("maintaing :" + pairName + "   head:" + BlockBed.isBlockHeadOfBed(worldObj.getBlockMetadata(xCoord, yCoord, zCoord)));
 	        		return; 
         		}
 	        	else
	        		System.out.println();
 	        	
 	        	//Attempt to rebond to target
 	        	if(attemptToBond(dirPre, dir, name))return;
 	        	
 		        //no partner found at intended location - check other side for new partner
 		        if(dirPre == 1)
 		        	dirPre = -1;
 		        else
 		        	dirPre = 1;
 		        //Attempt to bond on other side
 		        if(attemptToBond(dirPre, dir, name))return;
 		        
 		        //no pairs available - reset pairName to ""
 		        System.out.print("no avail - 1");
 		        this.pairName = "";
 			}
 			
 		}
 		else
 		{
 			/**do the following if not assigned**/
 			
 			//try to bind to the right
 			if(attemptToBond(1, dir, name))return;
 	        
 			//try to bond to the left
 			if(attemptToBond(-1, dir, name))return;
 			System.out.print("no avail - 2");
 		}
 		return;
 	}
 	
 	private String checkSideForPossiblePairName(int side, int dir, String name)
 	{
 		System.out.print("still bonded?   ");
 		String pairName = "";
         TileEntity te = ColoredBedTileEntity.locateAdjacentTile(worldObj, xCoord, yCoord, zCoord, dir + side);
         if (te !=null && te instanceof ColoredBedTileEntity)
         {
         	if (side == 1) pairName = ColoredBedHandler.getPairName(name, ColoredBedHandler.iconNames.get(((ColoredBedTileEntity)te).id));
         	if (side == -1) pairName = ColoredBedHandler.getPairName(ColoredBedHandler.iconNames.get(((ColoredBedTileEntity)te).id), name);
         }
         return pairName;
 	}
 	
 	private boolean attemptToBond(int side, int dir, String name)
 	{
 		boolean head = BlockBed.isBlockHeadOfBed(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
 		String pairName = "";
 		int dirT = side + dir;
         if (dirT > 3)
         	dirT -= 4;
         else if (dirT < 0)
         	dirT += 4;
         TileEntity te = worldObj.getBlockTileEntity(xCoord + BlockBed.footBlockToHeadBlockMap[dirT][0], yCoord, zCoord + BlockBed.footBlockToHeadBlockMap[dirT][1]);
         if (te !=null && te instanceof ColoredBedTileEntity)
         {
         	ColoredBedTileEntity cte = (ColoredBedTileEntity)te;
         	
         	if (side == 1) pairName = ColoredBedHandler.getPairName(name, ColoredBedHandler.iconNames.get((cte).id));
         	if (side == -1) pairName = ColoredBedHandler.getPairName(ColoredBedHandler.iconNames.get((cte).id), name);
         	System.out.print("target: "+(cte).pairName+" - ");
         	  /*valid pairing*/   /*available*/                    /*Facing the right direction*/                     /*correct part of bed*/
         	if(pairName != "" && (cte).pairName == "" && BlockBed.getDirection(cte.getBlockMetadata()) == dir  &&  head == BlockBed.isBlockHeadOfBed(cte.getBlockMetadata()))
         	{
         		//new partner - make sure this new partner is aware of the change
                 this.pairName = pairName;
                 setAdjacentBedPairName(dirT, pairName);
                 System.out.println("bonding :" + pairName + "   side:" + side + "   head:" + head);
         		return true;
         	}
         	System.out.println();
         }
         return false;
 	}
 	
 	private void setAdjacentBedPairName(int dir, String newPairName)
 	{
 		System.out.print("setting adjacent   R:"+worldObj.isRemote+"  ");
 		TileEntity te = ColoredBedTileEntity.locateAdjacentTile(worldObj, xCoord, yCoord, zCoord, dir);
 		if (te != null && te instanceof ColoredBedTileEntity)
         	((ColoredBedTileEntity)te).pairName = newPairName;
 	}
 	
 	
 }
