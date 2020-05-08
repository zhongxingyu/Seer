 package hunternif.mc.dota2items.core;
 
 import hunternif.mc.dota2items.Config;
 import hunternif.mc.dota2items.Dota2Items;
 import hunternif.mc.dota2items.Sound;
 import hunternif.mc.dota2items.core.buff.BuffInstance;
 import hunternif.mc.dota2items.item.Dota2Item;
 import hunternif.mc.dota2items.network.EntityStatsSyncPacket;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.monster.EntityCreeper;
 import net.minecraft.entity.monster.IMob;
 import net.minecraft.entity.passive.EntityWolf;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.PlayerCapabilities;
 import net.minecraft.item.ItemStack;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.util.MathHelper;
 import net.minecraftforge.common.IExtendedEntityProperties;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
 import net.minecraftforge.event.entity.living.LivingAttackEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
 import net.minecraftforge.event.entity.player.PlayerDropsEvent;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 import cpw.mods.fml.relauncher.Side;
 
 public class Mechanics {
 	private static final String[] walkSpeedObfFields = {"walkSpeed", "g", "field_75097_g"};
 	private static final String[] timeSinceIgnitedObfFields = {"timeSinceIgnited", "d", "field_70833_d"};
 	
 	private static final String EXT_PROP_STATS = "Dota2ItemsEntityStats";
 	
 	/** Equals to Base Hero health (with base strength bonuses) over Steve's base health.
 	 * This gives a zombie attack damage of 22.5~52.5. Seems fair to me. */
 	public static final float DOTA_VS_MINECRAFT_DAMAGE = (float)EntityStats.BASE_PLAYER_HP/20f;
 	public static final float GOLD_PER_MOB_HP = 2.5f;
 	public static final float GOLD_LOST_PER_LEVEL = 30f;
 	
 	public static final int FOOD_THRESHOLD_FOR_HEAL = 10;
 	public static final float GOLD_PER_SECOND = 0.25f;
 	
 	private static final int SYNC_STATS_INTERVAL = 10;
 	
 	private Map<EntityLiving, EntityStats> clientEntityStats = new ConcurrentHashMap<EntityLiving, EntityStats>();
 	private Map<EntityLiving, EntityStats> serverEntityStats = new ConcurrentHashMap<EntityLiving, EntityStats>();
 	private Map<EntityLiving, EntityStats> getEntityStatsMap(Side side) {
 		return side.isClient() ? clientEntityStats : serverEntityStats;
 	}
 	
 	private Map<EntityPlayer, ItemStack[]> clientInventories = new ConcurrentHashMap<EntityPlayer, ItemStack[]>();
 	private Map<EntityPlayer, ItemStack[]> serverInventories = new ConcurrentHashMap<EntityPlayer, ItemStack[]>();
 	private Map<EntityPlayer, ItemStack[]> getInventoryMap(Side side) {
 		return side.isClient() ? clientInventories : serverInventories;
 	}
 	
 	/** Guaranteed to be non-null. */
 	public EntityStats getEntityStats(EntityLiving entity) {
 		Map<EntityLiving, EntityStats> entityStats = getEntityStatsMap(getSide(entity));
 		EntityStats stats = entityStats.get(entity);
 		if (stats == null) {
 			stats = new EntityStats(entity);
 			entityStats.put(entity, stats);
 		}
 		return stats;
 	}
 	
 	
 	@ForgeSubscribe
 	public void onPlayerDrops(PlayerDropsEvent event) {
 		Iterator<EntityItem> iter = event.drops.iterator();
 		while (iter.hasNext()) {
 			EntityItem entityItem = iter.next();
 			ItemStack stack = entityItem.getEntityItem();
 			if (stack.getItem() instanceof Dota2Item) {
 				Dota2Item dota2Item = (Dota2Item) stack.getItem();
 				if (!dota2Item.dropsOnDeath) {
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
 		// Check if the entity can attack
 		Entity entity = event.source.getEntity();
 		if (entity != null && entity instanceof EntityLiving) {
 			Map<EntityLiving, EntityStats> entityStats = getEntityStatsMap(getSide(entity));
 			EntityStats stats = entityStats.get(entity);
 			if (stats != null) {
 				long worldTime = entity.worldObj.getTotalWorldTime();
 				boolean attackTimeoutPassed = stats.lastAttackTime + (long)(stats.getAttackTime()*20f) <= worldTime;
 				if (stats.canAttack() && attackTimeoutPassed) {
 					stats.lastAttackTime = worldTime;
 				} else {
 					event.setCanceled(true);
 				}
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onLivingHurt(LivingHurtEvent event) {
 		Map<EntityLiving, EntityStats> entityStats = getEntityStatsMap(getSide(event.entity));
 		int damage = event.ammount;
 		float dotaDamage = (float)damage * DOTA_VS_MINECRAFT_DAMAGE;
 		
 		// Check if the target entity is invulnerable or if damage is magical and target is magic immune
 		EntityStats targetStats = entityStats.get(event.entityLiving);
 		if (targetStats != null) {
 			if (targetStats.isInvulnerable()) {
 				FMLLog.log(Dota2Items.ID, Level.FINE, "invulnerable");
 				event.setCanceled(true);
 				return;
 			} else if (event.source.isMagicDamage() && targetStats.isMagicImmune()) {
 				FMLLog.log(Dota2Items.ID, Level.FINE, "magic immune");
 				event.setCanceled(true);
 				return;
 			}
 		}
 		
 		// Apply attack bonuses to the source player
 		if (event.source.getEntity() instanceof EntityPlayer) {
 			EntityPlayer player = (EntityPlayer) event.source.getEntity();
 			EntityStats sourceStats = entityStats.get(player);
 			if (sourceStats != null) {
 				dotaDamage = sourceStats.getDamage(dotaDamage, !event.source.isProjectile());
 			}
 			// If the player is the attacker, his target must have the EntityStats:
 			if (targetStats == null) {
 				targetStats = new EntityStats(event.entityLiving);
 				entityStats.put(event.entityLiving, targetStats);
 			}
 		}
 		
 		// Apply armor bonuses to the player being hurt
 		int armor = 0;
 		if (targetStats != null) {
 			armor = targetStats.getArmor(armor);
 		}
 		
 		// The formula was taken from Dota 2 Wiki
 		float armorMultiplier = 1f;
 		if (armor > 0) {
 			armorMultiplier = 1f - ((0.06f * (float)armor) / (1 + 0.06f * (float)armor));
 		} else if (armor < 0) {
 			armor = Math.max(-20, armor);
 			armorMultiplier = 2f - (float) Math.pow(0.94, (double) -armor);
 		}
 		dotaDamage *= armorMultiplier;
 		
 		// Account for the fact that Stats may give bonus health.
 		float bonusHealthMultiplier = 1f;
 		if (targetStats != null) {
 			bonusHealthMultiplier = (float)targetStats.baseHealth / (float)targetStats.getMaxHealth();
 		}
 		dotaDamage *= bonusHealthMultiplier;
 		
 		float floatMCDamage = dotaDamage / DOTA_VS_MINECRAFT_DAMAGE;
 		int intMCDamage = MathHelper.floor_float(floatMCDamage);
 		// Store or apply the partial damage, that doesn't constitute enough to deplete 1 half-heart.
 		if (targetStats != null) {
 			float partialDamage = floatMCDamage - (float)intMCDamage - targetStats.partialHalfHeart;
 			int partialDamageFloor = MathHelper.floor_float(partialDamage);
 			intMCDamage += partialDamageFloor;
 			partialDamage -= (float)partialDamageFloor;
 			targetStats.partialHalfHeart = -partialDamage;
 			if (partialDamageFloor > 0) {
 				FMLLog.log(Dota2Items.ID, Level.INFO, "Applied carry-over damage: %d", partialDamageFloor);
 			}
 		}
 		if (event.entityLiving instanceof EntityPlayer || event.source.getEntity() instanceof EntityPlayer) {
 			FMLLog.log(Dota2Items.ID, Level.INFO, "Changed damage from %d to %.2f", damage, floatMCDamage);
 		}
 		event.ammount = intMCDamage;
 	}
 	
 	public void updateAllEntityStats(Side side) {
 		Map<EntityLiving, EntityStats> entityStats = getEntityStatsMap(side);
 		for (Entry<EntityLiving, EntityStats> entry : entityStats.entrySet()) {
 			EntityLiving entity = entry.getKey();
 			EntityStats stats = entry.getValue();
 			for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 				if (!buffInst.isItemPassiveBuff && entity.worldObj.getTotalWorldTime() > buffInst.endTime) {
 					stats.removeBuff(buffInst);
 				}
 			}
 		}
 	}
 	
 	public void updatePlayerInventories(Side side) {
 		List<EntityPlayer> players;
 		if (side.isClient()) {
 			players = new ArrayList<EntityPlayer>();
 			if (Minecraft.getMinecraft().thePlayer != null) {
 				players.add(Minecraft.getMinecraft().thePlayer);
 			}
 		} else {
 			players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
 		}
 		Map<EntityPlayer, ItemStack[]> inventoryMap = getInventoryMap(side);
 		for (EntityPlayer player : players) {
 			updatePlayerInventory(player);
 		}
 	}
 	private void updatePlayerInventory(EntityPlayer player) {
 		Side side = getSide(player);
 		Map<EntityPlayer, ItemStack[]> inventoryMap = getInventoryMap(side);
 		if (player.inventory == null) {
 			return;
 		}
 		ItemStack[] currentInventory = Arrays.copyOfRange(player.inventory.mainInventory, 0, 10);
 		ItemStack[] oldInventory = inventoryMap.get(player);
 		if (oldInventory == null) {
 			inventoryMap.put(player, currentInventory);
 			updatePlayerBuffs(player);
 		} else {
 			if (!sameItemsStacks(currentInventory, oldInventory)) {
 				inventoryMap.put(player, currentInventory);
 				updatePlayerBuffs(player);
 			}
 		}
 	}
 	
 	private void updatePlayerBuffs(EntityPlayer player) {
 		FMLLog.log(Dota2Items.ID, Level.FINER, "Updating buffs on player " + player.username);
 		EntityStats stats = getEntityStats(player);
 		// Remove all passive item Buffs to add them again later:
 		for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 			if (buffInst.isItemPassiveBuff) {
 				stats.removeBuff(buffInst);
 			}
 		}
 		for (int i = 0; i < 10; i++) {
 			ItemStack stack = player.inventory.mainInventory[i];
 			if (stack != null && stack.getItem() instanceof Dota2Item) {
 				Dota2Item item = (Dota2Item) stack.getItem();
 				if (item.passiveBuff != null) {
 					stats.addBuff(new BuffInstance(item.passiveBuff, player.entityId, true));
 				}
 			}
 		}
 		//NOTE for now movement speed bonus will only be applied to players, not to mobs:
 		ReflectionHelper.setPrivateValue(PlayerCapabilities.class, player.capabilities, stats.getMovementSpeed(), walkSpeedObfFields);
 	}
 	
 	private static boolean sameItemsStacks(ItemStack[] bar1, ItemStack[] bar2) {
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
 		Map<EntityLiving, EntityStats> entityStats = getEntityStatsMap(getSide(event.entityLiving));
 		EntityStats stats = entityStats.get(event.entityLiving);
 		if (stats != null) {
 			// Regenerate health and mana every second:
 			if (event.entityLiving instanceof EntityPlayer) {
 				regenHealthManaAndGold((EntityPlayer)event.entityLiving, stats);
 				// Synchronize stats with all clients every SYNC_STATS_INTERVAL seconds:
 				int time = event.entityLiving.ticksExisted;
				if (!event.entityLiving.worldObj.isRemote && time - stats.lastSyncTime >= 20 * SYNC_STATS_INTERVAL) {
 					stats.lastSyncTime = time;
 					PacketDispatcher.sendPacketToPlayer(new EntityStatsSyncPacket(stats).makePacket(), (Player)event.entityLiving);
 				}
 			}
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
 					int timeSinceIgnited = ReflectionHelper.getPrivateValue(EntityCreeper.class, creeper, timeSinceIgnitedObfFields);
 					ReflectionHelper.setPrivateValue(EntityCreeper.class, creeper, timeSinceIgnited-1, timeSinceIgnitedObfFields);
 				}
 			}
 		}
 	}
 	
 	@ForgeSubscribe
 	public void onLivingDeath(LivingDeathEvent event) {
 		Map<EntityLiving, EntityStats> entityStats = getEntityStatsMap(getSide(event.entityLiving));
 		EntityStats stats = entityStats.get(event.entityLiving);
 		if (stats != null) {
 			// Drop gold coins
 			if (event.entityLiving instanceof EntityPlayer) {
 				int level = ((EntityPlayer)event.entityLiving).experienceLevel + 1;
 				int goldAmount = MathHelper.floor_float(GOLD_LOST_PER_LEVEL*level);
 				if (!event.entityLiving.worldObj.isRemote) {
 					//200 + level*9; That would allow to farm lots of gold on your own death.
 					scatterGoldAt(event.entityLiving, goldAmount);
 					stats.removeGold(goldAmount);
 					PacketDispatcher.sendPacketToPlayer(new EntityStatsSyncPacket(stats).makePacket(), (Player)event.entityLiving);
 					
 				}
 			} else {
 				if (!event.entity.worldObj.isRemote && (event.entity instanceof IMob ||
 						(event.entity instanceof EntityWolf && ((EntityWolf)event.entity).isAngry()))) {
 					int goldAmount = MathHelper.floor_float(GOLD_PER_MOB_HP * (float)event.entityLiving.getMaxHealth());
 					scatterGoldAt(event.entity, goldAmount);
 				}
 				entityStats.remove(event.entityLiving);
 			}
 		}
 	}
 	
 	private static Side getSide(Entity entity) {
 		return entity.worldObj.isRemote ? Side.CLIENT : Side.SERVER;
 	}
 	
 	private static void regenHealthManaAndGold(EntityLiving entity, EntityStats stats) {
 		if (shouldHeal(entity)) {
 			float halfHeartEquivalent = (float)stats.getMaxHealth() / (float)entity.getMaxHealth();
 			float partialHealth = stats.partialHalfHeart + stats.getHealthRegen() /20f / halfHeartEquivalent;
 			if (partialHealth >= 1) {
 				int floor = MathHelper.floor_float(partialHealth);
 				entity.heal(floor);
 				partialHealth -= (float) floor;
 			}
 			stats.partialHalfHeart = partialHealth;
 		} else if (stats.partialHalfHeart > 0) {
 			stats.partialHalfHeart = 0;
 		}
 		if (entity.getHealth() > 0 && stats.getMana() < stats.getMaxMana()) {
 			stats.addMana(stats.getManaRegen()/20f);
 		}
 		stats.addGold(GOLD_PER_SECOND/20f);
 	}
 	
 	public static boolean shouldHeal(EntityLiving entity) {
 		boolean shouldHeal = entity.getHealth() > 0 && entity.getHealth() < entity.getMaxHealth();
 		if (entity instanceof EntityPlayer) {
 			shouldHeal &= ((EntityPlayer)entity).getFoodStats().getFoodLevel() >= FOOD_THRESHOLD_FOR_HEAL;
 		}
 		return shouldHeal;
 	}
 	
 	@ForgeSubscribe
 	public void onEntityConstructing(EntityConstructing event) {
 		if (event.entity instanceof EntityPlayer && !event.entity.worldObj.isRemote) {
 			event.entity.registerExtendedProperties(EXT_PROP_STATS, getEntityStats((EntityLiving)event.entity));
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
 			EntityStats oldStats = getEntityStats(player);
 			EntityStats newStats = (EntityStats)props;
 			newStats.entityId = oldStats.entityId;
 			newStats.setGold(oldStats.getFloatGold());
 			Map<EntityLiving, EntityStats> entityStats = getEntityStatsMap(getSide(player));
 			entityStats.put(player, newStats);
 			updatePlayerBuffs(player);
 			newStats.setMana(newStats.getMaxMana());
 			return newStats;
 		}
 		return getEntityStats(player);
 	}
 	
 	@ForgeSubscribe
 	public void onPickupGold(EntityItemPickupEvent event) {
 		ItemStack stack = event.item.getEntityItem();
 		if (stack.itemID == Config.goldCoin.getID()) {
 			event.entityLiving.worldObj.playSoundAtEntity(event.entity, Sound.COINS.name, 0.8f, 1f);
 			EntityStats stats = getEntityStats(event.entityLiving);
 			stats.addGold(stack.stackSize);
 			if (!event.entityLiving.worldObj.isRemote) {
 				PacketDispatcher.sendPacketToPlayer(new EntityStatsSyncPacket(stats).makePacket(), (Player)event.entityLiving);
 			}
 			event.item.setDead();
 			event.setCanceled(true);
 		}
 	}
 	
 	/** Drops gold coins at given entity in 5 approx. equal portions. */
 	private static void scatterGoldAt(Entity entity, int goldAmount) {
 		int portion = MathHelper.ceiling_float_int((float)goldAmount / 5f);
 		while (goldAmount > 0) {
 			int curPortion = goldAmount > portion ? portion : goldAmount;
 			goldAmount -= curPortion;
 			entity.dropItem(Config.goldCoin.getID(), curPortion);
 		}
 	}
 }
