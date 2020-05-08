 //  =====GPL=============================================================
 //  This program is free software; you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation; version 2 dated June, 1991.
 // 
 //  This program is distributed in the hope that it will be useful, 
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 // 
 //  You should have received a copy of the GNU General Public License
 //  along with this program;  if not, write to the Free Software
 //  Foundation, Inc., 675 Mass Ave., Cambridge, MA 02139, USA.
 //  =====================================================================
 //
 //
 // Copyright 2011-2014 Michael Sheppard (crackedEgg)
 //
 package com.parachute.common;
 
 import java.util.List;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.init.Blocks;
 import net.minecraft.init.Items;
 import net.minecraft.item.Item;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 public class EntityParachute extends Entity {
 
 	private boolean isTurning;
 	private int newRotationInc;
 	private double newPosX;
 	private double newPosY;
 	private double newPosZ;
 	private double newRotationYaw;
 	private double newRotationPitch;
 	@SideOnly(Side.CLIENT)
 	private double velocityX;
 	@SideOnly(Side.CLIENT)
 	private double velocityY;
 	@SideOnly(Side.CLIENT)
 	private double velocityZ;
 	private double motionFactor;
 	private double maxAltitude;
 	private boolean allowThermals;
 	private boolean smallCanopy;
 
 	private final int hitTime = 10;
 	private final float maxDamage = 40.0F;
 
 	final static double drift = 0.004;
 	final static double ascend = drift * -10.0;
 
 	final static int modeDrift = 0; // key up
 	final static int modeAscend = 1; // key down
 
 	final static double forwardSpeed = 0.75;
 	final double dropDistance = 3.0;
 
 	private final double d2r = 0.0174532925199433;
 	private final double r2d = 57.2957795130823;
 
 
 	public EntityParachute(World world)
 	{
 		super(world);
 
 		smallCanopy = Parachute.instance.isSmallCanopy();
 
 		preventEntitySpawning = true;
 		setSize(2.0F, 1.0F);
 		yOffset = height / 2F;
 		motionFactor = 0.07D;
 		isTurning = true;
 
 		allowThermals = Parachute.instance.getAllowThermals();
 		maxAltitude = Parachute.instance.getMaxAltitude();
 	}
 
 	public EntityParachute(World world, double x, double y, double z)
 	{
 		this(world);
 
 		setPosition(x, y + (double) yOffset, z);
 
 		motionX = 0.0D;
 		motionY = 0.0D;
 		motionZ = 0.0D;
 
 		prevPosX = x;
 		prevPosY = y;
 		prevPosZ = z;
 	}
 
 	@Override
 	protected boolean canTriggerWalking()
 	{
 		return false;
 	}
 
 	@Override
 	protected void entityInit()
 	{
 		dataWatcher.addObject(17, new Integer(0)); // time since last hit
 		dataWatcher.addObject(18, new Integer(1)); // forward direction
 		dataWatcher.addObject(19, new Float(0.0F)); // damage taken | current damage
 	}
 
 	@Override
 	public AxisAlignedBB getCollisionBox(Entity entity)
 	{
 		if (entity != riddenByEntity && entity.ridingEntity != this) {
 			return entity.boundingBox;
 		}
 		return null;
 	}
 
 	@Override
 	public AxisAlignedBB getBoundingBox()
 	{
 		return boundingBox;
 	}
 
 	@Override
 	public boolean canBePushed()
 	{
 		return true;
 	}
 
 	@Override
 	public double getMountedYOffset()
 	{
 		return smallCanopy ? -2.5 : -3.5;
 	}
 
 	public void destroyParachute()
 	{
 		this.setDead();
 	}
 
 	// parachute takes additional damage from being hit
 	@Override
 	public boolean attackEntityFrom(DamageSource damagesource, float damage)
 	{
 		if (!worldObj.isRemote && !isDead) {
 			setForwardDirection(-getForwardDirection());
 			setTimeSinceHit(hitTime);
 			setDamageTaken(getDamageTaken() + damage * 10.0F);
 			setBeenAttacked();
 
 			if (getDamageTaken() > maxDamage) {
 				if (riddenByEntity != null) {
 					riddenByEntity.mountEntity(this);
 				}
 				// drop a parachute item
 //				dropRemains();
 				destroyParachute(); // aaaaiiiiieeeeee!!! ... thud!
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean canBeCollidedWith()
 	{
 		return !isDead;
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int inc)
 	{
 		if (isTurning) {
 			newRotationInc = inc + 5;
 		} else {
 			double newX = x - posX;
 			double newY = y - posY;
 			double newZ = z - posZ;
 			double magnitude = newX * newX + newY * newY + newZ * newZ;
 
 			if (magnitude <= 1.0D) {
 				return;
 			}
 
 			newRotationInc = 3;
 		}
 
 		// position
 		newPosX = x;
 		newPosY = y;
 		newPosZ = z;
 
 		// rotation
 		newRotationYaw = yaw;
 		newRotationPitch = pitch;
 
 		// forward/vertical motion
 		motionX = velocityX;
 		motionY = velocityY;
 		motionZ = velocityZ;
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void setVelocity(double x, double y, double z)
 	{
 		velocityX = motionX = x;
 		velocityY = motionY = y;
 		velocityZ = motionZ = z;
 	}
 
 	@Override
 	public void onUpdate()
 	{
 		super.onUpdate();
 
 		// the player has probably been killed or pressed LSHIFT
 		if (riddenByEntity == null) {
 			if (!worldObj.isRemote) {
 //				dropRemains();
 				destroyParachute();
 			}
 			return;
 		}
 
 		if (getTimeSinceHit() > 0) {
 			setTimeSinceHit(getTimeSinceHit() - 1);
 		}
 		if (getDamageTaken() > 0.0F) {
 			setDamageTaken(getDamageTaken() - 1.0F);
 		}
 
 		prevPosX = posX;
 		prevPosY = posY;
 		prevPosZ = posZ;
 
 		// forward velocity
 		double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
 
 		// drop the chute when close to ground
		checkShouldDropChute(posX, posY, posZ, smallCanopy ? dropDistance : dropDistance + 1.0);
 
 		// forward velocity
 		if (worldObj.isRemote && isTurning) {
 			if (newRotationInc > 0) {
 				double x = posX + (newPosX - posX) / (double) newRotationInc;
 				double y = posY + (newPosY - posY) / (double) newRotationInc;
 				double z = posZ + (newPosZ - posZ) / (double) newRotationInc;
 
 				double adjYaw = MathHelper.wrapAngleTo180_double(newRotationYaw - (double) rotationYaw);
 
 				rotationYaw += adjYaw / (double) newRotationInc;
 				rotationPitch += (newRotationPitch - (double) rotationPitch) / (double) newRotationInc;
 				--newRotationInc;
 
 				setPosition(x, y, z);
 				setRotation(rotationYaw, rotationPitch);
 			} else {
 				motionY -= currentDescentRate();
 
 				double x = posX + motionX;
 				double y = posY + motionY;
 				double z = posZ + motionZ;
 				setPosition(x, y, z);
 
 				motionX *= 0.99D;
 				motionY *= 0.95D;
 				motionZ *= 0.99D;
 			}
 		} else {
             // moveForward happens when the 'W' key is pressed. Value is either 0.0 | ~0.98
 			// when allowThermals is false forwardMovement is set to the constant 'forwardSpeed'
 			// and appied to motionX and motionZ
 			double forwardMovement = allowThermals ? (double) ((EntityLivingBase) riddenByEntity).moveForward : forwardSpeed;
 			if (riddenByEntity != null && riddenByEntity instanceof EntityLivingBase) {
 				if (forwardMovement > 0.0) {
 					double x = -Math.sin((double) (riddenByEntity.rotationYaw * d2r));
 					double z = Math.cos((double) (riddenByEntity.rotationYaw * d2r));
 					motionX += (x * motionFactor * 0.05) * forwardMovement;
 					motionZ += (z * motionFactor * 0.05) * forwardMovement;
 				}
 				// while on the parachute reduce damage to player when colliding
 				riddenByEntity.fallDistance = 0.0F;
 				riddenByEntity.isCollided = false;
 			}
 
 			// forward velocity
 			double localvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
 
 			if (localvelocity > 0.35D) {
 				double motionAdj = 0.35D / localvelocity;
 				motionX *= motionAdj;
 				motionZ *= motionAdj;
 				localvelocity = 0.35D;
 			}
 
 			if (localvelocity > velocity && motionFactor < 0.35D) {
 				motionFactor += (0.35D - motionFactor) / 35.0D;
 				if (motionFactor > 0.35D) {
 					motionFactor = 0.35D;
 				}
 			} else {
 				motionFactor -= (motionFactor - 0.07D) / 35.0D;
 				if (motionFactor < 0.07D) {
 					motionFactor = 0.07D;
 				}
 			}
 
 			motionY -= currentDescentRate();
 
 			moveEntity(motionX, motionY, motionZ);
 
 			motionX *= 0.99D;
 			motionY *= 0.95D;
 			motionZ *= 0.99D;
 
 			rotationPitch = 0.0F;
 			double yaw = rotationYaw;
 			double delta_X = prevPosX - posX;
 			double delta_Z = prevPosZ - posZ;
 
 			if (delta_X * delta_X + delta_Z * delta_Z > 0.001D) {
 				yaw = (float) ((Math.atan2(delta_Z, delta_X) * r2d));
 			}
 
 			double adjustedYaw = MathHelper.wrapAngleTo180_double(yaw - (double) rotationYaw);
 
 			if (adjustedYaw > 20.0D) {
 				adjustedYaw = 20.0D;
 			}
 			if (adjustedYaw < -20.0D) {
 				adjustedYaw = -20.0D;
 			}
 
 			rotationYaw += adjustedYaw;
 			setRotation(rotationYaw, rotationPitch);
 
 			if (!worldObj.isRemote) {
 				List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.2D, 0.0D, 0.2D));
 				if (list != null && list.isEmpty()) {
 					for (int k = 0; k < list.size(); k++) {
 						Entity entity = (Entity) list.get(k);
 						if (entity != riddenByEntity && entity.canBePushed() && (entity instanceof EntityParachute)) {
 							entity.applyEntityCollision(this);
 						}
 					}
 				}
 
 				// something bad happened, somehow the skydiver was killed.
 				if (riddenByEntity != null && riddenByEntity.isDead) {
 					riddenByEntity = null;
 					if (!worldObj.isRemote) {
 						destroyParachute();
 					}
 				}
 			}
 		}
 	}
 
 	public double currentDescentRate()
 	{
 		double descentRate = drift; // defaults to drift
 		
 		if (!allowThermals) {
 			return descentRate;
 		}
 
 		EntityPlayer player = (EntityPlayer) riddenByEntity;
 		if (player != null) {
 			PlayerInfo pInfo = ParachutePlayerManager.instance().getPlayerInfoFromPlayer(player);
             if (pInfo != null) {
 				if (pInfo.keyPressed) {
 					descentRate = ascend;
 				}
 			}
 		}
 
 		if (maxAltitude > 0.0D) { // altitude limiting
 			if (posY >= maxAltitude) {
 				descentRate = drift;
 			}
 		}
 
 		return descentRate;
 	}
 
 	protected boolean checkShouldDropChute(double x, double y, double z, double distance)
 	{
 		boolean shouldDrop = false;
 
 		if (isNearGround(x, y, z, distance)) {
 			if (riddenByEntity != null) {
 				riddenByEntity.fallDistance = 0.0F;
 				riddenByEntity.mountEntity(this);
 				if (!worldObj.isRemote) {
 					destroyParachute();
 				} else {
 					riddenByEntity = null;
 				}
 			}
 			shouldDrop = true;
 		}
 		return shouldDrop;
 	}
 
 	public boolean isNearGround(double posx, double posy, double posz, double distance)
 	{
 		boolean nearGround = false;
 
 		int x = MathHelper.floor_double(posx);
 		int y = MathHelper.floor_double(posy - distance);
 		int z = MathHelper.floor_double(posz);
 
 		if (!worldObj.isAirBlock(x, y, z)) {
 			nearGround = true;
 		}
 		return nearGround;
 	}
 
 	@Override
 	public void updateRiderPosition()
 	{
 		if (riddenByEntity != null) {
 			double cosYaw = Math.cos((double) rotationYaw * d2r) * 0.4D;
 			double sinYaw = Math.sin((double) rotationYaw * d2r) * 0.4D;
 			riddenByEntity.setPosition(posX + cosYaw, posY + getMountedYOffset() + riddenByEntity.getYOffset(), posZ + sinYaw);
 		}
 	}
 
 	@Override
 	public void updateRidden()
 	{
 		if (this.ridingEntity.isDead) {
 			this.ridingEntity = null;
 		} else {
 			this.motionX = 0.0D;
 			this.motionY = 0.0D;
 			this.motionZ = 0.0D;
 			this.onUpdate();
 
 			if (this.ridingEntity != null) {
 				this.ridingEntity.updateRiderPosition();
 			}
 		}
 	}
 
 	// when parachute is destroyed drop the 'remains'
 	protected void dropRemains()
 	{
 		dropItem(Item.getItemFromBlock(Blocks.wool), 1);
 		dropItem(Items.string, 1);
 	}
 
 	@Override
 	protected void writeEntityToNBT(NBTTagCompound nbt)
 	{
 	}
 
 	@Override
 	protected void readEntityFromNBT(NBTTagCompound nbt)
 	{
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public float getShadowSize()
 	{
 		return 0.0F;
 	}
 
 	public void setDamageTaken(float f)
 	{
 		dataWatcher.updateObject(19, Float.valueOf(f));
 	}
 
 	public float getDamageTaken()
 	{
 		return dataWatcher.getWatchableObjectFloat(19);
 	}
 
 	public void setTimeSinceHit(int time)
 	{
 		dataWatcher.updateObject(17, Integer.valueOf(time));
 	}
 
 	public int getTimeSinceHit()
 	{
 		return dataWatcher.getWatchableObjectInt(17);
 	}
 
 	public void setForwardDirection(int forward)
 	{
 		dataWatcher.updateObject(18, Integer.valueOf(forward));
 	}
 
 	public int getForwardDirection()
 	{
 		return dataWatcher.getWatchableObjectInt(18);
 	}
 
 	@SideOnly(Side.CLIENT)
 	public void func_70270_d(boolean turning)
 	{
 		isTurning = turning;
 	}
 
 }
