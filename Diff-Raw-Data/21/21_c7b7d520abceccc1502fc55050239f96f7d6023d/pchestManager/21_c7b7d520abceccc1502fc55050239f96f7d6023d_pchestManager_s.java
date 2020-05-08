 package nl.rodey.personalchest;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class pchestManager {
 
 	private static Logger log = Logger.getLogger("Minecraft");
 	private final pchestMain plugin;
 	
     public ItemStack[] chestContents=null;
 
 	public pchestManager(pchestMain plugin) {
         this.plugin = plugin;
 	}
 
 	public boolean create(ItemStack[] chestContents, Block block) {
 		String blockWorldName = block.getWorld().getName();
 		
 		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds"+ File.separator + blockWorldName);
 		worldDataFolder.mkdirs();		
 		
		return saveSingleChest(chestContents, block, worldDataFolder);
 
 	}
 	
 	public boolean createPersonal(String playerName, ItemStack[] chestContents, Block block) {
 
 		String blockWorldName = block.getWorld().getName();
 		
 		File personalChestFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Players" + File.separator + playerName + File.separator + "Worlds" + File.separator + blockWorldName);
 		personalChestFolder.mkdirs();
 
 
 		
 		if(!checkDoubleChest(block))
 		{
         	if(plugin.debug)
 			{ 
 				log.info("[PersonalChest] Saved Single Chest");
 			}
 
     		return saveSingleChest(chestContents, block, personalChestFolder);
 		}
 		else
 		{
         	if(plugin.debug)
 			{ 
 				log.info("[PersonalChest] Saved Double Chest");
 			}
         	
         	return saveDoubleChest(chestContents, block, personalChestFolder);
 		}
 	}
 
 	public boolean load(Player player, Chest chest, Block block) {
 
     	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
 		String blockWorldName = block.getWorld().getName();
 		
 		boolean complete = false;
 
 		Inventory newInv = chest.getInventory();
 		// First clear inventory befor loading file
 		newInv.clear();
 		
 		String playerName = player.getName();
 		
 		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
 		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
 		
 		File personalChestFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Players" + File.separator + playerName + File.separator + "Worlds" + File.separator + blockWorldName);
 		personalChestFolder.mkdirs();
 		
 		File personalchestFile = new File(personalChestFolder , blockFilename + ".chest");
 		
 		
 		if (!chestFile.exists())
 		{
 			String data = plugin.pchestWorlds;
 			if(data != null)
 			{
 				
 				//Check if the world is an PersonalChest world
 		        String[] worlds = data.split(",");
 		        
 		        for (String world : worlds)
 		        {
 		        	if(plugin.debug)
 	    			{ 
 	    				log.info("[PersonalChest] World Check: " + blockWorldName + " - " + world);
 	    			}
 		        	
 		        	if(blockWorldName.equalsIgnoreCase(world))
 		        	{
 			        	if(plugin.debug)
 		    			{ 
 		    				log.info("[PersonalChest] World Check: They are Equal");
 		    			}
 			        	
 		                chestContents = newInv.getContents();
 		        		
 		                if(create(chestContents, block))
 		                {
 		        			if(plugin.debug)
 		        			{ 
 		        				log.info("[PersonalChest] Chest Added to list because it is an PersonalChest World");
 		        			}
 		                }
 		                else
 		                {
 		        			if(plugin.debug)
 		        			{ 
 		        				log.info("[PersonalChest] Error occured while creating the new Chest");
 		        			}
 		                }
 		        	}
 		        	else
 		        	{		    			
 		    			//Check if player file is there to delete.
 		    			personalchestFile.delete();
 		    			
 		    			// Punched Chest isn't a PersonalChest
 		    			complete = true;
 		    			
 		    			return complete;
 		        	}
 		        }
 			}
 		}		
 		
 		if (!personalchestFile.exists())
 		{		
 			try {
 				copy(chestFile, personalchestFile);
 				if(plugin.debug)
 				{ 
 					log.info("[PersonalChest] Inventory copied "+ blockFilename+" "+playerName);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		if(!checkDoubleChest(block))
 		{
         	if(plugin.debug)
 			{ 
 				log.info("[PersonalChest] Load Single Chest");
 			}
         	
 			complete = loadSingleChest(newInv, personalchestFile);
 		}
 		else
 		{
         	if(plugin.debug)
 			{ 
 				log.info("[PersonalChest] Load Double Chest");
 			}
 			complete = loadDoubleChest(newInv, personalchestFile, personalChestFolder, block);
 		}
 		
 		return complete;		
 	}
 	
 	private void copy(File source, File target) throws IOException {
 
         InputStream in = new FileInputStream(source);
         OutputStream out = new FileOutputStream(target);
     
         // Copy the bits from instream to outstream
         byte[] buf = new byte[1024];
         int len;
 
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
 
         in.close();
         out.close();
     }
 
 	public boolean remove(Chest chest, String blockFilename, String blockWorldName) {
 		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
 		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
 		
 		// Add the original Items back in the chest
 		Inventory newInv = chest.getInventory();	
 
 		loadSingleChest(newInv, chestFile);		
 		
 		if(chestFile.delete())
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean checkChestRegisterd(Block block)
 	{
 		Chest chest = (Chest) block.getState();
 
 		Inventory newInv = chest.getInventory();
 		
 		String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
 		String blockWorldName = block.getWorld().getName();
 		
 		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName);
 		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
 		
 		if (chestFile.exists())
 		{
 			if(plugin.debug)
 			{ 
 				log.info("[PersonalChest] Chest is Registerd "+ blockFilename);
 			}
 			return true;
 		}
 		else
 		{
 			String data = plugin.pchestWorlds;
 			if(data != null)
 			{
 				
 				//Check if the world is an PersonalChest world
 		        String[] worlds = data.split(",");
 		        
 		        for (String world : worlds)
 		        {
 		        	if(plugin.debug)
 	    			{ 
 	    				log.info("[PersonalChest] World Check: " + blockWorldName + " - " + world);
 	    			}
 		        	
 		        	if(blockWorldName.equalsIgnoreCase(world))
 		        	{
 
 			        	if(plugin.debug)
 		    			{ 
 		    				log.info("[PersonalChest] World Check: They are Equal");
 		    			}
 			        	
 		                chestContents = newInv.getContents();
 		        		
 		                if(create(chestContents, block))
 		                {
 		        			if(plugin.debug)
 		        			{ 
 		        				log.info("[PersonalChest] Chest Added to list because it is an PersonalChest World");
 		        			}
 		                }
 		                else
 		                {
 		        			if(plugin.debug)
 		        			{ 
 		        				log.info("[PersonalChest] Error occured while creating the new Chest");
 		        			}
 		                }
 		                
 		    			return true;
 		        	}
 		        }
 			}
 		}
 		
 		return false;
 	}
 	
 	public String checkOtherChestPosition(Block block, Block block2) {
 		if (block.getRelative(BlockFace.NORTH).getTypeId() == 54) {
 			return "LEFT";
 		} else if (block.getRelative(BlockFace.EAST).getTypeId() == 54) {
 			return "RIGHT";
 		} else if (block.getRelative(BlockFace.SOUTH).getTypeId() == 54) {
 			return "RIGHT";
 		} else if (block.getRelative(BlockFace.WEST).getTypeId() == 54) {
 			return "LEFT";
 		}
 		return "LEFT";
 	}
 	
 	public boolean checkDoubleChest(Block block) {
 		if (block.getRelative(BlockFace.NORTH).getTypeId() == 54) {
 			return true;
 		} else if (block.getRelative(BlockFace.EAST).getTypeId() == 54) {
 			return true;
 		} else if (block.getRelative(BlockFace.SOUTH).getTypeId() == 54) {
 			return true;
 		} else if (block.getRelative(BlockFace.WEST).getTypeId() == 54) {
 			return true;
 		}
 		return false;
 	}
 	
 	public Block getDoubleChest(Block block) {
 		if (block.getRelative(BlockFace.NORTH).getTypeId() == 54) {
 			return block.getRelative(BlockFace.NORTH);
 		} else if (block.getRelative(BlockFace.EAST).getTypeId() == 54) {
 			return block.getRelative(BlockFace.EAST);
 		} else if (block.getRelative(BlockFace.SOUTH).getTypeId() == 54) {
 			return block.getRelative(BlockFace.SOUTH);
 		} else if (block.getRelative(BlockFace.WEST).getTypeId() == 54) {
 			return block.getRelative(BlockFace.WEST);
 		}
 		return null;
 	}
 
 	public boolean checkChestOpened(Block block) 
 	{
 	
     	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
 		String blockWorldName = block.getWorld().getName();
 		
 		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "OPEN");
 		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
 
 		if (chestFile.exists())
 		{
 			try {
 				
 				final BufferedReader in = new BufferedReader(new FileReader(chestFile));
 
 				String line;
 				line = in.readLine();
 
 				Player playerInFile = plugin.getPlayerByString(line);
 				
 				if(playerInFile!=null && playerInFile.isOnline())
 				{
 					if(plugin.debug)
 					{ 
 						log.info("[PersonalChest] " + line + " is Online.");
 					}
 					in.close();
 					return true;
 				}
 				else
 				{
 					if(plugin.debug)
 					{ 
 						log.info("[PersonalChest] " + line + " is Offline.");
 					}
 					in.close();
 					removeChestOpened(block);
 					return false;
 				}
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 		
 		return false;
 	}
 	
 	public void setChestOpened(Block block, Player player) 
 	{
     	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
 		String blockWorldName = block.getWorld().getName();
 
 		String playerName = player.getName();
 		
 		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "OPEN");
 		worldDataFolder.mkdirs();
 		
 		try {
 			File chestFile = new File(worldDataFolder , blockFilename + ".chest");
 			
 			chestFile.createNewFile();
 	
 			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));
 
         	out.write(playerName);
         	
 			out.close();
 	
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		if(checkDoubleChest(block))
 		{
 			Block block2 = getDoubleChest(block);
 
 	    	String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
 	    	
 	    	try {
 				File chestFile2 = new File(worldDataFolder , blockFilename2 + ".chest");
 				
 				chestFile2.createNewFile();
 		
 				final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile2));
 
 	        	out.write(playerName);
 	        	
 				out.close();
 		
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void removeChestOpened(Block block) {
     	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
 		String blockWorldName = block.getWorld().getName();
 		
 		File worldDataFolder = new File(plugin.getDataFolder().getAbsolutePath(), "chests" + File.separator + "Worlds" + File.separator + blockWorldName + File.separator + "OPEN");
 		File chestFile = new File(worldDataFolder , blockFilename + ".chest");
 		
 		chestFile.delete();
 		
 		if(checkDoubleChest(block))
 		{
 			Block block2 = getDoubleChest(block);
 
 	    	String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
 	    	
 			File chestFile2 = new File(worldDataFolder , blockFilename2 + ".chest");
 			
 			chestFile2.delete();
 		}
 	}
 	
 	public boolean saveSingleChest(ItemStack[] chestContents, Block block, File dataFolder)
 	{
     	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
     	
 		try {
 			final File chestFile = new File(dataFolder , blockFilename + ".chest");
 			if (chestFile.exists())
 				chestFile.delete();
 			chestFile.createNewFile();
 
 			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));
 
 			for(int i =0;i<27;i++)
 	        {
 		        if(chestContents[i]!=null)
 		        {
 			        out.write(chestContents[i].getTypeId() + ":" + chestContents[i].getAmount() + ":" + chestContents[i].getDurability() + "\r\n");
 		        }
 		        else
 		        {
 		        	out.write("0:0:0\r\n");
 		        }
 	        }
 			out.close();
 
 			if(plugin.debug)
 			{ 
 				log.info("[PersonalChest] Chest created!");
 			}
 			
 			return true;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			
 			return false;
 		}
 	}
 	
 
 	
 	public boolean saveDoubleChest(ItemStack[] chestContents, Block block, File dataFolder)
 	{
 		Chest chest = (Chest) block.getState();
 		Inventory inv = chest.getInventory();
 		
     	String blockFilename = block.getX()+"_"+block.getY()+"_"+block.getZ();
     	
 		Block block2 = getDoubleChest(block);
 		Chest chest2 = (Chest) block2.getState();
 		Inventory inv2 = chest2.getInventory();
 		
 		String blockFilename2 = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
 
 		int startPos1 = 0;
 		int endPos1 = 27;
 		int startPos2 = 27;
 		int endPos2 = 54;
 		
 		if(checkOtherChestPosition(block, block2) == "RIGHT")
 		{
 			if(plugin.debug)
 			{ 
 				log.info("[PersonalChest] Other Chest is on the RIGHT side");
 			}
 			startPos1 = 27;
 			endPos1 = 54;
 			startPos2 = 0;
 			endPos2 = 27;
 		}
 		else
 		{
 			if(plugin.debug)
 			{ 
 				log.info("[PersonalChest] Other Chest is on the LEFT side");
 			}
 			
 		}
 		
 		try {
 			final File chestFile = new File(dataFolder , blockFilename + ".chest");
 			if (chestFile.exists())
 				chestFile.delete();
 			chestFile.createNewFile();
 
 			final BufferedWriter out = new BufferedWriter(new FileWriter(chestFile));
 
 			for(int i =startPos1;i<endPos1;i++)
 	        {
 		        if(chestContents[i]!=null)
 		        {
 			        out.write(chestContents[i].getTypeId() + ":" + chestContents[i].getAmount() + ":" + chestContents[i].getDurability() + "\r\n");
 		        }
 		        else
 		        {
 		        	out.write("0:0:0\r\n");
 		        }
 	        }
 			out.close();
 			
 			// Save Chest 2
 			final File chestFile2 = new File(dataFolder , blockFilename2 + ".chest");
 			if (chestFile2.exists())
 				chestFile2.delete();
 			chestFile2.createNewFile();
 
 			final BufferedWriter out2 = new BufferedWriter(new FileWriter(chestFile2));
 
 			for(int i=startPos2;i<endPos2;i++)
 	        {
 		        if(chestContents[i]!=null)
 		        {
 			        out2.write(chestContents[i].getTypeId() + ":" + chestContents[i].getAmount() + ":" + chestContents[i].getDurability() + "\r\n");
 		        }
 		        else
 		        {
 		        	out2.write("0:0:0\r\n");
 		        }
 	        }
 			out2.close();
 
 			if(plugin.debug)
 			{ 
 				log.info("[PersonalChest] Chest created!");
 			}
 			
 			//Clear inventory
 			inv.clear();
 			inv2.clear();
 			
 			return true;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			
 			return false;
 		}
 	}
 	
 	public boolean loadSingleChest(Inventory inv, File chestFile)
 	{		
 		try {
 			
 			final BufferedReader in = new BufferedReader(new FileReader(chestFile));
 
 			String line;
 			int field = 0;
 			while ((line = in.readLine()) != null) {
 				if (line != "") {
 					final String[] parts = line.split(":");
 					try {
 						int type = Integer.parseInt(parts[0]);
 						int amount = Integer.parseInt(parts[1]);
 						short damage = Short.parseShort(parts[2]);
 						if (type != 0) {
 							inv.setItem(field, new ItemStack(type, amount, damage));
 						}
 					} catch (NumberFormatException e) {
 						// ignore
 					}
 				}
 				field++;
 			}
 
 			in.close();
 			
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean loadDoubleChest(Inventory inv, File chestFile, File dataFolder, Block block)
 	{
 		Block block2 = getDoubleChest(block);
 
 		String blockFilename = block2.getX()+"_"+block2.getY()+"_"+block2.getZ();
 		
 		File chestFile2 = new File(dataFolder , blockFilename + ".chest");
 	
 
 		Chest chest = (Chest) block2.getState();
 		Inventory inv2 = chest.getInventory();
 		
 		inv.clear();
 		inv2.clear();
 		
 		try {
 			
 			final BufferedReader in = new BufferedReader(new FileReader(chestFile));
 
 			String line;
 			int field = 0;
 			while ((line = in.readLine()) != null) {
 				if (line != "") {
 					final String[] parts = line.split(":");
 					try {
 						int type = Integer.parseInt(parts[0]);
 						int amount = Integer.parseInt(parts[1]);
 						short damage = Short.parseShort(parts[2]);
 						if (type != 0) {
 							inv.setItem(field, new ItemStack(type, amount, damage));
 						}
 					} catch (NumberFormatException e) {
 						// ignore
 					}
 				}
 				field++;
 			}
 
 			in.close();
 			
 			final BufferedReader in2 = new BufferedReader(new FileReader(chestFile2));
 
 			String line2;
 			int field2 = 0;
 			while ((line2 = in2.readLine()) != null) {
 				if (line2 != "") {
 					final String[] parts = line2.split(":");
 					try {
 						int type = Integer.parseInt(parts[0]);
 						int amount = Integer.parseInt(parts[1]);
 						short damage = Short.parseShort(parts[2]);
 						if (type != 0) {
 							inv2.setItem(field2, new ItemStack(type, amount, damage));
 						}
 					} catch (NumberFormatException e) {
 						// ignore
 					}
 				}
 				field2++;
 			}
 
 			in2.close();
 			
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 }
