 package com.github.rossrkk.utilities.tileentities;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 
 import com.github.rossrkk.utilities.power.IPower;
 
 public class TECable extends TileEntity implements IPower {
 
 	public int power;
 	public int maxPower = 32;
 
 	public int toTransfer = 16;
 
 	@Override
 	public void updateEntity() {
 		/*transfer(xCoord - 1, yCoord, zCoord);
 		transfer(xCoord + 1, yCoord, zCoord);
 		transfer(xCoord, yCoord - 1, zCoord);
 		transfer(xCoord, yCoord + 1, zCoord);
 		transfer(xCoord, yCoord, zCoord - 1);
 		transfer(xCoord, yCoord, zCoord + 1);*/
 		int randomSide = worldObj.rand.nextInt(6);
 		//System.out.println(randomSide);
 		switch (randomSide) {
 		case 0: transfer(xCoord, yCoord, zCoord + 1);
 		break;
 		case 1: transfer(xCoord - 1, yCoord, zCoord);
 		break;
 		case 2: transfer(xCoord + 1, yCoord, zCoord);
 		break;
 		case 3: transfer(xCoord, yCoord - 1, zCoord);
 		break;
 		case 4: transfer(xCoord, yCoord + 1, zCoord);
 		break;
 		case 5: transfer(xCoord, yCoord, zCoord - 1);
 		break;
 		}
 	}
 
 	public void transfer(int x, int y, int z) {
 		if (worldObj.getBlockTileEntity(x, y, z) instanceof IPower 
 				&& !((IPower)worldObj.getBlockTileEntity(x, y, z)).isGenerator() 
 				&& power >= toTransfer) {
 			power = power + ((IPower)worldObj.getBlockTileEntity(x, y, z)).incrementPower(toTransfer) - toTransfer;
 		}
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
 		power = compound.getInteger("power");
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
 		compound.setInteger("power", power);
 	}
 
 	@Override
 	public int getPower() {
 		return power;
 	}
 
 	@Override
 	public int incrementPower(int count) {
 		int totalPower = count + power;
 		if (totalPower > maxPower) {
 			power = maxPower;
 			return totalPower - maxPower;
 		} else {
 			power = totalPower;
 			return 0;
 		}
 	}
 
 	@Override
 	public boolean isGenerator() {
 		return false;
 	}
 
 }
