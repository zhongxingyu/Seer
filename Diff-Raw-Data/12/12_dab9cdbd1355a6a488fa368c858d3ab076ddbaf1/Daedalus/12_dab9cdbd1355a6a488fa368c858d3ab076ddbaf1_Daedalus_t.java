 package hunternif.mc.dota2items.item;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.Sound;
 import hunternif.mc.dota2items.core.EntityStats;
 import hunternif.mc.dota2items.util.MCConstants;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityArrow;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 
 public class Daedalus extends Dota2Item {
 
 	public Daedalus(int id) {
 		super(id);
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
 		EntityStats stats = Dota2Items.mechanics.getOrCreateEntityStats(player);
 		long worldTime = world.getTotalWorldTime();
 		boolean attackTimeoutPassed = stats.lastAttackTime +
 				(long)(stats.getAttackTime() * MCConstants.TICKS_PER_SECOND) <= worldTime;
 		if (stats.canAttack() && attackTimeoutPassed) {
			stats.lastAttackTime = worldTime;
 		} else {
 			return stack;
 		}
 		
 		boolean infiniteArrows = player.capabilities.isCreativeMode ||
 				EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) > 0;
 				
 		if (infiniteArrows || player.inventory.hasItem(Item.arrow.itemID)) {
 			
 			EntityArrow entityarrow = new EntityArrow(world, player, 2.0F);
 			entityarrow.setIsCritical(true);
 
 			int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
 			if (k > 0) {
 				entityarrow.setDamage(entityarrow.getDamage() + (double)k * 0.5D + 0.5D);
 			}
 
 			int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
 			if (l > 0) {
 				entityarrow.setKnockbackStrength(l);
 			}
 
 			if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) > 0) {
 				entityarrow.setFire(100);
 			}
 
 			//stack.damageItem(1, player); Dota 2 Items are not damaged.
 			world.playSoundAtEntity(player, Sound.ARROW.getName(), 1, 1);
 
 			if (infiniteArrows) {
 				entityarrow.canBePickedUp = 2;
 			} else {
 				player.inventory.consumeInventoryItem(Item.arrow.itemID);
 			}
 
 			if (!world.isRemote) {
 				world.spawnEntityInWorld(entityarrow);
 			}
 		}
 		return stack;
 	}
 
 }
