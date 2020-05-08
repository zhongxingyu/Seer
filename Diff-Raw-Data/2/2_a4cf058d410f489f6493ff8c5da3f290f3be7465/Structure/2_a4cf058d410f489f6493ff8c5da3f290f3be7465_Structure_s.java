 package com.censoredsoftware.demigods.engine.structure;
 
 import com.censoredsoftware.censoredlib.data.location.Region;
 import com.censoredsoftware.censoredlib.schematic.Schematic;
 import com.censoredsoftware.demigods.engine.player.DCharacter;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import org.bukkit.Location;
 import org.bukkit.event.Listener;
 
 import javax.annotation.Nullable;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 public interface Structure
 {
 	public String getName();
 
 	public Design getDesign(final String name);
 
 	public Set<Flag> getFlags();
 
 	public Listener getUniqueListener();
 
 	public boolean sanctify(StructureData data, DCharacter character);
 
 	public boolean corrupt(StructureData data, DCharacter character);
 
 	public boolean birth(StructureData data, DCharacter character);
 
 	public boolean kill(StructureData data, DCharacter character);
 
 	public float getDefSanctity();
 
 	public float getSanctityRegen();
 
 	public int getRadius();
 
 	public Collection<StructureData> getAll();
 
 	public StructureData createNew(Location reference, boolean generate);
 
 	public interface Design
 	{
 		public String getName();
 
 		public Set<Location> getClickableBlocks(Location reference);
 
 		public Schematic getSchematic();
 	}
 
 	public interface InteractFunction<T>
 	{
 		public T apply(@Nullable StructureData data, @Nullable DCharacter character);
 	}
 
 	public enum Flag
 	{
 		DELETE_WITH_OWNER, DESTRUCT_ON_BREAK, PROTECTED_BLOCKS, NO_GRIEFING, NO_PVP, PRAYER_LOCATION, TRIBUTE_LOCATION, NO_OVERLAP
 	}
 
 	public static class Util
 	{
 		public static StructureData getStructureRegional(final Location location)
 		{
 			try
 			{
 				return Iterables.find(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 				{
 					@Override
 					public boolean apply(StructureData save)
 					{
 						return save.getLocations().contains(location);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static StructureData getStructureGlobal(final Location location)
 		{
 			try
 			{
 				return Iterables.find(StructureData.Util.loadAll(), new Predicate<StructureData>()
 				{
 					@Override
 					public boolean apply(StructureData save)
 					{
 						return save.getLocations().contains(location);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static Set<StructureData> getStructuresInRegionalArea(Location location)
 		{
 			final Region center = Region.Util.getRegion(location);
 			Set<StructureData> set = new HashSet<StructureData>();
 			for(Region region : center.getSurroundingRegions())
 				set.addAll(getStructuresInSingleRegion(region));
 			return set;
 		}
 
 		public static Collection<StructureData> getStructuresInSingleRegion(final Region region)
 		{
 			return StructureData.Util.findAll(new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRegion().equals(region.toString());
 				}
 			});
 		}
 
 		public static boolean partOfStructureWithType(final Location location, final String type)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getTypeName().equals(type) && save.getLocations().contains(location);
 				}
 			});
 		}
 
 		public static boolean partOfStructureWithAllFlags(final Location location, final Flag... flags)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
					return save.getRawFlags() != null && save.getLocations().contains(location) || save.getRawFlags().containsAll(Collections2.transform(Sets.newHashSet(flags), new Function<Flag, String>()
 					{
 						@Override
 						public String apply(Flag flag)
 						{
 							return flag.name();
 						}
 					}));
 				}
 			});
 		}
 
 		public static boolean partOfStructureWithFlag(final Location location, final Flag... flags)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					if(save.getRawFlags() == null || !save.getLocations().contains(location)) return false;
 					for(Flag flag : flags)
 						if(save.getRawFlags().contains(flag.name())) return true;
 					return false;
 				}
 			});
 		}
 
 		public static boolean partOfStructureWithFlag(final Location location, final Flag flag)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getLocations().contains(location);
 				}
 			});
 		}
 
 		public static boolean isClickableBlockWithFlag(final Location location, final Flag flag)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getClickableBlocks().contains(location);
 				}
 			});
 		}
 
 		public static boolean isInRadiusWithFlag(Location location, Flag flag)
 		{
 			return !getInRadiusWithFlag(location, flag).isEmpty();
 		}
 
 		public static Collection<StructureData> getInRadiusWithFlag(final Location location, final Flag... flags)
 		{
 			try
 			{
 				return Collections2.filter(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 				{
 					@Override
 					public boolean apply(StructureData save)
 					{
 						if(save.getRawFlags() == null || !save.getLocations().contains(location) || !save.getReferenceLocation().getWorld().equals(location.getWorld()) || save.getReferenceLocation().distance(location) <= save.getType().getRadius()) return false;
 						for(Flag flag : flags)
 							if(save.getRawFlags().contains(flag.name())) return true;
 						return false;
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static Collection<StructureData> getInRadiusWithFlag(final Location location, final Flag flag)
 		{
 			try
 			{
 				return Collections2.filter(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 				{
 					@Override
 					public boolean apply(StructureData save)
 					{
 						return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getReferenceLocation().getWorld().equals(location.getWorld()) && save.getReferenceLocation().distance(location) <= save.getType().getRadius();
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			return null;
 		}
 
 		public static Set<StructureData> getInRadiusWithFlag(final Location location, final Flag flag, final int radius)
 		{
 			return Sets.filter(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name()) && save.getReferenceLocation().getWorld().equals(location.getWorld()) && save.getReferenceLocation().distance(location) <= radius;
 				}
 			});
 		}
 
 		public static void regenerateStructures()
 		{
 			for(StructureData save : StructureData.Util.loadAll())
 				save.generate();
 		}
 
 		public static Collection<StructureData> getStructureWithFlag(final Flag flag)
 		{
 			return StructureData.Util.findAll(new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags() != null && save.getRawFlags().contains(flag.name());
 				}
 			});
 		}
 
 		public static boolean noOverlapStructureNearby(Location location)
 		{
 			return Iterables.any(getStructuresInRegionalArea(location), new Predicate<StructureData>()
 			{
 				@Override
 				public boolean apply(StructureData save)
 				{
 					return save.getRawFlags().contains(Flag.NO_OVERLAP.name());
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
 
 		/**
 		 * Updates favor for all structures.
 		 */
 		public static void updateSanctity()
 		{
 			for(StructureData data : getStructureWithFlag(Flag.DESTRUCT_ON_BREAK))
 				data.corrupt(-1F * data.getType().getSanctityRegen());
 		}
 	}
 }
