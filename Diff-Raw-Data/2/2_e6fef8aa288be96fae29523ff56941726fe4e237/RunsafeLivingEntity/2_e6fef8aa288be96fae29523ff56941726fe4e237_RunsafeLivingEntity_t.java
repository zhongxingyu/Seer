 package no.runsafe.framework.server.entity;
 
 import no.runsafe.framework.server.ObjectWrapper;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.server.potion.RunsafePotionEffect;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Projectile;
 import org.bukkit.util.Vector;
 
 import java.util.HashSet;
 import java.util.List;
 
 public class RunsafeLivingEntity extends RunsafeEntity
 {
 	public RunsafeLivingEntity(LivingEntity toWrap)
 	{
 		super(toWrap);
 		this.entity = toWrap;
 	}
 
 	public LivingEntity getRaw()
 	{
 		return entity;
 	}
 
 	public int getHealth()
 	{
 		return entity.getHealth();
 	}
 
 	public void setHealth(int i)
 	{
 		entity.setHealth(i);
 	}
 
 	public int getMaxHealth()
 	{
 		return entity.getMaxHealth();
 	}
 
 	public double getEyeHeight()
 	{
 		return entity.getEyeHeight();
 	}
 
 	public double getEyeHeight(boolean b)
 	{
 		return entity.getEyeHeight(b);
 	}
 
 	public RunsafeLocation getEyeLocation()
 	{
 		return ObjectWrapper.convert(entity.getEyeLocation());
 	}
 
 	public RunsafeBlock getTarget()
 	{
 		HashSet<Byte> transparent = new HashSet<Byte>();
 		for (Material material : Material.values())
 			if (material.isTransparent())
 				transparent.add((byte) material.getId());
 		return getTargetBlock(transparent, 300);
 	}
 
 	public RunsafeEntity Fire(String projectileType)
 	{
 		return Fire(EntityType.fromName(projectileType).getEntityClass());
 	}
 
 	public RunsafeEntity Fire(ProjectileEntity projectileType)
 	{
 		return Fire(projectileType.getEntityType());
 	}
 
 	public RunsafeEntity Launch(String entityType)
 	{
 		return Launch(EntityType.fromName(entityType).getEntityClass());
 	}
 
 	public RunsafeEntity Launch(RunsafeEntityType entityType)
 	{
 		return Launch(entityType.getEntityType());
 	}
 
 	public List<RunsafeBlock> getLineOfSight(HashSet<Byte> transparent, int maxDistance)
 	{
 		return ObjectWrapper.convert(entity.getLineOfSight(transparent, maxDistance));
 	}
 
 	public RunsafeBlock getTargetBlock(HashSet<Byte> transparent, int maxDistance)
 	{
 		return ObjectWrapper.convert(entity.getTargetBlock(transparent, maxDistance));
 	}
 
 	public List<RunsafeBlock> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance)
 	{
 		return ObjectWrapper.convert(entity.getLastTwoTargetBlocks(transparent, maxDistance));
 	}
 
 	public int getRemainingAir()
 	{
 		return entity.getRemainingAir();
 	}
 
 	public void setRemainingAir(int i)
 	{
 		entity.setRemainingAir(i);
 	}
 
 	public int getMaximumAir()
 	{
 		return entity.getMaximumAir();
 	}
 
 	public void setMaximumAir(int i)
 	{
 		entity.setMaximumAir(i);
 	}
 
 	public void damage(int i)
 	{
 		entity.damage(i);
 	}
 
 	public void damage(int i, RunsafeEntity source)
 	{
 		entity.damage(i, source.getRaw());
 	}
 
 	public int getMaximumNoDamageTicks()
 	{
 		return entity.getMaximumNoDamageTicks();
 	}
 
 	public void setMaximumNoDamageTicks(int i)
 	{
 		entity.setMaximumNoDamageTicks(i);
 	}
 
 	public int getLastDamage()
 	{
 		return entity.getLastDamage();
 	}
 
 	public void setLastDamage(int i)
 	{
 		entity.setLastDamage(i);
 	}
 
 	public int getNoDamageTicks()
 	{
 		return entity.getNoDamageTicks();
 	}
 
 	public void setNoDamageTicks(int i)
 	{
 		entity.setNoDamageTicks(i);
 	}
 
 	public RunsafePlayer getKiller()
 	{
 		return ObjectWrapper.convert(entity.getKiller());
 	}
 
 	private RunsafeEntity Fire(Class<? extends Entity> projectile)
 	{
 		if (!Projectile.class.isAssignableFrom(projectile))
 			return null;
 		return ObjectWrapper.convert(entity.launchProjectile(projectile.asSubclass(Projectile.class)));
 	}
 
 	public boolean addPotionEffect(RunsafePotionEffect effect)
 	{
 		return this.entity.addPotionEffect(effect.getRaw());
 	}
 
 	public boolean addPotionEffect(RunsafePotionEffect effect, boolean force)
 	{
 		return this.entity.addPotionEffect(effect.getRaw(), force);
 	}
 
 	public void addPotionEffects(List<RunsafePotionEffect> effects)
 	{
 		for (RunsafePotionEffect effect : effects)
 			this.entity.addPotionEffect(effect.getRaw());
 	}
 
	public boolean hasPotionEffect(RunsafePotionEffect effect)
 	{
 		return this.entity.hasPotionEffect(effect.getRaw().getType());
 	}
 
 	public void removePotionEffect(RunsafePotionEffect effect)
 	{
 		this.entity.removePotionEffect(effect.getRaw().getType());
 	}
 
 	private RunsafeEntity Launch(Class<? extends Entity> launch)
 	{
 		Vector velocity = entity.getEyeLocation().getDirection().multiply(2);
 		Entity launched = entity.getWorld().spawn(entity.getEyeLocation().add(velocity), launch);
 		launched.setVelocity(velocity);
 		return ObjectWrapper.convert(launched);
 	}
 
 	private final LivingEntity entity;
 }
