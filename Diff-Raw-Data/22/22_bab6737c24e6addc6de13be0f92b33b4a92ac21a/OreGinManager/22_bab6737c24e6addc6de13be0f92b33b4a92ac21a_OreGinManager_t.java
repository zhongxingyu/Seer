 package com.github.MrTwiggy.MachineFactory.Managers;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
 import com.github.MrTwiggy.MachineFactory.Interfaces.Machine;
 import com.github.MrTwiggy.MachineFactory.Interfaces.Manager;
 import com.github.MrTwiggy.MachineFactory.Machines.OreGin;
 import com.github.MrTwiggy.MachineFactory.Properties.OreGinProperties;
 import com.github.MrTwiggy.MachineFactory.SoundCollections.OreGinSoundCollection;
 import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;
 import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse.InteractionResult;
 
 
 /**
  * OreGinManager.java
  * Purpose: Manages the maintenance, creation, and destruction of OreGins
  *
  * @author MrTwiggy
  * @version 0.1 1/08/13
  */
 public class OreGinManager implements Manager
 {
 	
 	private List<OreGin> oreGins; //List of current OreGins
 	private MachineFactoryPlugin plugin; //OreGinPlugin object
 	
 	private int blockBreaksDuringCycle = 0;//The number of blocks broken during the current cycle.
 	
 	/**
 	 * Constructor
 	 */
 	public OreGinManager(MachineFactoryPlugin plugin)
 	{
 		this.plugin = plugin;
 		oreGins = new ArrayList<OreGin>();
 		
 		updateMachines();
 	}
 
 	/**
 	 * Updates all the active OreGins
 	 */
 	public void updateMachines()
 	{
 		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
 		{
 		    @Override  
 		    public void run() 
 		    {
 		    	blockBreaksDuringCycle = 0;
 		    	for (OreGin oreGin : oreGins)
 				{
 					oreGin.update();
 				}
 		    }
 		}, 0L, MachineFactoryPlugin.OREGIN_UPDATE_CYCLE);
 	}
 	
 	/**
 	 * 'blockBreaksDuringCycle' public accessor
 	 */
 	public int getBlockBreaksDuringCycle()
 	{
 		return blockBreaksDuringCycle;
 	}
 	
 	/**
 	 * Increments the blockBreaksDuringCycle member by 1.
 	 */
 	public void incrementBlockBreaksDuringCycle()
 	{
 		blockBreaksDuringCycle++;
 	}
 	
 	/**
 	 * Load OreGins from file
 	 */
 	public void load(File file) throws IOException 
 	{
 		FileInputStream fileInputStream = new FileInputStream(file);
 		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
 
 		String line;
 		while ((line = bufferedReader.readLine()) != null) 
 		{
 			String parts[] = line.split(" ");
 			//ORDER world loc_x loc_y loc_z tier_level block_breaks mining_distance mining broken
 			
 			Location oreGinLocation = new Location(Bukkit.getWorld(parts[0]), Integer.parseInt(parts[1]),
 								Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));	
 			int tierLevel = Integer.parseInt(parts[4]);
 			int blockBreaks = Integer.parseInt(parts[5]);
 			int miningDistance = Integer.parseInt(parts[6]);
 			boolean mining = Boolean.parseBoolean(parts[7]);
 			boolean broken = Boolean.parseBoolean(parts[8]);
 			
			if (oreGinLocation.getBlock().getType().equals(Material.DISPENSER))
			{
				OreGin oreGin = new OreGin(blockBreaks, tierLevel, mining, broken, miningDistance, oreGinLocation, this);
				addMachine(oreGin);
			}
 		}
 
 		MachineFactoryPlugin.sendConsoleMessage("Successfully loaded " + oreGins.size() + " Ore Gins!");
 		fileInputStream.close();
 	}
 
 	/**
 	 * Save OreGins to file
 	 */
 	public void save(File file) throws IOException {
 		FileOutputStream fileOutputStream = new FileOutputStream(file);
 		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
 		
 		for (OreGin oreGin : oreGins) 
 		{
 			//ORDER world loc_x loc_y loc_z tier_level block_breaks mining_distance mining broken
 			Location location = oreGin.getLocation();
 			bufferedWriter.append(location.getWorld().getName());
 			bufferedWriter.append(" ");
 			bufferedWriter.append(Integer.toString(location.getBlockX()));
 			bufferedWriter.append(" ");
 			bufferedWriter.append(Integer.toString(location.getBlockY()));
 			bufferedWriter.append(" ");
 			bufferedWriter.append(Integer.toString(location.getBlockZ()));
             bufferedWriter.append(" ");
             bufferedWriter.append(Integer.toString(oreGin.getTierLevel()));
             bufferedWriter.append(" ");
             bufferedWriter.append(Integer.toString(oreGin.getBlockBreaks()));
             bufferedWriter.append(" ");
             bufferedWriter.append(Integer.toString(oreGin.getMiningDistance()));
             bufferedWriter.append(" ");
             bufferedWriter.append(Boolean.toString(oreGin.getMining()));
             bufferedWriter.append(" ");
             bufferedWriter.append(Boolean.toString(oreGin.getBroken()));
             bufferedWriter.append("\n");
             
         }
 		
 		MachineFactoryPlugin.sendConsoleMessage("Successfully saved " + oreGins.size() + " Ore Gins!");
 
 		bufferedWriter.flush();
 		fileOutputStream.close();
 	}
 
 	/**
 	 * Attempts to create an OreGin at the location and returns result message
 	 */
 	public InteractionResponse createMachine(Location machineLocation)
 	{
 		OreGinProperties desiredTierProperties = MachineFactoryPlugin.Ore_Gin_Properties.get(1);
 		Material upgradeMaterial = desiredTierProperties.getUpgradeMaterial();
 		
 		if (OreGin.isValidOreGinCreationLocation(machineLocation))
 		{
 			if (!machineExistsAt(machineLocation))
 			{
 				OreGin oreGin = new OreGin(machineLocation, this);
 				if (oreGin.isValidUpgrade(1))
 				{
 					addMachine(oreGin);
 					oreGin.removeUpgradeMaterial(1);
 					
 					return new InteractionResponse(InteractionResult.SUCCESS,
 							"Successfully created OreGin!");
 				}
 				else
 				{
 					return new InteractionResponse(InteractionResult.FAILURE,
 							"Unsuccessful in creating OreGin!");
 				}
 			}
 			else
 			{
 				OreGinSoundCollection.getErrorSound().playSound(machineLocation);
 				
 				return new InteractionResponse(InteractionResult.FAILURE,
 						"Missing creation materials! " 
 						+ OreGin.getRequiredAvailableMaterials(desiredTierProperties.getUpgradeAmount(),
 						upgradeMaterial, machineLocation));
 			}
 		}
 		else
 		{
 			OreGinSoundCollection.getErrorSound().playSound(machineLocation);
 			
 			return new InteractionResponse(InteractionResult.FAILURE,
 					"Space above OreGin must be empty!");
 		}
 	}
 	
 	/**
 	 * Attempts to create an OreGin of given OreGin data
 	 */
 	public InteractionResponse addMachine(Machine machine)
 	{
 		OreGin oreGin = (OreGin)machine;
 		if(oreGin.getLocation().getBlock().getType().equals(Material.DISPENSER) && !machineExistsAt(oreGin.getLocation()))
 		{
 			oreGins.add(oreGin);
 			return new InteractionResponse(InteractionResult.SUCCESS, "");
 		}
 		else
 		{
 			return new InteractionResponse(InteractionResult.FAILURE, "");
 		}
 	}
 	
 	/**
 	 * Returns the OreGin with a matching Location, if any
 	 */
 	public Machine getMachine(Location machineLocation)
 	{
 		for (OreGin oreGin : oreGins)
 		{
 			if (oreGin.getLocation().equals(machineLocation))
 				return oreGin;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Removes a specific OreGin from the list
 	 */
 	public void removeMachine(Machine machine)
 	{
 		oreGins.remove((OreGin)machine);
 	}
 	
 	/**
 	 * Returns whether an OreGin exists at the given Location
 	 */
 	public boolean machineExistsAt(Location machineLocation)
 	{
 		return ((getMachine(machineLocation) != null) || oreGinLightExistsAt(machineLocation));
 	}
 	
 	/**
 	 * Returns whether item is an OreGin
 	 */
 	public boolean isOreGin(ItemStack item)
 	{
 		boolean result = false;
 		
 		if (item.getItemMeta().getDisplayName() == null)
 			return false;
 		
 		for(int i = 1; i <= MachineFactoryPlugin.MAX_OREGIN_TIERS; i++)
 		{
 			if (item.getItemMeta().getDisplayName().equalsIgnoreCase("T" + i + " OreGin"))
 			{
 				result = true;
 				break;
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * Returns whether location contains an OreGin light
 	 */
 	public boolean oreGinLightExistsAt(Location lightLocation)
 	{
 		return (getMachine(lightLocation.getBlock().getRelative(BlockFace.DOWN).getLocation()) != null
 				&& (lightLocation.getBlock().getType().equals(MachineFactoryPlugin.LIGHT_OFF) 
 						|| lightLocation.getBlock().getType().equals(MachineFactoryPlugin.LIGHT_ON)));
 	}
 
 	/**
 	 * Returns the OreGinSaves file name
 	 */
 	public String getSavesFileName()
 	{
 		return MachineFactoryPlugin.ORE_GIN_SAVES_FILE;
 	}
 }
