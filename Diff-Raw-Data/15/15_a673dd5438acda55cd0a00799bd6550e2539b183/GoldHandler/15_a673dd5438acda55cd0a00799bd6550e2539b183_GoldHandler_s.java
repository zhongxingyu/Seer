 package hunternif.mc.dota2items.core;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.Sound;
 import hunternif.mc.dota2items.config.Config;
 import hunternif.mc.dota2items.util.MCConstants;
 
 import java.util.Set;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.monster.IMob;
 import net.minecraft.entity.passive.EntityWolf;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MathHelper;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
 
 public class GoldHandler implements IEntityUpdater {
 	public static final float GOLD_PER_MOB_HP = 2.5f;
 	public static final float GOLD_AWARDED_PER_LEVEL = 9f;
 	public static final float GOLD_LOST_PER_LEVEL = 30f;
 	public static final float GOLD_PER_SECOND = 0.25f;
 	
 	//@ForgeSubscribe
 	/**
 	 * Code for handling gold coin items. If it is ever used again, it must be
 	 * changed to account for reliable and unreliabale gold.
 	 */
 	public void onPickupGold(EntityItemPickupEvent event) {
 		ItemStack stack = event.item.getEntityItem();
 		if (stack.itemID == Config.goldCoin.getID()) {
 			event.entityLiving.worldObj.playSoundAtEntity(event.entity, Sound.COINS.getName(), 0.8f, 1f);
 			EntityStats stats = Dota2Items.stats.getOrCreateEntityStats(event.entityLiving);
 			stats.addGold(stack.stackSize, 0);
 			if (!event.entityLiving.worldObj.isRemote) {
 				stats.sendSyncPacketToClient((EntityPlayer)event.entityLiving);
 			}
 			event.item.setDead();
 			event.setCanceled(true);
 		}
 	}
 	
 	/** Drops gold coins at given entity in 5 approx. equal portions. */
 	public static void scatterGoldAt(Entity entity, int goldAmount) {
 		int portion = MathHelper.ceiling_float_int((float)goldAmount / 5f);
 		while (goldAmount > 0) {
 			int curPortion = goldAmount > portion ? portion : goldAmount;
 			goldAmount -= curPortion;
 			entity.dropItem(Config.goldCoin.getID(), curPortion);
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onLivingDeath(LivingDeathEvent event) {
		EntityStats stats = Dota2Items.stats.getEntityStats(event.entityLiving);
		if (stats == null) {
			return;
		}
 		// Drop gold coins:
 		if (event.entityLiving instanceof EntityPlayer) {
 			if (!event.entityLiving.worldObj.isRemote) {
 				int level = ((EntityPlayer)event.entityLiving).experienceLevel + 1;
 				
 				// Deduct unreliable gold from the dead player:
 				stats.deductUnreliableGold(GOLD_LOST_PER_LEVEL * level);
 				stats.sendSyncPacketToClient((EntityPlayer)event.entityLiving);
 				
 				// Award gold to assisting killers:
 				float awardedGold = 200 + GOLD_AWARDED_PER_LEVEL * level;
 				Set<Integer> playerAttackersIDs = stats.getPlayerAttackersIDs();
 				for (int playerID : playerAttackersIDs) {
 					EntityPlayer player = (EntityPlayer) event.entityLiving.worldObj.getEntityByID(playerID);
 					EntityStats playerStats = Dota2Items.stats.getOrCreateEntityStats(player);
 					// Award reliable gold. Disregarding killing streak so far.
 					playerStats.addGold(awardedGold / playerAttackersIDs.size(), 0);
 					playerStats.sendSyncPacketToClient(player);
 				}
 				//TODO: award some gold to non-killer players who are just chilling around
 			}
 		} else {
 			if (event.source.getSourceOfDamage() instanceof EntityPlayer) {
 				EntityPlayer killer = (EntityPlayer)event.source.getSourceOfDamage();
 				// Gold is dropped from monsters (IMob) and angry wolves:
 				if (!event.entity.worldObj.isRemote && (event.entity instanceof IMob ||
 						(event.entity instanceof EntityWolf && ((EntityWolf)event.entity).isAngry()))) {
 					int goldAmount = MathHelper.floor_float(GOLD_PER_MOB_HP * (float)event.entityLiving.getMaxHealth());
 					EntityStats killerStats = Dota2Items.stats.getOrCreateEntityStats(killer);
 					// From npc kills - only unreliable gold:
 					killerStats.addGold(0, goldAmount);
 					killerStats.sendSyncPacketToClient(killer);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void update(EntityLivingBase entity, EntityStats stats, LivingUpdateEvent event) {
 		if (entity instanceof EntityPlayer) {
 			// Add unreliable gold per second:
 			stats.addGold(0, GOLD_PER_SECOND / MCConstants.TICKS_PER_SECOND);
 		}
 	}
 }
