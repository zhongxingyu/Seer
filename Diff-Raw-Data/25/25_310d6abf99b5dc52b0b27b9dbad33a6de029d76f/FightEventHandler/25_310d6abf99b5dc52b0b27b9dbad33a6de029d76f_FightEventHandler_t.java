 package assets.levelup;
 
 import java.util.Random;
 
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemSword;
 import net.minecraft.util.DamageSource;
 import net.minecraft.util.EntityDamageSourceIndirect;
 import net.minecraft.util.MathHelper;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 
 public class FightEventHandler {
 
 	@ForgeSubscribe
 	public void onHurting(LivingHurtEvent event)
 	{
 		DamageSource damagesource = event.source;
 		float i = event.ammount;
 		if(damagesource.getEntity() instanceof EntityPlayer)
 		{
 			EntityPlayer entityplayer = (EntityPlayer)damagesource.getEntity();
 			if (damagesource instanceof EntityDamageSourceIndirect)
 	        {
 	            if (!damagesource.damageType.equals("arrow"))
 	            {
 	                i *= 1.0F + BowEventHandler.getArcherSkill(entityplayer) / 100F;
 	            }
 	            if (getDistance(event.entityLiving, entityplayer) < 256F && entityplayer.isSneaking() && !canSeePlayer(event.entityLiving) && !entityIsFacing(event.entityLiving, entityplayer))
 	            {
 	                i *= 1.5F;
 	                entityplayer.addChatMessage("Sneak attack for 1.5x damage!");
 	            }
 	        }
 	        else
 	        {
 	        	if(entityplayer.getCurrentEquippedItem() != null)
 	        	{
 	        		int j = getSwordSkill(entityplayer);
 	        		Random rand = new Random();
 	        		if(rand.nextDouble() <= j / 200D)
 	        			i *=2.0F;
 	        		i *= 1.0F +(int) (j/5) / 20F;
 	        	}
 	            if (entityplayer.isSneaking() && !canSeePlayer(event.entityLiving) && !entityIsFacing(event.entityLiving, entityplayer))
 	            {
 	                i *= 2.0F;
 	                entityplayer.addChatMessage("Sneak attack for 2x damage!");
 	            }
 	        }
 		}
 		if(event.entityLiving instanceof EntityPlayer)
 		{
 			EntityPlayer player = (EntityPlayer)event.entityLiving;
 			int j = getDefenseSkill(player);
 			if(!damagesource.isUnblockable())
 				i *= 1.0F -(int) (j/5) / 20F;
 			if(player.isBlocking() && new Random().nextFloat()<j/100F)
 			{
 				i *= 0F;
 			}
 		}
		event.ammount = i;
 	}
 	private int getDefenseSkill(EntityPlayer player)
 	{
 		return PlayerExtendedProperties.getSkillFromIndex(player, 2);
 	}
 	private int getSwordSkill(EntityPlayer player)
 	{
 		return PlayerExtendedProperties.getSkillFromIndex(player, 1);
 	}
 	public static boolean canSeePlayer(EntityLivingBase entityLiving)
     {
         EntityPlayer entityplayer = entityLiving.worldObj.getClosestVulnerablePlayerToEntity(entityLiving, 16D);
         if (entityplayer != null && entityLiving.canEntityBeSeen(entityplayer))
         {
             if (entityplayer.isSneaking())
             {
             	return entityHasVisionOf(entityLiving, entityplayer);
             }
             else
             {
                 return true;
             }
         }
         return false;
     }
 	
 	public static float getDistance(EntityLivingBase entityLiving, EntityLivingBase entityliving1)
     {
         return (float)MathHelper.floor_double_long((entityliving1.posX - entityLiving.posX) * (entityliving1.posX - entityLiving.posX) + (entityliving1.posZ - entityLiving.posZ) * (entityliving1.posZ - entityLiving.posZ));
     }
 
     public static float getPointDistance(double d, double d1, double d2, double d3)
     {
         return (float)MathHelper.floor_double_long((d2 - d) * (d2 - d) + (d3 - d1) * (d3 - d1));
     }
 
     public static boolean compareAngles(float f, float f1, float f2)
     {
         if (MathHelper.abs(f - f1) < f2)
         {
             return true;
         }
         if (f + f2 >= 360F)
         {
             float f3 = (f + f2) - 360F;
             if (f3 > f1)
             {
                 return true;
             }
         }
         if (f1 + f2 >= 360F)
         {
             float f4 = (f1 + f2) - 360F;
             if (f4 > f)
             {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean entityHasVisionOf(EntityLivingBase entityLiving, EntityPlayer player)
     {
         if (entityLiving == null || player == null)
         {
             return false;
         }
         if (getDistance(entityLiving, player) > 256F - (float)(PlayerExtendedProperties.getSkillFromIndex(player, "Sneaking") / 5) * 12.8F)
         {
             return false;
         }
         if (!entityLiving.canEntityBeSeen(player))
         {
             return false;
         }
         float f = -(float)(player.posX - entityLiving.posX);
         float f1 = (float)(player.posZ - entityLiving.posZ);
         float f2 = entityLiving.rotationYaw;
         if (f2 < 0.0F)
         {
             float f3 = ((float)MathHelper.floor_float(MathHelper.abs(f2) / 360F) + 1.0F) * 360F;
             f2 = f3 + f2;
         }
         else
         {
             for (; f2 > 360F; f2 -= 360F) { }
         }
         float f4 = ((float)Math.atan2(f, f1) * 180F) / 3.141593F;
         if (f < 0.0F)
         {
             f4 = 360F + f4;
         }
         return compareAngles(f2, f4, 22.5F);
     }
 
     public static boolean entityIsFacing(EntityLivingBase entityLiving, EntityLivingBase entityliving1)
     {
         if (entityLiving == null || entityliving1 == null)
         {
             return false;
         }
         float f = -(float)(entityliving1.posX - entityLiving.posX);
         float f1 = (float)(entityliving1.posZ - entityLiving.posZ);
         float f2 = entityLiving.rotationYaw;
         if (f2 < 0.0F)
         {
             float f3 = ((float)MathHelper.floor_float(MathHelper.abs(f2) / 360F) + 1.0F) * 360F;
             f2 = f3 + f2;
         }
         else
         {
             for (; f2 > 360F; f2 -= 360F) { }
         }
         float f4 = ((float)Math.atan2(f, f1) * 180F) / 3.141593F;
         if (f < 0.0F)
         {
             f4 = 360F + f4;
         }
         return compareAngles(f2, f4, 22.5F);
     }
 }
