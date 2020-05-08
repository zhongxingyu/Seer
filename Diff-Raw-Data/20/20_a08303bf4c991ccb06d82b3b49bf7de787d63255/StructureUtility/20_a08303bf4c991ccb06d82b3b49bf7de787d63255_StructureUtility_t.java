 package com.censoredsoftware.Demigods.Engine.Utility;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureInfo;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureSave;
 import com.censoredsoftware.Demigods.Engine.Object.Structure.StructureSchematic;
 
 public class StructureUtility
 {
 	public static StructureSave getStructure(Location location)
 	{
 		for(StructureSave structureSave : StructureSave.loadAll())
 		{
 			if(!structureSave.getReferenceLocation().getWorld().equals(location.getWorld())) continue;
 			if(structureSave.getLocations().contains(location)) return structureSave;
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
 
 	public static boolean partOfStructureWithFlag(Location location, StructureInfo.Flag flag)
 	{
 		for(StructureInfo info : getStructureInfoFromFlag(flag))
 		{
 			for(StructureSave save : getStructuresByInfo(info))
 			{
 				if(!save.getReferenceLocation().getWorld().equals(location.getWorld())) continue;
 				if(save.getLocations().contains(location)) return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean isCenterBlockWithFlag(Location location, StructureInfo.Flag flag)
 	{
 		for(StructureInfo info : getStructureInfoFromFlag(flag))
 		{
 			for(StructureSave save : getStructuresByInfo(info))
 			{
 				if(!save.getReferenceLocation().getWorld().equals(location.getWorld())) continue;
 				if(save.getClickableBlock().equals(location)) return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean isInRadiusWithFlag(Location location, StructureInfo.Flag flag)
 	{
 		return getInRadiusWithFlag(location, flag) != null;
 	}
 
 	public static StructureSave getInRadiusWithFlag(Location location, StructureInfo.Flag flag)
 	{
 		for(StructureInfo info : getStructureInfoFromFlag(flag))
 		{
 			for(StructureSave save : getStructuresByInfo(info))
 			{
 				if(!save.getReferenceLocation().getWorld().equals(location.getWorld())) continue;
 				if(save.getReferenceLocation().distance(location) <= save.getStructureInfo().getRadius()) return save;
 			}
 		}
 		return null;
 	}
 
 	public static boolean isTrespassingInNoGriefingZone(Player player)
 	{
 		Location location = player.getLocation();
 		if(ZoneUtility.zoneNoBuild(player, player.getLocation())) return true;
 		if(isInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE))
 		{
 			StructureSave save = getInRadiusWithFlag(location, StructureInfo.Flag.NO_GRIEFING_ZONE);
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
 
 	public static Set<StructureInfo> getStructureInfoFromFlag(final StructureInfo.Flag flag)
 	{
 		return new HashSet<StructureInfo>()
 		{
 			{
 				for(StructureInfo info : Demigods.getLoadedStructures())
 				{
 					if(info.getFlags().contains(flag)) add(info);
 				}
 			}
 		};
 	}
 
 	public static List<StructureSave> getStructuresByInfo(StructureInfo info)
 	{
		return StructureSave.findAll("structureType", info.getStructureType());
 	}
 
 	public static Set<Location> getLocations(final Location reference, final Set<StructureSchematic> schematics)
 	{
 		return new HashSet<Location>()
 		{
 			{
 				for(StructureSchematic schematic : schematics)
 				{
 					addAll(schematic.getBlockLocations(reference));
 				}
 			}
 		};
 	}
 }
