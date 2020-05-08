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
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
 import com.github.MrTwiggy.MachineFactory.Interfaces.Machine;
 import com.github.MrTwiggy.MachineFactory.Interfaces.Manager;
 import com.github.MrTwiggy.MachineFactory.Machines.Cloaker;
 import com.github.MrTwiggy.MachineFactory.SoundCollections.CloakerSoundCollection;
 import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;
 import com.github.MrTwiggy.MachineFactory.Utility.InventoryStringDeSerializer;
 import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse.InteractionResult;
 
 /**
  * CloakerManager.java
  * Purpose: Manages the creation, destruction, and maintenence of Cloaker objects
  *
  * @author MrTwiggy
  * @version 0.1 1/17/13
  */
 public class CloakerManager implements Manager
 {
 	
 	private List<Cloaker> cloakers;
 	private MachineFactoryPlugin plugin;
 	
 	/**
 	 * Constructor
 	 */
 	public CloakerManager(MachineFactoryPlugin plugin)
 	{
 		this.plugin = plugin;
 		cloakers = new ArrayList<Cloaker>();
 		
 		updateMachines();
 	}
 
 	/**
 	 * Saves all Cloaker objects to file
 	 */
 	public void save(File file) throws IOException 
 	{
 		FileOutputStream fileOutputStream = new FileOutputStream(file);
 		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
 		
 		for (Cloaker cloaker : cloakers) 
 		{
 			//ORDER world loc_x loc_y loc_z tier_level active cloaked_duration INVENTORY
 			Location location = cloaker.getLocation();
 			bufferedWriter.append(location.getWorld().getName());
 			bufferedWriter.append(" ");
 			bufferedWriter.append(Integer.toString(location.getBlockX()));
 			bufferedWriter.append(" ");
 			bufferedWriter.append(Integer.toString(location.getBlockY()));
 			bufferedWriter.append(" ");
 			bufferedWriter.append(Integer.toString(location.getBlockZ()));
             bufferedWriter.append(" ");
             bufferedWriter.append(Integer.toString(cloaker.getTierLevel()));
             bufferedWriter.append(" ");
             bufferedWriter.append(Boolean.toString(cloaker.getActive()));
             bufferedWriter.append(" ");
             bufferedWriter.append(Double.toString(cloaker.getCloakedDuration()));
             bufferedWriter.append(" ");
             bufferedWriter.append(InventoryStringDeSerializer.InventoryToString(cloaker.getInventory()));
             bufferedWriter.append("\n");
             
         }
 		
 		MachineFactoryPlugin.sendConsoleMessage("Successfully saved " + cloakers.size() + " Cloakers!");
 
 		bufferedWriter.flush();
 		fileOutputStream.close();
 	}
 
 	/**
 	 * Loads all Cloaker objects from file
 	 */
 	public void load(File file) throws IOException 
 	{
 		FileInputStream fileInputStream = new FileInputStream(file);
 		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
 
 		String line;
 		while ((line = bufferedReader.readLine()) != null) 
 		{
 			String parts[] = line.split(" ");
 			//ORDER world loc_x loc_y loc_z tier_level active cloaked_duration INVENTORY
 			
 			Location cloakerLocation = new Location(Bukkit.getWorld(parts[0]), Integer.parseInt(parts[1]),
 								Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));	
 			int tierLevel = Integer.parseInt(parts[4]);
 			boolean active = Boolean.parseBoolean(parts[5]);
 			double cloaked_duration = Double.parseDouble(parts[6]);
 			Inventory cloaker_inventory = InventoryStringDeSerializer.StringToInventory(parts[7]);
			
			if (Cloaker.isCloakerType(cloakerLocation.getBlock().getType()))
			{
				Cloaker cloaker = new Cloaker(tierLevel, active, cloaked_duration, cloaker_inventory,
						cloakerLocation);
				addMachine(cloaker);
			}
 		}
 
 		MachineFactoryPlugin.sendConsoleMessage("Successfully loaded " + cloakers.size() + " Cloakers!");
 		fileInputStream.close();
 	}
 
 	/**
 	 * Updates all Cloaker machines
 	 */
 	@Override
 	public void updateMachines()
 	{
 		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
 		{
 		    @Override  
 		    public void run() 
 		    {
 		    	for (Cloaker cloaker : cloakers)
 				{
 					cloaker.update();
 				}
 		    }
 		}, 0L, MachineFactoryPlugin.CLOAKER_UPDATE_CYCLE);
 	}
 
 	/**
 	 * Attempts to create a Cloaker at given location
 	 */
 	public InteractionResponse createMachine(Location machineLocation) 
 	{
 		Cloaker cloaker = new Cloaker(machineLocation);
 		if (cloaker.createCloaker())
 		{
 			cloakers.add(cloaker);
 			return new InteractionResponse(InteractionResult.SUCCESS,
 					"Successfully created Cloaker!");
 		}
 		else
 		{
 			CloakerSoundCollection.getErrorSound().playSound(machineLocation);
 			return new InteractionResponse(InteractionResult.FAILURE,
 					"Missing the required upgrade materials from chest above Cloaker to create!");
 		}
 	}
 
 	/**
 	 * Attempts to add an existing Cloaker
 	 */
 	public InteractionResponse addMachine(Machine machine)
 	{
 		if (!machineExistsAt(machine.getLocation()))
 		{
 			cloakers.add((Cloaker)machine);
 			return new InteractionResponse(InteractionResult.SUCCESS,
 					"A Cloaker of tier level " + ((Cloaker)machine).getTierLevel() + " has been placed!");
 		}
 		else
 		{
 			return new InteractionResponse(InteractionResult.FAILURE,
 					"Unable to create Cloaker here!");
 		}
 	}
 
 	/**
 	 * Returns Cloaker at given location, if one exists.
 	 */
 	public Machine getMachine(Location machineLocation) 
 	{
 		for (Cloaker cloaker : cloakers)
 		{
 			if (cloaker.getLocation().equals(machineLocation))
 				return cloaker;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Returns whether a Cloaker exists at given location
 	 */
 	public boolean machineExistsAt(Location machineLocation)
 	{
 		return (getMachine(machineLocation) != null);
 	}
 	
 	/**
 	 * Returns whether item is an OreGin
 	 */
 	public boolean isCloaker(ItemStack item)
 	{
 		boolean result = false;
 		
 		if (item.getItemMeta().getDisplayName() == null)
 			return false;
 		
 		for(int i = 1; i <= MachineFactoryPlugin.MAX_OREGIN_TIERS; i++)
 		{
 			if (item.getItemMeta().getDisplayName().equalsIgnoreCase(Cloaker.CloakerName(i)))
 			{
 				result = true;
 				break;
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * Removes given Cloaker
 	 */
 	public void removeMachine(Machine machine) 
 	{
 		Cloaker cloaker = (Cloaker)machine;
 		cloakers.remove(cloaker);
 	}
 	
 	/**
 	 * Returns the Cloaker Saves file name
 	 */
 	public String getSavesFileName()
 	{
 		return MachineFactoryPlugin.CLOAKER_SAVES_FILE;
 	}
 
 }
