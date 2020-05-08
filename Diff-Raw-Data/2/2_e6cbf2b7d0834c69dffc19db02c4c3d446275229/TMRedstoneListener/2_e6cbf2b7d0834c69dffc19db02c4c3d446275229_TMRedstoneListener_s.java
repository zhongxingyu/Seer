 package me.thommy101.TMinecart;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.griefcraft.lwc.LWC;
 import com.griefcraft.model.Protection;
 
 public class TMRedstoneListener implements Listener
 {
 private static TMinecart plugin;
 	
 	public TMRedstoneListener(TMinecart instance)
 	{
 		plugin = instance;
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onRedstoneChange(BlockRedstoneEvent event)
 	{
 		Block dRail = event.getBlock();
 		//If block isn't a detectorrail || new current isn't 1(ON) -> return
 		if(dRail.getTypeId()!=28 || event.getNewCurrent()!=1) return;
 		Location loc = new Location(dRail.getWorld(), dRail.getX(), dRail.getY()-2, dRail.getZ());
 		Sign sign = null;
 		if(loc.getBlock().getTypeId()==63||loc.getBlock().getTypeId()==68)
 		{
 			sign = (Sign)loc.getBlock().getState();
 		}
 		if(sign == null) return;
 
 		String line1 = sign.getLine(1);
 		if(line1.equals("[collect]"))
 		{
 			//things to do when collecting
 			List<Chest> chests = findChest(dRail, false);
 			List<Entity> entitys = findCart(dRail);
 			List<Integer> blockIds=readBlocks(sign);
 			for(Entity storageCart:entitys)
 			{
 				for(Chest chest:chests)
 				{
 					for(ItemStack itemstack:chest.getInventory().getContents())
 					{
 						if(itemstack==null) continue;
						if(!blockIds.contains(itemstack.getTypeId()) || blockIds.contains(-1))
 						{
 							//Itemstack is on blockIds' list
 							int leftover = modCart(storageCart, itemstack, true);
 							itemstack.setAmount(itemstack.getAmount()-leftover);
 							modChest(chest.getLocation(), itemstack, false);
 						}
 					}
 				}
 			}
 		}
 		else if (line1.equals("[deposit]"))
 		{
 			//things to do when depositing
 			List<Integer> blockIds=readBlocks(sign);
 			List<Entity> entitys = findCart(dRail);
 			List<Chest> chests = findChest(dRail, true);
 			for(Entity storageCart:entitys)
 			{
 				for(Chest chest:chests)
 				{
 					StorageMinecart cart=(StorageMinecart) storageCart;
 					for(ItemStack itemstack:cart.getInventory().getContents())
 					{
 						if(itemstack==null) continue;
 						if(blockIds.contains(itemstack.getTypeId()) || blockIds.contains(-1))
 						{
 							//Itemstack is on blockIds' list
 							int leftover = modChest(chest.getLocation(), itemstack, true);
 							itemstack.setAmount(itemstack.getAmount()-leftover);
 							modCart(storageCart, itemstack, false);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Finds chest next to dRail and optional checks if they are private or not.
 	 * @param dRail			Detectorrail that needs to be searched next to.
 	 * @param privateCheck	Need chests to be checked if they are private?
 	 * @return				List of chests.
 	 */
 	private List<Chest> findChest(Block dRail, boolean privateCheck)
 	{
 		//Initialize List of chests
 		List<Chest> chestList = new ArrayList<Chest>();
 		//Make array of possible locations of chests
 		World world = dRail.getWorld();
 		int intX = dRail.getX();
 		int intY = dRail.getY();
 		int intZ = dRail.getZ();
 		Location locations[]={
 				new Location(world, intX+1, intY, intZ),
 				new Location(world, intX+1, intY-1, intZ),
 				new Location(world, intX-1, intY, intZ),
 				new Location(world, intX-1, intY-1, intZ),
 				new Location(world, intX, intY, intZ+1),
 				new Location(world, intX, intY-1, intZ+1),
 				new Location(world, intX, intY, intZ-1),
 				new Location(world, intX, intY-1, intZ-1)};
 		//Check every location for vallid chest, if valid, add it to list
 		for(Location location:locations)
 		{
 			if(location.getBlock().getTypeId()==54)
 			{
 				if(privateCheck)
 				{
 					if(isChestPublic(location.getBlock()))
 						chestList.add((Chest)location.getBlock().getState());
 				}
 				else
 					chestList.add((Chest)location.getBlock().getState());
 			}
 		}
 		//Return the list
 		return chestList;
 	}
 	
 	/**
 	 * Checks if chest is public (with LWC plugin)
 	 * @param chest		Chest needed to be checked
 	 * @return			True if chest is public/not registered, false if else
 	 */
 	private boolean isChestPublic(Block chest)
 	{
 		if(plugin.LWCEnabled)
 		{
 			Protection protection = LWC.getInstance().findProtection(chest);
 			if(protection==null || protection.typeToString().equalsIgnoreCase("public")) return true;
 		}else{
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Finds a cart close to the detector rail
 	 * Executes isValidStorageCart to validate carts
 	 * 
 	 * @param dRail		Detector Rail block.
 	 * @return			Entity list of valid storage carts
 	 */
 	private List<Entity> findCart(Block dRail)
 	{
 		//Initialize List of entitys
 		List<Entity> entityList = new ArrayList<Entity>();
 		//Get array of entitys in chunk of Detector Rail.
 		Entity[] entitys = dRail.getChunk().getEntities();
 		//Check every entity for a vallid storage cart close to the Detector Rail
 		// If valid, add it to list
 		for(Entity entity:entitys)
 		{
 			if(isValidStorageCart(entity, dRail.getLocation()))
 			{
 				entityList.add(entity);
 			}
 		}
 		return entityList;
 	}
 	
 	/**
 	 * Checks if entity is a valid storage minecart, which is close to the detector rail.
 	 * 
 	 * @param entity			The entity needed to be checked.
 	 * @param dRailLocation		The location of the detector rail.
 	 * @return					True if vallid storage minecart.
 	 */
 	private boolean isValidStorageCart(Entity entity, Location dRailLocation)
 	{
 		if(!(entity instanceof StorageMinecart)) return false;
 		double distanceToRail=entity.getLocation().distance(dRailLocation);
 		return distanceToRail <= 1.5;
 	}
 	
 	/**
 	 * Reads the sign to check the information.
 	 * 
 	 * @param sign	The sign needed to be checked.
 	 * @return		An array of block ID's need to be checked in next part.
 	 */
 	//TODO Make an "*" option to get all blocks out of chest/cart
 	private List<Integer> readBlocks(Sign sign)
 	{
 		List<Integer> ids = new ArrayList<Integer>();
 		String line2=sign.getLine(2);
 		String line2and3=line2+","+sign.getLine(3);
 		if(line2.equals("*") || line2.equalsIgnoreCase("all"))
 		{
 			ids.add(-1);
 			return ids;
 		}
 		String splitLine[] = line2and3.split(",");
 		for(String linePart:splitLine)
 		{
 			try
 			{
 				ids.add(Integer.parseInt(linePart.trim()));
 			}
 			catch(NumberFormatException ex){}
 		}
 		return ids;
 	}
 	
 	/**
 	 * Modifies a storage minecart.
 	 * Fills/empties a cart with given "amount" of "blockid".
 	 * 
 	 * @param entity	The entity that is the storage minecart.
 	 * @param itemstack	The itemstack that need to be put in/get out of the storage minecart.
 	 * @param fill		True if fill, False if empty.
 	 * @return			Amount that didn't fit in the cart.
 	 */
 	private int modCart(Entity entity, ItemStack itemstack, boolean fill)
 	{
 		StorageMinecart storageMinecart=(StorageMinecart) entity;
 		if(fill)
 		{
 			//ItemStack itemstack = new ItemStack(blockid, amount);
 			HashMap<Integer, ItemStack> hmLeftover = storageMinecart.getInventory().addItem(itemstack);
 			int leftover = 0;
 			if(!(hmLeftover.isEmpty()) || hmLeftover==null)
 			{
 				leftover = hmLeftover.get(0).getAmount();
 			}
 			return leftover;
 		}else{
 			//empty
 			storageMinecart.getInventory().removeItem(itemstack);
 		}
 		return 0;
 	}
 	
 	/**
 	 * Modifies a chest.
 	 * Fills/empties a chest with given "amount" of "blockid".
 	 * 
 	 * @param location	Location of the chest.
 	 * @param itemstack	The itemstack that need to be put in/get out of the chest.
 	 * @param fill		True if fill, False if empty.
 	 * @return			Amount that didn't fit in the cart.
 	 */
 	private int modChest(Location location, ItemStack itemstack, boolean fill)
 	{
 		Chest chest = (Chest)location.getBlock().getState();
 		if(fill)
 		{
 			HashMap<Integer, ItemStack> hmLeftover = chest.getInventory().addItem(itemstack);
 			int leftover = 0;
 			if(!(hmLeftover.isEmpty()) || hmLeftover==null)
 			{
 				leftover = hmLeftover.get(0).getAmount();
 			}
 			return leftover;
 		}else{
 			//empty
 			chest.getInventory().removeItem(itemstack);
 		}
 		return 0;
 	}
 }
