 /*******************************************************************************
  * EntityArrowBase.java
  * Copyright (c) 2013 WildBamaBoy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 
 package arrowsplus.entity;
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.enchantment.EnchantmentThorns;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.monster.EntityEnderman;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.entity.projectile.EntityArrow;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.network.packet.Packet70GameEvent;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 import arrowsplus.core.ArrowsPlus;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 import cpw.mods.fml.common.ObfuscationReflectionHelper;
 import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
 
 /**
  * Handles how the arrow entity behaves.
  */
 public class EntityArrowBase extends EntityArrow implements IEntityAdditionalSpawnData
 {
 	/** The arrow type/damage/metadata value of the arrow fired. */
 	public int arrowType;
 
 	/** The bow type/damage/metadata value of the bow that fired this arrow. */
 	public int bowType;
 
 	/** How many blocks the arrow has ignored. For Hickory arrow. */
 	public int blocksIgnored;	
 
 	/** How many ticks the arrow has been in the air. */
 	public int inAirTicks;
 
 	/** The arrow's wind resistance value. Subtracted from 0.10. */
 	public double windResistance;
 
 	/** Is the bow stuck in the ground? */
 	public boolean isInGround;
 
 	public int softMapleCaughtByWindTicks;
 	public boolean softMapleIsFlyingIntoAir;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param 	world	The world the arrow is being spawned in.
 	 */
 	public EntityArrowBase(World world) 
 	{
 		super(world);
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param 	world			The world the arrow is being spawned in.
 	 * @param 	entityLiving	The entity that fired the arrow.
 	 * @param 	velocity		The starting velocity of the arrow.
 	 * @param 	arrowType		The type of arrow that this entity should represent and act like.
 	 * @param 	bowType			The type of bow that fired this arrow.
 	 */
 	public EntityArrowBase(World world, EntityLivingBase entityLiving, float velocity, int arrowType, int bowType)
 	{
 		super(world, entityLiving, velocity);
 		this.arrowType = arrowType;
 		this.bowType = bowType;
 		this.inAirTicks = 0;
 		this.windResistance = ArrowsPlus.getArrowWindResistanceByWoodType(arrowType);
 		this.blocksIgnored = 0;
 
 		//Soft maple "unreliable"
 		if (arrowType == 5)
 		{
 			if (ArrowsPlus.getBooleanWithProbability(15))
 			{
 				world.playSoundAtEntity(entityLiving, "damage.fallbig", 1.0F, 1.0F);
 				setDead();
 			}
 		}
 
 		this.softMapleIsFlyingIntoAir = false;
 		this.softMapleCaughtByWindTicks = 0;
 	}
 
 	@Override
 	public void onUpdate()
 	{
 		this.onEntityUpdate();
 
 		//Representation of EntityArrow onUpdate() code.
 		//
 		//
 		int xTile = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 0);
 		int yTile = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 1);
 		int zTile = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 2);
 		int inTile = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 3);
 		int inData = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 4);
 		boolean inGround = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 5);
 		int ticksInGround = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 9);
 		int ticksInAir = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 10);
 
 		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
 		{
 			float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
 			this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
 			this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(this.motionY, f) * 180.0D / Math.PI);
 		}
 
 		int tileId = this.worldObj.getBlockId(xTile, yTile, zTile);
 
 		if (tileId > 0)
 		{
 			Block.blocksList[tileId].setBlockBoundsBasedOnState(this.worldObj, xTile, yTile, zTile);
 			AxisAlignedBB bb = Block.blocksList[tileId].getCollisionBoundingBoxFromPool(this.worldObj, xTile, yTile, zTile);
 
 			if (bb != null && bb.isVecInside(this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX, this.posY, this.posZ)))
 			{
 				ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, true, 5);
 				inGround = true;
 			}
 		}
 
 		if (this.arrowShake > 0)
 		{
 			--this.arrowShake;
 		}
 
 		if (inGround)
 		{
 			int blockId = this.worldObj.getBlockId(xTile, yTile, zTile);
 			int blockData = this.worldObj.getBlockMetadata(xTile, yTile, zTile);
 
 			if (blockId == inTile && blockData == inData)
 			{
 				++ticksInGround;
 				ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, new Integer(ticksInGround), 9);
 
 				if (ticksInGround == 1200)
 				{
 					this.setDead();
 				}
 			}
 
 			else
 			{
 				ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, false, 5);
 				this.motionX *= this.rand.nextFloat() * 0.2F;
 				this.motionY *= this.rand.nextFloat() * 0.2F;
 				this.motionZ *= this.rand.nextFloat() * 0.2F;
 				ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, new Integer(0), 9);
 				ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, new Integer(0), 10);
 			}
 		}
 
 		else
 		{
 			++ticksInAir;
 			Vec3 positionVector = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX, this.posY, this.posZ);
 			Vec3 motionVector = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
 
 			MovingObjectPosition movingObjPosition = this.worldObj.rayTraceBlocks_do_do(positionVector, motionVector, false, true);
 			positionVector = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX, this.posY, this.posZ);
 			motionVector = this.worldObj.getWorldVec3Pool().getVecFromPool(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
 
 			if (movingObjPosition != null)
 			{
 				motionVector = this.worldObj.getWorldVec3Pool().getVecFromPool(movingObjPosition.hitVec.xCoord, movingObjPosition.hitVec.yCoord, movingObjPosition.hitVec.zCoord);
 			}
 
 			Entity entity = null;
 			List entitiesInBoundingBox = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
 			double baseDistanceToIntercept = 0.0D;
 
 
 			for (int i = 0; i < entitiesInBoundingBox.size(); ++i)
 			{
 				Entity entityInBoundingBox = (Entity)entitiesInBoundingBox.get(i);
 
 				if (entityInBoundingBox.canBeCollidedWith() && (entityInBoundingBox != this.shootingEntity || ticksInAir >= 5))
 				{
 					AxisAlignedBB expandedBoundingBox = entityInBoundingBox.boundingBox.expand(0.3F, 0.3F, 0.3F);
 					MovingObjectPosition interceptPosition = expandedBoundingBox.calculateIntercept(positionVector, motionVector);
 
 					if (interceptPosition != null)
 					{
 						double realDistanceToIntercept = positionVector.distanceTo(interceptPosition.hitVec);
 
 						if (realDistanceToIntercept < baseDistanceToIntercept || baseDistanceToIntercept == 0.0D)
 						{
 							entity = entityInBoundingBox;
 							baseDistanceToIntercept = realDistanceToIntercept;
 						}
 					}
 				}
 			}
 
 			if (entity != null)
 			{
 				movingObjPosition = new MovingObjectPosition(entity);
 			}
 
 			if (movingObjPosition != null && movingObjPosition.entityHit != null && movingObjPosition.entityHit instanceof EntityPlayer)
 			{
 				EntityPlayer entityplayer = (EntityPlayer)movingObjPosition.entityHit;
 
 				if (entityplayer.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).func_96122_a(entityplayer))
 				{
 					movingObjPosition = null;
 				}
 			}
 
 			float damageCalc;
 			float unknown;
 
 			if (movingObjPosition != null)
 			{
 				if (movingObjPosition.entityHit != null)
 				{
 					damageCalc = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
 					int damage = MathHelper.ceiling_double_int(getDamage());
 					boolean hasArmor = false;
 					EntityPlayer player = (EntityPlayer)this.shootingEntity;

 					//Account for block penetration
 					damage -= blocksIgnored;
 
 					if (this.getIsCritical())
 					{
 						damage += this.rand.nextInt(damage / 2 + 2);
 					}
 
 					DamageSource damagesource = null;
 
 					if (this.shootingEntity == null)
 					{
 						damagesource = DamageSource.causeArrowDamage(this, this);
 					}
 
 					else
 					{
 						damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity);
 					}
 
 					if (this.isBurning() && !(movingObjPosition.entityHit instanceof EntityEnderman))
 					{
 						movingObjPosition.entityHit.setFire(5);
 					}
 
 					if (this.arrowType == 7)
 					{
						if (player != null)
 						{
							for (ItemStack stack : player.inventory.armorInventory)
 							{
								if (stack != null)
								{
									hasArmor = true;
									damage = 0;
									break;
								}
 							}
 						}
 					}

 					if (movingObjPosition.entityHit.attackEntityFrom(damagesource, damage))
 					{
 						if (movingObjPosition.entityHit instanceof EntityLivingBase)
 						{
 							EntityLivingBase entitylivingbase = (EntityLivingBase)movingObjPosition.entityHit;
 
 							//Sycamore
 							if (this.arrowType == 3)
 							{
 								if (entitylivingbase instanceof EntityPlayer)
 								{
 									player.triggerAchievement(ArrowsPlus.instance.achievementWitherPlayer);
 								}
 
 								entitylivingbase.addPotionEffect(new PotionEffect(Potion.wither.id, 7 * 20, 0));
 							}
 
 							//Gum
 							if (this.arrowType == 4)
 							{
 								entitylivingbase.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 7 * 20, 0));
 							}
 
 							//Ash
 							if (this.arrowType == 6)
 							{	
 								if (worldObj.rand.nextBoolean() && worldObj.rand.nextBoolean() && worldObj.rand.nextBoolean())
 								{
 									entitylivingbase.addPotionEffect(new PotionEffect(Potion.blindness.id, 7 * 20, 0));
 
 									if (entitylivingbase instanceof EntityPlayer)
 									{
 										player.triggerAchievement(ArrowsPlus.instance.achievementBlindPlayer);
 									}
 								}
 							}
 
 							//Beech
 							if (this.arrowType == 7)
 							{
 								if (!hasArmor)
 								{
 									if (entitylivingbase instanceof EntityPlayer)
 									{
 										player.triggerAchievement(ArrowsPlus.instance.achievementPoisonPlayer);
 									}
 
 									entitylivingbase.addPotionEffect(new PotionEffect(Potion.poison.id, 7 * 20, 0));
 								}
 							}
 
 							if (!this.worldObj.isRemote)
 							{
 								entitylivingbase.setArrowCountInEntity(entitylivingbase.getArrowCountInEntity() + 1);
 							}
 
 							if (this.getKnockbackStrength() > 0)
 							{
 								unknown = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
 
 								if (unknown > 0.0F)
 								{
 									movingObjPosition.entityHit.addVelocity(this.motionX * this.getKnockbackStrength() * 0.6000000238418579D / unknown, 0.1D, this.motionZ * this.getKnockbackStrength() * 0.6000000238418579D / unknown);
 								}
 							}
 
 							if (this.shootingEntity != null)
 							{
 								EnchantmentThorns.func_92096_a(this.shootingEntity, entitylivingbase, this.rand);
 							}
 
 							if (this.shootingEntity != null && movingObjPosition.entityHit != this.shootingEntity && movingObjPosition.entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP)
 							{
 								((EntityPlayerMP)this.shootingEntity).playerNetServerHandler.sendPacketToPlayer(new Packet70GameEvent(6, 0));
 							}
 						}
 
 						this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
 
 						if (!(movingObjPosition.entityHit instanceof EntityEnderman))
 						{
 							this.setDead();
 						}
 					}
 
 					else
 					{
 						this.motionX *= -0.10000000149011612D;
 						this.motionY *= -0.10000000149011612D;
 						this.motionZ *= -0.10000000149011612D;
 						this.rotationYaw += 180.0F;
 						this.prevRotationYaw += 180.0F;
 						ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, new Integer(0), 10);
 					}
 				}
 
 				else
 				{
 					//Allow for hard maple block penetration.
 					if (arrowType == 8 && blocksIgnored < 3)
 					{
 						blocksIgnored++;
 					}
 
 					else
 					{
 						xTile = movingObjPosition.blockX;
 						yTile = movingObjPosition.blockY;
 						zTile = movingObjPosition.blockZ;
 						inTile = worldObj.getBlockId(xTile, yTile, zTile);
 						inData = worldObj.getBlockMetadata(xTile, yTile, zTile);
 
 						ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, new Integer(movingObjPosition.blockX), 0);
 						ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, new Integer(movingObjPosition.blockY), 1);
 						ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, new Integer(movingObjPosition.blockZ), 2);
 						ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, new Integer(this.worldObj.getBlockId(xTile, yTile, zTile)), 3);
 						ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, new Integer(this.worldObj.getBlockMetadata(xTile, yTile, zTile)), 4);
 
 						this.motionX = ((float)(movingObjPosition.hitVec.xCoord - this.posX));
 						this.motionY = ((float)(movingObjPosition.hitVec.yCoord - this.posY));
 						this.motionZ = ((float)(movingObjPosition.hitVec.zCoord - this.posZ));
 						damageCalc = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
 						this.posX -= this.motionX / damageCalc * 0.05000000074505806D;
 						this.posY -= this.motionY / damageCalc * 0.05000000074505806D;
 						this.posZ -= this.motionZ / damageCalc * 0.05000000074505806D;
 						this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
 
 						inGround = true;
 						ObfuscationReflectionHelper.setPrivateValue(EntityArrow.class, this, true, 5);
 
 						this.arrowShake = 7;
 						this.setIsCritical(false);
 
 						if (inTile != 0)
 						{
 							Block.blocksList[inTile].onEntityCollidedWithBlock(this.worldObj, xTile, yTile, zTile, this);
 						}
 					}
 				}
 			}
 
 			if (this.getIsCritical())
 			{
 				for (int i = 0; i < 4; ++i)
 				{
 					this.worldObj.spawnParticle("crit", this.posX + this.motionX * i / 4.0D, this.posY + this.motionY * i / 4.0D, this.posZ + this.motionZ * i / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ);
 				}
 			}
 
 			this.posX += this.motionX;
 			this.posY += this.motionY;
 			this.posZ += this.motionZ;
 			damageCalc = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
 			this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
 
 			while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
 			{
 				this.prevRotationPitch += 360.0F;
 			}
 
 			while (this.rotationYaw - this.prevRotationYaw < -180.0F)
 			{
 				this.prevRotationYaw -= 360.0F;
 			}
 
 			while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
 			{
 				this.prevRotationYaw += 360.0F;
 			}
 
 			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
 			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
 			float motionMultiplier = 0.99F;
 			float motionYDecrement = 0.05F;
 
 			if (this.isInWater())
 			{
 				for (int j1 = 0; j1 < 4; ++j1)
 				{
 					unknown = 0.25F;
 					this.worldObj.spawnParticle("bubble", this.posX - this.motionX * unknown, this.posY - this.motionY * unknown, this.posZ - this.motionZ * unknown, this.motionX, this.motionY, this.motionZ);
 				}
 
 				motionMultiplier = 0.8F;
 			}
 
 			this.motionX *= motionMultiplier;
 			this.motionY *= motionMultiplier;
 			this.motionZ *= motionMultiplier;
 			this.motionY -= motionYDecrement;
 			this.setPosition(this.posX, this.posY, this.posZ);
 			this.doBlockCollisions();
 
 			try
 			{
 				//Account for differences between isInGround and inGround.
 				if (!isInGround && inGround)
 				{
 					//The arrow has struck the ground.
 					isInGround = true;
 
 					if (!worldObj.isRemote)
 					{
 						//Determine if the arrow broke.
 						int probabilityOfBreaking = 0;
 
 						switch (arrowType)
 						{
 						case 0: probabilityOfBreaking = 25; break;
 						case 1: probabilityOfBreaking = 50; break;
 						case 2: probabilityOfBreaking = 30; break;
 						case 3: probabilityOfBreaking = 30; break;
 						case 4: probabilityOfBreaking = 25; break;
 						case 5: probabilityOfBreaking = 15; break;
 						case 6: probabilityOfBreaking = 75; break;
 						case 7: probabilityOfBreaking = 15; break;
 						case 8: probabilityOfBreaking = 15; break;
 						case 9: probabilityOfBreaking = 100; break;
 						case 10: probabilityOfBreaking = 10; break;
 						case 11: probabilityOfBreaking = 5; break;
 						}
 
 						if (ArrowsPlus.getBooleanWithProbability(probabilityOfBreaking))
 						{
 							setDead();
 						}
 
 						//Sypherus arrow random explosion.
 						if (this.arrowType == 11)
 						{
 							if (worldObj.rand.nextBoolean())
 							{
 								worldObj.createExplosion(this, posX, posY, posZ, 2.0F, false);
 							}
 						}
 
 						//Sypherus bow teleportation
 						if (this.bowType == 11)
 						{
 							if (shootingEntity instanceof EntityPlayerMP)
 							{
 								if (ArrowsPlus.getBooleanWithProbability(10))
 								{
 									this.worldObj.playSoundEffect(shootingEntity.posX, shootingEntity.posY, shootingEntity.posZ, "fire.ignite", 1.0F, 1.0F);
 								}
 
 								else
 								{
 									EntityPlayerMP entityPlayerMP = (EntityPlayerMP)shootingEntity;
 									entityPlayerMP.playerNetServerHandler.setPlayerLocation(posX, posY, posZ, entityPlayerMP.rotationYaw, entityPlayerMP.rotationPitch);
 									this.worldObj.playSoundEffect(posX, posY, posZ, "mob.endermen.portal", 1.0F, 1.0F);

 									entityPlayerMP.triggerAchievement(ArrowsPlus.instance.achievementTeleport);
 								}
 							}
 						}
 
 						//Check for hickory arrow.
 						if (this.arrowType == 9)
 						{
 							int inBlockX = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 0);
 							int inBlockY = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 1);
 							int inBlockZ = ObfuscationReflectionHelper.getPrivateValue(EntityArrow.class, this, 2);
 
 							if (inBlockX != -1 && inBlockY != -1 && inBlockZ != -1)
 							{
 								int blockId = this.worldObj.getBlockId(inBlockX, inBlockY, inBlockZ);
 
 								if (blockId != Block.bedrock.blockID)
 								{
 									this.worldObj.setBlock(inBlockX, inBlockY, inBlockZ, 0);
 								}
 							}
 						}
 
 					}
 				}
 
 				//Still flying.
 				else if (!isInGround && !inGround)
 				{				
 					if (!worldObj.isRemote)
 					{
 						//Check wind factor.
 						double windFactor = 0.10D - windResistance;
 
 						//Should wind be applied?
 						if (worldObj.rand.nextBoolean())
 						{
 							//Should wind apply to X?
 							if (worldObj.rand.nextBoolean())
 							{
 								//Should wind apply to +X?
 								if (worldObj.rand.nextBoolean())
 								{
 									setVel(motionX + windFactor, motionY, motionZ);
 								}
 
 								//Wind applies to -X.
 								else
 								{
 									setVel(motionX - windFactor, motionY, motionZ);
 								}
 							}
 
 							//Should wind apply to Z?
 							if (worldObj.rand.nextBoolean())
 							{
 								//Should wind apply to +Z?
 								if (worldObj.rand.nextBoolean())
 								{
 									setVel(motionX, motionY, motionZ + windFactor);
 								}
 
 								//Wind applies to -Z.
 								else
 								{
 									setVel(motionX, motionY, motionZ - windFactor);
 								}
 							}
 						}
 
 						//Soft maple flying into the wind.
 						if (this.arrowType == 5)
 						{
 							if (!softMapleIsFlyingIntoAir && ArrowsPlus.getBooleanWithProbability(3))
 							{
 								this.worldObj.playSoundAtEntity(shootingEntity, "mob.enderdragon.wings", 0.5F, 1.0F);
 								softMapleIsFlyingIntoAir = true;
 							}
 
 							else if (softMapleIsFlyingIntoAir)
 							{
 								if (softMapleCaughtByWindTicks != 20)
 								{
 									boolean changeX = worldObj.rand.nextBoolean();
 									boolean changeY = worldObj.rand.nextBoolean();
 									boolean changeZ = worldObj.rand.nextBoolean();
 
 									if (changeX) setVel(motionX + 0.15D, motionY, motionZ);
 									if (changeY) setVel(motionX, motionY + 0.15D, motionZ);
 									if (changeZ) setVel(motionX, motionY, motionZ + 0.15D);
 									softMapleCaughtByWindTicks++;
 								}
 							}
 						}
 					}
 				}
 			}
 
 			catch (Throwable e)
 			{
 				ArrowsPlus.instance.log(e);
 			}
 		}
 	}
 
 	@Override
 	public void onCollideWithPlayer(EntityPlayer par1EntityPlayer)
 	{
 		if (!this.worldObj.isRemote && this.isInGround && this.arrowShake <= 0)
 		{
 			boolean flag = this.canBePickedUp == 1 || this.canBePickedUp == 2 && par1EntityPlayer.capabilities.isCreativeMode;
 
 			if (this.canBePickedUp == 1 && !par1EntityPlayer.inventory.addItemStackToInventory(new ItemStack(getItemArrowByArrowType(), 1)))
 			{
 				flag = false;
 			}
 
 			if (flag)
 			{
 				this.playSound("random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
 				par1EntityPlayer.onItemPickup(this, 1);
 				this.setDead();
 			}
 		}
 	}
 
 	public double getDamage()
 	{
 		return ArrowsPlus.getArrowDamageModifierByWoodType(arrowType) + ArrowsPlus.getBowDamageModifierByWoodType(bowType);
 	}
 
 	@Override
 	public void writeSpawnData(ByteArrayDataOutput data) 
 	{
 		data.writeInt(arrowType);
 		data.writeInt(bowType);
 	}
 
 	@Override
 	public void readSpawnData(ByteArrayDataInput data) 
 	{
 		arrowType = data.readInt();
 		bowType = data.readInt();
 	}
 
 	public void setVel(double par1, double par3, double par5)
 	{
 		this.motionX = par1;
 		this.motionY = par3;
 		this.motionZ = par5;
 
 		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
 		{
 			float f = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
 			this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(par1, par5) * 180.0D / Math.PI);
 			this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(par3, f) * 180.0D / Math.PI);
 			this.prevRotationPitch = this.rotationPitch;
 			this.prevRotationYaw = this.rotationYaw;
 			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
 		}
 	}
 
 	/**
 	 * Sets the amount of knockback the arrow applies when it hits a mob.
 	 */
 	public int getKnockbackStrength()
 	{
 		if (this.arrowType == 8 || this.arrowType == 10)
 		{
 			return 1;
 		}
 
 		else
 		{
 			return 0;
 		}
 	}
 
 	public Item getItemArrowByArrowType()
 	{
 		switch (arrowType)
 		{
 		case 0: return ArrowsPlus.instance.itemArrowAspen;
 		case 1: return ArrowsPlus.instance.itemArrowCottonwood;
 		case 2: return ArrowsPlus.instance.itemArrowAlder;
 		case 3: return ArrowsPlus.instance.itemArrowSycamore;
 		case 4: return ArrowsPlus.instance.itemArrowGum;
 		case 5: return ArrowsPlus.instance.itemArrowSoftMaple;
 		case 6: return ArrowsPlus.instance.itemArrowAsh;
 		case 7: return ArrowsPlus.instance.itemArrowBeech;
 		case 8: return ArrowsPlus.instance.itemArrowHardMaple;
 		case 9: return ArrowsPlus.instance.itemArrowHickory;
 		case 10: return ArrowsPlus.instance.itemArrowMahogany;
 		case 11: return ArrowsPlus.instance.itemArrowSypherus;
 		default: return null;
 		}
 	}
 }
