 package loecraftpack.common.potions;
 
 import loecraftpack.ponies.abilities.mechanics.MechanicHiddenOres;
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.potion.Potion;
 
 /**
  * This effect allows the player to use the Ore Vision mechanic
  */
 public class PotionOreVision extends Potion {
 
 	public PotionOreVision(int potionID, boolean bad, int color) {
 		super(potionID, bad, color);
 	}
 	
 	public void performEffect(EntityLiving entityLiving, int level)
 	{
		if (entityLiving.worldObj.isRemote)
 		{
			if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.entityId == entityLiving.entityId)
			{
				MechanicHiddenOres.revealHiddenGems = true;
				MechanicHiddenOres.powerLevel = level;
			}
 		}
 	}
 	
 	public boolean isReady(int par1, int par2)
     {
 		int k;
         
         k = 20 >> par2;
         return k > 0 ? par1 % k == 0 : true;
     }
 }
