 package Iamshortman.DragonsReach.Common.TileEntity;
 
 import Iamshortman.DragonsReach.Common.Block.BlockDragonsReach;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 public class TileEntityRainbowBridgeEmitter extends TileEntity
 {
 
 	public TileEntityRainbowBridgeEmitter()
 	{
 	
 	}
 
 	@Override
 	public void updateEntity()
 	{
 		super.updateEntity();
		if (!this.worldObj.isRemote)
 		{
			int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
			if (this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
			{
				this.EmitBridge(meta);
			}
			else
			{
				this.unEmitBridge(meta);
			}
 		}
 	}
 	
 	private void EmitBridge(int blockMetadata)
 	{
 		// X, Y, and Z movement offsets
 		int offset[] = {0,0,0};
 		switch(blockMetadata)
 		{
 		case 0:
 			//Downward
 			offset[1] = -1;
 			break;
 		case 1:
 			//Upwards
 			offset[1] = 1;
 			break;
 		case 2:
 			//North
 			offset[2] = -1;
 			break;
 		case 3:
 			//South
 			offset[2] = 1;
 			break;
 		case 4:
 			//West
 			offset[0] = -1;
 			break;
 		case 5:
 			//East
 			offset[0] = 1;
 			break;
 		}
 		
 		int pos[] = {this.xCoord, this.yCoord, this.zCoord};
 		
 		//Offsets the coords for the first time before the loop runs
 		offsetPostion(pos,offset);
 		
 		//TODO Make Bridge Length Save for power Consumption
 		for(int i = 0; i < 64; i++)
 		{
 			int ID = this.worldObj.getBlockId(pos[0], pos[1], pos[2]);
 			
 			if(ID != BlockDragonsReach.rainbowBridge.blockID)
 			{
 				if(ID == 0)
 				{
 					this.worldObj.setBlock(pos[0], pos[1], pos[2], BlockDragonsReach.rainbowBridge.blockID, 0, 2);
 				}
 				//Exits the loop after a single block place or if it finds a non-bridge Block.
 				break;
 			}
 			
 			offsetPostion(pos,offset);
 		}
 	}
 
 	private void unEmitBridge(int blockMetadata)
 	{
 		// X, Y, and Z movement offsets
 		int offset[] = {0,0,0};
 		switch(blockMetadata)
 		{
 		case 0:
 			//Downward
 			offset[1] = -1;
 			break;
 		case 1:
 			//Upwards
 			offset[1] = 1;
 			break;
 		case 2:
 			//North
 			offset[2] = -1;
 			break;
 		case 3:
 			//South
 			offset[2] = 1;
 			break;
 		case 4:
 			//West
 			offset[0] = -1;
 			break;
 		case 5:
 			//East
 			offset[0] = 1;
 			break;
 		}
 		
 		int pos[] = {this.xCoord, this.yCoord, this.zCoord};
 		
 		//Offsets the coords for the first time before the loop runs
 		offsetPostion(pos,offset);
 		
 		for(int i = 0; i < 64; i++)
 		{
 			int ID = this.worldObj.getBlockId(pos[0], pos[1], pos[2]);
 			
 			if(ID == BlockDragonsReach.rainbowBridge.blockID)
 			{
 				this.worldObj.setBlockToAir(pos[0], pos[1], pos[2]);
 				//Exits the loop after a single block removal or if it finds a non-bridge Block.
 				break;
 			}
 			
 			offsetPostion(pos,offset);
 		}
 	}
 	
 	private void offsetPostion(int[] pos, int[] offset)
 	{
 		for(int i = 0; i < pos.length; i++)
 		{
 			pos[i] += offset[i];
 		}
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound par1nbtTagCompound)
 	{
 		super.writeToNBT(par1nbtTagCompound);
 	}
 	
 	
 	@Override
 	public void readFromNBT(NBTTagCompound par1nbtTagCompound)
 	{
 		super.readFromNBT(par1nbtTagCompound);
 	}
 }
