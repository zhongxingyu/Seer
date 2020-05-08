 package net.croxis.plugins.civilmineation;
 
 import net.croxis.plugins.civilmineation.components.CityComponent;
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class SignInteractListener implements Listener{
 	@EventHandler
 	public void onBlockInteract(PlayerInteractEvent event){
 		//Left click is click, Right click is cycle
 		//Debug lines to see what the null error is from
 		//error is right clicking bottom block
 		if (event.getClickedBlock() == null){
 			return;
 		}
 		event.getClickedBlock();
 		event.getClickedBlock().getType();
 		if (event.getClickedBlock().getType().equals(Material.WALL_SIGN)
 				|| event.getClickedBlock().getType().equals(Material.SIGN)
 				|| event.getClickedBlock().getType().equals(Material.SIGN_POST)){
 			if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
 				Sign sign = (Sign) event.getClickedBlock().getState();
 				PlotComponent plot = CivAPI.getPlot(event.getClickedBlock().getChunk());
 				if (plot == null)
 					return;
 				if (plot.getCity() == null)
 					return;
 				PlotComponent plotCheck = CivAPI.getPlot(sign);
 				if(plotCheck != null){
 					if(sign.getLine(2).equalsIgnoreCase("=For Sale=")){
 						ResidentComponent resident = CivAPI.getResident(event.getPlayer());
 						double price = 0;
 						try{
 			    			price = Double.parseDouble(sign.getLine(3));
 			    		} catch (NumberFormatException e) {
 			    			event.getPlayer().sendMessage("Bad price value. Try a new [sell] sign.");
 			    			event.setCancelled(true);
 			    			return;
 		    			}
 						//TODO: Add exception for embasee plots
						if (!plot.getCity().getName().equalsIgnoreCase(resident.getName())){
 							event.getPlayer().sendMessage("Not member of city.");
 			    			event.setCancelled(true);
 			    			return;
 						}
 						if (CivAPI.econ.getBalance(event.getPlayer().getName()) < price){
 							event.getPlayer().sendMessage("Not enough money.");
 			    			event.setCancelled(true);
 			    			return;
 						}
 						CivAPI.econ.withdrawPlayer(event.getPlayer().getName(), price);
 						if(plot.getResident() == null){
 							CivAPI.econ.depositPlayer(plot.getCity().getName(), price);
 						} else {
 							CivAPI.econ.depositPlayer(plot.getResident().getName(), price);
 						}
 						plot.setResident(resident);
 						plot.setName(resident.getName());
 						CivAPI.plugin.getDatabase().save(plot);
 						CivAPI.updatePlotSign(plot);
 						return;
 					}
 				}
 				// Immigration
 				CityComponent city = CivAPI.plugin.getDatabase().find(CityComponent.class).where()
 						.eq("charter_x", event.getClickedBlock().getX())
 						.eq("charter_y", event.getClickedBlock().getY()+1)
 						.eq("charter_z", event.getClickedBlock().getZ()).findUnique();
 				if (city == null)
 					return;
 				if (plot.getCity().getName().equalsIgnoreCase(city.getName())){
 					
 					if (sign.getLine(3).contains("Open") && sign.getLine(0).contains("=Demographics=")){
 						ResidentComponent resident = CivAPI.getResident(event.getPlayer());
 						if (CivAPI.addResident(resident, city))
 							CivAPI.broadcastToCity("Welcome " + resident.getName() + " to our city!", city);
 					}
 				}
 				
 				
 			} else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 				if (event.getClickedBlock().getType().equals(Material.WALL_SIGN)){
 					Sign sign = (Sign) event.getClickedBlock().getState();
 					ResidentComponent resident = CivAPI.getResident(event.getPlayer());
 					if (sign.getLine(0).contains("=Demographics=")){
 						Civilmineation.log("Demographics update click");
 						PlotComponent plot = CivAPI.getPlot(event.getClickedBlock().getChunk());
 						
 						if (plot == null){
 							Civilmineation.log("a");
 							return;
 						}
 						if (plot.getCity() == null){
 							Civilmineation.log("b");
 							return;
 						}
 						if (resident.getCity() == null){
 							Civilmineation.log("c");
 							return;
 						}
 						if (!plot.getCity().getName().equalsIgnoreCase(resident.getCity().getName())){
 							Civilmineation.log("d");
 							Civilmineation.log(plot.getCity().getName());
 							Civilmineation.log(resident.getCity().getName());
 							return;
 						}
 						if (resident.isMayor() || resident.isCityAssistant()){
 							Civilmineation.log("e");
 							if (sign.getLine(3).contains("Open")) {
 								sign.setLine(3, ChatColor.RED + "Closed");
 								sign.update();
 							} else {
 								sign.setLine(3, ChatColor.GREEN + "Open");
 								sign.update();
 							}
 						}
 					} else if (sign.getLine(0).contains("City Charter") && (resident.isMayor() || resident.isCityAssistant())){
 						Civilmineation.log("City charter update click");
 						CivAPI.updateCityCharter(CivAPI.getCity(sign.getLocation()));
 					}
 				}
 			}
 		}
 	}
 
 }
