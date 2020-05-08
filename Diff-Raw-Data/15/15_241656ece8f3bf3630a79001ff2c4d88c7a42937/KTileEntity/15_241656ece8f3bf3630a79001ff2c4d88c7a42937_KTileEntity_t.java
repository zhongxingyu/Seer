 package com.allaryin.kinkspring.tile;
 
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Vec3;
 import net.minecraftforge.common.ForgeDirection;
 
 public class KTileEntity extends TileEntity {
 	
 	protected boolean	_redstone			= false;
 	
 	protected boolean	_supports_facing	= false;
 	protected ForgeDirection 	_facing		= ForgeDirection.UNKNOWN;
 	
 	public boolean isRedstonePowered() {
 		return _redstone;
 	}
 
 	public void pollRedstoneState() {
 		final boolean prevRedstone = _redstone;
 		_redstone = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord,
 				zCoord);
 		if( _redstone != prevRedstone ) {
 			System.out.println("Redstone -> "+_redstone);
 		}
 	}
 	
 	public ForgeDirection getFacing() {
 		return _facing;
 	}
 	public void setFacing(ForgeDirection facing) {
 		if( _facing != facing ) {
 			_facing = facing;
 			System.out.println("Facing -> "+facing);
			this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, _facing.ordinal(), 3);
 		}
 	}
 	
 	public boolean supportsFacing() {
 		return _supports_facing;
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound nbt) {
 		super.writeToNBT(nbt);
 		if( _supports_facing ) {
 			nbt.setByte("facing", (byte)_facing.ordinal());
 		}
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound nbt) {
 		super.readFromNBT(nbt);
 		if( _supports_facing ) {
 			setFacing( ForgeDirection.getOrientation(nbt.getByte("facing")) );
 		}
 	}
 
 	public void onBlockPlacedBy(EntityLivingBase living, ItemStack itemStack) {
 		rotateToLiving(living);
 	}
 	
 	public void rotateToLiving(EntityLivingBase living) {
 		if ( _supports_facing ) {
 			ForgeDirection facing;
 			
 			Vec3 look = living.getLookVec();
 			if( look.yCoord < -0.55 && living.posY > this.yCoord ) {
 				facing = ForgeDirection.UP;
 			} else if ( look.yCoord > 0.6 && (living.posY + 1) > this.yCoord ) {
 				facing = ForgeDirection.DOWN;
 			} else if ( Math.abs(look.xCoord) > Math.abs(look.zCoord) ) {
 				if ( look.xCoord > 0 )
 					facing = ForgeDirection.WEST;
 				else
 					facing = ForgeDirection.EAST;
 			} else {
 				if ( look.zCoord > 0 )
 					facing = ForgeDirection.NORTH;
 				else
 					facing = ForgeDirection.SOUTH;			
 			}
 			
 			setFacing(facing);
 		}
 	}
 }
