 package net.croxis.plugins.civilmineation;
 
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class SignChangeListener implements Listener{
 	@EventHandler
     public void onSignChangeEvent(SignChangeEvent event){
 		ResidentComponent resident = CivAPI.getResident(event.getPlayer().getName());
 		PlotComponent plot = CivAPI.getPlot(event.getBlock().getChunk());
 		
 		if (event.getLine(0).equalsIgnoreCase("[claim]")){
     		if (!CivAPI.isCityAdmin(resident)){
     			cancelBreak(event, "You must be a city admin");
     		} else if (plot.getCity() != null){
 				cancelBreak(event, "A city has already claimed this chunk");
 			} 
     		
     		PlotComponent p = CivAPI.getPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX() + 1, event.getBlock().getChunk().getZ());
    		if (!CivAPI.isClaimedByCity(p, resident.getCity())){
     			p = CivAPI.getPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX() - 1, event.getBlock().getChunk().getZ());
    			if (!CivAPI.isClaimedByCity(p, resident.getCity())){
     				p = CivAPI.getPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ() + 1);
    				if (!CivAPI.isClaimedByCity(p, resident.getCity())){
     					p = CivAPI.getPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ() + 1);
    					if (!CivAPI.isClaimedByCity(p, resident.getCity())){
     						cancelBreak(event, "This claim must be adjacent to an existing claim.");
     					}
     				}
     			}
     		} else if (resident.getCity().getCulture() < Math.pow(CivAPI.getPlots(resident.getCity()).size(), 1.5)){
 				cancelBreak(event, "You do not have enough culture: " + ChatColor.LIGHT_PURPLE + Integer.toString(resident.getCity().getCulture()) + ChatColor.BLACK + "/" + ChatColor.LIGHT_PURPLE + Double.toString(Math.pow(CivAPI.getPlots(resident.getCity()).size(), 1.5)));
     		}
     		if(event.isCancelled())
     			return;
     		CivAPI.claimPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ(), event.getBlock(), resident.getCity());
 			event.setLine(0, resident.getCity().getName());
 			return;
     	}
 	}
 	
 	public void cancelBreak(SignChangeEvent event, String message){
 		event.setCancelled(true);
 		event.getPlayer().sendMessage(message);
 		event.getBlock().breakNaturally();
 		return;
 	}
 
 }
