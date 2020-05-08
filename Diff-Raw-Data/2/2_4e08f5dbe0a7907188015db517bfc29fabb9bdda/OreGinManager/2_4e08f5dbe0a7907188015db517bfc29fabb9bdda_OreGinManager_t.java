 package com.github.MrTwiggy.OreGin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 
 
 /**
  * OreGinManager.java
  * Purpose: Manages the maintenance, creation, and destruction of OreGins
  *
  * @author MrTwiggy
  * @version 0.1 1/08/13
  */
 public class OreGinManager implements Listener
 {
 	
 	List<OreGin> oreGins; //List of current OreGins
 	public OreGinPlugin plugin; //OreGinPlugin object
 	
 	/**
 	 * Constructor
 	 */
 	public OreGinManager(OreGinPlugin plugin)
 	{
 		this.plugin = plugin;
 		oreGins = new ArrayList<OreGin>();
 		
 		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 		    @Override  
 		    public void run() {
 		        UpdateOreGins();
 		    }
 		}, 0L, OreGinPlugin.UPDATE_CYCLE);
 	}
 	
 	/**
 	 * Updates all the OreGins
 	 */
 	public void UpdateOreGins()
 	{
 		for (OreGin oreGin : oreGins)
 		{
 			oreGin.Update();
 		}
 	}
 	
 	/**
 	 * Attempts to create an OreGin at the location
 	 */
 	public boolean CreateOreGin(Location machineLocation)
 	{
 		//Add logic for attempting to create an OreGin
 		if (!OreGinExistsAt(machineLocation) && OreGin.ValidUpgrade(machineLocation, 1))
 		{
 			oreGins.add(new OreGin(machineLocation));
 			plugin.getLogger().info("New OreGin created!");
 			return true;
 		}
 			
 		return false;
 	}
 	
 	/**
 	 * Attempts to create an OreGin of given OreGin data
 	 */
 	public boolean CreateOreGin(OreGin oreGin)
 	{
 		if(oreGin.GetLocation().getBlock().getType().equals(Material.DISPENSER) && !OreGinExistsAt(oreGin.GetLocation()))
 		{
 			oreGins.add(oreGin);
 			plugin.getLogger().info("New OreGin created!");
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * Returns the OreGin with a matching Location, if any
 	 */
 	public OreGin GetOreGin(Location machineLocation)
 	{
 		for (OreGin oreGin : oreGins)
 		{
 			if (oreGin.GetLocation().equals(machineLocation))
 				return oreGin;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Returns whether an OreGin exists at the given Location
 	 */
 	public boolean OreGinExistsAt(Location machineLocation)
 	{
 		return (GetOreGin(machineLocation) != null);
 	}
 	
 	/**
 	 * Returns whether item is an OreGin
 	 */
 	public boolean IsOreGin(ItemStack item)
 	{
 		boolean result = false;
 		
 		if (item.getItemMeta().getDisplayName() == null)
 			return false;
 		
		for(int i = 1; i <= OreGinPlugin.MAX_TIERS; i++)
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
 	public boolean OreGinLightExistsAt(Location lightLocation)
 	{
 		return (OreGinExistsAt(lightLocation.getBlock().getRelative(BlockFace.DOWN).getLocation())
 				&& (lightLocation.getBlock().getType().equals(OreGinPlugin.LIGHT_OFF) 
 						|| lightLocation.getBlock().getType().equals(OreGinPlugin.LIGHT_ON)));
 	}
 	
 }
