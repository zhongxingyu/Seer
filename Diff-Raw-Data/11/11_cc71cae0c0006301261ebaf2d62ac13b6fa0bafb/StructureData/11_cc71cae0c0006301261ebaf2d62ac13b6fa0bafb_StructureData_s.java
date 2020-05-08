 package com.censoredsoftware.demigods.engine.structure;
 
 import com.censoredsoftware.censoredlib.data.location.CLocation;
 import com.censoredsoftware.censoredlib.data.location.Region;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.data.util.CLocations;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 import java.util.*;
 
 public class StructureData implements ConfigurationSerializable
 {
 	private UUID id;
 	private String type;
 	private UUID referenceLocation;
 	private List<String> flags;
 	private String region;
 	private String design;
 	private Float corruption, sanctity;
 	private Boolean active;
 	private UUID owner;
 	private Map<String, Long> corruptors, sanctifiers;
 
 	public StructureData()
 	{}
 
 	public StructureData(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		type = conf.getString("type");
 		referenceLocation = UUID.fromString(conf.getString("referenceLocation"));
 		flags = conf.getStringList("flags");
 		region = conf.getString("region");
 		design = conf.getString("design");
 
 		if(conf.contains("corruption"))
 		{
 			try
 			{
 				corruption = Float.valueOf(conf.getString("corruption"));
 			}
 			catch(Throwable ignored)
 			{}
 		}
 		if(conf.contains("sanctity"))
 		{
 			try
 			{
 				sanctity = Float.valueOf(conf.getString("sanctity"));
 			}
 			catch(Throwable ignored)
 			{}
 		}
 		if(conf.getString("active") != null) active = conf.getBoolean("active");
 		if(conf.getString("owner") != null) owner = UUID.fromString(conf.getString("owner"));
         if(conf.isConfigurationSection("corruptors"))
         {
 			corruptors = Maps.newHashMap(Maps.transformValues(conf.getConfigurationSection("corruptors").getValues(false), new Function<Object, Long>()
 			{
 				@Override
 				public Long apply(Object o)
 				{
 					try
 					{
 						return Long.parseLong(o.toString());
 					}
 					catch(Throwable ignored)
 					{}
 					return null;
 				}
 			}));
 		}
 		if(conf.isConfigurationSection("sanctifiers"))
 		{
 			sanctifiers = Maps.newHashMap(Maps.transformValues(conf.getConfigurationSection("sanctifiers").getValues(false), new Function<Object, Long>()
 			{
 				@Override
 				public Long apply(Object o)
 				{
 					try
 					{
 						return Long.parseLong(o.toString());
 					}
 					catch(Throwable ignored)
 					{}
 					return null;
 				}
 			}));
 		}
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("type", type);
 		map.put("referenceLocation", referenceLocation.toString());
 		map.put("flags", flags);
 		map.put("region", region);
 		map.put("design", design);
 		if(sanctity != null) map.put("sanctity", sanctity.toString());
 		if(active != null) map.put("active", active);
 		if(owner != null) map.put("owner", owner.toString());
         if(corruptors != null && !corruptors .isEmpty()) map.put("corruptors", corruptors);
 		if(sanctifiers != null && !sanctifiers.isEmpty()) map.put("sanctifiers", sanctifiers);
 		return map;
 	}
 
 	public void generateId()
 	{
 		id = UUID.randomUUID();
 	}
 
 	public void setType(String type)
 	{
 		this.type = type;
 	}
 
 	public void setSanctity(float sanctity)
 	{
 		this.sanctity = sanctity;
 	}
 
 	public void corrupt(DCharacter character, float amount)
 	{
 		if(getType().corrupt(this, character))
 		{
 			addCorruptor(character.getId());
 			corrupt(amount);
 			if(getCorruption() >= getSanctity() && getType().kill(this, character)) kill(character);
         }
 	}
 
     public void corrupt(float amount)
     {
         if(corruption == null || corruption < 0F) corruption = 0F;
 		corruption = corruption + amount;
 		save();
 	}
 
 	public void kill(DCharacter character)
 	{
 		if(getType().kill(this, character)) remove();
		remove();
 	}
 
 	public void setDesign(String name)
 	{
 		this.design = name;
 	}
 
 	public void setReferenceLocation(Location reference)
 	{
 		CLocation dLocation = CLocations.create(reference);
 		this.referenceLocation = dLocation.getId();
 		setRegion(dLocation.getRegion());
 	}
 
 	public void setOwner(UUID id)
 	{
 		this.owner = id;
 		addSanctifier(id);
 	}
 
 	public void sanctify(DCharacter character, float amount)
 	{
 		if(getType().sanctify(this, character))
         {
 			addSanctifier(character.getId());
 			sanctify(amount);
         }
     }
 
     public void sanctify(float amount)
     {
         if(sanctity == null || sanctity < 0F) sanctity = 0F;
         sanctity = sanctity + amount;
         save();
     }
 
     public void setCorruptors(Map<String, Long> corruptors)
     {
         this.corruptors = corruptors;
     }
 
     public void addCorruptor(UUID id)
 	{
 		if(corruptors == null) corruptors = Maps.newHashMap();
 		corruptors.put(id.toString(), System.currentTimeMillis());
 		save();
 	}
 
 	public void removeCorruptor(UUID id)
 	{
 		corruptors.remove(id.toString());
 	}
 
 	public void setSanctifiers(Map<String, Long> sanctifiers)
 	{
 		this.sanctifiers = sanctifiers;
 	}
 
 	public void addSanctifier(UUID id)
 	{
 		if(sanctifiers == null) sanctifiers = Maps.newHashMap();
 		sanctifiers.put(id.toString(), System.currentTimeMillis());
 		save();
 	}
 
 	public void removeSanctifier(UUID id)
 	{
 		sanctifiers.remove(id.toString());
 	}
 
 	public void setActive(Boolean bool)
 	{
 		this.active = bool;
 	}
 
 	public Location getReferenceLocation()
 	{
 		return CLocations.load(referenceLocation).toLocation();
 	}
 
 	public Set<Location> getClickableBlocks()
 	{
 		return getType().getDesign(design).getClickableBlocks(getReferenceLocation());
 	}
 
 	public Set<Location> getLocations()
 	{
 		return getType().getDesign(design).getSchematic().getLocations(getReferenceLocation());
 	}
 
 	public Structure getType()
 	{
 		for(Structure structure : Demigods.mythos().getStructures())
 			if(structure.getName().equalsIgnoreCase(this.type)) return structure;
 		return null;
 	}
 
 	public Boolean hasOwner()
 	{
 		return this.owner != null;
 	}
 
     public Float getCorruption()
     {
        if(corruption == null || corruption <= 0F) corruption = getType().getDefSanctity();
         return corruption;
     }
 
 	public Float getSanctity()
 	{
 		if(sanctity == null || sanctity <= 0F) sanctity = getType().getDefSanctity();
 		return sanctity;
 	}
 
 	public UUID getOwner()
 	{
 		return this.owner;
 	}
 
 	public Boolean hasMembers()
 	{
 		return this.sanctifiers != null && !sanctifiers.isEmpty();
 	}
 
     public Collection<UUID> getCorruptors()
     {
         return Collections2.transform(corruptors.keySet(), new Function<String, UUID>()
 		{
 			@Override
 			public UUID apply(String s)
 			{
 				return UUID.fromString(s);
 			}
 		});
 	}
 
 	public Collection<UUID> getSanctifiers()
 	{
 		return Collections2.transform(sanctifiers.keySet(), new Function<String, UUID>()
 		{
 			@Override
 			public UUID apply(String s)
 			{
 				return UUID.fromString(s);
 			}
 		});
 	}
 
 	public String getTypeName()
 	{
 		return type;
 	}
 
 	public Boolean getActive()
 	{
 		return this.active;
 	}
 
 	private void setRegion(Region region)
 	{
 		this.region = region.toString();
 	}
 
 	public String getRegion()
 	{
 		return region;
 	}
 
 	public void addFlags(Set<Structure.Flag> flags)
 	{
 		for(Structure.Flag flag : flags)
 			getRawFlags().add(flag.name());
 	}
 
 	public List<String> getRawFlags()
 	{
 		if(this.flags == null) flags = Lists.newArrayList();
 		return this.flags;
 	}
 
 	public UUID getId()
 	{
 		return this.id;
 	}
 
 	public void generate()
 	{
 		getType().getDesign(design).getSchematic().generate(getReferenceLocation());
 	}
 
 	public void save()
 	{
 		DataManager.structures.put(getId(), this);
 	}
 
 	public void remove()
 	{
 		for(Location location : getLocations())
 			location.getBlock().setType(Material.AIR);
 		CLocations.delete(referenceLocation);
 		Util.remove(id);
 	}
 
 	@Override
 	public String toString()
 	{
 		return Objects.toStringHelper(this).add("id", this.id).toString();
 	}
 
 	@Override
 	public int hashCode()
 	{
 		return Objects.hashCode(id);
 	}
 
 	@Override
 	public boolean equals(Object other)
 	{
 		return other != null && other instanceof StructureData && ((StructureData) other).getId() == getId();
 	}
 
 	public static class Util
 	{
 		public static void remove(UUID id)
 		{
 			DataManager.structures.remove(id);
 		}
 
 		public static StructureData load(UUID id)
 		{
 			return DataManager.structures.get(id);
 		}
 
 		public static Collection<StructureData> loadAll()
 		{
 			return DataManager.structures.values();
 		}
 
 		public static Collection<StructureData> findAll(Predicate<StructureData> predicate)
 		{
 			return Collections2.filter(DataManager.structures.values(), predicate);
 		}
 	}
 }
