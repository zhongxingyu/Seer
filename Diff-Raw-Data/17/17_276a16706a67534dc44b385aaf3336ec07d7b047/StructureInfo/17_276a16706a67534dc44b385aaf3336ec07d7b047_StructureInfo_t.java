 package com.censoredsoftware.Demigods.Engine.Object.Structure;
 
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.event.Listener;
 
 public interface StructureInfo
 {
 	public String getStructureType();
 
 	public Set<StructureSchematic> getSchematics();
 
 	public int getRadius();
 
 	public Location getClickableBlock(Location reference);
 
 	public Listener getUniqueListener();
 
 	public StructureSave getAll();
 
 	public Set<Flag> getFlags();
 
 	public void createNew(Location reference, boolean generate);
 
 	public static enum Flag
 	{
		PROTECTED_BLOCKS, NO_PVP_ZONE, NO_GRIEFING_ZONE, TRIBUTE_LOCATION, PRAYER_LOCATION
 	}
 }
