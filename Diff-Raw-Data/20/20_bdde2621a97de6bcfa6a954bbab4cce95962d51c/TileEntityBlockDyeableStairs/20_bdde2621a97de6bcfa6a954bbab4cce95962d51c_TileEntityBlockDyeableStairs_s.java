 package ModToDyeFor;
 
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.TileEntity;
 
 public class TileEntityBlockDyeableStairs extends TileEntity {
 
	int stairOrientation = 2;
 	
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
 }
