 package assets.levelup;
 
 import java.util.Map;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.entity.projectile.EntityArrow;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
 import net.minecraftforge.event.entity.player.ArrowNockEvent;
 
 public class BowEventHandler {
 	
 	@ForgeSubscribe(receiveCanceled=true)
 	public void onSpawn(EntityJoinWorldEvent event)
 	{
 		if(event.entity instanceof EntityArrow )
 		{
 			EntityArrow arrow = (EntityArrow) event.entity;
 			if(arrow.shootingEntity instanceof EntityPlayer)
 			{
 				int archer = getArcherSkill((EntityPlayer) arrow.shootingEntity);
 				if(archer!=0)
 				{
 					arrow.motionX *= 1.0F + archer / 100F;
 					arrow.motionY *= 1.0F + archer / 100F;
 					arrow.motionZ *= 1.0F + archer / 100F;
 				}
 			}
 		}
 		else if(event.entity instanceof EntityPlayerMP)
 		{
 			byte cl = PlayerExtendedProperties.getPlayerClass((EntityPlayer) event.entity);
 			int death = PlayerExtendedProperties.getPlayerDeathLevel((EntityPlayer) event.entity);
 			Map<String,Integer> skills = PlayerExtendedProperties.getSkillMap((EntityPlayer) event.entity);
 			int[] data = new int[1+skills.size()];
 			data[0] = death;
 			for(int i = 0; i<skills.size();i++)
 			{
 				data[1+i] = skills.get(ClassBonus.skillNames[i]);
 			}
 			((EntityPlayerMP)event.entity).playerNetServerHandler.sendPacketToPlayer(SkillPacketHandler.getPacket("INIT", event.entity.entityId, cl, data));
 		}
 	}
 	
 	@ForgeSubscribe(receiveCanceled=true)
 	public void onBowUse(ArrowNockEvent event)
 	{
		int archer = getArcherSkill(event.entityPlayer);
 		if(archer!=0)
 		{
			if (event.entityPlayer.capabilities.isCreativeMode || event.entityPlayer.inventory.hasItem(Item.arrow.itemID))
 	        {
				event.entityPlayer.setItemInUse(event.result, event.result.getItem().getMaxItemUseDuration(event.result)-archer/5);
 	        }
 			event.setCanceled(true);
 		}
 	}
 	
 	public static int getArcherSkill(EntityPlayer player)
 	{
 		return PlayerExtendedProperties.getSkillFromIndex(player, 5);
 	}
 }
