 package techguy.mods.undead.common;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityArrow;
 import net.minecraft.entity.projectile.EntityPotion;
 import net.minecraft.item.Item;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.potion.Potion;
 import net.minecraft.stats.AchievementList;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 public class ModUndeadEntityVent extends EntityMob
 {
 	private int field_40152_d;
 
 	public ModUndeadEntityVent(World world)
 	{
 		super(world);
 		texture = "/undeadPlus/mob/vent.png";
 		experienceValue = 7;
 	}
 
 	public int getMaxHealth()
 	{
 		return 20;
 	}
 
 	public int getDropItemId()
 	{
 		return Item.gunpowder.shiftedIndex;
 	}
 
 	public boolean attackEntityFrom(DamageSource damagesource, int i)
 	{
 		return super.attackEntityFrom(damagesource, i);
 	}
 
 	public void onDeath(DamageSource damagesource)
 	{
 		EntityPotion entitypotion = new EntityPotion(worldObj);
 		entitypotion.posY = posY + (double)(height) + 0.5D;
 		worldObj.spawnEntityInWorld(new EntityPotion(worldObj, this, 16388));
 
 		//Minecraft.getMinecraft().thePlayer.triggerAchievement(ModUndeadMainRegistry.ventKill);
 
 		if (damagesource.getEntity() instanceof EntityPlayer)
 		{
 			EntityPlayer var2 = (EntityPlayer)damagesource.getEntity();
 			var2.triggerAchievement(ModUndeadMainRegistry.ventKill);
 		}
 		super.onDeath(damagesource);
 	}
 
 	private int field_70846_g;
 
 
 
 	protected void entityInit()
 	{
 		super.entityInit();
 		this.dataWatcher.addObject(16, new Byte((byte)0));
 	}
 
 
 	/**
 	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
 	 * use this to react to sunlight and start to burn.
 	 */
 	public void onLivingUpdate()
 	{
 
 		for (float i = 0.1F; i < 0.3F; i++)
 		{
 			worldObj.spawnParticle("largesmoke", posX + (rand.nextDouble() - 0.5D) * (double)width, posY + rand.nextDouble() * (double)height, posZ + (rand.nextDouble() - 0.5D) * (double)width, 0.0D, 0.0D, 0.0D);
 		}
 
 		super.onLivingUpdate();
 	}
 
 	/**
 	 * Basic mob attack. Default to touch of death in EntityCreature. Overridden by each mob to define their attack.
 	 */
 	protected void attackEntity(Entity par1Entity, float par2)
 	{
 		if (this.attackTime <= 0 && par2 < 2.0F && par1Entity.boundingBox.maxY > this.boundingBox.minY && par1Entity.boundingBox.minY < this.boundingBox.maxY)
 		{
 			this.attackTime = 20;
 			this.attackEntityAsMob(par1Entity);
 		}
 		else if (par2 < 30.0F)
 		{
 			double var3 = par1Entity.posX - this.posX;
 			double var5 = par1Entity.boundingBox.minY + (double)(par1Entity.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
 			double var7 = par1Entity.posZ - this.posZ;
 
 			if (this.attackTime == 0)
 			{
 				++this.field_70846_g;
 
 				if (this.field_70846_g == 1)
 				{
 					this.attackTime = 60;
 					this.func_70844_e(true);
 				}
 				else if (this.field_70846_g <= 4)
 				{
 					this.attackTime = 6;
 				}
 				else
 				{
 					this.attackTime = 100;
 					this.field_70846_g = 0;
 					this.func_70844_e(false);
 				}
 
 				if (this.field_70846_g > 1)
 				{
 					float var9 = MathHelper.sqrt_float(par2) * 0.5F;
 					this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1009, (int)this.posX, (int)this.posY, (int)this.posZ, 0);
 
 					Minecraft mc = Minecraft.getMinecraft();
 					for (int var10 = 0; var10 < 1; ++var10)
 					{
 						ModUndeadEntityPoisonousBall var11 = new ModUndeadEntityPoisonousBall(this.worldObj, this, var3 + this.rand.nextGaussian() * (double)var9, var5, var7 + this.rand.nextGaussian() * (double)var9);
 						var11.posY = this.posY + (double)(this.height / 2.0F) + 0.5D;
 						this.worldObj.spawnEntityInWorld(var11);
 					}
 				}
 			}
 
 			this.rotationYaw = (float)(Math.atan2(var7, var3) * 180.0D / Math.PI) - 90.0F;
 			this.hasAttacked = true;
 		}
 	}
 
 	public boolean func_70845_n()
 	{
 		return (this.dataWatcher.getWatchableObjectByte(16) & 1) != 0;
 	}
 
 	public void func_70844_e(boolean par1)
 	{
 		byte var2 = this.dataWatcher.getWatchableObjectByte(16);
 
 		if (par1)
 		{
 			var2 = (byte)(var2 | 1);
 		}
 		else
 		{
 			var2 &= -2;
 		}
 
 		this.dataWatcher.updateObject(16, Byte.valueOf(var2));
 	}
 
 	/**
 	 * Checks to make sure the light is not too bright where the mob is spawning
 	 */
 	protected boolean isValidLightLevel()
 	{
 		return true;
 	}
 
 	/**
 	 * Returns the amount of damage a mob should deal.
 	 */
 	public int getAttackStrength(Entity par1Entity)
 	{
 		return 6;
 	}
 
 	protected String getLivingSound()
 	{
 		return "undeadPlusAudio.mobs.vent.growl";
 	}
 
 	protected String getHurtSound()
 	{
 		return "undeadPlusAudio.mobs.vent.hit";
 	}
 
 	protected String getDeathSound()
 	{
 		return "undeadPlusAudio.mobs.vent.die";
 	}
 }
