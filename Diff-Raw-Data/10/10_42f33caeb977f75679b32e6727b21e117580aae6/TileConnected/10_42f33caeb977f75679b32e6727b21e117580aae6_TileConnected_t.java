 package tpw_rules.connectedmachines.tile;
 
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 
 public class TileConnected extends TileEntity {
     public ForgeDirection facing;
 
     public TileConnected() {
         facing = ForgeDirection.UP;
     }
 
     @Override
     public void readFromNBT(NBTTagCompound tag) {
         super.readFromNBT(tag);
        facing = ForgeDirection.getOrientation(tag.getByte("facing"));
     }
 
     @Override
     public void writeToNBT(NBTTagCompound tag) {
         super.writeToNBT(tag);
         tag.setByte("facing", (byte)facing.ordinal());
     }
 
     @Override
     public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {
         this.readFromNBT(packet.customParam1);
     }
 
     @Override
     public Packet getDescriptionPacket()
     {
         NBTTagCompound tag = new NBTTagCompound();
         this.writeToNBT(tag);
         return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, tag);
     }
 }
