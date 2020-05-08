 package com.censoredsoftware.Demigods.Engine.Block;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 
 import redis.clients.johm.*;
 
import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.DemigodsData;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedBlock;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedLocation;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedModelFactory;
 
 @Model
 public class Altar
 {
 	@Id
	private long id;
 	@Reference
 	@Indexed
 	private TrackedLocation center;
 	@Attribute
 	@Indexed
 	private boolean active;
 	@CollectionSet(of = TrackedBlock.class)
 	@Indexed
 	private Set<TrackedBlock> blocks;
 
 	void setCenter(TrackedLocation center)
 	{
		Demigods.message.broadcast("Altar " + getId() + " set to " + center.getId());
 		this.center = center;
 	}
 
 	/**
 	 * Sets the active status of this Altar to <code>option</code>.
 	 * 
 	 * @param option the option to set.
 	 */
 	public void setActive(boolean option)
 	{
 		this.active = option;
 	}
 
 	public static void save(Altar altar)
 	{
 		DemigodsData.jOhm.save(altar);
 	}
 
 	public void delete()
 	{
 		DemigodsData.jOhm.delete(Altar.class, getId());
 	}
 
 	public static Altar load(long id) // TODO This belongs somewhere else.
 	{
 		return DemigodsData.jOhm.get(Altar.class, id);
 	}
 
 	public static Set<Altar> loadAll()
 	{
 		return DemigodsData.jOhm.getAll(Altar.class);
 	}
 
 	/**
 	 * Removes the Altar completely.
 	 */
 	public void remove()
 	{
 		for(TrackedBlock block : this.blocks)
 		{
 			block.remove();
 		}
 		delete();
 	}
 
	public long getId()
 	{
 		return this.id;
 	}
 
 	/**
 	 * Returns the location of the Altar.
 	 * 
 	 * @return Location
 	 */
 	public Location getLocation()
 	{
 		return this.center.toLocation();
 	}
 
 	/**
 	 * Returns true if the Altar is marked as active.
 	 * 
 	 * @return boolean
 	 */
 	public boolean isActive()
 	{
 		return active;
 	}
 
 	/**
 	 * Returns true if the <code>location</code> matches a location within the Altar.
 	 * 
 	 * @param location the location to check.
 	 * @return boolean
 	 */
 	public boolean locationMatches(Location location)
 	{
 		for(TrackedBlock block : this.blocks)
 		{
 			if(block.getLocation().equals(location)) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Generates a full Altar structure.
 	 */
 	void generate()
 	{
 		Set<TrackedBlock> blocks = new HashSet<TrackedBlock>();
 		Location location = getLocation();
 
 		// Remove the emerald block
 		location.getBlock().setTypeId(0);
 
 		// Split the location so we can build off of it
 		double locX = location.getX();
 		double locY = location.getY();
 		double locZ = location.getZ();
 		World locWorld = location.getWorld();
 
 		// Create the enchantment table
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 2, locZ), "altar", Material.ENCHANTMENT_TABLE));
 
 		// Create magical table stand
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 1, locZ), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 4, locZ + 2), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 4, locZ - 2), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 4, locZ - 2), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 4, locZ + 2), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 5, locZ + 2), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 5, locZ - 2), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 5, locZ - 2), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 5, locZ + 2), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 6, locZ), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 1, locY + 5, locZ - 1), "altar", Material.getMaterial(5), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 1, locY + 5, locZ), "altar", Material.getMaterial(5), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 1, locY + 5, locZ + 1), "altar", Material.getMaterial(5), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 1, locY + 5, locZ), "altar", Material.getMaterial(5), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 1, locY + 5, locZ + 1), "altar", Material.getMaterial(5), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 1, locY + 5, locZ - 1), "altar", Material.getMaterial(5), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 5, locZ), "altar", Material.getMaterial(5), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 5, locZ - 1), "altar", Material.getMaterial(5), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 5, locZ + 1), "altar", Material.getMaterial(5), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 3, locY, locZ + 3), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 3, locY, locZ - 3), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 3, locY, locZ - 3), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 3, locY, locZ + 3), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 3, locZ + 2), "altar", Material.getMaterial(44), (byte) 13));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 3, locZ - 2), "altar", Material.getMaterial(44), (byte) 13));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 3, locZ - 2), "altar", Material.getMaterial(44), (byte) 13));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 3, locZ + 2), "altar", Material.getMaterial(44), (byte) 13));
 
 		// Left beam
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 1, locY + 4, locZ - 2), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 4, locZ - 2), "altar", Material.getMaterial(98), (byte) 3));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 1, locY + 4, locZ - 2), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 1, locY + 5, locZ - 2), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 5, locZ - 2), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 1, locY + 5, locZ - 2), "altar", Material.getMaterial(126), (byte) 1));
 
 		// Right beam
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 1, locY + 4, locZ + 2), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 4, locZ + 2), "altar", Material.getMaterial(98), (byte) 3));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 1, locY + 4, locZ + 2), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 1, locY + 5, locZ + 2), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 5, locZ + 2), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 1, locY + 5, locZ + 2), "altar", Material.getMaterial(126), (byte) 1));
 
 		// Top beam
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 4, locZ + 1), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 4, locZ), "altar", Material.getMaterial(98), (byte) 3));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 4, locZ - 1), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 5, locZ + 1), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 5, locZ), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + 2, locY + 5, locZ - 1), "altar", Material.getMaterial(126), (byte) 1));
 
 		// Bottom beam
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 4, locZ + 1), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 4, locZ), "altar", Material.getMaterial(98), (byte) 3));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 4, locZ - 1), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 5, locZ + 1), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 5, locZ), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - 2, locY + 5, locZ - 1), "altar", Material.getMaterial(126), (byte) 1));
 
 		// Set locations to use for building
 		Location topLeft = new Location(locWorld, locX + 2, locY + 1, locZ - 2);
 		Location topRight = new Location(locWorld, locX + 2, locY + 1, locZ + 2);
 		Location botLeft = new Location(locWorld, locX - 2, locY + 1, locZ - 2);
 		Location botRight = new Location(locWorld, locX - 2, locY + 1, locZ + 2);
 
 		// Top left of platform
 		blocks.add(TrackedModelFactory.createTrackedBlock(topLeft, "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topLeft.subtract(1, 0, 0), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topLeft.add(0, 0, 1), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topLeft.add(1, 0, 0), "altar", Material.getMaterial(44), (byte) 5));
 
 		// Top right of platform
 		blocks.add(TrackedModelFactory.createTrackedBlock(topRight, "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topRight.subtract(1, 0, 0), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topRight.subtract(0, 0, 1), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topRight.add(1, 0, 0), "altar", Material.getMaterial(44), (byte) 5));
 
 		// Bottom left of platform
 		blocks.add(TrackedModelFactory.createTrackedBlock(botLeft, "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botLeft.add(1, 0, 0), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botLeft.add(0, 0, 1), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botLeft.subtract(1, 0, 0), "altar", Material.getMaterial(44), (byte) 5));
 
 		// Bottom right of platform
 		blocks.add(TrackedModelFactory.createTrackedBlock(botRight, "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botRight.subtract(0, 0, 1), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botRight.add(1, 0, 0), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botRight.add(0, 0, 1), "altar", Material.getMaterial(44), (byte) 5));
 
 		// Create central structure of platform
 		for(int i = 1; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 1, locZ + i), "altar", Material.getMaterial(44), (byte) 5));
 		for(int i = 1; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX, locY + 1, locZ - i), "altar", Material.getMaterial(44), (byte) 5));
 		for(int i = 1; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX - i, locY + 1, locZ), "altar", Material.getMaterial(44), (byte) 5));
 		for(int i = 1; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(new Location(locWorld, locX + i, locY + 1, locZ), "altar", Material.getMaterial(44), (byte) 5));
 
 		// Build steps on all sides.
 		Location leftSteps = new Location(locWorld, locX + 2, locY, locZ - 4);
 		Location rightSteps = new Location(locWorld, locX + 2, locY, locZ + 4);
 		Location topSteps = new Location(locWorld, locX + 4, locY, locZ - 2);
 		Location botSteps = new Location(locWorld, locX - 4, locY, locZ - 2);
 
 		// Create left steps
 		blocks.add(TrackedModelFactory.createTrackedBlock(leftSteps, "altar", Material.getMaterial(44), (byte) 5));
 		for(int i = 1; i < 5; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(leftSteps.subtract(1, 0, 0), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(leftSteps.add(0, 0, 1), "altar", Material.getMaterial(98)));
 		for(int i = 1; i < 5; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(leftSteps.add(1, 0, 0), "altar", Material.getMaterial(98)));
 
 		// Create right steps
 		blocks.add(TrackedModelFactory.createTrackedBlock(rightSteps, "altar", Material.getMaterial(44), (byte) 5));
 		for(int i = 1; i < 5; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(rightSteps.subtract(1, 0, 0), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(rightSteps.subtract(0, 0, 1), "altar", Material.getMaterial(98)));
 		for(int i = 1; i < 5; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(rightSteps.add(1, 0, 0), "altar", Material.getMaterial(98)));
 
 		// Create top steps
 		blocks.add(TrackedModelFactory.createTrackedBlock(topSteps, "altar", Material.getMaterial(44), (byte) 5));
 		for(int i = 1; i < 5; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(topSteps.add(0, 0, 1), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topSteps.subtract(1, 0, 0), "altar", Material.getMaterial(98)));
 		for(int i = 1; i < 5; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(topSteps.subtract(0, 0, 1), "altar", Material.getMaterial(98)));
 
 		// Create bottom steps
 		blocks.add(TrackedModelFactory.createTrackedBlock(botSteps, "altar", Material.getMaterial(44), (byte) 5));
 		for(int i = 1; i < 5; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(botSteps.add(0, 0, 1), "altar", Material.getMaterial(44), (byte) 5));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botSteps.add(1, 0, 0), "altar", Material.getMaterial(98)));
 		for(int i = 1; i < 5; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(botSteps.subtract(0, 0, 1), "altar", Material.getMaterial(98)));
 
 		// Create left step towers
 		for(int i = 0; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(leftSteps.add(0, 1, 0), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(leftSteps.add(0, 1, 0), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(leftSteps.subtract(4, 0, 0), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(leftSteps, "altar", Material.getMaterial(126), (byte) 1));
 		for(int i = 0; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(leftSteps.subtract(0, 1, 0), "altar", Material.getMaterial(98)));
 
 		// Create right step towers
 		for(int i = 0; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(rightSteps.add(0, 1, 0), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(rightSteps.add(0, 1, 0), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(rightSteps.subtract(4, 0, 0), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(rightSteps, "altar", Material.getMaterial(126), (byte) 1));
 		for(int i = 0; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(rightSteps.subtract(0, 1, 0), "altar", Material.getMaterial(98)));
 
 		// Create top step towers
 		for(int i = 0; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(topSteps.add(0, 1, 0), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topSteps.add(0, 1, 0), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topSteps.add(0, 0, 4), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(topSteps, "altar", Material.getMaterial(126), (byte) 1));
 		for(int i = 0; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(topSteps.subtract(0, 1, 0), "altar", Material.getMaterial(98)));
 
 		// Create bottom step towers
 		for(int i = 0; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(botSteps.add(0, 1, 0), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botSteps.add(0, 1, 0), "altar", Material.getMaterial(126), (byte) 1));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botSteps.add(0, 0, 4), "altar", Material.getMaterial(98)));
 		blocks.add(TrackedModelFactory.createTrackedBlock(botSteps, "altar", Material.getMaterial(126), (byte) 1));
 		for(int i = 0; i < 3; i++)
 			blocks.add(TrackedModelFactory.createTrackedBlock(botSteps.subtract(0, 1, 0), "altar", Material.getMaterial(98)));
 
 		this.blocks = blocks;
 	}
 
 	@Override
 	public boolean equals(Object object)
 	{
 		return !(object == null || !(object instanceof Altar)) && getId() == parse(object).getId();
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Altar{id=" + getId() + ",active=" + isActive() + ",center=" + getLocation().getWorld().getName() + "," + getLocation().getX() + "," + getLocation().getY() + "," + getLocation().getZ() + "}";
 	}
 
 	/**
 	 * Parses the save object into a new Altar object and returns it.
 	 * 
 	 * @param object the save to parse.
 	 * @return Altar
 	 */
 	public static Altar parse(Object object)
 	{
 		if(object instanceof Altar) return (Altar) object;
 		else if(object instanceof String)
 		{
 			// Cast the object into a string
 			String string = (String) object;
 
 			// Validate that it's an Altar save
 			if(!string.startsWith("Altar{id=")) return null;
 
 			// Begin splitting the string into the different variables to parse with
 			string = string.substring(9).replace("}", "");
 			String[] data = string.split(",");
 
 			// Parse the location
 			String[] locs = data[2].substring(7).split(",");
 			Location location = new Location(Bukkit.getWorld(locs[0]), Integer.parseInt(locs[1]), Integer.parseInt(locs[2]), Integer.parseInt(locs[3]));
 
 			// Build the object
 			Altar altar = BlockFactory.createAltar(location);
 			altar.setActive(Boolean.parseBoolean(data[1].substring(7)));
 
 			// Return the new Altar
 			return altar;
 		}
 
 		return null;
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 }
