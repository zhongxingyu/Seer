 package no.runsafe.framework.internal.wrapper.entity;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.chunk.IChunk;
 import no.runsafe.framework.api.entity.IEntity;
 import no.runsafe.framework.api.minecraft.RunsafeEntityType;
 import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
 import no.runsafe.framework.internal.wrapper.ObjectWrapper;
 import no.runsafe.framework.internal.wrapper.metadata.BukkitMetadata;
 import no.runsafe.framework.minecraft.entity.EntityType;
 import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageByEntityEvent;
 import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageEvent;
 import org.bukkit.Location;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftHorse;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.util.Vector;
 
 import javax.annotation.Nullable;
 import java.util.List;
 import java.util.UUID;
 
 public abstract class BukkitEntity extends BukkitMetadata
 {
 	protected BukkitEntity(Entity toWrap)
 	{
 		super(toWrap);
 		entity = toWrap;
 	}
 
 	@Override
 	public Entity getRaw()
 	{
 		return entity;
 	}
 
 	@Nullable
 	public ILocation getLocation()
 	{
 		if (entity == null)
 			return null;
 		return ObjectWrapper.convert(entity.getLocation());
 	}
 
 	@Nullable
 	public IWorld getWorld()
 	{
 		if (entity == null)
 			return null;
 		return ObjectWrapper.convert(entity.getWorld());
 	}
 
 	public boolean teleport(ILocation location)
 	{
 		if (location == null)
 			return false;
 
 		IChunk targetChunk = location.getChunk();
 		if (targetChunk.isUnloaded())
 			targetChunk.load();
 
		dismountBeforeTeleport(location);
 		return entity.teleport((Location) ObjectUnwrapper.convert(location));
 	}
 
 	public boolean teleport(IEntity entity)
 	{
		dismountBeforeTeleport(entity.getLocation());
 		return this.entity.teleport((Entity) ObjectUnwrapper.convert(entity));
 	}
 
	private void dismountBeforeTeleport(ILocation location)
 	{
 		Entity vehicle = entity.getVehicle();
 		if (vehicle != null)
 		{
 			if (vehicle.getType() == org.bukkit.entity.EntityType.HORSE && getWorld().isWorld(location.getWorld()))
 			{
				vehicle.setPassenger(null);
				vehicle.teleport((Location) ObjectUnwrapper.convert(location));
 			}
 			else
 			{
 				vehicle.setPassenger(null);
 			}
 		}
 	}
 
 	public List<IEntity> getNearbyEntities(double x, double y, double z)
 	{
 		return ObjectWrapper.convert(entity.getNearbyEntities(x, y, z));
 	}
 
 	public int getEntityId()
 	{
 		return entity.getEntityId();
 	}
 
 	public int getFireTicks()
 	{
 		return entity.getFireTicks();
 	}
 
 	public int getMaxFireTicks()
 	{
 		return entity.getMaxFireTicks();
 	}
 
 	public void setFireTicks(int i)
 	{
 		entity.setFireTicks(i);
 	}
 
 	public void remove()
 	{
 		entity.remove();
 	}
 
 	public boolean isDead()
 	{
 		return entity.isDead();
 	}
 
 	@Nullable
 	public IEntity getPassenger()
 	{
 		if (entity == null)
 			return null;
 		return ObjectWrapper.convert(entity.getPassenger());
 	}
 
 	public boolean setPassenger(IEntity entity)
 	{
 		return this.entity.setPassenger((Entity) ObjectUnwrapper.convert(entity));
 	}
 
 	public boolean isEmpty()
 	{
 		return entity.isEmpty();
 	}
 
 	public boolean eject()
 	{
 		return entity.eject();
 	}
 
 	public float getFallDistance()
 	{
 		return entity.getFallDistance();
 	}
 
 	public void setFallDistance(float distance)
 	{
 		entity.setFallDistance(distance);
 	}
 
 	public void setLastDamageCause(RunsafeEntityDamageEvent entityDamageEvent)
 	{
 		entity.setLastDamageCause(entityDamageEvent.getRaw());
 	}
 
 	public RunsafeEntityDamageEvent getLastDamageCause()
 	{
 		EntityDamageEvent event = entity.getLastDamageCause();
 
 		if (event instanceof EntityDamageByEntityEvent)
 			return new RunsafeEntityDamageByEntityEvent((EntityDamageByEntityEvent) event);
 
 		return new RunsafeEntityDamageEvent(entity.getLastDamageCause());
 	}
 
 	public UUID getUniqueId()
 	{
 		return entity.getUniqueId();
 	}
 
 	public int getTicksLived()
 	{
 		return entity.getTicksLived();
 	}
 
 	public void setTicksLived(int ticks)
 	{
 		entity.setTicksLived(ticks);
 	}
 
 	public boolean isInsideVehicle()
 	{
 		return entity.isInsideVehicle();
 	}
 
 	public boolean leaveVehicle()
 	{
 		return entity.leaveVehicle();
 	}
 
 	public IEntity getVehicle()
 	{
 		return ObjectWrapper.convert(entity.getVehicle());
 	}
 
 	public RunsafeEntityType getEntityType()
 	{
 		return EntityType.convert(entity.getType());
 	}
 
 	public void setVelocity(Vector velocity)
 	{
 		entity.setVelocity(velocity);
 	}
 
 	protected final Entity entity;
 }
