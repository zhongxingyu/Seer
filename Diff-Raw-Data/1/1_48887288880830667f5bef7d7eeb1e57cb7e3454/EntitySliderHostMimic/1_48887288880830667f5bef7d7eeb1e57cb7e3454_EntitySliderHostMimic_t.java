 package net.aetherteam.aether.entities.bosses;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.aetherteam.aether.Aether;
 import net.aetherteam.aether.AetherCommonPlayerHandler;
 import net.aetherteam.aether.AetherNameGen;
 import net.aetherteam.aether.dungeons.Dungeon;
 import net.aetherteam.aether.dungeons.DungeonHandler;
 import net.aetherteam.aether.dungeons.keys.DungeonKey;
 import net.aetherteam.aether.dungeons.keys.EnumKeyType;
 import net.aetherteam.aether.enums.EnumBossType;
 import net.aetherteam.aether.interfaces.IAetherBoss;
 import net.aetherteam.aether.packets.AetherPacketHandler;
 import net.aetherteam.aether.party.Party;
 import net.aetherteam.aether.party.PartyController;
 import net.aetherteam.aether.party.members.PartyMember;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 
 public class EntitySliderHostMimic extends EntityMiniBoss implements IAetherBoss
 {
     public boolean hasBeenAttacked;
     public int eyeCap = 4;
     public List Eyes = new ArrayList();
     public int scareTime;
     public String bossName;
     public int sendDelay = 15;
     public int sendRespawnDelay = 10;
     private int chatTime;
 
     public EntitySliderHostMimic(World var1)
     {
         super(var1);
         this.setSize(2.0F, 2.5F);
         this.bossName = AetherNameGen.gen();
         this.isImmuneToFire = true;
         this.health = this.getMaxHealth();
         this.moveSpeed = 1.2F;
         this.hasBeenAttacked = false;
         this.scareTime = 0;
         this.texture = "/net/aetherteam/aether/client/sprites/mobs/host/hostblue.png";
     }
 
     public void entityInit()
     {
         super.entityInit();
         this.dataWatcher.addObject(16, Byte.valueOf((byte) 0));
         this.dataWatcher.addObject(17, Byte.valueOf((byte) 0));
     }
 
     public boolean isAwake()
     {
         return this.dataWatcher.getWatchableObjectByte(16) == 1;
     }
 
     public void setAwake(boolean var1)
     {
         if (var1)
         {
             this.scareItUp();
             this.worldObj.playSoundAtEntity(this, "aeboss.slider.awake", 2.5F, 1.0F / (this.rand.nextFloat() * 0.2F + 0.9F));
             this.dataWatcher.updateObject(16, Byte.valueOf((byte) 1));
         } else
         {
             this.dataWatcher.updateObject(16, Byte.valueOf((byte) 0));
         }
     }
 
     public boolean isSendMode()
     {
         return this.dataWatcher.getWatchableObjectByte(17) == 1;
     }
 
     public void setSendMode(boolean var1)
     {
         if (var1)
         {
             this.dataWatcher.updateObject(17, Byte.valueOf((byte) 1));
         } else
         {
             this.dataWatcher.updateObject(17, Byte.valueOf((byte) 0));
         }
     }
 
     /**
      * Finds the closest player within 16 blocks to attack, or null if this Entity isn't interested in attacking
      * (Animals, Spiders at day, peaceful PigZombies).
      */
     protected Entity findPlayerToAttack()
     {
         EntityPlayer var1 = this.worldObj.getClosestVulnerablePlayerToEntity(this, 8.5D);
         return var1 != null && this.canEntityBeSeen(var1) ? var1 : null;
     }
 
     /**
      * Returns the sound this mob makes while it's alive.
      */
     protected String getLivingSound()
     {
         return "ambient.cave.cave";
     }
 
     /**
      * Returns the sound this mob makes when it is hurt.
      */
     protected String getHurtSound()
     {
         return "step.stone";
     }
 
     /**
      * Returns the sound this mob makes on death.
      */
     protected String getDeathSound()
     {
         return "aeboss.slider.die";
     }
 
     /**
      * Plays step sound at given x, y, z for the entity
      */
     protected void playStepSound(int var1, int var2, int var3, int var4)
     {
         this.worldObj.playSoundAtEntity(this, "mob.cow.step", 0.15F, 1.0F);
     }
 
     /**
      * Takes in the distance the entity has fallen this tick and whether its on the ground to update the fall distance
      * and deal fall damage if landing on the ground.  Args: distanceFallenThisTick, onGround
      */
     protected void updateFallState(double var1, boolean var3)
     {
         if (this.isAwake())
         {
             super.updateFallState(var1, var3);
         }
     }
 
     /**
      * Called when the mob is falling. Calculates and applies fall damage.
      */
     protected void fall(float var1)
     {
         if (this.isAwake())
         {
             super.fall(var1);
         }
     }
 
     /**
      * Determines if an entity can be despawned, used on idle far away entities
      */
     public boolean canDespawn()
     {
         return false;
     }
 
     /**
      * Called to update the entity's position/logic.
      */
     public void onUpdate()
     {
         super.onUpdate();
         this.extinguish();
 
         if (this.chatTime >= 0)
         {
             --this.chatTime;
         }
 
         if (!this.isAwake())
         {
             this.texture = "/net/aetherteam/aether/client/sprites/bosses/slider/sliderSleep.png";
             this.motionX = 0.0D;
             this.motionY = 0.0D;
             this.motionZ = 0.0D;
             this.jumpMovementFactor = 0.0F;
             this.renderYawOffset = this.rotationPitch = this.rotationYaw = 0.0F;
         }
 
         EntityPlayer var1 = this.worldObj.getClosestPlayerToEntity(this, 8.5D);
 
         if (this.entityToAttack == null && var1 != null && this.canEntityBeSeen(var1) && !var1.isDead && !var1.capabilities.isCreativeMode)
         {
             this.entityToAttack = var1;
             this.setSendMode(true);
         }
 
         if (this.entityToAttack != null && this.entityToAttack instanceof EntityLiving && this.canEntityBeSeen(this.entityToAttack) && !this.entityToAttack.isDead)
         {
             this.faceEntity(this.entityToAttack, 10.0F, 10.0F);
 
             if (!this.isAwake())
             {
                 this.setAwake(true);
             }
 
             if (!this.hasBeenAttacked)
             {
                 this.hasBeenAttacked = true;
             }
 
             if (this.isSendMode())
             {
                 this.motionX = 0.0D;
                 this.motionY = 0.0D;
                 this.motionZ = 0.0D;
                 this.jumpMovementFactor = 0.0F;
                 this.renderYawOffset = this.rotationPitch = this.rotationYaw = 0.0F;
 
                 if (this.Eyes.size() < this.eyeCap)
                 {
                     if (this.sendDelay <= 0 && !this.worldObj.isRemote)
                     {
                         this.sendEye((EntityLiving) this.entityToAttack);
                     }
                 } else if (this.sendRespawnDelay <= 0)
                 {
                     if (!this.worldObj.isRemote)
                     {
                         this.sendEye((EntityLiving) this.entityToAttack);
                         this.sendRespawnDelay = 100;
                     }
                 } else
                 {
                     this.setSendMode(false);
                 }
             }
         } else
         {
             this.entityToAttack = null;
             this.hasBeenAttacked = false;
             this.killEyes();
             this.setAwake(false);
             this.setSendMode(false);
         }
 
         if (!this.hasBeenAttacked && this.isAwake())
         {
             this.texture = "/net/aetherteam/aether/client/sprites/mobs/host/hostblue.png";
             this.killEyes();
         }
 
         if (this.Eyes.size() > this.eyeCap)
         {
             ((Entity) this.Eyes.remove(0)).setDead();
         }
 
         if (this.hasBeenAttacked || this.entityToAttack != null && this.canEntityBeSeen(this.entityToAttack))
         {
             this.texture = "/net/aetherteam/aether/client/sprites/mobs/host/hostred.png";
         }
 
         if (this.scareTime > 0)
         {
             --this.scareTime;
         }
 
         if (this.sendDelay > 0)
         {
             --this.sendDelay;
         }
 
         if (this.sendRespawnDelay > 0)
         {
             --this.sendRespawnDelay;
         }
     }
 
     /**
      * Basic mob attack. Default to touch of death in EntityCreature. Overridden by each mob to define their attack.
      */
     protected void attackEntity(Entity var1, float var2)
     {
        if (!(var1 instanceof EntityLiving)) return;
         EntityLiving var3 = (EntityLiving) var1;
 
         if (var2 < 8.5F && this.canEntityBeSeen(var3))
         {
             double var4 = var1.posX - this.posX;
             double var6 = var1.posZ - this.posZ;
 
             if (var3 != null)
             {
                 if (var3.isDead || !this.canEntityBeSeen(var3))
                 {
                     var3 = null;
                 }
 
                 if (this.Eyes.size() <= 0 && this.canEntityBeSeen(var3))
                 {
                     this.setSendMode(true);
                 }
             }
 
             this.rotationYaw = (float) (Math.atan2(var6, var4) * 180.0D / Math.PI) - 90.0F;
             this.hasAttacked = true;
         }
     }
 
     public void sendEye(EntityLiving var1)
     {
         while (this.Eyes.size() > this.eyeCap)
         {
             ((Entity) this.Eyes.remove(0)).setDead();
         }
 
         this.hasBeenAttacked = true;
 
         if (!this.isAwake())
         {
             this.setAwake(true);
         }
 
         EntityHostEye var2 = new EntityHostEye(this.worldObj, this.posX - 1.5D, this.posY + 1.5D, this.posZ - 1.5D, this.rotationYaw, this.rotationPitch, this, var1);
         this.worldObj.spawnEntityInWorld(var2);
         this.worldObj.playSoundAtEntity(this, "aeboss.slider.awake", 2.5F, 1.0F / (this.rand.nextFloat() * 0.2F + 0.9F));
         this.Eyes.add(var2);
         this.sendDelay = 30;
     }
 
     public void killEyes()
     {
         while (this.Eyes.size() != 0)
         {
             ((Entity) this.Eyes.remove(0)).setDead();
         }
     }
 
     private void scareItUp()
     {
         if (this.scareTime <= 0)
         {
             this.worldObj.playSoundAtEntity(this, "aemob.sentryGolem.creepySeen", 5.0F, 1.0F);
             this.scareTime = 2000;
         }
     }
 
     /**
      * Adds to the current velocity of the entity. Args: x, y, z
      */
     public void addVelocity(double var1, double var3, double var5)
     {
         if (this.isAwake() && !this.isSendMode())
         {
             super.addVelocity(var1, var3, var5);
         }
     }
 
     /**
      * knocks back this entity
      */
     public void knockBack(Entity var1, int var2, double var3, double var5)
     {
         if (this.isAwake() && !this.isSendMode())
         {
             super.knockBack(var1, var2, var3, var5);
         }
     }
 
     /**
      * Called when the entity is attacked.
      */
     public boolean attackEntityFrom(DamageSource var1, int var2)
     {
         this.hasBeenAttacked = true;
         Entity var3 = var1.getSourceOfDamage();
         Entity var4 = var1.getEntity();
 
         if (var3 != null && var1.isProjectile())
         {
             if (var4 instanceof EntityPlayer && ((EntityPlayer) var4).getCurrentEquippedItem() != null)
             {
                 this.chatItUp((EntityPlayer) var4, "也许该换成剑来攻击, 我的" + ((EntityPlayer) var4).getCurrentEquippedItem().getItem().getItemDisplayName(((EntityPlayer) var4).getCurrentEquippedItem()) + "对付不了这玩意儿!");
                 this.chatTime = 60;
             }
 
             return false;
         } else
         {
             if (var4 instanceof EntityPlayer)
             {
                 EntityPlayer var5 = (EntityPlayer) var4;
                 AetherCommonPlayerHandler var6 = Aether.getPlayerBase(var5);
                 PartyMember var7 = PartyController.instance().getMember(var5);
                 Party var8 = PartyController.instance().getParty(var7);
                 Side var9 = FMLCommonHandler.instance().getEffectiveSide();
 
                 if (var6 != null)
                 {
                     boolean var10 = true;
 
                     if (!var5.isDead && var10)
                     {
                         var6.setCurrentBoss(this);
                     }
                 }
             }
 
             return super.attackEntityFrom(var1, var2);
         }
     }
 
     private void chatItUp(EntityPlayer var1, String var2)
     {
         if (this.chatTime <= 0)
         {
             Aether.proxy.displayMessage(var1, var2);
             this.chatTime = 60;
         }
     }
 
     /**
      * Checks if the entity's current position is a valid location to spawn this entity.
      */
     public boolean getCanSpawnHere()
     {
         int var1 = MathHelper.floor_double(this.posX);
         int var2 = MathHelper.floor_double(this.boundingBox.minY);
         int var3 = MathHelper.floor_double(this.posZ);
         return this.rand.nextInt(25) == 0 && this.getBlockPathWeight(var1, var2, var3) >= 0.0F && this.worldObj.checkNoEntityCollision(this.boundingBox) && this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).size() == 0 && !this.worldObj.isAnyLiquid(this.boundingBox) && this.worldObj.difficultySetting > 0;
     }
 
     /**
      * (abstract) Protected helper method to write subclass entity data to NBT.
      */
     public void writeEntityToNBT(NBTTagCompound var1)
     {
         super.writeEntityToNBT(var1);
         this.hasBeenAttacked = var1.getBoolean("HasBeenAttacked");
         this.killEyes();
         this.setAwake(var1.getBoolean("Awake"));
         this.setSendMode(var1.getBoolean("SendMode"));
     }
 
     /**
      * (abstract) Protected helper method to read subclass entity data from NBT.
      */
     public void readEntityFromNBT(NBTTagCompound var1)
     {
         super.readEntityFromNBT(var1);
         var1.setBoolean("HasBeenAttacked", this.hasBeenAttacked);
         var1.setBoolean("Awake", this.isAwake());
         var1.setBoolean("SendMode", this.isSendMode());
     }
 
     /**
      * Called when the mob's health reaches 0.
      */
     public void onDeath(DamageSource var1)
     {
         this.boss = new EntitySliderHostMimic(this.worldObj);
 
         if (var1.getEntity() instanceof EntityPlayer)
         {
             EntityPlayer var2 = (EntityPlayer) var1.getEntity();
             Party var3 = PartyController.instance().getParty(PartyController.instance().getMember(var2));
             Dungeon var4 = DungeonHandler.instance().getInstanceAt(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
 
             if (var4 != null && var3 != null)
             {
                 DungeonHandler.instance().addKey(var4, var3, new DungeonKey(EnumKeyType.Host));
                 PacketDispatcher.sendPacketToAllPlayers(AetherPacketHandler.sendDungeonKey(var4, var3, EnumKeyType.Host));
             }
         }
 
         this.killEyes();
         super.onDeath(var1);
     }
 
     public int getMaxHealth()
     {
         return 175;
     }
 
     public int getBossMaxHP()
     {
         return 175;
     }
 
     public int getBossEntityID()
     {
         return this.entityId;
     }
 
     public String getBossTitle()
     {
         return "拟态滑行主宰:" + this.bossName;
     }
 
     public Entity getBossEntity()
     {
         return this;
     }
 
     public int getBossStage()
     {
         return 0;
     }
 
     public EnumBossType getBossType()
     {
         return EnumBossType.MINI;
     }
 }
