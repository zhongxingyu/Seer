 package MysticWorld.Entity;
 
 import net.minecraft.entity.EntityAgeable;
 import net.minecraft.entity.ai.EntityAIControlledByPlayer;
 import net.minecraft.entity.ai.EntityAIFollowParent;
 import net.minecraft.entity.ai.EntityAILookIdle;
 import net.minecraft.entity.ai.EntityAIMate;
 import net.minecraft.entity.ai.EntityAIPanic;
 import net.minecraft.entity.ai.EntityAISwimming;
 import net.minecraft.entity.ai.EntityAITempt;
 import net.minecraft.entity.ai.EntityAIWander;
 import net.minecraft.entity.ai.EntityAIWatchClosest;
 import net.minecraft.entity.effect.EntityLightningBolt;
 import net.minecraft.entity.monster.EntityPigZombie;
 import net.minecraft.entity.passive.EntityAnimal;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.stats.AchievementList;
 import net.minecraft.world.World;
 
 public class EntityPigman extends EntityAnimal
 {
     public EntityPigman(World par1World)
     {
         super(par1World);
         this.setSize(0.9F, 0.9F);
         this.getNavigator().setAvoidsWater(true);
         float f = 0.25F;
         this.tasks.addTask(0, new EntityAISwimming(this));
         this.tasks.addTask(1, new EntityAIPanic(this, 0.38F));
         this.tasks.addTask(3, new EntityAIMate(this, f));
         this.tasks.addTask(4, new EntityAITempt(this, 0.3F, Item.carrot.itemID, false));
         this.tasks.addTask(5, new EntityAIFollowParent(this, 0.28F));
         this.tasks.addTask(6, new EntityAIWander(this, f));
         this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
         this.tasks.addTask(8, new EntityAILookIdle(this));
     }
     
     
     public boolean isAIEnabled()
     {
         return true;
     }
 
     public int getMaxHealth()
     {
         return 20;
     }
 
     protected void updateAITasks()
     {
         super.updateAITasks();
     }
 
     protected void entityInit()
     {
         super.entityInit();
         this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
     }
     
     protected String getLivingSound()
     {
         return "mob.pig.say";
     }
 
     protected String getHurtSound()
     {
         return "mob.pig.say";
     }
 
     protected String getDeathSound()
     {
         return "mob.pig.death";
     }
 
     protected void playStepSound(int par1, int par2, int par3, int par4)
     {
         this.playSound("mob.pig.step", 0.15F, 1.0F);
     }
 
     protected int getDropItemId()
     {
         return this.isBurning() ? Item.porkCooked.itemID : Item.porkRaw.itemID;
     }
 
     protected void dropFewItems(boolean par1, int par2)
     {
         int j = this.rand.nextInt(3) + 1 + this.rand.nextInt(1 + par2);
 
         for (int k = 0; k < j; ++k)
         {
             if (this.isBurning())
             {
                 this.dropItem(Item.porkCooked.itemID, 1);
             }
             else
             {
                 this.dropItem(Item.porkRaw.itemID, 1);
             }
         }
 
 
     }
 
     public void onStruckByLightning(EntityLightningBolt par1EntityLightningBolt)
     {
         if (!this.worldObj.isRemote)
         {
             EntityPigZombie entitypigzombie = new EntityPigZombie(this.worldObj);
             entitypigzombie.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
             this.worldObj.spawnEntityInWorld(entitypigzombie);
             this.setDead();
         }
     }
     
     public EntityPigman spawnBabyAnimal(EntityAgeable par1EntityAgeable)
     {
         return new EntityPigman(this.worldObj);
     }
 
     public boolean isBreedingItem(ItemStack par1ItemStack)
     {
         return par1ItemStack != null && par1ItemStack.itemID == Item.carrot.itemID;
     }
 
     public EntityAgeable createChild(EntityAgeable par1EntityAgeable)
     {
         return this.spawnBabyAnimal(par1EntityAgeable);
     }
 }
