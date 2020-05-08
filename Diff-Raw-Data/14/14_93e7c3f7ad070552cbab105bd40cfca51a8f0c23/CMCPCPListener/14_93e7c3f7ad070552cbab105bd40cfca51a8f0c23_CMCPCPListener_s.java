 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 public class CMCPCPListener extends PluginListener {
 	Server server = etc.getServer();
 	Logger log = Logger.getLogger("Minecraft");
 	private CMCPCPprops props = new CMCPCPprops();
 	private int CoffeePotSize = props.getCOFFEEPOTSIZE();
 	private int CoffeeDirty = props.getCOFFEEDIRTY();
 	private int CoffeeBrewDelay = props.getCOFFEEBREWDELAY() * 1000 * 60;
 	private boolean hungergive = props.getGIVEHUNGER();
 	private int CoffeeLeft;
 	private boolean bip;
 	protected Timer brew;
 	
 	public boolean onCommand(Player player, String[] split) {
 		if (split[0].equalsIgnoreCase("/cmcpcp")){
 			if (split.length == 1){
				player.sendMessage("6[CMCPCP] eCanary Mod Coffee Pot Control Protocol v2.0");
 				if (CMCPCP.isLatest()){
 					player.sendMessage("6[CMCPCP] bSoftware is UP TO DATE");
 					return true;
 				}else{
 					player.sendMessage("6[CMCPCP] bYour software needs an upgrade to ev" + CMCPCP.currver);
 					return true;
 				}
 			}else if (player.canUseCommand("/CMCPCP")){
 				if (split[1].equalsIgnoreCase("BREW")){
 					if (CoffeeLeft == 0){
 						if (!bip){
 							if (CoffeeDirty >= 5) server.messageAll("6[CMCPCP] cThe coffee pot is dirty and needs to be cleaned!");
 							brew = new Timer();
 							server.messageAll("6[CMCPCP] bCoffee is now being brewed!");
 							log.info("[CMCPCP] Coffee is now being brewed!");
 							bip = true;
 							brew.schedule(new Brewing(), CoffeeBrewDelay);
 						}else{
 							player.sendMessage("6[CMCPCP] cCoffee is being brewed already!");
 						}
 						return true;
 					}else{
 						player.sendMessage("6[CMCPCP] cCoffee has already been brewed!");
 						return true;
 					}
 				}else if (split[1].equalsIgnoreCase("GET")){
 					if (CoffeeLeft > 0){
 						if (CoffeeDirty < 5){
 							if (hungergive){
 								if (player.getFoodLevel() < 20){
 									int foodlevel = player.getFoodLevel() + 1;
 									player.sendMessage("6[CMCPCP] bHere's e1b cup of coffee");
 									player.setFoodLevel(foodlevel);
 									CoffeeLeft -= 1;
 									return true;
 								}else{
 									player.sendMessage("6[CMCPCP] cCoffee saturation is maxed!");
 									player.sendMessage("6[CMCPCP] cYou can't drink anymore coffee!");
 									return true;
 								}
 							}else{
 								if (player.getHealth() < 20){
 									player.sendMessage("6[CMCPCP] bHere's 1 Cup-o-Coffee");
 									player.increaseHealth(1);
 									CoffeeLeft -= 1;
 									return true;
 								}else{
 									player.sendMessage("6[CMCPCP] cYour coffee saturation is maxed!");
 									player.sendMessage("6[CMCPCP] cYou can't drink anymore coffee!");
 									return true;
 								}
 							}
 						}else{
 							if (hungergive){
 								int foodlevel = player.getFoodLevel() - 1;
 								player.setFoodLevel(foodlevel);
 								player.sendMessage("6[CMCPCP] cThe coffee pot is dirty and needs to be cleaned!");
 								return true;
 							}else{
 								player.increaseHealth(-1);
 								player.sendMessage("6[CMCPCP] cThe coffee pot is dirty and needs to be cleaned!");
 								return true;
 							}
 						}
 					}else if (bip){
 						player.sendMessage("6[CMCPCP] cCoffee brewing is in progress!");
 						return true;
 					}else{
 						player.sendMessage("6[CMCPCP] bSorry the coffee pot is cempty.");
 						return true;
 					}
 				}else if (split[1].equalsIgnoreCase("CHECK")){
 					if (CoffeeLeft > 80%CoffeePotSize){
 						player.sendMessage("6[CMCPCP] bCups left = 2" + String.valueOf(CoffeeLeft));
 						return true;
 					}else if (CoffeeLeft > 60%CoffeePotSize){
 						player.sendMessage("6[CMCPCP] bCups left = 9" + String.valueOf(CoffeeLeft));
 						return true;
 					}else if (CoffeeLeft > 40%CoffeePotSize){
 						player.sendMessage("6[CMCPCP] bCups left = e" + String.valueOf(CoffeeLeft));
 						return true;
 					}else if (CoffeeLeft > 20%CoffeePotSize){
 						player.sendMessage("6[CMCPCP] bCups left = 6" + String.valueOf(CoffeeLeft));
 						return true;
 					}else if (CoffeeLeft > 0%CoffeePotSize){
 						player.sendMessage("6[CMCPCP] bCups left = c" + String.valueOf(CoffeeLeft));
 						return true;
 					}else if (CoffeeLeft == 0){
 						player.sendMessage("6[CMCPCP] bThe coffee pot is 4EMPTY");
 						return true;
 					}
 				}else if (split[1].equalsIgnoreCase("CLEAN")){
 					player.sendMessage("6[CMCPCP] bCoffee Pot cleaned!");
 					CoffeeLeft = 0;
 					CoffeeDirty = 0;
 					return true;
 				}else{
 					player.sendMessage("6[CMCPCP] cERROR 400: BAD REQUEST");
 					player.sendMessage("6[CMCPCP] bUsage: /CMCPCP <BREW|GET|CHECK>");
 					return true;
 				}
 			}else{
 				player.sendMessage("6[CMCPCP] cERROR 418: I'M A TEAPOT");
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void killTask(){
 	    brew.cancel();
         brew.purge();
 	}
 	
 	private class Brewing extends TimerTask {
 		public Brewing(){ }
 		
 		public void run(){
 			server.messageAll("6[CMCPCP] 2Coffee is done brewing!");
 			log.info("[CMCPCP] Coffee is done brewing!");
 			CoffeeDirty += 1;
 			props.setCOFFEEPOTDIRTY(CoffeeDirty + 1);
 			CoffeeLeft = CoffeePotSize;
 			bip = false;
 		}
 	}
 }
 
 /*******************************************************************************\
 * CanaryMod Coffee Pot Control Protocol v2.x                                    *
 * Copyright (C) 2012 Visual Illusions Entertainment                             *
 * @author darkdiplomat <darkdiplomat@visualillusionsent.net>                    *
 *                                                                               *
 * This file is part of CanaryMod Coffee Pot Control Protocol                    *
 *                                                                               *
 * This program is free software: you can redistribute it and/or modify          *
 * it under the terms of the GNU General Public License as published by          *
 * the Free Software Foundation, either version 3 of the License, or             *
 * (at your option) any later version.                                           *
 *                                                                               *
 * This program is distributed in the hope that it will be useful,               *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                          *
 *                                                                               *
 * You should have received a copy of the GNU General Public License             *
 * along with this program.  If not, see http://www.gnu.org/licenses/gpl.html    *
 *                                                                               *
 \*******************************************************************************/
