 package hunternif.mc.dota2items.core;
 
 import hunternif.mc.dota2items.core.buff.BuffInstance;
 import hunternif.mc.dota2items.effect.ContinuousEffect;
 import hunternif.mc.dota2items.network.BuffPacket;
 import hunternif.mc.dota2items.network.EntityHurtPacket;
 import hunternif.mc.dota2items.util.NetworkUtil;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraftforge.common.IExtendedEntityProperties;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 
 public class StatsTracker implements IPlayerTracker {
 
 	private static final String EXT_PROP_STATS = "Dota2ItemsEntityStats";
 	
 	private Map<EntityLivingBase, EntityStats> clientEntityStats = new ConcurrentHashMap<EntityLivingBase, EntityStats>();
 	private Map<EntityLivingBase, EntityStats> serverEntityStats = new ConcurrentHashMap<EntityLivingBase, EntityStats>();
 	Map<EntityLivingBase, EntityStats> getEntityStatsMap(Side side) {
 		return side.isClient() ? clientEntityStats : serverEntityStats;
 	}
 	
 	/** Guaranteed to be non-null for non-null entities. */
 	public EntityStats getOrCreateEntityStats(EntityLivingBase entity) {
 		if (entity == null) return null;
 		Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(entity));
 		EntityStats stats = entityStats.get(entity);
 		if (stats == null) {
 			stats = new EntityStats(entity);
 			entityStats.put(entity, stats);
 		} else if (stats.entity != entity) {
 			// Happens when the entity is copied, e.g. when changing dimension.
 			stats.entity = entity;
 		}
 		return stats;
 	}
 	
 	/** Can be null. Accepts Entity, although stats only exist for EntityLivingBase. */
 	public EntityStats getEntityStats(Entity entity) {
 		if (entity == null) return null;
 		Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(entity));
 		EntityStats stats = entityStats.get(entity);
 		if (stats != null && stats.entity != entity) {
 			// Happens when the entity is copied, e.g. when changing dimension.
 			stats.entity = (EntityLivingBase)entity;
 		}
 		return stats;
 	}
 	
	@ForgeSubscribe
 	public void onLivingDeath(LivingDeathEvent event) {
 		if (!event.isCanceled() && !(event.entityLiving instanceof EntityPlayer)) {
 			Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(event.entityLiving));
 			entityStats.remove(event.entityLiving);
 		}
 	}
 	
 	private static Side getSide(Entity entity) {
 		return entity.worldObj.isRemote ? Side.CLIENT : Side.SERVER;
 	}
 	
 	@ForgeSubscribe
 	public void onEntityConstructing(EntityConstructing event) {
 		if (event.entity instanceof EntityPlayer && !event.entity.worldObj.isRemote) {
 			event.entity.registerExtendedProperties(EXT_PROP_STATS, getOrCreateEntityStats((EntityLivingBase)event.entity));
 		}
 	}
 	
 	public void removeBuffsOnHurt(EntityLivingBase entity) {
 		boolean shouldSync = false;
 		EntityStats stats = getOrCreateEntityStats(entity);
 		for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 			if (buffInst.buff.isRemovedOnHurt) {
 				stats.removeBuff(buffInst);
 				shouldSync = true;
 			}
 		}
 		if (!entity.worldObj.isRemote && shouldSync) {
 			NetworkUtil.sendToAllAround(new EntityHurtPacket(entity).makePacket(), entity);
 		}
 	}
 	
 	@Override
 	public void onPlayerLogin(EntityPlayer player) {
 		EntityStats stats = getOrCreateEntityStats(player);
 		for (BuffInstance buffInst : stats.getAppliedBuffs()) {
 			if (!buffInst.isItemPassiveBuff()) {
 				PacketDispatcher.sendPacketToAllPlayers(new BuffPacket(buffInst).makePacket());
 			}
 			if (ContinuousEffect.buffHasEffect(buffInst.buff)) {
 				ContinuousEffect effect = ContinuousEffect.construct(buffInst.buff, player);
 				player.worldObj.spawnEntityInWorld(effect);
 			}
 		}
 		stats.sendSyncPacketToClient(player);
 	}
 
 	@Override
 	public void onPlayerLogout(EntityPlayer player) {}
 
 	@Override
 	public void onPlayerChangedDimension(EntityPlayer player) {}
 
 	/**
 	 * Upon respawn the EntityPlayer is constructed anew, however, with a wrong
 	 * entityID at the moment of EntityConstructing event dispatch. That entityID
 	 * is changed later on, but the ExtendedProperties have already been written
 	 * and cannot be removed. So let us manually copy the required values into
 	 * the existing ExtendedProperties.  
 	 */
 	@Override
 	public void onPlayerRespawn(EntityPlayer player) {
 		IExtendedEntityProperties props = (player.getExtendedProperties(EXT_PROP_STATS));
 		if (props != null) {
 			EntityStats oldStats = getOrCreateEntityStats(player);
 			EntityStats newStats = (EntityStats)props;
 			newStats.entity = player;
 			newStats.setGold(oldStats.getReliableGold(), oldStats.getUnreliableGold());
 			Map<EntityLivingBase, EntityStats> entityStats = getEntityStatsMap(getSide(player));
 			entityStats.put(player, newStats);
 			newStats.setMana(newStats.getMaxMana());
 			
 			newStats.sendSyncPacketToClient(player);
 		}
 	}
 }
