 package com.censoredsoftware.demigods.engine.structure;
 
 import com.censoredsoftware.censoredlib.data.location.CLocation;
 import com.censoredsoftware.censoredlib.data.location.Region;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.data.util.CLocations;
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.entity.Player;
 
 import java.util.*;
 
 public class StructureData implements ConfigurationSerializable
 {
 	private UUID id;
 	private String type;
 	private UUID referenceLocation;
 	private List<String> flags;
 	private String region;
 	private String design;
 	private Float life;
 	private Boolean active;
 	private UUID owner;
 	private List<String> members;
 
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
 		try
 		{
 			life = Float.valueOf(conf.getString("life"));
 		}
 		catch(Throwable ignored)
 		{}
 		if(conf.getString("active") != null) active = conf.getBoolean("active");
 		if(conf.getString("owner") != null) owner = UUID.fromString(conf.getString("owner"));
 		if(conf.isList("members")) members = conf.getStringList("members");
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
		if(life != null) map.put("life", life.toString());
 		if(active != null) map.put("active", active);
 		if(owner != null) map.put("owner", owner.toString());
 		if(members != null) map.put("members", members);
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
 
 	public void setLife(float life)
 	{
 		this.life = life;
 	}
 
 	public void damageBy(Player player)
 	{
 		if(!DataManager.hasKeyTemp(player.getName(), "damage")) DataManager.saveTemp(player.getName(), "damage", 1F);
 		float damage = (float) DataManager.getValueTemp(player.getName(), "damage");
 		player.sendMessage("Damage! :D" + damage);
 		damage++;
 		DataManager.saveTemp(player.getName(), "damage", damage);
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
 		addMember(id);
 	}
 
 	public void setMembers(List<String> members)
 	{
 		this.members = members;
 	}
 
 	public void addMember(UUID id)
 	{
 		members.add(id.toString());
 		save();
 	}
 
 	public void removeMember(UUID id)
 	{
 		members.remove(id.toString());
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
 
 	public Float getLife()
 	{
 		if(life == null) life = getType().getLife();
 		return life;
 	}
 
 	public UUID getOwner()
 	{
 		return this.owner;
 	}
 
 	public Boolean hasMembers()
 	{
 		return this.members != null && !members.isEmpty();
 	}
 
 	public Collection<UUID> getMembers()
 	{
 		return Collections2.transform(members, new Function<String, UUID>()
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
