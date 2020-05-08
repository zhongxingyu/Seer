 package com.Satrosity.XrayProtect;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.yaml.snakeyaml.Yaml;
 
 /**
  * Controls all hidden chests data, including the hiding and revealing of a chest, and loading and saving the contents of chests to a file.
  * @author Steven 'Satrosity' Mance
  */
 public class ChestManager {
 	
 	//HashMap of Hidden Chests
 	private HashMap<Location,ChestData> hiddenChests;
 	private static XrayProtect plugin;
 	private File ChestDataFile;
 	
 	/**
 	 * Class Constructor, calls load function to load all chests from the chest save file
 	 * @param instance  The instance of the plugin running
 	 */
 	public ChestManager(XrayProtect instance)
 	{
 		plugin = instance;
 		hiddenChests = new HashMap<Location,ChestData>();
 		//File of chest data save file
 		ChestDataFile = new File("plugins/XrayProtect/ChestData.yml");
 		//Load the chest data from its save file
 		loadHiddenChests();
 	}
 	/**
 	 * Turns the block at the given location into a chest, restoring the contents from the data given by the associated ChestData for the location,
 	 * and then removing the data from the manager and saving.
 	 * @param loc  The location of the hidden chest to reveal
 	 */
 	public void revealChest(Location loc)
 	{
 		//Get block at the location
 		Block b = plugin.getServer().getWorld(loc.getWorld().getName()).getBlockAt(loc);
 		//Turn it into a chest
 		b.setTypeId(54);
 		
 		//Cast to a chest object
 		BlockState bs = b.getState();
 		if(bs instanceof Chest) {
 			Chest chest = (Chest)bs;
 			//Get associated stored chest data for the location
 			ChestData cd = hiddenChests.get(loc);
 			//Restore items to the chest
 			for(ItemStack item : cd.getItems())
 			{
 				if(item != null) {
 					chest.getInventory().addItem(item);
 				}
 			}
 			b.setData(chest.getRawData());
 			//Remove the chest data from the manager since it is no longer hidden
 			hiddenChests.remove(loc);
 		}
 		//Save the state of the hidden chests in the chest manager
 		save();
 	}
 	/**
 	 * Inserts the location and contents of a chest into the ChestManager hiddenChests map, then turns the chest block to a surrounding block and saves
 	 * @param loc  The location of the chest to store and hide
 	 */
 	public void insertSingleChest(Location loc)
 	{
 		//Get block at the location
 		BlockState bs = plugin.getServer().getWorld(loc.getWorld().getName()).getBlockAt(loc).getState();
 		//Cast into a chest object
 		if(bs instanceof Chest) {
 			Chest chest = (Chest)bs;
 			//Get items inside the chest
 			ItemStack[] items = chest.getInventory().getContents();
 			//Clear contents of the chest at the location so when the block is changed the items don't drop everywhere
 			chest.getInventory().clear();
 			
 			//Add chest data to hiddenChests map, change the chest block to something like stone, then save the state of the ChestManager's hidden chests
 			if(!hiddenChests.containsKey(loc)) {
 				hiddenChests.put(loc,new ChestData(items));
 				hideChest(loc);
 				save();
 			}
 		}
 	}
 	/**
 	 * Inserts the locations and contents of 2 chests combined as a double chest into the ChestManager's hidden chests map, then turns the chest into a surrounding block and saves.
 	 * @param loc  The location of the chest to store and hide
 	 * @param connectedLoc  The location of the connected chest to store and hide
 	 */
 	public void insertDoubleChest(Location loc, Location connectedLoc) {
 		
 		//Get the chests at the locations
 		BlockState bs = plugin.getServer().getWorld(loc.getWorld().getName()).getBlockAt(loc).getState();
 		Chest chest1 = (Chest)bs;
 		BlockState bs2 = plugin.getServer().getWorld(connectedLoc.getWorld().getName()).getBlockAt(connectedLoc).getState();
 		Chest chest2 = (Chest)bs2;
 			
 		//Must split the inventory into 2 arrays
 		//add first chest
 		ItemStack[] items = chest1.getInventory().getContents(); //this will get inventory of the combined chest
 		chest1.getInventory().clear();
 		chest2.getInventory().clear();
 			
 		//split inventory if greater than 27
 		ItemStack[] chestInvA = new ItemStack[27];
 		ItemStack[] chestInvB = new ItemStack[27];
 		
 		System.arraycopy(items, 0, chestInvA, 0, chestInvA.length);
 		System.arraycopy(items, chestInvA.length, chestInvB, 0, chestInvB.length);
 
 		//add first chest
 		//Add chest data to hiddenChests map, change the chest block to something like stone, then save the state of the ChestManager's hidden chests
 		if(!hiddenChests.containsKey(loc)) {
 			hiddenChests.put(loc,new ChestData(chestInvA));
 			hideChest(loc);
 			save();
 		}
 			
 		//add second chest
 		//Add chest data to hiddenChests map, change the chest block to something like stone, then save the state of the ChestManager's hidden chests
 		if(!hiddenChests.containsKey(connectedLoc)) {
 			hiddenChests.put(connectedLoc,new ChestData(chestInvB));
 			hideChest(connectedLoc);
 			save();
 		}
 	}
 	/**
 	 * Turns the chest at the given location into the block below it
 	 * @param loc  The location of the chest to change
 	 */
 	private void hideChest(Location loc)
 	{
 		Location under = new Location(loc.getWorld(), loc.getBlockX(), loc.getY()-1, loc.getZ());
 		int underId = plugin.getServer().getWorld(loc.getWorld().getName()).getBlockTypeIdAt(under);
 		plugin.getServer().getWorld(loc.getWorld().getName()).getBlockAt(loc).setTypeId(underId);
 	}
 	/**
 	 * Returns true if there is a hidden chest at the location, otherwise returns false
 	 * @param loc  Location to check for a hidden chest at
 	 * @return true if there is a hidden chest at the location, otherwise returns false
 	 */
 	public boolean isChestHiddenAt(Location loc)
 	{
 		return hiddenChests.containsKey(loc);
 	}
 	/**
 	 * Outputs the current locations and item contents of ChestManager's hidden chests map to a file in YAML format.
 	 */
 	private void save() {
 
 		//Save hidden chests to a file
 		try{
 			FileWriter fstream = new FileWriter(ChestDataFile, false);
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write("");
 			int i = 0;
 			
 			//For each location in hiddenChests
 			for(Location loc : hiddenChests.keySet()) {
 				String index = i+": \n";
 				ChestData cd = hiddenChests.get(loc);
 				String locationSave = "    Location: {X: "+loc.getBlockX() +", Y: "+loc.getBlockY()+", Z: "+ loc.getBlockZ() +", World: " +loc.getWorld().getName()+"}\n";
 				String itemSave = "    Items: \n";
 
 				//Item serialization doesn't work as elegantly with YAML as I had hoped, so need to do it from scratch
 				ItemStack[] itemsList = cd.getItems();
 				if(itemsList != null) 
 				{
 					for(ItemStack item : itemsList)
 					{
 						if(item != null) {
 							Integer typeID = item.getTypeId();
 							Integer amount = item.getAmount();
 							Short damage = (short) item.getDurability();
 							Map<Enchantment,Integer> enchantments = item.getEnchantments();
 							
 							//add type
 							itemSave += "        - {type: "+typeID;
 							//add amount if more than 1
 							if(amount > 1)
 								itemSave += ", amount: " + amount;
 							//add damage if more than 0
 							if(damage > 0)
 								itemSave += ", damage: " + damage;
 							//add enchantments if any
 							if(enchantments.size() > 0){
 							
 								String enchantList = "{";
 								int encListSize = enchantments.keySet().size();
 								int encListCounter = 0;
 								for(Enchantment enc : enchantments.keySet())
 								{
 									if(encListCounter < encListSize-1)
 										enchantList += enc.getName() + ": " + item.getEnchantmentLevel(enc) + ", ";
 									else
 										enchantList += enc.getName() + ": " + item.getEnchantmentLevel(enc) + "}";
 									encListCounter++;
 								}
 								if(encListSize == 0)
 									enchantList += "}";
 								itemSave += ", enchantments: " + enchantList;
 							}
 							itemSave += "}\n";
 						}
 					}
 				}
 				out.write(index);
 				out.write(locationSave);
 				out.write(itemSave);
 				
 				i++;
 			}
 			out.close();
 			fstream.close();
 		}catch (Exception e){//Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 
 	}
 	/**
 	 * Loads the data from ChestDataFile into ChestManager's hidden chest map using YAML.
 	 */
 	@SuppressWarnings("unchecked")
 	public void loadHiddenChests()
 	{
 		Reader reader = null;
 		Yaml yaml = new Yaml();
 		HashMap<Integer, HashMap<String,Object>> chestsToLoad = null;
         try {
             reader = new FileReader(ChestDataFile);
             chestsToLoad = (HashMap<Integer, HashMap<String,Object>>)yaml.load(reader);
         } catch (final FileNotFoundException fnfe) {
         	 System.out.println("ChestData.YML Not Found!");
         	   try{
 	            	  String strManyDirectories="plugins/XrayProtect";
 	            	  boolean success = (new File(strManyDirectories)).mkdirs();
 	           }catch (Exception e){  System.err.println("Error: " + e.getMessage()); }
         } finally {
             if (null != reader)
                 try { reader.close();} catch (final IOException ioe) {}
         }
         if(chestsToLoad != null)
         {
         	for(Integer key : chestsToLoad.keySet())
         	{
         		HashMap<String,Object> StoredChestData = chestsToLoad.get(key);
         		//Load Location
         		HashMap<String,Object> LocationData = (HashMap<String, Object>) StoredChestData.get("Location");
         		int x = (int) LocationData.get("X");
         		int y = (int) LocationData.get("Y");
         		int z = (int) LocationData.get("Z");
         		String world = (String) LocationData.get("World");
         		
        		World w = plugin.getServer().getWorlds().get(0);
         		Location loc = new Location(w, x, y, z);
         		//Load Item list
         			ArrayList<HashMap<String,Object>> ItemsData = (ArrayList<HashMap<String,Object>>) StoredChestData.get("Items");
     				ItemStack[] items = new ItemStack[0];
         			if(ItemsData != null){
 		        		items = new ItemStack[ItemsData.size()];
 		        		int i = 0;
 		        		for(HashMap<String,Object> itemSerial : ItemsData) {
 		        			//set type
 		        			int typeid = (int) itemSerial.get("type");
 		        			items[i] = new ItemStack(typeid);
 		        			//items[i].setTypeId(typeid);
 		        			//set amount
 		        			int amount = 1;
 		        			if(itemSerial.containsKey("amount"))
 		        				amount = (int) itemSerial.get("amount");
 		        			items[i].setAmount(amount);
 		        			//set damage
 		        			short damage = 0;
 		        			if(itemSerial.containsKey("damage"))
 		        				damage = (short) ((int)itemSerial.get("damage"));
 		        			items[i].setDurability(damage);
 		        			//set enchantments
 		          			if(itemSerial.containsKey("enchantments")) {
 		        				HashMap<String,Integer> enchantments = (HashMap<String, Integer>) itemSerial.get("enchantments");
 		        				for(String encName : enchantments.keySet())
 		        				{
 		        					int level = enchantments.get(encName);
 		        					items[i].addUnsafeEnchantment(Enchantment.getByName(encName), level);
 		        				}
 		          			}
 		        			i++;
 		        		}
         			}
 	        		//Lastly, add the chest data to hashmap
 	        		hiddenChests.put(loc, new ChestData(items));
         	}
         }
 	}
 
 }
