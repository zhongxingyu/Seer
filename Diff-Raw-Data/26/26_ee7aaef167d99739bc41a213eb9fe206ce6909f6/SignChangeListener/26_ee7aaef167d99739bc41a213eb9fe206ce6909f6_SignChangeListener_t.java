 package net.croxis.plugins.civilmineation;
 
 import java.util.HashSet;
 
 import net.croxis.plugins.civilmineation.components.CityComponent;
 import net.croxis.plugins.civilmineation.components.CivilizationComponent;
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 import net.croxis.plugins.civilmineation.components.SignComponent;
 import net.croxis.plugins.research.TechManager;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class SignChangeListener implements Listener{
 	@EventHandler
     public void onSignChangeEvent(SignChangeEvent event){
 		ResidentComponent resident = CivAPI.getResident(event.getPlayer().getName());
 		PlotComponent plot = CivAPI.getPlot(event.getBlock().getChunk());
 		CivilizationComponent civComponent = CivAPI.plugin.getDatabase().find(CivilizationComponent.class).where().ieq("name", event.getLine(1)).findUnique();
 		CityComponent cityComponent = CivAPI.plugin.getDatabase().find(CityComponent.class).where().ieq("name", event.getLine(2)).findUnique();
 		if (event.getLine(0).equalsIgnoreCase("[New Civ]")){
     		if (CivAPI.isClaimed(plot)){
     			cancelBreak(event, "This plot is claimed");
     		} else if (event.getLine(1).isEmpty() || event.getLine(2).isEmpty()){
     			cancelBreak(event, "Civ name on second line, Capital name on third line");
     		} else if (civComponent != null || cityComponent != null){
 				cancelBreak(event, "That civ or city name already exists");
 			} else if (resident.getCity() != null){
 				cancelBreak(event, "You must leave your city first.");
 			} else if (plot.getCity() != null){
 				cancelBreak(event, "That plot is part of a city.");
 			}
     		if (event.isCancelled())
     			return;
 			//TODO: Distance check to another city
 			//TODO: Check for room for interface placements
 			CivilizationComponent civ = CivAPI.createCiv(event.getLine(1));
 	    	ResidentComponent mayor = CivAPI.getResident(event.getPlayer());
 	    	CityComponent city = CivAPI.createCity(event.getLine(2), event.getPlayer(), mayor, event.getBlock(), civ, true);
 	    	SignComponent signComp = CivAPI.createSign(event.getBlock(), city.getName() + " charter", SignType.CITY_CHARTER, city.getEntityID());
 	    	event.getBlock().getRelative(BlockFace.UP).setTypeIdAndData(68, signComp.getRotation(), true);
 			Sign plotSign = (Sign) event.getBlock().getRelative(BlockFace.UP).getState();
 	    	CivAPI.claimPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ(), city.getName() + " Founding Square", event.getBlock().getRelative(BlockFace.UP), city);
 			plotSign.setLine(0, city.getName());
 			plotSign.update();
 			
 			event.setLine(0, ChatColor.DARK_AQUA + "City Charter");
 			event.setLine(3, "Mayor " + event.getPlayer().getName());
 			event.getBlock().getRelative(BlockFace.DOWN).setTypeIdAndData(68, signComp.getRotation(), true);
 			//event.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setTypeIdAndData(68, rotation, true);
 			CivAPI.updateCityCharter(city);	
 			CivAPI.plugin.getServer().broadcastMessage("A new civilization has been founded!");
 			
     	} else if (event.getLine(0).equalsIgnoreCase("[claim]")){
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
     					p = CivAPI.getPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ() - 1);
     					if (!CivAPI.isClaimedByCity(p, resident.getCity())){
     						cancelBreak(event, "This claim must be adjacent to an existing claim.");
     					}
     				}
     			}
     		} 
     		Civilmineation.logDebug("You do not have enough culture: " + ChatColor.LIGHT_PURPLE + Integer.toString(resident.getCity().getCulture()) + ChatColor.BLACK + "/" + ChatColor.LIGHT_PURPLE + Double.toString(Math.pow(CivAPI.getPlots(resident.getCity()).size(), 1.5)));
     		if (resident.getCity().getCulture() < Math.pow(CivAPI.getPlots(resident.getCity()).size(), 1.5)){
 				cancelBreak(event, "You do not have enough culture: " + ChatColor.LIGHT_PURPLE + Integer.toString(resident.getCity().getCulture()) + ChatColor.BLACK + "/" + ChatColor.LIGHT_PURPLE + Double.toString(Math.pow(CivAPI.getPlots(resident.getCity()).size(), 1.5)));
     		}
     		if(event.isCancelled())
     			return;
     		CivAPI.claimPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ(), event.getBlock(), resident.getCity());
 			event.setLine(0, resident.getCity().getName());
 			return;
     	} else if (event.getLine(0).equalsIgnoreCase("[build]")) {
     		event.getBlock().breakNaturally();
     		if (!CivAPI.isClaimed(plot)){
     			cancelBreak(event, "This plot is unclaimed");
     		} else if (!CivAPI.isCityAdmin(resident)){
     			cancelBreak(event, "You are not a city admin or plot owner");
     		} else if (!plot.getCity().getName().equalsIgnoreCase(resident.getCity().getName())){
     			cancelBreak(event, "This is not your city");
     		} else if (event.getLine(1).isEmpty()){
     			cancelBreak(event, "Invalid building type");
     		}			
 			if (event.isCancelled())
 				return;
     		CityPlotType type = CityPlotType.valueOf(event.getLine(1).toUpperCase());
     		double cost = 0;
     		double value = 0;
     		HashSet<Integer> ids = new HashSet<Integer>();
     		String tech = "";
     		if (type == null){
     			event.getPlayer().sendMessage("Invalid building type");
     			event.setCancelled(true);
     			return;
     		}
     		if (type == CityPlotType.LIBRARY){
 				cost = 25;
 				tech = "Writing";
 				ids.add(47);
 			} else if (type == CityPlotType.UNIVERSITY){
 				cost = 55;
 				tech = "Education";
 				ids.add(47);
 			}
 
 			if (!TechManager.hasTech(CivAPI.getMayor(resident).getName(), tech)){
     			event.getPlayer().sendMessage("You need " + tech);
 				event.setCancelled(true);
 				return;
 			}
 			
     		if (type.equals(CityPlotType.LIBRARY) || type.equals(CityPlotType.UNIVERSITY)){
     			long time = System.currentTimeMillis();
     			ChunkSnapshot chunkShot = event.getBlock().getChunk().getChunkSnapshot();
     			for (int x=0; x<16; x++){
     				if (value >= cost)
     					break;
     				for (int z=0; z<16; z++){
     					if (value >= cost)
     						break;
     					for (int y=16; y<Bukkit.getServer().getWorld("world").getMaxHeight()/2; y++){
     						if (value >= cost)
     							break;
     						if (chunkShot.getBlockTypeId(x, y, z) == 47)
     							value++;
     					}
     				}
     			}
     			Civilmineation.logDebug("Library scan time: " + Long.toString(System.currentTimeMillis() - time));    			
     		}
     		
     		if (type.equals(CityPlotType.LIBRARY) || type.equals(CityPlotType.UNIVERSITY)){
     			if (value < cost){
     				event.getPlayer().sendMessage("Insufficant books: " + Double.toString(value) + "/" + Double.toString(cost));
     				event.setCancelled(true);
     				return;
     			}    				
     		} /*else if (5 == 6){//TODO: This is for finance based construction
     			if (CivAPI.econ.getBalance(plot.getCity().getName()) < cost){
     	    		event.getPlayer().sendMessage("Insufficant funds");
     				event.setCancelled(true);
     				return;
         		}
     			CivAPI.econ.withdrawPlayer(plot.getCity().getName(), cost);
     		}*/
     		plot.setType(type);
     		if (plot.getResident() == null)
     			plot.setName(plot.getCity().getName() + " " + type.toString());
     		else
     			if (Bukkit.getServer().getPlayer(plot.getResident().getName()).isOnline())
     				plot.setName(ChatColor.GREEN + plot.getResident().getName() + " " + type.toString());
     			else
     				plot.setName(ChatColor.RED + plot.getResident().getName() + " " + type.toString());
     		event.getPlayer().sendMessage("Library creation successful");
     		CivAPI.save(plot);
     	} else if (event.getLine(0).equalsIgnoreCase("[civ]")){
     		event.getBlock().breakNaturally();
     		if (!CivAPI.isCivAdmin(resident)){
     			cancelBreak(event, "You must be a civ administrator!");
     		}
     		if (event.isCancelled())
     			return;
    		if (event.getLine(1).equalsIgnoreCase("[color]") || event.getLine(1).equalsIgnoreCase("[colour]") ){
    			ChatColor color = ChatColor.YELLOW;
    			try{
    			color = ChatColor.valueOf(event.getLine(2).toUpperCase());
    			} catch (IllegalArgumentException e){
     				cancelBreak(event, "Invalid color");
     				return;
     			}
     			CivilizationComponent civ = resident.getCity().getCivilization();
     			civ.setChatcolor(color);
     			CivAPI.save(civ);
     		}
     	} else if (event.getLine(0).equalsIgnoreCase("[city]")){
     		event.getBlock().breakNaturally();
     		if (!CivAPI.isCityAdmin(resident)){
     			cancelBreak(event, "You must be a city administrator!");
     		}
     		if (event.isCancelled())
     			return;
    		if (event.getLine(1).equalsIgnoreCase("[color]") || event.getLine(1).equalsIgnoreCase("[colour]") ){
    			ChatColor color = ChatColor.YELLOW;
    			try{
    			color = ChatColor.valueOf(event.getLine(2).toUpperCase());
    			} catch (IllegalArgumentException e){
     				cancelBreak(event, "Invalid color");
     				return;
     			}
     			CityComponent city = resident.getCity();
     			city.setChatcolor(color);
     			CivAPI.save(city);
     		}
     		
     	}
 	}
 	
 	public void cancelBreak(SignChangeEvent event, String message){
 		event.setCancelled(true);
 		event.getPlayer().sendMessage(message);
 		event.getBlock().breakNaturally();
 		return;
 	}
 
 }
