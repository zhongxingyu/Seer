 package com.censoredsoftware.demigods.structure;
 
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.location.DLocation;
 import com.censoredsoftware.demigods.location.Region;
 import com.censoredsoftware.demigods.structure.deity.Obelisk;
 import com.censoredsoftware.demigods.structure.deity.Shrine;
 import com.censoredsoftware.demigods.structure.global.Altar;
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.event.Listener;
 
 import java.util.*;
 
 public class Structure implements ConfigurationSerializable
 {
 	private UUID id;
 	private String type;
 	private UUID referenceLocation;
 	private List<String> flags;
 	private String region;
 	private String design;
 	private Boolean active;
 	private UUID owner;
 	private List<String> members;
 
 	public Structure()
 	{}
 
 	public Structure(UUID id, ConfigurationSection conf)
 	{
 		this.id = id;
 		type = conf.getString("type");
 		referenceLocation = UUID.fromString(conf.getString("referenceLocation"));
 		flags = conf.getStringList("flags");
 		region = conf.getString("region");
 		design = conf.getString("design");
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
 
 	public void setDesign(String name)
 	{
 		this.design = name;
 	}
 
 	public void setReferenceLocation(Location reference)
 	{
 		DLocation dLocation = DLocation.Util.create(reference);
 		this.referenceLocation = dLocation.getId();
 		setRegion(dLocation.getRegion());
 	}
 
 	public void setOwner(UUID id)
 	{
 		this.owner = id;
 	}
 
 	void setMembers(List<String> members)
 	{
 		this.members = members;
 	}
 
 	public void addMember(UUID id)
 	{
 		members.add(id.toString());
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
 		return DLocation.Util.load(referenceLocation).toLocation();
 	}
 
 	public Set<Location> getClickableBlocks()
 	{
 		return getType().getDesign(design).getClickableBlocks(getReferenceLocation());
 	}
 
 	public Set<Location> getLocations()
 	{
 		return getType().getDesign(design).getSchematic().getLocations(getReferenceLocation());
 	}
 
 	public Type getType()
 	{
 		for(Type structure : Type.values())
 			if(structure.getName().equalsIgnoreCase(this.type)) return structure;
 		return null;
 	}
 
 	public Boolean hasOwner()
 	{
 		return this.owner != null;
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
 
 	public void setRegion(Region region)
 	{
 		this.region = region.toString();
 	}
 
 	public String getRegion()
 	{
 		return region;
 	}
 
 	public void addFlags(Set<Flag> flags)
 	{
 		for(Flag flag : flags)
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
 			location.getBlock().setTypeId(Material.AIR.getId());
 		DLocation.Util.delete(referenceLocation);
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
 		return other != null && other instanceof Structure && ((Structure) other).getId() == getId();
 	}
 
 	public enum Flag
 	{
 		DELETE_WITH_OWNER, PROTECTED_BLOCKS, NO_GRIEFING, NO_PVP, PRAYER_LOCATION, TRIBUTE_LOCATION, NO_OVERLAP
 	}
 
 	public enum Type
 	{
 		/**
 		 * General
 		 */
 		// Altar
 		ALTAR(Altar.name, Altar.designs, Altar.getDesign, Altar.createNew, Altar.flags, Altar.listener, Altar.radius),
 
 		// Obelisk
 		OBELISK(Obelisk.name, Obelisk.designs, Obelisk.getDesign, Obelisk.createNew, Obelisk.flags, Obelisk.listener, Obelisk.radius),
 
 		// Shrine
 		SHRINE(Shrine.name, Shrine.designs, Shrine.getDesign, Shrine.createNew, Shrine.flags, Shrine.listener, Shrine.radius);
 
 		private String name;
 		private Design[] designs;
 		private Function<Location, Design> getDesign;
 		private Function<Design, Structure> createNew;
 		private Set<Flag> flags;
 		private Listener listener;
 		private int radius;
 
 		private Type(String name, Design[] designs, Function<Location, Design> getDesign, Function<Design, Structure> createNew, Set<Flag> flags, Listener listener, int radius)
 		{
 			this.name = name;
 			this.designs = designs;
 			this.getDesign = getDesign;
 			this.createNew = createNew;
 			this.flags = flags;
 			this.listener = listener;
 			this.radius = radius;
 		}
 
 		public String getName()
 		{
 			return name;
 		}
 
 		public Design getDesign(final String name)
 		{
 			try
 			{
 				return Iterables.find(Sets.newHashSet(designs), new Predicate<Design>()
 				{
 					@Override
 					public boolean apply(Design design)
 					{
						return design.getName().equals(name);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public Set<Flag> getFlags()
 		{
 			return flags;
 		}
 
 		public Listener getUniqueListener()
 		{
 			return listener;
 		}
 
 		public int getRadius()
 		{
 			return radius;
 		}
 
 		public Collection<Structure> getAll()
 		{
 			return Collections2.filter(Util.loadAll(), new Predicate<Structure>()
 			{
 				@Override
 				public boolean apply(Structure save)
 				{
 					return save.getTypeName().equals(getName());
 				}
 			});
 		}
 
 		public Structure createNew(Location reference, boolean generate)
 		{
 			Design design = getDesign.apply(reference);
 			Structure save = createNew.apply(design);
 
 			// All structures need these
 			save.generateId();
 			save.setReferenceLocation(reference);
 			save.setType(getName());
 			save.setDesign(design.getName());
 			save.addFlags(getFlags());
 			save.setActive(true);
 			save.save();
 
 			if(generate) save.generate();
 			return save;
 		}
 
 		public interface Design
 		{
 			public String getName();
 
 			public Set<Location> getClickableBlocks(Location reference);
 
 			public Schematic getSchematic();
 		}
 	}
 
 	public static class Util
 	{
 		public static Structure getStructureRegional(final Location location)
 		{
 			try
 			{
 				return Iterables.find(getStructuresInRegionalArea(location), new Predicate<Structure>()
 				{
 					@Override
 					public boolean apply(Structure save)
 					{
 						return save.getLocations().contains(location);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static Structure getStructureGlobal(final Location location)
 		{
 			try
 			{
 				return Iterables.find(loadAll(), new Predicate<Structure>()
 				{
 					@Override
 					public boolean apply(Structure save)
 					{
 						return save.getLocations().contains(location);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static Set<Structure> getStructuresInRegionalArea(Location location)
 		{
 			final Region center = Region.Util.getRegion(location);
 			Set<Structure> set = new HashSet<Structure>();
 			for(Region region : center.getSurroundingRegions())
 				set.addAll(getStructuresInSingleRegion(region));
 			return set;
 		}
 
 		public static Collection<Structure> getStructuresInSingleRegion(final Region region)
 		{
 			return findAll(new Predicate<Structure>()
 			{
 				@Override
 				public boolean apply(Structure save)
 				{
 					return save.getRegion().equals(region.toString());
 				}
 			});
 		}
 
 		public static boolean partOfStructureWithType(final Location location, final String type)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<Structure>()
 			{
 				@Override
 				public boolean apply(Structure save)
 				{
 					return save.getTypeName().equals(type) && save.getLocations().contains(location);
 				}
 			});
 		}
 
 		public static boolean partOfStructureWithFlag(final Location location, final Structure.Flag flag)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<Structure>()
 			{
 				@Override
 				public boolean apply(Structure save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getLocations().contains(location);
 				}
 			});
 		}
 
 		public static boolean isClickableBlockWithFlag(final Location location, final Structure.Flag flag)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<Structure>()
 			{
 				@Override
 				public boolean apply(Structure save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getClickableBlocks().contains(location);
 				}
 			});
 		}
 
 		public static boolean isInRadiusWithFlag(Location location, Structure.Flag flag)
 		{
 			return getInRadiusWithFlag(location, flag) != null;
 		}
 
 		public static Structure getInRadiusWithFlag(final Location location, final Structure.Flag flag)
 		{
 			try
 			{
 				return Iterables.find(getStructuresInRegionalArea(location), new Predicate<Structure>()
 				{
 					@Override
 					public boolean apply(Structure save)
 					{
 						return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getReferenceLocation().getWorld().equals(location.getWorld()) && save.getReferenceLocation().distance(location) <= save.getType().getRadius();
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static void regenerateStructures()
 		{
 			for(Structure save : loadAll())
 				save.generate();
 		}
 
 		public static Collection<Structure> getStructureWithFlag(final Structure.Flag flag)
 		{
 			return findAll(new Predicate<Structure>()
 			{
 				@Override
 				public boolean apply(Structure save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name());
 				}
 			});
 		}
 
 		public static boolean noOverlapStructureNearby(Location location)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<Structure>()
 			{
 				@Override
 				public boolean apply(Structure save)
 				{
 					return save.getRawFlags().contains(Structure.Flag.NO_OVERLAP.name());
 				}
 			});
 		}
 
 		/**
 		 * Strictly checks the <code>reference</code> location to validate if the area is safe
 		 * for automated generation.
 		 * 
 		 * @param reference the location to be checked
 		 * @param area how big of an area (in blocks) to validate
 		 * @return Boolean
 		 */
 		public static boolean canGenerateStrict(Location reference, int area)
 		{
 			Location location = reference.clone();
 			location.subtract(0, 1, 0);
 			location.add((area / 3), 0, (area / 2));
 
 			// Check ground
 			for(int i = 0; i < area; i++)
 			{
 				if(!location.getBlock().getType().isSolid()) return false;
 				location.subtract(1, 0, 0);
 			}
 
 			// Check ground adjacent
 			for(int i = 0; i < area; i++)
 			{
 				if(!location.getBlock().getType().isSolid()) return false;
 				location.subtract(0, 0, 1);
 			}
 
 			// Check ground adjacent again
 			for(int i = 0; i < area; i++)
 			{
 				if(!location.getBlock().getType().isSolid()) return false;
 				location.add(1, 0, 0);
 			}
 
 			location.add(0, 1, 0);
 
 			// Check air diagonally
 			for(int i = 0; i < area + 1; i++)
 			{
 				if(location.getBlock().getType().isSolid()) return false;
 				location.add(0, 1, 1);
 				location.subtract(1, 0, 0);
 			}
 
 			return true;
 		}
 
 		public static void remove(UUID id)
 		{
 			DataManager.structures.remove(id);
 		}
 
 		public static Structure load(UUID id)
 		{
 			return DataManager.structures.get(id);
 		}
 
 		public static Collection<Structure> loadAll()
 		{
 			return DataManager.structures.values();
 		}
 
 		public static Collection<Structure> findAll(Predicate<Structure> predicate)
 		{
 			return Collections2.filter(DataManager.structures.values(), predicate);
 		}
 	}
 }
