 package mods.cc.rock.event;
 
 import java.util.Random;
 
import mods.cc.rock.mod_CookingCraft;
 import net.minecraft.entity.monster.EntityZombie;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingDropsEvent;
 
 public class CookingCraftLivingDropsEvent {
 	private Random rand;
 	@ForgeSubscribe
 	public void onEntityDrop(LivingDropsEvent event)
 	{
 		this.rand = new Random();
 		if(event.entityLiving instanceof EntityZombie)
 		{
 			//event.entityLiving.dropItem(mod_CookingCraft.fryingPan.itemID, this.rand.nextInt(3));
 		}
 	}
 
 }
