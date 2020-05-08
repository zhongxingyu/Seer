 package ModToDyeFor;
 
 import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet130UpdateSign;
import net.minecraft.src.Packet132TileEntityData;
 import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityMobSpawner;
 
 public class TileEntityBlockDyeableStairs extends TileEntity {
 
	int stairOrientation = 0;
 	
     /**
      * Writes a tile entity to NBT.
      */
     public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
         super.writeToNBT(par1NBTTagCompound);
         par1NBTTagCompound.setInteger("Orientation", this.stairOrientation);
     }
 
     /**
      * Reads a tile entity from NBT.
      */
     public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
         super.readFromNBT(par1NBTTagCompound);
         this.stairOrientation = par1NBTTagCompound.getInteger("Orientation");
     }
    
    public Packet getDescriptionPacket() {
        NBTTagCompound var1 = new NBTTagCompound();
        this.writeToNBT(var1);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, var1);
    }
    
    public void onDataPacket(NetworkManager net, Packet132TileEntityData pkt) {
    	this.readFromNBT(pkt.customParam1);
    }
 }
