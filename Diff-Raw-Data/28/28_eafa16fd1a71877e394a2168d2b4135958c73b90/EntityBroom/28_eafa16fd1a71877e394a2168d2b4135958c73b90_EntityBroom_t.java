 package assets.quidcraft.entities;
 
 import java.util.List;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.init.Items;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import assets.quidcraft.Quidcraft;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class EntityBroom extends Entity {
 	private int broomPosRotationIncrements;
 	private double broomX, broomY, broomZ;
 	private double broomYaw, broomPitch;
 	public boolean isGoingUp;
 	public boolean isGoingDown;
 	@SideOnly(Side.CLIENT)
 	private double velocityX;
 	@SideOnly(Side.CLIENT)
 	private double velocityY;
 	@SideOnly(Side.CLIENT)
 	private double velocityZ;
 
 	public EntityBroom(World world) {
 		super(world);
 		this.preventEntitySpawning = true;
 		this.setSize(1.3F, 0.6F);
 		this.yOffset = this.height / 2.0F;
 	}
 
 	public EntityBroom(World world, double d, double d1, double d2) {
 		this(world);
 		this.setPosition(d, d1 + this.yOffset, d2);
 		this.motionX = 0.0D;
 		this.motionY = 0.0D;
 		this.motionZ = 0.0D;
 		this.prevPosX = d;
 		this.prevPosY = d1;
 		this.prevPosZ = d2;
 	}
 
 	@Override
 	public double getMountedYOffset() {
 		return 0;
 	}
 
 	@Override
 	protected void updateFallState(double par1, boolean par3) {
 	}
 
 	@Override
 	public boolean attackEntityFrom(DamageSource damagesource, float i) {
 		if (this.isEntityInvulnerable()) {
 			return false;
 		} else if (!this.worldObj.isRemote && !this.isDead) {
 			this.setForwardDirection(-this.getForwardDirection());
 			this.setTimeSinceHit(10);
 			this.setDamageTaken(this.getDamageTaken() + i * 10);
 			this.setBeenAttacked();
 			if (damagesource.getEntity() instanceof EntityPlayer && ((EntityPlayer) damagesource.getEntity()).capabilities.isCreativeMode) {
 				this.setDamageTaken(100);
 			}
 			if (this.getDamageTaken() > 40) {
 				if (this.riddenByEntity != null) {
 					this.riddenByEntity.mountEntity(this);
 				}
 				this.func_145778_a(Quidcraft.proxy.Broom, 1, 0.0F);
 				this.setDead();
 			}
 			return true;
 		} else
 			return true;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void setPositionAndRotation2(double d, double d1, double d2, float f, float f1, int i) {
        this.broomPosRotationIncrements = i + 5;
 		this.broomX = d;
 		this.broomY = d1;
 		this.broomZ = d2;
 		this.broomYaw = f;
 		this.broomPitch = f1;
 		this.motionX = this.velocityX;
 		this.motionY = this.velocityY;
 		this.motionZ = this.velocityZ;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void setVelocity(double d, double d1, double d2) {
 		this.velocityX = this.motionX = d;
 		this.velocityY = this.motionY = d1;
 		this.velocityZ = this.motionZ = d2;
 	}
 
 	@Override
 	public boolean interactFirst(EntityPlayer par1EntityPlayer) {
 		if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityPlayer && this.riddenByEntity != par1EntityPlayer) {
 			return true;
 		} else {
 			if (!this.worldObj.isRemote) {
 				par1EntityPlayer.mountEntity(this);
 			}
 			return true;
 		}
 	}
 
 	public void setDamageTaken(float par1) {
 		this.dataWatcher.updateObject(19, Float.valueOf(par1));
 	}
 
 	public float getDamageTaken() {
 		return this.dataWatcher.getWatchableObjectFloat(19);
 	}
 
 	public void setTimeSinceHit(int par1) {
 		this.dataWatcher.updateObject(17, Integer.valueOf(par1));
 	}
 
 	public int getTimeSinceHit() {
 		return this.dataWatcher.getWatchableObjectInt(17);
 	}
 
 	public void setForwardDirection(int par1) {
 		this.dataWatcher.updateObject(18, Integer.valueOf(par1));
 	}
 
 	public int getForwardDirection() {
 		return this.dataWatcher.getWatchableObjectInt(18);
 	}
 
 	@Override
 	public void onUpdate() {
 		super.onUpdate();
 		if (this.getTimeSinceHit() > 0) {
 			this.setTimeSinceHit(this.getTimeSinceHit() - 1);
 		}
 		if (this.getDamageTaken() > 0) {
 			this.setDamageTaken(this.getDamageTaken() - 1);
 		}
 		this.prevPosX = this.posX;
 		this.prevPosY = this.posY;
 		this.prevPosZ = this.posZ;
 		//int i = 5;
 		double d1 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		if (this.worldObj.isRemote) {
 			if (this.broomPosRotationIncrements > 0) {
 				double d4 = this.posX + (this.broomX - this.posX) / this.broomPosRotationIncrements;
 				double d10 = this.posY + (this.broomY - this.posY) / this.broomPosRotationIncrements;
 				double d13 = this.posZ + (this.broomZ - this.posZ) / this.broomPosRotationIncrements;
 				double d17 = MathHelper.wrapAngleTo180_double(this.broomYaw - this.rotationYaw);
 				this.rotationYaw = (float) (this.rotationYaw + d17 / this.broomPosRotationIncrements);
 				this.rotationPitch = (float) (this.rotationPitch + (this.broomPitch - this.rotationPitch) / this.broomPosRotationIncrements);
 				--this.broomPosRotationIncrements;
 				this.setPosition(d4, d10, d13);
 				this.setRotation(this.rotationYaw, this.rotationPitch);
 			} else {
 				double d5 = this.posX + this.motionX;
 				double d11 = this.posY + this.motionY;
 				double d14 = this.posZ + this.motionZ;
 				this.setPosition(d5, d11, d14);
 				this.motionX *= 0.99D;
 				this.motionY *= 0.95D;
 				this.motionZ *= 0.99D;
 			}
 		} else {
 			if (this.riddenByEntity != null && this.riddenByEntity instanceof EntityLivingBase) {
 				double d4 = ((EntityLivingBase) this.riddenByEntity).moveForward;//>0 for forward key, <0 for backward
 				double d5 = ((EntityLivingBase) this.riddenByEntity).moveStrafing;//>0 for left key, <0 for right key
 				double d0 = -Math.sin(this.riddenByEntity.rotationYaw * (float) Math.PI / 180.0F);
 				double d11 = Math.cos(this.riddenByEntity.rotationYaw * (float) Math.PI / 180.0F);
 				this.motionX += (d5 * d11 + d0 * d4) * 0.08;
 				this.motionZ += (d4 * d11 - d0 * d5) * 0.08;
 			}
 			if (this.riddenByEntity != null) {
 				double acc = 0.05D;
 				//move up if hitting wall
 				if (this.isCollidedHorizontally)
 					this.motionY += acc;
 				//move up/down
 				if (Math.abs(this.motionY) <= 1.0D) {
 					if (isGoingUp)
 						this.motionY += acc;
 					else if (!onGround && isGoingDown)
 						this.motionY -= acc;
 				} else {
 					if (this.motionY < 0)
 						this.motionY += acc;
 					else if (this.motionY > 0)
 						this.motionY -= acc;
 				}
 				//decrease y speed
 				if (!isGoingUp && !isGoingDown && !isCollidedHorizontally) {
 					if (motionY > 0) {
 						motionY -= acc;
 						if (motionY < 0)
 							motionY = 0;
 					} else {
 						motionY += acc;
 						if (motionY > 0)
 							motionY = 0;
 					}
 				}
 			} else {
 				if (!this.onGround) {
 					this.motionY -= 0.1;
 					if (this.motionY < -0.5D)
 						this.motionY = -0.5D;
 				}
 				//slow down
 				this.motionX *= 0.5D;
 				this.motionZ *= 0.5D;
 				this.isGoingDown = false;
 			}
 			this.moveEntity(this.motionX, this.motionY, this.motionZ);
 			if (this.isCollidedHorizontally && d1 > 1.0D) {
 				if (!this.worldObj.isRemote) {
 					this.setDead();
 					this.func_145778_a(Items.wheat, 1, 0.0F);
 					this.func_145778_a(Items.stick, 2, 0.0F);
 				}
 			}
 			this.rotationPitch = 0.0F;
 			double rot = this.rotationYaw;
 			double difX = this.prevPosX - this.posX;
 			double difZ = this.prevPosZ - this.posZ;
 			if (difX * difX + difZ * difZ > 0.001D) {
 				rot = ((float) (Math.atan2(difZ, difX) * 180.0D / Math.PI));
 			}
 			double var14 = MathHelper.wrapAngleTo180_double(rot - this.rotationYaw);
 			if (var14 > 20.0D) {
 				var14 = 20.0D;
 			}
 			if (var14 < -20.0D) {
 				var14 = -20.0D;
 			}
 			this.rotationYaw = (float) (this.rotationYaw + var14);
 			this.setRotation(this.rotationYaw, this.rotationPitch);
 			if (!worldObj.isRemote) {
 				List<?> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.2D, 0.0D, 0.2D));
 				if (list != null && !list.isEmpty()) {
 					for (int j1 = 0; j1 < list.size(); ++j1) {
 						Entity entity = (Entity) list.get(j1);
 						if (entity != this.riddenByEntity && entity.canBePushed() && entity instanceof EntityBroom) {
 							entity.applyEntityCollision(this);
 						}
 					}
 				}
 				if (this.riddenByEntity != null && this.riddenByEntity.isDead) {
 					this.riddenByEntity = null;
 				}
 			}
 		}
 	}
 
 	@Override
 	protected boolean canTriggerWalking() {
 		return false;
 	}
 
 	@Override
 	protected void entityInit() {
 		this.dataWatcher.addObject(17, new Integer(0));
 		this.dataWatcher.addObject(18, new Integer(1));
 		this.dataWatcher.addObject(19, new Float(0.0F));
 	}
 
 	@Override
 	public boolean canBeCollidedWith() {
 		return !this.isDead;
 	}
 
 	@Override
 	public AxisAlignedBB getCollisionBox(Entity par1Entity) {
 		return par1Entity.boundingBox;
 	}
 
 	@Override
 	public AxisAlignedBB getBoundingBox() {
 		return this.boundingBox;
 	}
 
 	@Override
 	public boolean canBePushed() {
 		return true;
 	}
 
 	@Override
 	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
 	}
 
 	@Override
 	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
 	}
 }
