 package net.croxis.plugins.civilmineation;
 
 import net.croxis.plugins.civilmineation.components.PlotComponent;
 import net.croxis.plugins.civilmineation.components.ResidentComponent;
 import net.croxis.plugins.civilmineation.components.SignComponent;
 import net.croxis.plugins.research.TechManager;
 
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
 				SignComponent signComp = CivAPI.getSign(event.getClickedBlock());
				Civilmineation.logDebug("a");
 				if (plot.getCity() == null || signComp == null)
					return;
				Civilmineation.logDebug("b");
 				if(signComp.getType() == SignType.PLOT_INFO){
					Civilmineation.logDebug("c");
 					if(sign.getLine(2).equalsIgnoreCase("=For Sale=")){
						Civilmineation.logDebug("d");
 						ResidentComponent resident = CivAPI.getResident(event.getPlayer());
 						double price = 0;
 						try{
 			    			price = Double.parseDouble(sign.getLine(3));
 			    		} catch (NumberFormatException e) {
 			    			event.getPlayer().sendMessage("Bad price value. Try a new [sell] sign.");
 			    			event.setCancelled(true);
 			    			return;
 		    			}
 						if (!TechManager.hasTech(event.getPlayer().getName(), "Currency")){
 							event.getPlayer().sendMessage("You need to learn currency before being able to buy.");
 			    			event.setCancelled(true);
 			    			return;
 						}
 						//TODO: Add exception for embasee plots
 						if (!plot.getCity().getName().equalsIgnoreCase(resident.getCity().getName())){
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
 
 						sign.setLine(2, "");
 						sign.setLine(3, "");
 						sign.update();
 						CivAPI.updatePlotSign(plot);
 						return;
 					}
 				} else if (signComp.getType() == SignType.DEMOGRAPHICS){					
 					if (sign.getLine(3).contains("Open")){
 						ResidentComponent resident = CivAPI.getResident(event.getPlayer());
 						if (CivAPI.addResident(resident, plot.getCity()))
 							CivAPI.broadcastToCity("Welcome " + resident.getName() + " to our city!", plot.getCity());
 					}
 				}
 				
 			} else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 				if (event.getClickedBlock().getType().equals(Material.WALL_SIGN)){
 					Sign sign = (Sign) event.getClickedBlock().getState();
 					ResidentComponent resident = CivAPI.getResident(event.getPlayer());
 					SignComponent signComp = CivAPI.getSign(event.getClickedBlock());
 					if (signComp == null)
 						return;
 					if (signComp.getType() == SignType.DEMOGRAPHICS){
 						Civilmineation.logDebug("Demographics update click");
 						PlotComponent plot = CivAPI.getPlot(event.getClickedBlock().getChunk());
 						if (plot.getCity() == null){
 							Civilmineation.logDebug("b");
 							return;
 						}
 						if (resident.getCity() == null){
 							Civilmineation.logDebug("c");
 							return;
 						}
 						if (!plot.getCity().getName().equalsIgnoreCase(resident.getCity().getName())){
 							Civilmineation.logDebug("d");
 							Civilmineation.log(plot.getCity().getName());
 							Civilmineation.log(resident.getCity().getName());
 							return;
 						}
 						if (resident.isMayor() || resident.isCityAssistant()){
 							if (sign.getLine(3).contains("Open")) {
 								sign.setLine(3, ChatColor.RED + "Closed");
 								sign.update();
 							} else {
 								sign.setLine(3, ChatColor.GREEN + "Open");
 								sign.update();
 							}
 						}
 					} else if (signComp.getType() == SignType.CITY_CHARTER && (resident.isMayor() || resident.isCityAssistant())){
 						CivAPI.updateCityCharter(CivAPI.getCity(signComp.getEntityID()));
 					}
 				}
 			}
 		}
 	}
 
 }
