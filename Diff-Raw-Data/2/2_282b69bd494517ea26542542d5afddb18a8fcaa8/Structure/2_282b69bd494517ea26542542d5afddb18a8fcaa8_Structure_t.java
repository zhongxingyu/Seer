 package com.censoredsoftware.Demigods.Engine.Object.Structure;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
 
 public abstract class Structure
 {
 	public abstract String getStructureType();
 
 	public abstract StructureSchematic get(String name);
 
 	public abstract int getRadius();
 
 	public abstract Location getClickableBlock(Location reference);
 
 	public abstract Listener getUniqueListener();
 
 	public abstract Set<StructureSave> getAll();
 
 	public abstract Set<Structure.Flag> getFlags();
 
 	public abstract StructureSave createNew(Location reference, boolean generate);
 
 	public enum Flag
 	{
 		PROTECTED_BLOCKS, NO_GRIEFING, NO_PVP, PRAYER_LOCATION, TRIBUTE_LOCATION;
 	}
 
 	public static StructureSave getStructure(Location location)
 	{
 		for(StructureSave save : StructureSave.loadAll())
 		{
 			if(!save.getReferenceLocation().getWorld().equals(location.getWorld())) continue;
 			if(save.getLocations().contains(location)) return save;
 		}
 		return null;
 	}
 
 	public static boolean partOfStructureWithType(Location location, String structureType)
 	{
 		for(StructureSave save : StructureSave.findAll("structureType", structureType))
 		{
 			if(!save.getReferenceLocation().getWorld().equals(location.getWorld())) continue;
 			if(save.getClickableBlock().equals(location)) return true;
 		}
 		return false;
 	}
 
 	public static boolean partOfStructureWithFlag(Location location, Structure.Flag flag)
 	{
 		for(StructureSave save : StructureSave.findAll("flags", flag.name()))
 		{
 			if(save.getLocations().contains(location)) return true;
 		}
 		return false;
 	}
 
 	public static boolean isReferenceBlockWithFlag(Location location, Structure.Flag flag)
 	{
 		for(StructureSave save : StructureSave.findAll("flags", flag.name()))
 		{
 			if(save.getLocations().contains(location)) return true;
 		}
 		return false;
 	}
 
 	public static boolean isClickableBlockWithFlag(Location location, Structure.Flag flag)
 	{
 		for(StructureSave save : StructureSave.findAll("flags", flag.name()))
 		{
			if(save.getClickableBlock().equals(location)) return true;
 		}
 		return false;
 	}
 
 	public static boolean isInRadiusWithFlag(Location location, Structure.Flag flag)
 	{
 		return getInRadiusWithFlag(location, flag) != null;
 	}
 
 	public static StructureSave getInRadiusWithFlag(Location location, Structure.Flag flag)
 	{
 		for(StructureSave save : StructureSave.findAll("flags", flag.name()))
 		{
 			if(save.getReferenceLocation().distance(location) <= save.getStructureInfo().getRadius()) return save;
 		}
 		return null;
 	}
 
 	public static boolean isTrespassingInNoGriefingZone(Player player)
 	{
 		Location location = player.getLocation();
 		if(ZoneUtility.zoneNoBuild(player, player.getLocation())) return true;
 		if(isInRadiusWithFlag(location, Flag.NO_GRIEFING))
 		{
 			StructureSave save = getInRadiusWithFlag(location, Flag.NO_GRIEFING);
 			if(save.getOwner() != null && save.getOwner().getId().equals(PlayerWrapper.getPlayer(player).getCurrent().getId())) return false;
 			return true;
 		}
 		return false;
 	}
 
 	public static void regenerateStructures()
 	{
 		for(StructureSave save : StructureSave.loadAll())
 		{
 			save.generate();
 		}
 	}
 
 	public static Set<Structure> getStructuresWithFlag(final Structure.Flag flag)
 	{
 		return new HashSet<Structure>()
 		{
 			{
 				for(Structure structure : Demigods.getLoadedStructures())
 				{
 					if(structure.getFlags().contains(flag)) add(structure);
 				}
 			}
 		};
 	}
 
 	public static List<StructureSave> getStructuresByInfo(Structure info)
 	{
 		return StructureSave.findAll("structureType", info.getStructureType());
 	}
 }
