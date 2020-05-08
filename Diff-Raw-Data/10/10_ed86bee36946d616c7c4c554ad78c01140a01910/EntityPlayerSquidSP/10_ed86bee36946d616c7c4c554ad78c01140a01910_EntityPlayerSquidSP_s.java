 package ayamitsu.urtsquid.player;
 
 import java.util.List;
 
 import ayamitsu.urtsquid.network.PacketHandler;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.EntityClientPlayerMP;
 import net.minecraft.client.multiplayer.NetClientHandler;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EnumStatus;
 import net.minecraft.potion.Potion;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.Session;
 import net.minecraft.world.World;
 
 public class EntityPlayerSquidSP extends EntityClientPlayerMP {
 
 	public PlayerStatus playerStatus = new PlayerStatus();
 
     public float field_70861_d = 0.0F;
     public float field_70862_e = 0.0F;
     public float field_70859_f = 0.0F;
     public float field_70860_g = 0.0F;
     public float field_70867_h = 0.0F;
     public float field_70868_i = 0.0F;
 
     /** angle of the tentacles in radians */
     public float tentacleAngle = 0.0F;
 
     /** the last calculated angle of the tentacles in radians */
     public float lastTentacleAngle = 0.0F;
     protected float randomMotionSpeed = 0.0F;
     protected float field_70864_bA = 0.0F;
     protected float field_70871_bB = 0.0F;
     protected float randomMotionVecX = 0.0F;
     protected float randomMotionVecY = 0.0F;
     protected float randomMotionVecZ = 0.0F;
 
 	public EntityPlayerSquidSP(Minecraft par1Minecraft, World par2World, Session par3Session, NetClientHandler par4NetClientHandler) {
 		super(par1Minecraft, par2World, par3Session, par4NetClientHandler);
		System.out.println("Spawn Override Player SP");
		this.texture = "/mob/squid.png";
 		this.setSize(0.75F, 0.95F);
 		this.yOffset = 0.425F;
 		this.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
 		this.field_70864_bA = 1.0F / (this.rand.nextFloat() + 1.0F) * 0.2F;
 	}
 
 	@Override
 	public String getTexture() {
 		return this.texture;
 	}
 
 	@Override
 	public double getYOffset() {
 		return this.yOffset + 0.5D;
 	}
 
 	@Override
 	public float getEyeHeight() {
 		return 0.12F;//this.yOffset;//this.height * 0.5F;//this.yOffset;//this.height * 0.85F;// this.yOffset// 0.12F
 	}
 
 	@Override
 	protected void resetHeight() {
 		super.resetHeight();
 		this.setSize(0.75F, 0.95F);
 		this.yOffset = 0.425F;
 	}
 
 	@Override
 	public void preparePlayerToSpawn() {
 		this.yOffset = 0.425F;
 		this.setSize(0.75F, 0.95F);
 
 		if (this.worldObj != null) {
 			while (this.posY > 0.0D) {
 				this.setPosition(this.posX, this.posY, this.posZ);
 
 				if (this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty()) {
 					break;
 				}
 
 				++this.posY;
 			}
 
 			this.motionX = this.motionY = this.motionZ = 0.0D;
 			this.rotationPitch = 0.0F;
 		}
 
 		this.setEntityHealth(this.getMaxHealth());
 		this.deathTime = 0;
 	}
 
 	@Override
 	protected boolean pushOutOfBlocks(double par1, double par3, double par5)
 	{
 		int var7 = MathHelper.floor_double(par1);
 		int var8 = MathHelper.floor_double(par3);
 		int var9 = MathHelper.floor_double(par5);
 		double var10 = par1 - (double)var7;
 		double var12 = par3 - (double)var8;
 		double var14 = par5 - (double)var9;
 		List var16 = this.worldObj.getAllCollidingBoundingBoxes(this.boundingBox);
 
 		if (var16.isEmpty() && !this.worldObj.func_85174_u(var7, var8, var9)) {
 			return false;
 		} else {
 			boolean var17 = !this.worldObj.func_85174_u(var7 - 1, var8, var9);
 			boolean var18 = !this.worldObj.func_85174_u(var7 + 1, var8, var9);
 			boolean var19 = !this.worldObj.func_85174_u(var7, var8 - 1, var9);
 			boolean var20 = !this.worldObj.func_85174_u(var7, var8 + 1, var9);
 			boolean var21 = !this.worldObj.func_85174_u(var7, var8, var9 - 1);
 			boolean var22 = !this.worldObj.func_85174_u(var7, var8, var9 + 1);
 			byte var23 = 3;
 			double var24 = 9999.0D;
 
 			if (var17 && var10 < var24) {
 				var24 = var10;
 				var23 = 0;
 			}
 
 			if (var18 && 1.0D - var10 < var24) {
 				var24 = 1.0D - var10;
 				var23 = 1;
 			}
 
 			if (var20 && 1.0D - var12 < var24) {
 				var24 = 1.0D - var12;
 				var23 = 3;
 			}
 
 			if (var21 && var14 < var24) {
 				var24 = var14;
 				var23 = 4;
 			}
 
 			if (var22 && 1.0D - var14 < var24) {
 				var24 = 1.0D - var14;
 				var23 = 5;
 			}
 
 			float var26 = this.rand.nextFloat() * 0.2F + 0.1F;
 
 			if (var23 == 0) {
 				this.motionX = (double)(-var26);
 			}
 
 			if (var23 == 1) {
 				this.motionX = (double)var26;
 			}
 
 			if (var23 == 2) {
 				this.motionY = (double)(-var26);
 			}
 
 			if (var23 == 3) {
 				this.motionY = (double)var26;
 			}
 
 			if (var23 == 4) {
 				this.motionZ = (double)(-var26);
 			}
 
 			if (var23 == 5) {
 				this.motionZ = (double)var26;
 			}
 
 			return true;
 		}
 	}
 
 	@Override
 	public boolean canBreatheUnderwater() {
 		return true;
 	}
 
 	@Override
 	public float getCurrentPlayerStrVsBlock(Block block) {
 		return super.getCurrentPlayerStrVsBlock(block) * (this.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this) ? 5.0F : 1.0F);
 	}
 
 	public float getCurrentPlayerStrVsBlock(Block block, int meta) {
 		return super.getCurrentPlayerStrVsBlock(block, meta) * (this.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this) ? 5.0F : 1.0F);
 	}
 
 	@Override
 	public void onUpdate() {
 		super.onUpdate();
 		this.onUpdateSquid();
 	}
 
 	@Override
 	public void onEntityUpdate() {
 		int air = this.getAir();
 		super.onEntityUpdate();
 
 		if (this.isEntityAlive() && !this.isInsideOfMaterial(Material.water) && !this.isPotionActive(Potion.waterBreathing) && !this.capabilities.disableDamage) {
 			--air;
 			this.setAir(air);
 
 			if (this.getAir() == -20) {
 				this.setAir(0);
 				this.attackEntityFrom(DamageSource.drown, 2);
 			}
 		} else {
 			this.setAir(300);
 		}
 	}
 
 	public void onUpdateSquid() {
 		this.field_70862_e = this.field_70861_d;
 		this.field_70860_g = this.field_70859_f;
 		this.field_70868_i = this.field_70867_h;
 		this.lastTentacleAngle = this.tentacleAngle;
 		this.field_70867_h += this.field_70864_bA;
 
 		if (this.field_70867_h > ((float)Math.PI * 2F))
 		{
 			this.field_70867_h -= ((float)Math.PI * 2F);
 
 			if (this.rand.nextInt(10) == 0)
 			{
 				this.field_70864_bA = 1.0F / (this.rand.nextFloat() + 1.0F) * 0.2F;
 			}
 		}
 
 		if (this.isInWater())
 		{
 			float var1;
 
 			if (this.field_70867_h < (float)Math.PI)
 			{
 				var1 = this.field_70867_h / (float)Math.PI;
 				this.tentacleAngle = MathHelper.sin(var1 * var1 * (float)Math.PI) * (float)Math.PI * 0.25F;
 
 				if ((double)var1 > 0.75D)
 				{
 					this.randomMotionSpeed = 1.0F;
 					this.field_70871_bB = 1.0F;
 				}
 				else
 				{
 					this.field_70871_bB *= 0.8F;
 				}
 			}
 			else
 			{
 				this.tentacleAngle = 0.0F;
 				this.randomMotionSpeed *= 0.9F;
 				this.field_70871_bB *= 0.99F;
 			}
 
 			/*if (!this.worldObj.isRemote)
 			{
 				this.motionX = (double)(this.randomMotionVecX * this.randomMotionSpeed);
 				//this.motionY = (double)(this.randomMotionVecY * this.randomMotionSpeed);
 				this.motionZ = (double)(this.randomMotionVecZ * this.randomMotionSpeed);
 			}*/
 
 			var1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
 			//this.renderYawOffset += (-((float)Math.atan2(this.motionX, this.motionZ)) * 180.0F / (float)Math.PI - this.renderYawOffset) * 0.1F;
 			//this.rotationYaw = this.renderYawOffset;
 			this.field_70859_f += (float)Math.PI * this.field_70871_bB * 1.5F;
 			this.field_70861_d += (-((float)Math.atan2((double)var1, (this.posY - this.lastTickPosY)/*this.motionY*/)) * 180.0F / (float)Math.PI - this.field_70861_d) * 0.1F;
 		}
 		else
 		{
 			this.tentacleAngle = MathHelper.abs(MathHelper.sin(this.field_70867_h)) * (float)Math.PI * 0.25F;
 
 			/*if (!this.worldObj.isRemote)
 			{
 				this.motionX = 0.0D;
 				//this.motionY -= 0.08D;
 				//this.motionY *= 0.9800000190734863D;
 				this.motionZ = 0.0D;
 			}*/
 
 			if (this.ridingEntity == null) {
 				this.field_70861_d = (float)((double)this.field_70861_d + (double)(-90.0F - this.field_70861_d) * 0.02D);
 			} else {
 				float var1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
 				this.field_70861_d += (-((float)Math.atan2((double)var1, (this.posY - this.lastTickPosY)/*this.motionY*/)) * 180.0F / (float)Math.PI - this.field_70861_d) * 0.1F;
 			}
 		}
 	}
 
 }
