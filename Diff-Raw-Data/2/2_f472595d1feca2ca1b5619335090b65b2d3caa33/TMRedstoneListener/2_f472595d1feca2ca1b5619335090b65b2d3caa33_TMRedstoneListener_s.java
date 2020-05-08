 package me.thommy101.TMinecart;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 //import org.bukkit.block.Dispenser;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.inventory.Inventory;
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
 			sign = (Sign)loc.getBlock().getState();
 		if(sign == null) return;
 
 		String line1 = sign.getLine(1);
 		if(line1.trim().equalsIgnoreCase("[collect]"))
 		{
 			//things to do when collecting
 			int IFurnace=0;
 			ArrayList<Block> Containers = findContainer(dRail, true);
 			List<Entity> entitys = findCart(dRail);
 			ArrayList<List<Integer>> signData=readBlocks(sign);
 			if(entitys.isEmpty())plugin.log.warning("Event fired with no cart in reach!");
 			for(Entity storageCart:entitys)
 			{
 				for(Block container:Containers)
 				{
 					//get inventory of the container
 					Inventory inventory = null;
 //					if(container.getTypeId() == 23)
 //						inventory = ((Dispenser) container.getState()).getInventory();
 					if(container.getTypeId() == 54)
 						inventory = ((Chest) container.getState()).getInventory();
 					if(container.getTypeId() == 61)
 					{
 						inventory = ((Furnace) container.getState()).getInventory();
 						IFurnace=1;
 					}
 					if(inventory == null) continue;
 					
 					for(ItemStack itemstack:inventory.getContents())
 					{
 						if(itemstack==null) continue;
 						//copy the itemstack so it wont directly edit the inventory
 						ItemStack itemstack2 = itemstack.clone();
 						//Only run once for the furnace
 						if(IFurnace==1)
 						{
							itemstack2=inventory.getItem(3);
 							IFurnace=2;
 						}else if(IFurnace==2){
 							return;
 						}
 						//if the itemstack is empty/non existing, stop.
 						if(itemstack2==null)continue;
 						
 						for(int i = 0; i < signData.get(0).size(); i++)
 						{
 							//check the signdata for the item ID
 							if(signData.get(0).get(i) != itemstack2.getTypeId() && signData.get(0).get(i) != -1) continue;
 							//if item Amount equals to 0. stop
 							if(signData.get(2).get(i)==0) continue;
 							//check the signdata for the item Meta Data
 							if(signData.get(1).get(i)==-1 ||
 									signData.get(1).get(i)==itemstack2.getData().getData() ||
 									signData.get(0).get(0)==-1)
 							{
 								//check the signdata for the item Amount.
 								if(signData.get(2).get(i)!=-1&&itemstack2.getAmount()>signData.get(2).get(i))
 									itemstack2.setAmount(signData.get(2).get(i));
 								int leftover = modCart(storageCart, itemstack2, true);
 								itemstack2.setAmount(itemstack2.getAmount()-leftover);
 								modContainer(container.getLocation(), itemstack2, false, i);
 								//modify signdata item Amount if nesseserry
 								if(signData.get(2).get(i)>=0 && signData.get(0).get(i)!=-1)
 									signData.get(2).set(i, signData.get(2).get(i)-itemstack2.getAmount());
 							}
 						}
 					}
 				}
 			}
 		}
 		else if (line1.trim().equalsIgnoreCase("[deposit]"))
 		{
 			//things to do when depositing
 			ArrayList<List<Integer>> signData=readBlocks(sign);
 			List<Entity> entitys = findCart(dRail);
 			ArrayList<Block> Containers = findContainer(dRail, false);
 			for(Entity storageCart:entitys)
 			{
 				for(Block container:Containers)
 				{
 					StorageMinecart cart=(StorageMinecart) storageCart;
 					for(ItemStack itemstack:cart.getInventory().getContents())
 					{
 						//if the itemstack is empty/non existing, stop.
 						if(itemstack==null) continue;
 						for(int i = 0; i < signData.get(0).size(); i++)
 						{
 							//copy the itemstack so it wont directly edit the inventory
 							ItemStack itemstack2 = itemstack.clone();
 							//check the signdata for the item ID
 							if(signData.get(0).get(i) != itemstack2.getTypeId() && signData.get(0).get(i) != -1) continue;
 							//if item Amount equals to 0. stop
 							if(signData.get(2).get(i)==0) continue;
 							//check the signdata for the item Meta Data
 							if(signData.get(1).get(i)==-1 ||
 									signData.get(1).get(i)==itemstack2.getData().getData() ||
 									signData.get(0).get(0)==-1)
 							{
 								//check the signdata for the item Amount.
 								if(signData.get(2).get(i)!=-1&&itemstack2.getAmount()>signData.get(2).get(i))
 									itemstack2.setAmount(signData.get(2).get(i));
 								int leftover = modContainer(container.getLocation(), itemstack2, true, i);
 								itemstack2.setAmount(itemstack2.getAmount()-leftover);
 								modCart(storageCart, itemstack2, false);
 								//modify signdata item Amount if nesseserry
 								if(signData.get(2).get(i)>=0 && signData.get(0).get(i)!=-1)
 									signData.get(2).set(i, signData.get(2).get(i)-itemstack2.getAmount());
 							}
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
 	 * @return				List of vallid containers.
 	 */
 	private ArrayList<Block> findContainer(Block dRail, boolean privateCheck)
 	{
 		//Initialize List of chests
 		ArrayList<Block> VallidContainers = new ArrayList<Block>();
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
 			if(/*location.getBlock().getTypeId()==23 ||*/
 					location.getBlock().getTypeId()==54 ||
 					location.getBlock().getTypeId()==61)
 			{
 				if(privateCheck)
 				{
 					if(isBlockPublic(location.getBlock()))
 						VallidContainers.add(location.getBlock());
 				}
 				else
 					VallidContainers.add(location.getBlock());
 			}
 		}
 		//Return the list
 		return VallidContainers;
 	}
 	
 	/**
 	 * Checks if chest is public (with LWC plugin)
 	 * @param chest		Chest needed to be checked
 	 * @return			True if chest is public/not registered, false if else
 	 */
 	private boolean isBlockPublic(Block Block)
 	{
 		if(plugin.LWCEnabled)
 		{
 			Protection protection = LWC.getInstance().findProtection(Block);
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
 		//Make list, and get all entitys in surrounding chunks.
 		List<Entity> entitys = new ArrayList<Entity>();
 		
 		double intX = dRail.getLocation().getX();
 		double intY = dRail.getLocation().getY();
 		double intZ = dRail.getLocation().getZ();
 		
 		Location locations[]={
 				new Location(dRail.getWorld(), intX+1, intY, intZ),
 				new Location(dRail.getWorld(), intX-1, intY, intZ),
 				new Location(dRail.getWorld(), intX, intY, intZ+1),
 				new Location(dRail.getWorld(), intX, intY, intZ-1)};
 		for(Location location: locations)
 		{
 			for(Entity entity:location.getChunk().getEntities())
 				entitys.add(entity);
 		}
 		
 		//remove duplicates of list
 		HashSet<Entity> h = new HashSet<Entity>(entitys);
 		entitys.clear();
 		entitys.addAll(h);
 
 		
 		//Check every entity for a vallid storage cart close to the Detector Rail
 		// If valid, add it to list
 		for(Entity entity:entitys)
 		{
 			if(isValidStorageCart(entity, dRail.getLocation()))
 				entityList.add(entity);
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
 	 * @return		An ArrayList with 3 lists - Id, MetaData, Amount
 	 */
 	private ArrayList<List<Integer>> readBlocks(Sign sign)
 	{
 		ArrayList<List<Integer>> signData = new ArrayList<List<Integer>>();
 		if(sign.getLine(1).equalsIgnoreCase("*") || sign.getLine(1).equalsIgnoreCase("all"))
 		{
 			List<Integer> list = new ArrayList<Integer>();
 			list.add(-1);
 			signData.add(list);
 			return signData;
 		}
 		List<Integer> listId = new ArrayList<Integer>();
 		List<Integer> listMd = new ArrayList<Integer>();
 		List<Integer> listAm = new ArrayList<Integer>();
 		
 		String signrules;
 		if(sign.getLine(2).substring(sign.getLine(2).length()-1).equals(","))
 		{
 			signrules= sign.getLine(2)+sign.getLine(3);
 		}else{
 			signrules= sign.getLine(2)+","+sign.getLine(3);
 		}
 
 		//sign.getLine(2).length();
 		String[] stringParts = signrules.split(",");
 		for(String part:stringParts)
 		{
 			int id;//ID
 			int md;//Meta Data
 			int am;//Amount
 			if(part.contains(":") && part.contains("="))
 			{
 				String[] part1 = part.split(":");
 				id = parseInt(part1[0]);
 				String[] part2 = part1[1].split("=");
 				md = parseInt(part2[0]);
 				am = parseInt(part2[1]);
 			}
 			else if(part.contains(":"))
 			{
 				String[] part1 = part.split(":");
 				id = parseInt(part1[0]);
 				md = parseInt(part1[1]);
 				am = -1;
 			}
 			else if(part.contains("="))
 			{
 				String[] part1 = part.split("=");
 				id = parseInt(part1[0]);
 				md = -1;
 				am = parseInt(part1[1]);
 			}
 			else
 			{
 				id = parseInt(part);
 				md = -1;
 				am = -1;
 			}
 			listId.add(id);
 			listMd.add(md);
 			listAm.add(am);
 		}
 		signData.add(listId);
 		signData.add(listMd);
 		signData.add(listAm);
 		return signData;
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
 	private int modContainer(Location location, ItemStack itemstack, boolean fill, int index)
 	{
 		boolean Bfurnace = false;
 		Inventory inventory = null;
 //		if(location.getBlock().getTypeId() == 23)
 //			inventory = ((Dispenser) container.getState()).getInventory();
 		if(location.getBlock().getTypeId() == 54)
 			inventory = ((Chest) location.getBlock().getState()).getInventory();
 		if(location.getBlock().getTypeId() == 61)
 		{
 			//if index is larger than 2, it should not be put in the oven
 			//return the whole itemstack.
 			if(index > 2) return itemstack.getAmount();
 			Bfurnace = true;
 			inventory = ((Furnace) location.getBlock().getState()).getInventory();
 		}
 			
 		if(fill)
 		{
 			int leftover = 0;
 			if(Bfurnace)
 			{
 				if(inventory.getItem(index)!=null)
 				{
 					int amount = itemstack.getAmount() + inventory.getItem(index).getAmount();
 					if (inventory.getItem(index).getData().equals(itemstack.getData()))
 					{
 						if(amount > 64)
 						{
 							inventory.getItem(index).setAmount(64);
 							leftover = amount - 64;
 						}else{
 							inventory.getItem(index).setAmount(amount);
 						}
 					}
 				}else{//no item in furnace
 					inventory.setItem(index, itemstack);
 				}
 			}else{
 				HashMap<Integer, ItemStack> hmLeftover = inventory.addItem(itemstack);
 				if(!(hmLeftover.isEmpty()) || hmLeftover==null)
 				{
 					leftover = hmLeftover.get(0).getAmount();
 				}
 			}
 			return leftover;
 		}else{
 			//empty
 			
 			if(Bfurnace)
 			{
 				//THIS WORKS. BUT GETS FROM ALL SLOTS.. NOT ONLY THE OUTPUT
 				//inventory.remove(itemstack);
 				ItemStack temp = inventory.getItem(2).clone();
 				temp.setAmount(inventory.getItem(2).getAmount()-itemstack.getAmount());
 				inventory.setItem(2, temp);
 			}else{
 				inventory.removeItem(itemstack);
 			}
 
 		}
 		return 0;
 	}
 	
 	/**
 	 * Converts string to int
 	 * @param s
 	 * @return Integer if succeded, -1 if can't convert.
 	 */
 	private int parseInt(String s)
 	{
 		int i = -1;
 		try
 		{
 			i = Integer.parseInt(s);
 		}
 		catch(NumberFormatException ex){}
 		return i;
 	}
 }
