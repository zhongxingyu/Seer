 package com.censoredsoftware.Demigods.Engine.Object;
 
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.*;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.google.common.collect.Sets;
 
 @Model
 public class Pet implements Battle.Participant
 {
 	@Id
 	private Long Id;
 	@Attribute
 	@Indexed
 	private String entityType;
 	@Attribute
 	@Indexed
 	private String animalTamer;
 	@Attribute
 	@Indexed
 	private Boolean PvP;
 	@Attribute
 	@Indexed
 	private String UUID;
 	@Reference
 	@Indexed
 	private DCharacter owner;
 
 	public void save()
 	{
 		JOhm.save(this);
 	}
 
 	public void remove()
 	{
 		getEntity().remove();
 		delete();
 	}
 
 	public void delete()
 	{
 		JOhm.delete(Pet.class, this.Id);
 	}
 
 	public void setTamable(LivingEntity tameable)
 	{
 		if(!(tameable instanceof Tameable)) throw new IllegalArgumentException("LivingEntity not tamable.");
 		this.entityType = tameable.getType().getName();
 		this.UUID = tameable.getUniqueId().toString();
 	}
 
 	public void setOwner(DCharacter owner)
 	{
 		this.animalTamer = owner.getName();
 		this.owner = owner;
 		save();
 	}
 
 	public void setCanPvp(boolean PvP)
 	{
 		this.PvP = PvP;
 		save();
 	}
 
	public Boolean canPvp()
 	{
 		return this.PvP;
 	}
 
 	public LivingEntity getNearbyLivingEntity(Player player)
 	{
 		int searchRadius = Demigods.config.getSettingInt("caps.target_range");
 		for(Entity pet : player.getNearbyEntities(searchRadius, searchRadius, searchRadius))
 		{
 			if(!(pet instanceof LivingEntity) || !(pet instanceof Tameable)) continue;
 			if(pet.getUniqueId().toString().equals(this.UUID)) return (LivingEntity) pet;
 		}
 		return null;
 	}
 
 	public LivingEntity getEntity()
 	{
 		for(World world : Bukkit.getServer().getWorlds())
 		{
 			for(Entity pet : world.getLivingEntities())
 			{
 				if(!(pet instanceof Tameable)) continue;
 				if(pet.getUniqueId().toString().equals(this.UUID)) return (LivingEntity) pet;
 			}
 		}
 		return null;
 	}
 
 	public DCharacter getOwner()
 	{
 		if(this.owner == null)
 		{
 			disownPet();
 			delete();
 			return null;
 		}
 		else if(!this.owner.canUse()) return null;
 		return this.owner;
 	}
 
 	public Deity getDeity()
 	{
 		if(this.owner == null)
 		{
 			disownPet();
 			delete();
 			return null;
 		}
 		else if(!this.owner.canUse()) return null;
 		return this.owner.getDeity();
 	}
 
 	@Override
 	public Long getId()
 	{
 		return this.Id;
 	}
 
 	@Override
 	public Location getCurrentLocation()
 	{
 		return getEntity().getLocation();
 	}
 
 	@Override
 	public DCharacter getRelatedCharacter()
 	{
 		return getOwner();
 	}
 
 	public void disownPet()
 	{
 		if(this.getEntity() == null) return;
 		((Tameable) this.getEntity()).setOwner(new AnimalTamer()
 		{
 			@Override
 			public String getName()
 			{
 				return "Disowned";
 			}
 		});
 	}
 
 	public static class Util
 	{
 		public static Pet create(LivingEntity tameable, DCharacter owner)
 		{
 			if(!(tameable instanceof Tameable)) throw new IllegalArgumentException("LivingEntity not tamable.");
 			Pet wrapper = new Pet();
 			wrapper.setTamable(tameable);
 			wrapper.setOwner(owner);
 			wrapper.save();
 			return wrapper;
 		}
 
 		public static Pet load(Long id)
 		{
 			return JOhm.get(Pet.class, id);
 		}
 
 		public static List<Pet> findByType(EntityType type)
 		{
 			return JOhm.find(Pet.class, "entityType", type.getName());
 		}
 
 		public static List<Pet> findByTamer(String animalTamer)
 		{
 			return JOhm.find(Pet.class, "animalTamer", animalTamer);
 		}
 
 		public static List<Pet> findByUUID(java.util.UUID uniqueId)
 		{
 			return JOhm.find(Pet.class, "UUID", uniqueId.toString());
 		}
 
 		public static Set<Pet> loadAll()
 		{
 			try
 			{
 				return JOhm.getAll(Pet.class);
 			}
 			catch(Exception e)
 			{
 				return Sets.newHashSet();
 			}
 		}
 
 		public static Pet getTameable(LivingEntity tameable)
 		{
 			if(!(tameable instanceof Tameable)) throw new IllegalArgumentException("LivingEntity not tamable.");
 			try
 			{
 				return findByUUID(tameable.getUniqueId()).get(0);
 			}
 			catch(Exception ignored)
 			{}
 			return null;
 		}
 
 		public static void disownPets(String animalTamer)
 		{
 			for(Pet wrapper : findByTamer(animalTamer))
 			{
 				if(wrapper.getEntity() == null) continue;
 				((Tameable) wrapper.getEntity()).setOwner(new AnimalTamer()
 				{
 					@Override
 					public String getName()
 					{
 						return "Disowned";
 					}
 				});
 			}
 		}
 
 		public static void reownPets(AnimalTamer tamer, DCharacter character)
 		{
 			for(Pet wrapper : findByTamer(character.getName()))
 			{
 				((Tameable) wrapper.getEntity()).setOwner(tamer);
 			}
 		}
 	}
 }
