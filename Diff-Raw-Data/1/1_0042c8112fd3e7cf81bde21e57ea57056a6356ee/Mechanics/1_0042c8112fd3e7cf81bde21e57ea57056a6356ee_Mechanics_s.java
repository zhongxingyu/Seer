 package hunternif.mc.dota2items.core;
 
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.Sound;
 import hunternif.mc.dota2items.config.Config;
 import hunternif.mc.dota2items.core.buff.BuffInstance;
 import hunternif.mc.dota2items.effect.Effect;
 import hunternif.mc.dota2items.effect.EffectInstance;
 import hunternif.mc.dota2items.item.Dota2Item;
 import hunternif.mc.dota2items.util.MCConstants;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.ai.attributes.AttributeInstance;
 import net.minecraft.entity.ai.attributes.AttributeModifier;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.monster.EntityCreeper;
 import net.minecraft.entity.monster.IMob;
 import net.minecraft.entity.passive.EntityWolf;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EntityDamageSourceIndirect;
 import net.minecraft.util.MathHelper;
 import net.minecraftforge.common.IExtendedEntityProperties;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
 import net.minecraftforge.event.entity.living.LivingAttackEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 import net.minecraftforge.event.entity.player.PlayerDropsEvent;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 import cpw.mods.fml.relauncher.Side;
 
 public class Mechanics {
 	/** I have no idea how to generate these properly. */
 	private static final UUID uuid = UUID.fromString("92f7a640-0ac7-11e3-8ffd-0800200c9a66");
 	
 	private static final String[] timeSinceIgnitedObfFields = {"timeSinceIgnited", "d", "field_70833_d"};
 	
 	private static final String EXT_PROP_STATS = "Dota2ItemsEntityStats";
 	
 	/** Equals to Base Hero health (with base strength bonuses) over Steve's base health.
 	 * This gives a zombie attack damage of 22.5~52.5. Seems fair to me. */
 	public static final float DOTA_VS_MINECRAFT_DAMAGE = (float)EntityStats.BASE_PLAYER_HP / MCConstants.MINECRAFT_PLAYER_HP;
 	public static final float GOLD_PER_MOB_HP = 2.5f;
 	public static final float GOLD_AWARDED_PER_LEVEL = 9f;
 	public static final float GOLD_LOST_PER_LEVEL = 30f;
 	public static final int FOOD_THRESHOLD_FOR_HEAL = 0;
 	public static final float GOLD_PER_SECOND = 0.25f;
 	public static final float STR_PER_LEVEL = 2;
 	public static final float AGI_PER_LEVEL = 2;
 	public static final float INT_PER_LEVEL = 2;
 	
 	/** In seconds. */
 	public static final float SYNC_STATS_INTERVAL = 10;
 	
 	private Map<EntityLivingBase, EntityStats> clientEntityStats = new ConcurrentHashMap<EntityLivingBase, EntityStats>();
 	private Map<EntityLivingBase, EntityStats> serverEntityStats = new ConcurrentHashMap<EntityLivingBase, EntityStats>();
 	private Map<EntityLivingBase, EntityStats> getEntityStatsMap(Side side) {
 		return side.isClient() ? clientEntityStats : serverEntityStats;
 	}
 	
 	private Map<EntityPlayer, ItemStack[]> clientInventories = new ConcurrentHashMap<EntityPlayer, ItemStack[]>();
 	private Map<EntityPlayer, ItemStack[]> serverInventories = new ConcurrentHashMap<EntityPlayer, ItemStack[]>();
 	private Map<EntityPlayer, ItemStack[]> getInventoryMap(Side side) {
 		return side.isClient() ? clientInventories : serverInventories;
 	}
 	
 	/** Guaranteed to be non-null. */
 	public EntityStats getOrCreateEntityStats(EntityLivingBase entity) {
 		Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(entity));
 		EntityStats stats = entityStats.get(entity);
 		if (stats == null) {
 			stats = new EntityStats(entity);
 			entityStats.put(entity, stats);
 		}
 		return stats;
 	}
 	
 	/** Can be null. */
 	public EntityStats getEntityStats(Entity entity) {
 		Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(entity));
 		return entityStats.get(entity);
 	}
 	
 	
 	@ForgeSubscribe
 	public void onPlayerDrops(PlayerDropsEvent event) {
 		Iterator<EntityItem> iter = event.drops.iterator();
 		while (iter.hasNext()) {
 			EntityItem entityItem = iter.next();
 			ItemStack stack = entityItem.getEntityItem();
 			if (stack.getItem() instanceof Dota2Item) {
 				Dota2Item dota2Item = (Dota2Item) stack.getItem();
 				if (!dota2Item.getDropsOnDeath()) {
 					iter.remove();
 					List<ItemStack> list = Dota2Items.playerTracker.retainedItems.get(event.entityPlayer);
 					if (list == null) {
 						list = new ArrayList<ItemStack>();
 						Dota2Items.playerTracker.retainedItems.put(event.entityPlayer, list);
 					}
 					list.add(stack.copy());
 					event.entityPlayer.inventory.addItemStackToInventory(stack);
 				}
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onLivingAttack(LivingAttackEvent event) {
 		//BUG in MC: when the player is hurt, this event is posted twice!
 		// Check if the entity can attack
 		Entity entity = event.source.getEntity();
 		if (!(event.source instanceof EntityDamageSourceIndirect) // Actual attack has already been performed
 				&& entity != null && entity instanceof EntityLivingBase) {
 			EntityStats stats = getEntityStats(entity);
 			if (stats != null) {
 				long worldTime = entity.worldObj.getTotalWorldTime();
 				if (stats.lastAttackTime == worldTime) {
 					//See the bug notice above
 					return;
 				}
 				boolean attackTimeoutPassed = stats.lastAttackTime +
 						(long)(stats.getAttackTime() * MCConstants.TICKS_PER_SECOND) <= worldTime;
 				if (stats.canAttack() && attackTimeoutPassed) {
 					stats.lastAttackTime = worldTime;
 				} else {
 					event.setCanceled(true);
 				}
 			}
 		}
 		
 		EntityStats targetStats = getEntityStats(event.entityLiving);
 		EntityStats sourceStats = null;
 		if (event.source.getEntity() instanceof EntityLivingBase) {
 			sourceStats = getEntityStats(event.source.getEntity());
 		}
 		if (targetStats != null) {
 			if (targetStats.isInvulnerable()) {
 				Dota2Items.logger.info("invulnerable");
 				event.setCanceled(true);
 				return;
 			} else if (event.source.isMagicDamage() && targetStats.isMagicImmune()) {
 				Dota2Items.logger.info("magic immune");
 				event.setCanceled(true);
 				return;
 			}
 			// Try evading the attack:
 			boolean trueStrike = sourceStats != null && sourceStats.isTrueStrike();
 			if (targetStats.canEvade() && !trueStrike) {
 				Dota2Items.logger.info("evaded");
 				event.setCanceled(true);
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onLivingHurt(LivingHurtEvent event) {
 		float dotaDamage = event.ammount * DOTA_VS_MINECRAFT_DAMAGE;
 		if (dotaDamage == Float.POSITIVE_INFINITY) {
 			// This much damage can only come from a "kill" command, so disregard all calculations:
 			return;
 		}
 		
 		// Check if the target entity is invulnerable or if damage is magical and target is magic immune
 		EntityStats targetStats = getEntityStats(event.entityLiving);
 		EntityStats sourceStats = null;
 		if (event.source.getEntity() instanceof EntityLivingBase) {
 			sourceStats = getEntityStats(event.source.getEntity());
 		}
 		
 		// Apply attack bonuses to the attacker
 		if (sourceStats != null) {
 			dotaDamage += sourceStats.getBonusDamage();
 			dotaDamage = sourceStats.getDamage(dotaDamage, !event.source.isProjectile());
 			float critMultiplier = sourceStats.getCriticalMultiplier();
 			if (critMultiplier > 1f) {
 				if (sourceStats.entity instanceof EntityPlayer) {
 					((EntityPlayer)sourceStats.entity).onCriticalHit(event.entityLiving);
 				}
 				Dota2Items.logger.info("crit");
 				dotaDamage *= critMultiplier;
 				sourceStats.entity.worldObj.playSoundAtEntity(sourceStats.entity, Sound.CRIT.getName(), 1, 1);
 			}
 			// If the player is the attacker, his target must be given EntityStats:
 			if (targetStats == null && sourceStats.entity instanceof EntityPlayer) {
 				targetStats = getOrCreateEntityStats(event.entityLiving);
 				targetStats.addPlayerAttackerID(sourceStats.entity.entityId);
 			}
 		}
 		
 		if (event.source.isMagicDamage()) {
 			if (targetStats != null) {
 				dotaDamage *= 1f - targetStats.getSpellResistance();
 				dotaDamage *= targetStats.getAmplifyDamage(true);
 				//TODO test spell resistance and magic amplification.
 			}
 		} else {// Armor only applies to non-magical damage
 			if (targetStats != null) {
 				// Apply damage block:
 				ItemStack targetEquippedItem = event.entityLiving.getCurrentItemOrArmor(0);
 				boolean targetIsRanged = targetEquippedItem != null &&
 						(targetEquippedItem.itemID == Item.bow.itemID || targetEquippedItem.itemID == Config.daedalus.getID());
 				boolean isHero = event.source.getEntity() instanceof EntityPlayer;
 				dotaDamage -= targetStats.getDamageBlock(!targetIsRanged, isHero);
 				if (dotaDamage < 0) dotaDamage = 0;
 				
 				// Apply armor bonuses to the entity being hurt
 				int armor = 0;
 				armor = targetStats.getArmor();
 				// The formula was taken from Dota 2 Wiki
 				float armorMultiplier = 1f;
 				if (armor > 0) {
 					armorMultiplier = 1f - ((0.06f * (float)armor) / (1 + 0.06f * (float)armor));
 				} else if (armor < 0) {
 					armor = Math.max(-20, armor);
 					armorMultiplier = 2f - (float) Math.pow(0.94, (double) -armor);
 				}
 				dotaDamage *= armorMultiplier;
 				dotaDamage *= targetStats.getAmplifyDamage(false);
 			}
 			
 			// Apply lifesteal:
 			if (sourceStats != null) {
 				//TODO implement Unique Attack Modifiers
 				float lifeStolen = dotaDamage * sourceStats.getLifestealMultiplier();
 				if (lifeStolen > 0) {
 					sourceStats.heal(lifeStolen);
 					Entity entity = event.source.getEntity();
 					EffectInstance effect = new EffectInstance(Effect.lifesteal, entity.posX, entity.posY+1, entity.posZ);
 					EffectInstance.notifyPlayersAround(effect, entity);
 				}
 			}
 		}
 		
 		//--------------- Recalculate Dota damage to Minecraft -----------------
 		
 		// If target has bonus health, decrease damage accrodringly:
 		float bonusHealthMultiplier = 1f;
 		if (targetStats != null) {
 			bonusHealthMultiplier = (float)targetStats.baseHealth / (float)targetStats.getMaxHealth();
 		}
 		dotaDamage *= bonusHealthMultiplier;
 		
 		float floatMCDamage = dotaDamage / DOTA_VS_MINECRAFT_DAMAGE;
 		int intMCDamage = MathHelper.floor_float(floatMCDamage);
 		// Store or apply the partial damage, that doesn't constitute enough to deplete 1 half-heart.
 		if (targetStats != null) {
 			intMCDamage = targetStats.getDamageFloor(floatMCDamage);
 		}
 		if (event.entityLiving instanceof EntityPlayer || event.source.getEntity() instanceof EntityPlayer) {
 			Dota2Items.logger.info(String.format("Changed damage from %.2f to %.2f", event.ammount, floatMCDamage));
 		}
 		event.ammount = intMCDamage;
 	}
 	
 	private void updateMoveSpeed(EntityLivingBase entity, EntityStats stats) {
 		AttributeInstance moveSpeedAttribute = entity.func_110148_a(SharedMonsterAttributes.field_111263_d);
 		double newMoveSpeed = stats.getMovementSpeed();
 		double oldMoveSpeed = moveSpeedAttribute.func_111126_e();
 		if (newMoveSpeed != oldMoveSpeed) {
 			double baseMoveSpeed = moveSpeedAttribute.func_111125_b();
 			// Get the modifier:
 			AttributeModifier speedModifier = moveSpeedAttribute.func_111127_a(uuid);
 			if (speedModifier != null) {
 				// Remove the old modifier
 				moveSpeedAttribute.func_111124_b(speedModifier);
 			}
 			// I think the argument "2" stands for operation "add percentage":
 			speedModifier = new AttributeModifier(uuid, "Speed bonus from Dota 2 Items", newMoveSpeed / baseMoveSpeed - 1.0, 2)
 				.func_111168_a(false); // I think this makes it non-persistent
 			moveSpeedAttribute.func_111121_a(speedModifier);
 		}
 	}
 	
 	private void checkAndUpdatePlayerInventory(EntityPlayer player) {
 		Side side = getSide(player);
 		Map<EntityPlayer, ItemStack[]> inventoryMap = getInventoryMap(side);
 		if (player.inventory == null) {
 			return;
 		}
 		ItemStack[] currentInventory = Arrays.copyOfRange(player.inventory.mainInventory, 0, 10);
 		// Check the item being dragged too:
 		currentInventory[9] = player.inventory.getItemStack();
 		ItemStack[] oldInventory = inventoryMap.get(player);
 		if (oldInventory == null) {
 			inventoryMap.put(player, currentInventory);
 			updatePlayerInventoryBuffs(player);
 		} else {
 			if (!isHotbarWithSameItems(currentInventory, oldInventory)) {
 				inventoryMap.put(player, currentInventory);
 				updatePlayerInventoryBuffs(player);
 			}
 		}
 	}
 	
 	private void updatePlayerInventoryBuffs(EntityPlayer player) {
 		Dota2Items.logger.fine("Updating buffs on player " + player.username);
 		EntityStats stats = getOrCreateEntityStats(player);
 		// Remove all passive item Buffs to add them again later:
 		for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 			if (buffInst.isItemPassiveBuff()) {
 				stats.removeBuff(buffInst);
 			}
 		}
 		for (int i = 0; i < 10; i++) {
 			ItemStack stack = player.inventory.mainInventory[i];
 			if (stack != null && stack.getItem() instanceof Dota2Item) {
 				Dota2Item item = (Dota2Item) stack.getItem();
 				if (item.getPassiveBuff() != null) {
 					stats.addBuff(new BuffInstance(item.getPassiveBuff(), player.entityId, true));
 				}
 			}
 		}
 		// Add the item being dragged too:
 		ItemStack stack = player.inventory.getItemStack();
 		if (stack != null && stack.getItem() instanceof Dota2Item) {
 			Dota2Item item = (Dota2Item) stack.getItem();
 			if (item.getPassiveBuff() != null) {
 				stats.addBuff(new BuffInstance(item.getPassiveBuff(), player.entityId, true));
 			}
 		}
 	}
 	
 	private static boolean isHotbarWithSameItems(ItemStack[] bar1, ItemStack[] bar2) {
 		if (bar1.length != bar2.length) {
 			return false;
 		}
 		for (int i = 0; i < bar1.length; i++) {
 			ItemStack stack1 = bar1[i];
 			ItemStack stack2 = bar2[i];
 			if ((stack1 == null && stack2 != null) ||
 				(stack1 != null && stack2 == null) ||
 				(stack1 != null && stack2 != null && !stack1.isItemEqual(stack2))) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	@ForgeSubscribe
 	public void onLivingUpdate(LivingUpdateEvent event) {
 		// All forced movement is to be processed here. (Cyclone, Force Staff etc.)
 		EntityStats stats = getEntityStats(event.entityLiving);
 		if (stats == null) {
 			return;
 		}
 		if (event.entityLiving instanceof EntityPlayer) {
 			checkAndUpdatePlayerInventory((EntityPlayer)event.entityLiving);
 			// Regenerate health and mana every second:
 			regenHealthManaAndGold((EntityPlayer)event.entityLiving, stats);
 			// Add base attributes per level:
 			addBaseAttributes((EntityPlayer)event.entityLiving, stats);
 			// Synchronize stats with all clients every SYNC_STATS_INTERVAL seconds:
 			int time = event.entityLiving.ticksExisted;
 			if (!event.entityLiving.worldObj.isRemote && time - stats.lastSyncTime >=
 					(long) (MCConstants.TICKS_PER_SECOND * SYNC_STATS_INTERVAL)) {
 				stats.sendSyncPacketToClient((EntityPlayer)event.entityLiving);
 			}
 		}
 		stats.clampMana();
 		for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 			if (!buffInst.isPermanent() && event.entity.worldObj.getTotalWorldTime() > buffInst.endTime) {
 				stats.removeBuff(buffInst);
 			}
 		}
 		updateMoveSpeed(event.entityLiving, stats);
 		if (!stats.canMove()) {
 			event.setCanceled(true);
 			// Update items in inventory so that cooldown keeps on ticking:
 			if (event.entityLiving instanceof EntityPlayer) {
 				((EntityPlayer)event.entityLiving).inventory.decrementAnimations();
 			}
 		}
 		// Workaround for creepers still exploding while having their attack disabled:
 		if (!stats.canAttack()) {
 			if (event.entityLiving instanceof EntityCreeper) {
 				EntityCreeper creeper = (EntityCreeper) event.entityLiving;
 				Integer timeSinceIgnited = ReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, timeSinceIgnitedObfFields);
 				ReflectionHelper.setPrivateValue(EntityCreeper.class, creeper, timeSinceIgnited.intValue()-1, timeSinceIgnitedObfFields);
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onLivingDeath(LivingDeathEvent event) {
 		Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(event.entityLiving));
 		EntityStats stats = entityStats.get(event.entityLiving);
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
 					EntityStats playerStats = getOrCreateEntityStats(player);
 					// Award reliable gold. Disregarding killing streak so far.
 					playerStats.addGold(awardedGold / playerAttackersIDs.size(), 0);
 					playerStats.sendSyncPacketToClient(player);
 				}
 				//TODO: award some gold to non-killer players who are just chilling around
 			}
 		} else {
 			if (event.source.getEntity() instanceof EntityPlayer) {
 				EntityPlayer killer = (EntityPlayer)event.source.getEntity();
 				// Gold is dropped from monsters (IMob) and angry wolves:
 				if (!event.entity.worldObj.isRemote && (event.entity instanceof IMob ||
 						(event.entity instanceof EntityWolf && ((EntityWolf)event.entity).isAngry()))) {
 					int goldAmount = MathHelper.floor_float(GOLD_PER_MOB_HP * (float)event.entityLiving.func_110138_aP());
 					EntityStats killerStats = getOrCreateEntityStats(killer);
 					// From npc kills - only unreliable gold:
 					killerStats.addGold(0, goldAmount);
 					killerStats.sendSyncPacketToClient(killer);
 				}
 			}
 			entityStats.remove(event.entityLiving);
 		}
 	}
 	
 	private static Side getSide(Entity entity) {
 		return entity.worldObj.isRemote ? Side.CLIENT : Side.SERVER;
 	}
 	
 	private static void regenHealthManaAndGold(EntityLivingBase entity, EntityStats stats) {
 		if (shouldHeal(entity, stats)) {
 			stats.heal(stats.getHealthRegen() / MCConstants.TICKS_PER_SECOND);
 		}
 		// func_110143_aJ = "getHealth"
 		if (entity.func_110143_aJ() > 0 && stats.getMana() < stats.getMaxMana()) {
 			stats.addMana(stats.getManaRegen() / MCConstants.TICKS_PER_SECOND);
 		}
 		// Unreliable gold:
 		stats.addGold(0, GOLD_PER_SECOND / MCConstants.TICKS_PER_SECOND);
 	}
 	
 	public static boolean shouldHeal(EntityLivingBase entity, EntityStats stats) {
 		int health = stats.getHealth(entity);
 		int maxHealth = stats.getMaxHealth();
 		boolean shouldHeal = health > 0 && health < maxHealth;
 		if (entity instanceof EntityPlayer) {
 			shouldHeal &= ((EntityPlayer)entity).getFoodStats().getFoodLevel() >= FOOD_THRESHOLD_FOR_HEAL;
 		}
 		return shouldHeal;
 	}
 	
 	private static void addBaseAttributes(EntityPlayer player, EntityStats stats) {
 		stats.setBaseStrength(EntityStats.BASE_PLAYER_STR + player.experienceLevel * STR_PER_LEVEL);
 		stats.setBaseAgility(EntityStats.BASE_PLAYER_AGI + player.experienceLevel * AGI_PER_LEVEL);
 		stats.setBaseIntelligence(EntityStats.BASE_PLAYER_INT + player.experienceLevel * INT_PER_LEVEL);
 	}
 	
 	@ForgeSubscribe
 	public void onEntityConstructing(EntityConstructing event) {
 		if (event.entity instanceof EntityPlayer && !event.entity.worldObj.isRemote) {
 			event.entity.registerExtendedProperties(EXT_PROP_STATS, getOrCreateEntityStats((EntityLivingBase)event.entity));
 		}
 	}
 	
 	/**
 	 * Upon respawn the EntityPlayer is constructed anew, however, with a wrong
 	 * entityID at the moment of EntityConstructing event dispatch. That entityID
 	 * is changed later on, but the ExtendedProperties have already been written
 	 * and cannot be removed. So let us manually copy the required values into
 	 * the existing ExtendedProperties.  
 	 */
 	EntityStats onPlayerRespawn(EntityPlayer player) {
 		IExtendedEntityProperties props = (player.getExtendedProperties(EXT_PROP_STATS));
 		if (props != null) {
 			EntityStats oldStats = getOrCreateEntityStats(player);
 			EntityStats newStats = (EntityStats)props;
 			newStats.entity = player;
 			newStats.setGold(oldStats.getReliableGold(), oldStats.getUnreliableGold());
 			Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(player));
 			entityStats.put(player, newStats);
 			updatePlayerInventoryBuffs(player);
 			newStats.setMana(newStats.getMaxMana());
 			return newStats;
 		}
 		return getOrCreateEntityStats(player);
 	}
 	
 	/*@ForgeSubscribe
 	public void onPickupGold(EntityItemPickupEvent event) {
 		ItemStack stack = event.item.getEntityItem();
 		if (stack.itemID == Config.goldCoin.getID()) {
 			event.entityLiving.worldObj.playSoundAtEntity(event.entity, Sound.COINS.getName(), 0.8f, 1f);
 			EntityStats stats = getOrCreateEntityStats(event.entityLiving);
 			stats.addGold(stack.stackSize);
 			if (!event.entityLiving.worldObj.isRemote) {
 				stats.sendSyncPacketToClient((EntityPlayer)event.entityLiving);
 			}
 			event.item.setDead();
 			event.setCanceled(true);
 		}
 	}
 	
 	*//** Drops gold coins at given entity in 5 approx. equal portions. *//*
 	private static void scatterGoldAt(Entity entity, int goldAmount) {
 		int portion = MathHelper.ceiling_float_int((float)goldAmount / 5f);
 		while (goldAmount > 0) {
 			int curPortion = goldAmount > portion ? portion : goldAmount;
 			goldAmount -= curPortion;
 			entity.dropItem(Config.goldCoin.getID(), curPortion);
 		}
 	}*/
 }
