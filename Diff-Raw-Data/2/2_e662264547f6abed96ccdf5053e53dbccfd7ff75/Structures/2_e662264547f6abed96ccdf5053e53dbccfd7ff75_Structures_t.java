 package com.censoredsoftware.demigods.util;
 
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.location.Region;
 import com.censoredsoftware.demigods.structure.Structure;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import org.bukkit.Location;
 
 import java.util.*;
 
 @SuppressWarnings("unchecked")
 public class Structures
 {
 	public static Structure.Save getStructureRegional(final Location location)
 	{
 		try
 		{
 			return Iterables.find(getStructuresInRegionalArea(location), new Predicate<Structure.Save>()
 			{
 				@Override
 				public boolean apply(Structure.Save save)
 				{
 					return save.getLocations().contains(location);
 				}
 			});
 		}
 		catch(NoSuchElementException ignored)
 		{}
 		return null;
 	}
 
 	public static Structure.Save getStructureSaveGlobal(final Location location)
 	{
 		try
 		{
 			return Iterables.find(loadAll(), new Predicate<Structure.Save>()
 			{
 				@Override
 				public boolean apply(Structure.Save save)
 				{
 					return save.getLocations().contains(location);
 				}
 			});
 		}
 		catch(NoSuchElementException ignored)
 		{}
 		return null;
 	}
 
 	public static Set<Structure.Save> getStructuresInRegionalArea(Location location)
 	{
 		final Region center = Region.Util.getRegion(location);
 		Set<Structure.Save> set = new HashSet<Structure.Save>();
 		for(Region region : center.getSurroundingRegions())
 			set.addAll(getStructuresInSingleRegion(region));
 		return set;
 	}
 
 	public static Collection<Structure.Save> getStructuresInSingleRegion(final Region region)
 	{
 		return findAll(new Predicate<Structure.Save>()
 		{
 			@Override
 			public boolean apply(Structure.Save save)
 			{
 				return save.getRegion().equals(region.toString());
 			}
 		});
 	}
 
 	public static boolean partOfStructureWithType(final Location location, final String type)
 	{
 		return Iterables.any(getStructuresInRegionalArea(location), new Predicate<Structure.Save>()
 		{
 			@Override
 			public boolean apply(Structure.Save save)
 			{
 				return save.getType().equals(type) && save.getLocations().contains(location);
 			}
 		});
 	}
 
 	public static boolean partOfStructureWithFlag(final Location location, final Structure.Flag flag)
 	{
 		return Iterables.any(getStructuresInRegionalArea(location), new Predicate<Structure.Save>()
 		{
 			@Override
 			public boolean apply(Structure.Save save)
 			{
 				return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getLocations().contains(location);
 			}
 		});
 	}
 
 	public static boolean isClickableBlockWithFlag(final Location location, final Structure.Flag flag)
 	{
 		return Iterables.any(getStructuresInRegionalArea(location), new Predicate<Structure.Save>()
 		{
 			@Override
 			public boolean apply(Structure.Save save)
 			{
 				return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getClickableBlocks().contains(location);
 			}
 		});
 	}
 
 	public static boolean isInRadiusWithFlag(Location location, Structure.Flag flag)
 	{
 		return getInRadiusWithFlag(location, flag) != null;
 	}
 
 	public static Structure.Save getInRadiusWithFlag(final Location location, final Structure.Flag flag)
 	{
 		try
 		{
 			return Iterables.find(getStructuresInRegionalArea(location), new Predicate<Structure.Save>()
 			{
 				@Override
 				public boolean apply(Structure.Save save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getReferenceLocation().getWorld().equals(location.getWorld()) && save.getReferenceLocation().distance(location) <= save.getStructure().getRadius();
 				}
 			});
 		}
		catch(NoSuchElementException ignored)
 		{}
 		return null;
 	}
 
 	public static void regenerateStructures()
 	{
 		for(Structure.Save save : loadAll())
 			save.generate(false);
 	}
 
 	public static Collection<Structure.Save> getStructuresSavesWithFlag(final Structure.Flag flag)
 	{
 		return findAll(new Predicate<Structure.Save>()
 		{
 			@Override
 			public boolean apply(Structure.Save save)
 			{
 				return save.getRawFlags() != null && save.getRawFlags().contains(flag.name());
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
 			if(!location.getBlock().getType().isTransparent()) return false;
 			location.add(0, 1, 1);
 			location.subtract(1, 0, 0);
 		}
 
 		return true;
 	}
 
 	public static void remove(UUID id)
 	{
 		DataManager.structures.remove(id);
 	}
 
 	public static Structure.Save load(UUID id)
 	{
 		return DataManager.structures.get(id);
 	}
 
 	public static Collection<Structure.Save> loadAll()
 	{
 		return DataManager.structures.values();
 	}
 
 	public static Collection<Structure.Save> findAll(Predicate<Structure.Save> predicate)
 	{
 		return Collections2.filter(DataManager.structures.values(), predicate);
 	}
 }
