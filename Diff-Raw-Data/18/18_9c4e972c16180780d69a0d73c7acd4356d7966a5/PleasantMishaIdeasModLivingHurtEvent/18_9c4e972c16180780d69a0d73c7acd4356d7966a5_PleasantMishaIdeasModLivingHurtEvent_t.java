 package artsin.pleasantmishaideas;
 
 import net.minecraft.entity.player.EntityPlayer;
 
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 
 public class PleasantMishaIdeasModLivingHurtEvent
 {
 	@ForgeSubscribe
 	public void onLivingHurt(LivingHurtEvent event)
 	{
 		if (event.entityLiving instanceof EntityPlayer)
 		{
 			EntityPlayer entityPlayer = (EntityPlayer)event.entityLiving;
 			
			if (entityPlayer.inventory.armorInventory[3] != null &&
					entityPlayer.inventory.armorInventory[2] != null &&
					entityPlayer.inventory.armorInventory[1] != null &&
					entityPlayer.inventory.armorInventory[0] != null)
 			{
				if (entityPlayer.inventory.armorInventory[3].itemID == PleasantMishaIdeasMod.ObsidianHelmet.shiftedIndex &&
						entityPlayer.inventory.armorInventory[2].itemID == PleasantMishaIdeasMod.ObsidianPlate.shiftedIndex &&
						entityPlayer.inventory.armorInventory[1].itemID == PleasantMishaIdeasMod.ObsidianLegs.shiftedIndex &&
						entityPlayer.inventory.armorInventory[0].itemID == PleasantMishaIdeasMod.ObsidianBoots.shiftedIndex)
				{
					event.setCanceled(true);
				}
 			}
 		}
 	}
 }
