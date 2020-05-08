 package gearteam.geartech.gear.tileentities;
 
 import net.minecraft.nbt.NBTTagCompound;
 
 import net.minecraft.tileentity.TileEntity;
 
 public class TileEntityShaft extends TileEntity {
 
 	private int rotation;
 	private int rotationSpeed;
 
 	public TileEntityShaft() {
 
 		rotation = 0;
 		rotationSpeed = 0;
 
 	}
 
	public int getRotation() {
 
 		return rotation;
 
 	}
 
 	public int getRotationSpeed() {
 
 		return rotationSpeed;
 
 	}
 
 	public void setRotationSpeed (final int speed) {
 
 		rotationSpeed = speed;
 
 	}
 
 	@Override public void readFromNBT (final NBTTagCompound tagCompound) {
 
 		super.readFromNBT(tagCompound);
 
 		rotation = tagCompound.getInteger("Direction");
 		rotationSpeed = tagCompound.getInteger("RotationSpeed");
 
 	}
 
 	@Override public void writeToNBT (final NBTTagCompound tagCompound) {
 
 		super.writeToNBT(tagCompound);
 
 		tagCompound.setInteger("Direction", rotation);
 		tagCompound.setInteger("RotationSpeed", rotationSpeed);
 
 	}
 
 }
