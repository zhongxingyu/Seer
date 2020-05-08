 package dk.slashwin.openframe;
 
 import cpw.mods.fml.common.Mod;
 import dk.slashwin.openframe.common.CoordTuple;
 import dk.slashwin.openframe.lib.BlockIDs;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 
 /**
  * User: DelusionalLogic
  * Date: 19-04-13
  * Time: 21:02
  */
 public class TileFrame extends TileEntity
 {
 	int sideData = 0;
 
 	public void setSide(int side)
 	{
 		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 		sideData ^= 1 << side;
 	}
 
 	public boolean getSide(int side)
 	{
 		return (sideData >> side & 1) == 1;
 	}
 
     public LinkedHashSet<CoordTuple> getNeighbors(LinkedHashSet<CoordTuple> checked)
     {
	    int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

         for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
         {
             if(!getSide(direction.ordinal()))
                 continue;
             int blockX = xCoord + direction.offsetX;
             int blockY = yCoord + direction.offsetY;
             int blockZ = zCoord + direction.offsetZ;
 
             int blockID = worldObj.getBlockId(blockX, blockY, blockZ);
 
             if(!checked.contains(new CoordTuple(blockX, blockY, blockZ)) && blockID != 0)
             {
                 TileEntity tileEntity = worldObj.getBlockTileEntity(blockX, blockY, blockZ);
                 if(tileEntity != null && tileEntity instanceof ISpecialFrameBlock)
                 {
                     if(((ISpecialFrameBlock)tileEntity).stickOnSide(direction.getOpposite().ordinal()))
                         checked.add(new CoordTuple(blockX, blockY, blockZ));
                 }
                 else
                 {
                     checked.add(new CoordTuple(blockX, blockY, blockZ));
                 }
 
                 if(blockID == BlockIDs.frameID)
                 {
                     TileEntity te = worldObj.getBlockTileEntity(blockX, blockY, blockZ);
                     if(te instanceof TileFrame)
                     {
                         checked = ((TileFrame) te).getNeighbors(checked);
                     }
                 }
             }
         }
         return checked;
     }
 
 	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
 	{
 		super.readFromNBT(par1NBTTagCompound);
 		sideData = par1NBTTagCompound.getInteger("sideData");
 	}
 
 	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
 	{
 		super.writeToNBT(par1NBTTagCompound);
 		par1NBTTagCompound.setInteger("sideData", sideData);
 	}
 
 	@Override
 	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt){
 		readFromNBT(pkt.customParam1);
 		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
 	}
 
 	@Override
 	public Packet getDescriptionPacket() {
 		NBTTagCompound nbttagcompound = new NBTTagCompound();
 		this.writeToNBT(nbttagcompound);
 		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
 	}
 }
