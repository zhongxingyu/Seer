 package mod.culegooner.SpawnEggDropsMod;
 
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.EntityList;
 import net.minecraft.entity.monster.IMob;
 import net.minecraft.entity.passive.IAnimals;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.entity.projectile.EntityArrow;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 
 public class EntityLivingHandler {
 
 	@ForgeSubscribe
 	public void onEntityLivingDeath(LivingDeathEvent event) {
 		if (event.source.getDamageType().equals("player")) {
 			EntityPlayer p = (EntityPlayer) event.source.getEntity();
 
 			if (EnchantmentHelper.getEnchantmentLevel(SpawnEggDropsModBase.eSpawnDrop.effectId, p.getHeldItem()) == 1) {
 				if ((event.entityLiving instanceof IAnimals)
 						|| (event.entityLiving instanceof IMob)) {
 					double rand = Math.random();
 					//rand = 0.0d;
					if (rand < 0.25d) {
 						int id = EntityList.getEntityID(event.entityLiving);
 						if (id > 0 && EntityList.entityEggs.containsKey(id)) {
 							ItemStack dropEgg = new ItemStack(
 									Item.monsterPlacer, 1, id);
 							event.entityLiving.entityDropItem(dropEgg, 0.0F);
 						}
 					}
 
 				}
 			}
 
 		}
 
 		if (event.source.getSourceOfDamage() instanceof EntityArrow) {
 			if (((EntityArrow) event.source.getSourceOfDamage()).shootingEntity != null) {
 				if (((EntityArrow) event.source.getSourceOfDamage()).shootingEntity instanceof EntityPlayer) {
 
 					EntityPlayer p = (EntityPlayer) event.source.getEntity();
 					
 					if (EnchantmentHelper.getEnchantmentLevel(SpawnEggDropsModBase.eSpawnDrop.effectId, p.getHeldItem()) == 1) {
 						if ((event.entityLiving instanceof IAnimals)
 								|| (event.entityLiving instanceof IMob)) {
 							double rand = Math.random();
 							//rand = 0.0d;
 
							if (rand < 0.25d) {
 								int id = EntityList.getEntityID(event.entityLiving);
 								if (id > 0
 										&& EntityList.entityEggs.containsKey(id)) {
 									ItemStack dropEgg = new ItemStack(Item.monsterPlacer, 1, id);
 									event.entityLiving.entityDropItem(dropEgg, 0.0F);
 								}
 
 							}
 
 						}
 
 					}
 				}
 
 			}
 
 		}
 	}
 }
