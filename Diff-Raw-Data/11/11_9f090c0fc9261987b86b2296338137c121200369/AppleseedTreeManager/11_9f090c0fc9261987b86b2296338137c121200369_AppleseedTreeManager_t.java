 package redsgreens.Appleseed;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.craftbukkit.block.CraftSign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
 import org.yaml.snakeyaml.Yaml;
 
 /**
  * AppleseedTreeManager handles the adding/removing/processing of trees
  * 
  * NOTE: the class has "synchronized" everywhere because saving trees can be executed
  *       in another thread
  *
  * @author redsgreens
  */
 public class AppleseedTreeManager {
 
     // hashmap of tree worlds, locations and data
     private HashMap<String, HashMap<AppleseedLocation, AppleseedTreeData>> WorldTrees = new HashMap<String, HashMap<AppleseedLocation, AppleseedTreeData>>();
 
     public static boolean SaveRunning = false;
     
     private Random rand = new Random();
 
     final int treeId = Material.LOG.getId();
     final int leafId = Material.LEAVES.getId();
 
 	public AppleseedTreeManager()
 	{
 		loadTrees();
 	}
 	
     // loop through the list of trees and drop items around them, then schedule the next run
     public synchronized void ProcessTrees(){
 
     	// only process trees in loaded worlds
     	List<World> worlds = Appleseed.Plugin.getServer().getWorlds();
 		Iterator<World> worldItr = worlds.iterator();
 		
 		while(worldItr.hasNext())
 		{
 			World world = worldItr.next();
 			String worldName = world.getName();
 			
 			// get the trees for this world
 			HashMap<AppleseedLocation, AppleseedTreeData> trees = WorldTrees.get(worldName);
 
 			// bail if no trees to process
 	    	if(trees.size() != 0){
 	    		
 	    		// iterate over the trees in this world
 	        	Set<AppleseedLocation> locations = trees.keySet();
 	        	Iterator<AppleseedLocation> itr = locations.iterator();
 	        	while(itr.hasNext()){
 	        		AppleseedLocation aloc = itr.next();
 
 	        		try {
 	        			
 	        			// make sure the chunk is loaded
 						Chunk chunk = world.getChunkAt(((Double)aloc.getX()).intValue(), ((Double)aloc.getZ()).intValue());
 						if(chunk == null)
 							continue;
 						else if(world.isChunkLoaded(chunk)){
 							Location loc = aloc.getLocation();
 							Block block = world.getBlockAt(loc);
 							if(block == null)
 								continue;
 
 							AppleseedTreeData tree = trees.get(aloc); 
 
 							if(isTree(loc)){
 								// the tree is alive
 								AppleseedItemStack iStack = tree.getItemStack();
 								if(iStack != null)
 								{
 									AppleseedTreeType treeType = Appleseed.Config.TreeTypes.get(iStack);
 
 									if(treeType != null)
 									{
 										// and has a treetype
 										
 										Boolean dropItem = false;
 										AppleseedCountMode countMode = tree.getCountMode();
 										
 										if(countMode == AppleseedCountMode.Drop)
 										{
 											if(rand.nextInt(((Math.round(((Double)(100 / treeType.getDropLikelihood())).intValue())))) == 0)
 												if(tree.decrementCount())
 													dropItem = true;
 										}
 										else
 										{
 											if(tree.decrementCount())
 												if(rand.nextInt(((Math.round(((Double)(100 / treeType.getDropLikelihood())).intValue())))) == 0)
 													dropItem = true;
 										}
 
 										if(dropItem)
 										{
 						    				Item item = loc.getWorld().dropItemNaturally(loc, tree.getItemStack().getItemStack());
 						    				
 						    				if(Appleseed.Config.MaxUncollectedItems != -1)
 						    				{
 						    					List<Entity> itemList = item.getNearbyEntities(10, 10, 10);
 						    					Iterator<Entity> itemItr = itemList.iterator();
 						    					Integer count = 0;
 						    					
 						    					while(itemItr.hasNext())
 						    					{
 						    						Entity entity = itemItr.next();
 						    						if(entity instanceof Item && !entity.isDead())
						    						{
						    							ItemStack iis = ((Item)entity).getItemStack();
						    							if(iis.getType() == item.getItemStack().getType())
						    								count += iis.getAmount();
						    						}
 						    					}
 						    					
 						    					if(count > Appleseed.Config.MaxUncollectedItems)
 						    						item.remove();
 						    				}
 										}
 
 										if(!tree.isAlive())
 											KillTree(loc);
 										
 						    			if(tree.hasSign())
 						    				updateSign(tree);
 									}
 									else
 										System.out.println("Appleseed: No TreeType in config.yml for \"" + AppleseedItemStack.getItemStackName(iStack) + "\"");
 								}
 							}
 							else if(world.getBlockAt(loc).getType() != Material.SAPLING)
 								itr.remove();
 
 						}
 					} catch (Exception e) {
 						System.out.println("Appleseed: Error dropping item in world \"" + aloc.getWorldName() + "\"");
 					}
 	        	}
 
 	        	asyncSaveTrees();
 	        }
     	
 		}
 
     	// reprocess the list every interval
 		Appleseed.Plugin.getServer().getScheduler().scheduleSyncDelayedTask(Appleseed.Plugin, new Runnable() {
 		    public void run() {
 		    	ProcessTrees();
 		    }
 		}, Appleseed.Config.DropInterval*20);
 
     }
 
     // add a tree to the hashmap
     public synchronized void AddTree(AppleseedLocation loc, AppleseedItemStack iStack, String player)
     {
 //    	Appleseed.PlayerManager.CapAddTree(player, loc.getWorldName());
     	WorldTrees.get(loc.getWorldName()).put(loc, new AppleseedTreeData(loc, iStack, player));
     }
 
     // add a tree to the hashmap
     public synchronized void AddTree(AppleseedLocation loc, AppleseedItemStack iStack, AppleseedCountMode cm, Integer dropcount, Integer fertilizercount, Integer intervalcount, String player)
     {
 //    	Appleseed.PlayerManager.CapAddTree(player, loc.getWorldName());
     	WorldTrees.get(loc.getWorldName()).put(loc, new AppleseedTreeData(loc, iStack, cm, dropcount, fertilizercount, intervalcount, player));
     }
 
     // return a tree if there is one at the specified location
     public synchronized AppleseedTreeData GetTree(AppleseedLocation loc)
     {
     	HashMap<AppleseedLocation, AppleseedTreeData> trees = WorldTrees.get(loc.getWorldName());
     	
     	if(trees.containsKey(loc))
     		// the location is the root of a tree
     		return trees.get(loc);
     	else 
     	{
     		// the location is not the root of a tree, scan down to see if they clicked above the root
     		World world = Appleseed.Plugin.getServer().getWorld(loc.getWorldName());
     		if(world == null)
     			return null;
     		
         	Block block = world.getBlockAt(loc.getLocation());
         	int treeCount = 0;
         	while(treeCount < 15 && block.getTypeId() == treeId && !trees.containsKey(block.getLocation()))
         	{
         		Block blockDown = block.getRelative(BlockFace.DOWN);
         		if(blockDown.getTypeId() == treeId)
         			block = blockDown;
         		else
         			break;
         		
         		treeCount++;
         	}
 
         	AppleseedLocation retval = new AppleseedLocation(block.getLocation());
         	if(!trees.containsKey(retval))
         		return null;
         	else
         		return trees.get(retval);
     	}
     }
 
     // for performance reasons calculate the distance squared between the location and all the other trees in the same world 
     public synchronized Boolean IsNewTreeTooClose(Location loc)
     {
     	HashMap<AppleseedLocation, AppleseedTreeData> trees = WorldTrees.get(loc.getWorld().getName());
     	Set<AppleseedLocation> locations = trees.keySet();
     	Iterator<AppleseedLocation> itr = locations.iterator();
     	
     	while(itr.hasNext())
     		if(calcDistanceSquared(itr.next().getLocation(), loc) < (Appleseed.Config.MinimumTreeDistance * Appleseed.Config.MinimumTreeDistance))
     			return true;
     	
     	return false;
     }
     
     // replace the trunk of a tree with air so the leaves disappear
     public synchronized void KillTree(Location loc)
     {
     	if(!WorldTrees.get(loc.getWorld().getName()).containsKey(new AppleseedLocation(loc)))
     		return;
     	
     	World world = loc.getWorld();
     	Block block = world.getBlockAt(loc);
     	
     	if(block.getType() != Material.LOG)
     		return;
     	
     	int i = 0;
     	while(block.getType() == Material.LOG && i < 16)
     	{
     		block.setType(Material.AIR);
     		
     		Block neighbor = block.getRelative(BlockFace.EAST);
     		if(neighbor.getType() == Material.LOG)
     			neighbor.setType(Material.AIR);
 
     		neighbor = block.getRelative(BlockFace.NORTH);
     		if(neighbor.getType() == Material.LOG)
     			neighbor.setType(Material.AIR);
 
     		neighbor = block.getRelative(BlockFace.NORTH_EAST);
     		if(neighbor.getType() == Material.LOG)
     			neighbor.setType(Material.AIR);
 
     		neighbor = block.getRelative(BlockFace.NORTH_WEST);
     		if(neighbor.getType() == Material.LOG)
     			neighbor.setType(Material.AIR);
 
     		neighbor = block.getRelative(BlockFace.SOUTH);
     		if(neighbor.getType() == Material.LOG)
     			neighbor.setType(Material.AIR);
 
     		neighbor = block.getRelative(BlockFace.SOUTH_EAST);
     		if(neighbor.getType() == Material.LOG)
     			neighbor.setType(Material.AIR);
 
     		neighbor = block.getRelative(BlockFace.SOUTH_WEST);
     		if(neighbor.getType() == Material.LOG)
     			neighbor.setType(Material.AIR);
 
     		neighbor = block.getRelative(BlockFace.WEST);
     		if(neighbor.getType() == Material.LOG)
     			neighbor.setType(Material.AIR);
 
     		block = block.getRelative(BlockFace.UP);
     		i++;
     	}
     }
     
     // load trees from disk
     @SuppressWarnings("unchecked")
 	private synchronized void loadTrees()
     {
     	try
     	{
     		// load trees for all loaded worlds
     		List<World> worlds = Appleseed.Plugin.getServer().getWorlds();
     		Iterator<World> worldItr = worlds.iterator();
     		while(worldItr.hasNext())
     			loadTrees(worldItr.next().getName());
     		
     		// load trees from old style trees.yml file 
             File inFile = new File(Appleseed.Plugin.getDataFolder(), "trees.yml");
             if (inFile.exists()){
             	Integer importedCount = 0;
                 Yaml yaml = new Yaml();
                 FileInputStream fis = new FileInputStream(inFile);
                 ArrayList<HashMap<String, Object>> loadData = (ArrayList<HashMap<String, Object>>)yaml.load(fis);
                 
                 for(int i=0; i<loadData.size(); i++)
                 {
                 	HashMap<String, Object> treeHash = loadData.get(i);
 
                 	AppleseedTreeData tree = AppleseedTreeData.LoadFromHash(treeHash);
                 	
                 	if(tree != null)
                 	{
                 		String treeWorld = tree.getWorld();
                 		if(!WorldTrees.containsKey(treeWorld))
                 			WorldTrees.put(treeWorld, new HashMap<AppleseedLocation, AppleseedTreeData>());
                 		
                 		if(!WorldTrees.get(treeWorld).containsKey(tree.getLocation()))
                 		{
                     		WorldTrees.get(treeWorld).put(tree.getLocation(), tree);
                     		importedCount++;
                 		}
                 	}
                 }
                 
                 fis.close();
                 inFile.renameTo(new File(Appleseed.Plugin.getDataFolder(), "trees.yml.old"));
                 saveTrees();
             	System.out.println("Appleseed: Imported " + importedCount.toString() + " trees from trees.yml.");
             }
     	}
     	catch (Exception ex)
     	{
             ex.printStackTrace();
     	}
     }
 
     @SuppressWarnings("unchecked")
 	public void loadTrees(String world)
     // load trees from disk for a specific world
     {
     	HashMap<AppleseedLocation, AppleseedTreeData> trees;
 		if(WorldTrees.containsKey(world))
 			trees = WorldTrees.get(world);
 		else
 		{
 			trees = new HashMap<AppleseedLocation, AppleseedTreeData>();
 			WorldTrees.put(world, trees);
 		}
 
         try {
 			Yaml yaml = new Yaml();
 			File inFile = new File(Appleseed.Plugin.getDataFolder(), "trees-" + world + ".yml");
 			if (inFile.exists()){
 			    FileInputStream fis = new FileInputStream(inFile);
 			    ArrayList<HashMap<String, Object>> loadData = (ArrayList<HashMap<String, Object>>)yaml.load(fis);
 			    
 			    for(int i=0; i<loadData.size(); i++)
 			    {
 			    	HashMap<String, Object> treeHash = loadData.get(i);
 
 			    	AppleseedTreeData tree = AppleseedTreeData.LoadFromHash(treeHash);
 			    	
 			    	if(tree != null)
 			    		if(!trees.containsKey(tree.getLocation()))
 			    			trees.put(tree.getLocation(), tree);
 			    }
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
     	System.out.println("Appleseed: " + ((Integer)trees.size()).toString() + " trees loaded in world " + world + ".");
     }
 
     public Boolean isWorldLoaded(String world)
     {
     	if(WorldTrees.containsKey(world))
     		return true;
     	else
     		return false;
     }
     
     // save trees to disk
     public synchronized void saveTrees()
     {
     	if(SaveRunning)
     		return;
     	
     	SaveRunning = true;
     	
         Iterator<String> worldItr = WorldTrees.keySet().iterator();
         
 		while(worldItr.hasNext())
 		{
 			String world = worldItr.next();
 			HashMap<AppleseedLocation, AppleseedTreeData> trees = WorldTrees.get(world);
 	    	ArrayList<HashMap<String, Object>> saveData = new ArrayList<HashMap<String, Object>>();
 
 	    	Set<AppleseedLocation> locations = trees.keySet();
 	    	Iterator<AppleseedLocation> itr = locations.iterator();
 	    	while(itr.hasNext()){
 	    		AppleseedLocation loc = itr.next();
 	    		saveData.add(trees.get(loc).MakeHashFromTree());
 	    	}
 	    	
 	    	try
 	    	{
 	            Yaml yaml = new Yaml();
 	            File outFile = new File(Appleseed.Plugin.getDataFolder(), "trees-" + world + ".yml");
 	            FileOutputStream fos = new FileOutputStream(outFile);
 	            OutputStreamWriter out = new OutputStreamWriter(fos);
 	            out.write(yaml.dump(saveData));
 	            out.close();
 	            fos.close();
 	    		
 	    	}
 	    	catch (Exception ex)
 	    	{
 	    		ex.printStackTrace();
 	    	}
 		}
 		
     	SaveRunning = false;
     }
 
     // schedule saveTrees() for 10 tics from now (generally .5 sec)
     private synchronized void asyncSaveTrees()
     {
 		Appleseed.Plugin.getServer().getScheduler().scheduleAsyncDelayedTask(Appleseed.Plugin, new Runnable() {
 		    public void run() {
 		    	if(!SaveRunning)
 		    		saveTrees();
 		    }
 		}, 10);
     }
     
     // see if the given location is a tree with a trunk and some leaves
     public final boolean isTree(Location loc)
     {
     	Location rootLoc;
     	if(WorldTrees.get(loc.getWorld().getName()).containsKey(new AppleseedLocation(loc)))
     		rootLoc = loc;
     	else
     	{
     		AppleseedTreeData tree = GetTree(new AppleseedLocation(loc));
     		if(tree == null)
     			return false;
     		else rootLoc = tree.getBukkitLocation();   			
     	}
     	
         final World world = rootLoc.getWorld();
 
         int treeCount = 0;        	
         final int rootX = rootLoc.getBlockX();
         final int rootY = rootLoc.getBlockY();
         final int rootZ = rootLoc.getBlockZ();
        
         final int maxY = 7;
         final int radius = 3;
         
         int leafCount = 0;
 
         if(world.getBlockTypeIdAt(rootLoc) == treeId)
         {
             for (int y = rootY; y <= rootY+maxY; y++) {
                 for (int x = rootX-radius; x <= rootX+radius; x++) {
                     for (int z = rootZ-radius; z <= rootZ+radius; z++) {
                         final int blockId = world.getBlockTypeIdAt(x, y, z);
                         if(blockId == treeId) 
                         	treeCount++;
                         else if(blockId == leafId) 
                         	leafCount++;
 
                         if(treeCount >= 3 && leafCount >= 8)
                         	return true;
                         
                     }
                 }
             }
         }
         return false;
     }
 
     private double calcDistanceSquared(Location loc1, Location loc2)
     {
     	if(loc1.getWorld() != loc2.getWorld())
         	return Double.MAX_VALUE;
     	
     	double dX = loc1.getX() - loc2.getX();
     	double dY = loc1.getY() - loc2.getY();
     	double dZ = loc1.getZ() - loc2.getZ();
 
     	Double retval = (dX*dX) + (dY*dY) + (dZ*dZ);
     	
     	if(retval.isInfinite() || retval.isNaN())
     		return Double.MAX_VALUE;
     	else
     		return retval;
     }
 
     public synchronized void updateSign(AppleseedTreeData tree)
     {
     	if(!tree.hasSign())
     		return;
     	
     	World world = Appleseed.Plugin.getServer().getWorld(tree.getWorld());
     	if(world == null)
     		return;
     	
     	Block block = world.getBlockAt(tree.getSign().getLocation());
     	if(block == null)
     		return;
 
     	Sign sign = null;
     	
     	Boolean signInvalid = false;
     	if(block.getType() != Material.WALL_SIGN)
     		signInvalid = true;
     	else
     	{
     		sign = new CraftSign(block);
     		if(!sign.getLine(0).equals("1[" + Appleseed.Config.SignTag + "]"))
     			signInvalid = true;
     	}
 
     	if(signInvalid || sign == null)
     	{
     		tree.setSign(null);
     		return;
     	}
     	
     	
     	String prefix;
     	if(tree.needsFertilizer())
     		prefix = "c";
     	else if(tree.isInfinite())
     		prefix = "b";
     	else
     		prefix = "a";
 
     	sign.setLine(1, "");
     	sign.setLine(2, prefix + AppleseedItemStack.getItemStackName(tree.getItemStack()));
     	sign.setLine(3, "");
     	
     	sign.update();
     }
     
 	public Integer getPlayerTreeCount(String playerName, String worldName)
 	{
 		Integer retval = 0;
 		Iterator<AppleseedTreeData> itr = WorldTrees.get(worldName).values().iterator();
 		
 		while(itr.hasNext())
 		{
 			AppleseedTreeData tree = itr.next();
 			if(tree.getPlayer().equalsIgnoreCase(playerName))
 				retval++;
 		}
 		
 		return retval;		
 	}
 
 	public Integer getPlayerTreeCount(String playerName)
 	{
 		Integer retval = 0;
 		Iterator<String> itr = WorldTrees.keySet().iterator();
 		
 		while(itr.hasNext())
 		{
 			String world = itr.next();
 			retval += getPlayerTreeCount(playerName, world);
 		}
 			
 		return retval;		
 	}
 
 	public Boolean CanPlayerAddTree(String player, String world)
 	{
 		if(Appleseed.Config.MaxTreesPerPlayer == -1)
 			return true;
 
 		Integer treeCount;
 		if(Appleseed.Config.MaxIsPerWorld)
 			treeCount = getPlayerTreeCount(player, world);
 		else
 			treeCount = getPlayerTreeCount(player);
 
 		if(treeCount < Appleseed.Config.MaxTreesPerPlayer)
 			return true;
 		else
 			return false;
 	}
 	
 }
