 package net.minecraft.src;
 
 public class EntityFBMob extends EntityMob
 {
 
 	public EntityFBMob(World world) 
 	{
 		super(world);
 	}
 	
 	public void handleHealthUpdate(byte byte0)
     {
         if(byte0 == 2)
         {
             field_704_R = 1.5F;
             heartsLife = heartsHalvesLife;
             hurtTime = maxHurtTime = 10;
             attackedAtYaw = 0.0F;
             worldObj.playSoundAtEntity(this, getHurtSound(), getSoundVolume(), 1.0F);
             attackEntityFrom(DamageSource.generic, 0);
         } else
         if(byte0 == 3)
         {
             worldObj.playSoundAtEntity(this, getDeathSound(), getSoundVolume(), 1.0F);
             health = 0;
             onDeath(DamageSource.generic);
         } else
         {
             super.handleHealthUpdate(byte0);
         }
     }
 	
    public boolean attackEntityFrom(DamageSource damagesource, int i)
     {
         if(worldObj.multiplayerWorld)
         {
             return false;
         }
         entityAge = 0;
         if(health <= 0)
         {
             return false;
         }
         field_704_R = 1.5F;
         boolean flag = true;
         if((float)heartsLife > (float)heartsHalvesLife / 2.0F)
         {
             if(i <= naturalArmorRating)
             {
                 return false;
             }
             damageEntity(damagesource, i - naturalArmorRating);
             naturalArmorRating = i;
             flag = false;
         } else
         {
         	naturalArmorRating = i;
             prevHealth = health;
             heartsLife = heartsHalvesLife;
             damageEntity(damagesource, i);
             hurtTime = maxHurtTime = 10;
         }
         attackedAtYaw = 0.0F;
         Entity entity = damagesource.getEntity();
         if (entity != null)
         {
             if (entity instanceof EntityPlayer)
             {
                 recentlyHit = 60;
                 attackingPlayer = (EntityPlayer)entity;
             }
             else if (entity instanceof EntityWolf)
             {
                 EntityWolf entitywolf = (EntityWolf)entity;
                 if (entitywolf.isTamed())
                 {
                     recentlyHit = 60;
                     attackingPlayer = null;
                 }
             }
         }
         if(flag)
         {
             worldObj.setEntityState(this, (byte)2);
             setBeenAttacked();
             if(entity != null)
             {
                 double d = entity.posX - posX;
                 double d1;
                 for(d1 = entity.posZ - posZ; d * d + d1 * d1 < 0.0001D; d1 = (Math.random() - Math.random()) * 0.01D)
                 {
                     d = (Math.random() - Math.random()) * 0.01D;
                 }
 
                 attackedAtYaw = (float)((Math.atan2(d1, d) * 180D) / 3.1415927410125732D) - rotationYaw;
                 knockBack(entity, i, d, d1);
             } else
             {
                 attackedAtYaw = (int)(Math.random() * 2D) * 180;
             }
         }
         if(health <= 0)
         {
             if(flag)
             {
                 worldObj.playSoundAtEntity(this, getDeathSound(), getSoundVolume(), 1.0F);
                 ModLoader.getMinecraftInstance().thePlayer.triggerAchievement(getKillAch());
             }
             onDeath(damagesource);
         } else
         if(flag)
         {
             worldObj.playSoundAtEntity(this, getHurtSound(), getSoundVolume(), 1.0F);
         }
         return true;
     }
     
     public int getMaxHealth()
 	{
 		return 12;
 	}
     
     public Achievement getKillAch()
     {
     	return null;
     }
     
     protected int field_34905_c;
     protected EntityPlayer field_34904_b;
 }
