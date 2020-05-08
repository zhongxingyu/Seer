 package com.censoredsoftware.demigods.player;
 
 import com.censoredsoftware.demigods.battle.Participant;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.deity.Deity;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.*;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 public class Pet implements Participant, ConfigurationSerializable
 {
 	private UUID id;
 	private String entityType;
 	private String animalTamer;
 	private boolean PvP, tameable;
 	private UUID entityUUID;
 	private UUID owner;
 
 	public Pet()
 	{}
 
 	public Pet(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		entityType = conf.getString("entityType");
 		if(conf.getString("animalTamer") != null) animalTamer = conf.getString("animalTamer");
 		PvP = conf.getBoolean("PvP");
 		tameable = conf.getBoolean("tameable");
 		entityUUID = UUID.fromString(conf.getString("entityUUID"));
 		if(conf.getString("owner") != null) owner = UUID.fromString(conf.getString("owner"));
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("entityType", entityType);
 		if(animalTamer != null) map.put("animalTamer", animalTamer);
 		map.put("PvP", PvP);
 		map.put("tamable", tameable);
 		map.put("entityUUID", entityUUID.toString());
 		if(owner != null) map.put("owner", owner.toString());
 		return map;
 	}
 
 	public void generateId()
 	{
 		id = UUID.randomUUID();
 	}
 
 	public void remove()
 	{
 		getEntity().remove();
 		delete();
 	}
 
 	public void delete()
 	{
 		DataManager.pets.remove(getId());
 	}
 
 	public void setPet(LivingEntity tameable)
 	{
 		this.entityType = tameable.getType().getName();
 		this.entityUUID = tameable.getUniqueId();
 	}
 
 	public void setOwner(DCharacter owner)
 	{
 		this.animalTamer = owner.getName();
 		this.owner = owner.getId();
 		Util.save(this);
 	}
 
 	public void setCanPvp(boolean PvP)
 	{
 		this.PvP = PvP;
 		Util.save(this);
 	}
 
 	public void setTameable(boolean tameable)
 	{
 		this.tameable = tameable;
 	}
 
 	public boolean canPvp()
 	{
 		return this.PvP;
 	}
 
 	public boolean isTameable()
 	{
 		return this.tameable;
 	}
 
 	public String getEntityType()
 	{
 		return entityType;
 	}
 
 	public String getAnimalTamer()
 	{
 		return animalTamer;
 	}
 
 	public UUID getEntityUUID()
 	{
 		return entityUUID;
 	}
 
 	public LivingEntity getEntity()
 	{
 		for(World world : Bukkit.getServer().getWorlds())
 		{
 			for(Entity pet : world.getLivingEntities())
 			{
 				if(!(pet instanceof LivingEntity)) continue;
 				if(pet.getUniqueId().equals(this.entityUUID)) return (LivingEntity) pet;
 			}
 		}
 		delete();
 		return null;
 	}
 
 	public DCharacter getOwner()
 	{
 		DCharacter owner = DCharacter.Util.load(this.owner);
 		if(owner == null)
 		{
 			disownPet();
 			delete();
 			return null;
 		}
 		else if(!owner.isUsable()) return null;
 		return owner;
 	}
 
 	public Deity getDeity()
 	{
 		if(getOwner() == null)
 		{
 			disownPet();
 			delete();
 			return null;
 		}
 		else if(!getOwner().isUsable()) return null;
 		return getOwner().getDeity();
 	}
 
 	@Override
 	public UUID getId()
 	{
 		return this.id;
 	}
 
 	@Override
 	public Location getCurrentLocation()
 	{
 		try
 		{
 			return getEntity().getLocation();
 		}
 		catch(Exception ignored)
 		{}
 		return null;
 	}
 
 	@Override
 	public DCharacter getRelatedCharacter()
 	{
 		return getOwner();
 	}
 
 	public void disownPet()
 	{
 		LivingEntity entity = this.getEntity();
 		if(entity == null || !(entity instanceof Tameable)) return;
 		((Tameable) entity).setOwner(new AnimalTamer()
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
 		public static Pet load(UUID id)
 		{
 			return DataManager.pets.get(id);
 		}
 
 		public static void save(Pet pet)
 		{
 			DataManager.pets.put(pet.getId(), pet);
 		}
 
 		public static Pet create(LivingEntity pet, DCharacter owner)
 		{
 			if(owner == null) throw new IllegalArgumentException("Owner cannot be null.");
 			Pet wrapper = new Pet();
 			wrapper.generateId();
 			wrapper.setPet(pet);
 			wrapper.setTameable(pet instanceof Tameable);
 			wrapper.setOwner(owner);
 			save(wrapper);
 			return wrapper;
 		}
 
 		public static Collection<Pet> findByType(final EntityType type)
 		{
 			return Collections2.filter(DataManager.pets.values(), new Predicate<Pet>()
 			{
 				@Override
 				public boolean apply(Pet pet)
 				{
 					return pet.getEntityType().equals(type.getName());
 				}
 			});
 		}
 
 		public static Collection<Pet> findByTamer(final String animalTamer)
 		{
 			return Collections2.filter(DataManager.pets.values(), new Predicate<Pet>()
 			{
 				@Override
 				public boolean apply(Pet pet)
 				{
 					return pet.isTameable() && pet.getAnimalTamer().equals(animalTamer);
 				}
 			});
 		}
 
 		public static Collection<Pet> findByUUID(final UUID uniqueId)
 		{
 			return Collections2.filter(DataManager.pets.values(), new Predicate<Pet>()
 			{
 				@Override
 				public boolean apply(Pet pet)
 				{
 					return pet.getEntityUUID().equals(uniqueId);
 				}
 			});
 		}
 
 		public static Collection<Pet> findByOwner(final UUID ownerId)
 		{
 			return Collections2.filter(DataManager.pets.values(), new Predicate<Pet>()
 			{
 				@Override
 				public boolean apply(Pet pet)
 				{
 					return pet.getOwner().getId().equals(ownerId);
 				}
 			});
 		}
 
 		public static Pet getPet(LivingEntity tameable)
 		{
 			return Iterables.getFirst(findByUUID(tameable.getUniqueId()), null);
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
 				if(wrapper.getEntity() != null) ((Tameable) wrapper.getEntity()).setOwner(tamer);
 		}
 	}
 }
