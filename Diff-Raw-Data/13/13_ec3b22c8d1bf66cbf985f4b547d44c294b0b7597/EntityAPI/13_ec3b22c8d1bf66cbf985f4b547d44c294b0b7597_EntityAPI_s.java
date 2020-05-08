 package btwmods;
 
 import java.util.List;
 
 import net.minecraft.src.DamageSource;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EnumCreatureType;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Packet5PlayerInventory;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldServer;
 import btwmods.entity.EntityEvent;
 import btwmods.entity.IEntityListener;
 import btwmods.entity.ISpawnLivingListener;
 import btwmods.entity.SpawnLivingEvent;
 import btwmods.events.EventDispatcher;
 import btwmods.events.EventDispatcherFactory;
 import btwmods.events.IAPIListener;
 
 public class EntityAPI {
 	private static EventDispatcher listeners = EventDispatcherFactory.create(new Class[] {
 		IEntityListener.class, ISpawnLivingListener.class
 	});
 
 	private EntityAPI() {}
 	
 	public static void addListener(IAPIListener listener) {
 		listeners.addListener(listener);
 	}
 
 	public static void removeListener(IAPIListener listener) {
 		listeners.removeListener(listener);
 	}
 
 	public static boolean onCheckEntityIsInvulnerable(Entity entity) {
 		if (!listeners.isEmpty(IEntityListener.class)) {
 			EntityEvent event = EntityEvent.CheckIsEntityInvulnerable(entity);
 			((IEntityListener)listeners).onEntityAction(event);
 			
 			if (event.isInvulnerable())
 				return true;
 		}
 		
 		return false;
 	}
 
 	public static boolean onTrampleFarmlandAttempt(int blockX, int blockY, int blockZ, Entity entity, float distanceFallen) {
 		EntityEvent event = EntityEvent.TrampleFarmlandAttempt(blockX, blockY, blockZ, entity, distanceFallen);
 		((IEntityListener)listeners).onEntityAction(event);
 		return event.isAllowed();
 	}
 	
 	public static void sendEntityEquipmentUpdate(EntityLiving entity) {
 		if (entity.worldObj instanceof WorldServer) {
 			WorldServer world = (WorldServer)entity.worldObj;
 			for (int slot = 0; slot < 5; ++slot) {
 				ItemStack item = entity.getEquipmentInSlot(slot);
 				world.getEntityTracker().sendPacketToTrackedPlayers(entity, new Packet5PlayerInventory(entity.entityId, slot, item));
 			}
 		}
 	}
 	
 	public static void onMobsSpawned(World world, EnumCreatureType creatureType, int validChunks, int oldEntityCount, List<EntityLiving> entities) {
 		SpawnLivingEvent event = new SpawnLivingEvent(world, creatureType, validChunks, oldEntityCount, entities);
 		((ISpawnLivingListener)listeners).onSpawnLivingAction(event);
 	}
 
 	public static void onEntityDamaged(EntityLiving entityLiving, DamageSource damageSource) {
		if (damageSource.getEntity() instanceof EntityLiving) {
			
			if (damageSource.getEntity() instanceof EntityPlayer)
				btwmods.PlayerAPI.onAttackedByPlayer(entityLiving, (EntityPlayer)damageSource.getEntity());
			
			EntityEvent event = EntityEvent.Attacked(entityLiving, damageSource);
			((IEntityListener)listeners).onEntityAction(event);
		}
 	}
 }
