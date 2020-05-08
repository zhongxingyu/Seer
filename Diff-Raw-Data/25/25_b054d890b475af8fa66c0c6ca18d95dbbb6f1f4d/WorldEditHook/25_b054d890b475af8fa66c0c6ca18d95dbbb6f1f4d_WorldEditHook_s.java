 package com.wolvencraft.prison.hooks;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalWorld;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.regions.Region;
 import com.sk89q.worldedit.Vector;
 import com.wolvencraft.prison.PrisonSuite;
 
 public class WorldEditHook {
 	/**
 	 * Checks if WorldEdit exists and is running
 	 * @return true if WorldEdit is usable, false otherwise
 	 */
 	public static boolean usingWorldEdit() {
 		return (PrisonSuite.getWorldEditPlugin() != null && PrisonSuite.getWorldEditPlugin().isEnabled());
 	}
 	
 	/**
 	 * Returns the item ID of the wand item used by the WorldEdit
 	 * @return Item ID of the wand item
 	 */
 	public static int getWandItem() {
 		return PrisonSuite.getWorldEditPlugin().getLocalConfiguration().wandItem;
 	}
 	
 	/**
 	 * Returns both selection points as an array for the specified player
 	 * @param player Player who performed the selection
 	 * @return An array of selection points or null if there is no selection
 	 */
 	public static Location[] getPoints(Player player) {
 		WorldEditPlugin we = PrisonSuite.getWorldEditPlugin();
 		Region sel = null;
		try { sel = we.getSession(player).getSelection((LocalWorld) player.getWorld()); }
 		catch (IncompleteRegionException ire) { return null; }
 		Location[] loc = {null, null};
 		if(sel == null) return null;
		loc[0] = toLocation((World) sel.getWorld(), sel.getMinimumPoint());
		loc[1] = toLocation((World) sel.getWorld(), sel.getMaximumPoint());
 		return loc;
 	}
 	
 	/**
 	 * Returns the maximum point of the selection
 	 * @param player Player who performed the selection
 	 * @return A selection point or null if there isn't one
 	 */
 	public static Location getMaximumPoint(Player player) {
 		WorldEditPlugin we = PrisonSuite.getWorldEditPlugin();
 		Region sel = null;
		try { sel = we.getSession(player).getSelection((LocalWorld) player.getWorld()); }
 		catch (IncompleteRegionException ire) { return null; }
 		if(sel == null) return null;
		return toLocation((World) sel.getWorld(), sel.getMaximumPoint());
 	}
 
 	/**
 	 * Returns the minimum point of the selection
 	 * @param player Player who performed the selection
 	 * @return A selection point or null if there isn't one
 	 */
 	public static Location getMinimumPoint(Player player) {
 		WorldEditPlugin we = PrisonSuite.getWorldEditPlugin();
 		Region sel = null;
		try { sel = we.getSession(player).getSelection((LocalWorld) player.getWorld()); }
 		catch (IncompleteRegionException ire) { return null; }
 		if(sel == null) return null;
		return toLocation((World) sel.getWorld(), sel.getMinimumPoint());
 	}
 	
 	private static Location toLocation(World world, Vector vector) {
 		return new Location(world, vector.getX(), vector.getY(), vector.getZ());
 	}
 }
