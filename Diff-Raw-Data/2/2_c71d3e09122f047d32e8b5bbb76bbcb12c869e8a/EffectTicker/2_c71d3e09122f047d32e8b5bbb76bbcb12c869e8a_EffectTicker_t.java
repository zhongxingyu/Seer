 package xelitez.frostcraft.effect;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 import xelitez.frostcraft.damage.EntityDamageSourceFrost;
 import xelitez.frostcraft.damage.EntityDamageSourceIndirectFrost;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.util.DamageSource;
 import net.minecraft.world.World;
 
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class EffectTicker implements ITickHandler
 {
 	private static List<Effect> entities = new ArrayList<Effect>();
 	
 	private static EffectTicker instance = new EffectTicker();
 	
 	public static EffectTicker instance()
 	{
 		return instance;
 	}
 	
 	public static void addEffect(Entity entityHit, PotionEffect potionEffect, Object... obj) 
 	{
 		if(entityHit instanceof EntityLiving)
 		{
 			addEffect((EntityLiving)entityHit, potionEffect, obj);
 		}
 	}
 	
 	public boolean hasEntityEffect(EntityLiving entity, Potion potion)
 	{
 		for(int i = 0;i< EffectTicker.entities.size();i++)
 		{
 			Effect effect = entities.get(i);
 			if(effect.entity.entityId == entity.entityId)
 			{
 				if(potion.id == effect.potionId)
 				{
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public static void addEffect(EntityLiving entity, PotionEffect effect, Object... firingEntity)
 	{	
 		if(entity.worldObj.isRemote)
 		{
 			return;
 		}
 		entity.addPotionEffect(effect);
 		Entity fireentity = null;
 		if(firingEntity != null && firingEntity.length >= 2 && firingEntity[1] instanceof Entity)
 		{
 			fireentity = (Entity) firingEntity[1];
 		}
 		Entity source = null;
 		if(firingEntity != null && firingEntity.length >= 1 && firingEntity[0] instanceof Entity)
 		{
 			source = (Entity) firingEntity[0];
 		}
 		for(int i = 0;i < entities.size();i++)
 		{
 			Effect ff = (Effect)entities.get(i);
 			if(ff.entity.entityId == entity.entityId)
 			{
 				if(ff.potionId == effect.getPotionID())
 				{
 					if(fireentity != null && ff.firingentity != fireentity)
 					{
 						ff.firingentity = fireentity;
 					}
 					if(source != null && ff.source != source)
 					{
 						ff.source = source;
 					}
 					if(ff.level < effect.getAmplifier())
 					{
 						ff.level = effect.getAmplifier();
 						ff.duration = effect.getDuration();
 						return;
 					}
 					if(ff.duration < effect.getDuration())
 					{
 						ff.duration = effect.getDuration();
 					}
 					return;
 				}
 			}
 		}
 		Effect peffect = new Effect(entity, effect);
 		if(fireentity != null)
 		{
 			peffect = peffect.setEntity(fireentity);
 		}
 		if(source != null)
 		{
 			peffect = peffect.setSource(source);
 		}
 		EffectTicker.entities.add(peffect);
 	}
 	
 	public void removeEffects(int dim)
 	{
 		for(int i = 0;i < entities.size();i++)
 		{
 			Effect effect = (Effect)entities.get(i);
 			if((effect.dimension == dim && effect.duration <= 0) || effect.entity.isDead)
 			{
 				entities.remove(effect);
 				removeEffects(dim);
 				return;
 			}
 		}
 	}
 	
 	@Override
 	public void tickStart(EnumSet<TickType> type, Object... tickData) 
 	{
 		World world = (World)tickData[0];
 		for(int i = 0;i < entities.size();i++)
 		{
 			Effect effect = (Effect)entities.get(i);
 			if(world.provider.dimensionId == effect.dimension)
 			{
 				effect.doTick(world);
 			}
 		}
 		this.removeEffects(world.provider.dimensionId);
 	}
 
 	@Override
 	public void tickEnd(EnumSet<TickType> type, Object... tickData) 
 	{
 
 	}
 
 	@Override
 	public EnumSet<TickType> ticks() 
 	{
 		return EnumSet.of(TickType.WORLD);
 	}
 
 	@Override
 	public String getLabel() 
 	{
 		return "FrostCraftEffectTicker";
 	}
 
 	private static class Effect
 	{
 		public EntityLiving entity;
 		public Entity firingentity;
 		public Entity source;
 		public int potionId = -1;
 		public int duration = 0;
 		public int level = 0;
 		
 		public double posX = 0;
 		public double posY = 0;
 		public double posZ = 0;
 		
 		public int dimension;
 		
 		public int counter = 0;
 		
 		public Effect(EntityLiving entity, PotionEffect effect)
 		{
 			this.entity = entity;
 			this.potionId = effect.getPotionID();
 			this.duration = effect.getDuration();
 			this.level = effect.getAmplifier();
 			this.dimension = entity.worldObj.provider.dimensionId;
 			if(effect.getPotionID() == FCPotion.freeze.id)
 			{
 				this.posX = entity.posX;
 				this.posY = entity.posY;
 				this.posZ = entity.posZ;
 			}
 		}
 		
 		public Effect setEntity(Entity entity)
 		{
 			this.firingentity = entity;
 			return this;
 		}
 		
 		public Effect setSource(Entity entity)
 		{
 			this.source = entity;
 			return this;
 		}
 		
 		public void doTick(World world)
 		{
 			if(this.potionId == FCPotion.freeze.id)
 			{
 				if(entity != null && !entity.isDead && entity.getActivePotionEffect(FCPotion.freeze) != null);
 				{
 					if(entity == null)
 					{
 						duration--;
 						counter++;
 						return;
 					}
 					entity.setPositionAndUpdate(posX, posY, posZ);
 					entity.motionX = 0.0D;
 					entity.motionY = 0.0D;
 					entity.motionZ = 0.0D;
 				}
 			}
 			if(this.potionId == FCPotion.frostburn.id)
 			{
 				if(entity != null && !entity.isDead && entity.getActivePotionEffect(FCPotion.frostburn) != null);
 				{
 					if(entity == null)
 					{
 						duration--;
 						counter++;
 						return;
 					}
					if(counter == 10) 
 					{
 						int damage = this.level + 1;
 						DamageSource var1 = null;
 						if(this.firingentity != null)
 						{
 							var1 = new EntityDamageSourceIndirectFrost(source, firingentity);
 						}
 						else if(source != null)
 						{
 							var1 = new EntityDamageSourceFrost(source);
 						}
 						else
 						{
 							var1 = new EntityDamageSourceFrost(entity);
 						}					
 						entity.attackEntityFrom(var1 != null ? var1 : DamageSource.cactus, damage);
 					}
 					if(counter >= 20)
 					{
 						counter = 0;
 					}
 				}
 			}
 			duration--;
 			counter++;
 		}
 	}
 }
