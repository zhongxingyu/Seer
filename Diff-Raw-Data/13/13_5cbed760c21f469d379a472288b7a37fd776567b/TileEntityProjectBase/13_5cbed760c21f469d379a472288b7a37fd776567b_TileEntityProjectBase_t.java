 package slimevoid.projectbench.tileentity;
 
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import slimevoid.projectbench.core.PBCore;
 import slimevoid.projectbench.core.lib.NBTLib;
 
 public class TileEntityProjectBase extends TileEntityBase {
 	
 	protected int rotation;
 	protected boolean active;
 	
 	public TileEntityProjectBase() {
 		this.rotation = 0;
 		this.active = false;
 	}
 	
 	@Override
 	public void onBlockPlaced(ItemStack itemstack, int side, EntityLivingBase entity) {
 		this.rotation = (int) Math.floor((double)((entity.rotationYaw * 4F) / 360F) + 0.5D) & 3;
 	}
 
 	@Override
 	public int getBlockID() {
 		return PBCore.blockProjectBase.blockID;
 	}
 	
 	public int getLightValue() {
 		return this.active ? 13 : 0;
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbttagcompound) {
 		super.readFromNBT(nbttagcompound);
 		this.rotation = nbttagcompound.getInteger(NBTLib.TILE_ROTATION);
 		int status = nbttagcompound.getByte(NBTLib.TILE_ACTIVE);
 		this.active = status > 0;
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound nbttagcompound) {
 		super.writeToNBT(nbttagcompound);
 		nbttagcompound.setByte(NBTLib.TILE_ROTATION, (byte) this.rotation);
 		nbttagcompound.setByte(NBTLib.TILE_ACTIVE, (byte) (this.active ? 1 : 0));
 	}
 
 	public int getBlockTexture(int x, int y, int z, int side) {
		// TODO :: Side based on Rotation
		System.out.println("Side: " + side);
		System.out.println("Rotation: " + this.rotation);
 		return side;
 	}
 }
