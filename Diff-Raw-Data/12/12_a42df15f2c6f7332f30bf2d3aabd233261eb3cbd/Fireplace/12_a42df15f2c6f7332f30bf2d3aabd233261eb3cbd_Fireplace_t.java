 package com.maienm.FlooNetwork;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.material.Sign;
 
 public class Fireplace
 {
 	/**
 	 * The list of materials of which a fireplace may be made.
 	 */
 	private static final Set<Material> ACCEPTED_MATERIALS = new HashSet<Material>(Arrays.asList(
 		new Material[] {Material.BRICK}
 	));
 
 	/**
 	 * The location of the fireplace.
 	 *
 	 * This is the location of the bottom left block.
 	 */
 	private Location location = null;
 
 	/**
 	 * The x direction of the fireplace.
 	 */
 	private int xDirection = 0;
 
 	/**
 	 * The z direction of the fireplace.
 	 */
 	private int zDirection = 0;
 
 	/**
 	 * The owner of the fireplace.
 	 */
 	public OfflinePlayer owner = null;
 
 	/**
 	 * The name of the fireplace.
 	 */
 	public String name = null;
 
 	/**
 	 * Check if a block is a valid fireplace block.
 	 */
 	private static boolean isValidMaterial(Location location)
 	{
 		return ACCEPTED_MATERIALS.contains(location.getBlock().getType());
 	}
 
 	/**
 	 * Check if a there is a fireplace starting from this block.
 	 */
 	private static Fireplace detectAt(Location location, int x, int z)
 	{
 		Location loc = location.clone();
 		if (isValidMaterial(loc) && 
 		    isValidMaterial(loc.add(0, 1, 0)) &&
 		    isValidMaterial(loc.add(0, 1, 0)) &&
 		    isValidMaterial(loc.add(x, 0, z)) &&
 		    isValidMaterial(loc.add(0, 1, 0)) &&
 		    isValidMaterial(loc.add(x, -1, z)) &&
 		    isValidMaterial(loc.add(0, 1, 0)) &&
 		    isValidMaterial(loc.add(x, -1, z)) &&
 		    isValidMaterial(loc.add(0, -1, 0)) &&
 		    isValidMaterial(loc.add(0, -1, 0)))
 		{
 			Fireplace fp = new Fireplace();
 			fp.location = location;
 			fp.xDirection = x;
 			fp.zDirection = z;
 			return fp;
 		}
 		return null;
 	}
 
 	/**
 	 * Detect a fireplace from a sign.
 	 * 
 	 * Returns a new Fireplace object if one could be found, or null if the block is not part of a fireplace.
 	 */
 	static Fireplace detect(Location location)
 	{
 		Fireplace fp = null;
 
 		// Get the sign data.
 		Block block;
 		Sign sign;
 		try
 		{
 			block = location.getBlock();
 			sign = (Sign) block.getState().getData();
 		}
 		catch (Exception e)
 		{
 			return null;
 		}
 
 		// Determine which block the sign was attached to.
 		Location attached = block.getRelative(sign.getAttachedFace()).getLocation();
 
 		// Detect the fireplace. This is based on the position of the sign.
 		switch (sign.getFacing())
 		{
 			case NORTH:
 				fp = detectAt(attached.clone().add(3, -1, 0), -1, 0);
 				break;
 
 			case EAST:
 				fp = detectAt(attached.clone().add(0, -1, 3), 0, -1);
 				break;
 
 			case SOUTH:
 				fp = detectAt(attached.clone().add(-3, -1, 0), 1, 0);
 				break;
 
 			case WEST:
 				fp = detectAt(attached.clone().add(0, -1, -3), 0, 1);
 				break;
 		}
 
 		return fp;
 	}
 
 	/**
 	 * Check if a given location is part of a fireplace.
 	 */
 	public boolean contains(Location location)
 	{
 		int x = this.location.getBlockX();
 		int y = this.location.getBlockY();
 		int z = this.location.getBlockZ();
 		int x1 = Math.min(x, x + 3 * xDirection);
 		int x2 = Math.max(x, x + 3 * xDirection);
 		int z1 = Math.min(z, z + 3 * zDirection);
 		int z2 = Math.max(z, z + 3 * zDirection);
 
 		int lx = location.getBlockX();
 		int ly = location.getBlockY();
 		int lz = location.getBlockZ();
 
 		return (x1 <= lx && lx <= x2 &&
 		        z1 <= lz && lz <= z2 &&
 			y >= ly && y <= ly + 3);
 	}
 
 	/**
 	 * Get the location of the sign belonging to this fireplace.
 	 */
 	public Location getSignLocation()
 	{
		return location.clone().add(xDirection * 3 - zDirection, 1, zDirection * 3 + xDirection);
 	}
 
 	/**
 	 * Print important information about a fireplace.
 	 */
 	public String toString()
 	{
		return String.format("Fireplace %s by %s, spanning from %d, %d, %d to %d, %d, %d (xDirection = %d, zDirection = %d)", 
 			name, owner != null ? owner.getName() : "Unknown",
 			location.getBlockX(), location.getBlockY(), location.getBlockZ(), 
			location.getBlockX() + xDirection * 3, location.getBlockY(), location.getBlockZ() + zDirection * 3,
			xDirection, zDirection);
 	}
 }
