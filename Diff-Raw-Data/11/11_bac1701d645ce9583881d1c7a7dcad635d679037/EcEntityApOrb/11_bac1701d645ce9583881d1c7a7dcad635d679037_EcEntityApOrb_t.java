 package net.minecraft.src.EnchantChanger;
 
 import net.minecraft.src.Block;
 import net.minecraft.src.DamageSource;
 import net.minecraft.src.Enchantment;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.MathHelper;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagList;
 import net.minecraft.src.World;
 import net.minecraft.src.mod_EnchantChanger;
 import net.minecraft.src.MultiToolHolders.ItemMultiToolHolder;
 import net.minecraft.src.MultiToolHolders.ToolHolderData;
 
 public class EcEntityApOrb extends Entity
 {
 	/**
 	 * A constantly increasing value that RenderXPOrb uses to control the colour shifting (Green / yellow)
 	 */
 	public int apColor;
 
 	/** The age of the XP orb in ticks. */
 	public int apOrbAge = 0;
 	public int field_35126_c;
 
 	/** The health of this XP orb. */
 	private int apOrbHealth = 5;
 
 	/** This is how much XP this orb has. */
 	private int apValue;
 
 	public EcEntityApOrb(World par1World, double par2, double par4, double par6, int par8)
 	{
 		super(par1World);
 		this.setSize(0.5F, 0.5F);
 		this.yOffset = this.height / 2.0F;
 		this.setPosition(par2, par4, par6);
 		this.rotationYaw = (float)(Math.random() * 360.0D);
 		this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
 		this.motionY = (double)((float)(Math.random() * 0.2D) * 2.0F);
 		this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
 		this.apValue = par8;
 	}
 
 	/**
 	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
 	 * prevent them from trampling crops
 	 */
 	protected boolean canTriggerWalking()
 	{
 		return false;
 	}
 
 	public EcEntityApOrb(World par1World)
 	{
 		super(par1World);
 		this.setSize(0.25F, 0.25F);
 		this.yOffset = this.height / 2.0F;
 	}
 
 	protected void entityInit() {}
 
 	public int getBrightnessForRender(float par1)
 	{
 		float var2 = 0.5F;
 
 		if (var2 < 0.0F)
 		{
 			var2 = 0.0F;
 		}
 
 		if (var2 > 1.0F)
 		{
 			var2 = 1.0F;
 		}
 
 		int var3 = super.getBrightnessForRender(par1);
 		int var4 = var3 & 255;
 		int var5 = var3 >> 16 & 255;
 		var4 += (int)(var2 * 15.0F * 16.0F);
 
 		if (var4 > 240)
 		{
 			var4 = 240;
 		}
 
 		return var4 | var5 << 16;
 	}
 
 	/**
 	 * Called to update the entity's position/logic.
 	 */
 	public void onUpdate()
 	{
 		super.onUpdate();
 
 		if (this.field_35126_c > 0)
 		{
 			--this.field_35126_c;
 		}
 
 		this.prevPosX = this.posX;
 		this.prevPosY = this.posY;
 		this.prevPosZ = this.posZ;
 		this.motionY -= 0.029999999329447746D;
 
 		if (this.worldObj.getBlockMaterial(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) == Material.lava)
 		{
 			this.motionY = 0.20000000298023224D;
 			this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
 			this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
 			this.worldObj.playSoundAtEntity(this, "random.fizz", 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
 		}
 
 		this.pushOutOfBlocks(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
 		double var1 = 8.0D;
 		EntityPlayer var3 = this.worldObj.getClosestPlayerToEntity(this, var1);
 
 		if (var3 != null)
 		{
 			double var4 = (var3.posX - this.posX) / var1;
 			double var6 = (var3.posY + (double)var3.getEyeHeight() - this.posY) / var1;
 			double var8 = (var3.posZ - this.posZ) / var1;
 			double var10 = Math.sqrt(var4 * var4 + var6 * var6 + var8 * var8);
 			double var12 = 1.0D - var10;
 
 			if (var12 > 0.0D)
 			{
 				var12 *= var12;
 				this.motionX += var4 / var10 * var12 * 0.1D;
 				this.motionY += var6 / var10 * var12 * 0.1D;
 				this.motionZ += var8 / var10 * var12 * 0.1D;
 			}
 		}
 
 		this.moveEntity(this.motionX, this.motionY, this.motionZ);
 		float var14 = 0.98F;
 
 		if (this.onGround)
 		{
 			var14 = 0.58800006F;
 			int var5 = this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));
 
 			if (var5 > 0)
 			{
 				var14 = Block.blocksList[var5].slipperiness * 0.98F;
 			}
 		}
 
 		this.motionX *= (double)var14;
 		this.motionY *= 0.9800000190734863D;
 		this.motionZ *= (double)var14;
 
 		if (this.onGround)
 		{
 			this.motionY *= -0.8999999761581421D;
 		}
 
 		++this.apColor;
 		++this.apOrbAge;
 
 		if (this.apOrbAge >= 6000)
 		{
 			this.setDead();
 		}
 	}
 
 	/**
 	 * Returns if this entity is in water and will end up adding the waters velocity to the entity
 	 */
 	public boolean handleWaterMovement()
 	{
 		return this.worldObj.handleMaterialAcceleration(this.boundingBox, Material.water, this);
 	}
 
 	/**
 	 * Will deal the specified amount of damage to the entity if the entity isn't immune to fire damage. Args:
 	 * amountDamage
 	 */
 	protected void dealFireDamage(int par1)
 	{
 		this.attackEntityFrom(DamageSource.inFire, par1);
 	}
 
 	/**
 	 * Called when the entity is attacked.
 	 */
 	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
 	{
 		this.setBeenAttacked();
 		this.apOrbHealth -= par2;
 
 		if (this.apOrbHealth <= 0)
 		{
 			this.setDead();
 		}
 
 		return false;
 	}
 
 	/**
 	 * (abstract) Protected helper method to write subclass entity data to NBT.
 	 */
 	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
 	{
 		par1NBTTagCompound.setShort("Health", (short)((byte)this.apOrbHealth));
 		par1NBTTagCompound.setShort("Age", (short)this.apOrbAge);
 		par1NBTTagCompound.setShort("Value", (short)this.apValue);
 	}
 
 	/**
 	 * (abstract) Protected helper method to read subclass entity data from NBT.
 	 */
 	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
 	{
 		this.apOrbHealth = par1NBTTagCompound.getShort("Health") & 255;
 		this.apOrbAge = par1NBTTagCompound.getShort("Age");
 		this.apValue = par1NBTTagCompound.getShort("Value");
 	}
 
 	/**
 	 * Called by a player entity when they collide with an entity
 	 */
 	public void onCollideWithPlayer(EntityPlayer player)
 	{
 		if (!this.worldObj.isRemote)
 		{
 			if (this.field_35126_c == 0 && player.xpCooldown == 0)
 			{
 				player.xpCooldown = 2;
 				this.worldObj.playSoundAtEntity(this, "random.orb", 0.1F, 0.5F * ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.8F));
 				player.onItemPickup(this, 1);
 				addAp(player);
 				this.setDead();
 			}
 		}
 	}
 	private void addAp(EntityPlayer player)
 	{
 		ItemStack[] items = new ItemStack[13];
 		for(int i=0;i<9;i++)
 		{
 			items[i] = player.inventory.getStackInSlot(i);
 		}
 		for(int i=0;i<player.inventory.armorInventory.length;i++)
 		{
 			items[i+9]=player.inventory.armorInventory[i];
 		}
 
 
 		for(int i = 0; i < items.length;i++)
 		{
 			if(items[i] != null && items[i].isItemEnchanted())
 			{
 				if(mod_EnchantChanger.loadMTH && items[i].getItem() instanceof ItemMultiToolHolder)
 				{
 					ToolHolderData tools = ((ItemMultiToolHolder)items[i].getItem()).tools;
					if(tools != null)
 					{
						for(int j = 0; j < tools.tools.length; j ++)
 						{
							if(tools.tools[j] != null && tools.tools[j].isItemEnchanted())
							{
								addApToItem(tools.tools[j]);
							}
 						}
 					}
 				}
 				else
 				{
 					addApToItem(items[i]);
 				}
 
 			}
 		}
 	}
 	public void addApToItem(ItemStack item)
 	{
 		NBTTagCompound nbt;
 		NBTTagList enchantList;
 		int prevAp;
 		short enchantmentId;
 		short enchantmentLv;
 		int nowAp;
 		nbt = item.getTagCompound();
 		enchantList = item.getEnchantmentTagList();
 		for(int j = 0; j < enchantList.tagCount();j++)
 		{
 			prevAp = ((NBTTagCompound)enchantList.tagAt(j)).getInteger("ap");
 			enchantmentId = ((NBTTagCompound)enchantList.tagAt(j)).getShort("id");
 			if(Enchantment.enchantmentsList[enchantmentId].getMaxLevel() == 1)
 				continue;
 			enchantmentLv = ((NBTTagCompound)enchantList.tagAt(j)).getShort("lvl");
 			nowAp = prevAp + this.apValue;
 			if(mod_EnchantChanger.magicEnchantment.contains(Integer.valueOf((int)enchantmentId)))
 				continue;
 			if(mod_EnchantChanger.isApLimit(enchantmentId, enchantmentLv, nowAp))
 			{
 				nowAp -= mod_EnchantChanger.getApLimit(enchantmentId, enchantmentLv);
 				if(enchantmentLv < Short.MAX_VALUE)
 					((NBTTagCompound)enchantList.tagAt(j)).setShort("lvl", (short) (enchantmentLv + 1));
 			}
 			((NBTTagCompound)enchantList.tagAt(j)).setInteger("ap", nowAp);
 		}
 	}
 	/**
 	 * Returns the XP value of this XP orb.
 	 */
 	public int getApValue()
 	{
 		return this.apValue;
 	}
 
 	/**
 	 * Returns a number from 1 to 10 based on how much XP this orb is worth. This is used by RenderXPOrb to determine
 	 * what texture to use.
 	 */
 	public int getTextureByXP()
 	{
 		return this.apValue >= 2477 ? 10 : (this.apValue >= 1237 ? 9 : (this.apValue >= 617 ? 8 : (this.apValue >= 307 ? 7 : (this.apValue >= 149 ? 6 : (this.apValue >= 73 ? 5 : (this.apValue >= 37 ? 4 : (this.apValue >= 17 ? 3 : (this.apValue >= 7 ? 2 : (this.apValue >= 3 ? 1 : 0)))))))));
 	}
 
 	/**
 	 * Get xp split rate (Is called until the xp drop code in EntityLiving.onEntityUpdate is complete)
 	 */
 	public static int getXPSplit(int par0)
 	{
 		return par0 >= 2477 ? 2477 : (par0 >= 1237 ? 1237 : (par0 >= 617 ? 617 : (par0 >= 307 ? 307 : (par0 >= 149 ? 149 : (par0 >= 73 ? 73 : (par0 >= 37 ? 37 : (par0 >= 17 ? 17 : (par0 >= 7 ? 7 : (par0 >= 3 ? 3 : 1)))))))));
 	}
 
 	/**
 	 * If returns false, the item will not inflict any damage against entities.
 	 */
 	public boolean canAttackWithItem()
 	{
 		return false;
 	}
 }
