 package mods.ifw.aurus.common;
 
 import java.util.ArrayList;
 
 import mods.ifw.aurus.pathfinding.bullshit.AStarNode;
 import mods.ifw.aurus.pathfinding.bullshit.AStarPathPlanner;
 import mods.ifw.aurus.pathfinding.bullshit.IAStarPathedEntity;
 import net.minecraft.block.Block;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.enchantment.EnchantmentThorns;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityFlying;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.monster.IMob;
 import net.minecraft.entity.passive.EntityVillager;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Tuple;
 import net.minecraft.world.EnumSkyBlock;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeHooks;
 
 // Eyedas
 
 public class EntityEye extends EntityFlying implements IMob, IAStarPathedEntity {
 	public final AStarPathPlanner pathPlanner;
 
 	public ArrayList<AStarNode> path = null;
 	public ArrayList<AStarNode> pathBack = null;
 
 	public int courseChangeCooldown = 0;
 	public double waypointX;
 	public double waypointY;
 	public double waypointZ;
 	private Entity targetedEntity = null;
 
 	private double angle;
 
 	public float rotY;
 
 	private int radius;
 	private int aggroCooldown = 0;
 	public int prevAttackCounter = 0;
 	public int attackCounter = 0;
 
 	private boolean searching = false;
 
 	ItemStack fetchStack = null;
 
 	private boolean pathProcessed;
 
 	private boolean markPath = true;
 
 	private AxisAlignedBB hidingSpot;
 	private AxisAlignedBB potentialHidingSpot;
 	private EntityPlayer player;
 	private ItemStack playerStack;
 
 	private boolean kleptomania = false;
 
 	public EntityEye(World par1World) {
 		super(par1World);
 		this.texture = "/mods/ifw_aurus/textures/models/Eye.png";
 
 		this.isImmuneToFire = true;
 		this.experienceValue = 1;
 
 		this.setSize(0.5f, 0.5f); // size determines vanishing point of
 									// renders
 
 		pathPlanner = new AStarPathPlanner(worldObj, this);
 	}
 
 	@Override
 	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2) {
 		dropStolenItem();
 
 		return super.attackEntityFrom(par1DamageSource, par2);
 	}
 
 	public boolean attackEntityAsMob(Entity par1Entity) {
 		int i = this.getAttackStrength(par1Entity);
 
 		if (this.isPotionActive(Potion.damageBoost)) {
 			i += 3 << this.getActivePotionEffect(Potion.damageBoost)
 					.getAmplifier();
 		}
 
 		if (this.isPotionActive(Potion.weakness)) {
 			i -= 2 << this.getActivePotionEffect(Potion.weakness)
 					.getAmplifier();
 		}
 
 		int j = 0;
 
 		if (par1Entity instanceof EntityLiving) {
 			i += EnchantmentHelper.getEnchantmentModifierLiving(this,
 					(EntityLiving) par1Entity);
 			j += EnchantmentHelper.getKnockbackModifier(this,
 					(EntityLiving) par1Entity);
 		}
 
 		boolean flag = par1Entity.attackEntityFrom(
 				DamageSource.causeMobDamage(this), i);
 
 		if (flag) {
 			if (j > 0) {
 				par1Entity.addVelocity(
 						(double) (-MathHelper.sin(this.rotationYaw
 								* (float) Math.PI / 180.0F)
 								* (float) j * 0.5F),
 						0.1D,
 						(double) (MathHelper.cos(this.rotationYaw
 								* (float) Math.PI / 180.0F)
 								* (float) j * 0.5F));
 				this.motionX *= 0.6D;
 				this.motionZ *= 0.6D;
 			}
 
 			int k = EnchantmentHelper.getFireAspectModifier(this);
 
 			if (k > 0) {
 				par1Entity.setFire(k * 4);
 			}
 
 			if (par1Entity instanceof EntityLiving) {
 				EnchantmentThorns.func_92096_a(this, (EntityLiving) par1Entity,
 						this.rand);
 			}
 		}
 
 		return flag;
 	}
 
 	@Override
 	protected void entityInit() {
 		super.entityInit();
 		this.dataWatcher.addObject(16, Byte.valueOf((byte) 0));
 	}
 
 	@Override
 	public int getMaxHealth() {
 		return 1;
 	}
 
 	public int getAttackStrength(Entity par1Entity) {
 		return 1;
 	}
 
 	@Override
 	public void onUpdate() {
 		super.onUpdate();
 		// if (this.targetedEntity != null) {
 		// double dX2 = this.targetedEntity.posX - this.posX;
 		// double dY2 = this.targetedEntity.posY - this.posY;
 		// double dZ2 = this.targetedEntity.posZ - this.posZ;
 		//
 		// // this.rotationYaw = ((EntityLiving)
 		// // this.targetedEntity).rotationYaw;
 		// }
 		byte var1 = this.dataWatcher.getWatchableObjectByte(16);
 	}
 
 	@Override
 	protected void updateEntityActionState() {
 		if ((!this.worldObj.isRemote) && (this.worldObj.difficultySetting == 0)) {
 			setDead();
 		}
 
 		despawnEntity();
 		this.prevAttackCounter = this.attackCounter;
 
 		if (path != null) {
 			// if I have successfully found a path to a hiding spot and I don't
 			// have a fetchStack, get one from the player.
 			if (!pathProcessed) {
 				pathProcessed = true;
 				processPath();
 			}
 			followPath();
 		} else {
 
 			double dX1 = this.waypointX - this.posX;
 			double dY1 = this.waypointY - this.posY;
 			double dZ1 = this.waypointZ - this.posZ;
 
 			double d1sq = dX1 * dX1 + dY1 * dY1 + dZ1 * dZ1;
 
 			// approach, but keep at a distance unless trying to steal.
 			if (d1sq > 16.0D || kleptomania) {
 				double d1 = MathHelper.sqrt_double(d1sq);
 				if (isCourseTraversable(
 						// (this.boundingBox.minX + this.boundingBox.maxX) / 2,
 						// this.boundingBox.minY + 0.5f,
 						// (this.boundingBox.minZ + this.boundingBox.maxZ) / 2,
 						posX, posY, posZ, this.waypointX, this.waypointY,
 						this.waypointZ)) {
 					this.motionX += dX1 / d1 * 0.04D;
 					this.motionY += dY1 / d1 * 0.04D;
 					this.motionZ += dZ1 / d1 * 0.04D;
 				} else {
 					this.targetedEntity = null;
 				}
 			} else if (this.targetedEntity == null) {
 				this.waypointX = (this.posX + (this.rand.nextFloat() * 2F - 1F) * 16.0F);
 				this.waypointY = (this.posY + (this.rand.nextFloat() * 2F - 1F) * 16.0F);
 				this.waypointZ = (this.posZ + (this.rand.nextFloat() * 2F - 1F) * 16.0F);
 			} else if (d1sq < 4.0D) {
 				this.waypointX = (this.posX - 4.0D * (this.targetedEntity.posX - this.posX));
 				this.waypointY = (this.targetedEntity.posY + 3.0D);
 				this.waypointZ = (this.posZ - 4.0D * (this.targetedEntity.posZ - this.posZ));
 			} else {
 				if (targetedEntity != null
 						&& ((EntityPlayer) targetedEntity)
 								.getCurrentEquippedItem() != null
 						&& worldObj.rand.nextInt(50000) < this
 								.valueOfItem(((EntityPlayer) targetedEntity)
 										.getCurrentEquippedItem().itemID)) {
 					kleptomania = true;
 				}
 			}
 
 			if (targetedEntity == null
 					|| ((EntityPlayer) targetedEntity).getCurrentEquippedItem() == null) {
 				kleptomania = false;
 			}
 
 			if (this.courseChangeCooldown-- <= 0) {
 				this.courseChangeCooldown += this.rand.nextInt(5) + 2;
 				if (this.targetedEntity != null) {
 					this.waypointX = this.targetedEntity.posX;
 					this.waypointY = (this.targetedEntity.posY + 1.5D);
 					this.waypointZ = this.targetedEntity.posZ;
 				} else {
 					this.waypointX = (this.posX + (this.rand.nextFloat() * 2F - 1F) * 16.0F);
 					this.waypointY = (this.posY + (this.rand.nextFloat() * 2F - 1F) * 16.0F);
 					this.waypointZ = (this.posZ + (this.rand.nextFloat() * 2F - 1F) * 16.0F);
 				}
 			}
 
 			if ((this.targetedEntity != null) && (this.targetedEntity.isDead)) {
 				this.targetedEntity = null;
 			}
 
 			if ((this.targetedEntity == null) || (this.aggroCooldown-- <= 0)) {
 				this.targetedEntity = this.worldObj
 						.getClosestVulnerablePlayerToEntity(this, 40.0D);
 
 				if (this.targetedEntity != null) {
 					this.aggroCooldown = 20;
 				}
 			}
 
 			double var9 = 64.0D;
 		}
 
 		if (this.targetedEntity != null) {
 			if (kleptomania
 					&& this.boundingBox.expand(0.5, 0.5, 0.5).intersectsWith(
 							targetedEntity.boundingBox)) {
 				player = (EntityPlayer) targetedEntity;
 				findPathToHidingSpotFrom(player);
 			}
 		}
 
 		if (this.targetedEntity != null && path == null) {
 			double dX2 = this.targetedEntity.posX - this.posX;
 			double dY2 = this.targetedEntity.posY
 					+ this.targetedEntity.getEyeHeight() - this.posY;
 			double dZ2 = this.targetedEntity.posZ - this.posZ;
 
 			this.renderYawOffset = this.rotationYaw = -(float) (Math.atan2(dX2,
 					dZ2) + 4 * 3.14) * 180 / 3.14f;
 
 			this.rotationPitch = this.rotationPitch
 					* 0.6F
 					+ 0.4F
 					* ((float) (Math.atan2(dZ2 * dZ2 + dX2 * dX2,
 							Math.copySign(dY2 * dY2, dY2)) - 0.0F) * 180 / 3.14f);
 
 			// this.rotationPitch = (float) (Math.atan2(dZ2 * dZ2 + dX2 * dX2,
 			// Math.copySign(dY2 * dY2, dY2)) - 0.0F) * 180 / 3.14f;
 
 			// if (canEntityBeSeen(this.targetedEntity)) {
 			// this.attackCounter += 1;
 			//
 			// if (this.attackCounter == 20) {
 			// this.attackCounter = -40;
 			// }
 			// } else if (this.attackCounter > 0) {
 			// this.attackCounter -= 1;
 			// }
 		} else {
 			this.renderYawOffset = (this.rotationYaw = -(float) Math.atan2(
 					this.motionX, this.motionZ) * 180.0F / 3.1415927F);
 			this.rotationPitch = this.rotationPitch
 					* 0.9F
 					+ 0.1F
 					* ((float) (Math.atan2(this.motionZ * this.motionZ
 							+ this.motionX * this.motionX, Math.copySign(
 							this.motionY * this.motionY, this.motionY)) - 0.0F) * 180 / 3.14f);
 
 			// if (this.attackCounter > 0) {
 			// this.attackCounter -= 1;
 			// }
 		}
 
 		// if (!this.worldObj.isRemote) {
 		// byte var21 = this.dataWatcher.getWatchableObjectByte(16);
 		// byte var12 = (byte) (this.attackCounter > 10 ? 1 : 0);
 		//
 		// if (var21 != var12) {
 		// this.dataWatcher.updateObject(16, Byte.valueOf(var12));
 		// }
 		// }
 	}
 
 	private boolean isCourseTraversable(double x, double y, double z) {
 		MovingObjectPosition mop = this.worldObj.rayTraceBlocks(
 				this.worldObj.getWorldVec3Pool().getVecFromPool(
 						this.boundingBox.minX, this.boundingBox.minY,
 						this.boundingBox.minZ), this.worldObj
 						.getWorldVec3Pool().getVecFromPool(x, y, z));
 
 		return mop == null;
 	}
 
 	protected boolean isCourseTraversable(double xStart, double yStart,
 			double zStart, double xEnd, double yEnd, double zEnd) {
 		double dX = xEnd - xStart;
 		double dY = yEnd - yStart;
 		double dZ = zEnd - zStart;
 
 		double distance = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
 
 		dX /= distance;
 		dY /= distance;
 		dZ /= distance;
 
 		AxisAlignedBB AABB = this.boundingBox.copy();
 		AABB.offset(xStart - this.boundingBox.minX, yStart
 				- this.boundingBox.minY, zStart - this.boundingBox.minZ);
 
 		for (int i = 1; i < distance; i++) {
 			AABB.offset(dX, dY, dZ);
 
 			if (!this.worldObj.getCollidingBoundingBoxes(this, AABB).isEmpty()) {
 				// for (Object object : this.worldObj.getCollidingBoundingBoxes(
 				// this, AABB)) {
 				// AxisAlignedBB box = (AxisAlignedBB) object;
 				// int id = worldObj.getBlockId((int) Math.floor(box.minX),
 				// (int) Math.floor(box.minY),
 				// (int) Math.floor(box.minZ));
 				// if (id != 0 && Block.blocksList[id] != null
 				// && Block.blocksList[id].blockMaterial.isSolid()) {
 				// return false;
 				// }
 				// }
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	protected String getLivingSound() {
 		return "fire.ignite";
 	}
 
 	@Override
 	protected String getHurtSound() {
 		return "step.stone";
 	}
 
 	@Override
 	protected String getDeathSound() {
 		return "random.break";
 	}
 
 	@Override
 	protected int getDropItemId() {
 		return 0;
 	}
 
 	@Override
 	protected void dropFewItems(boolean par1, int par2) {
 	}
 
 	@Override
 	protected float getSoundVolume() {
 		return 1.0F;
 	}
 
 	@Override
 	protected float getSoundPitch() {
 		return 2 + 1.0f * this.rand.nextFloat();
 	}
 
 	// stolen from EntityMob
 	protected boolean isValidLightLevel() {
 		int i = MathHelper.floor_double(this.posX);
 		int j = MathHelper.floor_double(this.boundingBox.minY);
 		int k = MathHelper.floor_double(this.posZ);
 
 		if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) > this.rand
 				.nextInt(32)) {
 			return false;
 		} else {
 			int l = this.worldObj.getBlockLightValue(i, j, k);
 
 			if (this.worldObj.isThundering()) {
 				int i1 = this.worldObj.skylightSubtracted;
 				this.worldObj.skylightSubtracted = 10;
 				l = this.worldObj.getBlockLightValue(i, j, k);
 				this.worldObj.skylightSubtracted = i1;
 			}
 
 			return l <= this.rand.nextInt(8);
 		}
 	}
 
	// Eyedas spawn near torches or in darkness.
 	@Override
 	public boolean getCanSpawnHere() {
 		return super.getCanSpawnHere()
				&& (this.isValidLightLevel() || isTorchNearby());
 	}
 
 	public boolean isTorchNearby() {
 		AxisAlignedBB aabb = this.boundingBox.copy().expand(2, 2, 2);
 		for (int i = (int) (Math.floor(aabb.minX)); i <= (int) (Math
 				.floor(aabb.maxX)); i++) {
 			for (int j = (int) (Math.floor(aabb.minY)); j <= (int) (Math
 					.floor(aabb.maxY)); j++) {
 				for (int k = (int) (Math.floor(aabb.minZ)); k <= (int) (Math
 						.floor(aabb.maxZ)); k++) {
 					if (worldObj.getBlockId(i, j, k) == Block.torchWood.blockID) {
 						System.out
 								.printf("Spawned Eydas near a torch at (%d, %d, %d).\n",
 										i, j, k);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public int getMaxSpawnedInChunk() {
 		return 2;
 	}
 
 	public boolean interact(EntityPlayer player) {
 		if (!worldObj.isRemote) {
 			System.out.println("Oh god my eye.");
 
 			// int scalar = 4;
 			//
 			// int x = (int) posX + (worldObj.rand.nextInt(32) - 16) * scalar;
 			// int y = (int) posY;// + (worldObj.rand.nextInt(32) - 16) *
 			// scalar;
 			// int z = (int) posZ + (worldObj.rand.nextInt(32) - 16) * scalar;
 			//
 			// while (!worldObj.isAirBlock(x, y, z)) {
 			// x = (int) posX + (worldObj.rand.nextInt(32) - 16) * scalar;
 			// y = (int) posY + (worldObj.rand.nextInt(32) - 16) * scalar;
 			// z = (int) posZ + (worldObj.rand.nextInt(32) - 16) * scalar;
 			// }
 
 			// findPathToHidingSpotFrom(player);
 
 			// findPathTo(x, y, z);
 		} else {
 			double var13 = -10.0D;
 			double var15 = 2.0D;
 			double var17 = 1.0D;
 
 			worldObj.spawnParticle("reddust", this.boundingBox.minX
 					+ this.width / 2, this.boundingBox.minY + this.height / 2,
 					this.boundingBox.minZ + this.width / 2, var13, var15, var17);
 		}
 
 		return super.interact(player);
 	}
 
 	private void findPathToHidingSpotFrom(EntityPlayer p) {
 		potentialHidingSpot = hidingSpotAround(p.posX, p.posY + 2, p.posZ, 16,
 				32);
 
 		if (potentialHidingSpot != null) {
 			player = p;
 			playerStack = player.inventory.getCurrentItem();
 
 			if (playerStack != null) {
 				// findPathBetween((int) Math.floor(this.boundingBox.minX +
 				// this.width / 2),
 				// (int) Math.floor(this.boundingBox.minY + (this.height -
 				// Math.floor(this.height)/2) ),
 				// (int) Math.floor(this.boundingBox.minZ + this.width / 2),
 				//
 				// (int) potentialHidingSpot.minX,
 				// (int) potentialHidingSpot.minY,
 				// (int) potentialHidingSpot.minZ);
 
 				double currX = (this.boundingBox.minX + this.boundingBox.maxX) / 2;
 				double currY = this.boundingBox.minY + 0.5;
 				double currZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2;
 
 				findPathBetween((int) Math.floor(currX),
 						(int) Math.floor(currY), (int) Math.floor(currZ),
 						(int) Math.floor(potentialHidingSpot.minX),
 						(int) Math.floor(potentialHidingSpot.minY),
 						(int) Math.floor(potentialHidingSpot.minZ));
 
 			}
 
 		}
 	}
 
 	@Override
 	public int getBrightnessForRender(float par1) {
 		return 240;
 	}
 
 	public boolean isRayOpen(double x1, double y1, double z1, double x2,
 			double y2, double z2) {
 		MovingObjectPosition mop = this.worldObj.rayTraceBlocks(this.worldObj
 				.getWorldVec3Pool().getVecFromPool(x1, y1, z1), this.worldObj
 				.getWorldVec3Pool().getVecFromPool(x2, y2, z2));
 
 		return mop == null;
 	}
 
 	private AxisAlignedBB hidingSpotAround(double xStart, double yStart,
 			double zStart, int minRange, int maxRange) {
 		AxisAlignedBB aabb = this.boundingBox.copy();
 		aabb = aabb.expand(1, 1, 1);
 
 		// ensures hiding spots are not preferentially in any one direction
 		int attempts = 26 + worldObj.rand.nextInt(13);
 
 		boolean found = false;
 
 		double x, y, z, xOff, yOff, zOff;
 
 		float angle, radius;
 
 		while (attempts > 0 && !found) {
 			attempts--;
 
 			// 2.39996 is the golden angle in radians. using it as the
 			// multiplier in this method ensures that randomish directions are
 			// chosen without missing any angles.
 			angle = 2.39996f * attempts;
 			radius = minRange
 					+ worldObj.rand.nextInt(2 * (maxRange - minRange))
 					- (maxRange - minRange);
 
 			xOff = radius * Math.cos(angle);
 			yOff = (worldObj.rand.nextInt(2 * 6) - 6);
 			zOff = radius * Math.sin(angle);
 
 			// xOff = worldObj.rand.nextInt(2 * (maxRange - minRange))
 			// - (maxRange - minRange);
 			// xOff += xOff > 0 ? minRange : -minRange;
 			// zOff = worldObj.rand.nextInt(2 * (maxRange - minRange))
 			// - (maxRange - minRange);
 			// zOff += zOff > 0 ? minRange : -minRange;
 
 			x = xStart + xOff;
 			y = yStart + yOff;
 			z = zStart + zOff;
 			// x = xStart + worldObj.rand.nextInt(2 * maxRange) - maxRange;//
 			// xOff;
 			// y = yStart + yOff;
 			// z = zStart + worldObj.rand.nextInt(2 * maxRange) - maxRange;
 
 			aabb.offset(x - aabb.minX, y - aabb.minY, z - aabb.minZ);
 
 			if (worldObj.getCollidingBlockBounds(aabb).isEmpty()) {
 				if (!isRayOpen(xStart, yStart, zStart, aabb.minX,
 						(aabb.minY + aabb.maxY) / 2, aabb.minZ)
 						&& !isRayOpen(xStart, yStart, zStart, aabb.maxX,
 								(aabb.minY + aabb.maxY) / 2, aabb.minZ)
 						&& !isRayOpen(xStart, yStart, zStart, aabb.maxX,
 								(aabb.minY + aabb.maxY) / 2, aabb.maxZ)
 						&& !isRayOpen(xStart, yStart, zStart, aabb.minX,
 								(aabb.minY + aabb.maxY) / 2, aabb.maxZ)) {
 					found = true;
 					return aabb;
 				}
 			}
 		}
 		return null;
 	}
 
 	private int lastTraversablePathIndex() {
 		int indexMinusOne = path.size() - 1;
 
 		AStarNode traversableStepMinusOne;
 
 		double offX = (Math.ceil(width) - width) / 2;
 		double offY = (Math.ceil(height) - height) / 2;
 		double offZ = (Math.ceil(width) - width) / 2;
 
 		do {
 			traversableStepMinusOne = path.get(indexMinusOne);
 			indexMinusOne--;
 		} while (this.isCourseTraversable(this.boundingBox.minX,
 				this.boundingBox.minY, this.boundingBox.minZ,
 				traversableStepMinusOne.x + offX, traversableStepMinusOne.y
 						+ offY, traversableStepMinusOne.z + offZ)
 				&& indexMinusOne >= 0);
 
 		return indexMinusOne + 1;
 	}
 
 	private void breadCrumbs() {
 		// markPath = !markPath;
 		if (markPath && worldObj.rand.nextInt(50) < path.size()) {
 			AStarNode step = path.get(path.size() - 1);
 			if (worldObj.isAirBlock(step.x, step.y, step.z))
 				worldObj.setBlock(step.x, step.y, step.z,
 						Aurus.pathMarker.blockID);
 			else if (worldObj.isAirBlock(step.x, step.y + 1, step.z))
 				worldObj.setBlock(step.x, step.y + 1, step.z,
 						Aurus.pathMarker.blockID);
 		}
 	}
 
 	private void processPath() {
 		// for (AStarNode as : path) {
 		// // worldObj.setBlock(as.x, as.y, as.z, Aurus.pathMarker.blockID);
 		// // worldObj.setBlock(as.x, as.y, as.z, Block.tallGrass.blockID);
 		// }
 
 		if (path.size() > 1) {
 			int toBeRemoved = (path.size() - 1) - lastTraversablePathIndex()
 					- 1;
 			for (int i = 0; i < toBeRemoved - 1; i++) {
 				breadCrumbs();
 				path.remove(path.size() - 1);
 			}
 		}
 	}
 
 	public void followPath() {
 		if (!path.isEmpty()) {
 			AStarNode step = path.get(path.size() - 1);
 
 			double offX = (Math.ceil(width) - width) / 2;
 			double offY = (Math.ceil(height) - height) / 2;
 			double offZ = (Math.ceil(width) - width) / 2;
 
 			this.waypointX = step.x + offX;
 			this.waypointY = step.y + offY;
 			this.waypointZ = step.z + offZ;
 
 			double dX1 = this.waypointX - this.boundingBox.minX;
 			double dY1 = this.waypointY - this.boundingBox.minY;
 			double dZ1 = this.waypointZ - this.boundingBox.minZ;
 
 			double d1sq = dX1 * dX1 + dY1 * dY1 + dZ1 * dZ1;
 
 			if (d1sq > 0.50D) {
 				double d1 = MathHelper.sqrt_double(d1sq);
 				this.motionX += dX1 / d1 * (d1 + 4) / 100; // 0.04D;
 				this.motionY += dY1 / d1 * (d1 + 4) / 100; // 0.04D;
 				this.motionZ += dZ1 / d1 * (d1 + 4) / 100; // 0.04D;
 			} else {
 
 				// pathMarking.
 				breadCrumbs();
 
 				// brake at key points to avoid drifting off path too far.
 				this.motionX *= 0.2D;
 				this.motionY *= 0.2D;
 				this.motionZ *= 0.2D;
 
 				pathBack.add(path.remove(path.size() - 1));
 				if (path.size() <= 0) {
 					AStarNode as = pathBack.get(pathBack.size() - 1);
 
 					// at the end.
 					onEndOfPathReached();
 
 					path = null;
 					return;
 				}
 
 				if (path.size() > 1) {
 					int toBeRemoved = (path.size() - 1)
 							- lastTraversablePathIndex() - 1;
 					for (int i = 0; i < toBeRemoved; i++) {
 						breadCrumbs();
 						path.remove(path.size() - 1);
 					}
 				}
 			}
 		}
 	}
 
 	public void findPathTo(int x, int y, int z) {
 		findPathBetween((int) posX, (int) posY, (int) this.posZ, x, y, z);
 	}
 
 	public void findPathBetween(int x1, int y1, int z1, int x2, int y2, int z2) {
 		if (!searching) {
 			pathBack = new ArrayList<AStarNode>();
 
 			pathPlanner.getPath(x1, y1, z1, x2, y2, z2, null);
 
 			searching = true;
 			System.out.println("Finding path.");
 		}
 	}
 
 	@Override
 	public void onFoundPath(ArrayList<AStarNode> result) {
 		searching = false;
 
 		path = result;
 		hidingSpot = potentialHidingSpot;
 
 		potentialHidingSpot = null;
 		pathProcessed = false;
 
 		tryToSteal();
 
 		System.out.println("Found path.");
 	}
 
 	@Override
 	public void onNoPathAvailable() {
 		searching = false;
 		path = null;
 
 		fetchStack = null;
 		hidingSpot = null;
 
 		System.out.println("Couldn't find path.");
 
 	}
 
 	public void tryToSteal() {
 		if (hidingSpot != null) {
 			if (playerStack != null) {
 				attackEntityAsMob(player);
 				fetchStack = player.inventory.decrStackSize(
 						player.inventory.currentItem, 1);
 				kleptomania = false;
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * Uses the EntityVillager's merchant info to determine the value of items
 	 * relative to 1000. Note that many items are not traded in this list,
 	 * including ironically the golden items that the Eyedas drop.
 	 * 
 	 * @param itemID
 	 * @return Returns a value that is 1000 / (The number of items traded per
 	 *         emerald). For example, rotten flesh is traded 64 per emerald, so
 	 *         its value is 1000/64. Written books are traded at a value of 1
 	 *         per emerald, so their value is 1000. An iron helmet is worth 6
 	 *         emeralds per weapon, so their value would be 6000. If an item is
 	 *         not listed on the merchant list, it gets a random value from 0 to
 	 *         1000.
 	 */
 
 	public int valueOfItem(int itemID) {
 		Tuple tuple = (Tuple) EntityVillager.villagerStockList.get(itemID);
 		if (tuple != null) {
 			int maxSellable = ((Integer) tuple.getSecond()).intValue();
 			return 1000 / maxSellable;
 		}
 		tuple = (Tuple) EntityVillager.blacksmithSellingList.get(itemID);
 		if (tuple != null) {
 			int maxSellable = ((Integer) tuple.getSecond()).intValue();
 			if (maxSellable > 0) {
 				return 500 * maxSellable;
 			} else {
 				maxSellable = -((Integer) tuple.getFirst()).intValue();
 				return 1000 / maxSellable;
 			}
 		}
 
 		return worldObj.rand.nextInt(200);
 	}
 
 	public void onEndOfPathReached() {
 		dropStolenItem();
 
 		hidingSpot = null;
 
 		/**
 		 * while fetch is fun, it can also be used as a combat mechanic. playful
 		 * gold dragons steal your weapons and armor, then run off and hide
 		 * them. This needs to be fine-tuned a bit. The entity should implement
 		 * IInventory, write the stolen item to its NBT, wait until a path is
 		 * found to actually steal the item. Also it should drop the item upon
 		 * death.
 		 */
 	}
 
 	private void dropStolenItem() {
 		if (fetchStack != null) {
 
 			EntityItem entityItem = new EntityItem(this.worldObj, this.posX,
 					this.posY, this.posZ, fetchStack);
 			entityItem.motionX = 0;
 			entityItem.motionY = 1;
 			entityItem.motionZ = 0;
 
 			this.worldObj.spawnEntityInWorld(entityItem);
 
 			if (worldObj.rand.nextDouble() < 0.1) {
 				ItemStack is = new ItemStack(Item.ingotGold,
 						worldObj.rand.nextInt(4) + 1);
 
 				if (worldObj.rand.nextDouble() < 0.1) {
 					if (worldObj.rand.nextInt(5) != 0) {
 						is = new ItemStack(getArmorItemForSlot(
 								worldObj.rand.nextInt(4) + 1, 1));
 					} else {
 						is = new ItemStack(Item.swordGold, 1);
 					}
 
 					EnchantmentHelper.addRandomEnchantment(
 							this.rand,
 							is,
 							5 + this.worldObj.difficultySetting
 									* this.rand.nextInt(6));
 				}
 				this.entityDropItem(is, 0.0f);
 			}
 
 			fetchStack = null;
 		}
 	}
 }
