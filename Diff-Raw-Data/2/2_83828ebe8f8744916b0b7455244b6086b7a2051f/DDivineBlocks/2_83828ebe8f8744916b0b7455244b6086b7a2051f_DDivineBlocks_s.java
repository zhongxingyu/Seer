 package com.legit2.Demigods;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 
 import com.legit2.Demigods.Database.DDatabase;
 import com.legit2.Demigods.Libraries.DivineBlock;
 import com.legit2.Demigods.Utilities.DCharUtil;
 import com.legit2.Demigods.Utilities.DDataUtil;
 import com.legit2.Demigods.Utilities.DObjUtil;
 
 public class DDivineBlocks
 {	
 	/* ---------------------------------------------------
 	 * Begin Shrine-related Methods
 	 * ---------------------------------------------------
 	 * 
 	 *  createShrine() : Creates a shrine at (Location)location.
 	 */
 	public static void createShrine(int charID, Location location)
 	{
 		int blockID = DObjUtil.generateInt(5);
 		DivineBlock block = new DivineBlock(location, blockID, charID, true, "shrine", DCharUtil.getDeity(charID));
 		DDataUtil.saveBlockData(blockID, "block_object", block);
 	}
 	
 	/*
 	 *  removeShrine() : Removes the shrine at (Location)location.
 	 */
 	public static void removeShrine(Location location)
 	{
 		location.getBlock().setType(Material.AIR);
 		removeDivineBlock(location);
 		Location locToMatch = location.add(0.5, 1.0, 0.5);
 		
 		for(Entity entity : location.getWorld().getEntities())
 		{			
 			if(entity.getLocation().equals(locToMatch))
 			{
 				entity.remove();
 			}
 		}
 	}
 
 	/*
 	 *  isShrine() : Returns true/false depending on if the block at (Location)location is an Altar or not.
 	 */
 	public static boolean isShrineBlock(Location location)
 	{
 		int blockID = getID(location);
 		if(blockID != -1)
 		{
 			if(getDivineBlockType(blockID).equalsIgnoreCase("shrine")) return true;
 		}
 		return false;
 	}
 	
 	/*
 	 *  getAllShrines() : Returns an ArrayList<Location> of (Player)player's Shrines.
 	 */
 	public static ArrayList<Location> getAllShrines()
 	{		
 		ArrayList<Location> shrines = new ArrayList<Location>();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{
 			if(divineBlock.getValue().get("block_object") == null) continue;
 			if(((DivineBlock) divineBlock.getValue().get("block_object")).getType().equalsIgnoreCase("shrine"))
 			{
 				Location blockLoc = ((DivineBlock) divineBlock.getValue().get("block_object")).getLocation();
 				shrines.add(blockLoc);
 			}
 		}
 		return shrines;
 	}
 	
 	/*
 	 *  getAllShrineBlocks() : Returns an ArrayList<DivineBlock> of (Player)player's Shrines.
 	 */
 	public static ArrayList<DivineBlock> getAllShrineBlocks()
 	{		
 		ArrayList<DivineBlock> shrines = new ArrayList<DivineBlock>();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{
 			if(divineBlock.getValue().get("block_object") == null) continue;
 			if(((DivineBlock) divineBlock.getValue().get("block_object")).getType().equalsIgnoreCase("shrine")) shrines.add((DivineBlock) divineBlock.getValue().get("block_object"));
 		}
 		return shrines;
 	}
 	
 	/*
 	 *  getCharShrines() : Returns an ArrayList<Location> of charID's shrines.
 	 */
 	public static ArrayList<Location> getCharShrines(int charID)
 	{
 		ArrayList<Location> shrines = new ArrayList<Location>();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{	
 			if(divineBlock.getValue().get("block_object") == null) continue;
 			if(((DivineBlock) divineBlock.getValue().get("block_object")).getParent() == charID)
 			{
 				Location blockLoc = ((DivineBlock) divineBlock.getValue().get("block_object")).getLocation();
 				shrines.add(blockLoc);
 			}
 		}
 		return shrines;
 	}
 	
 	/*
 	 *  getOwnerOfShrine() : Returns the owner of the shrine at (Location)location.
 	 */
 	public static int getShrineOwner(Location location)
 	{		
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{	
 			DivineBlock block = (DivineBlock) divineBlock.getValue().get("block_object");
 			if(block.getLocation().equals(location)) return block.getParent();
 		}
 		return -1;
 	}
 	
 	/*
 	 *  getDeityAtShrine() : Returns the deity of the shrine at (Location)location.
 	 */
 	public static String getShrineDeity(Location location)
 	{
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{			
 			DivineBlock block = (DivineBlock) divineBlock.getValue().get("block_object");
 			if(block.getLocation().equals(location)) return block.getDeity();
 		}
 		return null;
 	}
 	
 	/* ---------------------------------------------------
 	 * Begin Altar-related Methods
 	 * ---------------------------------------------------
 	 *
 	 *  createNewAltar() : Creates a new altar at (Location)location.
 	 */
 	public static void createAltar(Location location)
 	{
 		int parentID = createDivineParentBlock(location, 116, "all", "altar");
 		generateAltar(location, parentID);
 		DDatabase.saveDivineBlocks();
 	}
 	
 	/*
 	 *  createAltar() : Creates an altar at (Location)location for (int)parentID.
 	 */
 	public static void generateAltar(Location location, int parentID)
 	{	
 		location.subtract(0, 2, 0);
 		location.getBlock().setTypeId(0);
 
 		// Split the location so we can build off of it
 		double locX = location.getX();
 		double locY = location.getY();
 		double locZ = location.getZ();
 		World locWorld = location.getWorld();
 		
 		// Create magical table stand
 		createDivineBlock(new Location(locWorld, locX, locY + 1, locZ), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 4, locZ + 2), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 4, locZ - 2), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 4, locZ - 2), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 4, locZ + 2), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 5, locZ + 2), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 5, locZ - 2), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 5, locZ - 2), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 5, locZ + 2), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX, locY + 6, locZ), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX - 1, locY + 5, locZ - 1), parentID, 5, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX - 1, locY + 5, locZ), parentID, 5, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX - 1, locY + 5, locZ + 1), parentID, 5, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX + 1, locY + 5, locZ - 1), parentID, 5, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX + 1, locY + 5, locZ), parentID, 5, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX + 1, locY + 5, locZ + 1), parentID, 5, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX, locY + 5, locZ), parentID, 5, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX, locY + 5, locZ - 1), parentID, 5, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX, locY + 5, locZ + 1), parentID, 5, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX + 3, locY, locZ + 3), parentID, 44, (byte) 5);
 		createDivineBlock(new Location(locWorld, locX - 3, locY, locZ - 3), parentID, 44, (byte) 5);
 		createDivineBlock(new Location(locWorld, locX + 3, locY, locZ - 3), parentID, 44, (byte) 5);
 		createDivineBlock(new Location(locWorld, locX - 3, locY, locZ + 3), parentID, 44, (byte) 5);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 3, locZ + 2), parentID, 44, (byte) 13);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 3, locZ - 2), parentID, 44, (byte) 13);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 3, locZ - 2), parentID, 44, (byte) 13);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 3, locZ + 2), parentID, 44, (byte) 13);
 				
 		// Left beam
 		createDivineBlock(new Location(locWorld, locX + 1, locY + 4, locZ - 2), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX, locY + 4, locZ - 2), parentID, 98, (byte) 3);
 		createDivineBlock(new Location(locWorld, locX - 1, locY + 4, locZ - 2), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX + 1, locY + 5, locZ - 2), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX, locY + 5, locZ - 2), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX - 1, locY + 5, locZ - 2), parentID, 126, (byte) 1);
 		// Right beam
 		createDivineBlock(new Location(locWorld, locX + 1, locY + 4, locZ + 2), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX, locY + 4, locZ + 2), parentID, 98, (byte) 3);
 		createDivineBlock(new Location(locWorld, locX - 1, locY + 4, locZ + 2), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX + 1, locY + 5, locZ + 2), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX, locY + 5, locZ + 2), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX - 1, locY + 5, locZ + 2), parentID, 126, (byte) 1);
 		// Top beam
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 4, locZ + 1), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 4, locZ), parentID, 98, (byte) 3);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 4, locZ - 1), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 5, locZ + 1), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 5, locZ), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX + 2, locY + 5, locZ - 1), parentID, 126, (byte) 1);
 		// Bottom beam
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 4, locZ + 1), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 4, locZ), parentID, 98, (byte) 3);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 4, locZ - 1), parentID, 98);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 5, locZ + 1), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 5, locZ), parentID, 126, (byte) 1);
 		createDivineBlock(new Location(locWorld, locX - 2, locY + 5, locZ - 1), parentID, 126, (byte) 1);
 
 		
 		// Set locations to use for building
 		Location topLeft = new Location(locWorld, locX + 2, locY + 1, locZ - 2);
 		Location topRight = new Location(locWorld, locX + 2, locY + 1, locZ + 2);
 		Location botLeft = new Location(locWorld, locX - 2, locY + 1, locZ - 2);
 		Location botRight = new Location(locWorld, locX - 2, locY + 1, locZ + 2);
 		
 		// Top left of platform
 		createDivineBlock(topLeft, parentID, 44, (byte) 5);
 		createDivineBlock(topLeft.subtract(1, 0, 0), parentID, 44, (byte) 5);
 		createDivineBlock(topLeft.add(0, 0, 1), parentID, 44, (byte) 5);
 		createDivineBlock(topLeft.add(1, 0, 0), parentID, 44, (byte) 5);
 		// Top right of platform
 		createDivineBlock(topRight, parentID, 44, (byte) 5);
 		createDivineBlock(topRight.subtract(1, 0, 0), parentID, 44, (byte) 5);
 		createDivineBlock(topRight.subtract(0, 0, 1), parentID, 44, (byte) 5);
 		createDivineBlock(topRight.add(1, 0, 0), parentID, 44, (byte) 5);
 		// Bottom left of platform
 		createDivineBlock(botLeft, parentID, 44, (byte) 5);
 		createDivineBlock(botLeft.add(1, 0, 0), parentID, 44, (byte) 5);
 		createDivineBlock(botLeft.add(0, 0, 1), parentID, 44, (byte) 5);
 		createDivineBlock(botLeft.subtract(1, 0, 0), parentID, 44, (byte) 5);
 		// Bottom right of platform
 		createDivineBlock(botRight, parentID, 44, (byte) 5);
 		createDivineBlock(botRight.subtract(0, 0, 1), parentID, 44, (byte) 5);
 		createDivineBlock(botRight.add(1, 0, 0), parentID, 44, (byte) 5);
 		createDivineBlock(botRight.add(0, 0, 1), parentID, 44, (byte) 5);
 		
 		// Create central structure of platform
 		for(int i = 1; i<3; i++) createDivineBlock(new Location(locWorld, locX, locY + 1, locZ + i), parentID, 44, (byte) 5);
 		for(int i = 1; i<3; i++) createDivineBlock(new Location(locWorld, locX, locY + 1, locZ - i), parentID, 44, (byte) 5);
 		for(int i = 1; i<3; i++) createDivineBlock(new Location(locWorld, locX - i, locY + 1, locZ), parentID, 44, (byte) 5);
 		for(int i = 1; i<3; i++) createDivineBlock(new Location(locWorld, locX + i, locY + 1, locZ), parentID, 44, (byte) 5);
 		
 		// Build steps on all sides.
 		Location leftSteps = new Location(locWorld, locX + 2, locY, locZ - 4);
 		Location rightSteps = new Location(locWorld, locX + 2, locY, locZ + 4);
 		Location topSteps = new Location(locWorld, locX + 4, locY, locZ - 2);
 		Location botSteps = new Location(locWorld, locX - 4, locY, locZ - 2);
 	
 		// Create left steps
 		createDivineBlock(leftSteps, parentID, 44, (byte) 5);
 		for(int i = 1; i<5; i++) createDivineBlock(leftSteps.subtract(1, 0, 0), parentID, 44, (byte) 5);
 		createDivineBlock(leftSteps.add(0, 0, 1), parentID, 98);
 		for(int i = 1; i<5; i++) createDivineBlock(leftSteps.add(1, 0, 0), parentID, 98);
 		
 		// Create right steps
 		createDivineBlock(rightSteps, parentID, 44, (byte) 5);
 		for(int i = 1; i<5; i++) createDivineBlock(rightSteps.subtract(1, 0, 0), parentID, 44, (byte) 5);
 		createDivineBlock(rightSteps.subtract(0, 0, 1), parentID, 98);
 		for(int i = 1; i<5; i++) createDivineBlock(rightSteps.add(1, 0, 0), parentID, 98);
 		
 		// Create top steps
 		createDivineBlock(topSteps, parentID, 44, (byte) 5);
 		for(int i = 1; i<5; i++) createDivineBlock(topSteps.add(0, 0, 1), parentID, 44, (byte) 5);
 		createDivineBlock(topSteps.subtract(1, 0, 0), parentID, 98);
 		for(int i = 1; i<5; i++) createDivineBlock(topSteps.subtract(0, 0, 1), parentID, 98);
 		
 		// Create bottom steps
 		createDivineBlock(botSteps, parentID, 44, (byte) 5);
 		for(int i = 1; i<5; i++) createDivineBlock(botSteps.add(0, 0, 1), parentID, 44, (byte) 5);
 		createDivineBlock(botSteps.add(1, 0, 0), parentID, 98);
 		for(int i = 1; i<5; i++) createDivineBlock(botSteps.subtract(0, 0, 1), parentID, 98);
 		
 		// Create left step towers
 		for(int i = 0; i<3; i++) createDivineBlock(leftSteps.add(0, 1, 0), parentID, 98);
 		createDivineBlock(leftSteps.add(0, 1, 0), parentID, 126, (byte) 1);
 		createDivineBlock(leftSteps.subtract(4, 0, 0), parentID, 98);
 		createDivineBlock(leftSteps, parentID, 126, (byte) 1);
 		for(int i = 0; i<3; i++) createDivineBlock(leftSteps.subtract(0, 1, 0), parentID, 98);
 	
 		// Create right step towers
 		for(int i = 0; i<3; i++) createDivineBlock(rightSteps.add(0, 1, 0), parentID, 98);
 		createDivineBlock(rightSteps.add(0, 1, 0), parentID, 126, (byte) 1);
 		createDivineBlock(rightSteps.subtract(4, 0, 0), parentID, 98);
 		createDivineBlock(rightSteps, parentID, 126, (byte) 1);
 		for(int i = 0; i<3; i++) createDivineBlock(rightSteps.subtract(0, 1, 0), parentID, 98);
 	
 		// Create top step towers
 		for(int i = 0; i<3; i++) createDivineBlock(topSteps.add(0, 1, 0), parentID, 98);
 		createDivineBlock(topSteps.add(0, 1, 0), parentID, 126, (byte) 1);
 		createDivineBlock(topSteps.add(0, 0, 4), parentID, 98);
 		createDivineBlock(topSteps, parentID, 126, (byte) 1);
 		for(int i = 0; i<3; i++) createDivineBlock(topSteps.subtract(0, 1, 0), parentID, 98);
 	
 		// Create bottom step towers
 		for(int i = 0; i<3; i++) createDivineBlock(botSteps.add(0, 1, 0), parentID, 98);
 		createDivineBlock(botSteps.add(0, 1, 0), parentID, 126, (byte) 1);
 		createDivineBlock(botSteps.add(0, 0, 4), parentID, 98);
 		createDivineBlock(botSteps, parentID, 126, (byte) 1);
 		for(int i = 0; i<3; i++) createDivineBlock(botSteps.subtract(0, 1, 0), parentID, 98);
 	}
 
 	/*
 	 *  removeAltar() : Removes the Altar at (Location)location.
 	 */
 	public static void removeAltar(Location location)
 	{
 		removeDivineBlock(location);
 	}
 	
 	/*
 	 *  getAllAltars() : Returns an ArrayList<Location> the server's Altars.
 	 */
 	public static ArrayList<Location> getAllAltars()
 	{		
 		ArrayList<Location> altars = new ArrayList<Location>();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{
 			if(((DivineBlock) divineBlock.getValue().get("block_object")).getType().equalsIgnoreCase("altar") && ((DivineBlock) divineBlock.getValue().get("block_object")).isPermanent())
 			{
 				Location blockLoc = ((DivineBlock) divineBlock.getValue().get("block_object")).getLocation();
 				altars.add(blockLoc);
 			}
 		}
 		return altars;
 	}
 	
 	/*
 	 *  getAllAltarBlocks() : Returns an ArrayList<DivineBlock> of (Player)player's Shrines.
 	 */
 	public static ArrayList<DivineBlock> getAllAltarBlocks()
 	{		
 		ArrayList<DivineBlock> altars = new ArrayList<DivineBlock>();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{
 			if(divineBlock.getValue().get("block_object") == null) continue;
			if(((DivineBlock) divineBlock.getValue().get("block_object")).getType().equalsIgnoreCase("atlar")) altars.add((DivineBlock) divineBlock.getValue().get("block_object"));
 		}
 		return altars;
 	}
 	
 	/*
 	 *  isAltar() : Returns true/false depending on if the block at (Location)location is an Altar or not.
 	 */
 	public static boolean isAltarBlock(Location location)
 	{
 		int blockID = getID(location);
 		if(blockID != -1)
 		{
 			if(getDivineBlockType(blockID).equalsIgnoreCase("altar")) return true;
 		}
 		return false;
 	}
 	
 	/* ---------------------------------------------------
 	 * Begin Overall DivineBlock Methods
 	 * ---------------------------------------------------
 	 *
 	 *  getID() : Returns the (int)blockID for the (Location)location.
 	 */
 	public static int getID(Location location)
 	{
 		HashMap<Integer, HashMap<String, Object>> divineBlocks = DDataUtil.getAllBlockData();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : divineBlocks.entrySet())
 		{
 			// Define character-specific variables
 			DivineBlock block = (DivineBlock) divineBlock.getValue().get("block_object");
 			if(block.getLocation().equals(location)) return block.getID();
 		}
 		return -1;
 	}
 	
 	/*
 	 *  getDivineBlocks() : Returns an ArrayList<Location> of (int)parentID's Divine Blocks.
 	 */
 	public static ArrayList<Location> getDivineBlocks(int parentID)
 	{
 		ArrayList<Location> blocks = new ArrayList<Location>();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{
 			DivineBlock block = (DivineBlock) divineBlock.getValue().get("block_object");
 			if(block.getParent() == parentID) blocks.add(block.getLocation());
 		}
 		return blocks;
 	}
 	
 	/*
 	 *  createDivineParentBlock() : Creates a divine block at (Location)location with (Material)type.
 	 */
 	public static int createDivineParentBlock(Location location, int blockType, String blockDeity, String divineType)
 	{
 		int blockID = DObjUtil.generateInt(5);
 		DivineBlock block = new DivineBlock(location, blockID, blockID, true, divineType, blockDeity, blockType, (byte) 0);
 		DDataUtil.saveBlockData(blockID, "block_object", block);	
 		location.getBlock().setTypeId(blockType);
 		return blockID;
 	}
 	
 	/*
 	 *  createDivineBlock() : Creates a divine block at (Location)location with (Material)type.
 	 */
 	public static int createDivineBlock(Location location, int parentID, int blockType)
 	{
 		int blockID = DObjUtil.generateInt(5);
 		DivineBlock block = new DivineBlock(location, blockID, parentID, false, getDivineBlockType(parentID), getDivineBlockDeity(parentID), blockType);
 		DDataUtil.saveBlockData(blockID, "block_object", block);	
 		location.getBlock().setTypeId(blockType);
 		return blockID;
 	}
 	public static int createDivineBlock(Location location, int parentID, int blockType, byte byteData)
 	{
 		int blockID = DObjUtil.generateInt(5);
 		DivineBlock block = new DivineBlock(location, blockID, parentID, false, getDivineBlockType(parentID), getDivineBlockDeity(parentID), blockType, byteData);
 		DDataUtil.saveBlockData(blockID, "block_object", block);	
 		location.getBlock().setTypeId(blockType);
 		location.getBlock().setData(byteData);
 		return blockID;
 	}
 	
 	/*
 	 *  removeDivineBlock() : Removes the Divine Block at (Location)location.
 	 */
 	public static void removeDivineBlock(Location location)
 	{
 		int parentID = getDivineBlockParent(getID(location));
 		DDataUtil.removeAllBlockData(parentID);
 		
 		// Remove child blocks
 		for(Location blockLoc : getDivineBlocks(parentID))
 		{
 			int blockID = getID(blockLoc);
 			DDataUtil.removeAllBlockData(blockID);
 			blockLoc.getBlock().setTypeId(0);
 		}
 		
 		// Remove the parent
 		location.getBlock().setTypeId(0);
 		
 		DDatabase.saveDivineBlocks();
 	}
 	
 	/*
 	 *  removeBlocksWhereParent() : Removes all Divine Blocks where the parentID equals (int)parentID.
 	 */
 	public static void removeBlocksWhereParent(int parentID)
 	{
 		for(Location location : getDivineBlocks(parentID))
 		{
 			removeDivineBlock(location);
 		}
 	}
 	
 	/*
 	 *  isDivineBlock() : Returns a boolean for if the (Location)location is a divine block.
 	 */
 	public static boolean isDivineBlock(Location location)
 	{
 		HashMap<Integer, HashMap<String, Object>> divineBlocks = DDataUtil.getAllBlockData();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : divineBlocks.entrySet())
 		{
 			// Define character-specific variables
 			DivineBlock block = (DivineBlock) divineBlock.getValue().get("block_object");
 			if(block.getLocation().equals(location)) return true;
 		}
 		return false;
 	}
 	
 	/*
 	 *  getDivineBlock() : Returns the (String)divineType for (int)blockID.
 	 */
 	public static DivineBlock getDivineBlock(int blockID)
 	{
 		if(DDataUtil.hasBlockData(blockID, "block_object"))
 		{
 			DivineBlock block = (DivineBlock) DDataUtil.getBlockData(blockID, "block_object");
 			return block;
 		}
 		return null;
 	}
 	
 	/*
 	 *  getDivineBlockType() : Returns the (String)divineType for (int)blockID.
 	 */
 	public static String getDivineBlockType(int blockID)
 	{
 		if(DDataUtil.hasBlockData(blockID, "block_object"))
 		{
 			DivineBlock block = (DivineBlock) DDataUtil.getBlockData(blockID, "block_object");
 			return block.getType();
 		}
 		return null;
 	}
 	
 	/*
 	 *  getDivineBlockDeity() : Returns the (String)blockDeity for (int)blockID.
 	 */
 	public static String getDivineBlockDeity(int blockID)
 	{
 		if(DDataUtil.hasBlockData(blockID, "block_object"))
 		{
 			DivineBlock block = (DivineBlock) DDataUtil.getBlockData(blockID, "block_object");
 			return block.getDeity();
 		}
 		return null;
 	}
 	
 	/*
 	 *  getDivineBlockParent() : Returns the (int)parentID for (int)blockID.
 	 */
 	public static int getDivineBlockParent(int blockID)
 	{
 		if(DDataUtil.hasBlockData(blockID, "block_object"))
 		{
 			DivineBlock block = (DivineBlock) DDataUtil.getBlockData(blockID, "block_object");
 			return block.getParent();
 		}
 		return -1;
 	}
 	
 	/*
 	 *  getAllDivineBlocks() : Returns an ArrayList of all divine block locations.
 	 */
 	public static ArrayList<Location> getAllDivineBlocks()
 	{
 		ArrayList<Location> blocks = new ArrayList<Location>();
 		for(Entry<Integer, HashMap<String, Object>> divineBlock : DDataUtil.getAllBlockData().entrySet())
 		{
 			DivineBlock block = (DivineBlock) divineBlock.getValue().get("block_object");
 			blocks.add(block.getLocation());
 		}
 		return blocks;
 	}
 	
 	// IT'S A DIVING BLOCK, DAMNIT
 }
