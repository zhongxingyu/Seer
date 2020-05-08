 package com.craftminecraft.plugins.bansync.plugins;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.plugin.Plugin;
 
 import com.craftminecraft.plugins.bansync.BanSync;
 import com.craftminecraft.plugins.bansync.log.LogLevels;
 import com.worldcretornica.plotme.Plot;
 import com.worldcretornica.plotme.PlotManager;
 import com.worldcretornica.plotme.PlotMe;
 import com.worldcretornica.plotme.SqlManager;
 
 
 public class PlotMePluginHook {
 	private BanSync bansyncinterface = null;
 	private Boolean pluginHooked = false;
 	
 	public PlotMePluginHook (BanSync p) {
 		bansyncinterface = p;
 	}
 	
 	public Boolean isHooked()
 	{
 		return pluginHooked;
 	}
 	
 	public Boolean HookPlotMe()
 	{
 		Plugin p = bansyncinterface.getServer().getPluginManager().getPlugin("PlotMe");
 	    if (p != null && p instanceof PlotMe) {
 	    	bansyncinterface.logger.log(LogLevels.INFO, "PlotMe was found, hooked into PlotMe");
 	    	pluginHooked = true;
 	    	return true;
 	    } else {
 	    	pluginHooked = false;
 	    	bansyncinterface.logger.log(LogLevels.INFO, "PlotMe not Found");
 	    	return false;
 	    }	
 	}
 	
 	public void ClearPlotMePlots(String playerName)
 	{
 		bansyncinterface.logger.log(LogLevels.INFO, "Removing PlotMe Plots");
 		
 		List<World> worlds = bansyncinterface.getServer().getWorlds();
 		
 		for (World w : worlds) {
			HashMap<String, Plot> plots = new HashMap<String, Plot>();
			plots = PlotManager.getPlots(w);
 			if (!plots.equals(null))
 			{
 			if (plots.size() > 0)
 			{
 				for (String id : plots.keySet())
 				{
 					Plot plot = plots.get(id);
 					bansyncinterface.logger.log("[DEBUG] Found Plot " + plot.id + " Owner: " + plot.owner);
 					
 					if (plot.owner.equalsIgnoreCase(playerName))
 					{
 						bansyncinterface.logger.log(LogLevels.INFO, "Found plot " + plot.id + ", Removing it");
 						String plotID = plot.id;
 						
 						Location bottom = PlotManager.getPlotBottomLoc(w, plotID);
 						Location top = PlotManager.getPlotTopLoc(w, plotID);
 						PlotManager.clear(bottom, top);
 						
 						PlotManager.removeOwnerSign(w, plotID);
 						PlotManager.removeSellSign(w, plotID);
 						
 						SqlManager.deletePlot(PlotManager.getIdX(plotID), PlotManager.getIdZ(plotID), w.getName().toLowerCase());
 					}
 				}	
 			}
 			}
 		}
 	}
 }
