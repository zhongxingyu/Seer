 package net.croxis.plugins.civilmineation;
 
 import java.util.HashSet;
 
 import net.croxis.plugins.civilmineation.components.CityComponent;
 import net.croxis.plugins.civilmineation.components.CivComponent;
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
 		if (event.getLine(0).equalsIgnoreCase("[New Civ]")){
 			CivComponent civComponent = CivAPI.getCiv(event.getLine(1));
 			CityComponent cityComponent = CivAPI.getCity(event.getLine(2));
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
 			CivComponent civ = CivAPI.createCiv(event.getLine(1));
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
 			
     	} else if (event.getLine(0).equalsIgnoreCase("[new city]")){
     		if (CivAPI.isClaimed(plot)){
     			cancelBreak(event, "This plot is claimed");
     		}  else if (event.getLine(1).isEmpty() || event.getLine(2).isEmpty()){
     			cancelBreak(event, "City name on second line, Mayor name on third line");
     		} else if (!CivAPI.isCivAdmin(resident)){
     			cancelBreak(event, "You must be a national leader or assistant.");
     		}
     		ResidentComponent mayor = CivAPI.getResident(event.getLine(2));
     		CityComponent cityComponent = CivAPI.getCity(event.getLine(1));
     		if (mayor == null){
     			cancelBreak(event, "That player does not exist.");
     		} else if (cityComponent != null){
 				cancelBreak(event, "That city name already exists");
 			}	else if (mayor.getCity() == null){
 				cancelBreak(event, "That player must be in your civ!.");
 			} else if (!mayor.getCity().getCivilization().getName().equalsIgnoreCase(resident.getCity().getCivilization().getName())){
 				cancelBreak(event, "That player must be in your civ!.");
 			} else if (mayor.isMayor()){
 				cancelBreak(event, "That player can not be an existing mayor.");
 			} else if (plot.getCity() != null){
 				cancelBreak(event, "That plot is part of a city.");
 			} else if (resident.getCity().getCulture() < 50){
 				cancelBreak(event, "Not enough culture to found a city.");
 			}
 			if (event.isCancelled())
     			return;
 			resident.getCity().setCulture(resident.getCity().getCulture() - 50);
 			CivAPI.save(resident.getCity());
 			
 			CityComponent city = CivAPI.createCity(event.getLine(1), event.getPlayer(), mayor, event.getBlock(), mayor.getCity().getCivilization(), false);
 			SignComponent signComp = CivAPI.createSign(event.getBlock(), city.getName() + " charter", SignType.CITY_CHARTER, city.getEntityID());
 			CivAPI.claimPlot(event.getBlock().getWorld().getName(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ(), city.getName() + " Founding Square", event.getBlock().getRelative(BlockFace.UP), mayor.getCity());
 			event.getBlock().getRelative(BlockFace.UP).setTypeIdAndData(68, signComp.getRotation(), true);
 			event.setLine(0, ChatColor.BLUE + "City Charter");
 			event.setLine(1, city.getCivilization().getName());
 			event.setLine(2, city.getName());
 			event.setLine(3, "Mayor " + mayor.getName());
 			event.getBlock().getRelative(BlockFace.DOWN).setTypeIdAndData(68, signComp.getRotation(), true);
 			//event.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setTypeIdAndData(68, rotation, true);
 			CivAPI.updateCityCharter(city);
 			CivAPI.broadcastToCiv("The city of " + city.getName() + " has been founded!", mayor.getCity().getCivilization());
     	} else if (event.getLine(0).equalsIgnoreCase("[city charter]")){
     		 if (!CivAPI.isCityAdmin(resident)){
      			cancelBreak(event, "You must be a city administrator.");
     		} else if (!CivAPI.isClaimedByCity(plot, resident.getCity())){
     			cancelBreak(event, "This plot is not part of your city");
     		}  
     		CityComponent city = resident.getCity();
     		if (event.isCancelled())
     			return;
     		CivAPI.plugin.getDatabase().delete(CivAPI.plugin.getDatabase().find(SignComponent.class).where().eq("type", SignType.CITY_CHARTER).eq("entityID", resident.getCity().getEntityID()).findUnique());
     		CivAPI.plugin.getDatabase().delete(CivAPI.plugin.getDatabase().find(SignComponent.class).where().eq("type", SignType.CITY_CHARTER_CULTURE).eq("entityID", resident.getCity().getEntityID()).findUnique());
     		CivAPI.plugin.getDatabase().delete(CivAPI.plugin.getDatabase().find(SignComponent.class).where().eq("type", SignType.CITY_CHARTER_MONEY).eq("entityID", resident.getCity().getEntityID()).findUnique());
     		CivAPI.plugin.getDatabase().delete(CivAPI.plugin.getDatabase().find(SignComponent.class).where().eq("type", SignType.CITY_PERM_CIV_BUILD).eq("entityID", resident.getCity().getEntityID()).findUnique());
     		CivAPI.plugin.getDatabase().delete(CivAPI.plugin.getDatabase().find(SignComponent.class).where().eq("type", SignType.CITY_PERM_OUT_BUILD).eq("entityID", resident.getCity().getEntityID()).findUnique());
     		CivAPI.plugin.getDatabase().delete(CivAPI.plugin.getDatabase().find(SignComponent.class).where().eq("type", SignType.CITY_PERM_RES_BUILD).eq("entityID", resident.getCity().getEntityID()).findUnique());
     		CivAPI.plugin.getDatabase().delete(CivAPI.plugin.getDatabase().find(SignComponent.class).where().eq("type", SignType.DEMOGRAPHICS).eq("entityID", resident.getCity().getEntityID()).findUnique());
     		SignComponent signComp = CivAPI.createSign(event.getBlock(), city.getName() + " charter", SignType.CITY_CHARTER, city.getEntityID());
 			event.getBlock().getRelative(BlockFace.UP).setTypeIdAndData(68, signComp.getRotation(), true);
 			event.setLine(0, ChatColor.BLUE + "City Charter");
 			event.setLine(1, city.getCivilization().getName());
 			event.setLine(2, city.getName());
 			event.setLine(3, "Mayor " + CivAPI.getMayor(city).getName());
 			event.getBlock().getRelative(BlockFace.DOWN).setTypeIdAndData(68, signComp.getRotation(), true);
 			//event.getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setTypeIdAndData(68, rotation, true);
 			CivAPI.updateCityCharter(city);
 			event.getPlayer().sendMessage("Charter moved successfully. Please manually break old charter signs.");
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
     		build(event, resident, plot);
     	} else if (event.getLine(0).equalsIgnoreCase("[civ]")){
     		event.getBlock().breakNaturally();
     		if (!CivAPI.isCivAdmin(resident)){
     			cancelBreak(event, "You must be a civ administrator!");
     		}
     		if (event.isCancelled())
     			return;
     		CivComponent civ = resident.getCity().getCivilization();
     		if (event.getLine(1).equalsIgnoreCase("[color]") || event.getLine(1).equalsIgnoreCase("[colour]") ){
     			ChatColor color = ChatColor.YELLOW;
     			try{
     			color = ChatColor.valueOf(event.getLine(2).toUpperCase());
     			} catch (IllegalArgumentException e){
     				cancelBreak(event, "Invalid color");
     				return;
     			}
     			civ.setChatcolor(color.getChar());
     			CivAPI.save(civ);
     		} else if (event.getLine(1).equalsIgnoreCase("[assist]")){
     			ResidentComponent assistant = CivAPI.getResident(event.getLine(2));
         		if (event.getLine(2).isEmpty()){
         			cancelBreak(event, "Assistant name on the third line.");
         		} else if (!CivAPI.isKing(resident)){
         			cancelBreak(event, "You must be a king.");
         		} else if (assistant == null){
         			cancelBreak(event, "That player does not exist.");
         		} else if (assistant.getCity() == null){
     				cancelBreak(event, "That player must be in your civ!.");
     			} else if (!resident.getCity().getCivilization().getName().equalsIgnoreCase(assistant.getCity().getCivilization().getName())){
     				cancelBreak(event, "That player must be in your civ!.");
     			}
         		if (event.isCancelled())
         			return;
     			assistant.setCivAssistant(!assistant.isCivAssistant());
     			CivAPI.save(assistant);
     			if(assistant.isCivAssistant())
     				CivAPI.broadcastToCiv(assistant.getName() + " is now a civ assistant!", resident.getCity().getCivilization());
     			else
     				CivAPI.broadcastToCiv(assistant.getName() + " is no longer a civ assistant!", resident.getCity().getCivilization());
     			return;
         	} else if (event.getLine(1).equalsIgnoreCase("[tag]")){
         		if (event.getLine(2).isEmpty())
         			cancelBreak(event, "Tag name on the third line.");
         		if (event.isCancelled())
         			return;
         		civ.setTag(event.getLine(2));
         		CivAPI.save(civ);
         	} else if (event.getLine(1).equalsIgnoreCase("[rename]")) {
         		if (event.getLine(2).isEmpty())
         			cancelBreak(event, "New name on the third line.");
         		else if (!CivAPI.isKing(resident))
         			cancelBreak(event, "You must be a king.");
         		else if (CivAPI.getCiv(event.getLine(2)) != null)
         			cancelBreak(event, "That civ already exists.");
         		if (event.isCancelled())
         			return;
         		CivAPI.setName(event.getLine(2), civ);
         	}
     	} else if (event.getLine(0).equalsIgnoreCase("[city]")){
     		event.getBlock().breakNaturally();
     		if (!CivAPI.isCityAdmin(resident)){
     			cancelBreak(event, "You must be a city administrator!");
     		}
     		if (event.isCancelled())
     			return;
     		CityComponent city = resident.getCity();
     		if (event.getLine(1).equalsIgnoreCase("[color]") || event.getLine(1).equalsIgnoreCase("[colour]") ){
     			ChatColor color = ChatColor.YELLOW;
     			try{
     			color = ChatColor.valueOf(event.getLine(2).toUpperCase().trim().replace(" ", "_"));
     			} catch (IllegalArgumentException e){
     				cancelBreak(event, "Invalid color");
     				return;
     			}
     			city.setChatcolor(color.getChar());
     			CivAPI.save(city);
     		} else if (event.getLine(1).equalsIgnoreCase("[assist]")){
         		ResidentComponent assistant = CivAPI.getResident(event.getLine(2));
         		event.getBlock().breakNaturally();
         		if (event.getLine(2).isEmpty())
         			cancelBreak(event, "Assistant name on the second line.");
         		else if (!resident.isMayor())
         			cancelBreak(event, "You must be a mayor.");
         		else if (assistant == null)
         			cancelBreak(event, "That player does not exist.");
         		else if (assistant.getCity() == null)
     				cancelBreak(event, "That player must be in your city!.");
     			else if (!resident.getCity().getName().equalsIgnoreCase(assistant.getCity().getName()))
     				cancelBreak(event, "That player must be in your city!.");
         		if (event.isCancelled())
         			return;
     			assistant.setCityAssistant(!assistant.isCityAssistant());
     			CivAPI.save(assistant);
     			if(assistant.isCityAssistant())
     				CivAPI.broadcastToCity(assistant.getName() + " is now a city assistant!", resident.getCity());
     			else
     				CivAPI.broadcastToCity(assistant.getName() + " is no longer a city assistant!", resident.getCity());
     			return;
         	} else if (event.getLine(1).equalsIgnoreCase("[tag]")){
         		if (event.getLine(2).isEmpty())
         			cancelBreak(event, "Tag name on the third line.");
         		if (event.isCancelled())
         			return;
         		city.setTag(event.getLine(2));
         		CivAPI.save(city);
         	} else if (event.getLine(1).equalsIgnoreCase("[rename]")) {
         		if (event.getLine(2).isEmpty())
         			cancelBreak(event, "New name on the third line.");
         		else if (!resident.isMayor())
         			cancelBreak(event, "You must be mayor.");
         		else if (CivAPI.getCity(event.getLine(2)) != null)
         			cancelBreak(event, "That city already exists.");
         		if (event.isCancelled())
         			return;
         		CivAPI.setName(event.getLine(2), city);
         	}
     	}
 	}
 	
 	private void build(SignChangeEvent event, ResidentComponent resident, PlotComponent plot){
 		event.getBlock().breakNaturally();
 		if (!CivAPI.isClaimed(plot)){
 			cancelBreak(event, "This plot is unclaimed");
 		} else if (!CivAPI.isCityAdmin(resident)){
 			cancelBreak(event, "You are not a city admin or plot owner");
 		} else if (!plot.getCity().getName().equalsIgnoreCase(resident.getCity().getName())){
 			cancelBreak(event, "This is not your city");
 		} else if (event.getLine(1).isEmpty()){
 			cancelBreak(event, "Invalid building type");
 		} else if (!plot.getType().equals(CityPlotType.RESIDENTIAL)){
 			cancelBreak(event, "Can not change existing building");
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
 			if (CivAPI.plugin.getDatabase().find(PlotComponent.class).where().eq("city", resident.getCity()).eq("type", CityPlotType.LIBRARY).findList().isEmpty()){
 				event.getPlayer().sendMessage("A library is needed first.");
     			event.setCancelled(true);
     			return;
 			}
 		} else if (type == CityPlotType.EMBASSY){
 			cost = 20;
 			tech = "Writing";
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
 		} else if (type.equals(CityPlotType.EMBASSY)){
 			if (CivAPI.econ.getBalance(plot.getCity().getName()) < cost){
 	    		event.getPlayer().sendMessage("Insufficant funds");
 				event.setCancelled(true);
 				return;
     		}
 			CivAPI.econ.withdrawPlayer(plot.getCity().getName(), cost);
 		}
 		plot.setType(type);
 		if (plot.getResident() == null)
 			plot.setName(plot.getCity().getName() + " " + type.toString());
 		else
 			if (Bukkit.getServer().getPlayer(plot.getResident().getName()).isOnline())
 				plot.setName(ChatColor.GREEN + plot.getResident().getName() + " " + type.toString());
 			else
 				plot.setName(ChatColor.RED + plot.getResident().getName() + " " + type.toString());
 		event.getPlayer().sendMessage(type.toString().toLowerCase() + " creation successful");
 		CivAPI.save(plot);
 	}
 	
 	public void cancelBreak(SignChangeEvent event, String message){
 		event.setCancelled(true);
 		event.getPlayer().sendMessage(message);
 		event.getBlock().breakNaturally();
 		return;
 	}
 
 }
