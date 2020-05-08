 package de.hotmail.gurkilein.bankcraft;
 
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 public class MinecraftPlayerListener implements Listener{
 
 	
 	private Bankcraft bankcraft;
 	private ConfigurationHandler coHa;
 
 	public MinecraftPlayerListener(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 		this.coHa = bankcraft.getConfigurationHandler();
 	}
 	
 	@EventHandler
 	public void onLogin(PlayerLoginEvent event) {
 		bankcraft.getDebitorHandler().updateDebitorStatus(event.getPlayer());
 		if (bankcraft.getDebitorHandler().isCurrentlyDebitor(event.getPlayer())) {
 			if (Boolean.getBoolean(bankcraft.getConfigurationHandler().getString("general.remindDebitorOnLogin"))) {
 				bankcraft.getConfigurationHandler().printMessage(event.getPlayer(), "message.debitor", "0", event.getPlayer().getName());
 			}
 		}
 	}
 
 	@EventHandler
 	public void onklick(PlayerInteractEvent event) throws Exception {
 		Player p = event.getPlayer();
 		if (event.getClickedBlock() != null) {
			if (event.getClickedBlock().getType().equals(Material.WALL_SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST)) {
 				if (((Sign) event.getClickedBlock().getState()).getLine(0).contains("[Bank]")) {
 					if (!p.isSneaking()) {
 						Integer type = bankcraft.getSignDatabaseInterface().getType(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ(), event.getClickedBlock().getWorld());
 						if (type == -1) {
 							if (Bankcraft.perms.has(p, "bankcraft.admin")) {
 								p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Reinitializing Bankcraftsign...");
 								Sign sign = (Sign) event.getClickedBlock().getState();
 								if (((sign.getLine(1).equalsIgnoreCase(coHa.getString("signAndCommand.deposit")) | sign.getLine(1).equalsIgnoreCase(coHa.getString("signAndCommand.exchange")) | sign.getLine(1).equalsIgnoreCase(coHa.getString("signAndCommand.exchangexp")) | sign.getLine(1).equalsIgnoreCase(coHa.getString("signAndCommand.withdraw")) | sign.getLine(1).equalsIgnoreCase(coHa.getString("signAndCommand.withdrawxp")) | sign.getLine(1).equalsIgnoreCase(coHa.getString("signAndCommand.depositxp"))) && (Util.isPositive(sign.getLine(2))) || sign.getLine(2).equalsIgnoreCase("all")) == true) {
 									//ERSTELLEN DER BANK
 									sign.setLine(0, coHa.getString("signAndCommand.signColor") + "[Bank]");
 									double betrag = 0;
 									String typreihe = sign.getLine(1);
 									int signX = sign.getBlock().getX();
 									int signY = sign.getBlock().getY();
 									int signZ = sign.getBlock().getZ();
 									
 									if (typreihe.equalsIgnoreCase(coHa.getString("signAndCommand.deposit"))) {
 										if (sign.getLine(2).equalsIgnoreCase("all")) {
 											sign.setLine(2, "All");
 											betrag = -1;
 										} else {
 											betrag = new Double(sign.getLine(2));
 										}
 										type = 1;
 									}
 									if (typreihe.equalsIgnoreCase(coHa.getString("signAndCommand.withdraw"))) {
 										if (sign.getLine(2).equalsIgnoreCase("all")) {
 											sign.setLine(2, "All");
 											betrag = -1;
 										} else {
 											betrag = new Double(sign.getLine(2));
 										}
 										type = 2;
 									}
 
 									if (typreihe.equalsIgnoreCase(coHa.getString("signAndCommand.depositxp"))) {
 										if (sign.getLine(2).equalsIgnoreCase("all")) {
 											sign.setLine(2, "All");
 											betrag = -1;
 										} else {
 											betrag = new Double(sign.getLine(2));
 										}
 										type = 6;
 									}
 									if (typreihe.equalsIgnoreCase(coHa.getString("signAndCommand.withdrawxp"))) {
 										if (sign.getLine(2).equalsIgnoreCase("all")) {
 											sign.setLine(2, "All");
 											betrag = -1;
 										} else {
 											betrag = new Double(sign.getLine(2));
 										}
 										type = 7;
 									}
 									if (typreihe.equalsIgnoreCase(coHa.getString("signAndCommand.exchange"))) {
 										if (sign.getLine(2).equalsIgnoreCase("all")) {
 											sign.setLine(2, "All");
 											betrag = -1;
 										} else {
 											betrag = new Double(sign.getLine(2));
 										}
 										type = 12;
 									}
 									if (typreihe.equalsIgnoreCase(coHa.getString("signAndCommand.exchangexp"))) {
 										if (sign.getLine(2).equalsIgnoreCase("all")) {
 											sign.setLine(2, "All");
 											betrag = -1;
 										} else {
 											betrag = new Double(sign.getLine(2));
 										}
 										type = 13;
 									}
 									bankcraft.getSignDatabaseInterface().createNewSign(signX, signY, signZ, sign.getBlock().getWorld(), type, betrag+"");
 									
 									coHa.printMessage(p, "message.createdSignSuccessfully", "0", p.getName());
 
 								} else {
 									if (sign.getLine(1).equalsIgnoreCase(coHa.getString("signAndCommand.balance")) | (sign.getLine(1).equalsIgnoreCase(coHa.getString("signAndCommand.balancexp")))) {
 										sign.setLine(0, coHa.getString("signAndCommand.signColor") + "[Bank]");
 										int signX = sign.getBlock().getX();
 										int signY = sign.getBlock().getY();
 										int signZ = sign.getBlock().getZ();
 										if (sign.getLine(1).equals(coHa.getString("signAndCommand.balancexp"))) {
 											if (sign.getLine(2).isEmpty()) {
 												bankcraft.getSignDatabaseInterface().createNewSign(signX, signY, signZ, sign.getBlock().getWorld(), 5, "0");
 											} else {
 												bankcraft.getSignDatabaseInterface().createNewSign(signX, signY, signZ, sign.getBlock().getWorld(), 11, "0");
 											}
 										} else {
 											if (sign.getLine(2).isEmpty()) {
 												bankcraft.getSignDatabaseInterface().createNewSign(signX, signY, signZ, sign.getBlock().getWorld(), 0, "0");
 											} else {
 												bankcraft.getSignDatabaseInterface().createNewSign(signX, signY, signZ, sign.getBlock().getWorld(), 10, "0");
 											}
 										}
 										coHa.printMessage(p, "message.createdSignSuccessfully", "0", p.getName());
 									} else {
 										p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Could not reinitialize the sign! Removing it now!");
 										event.getClickedBlock().setType(Material.AIR);
 									}
 								}
 							} else {
 								p.sendMessage(coHa.getString("chat.color") + coHa.getString("chat.prefix") + "Sign not in Database! Please contact an admin!");
 							}
 							return;
 						}
 						if (Bankcraft.perms.has(p, "bankcraft.use") | (Bankcraft.perms.has(p, "bankcraft.use.money") && (type == 0 | type == 1 | type == 2 | type == 3 | type == 4)) | (Bankcraft.perms.has(p, "bankcraft.use.exp") && (type == 5 | type == 6 | type == 7 | type == 8 | type == 9))) {
 							if ((event.getAction() == Action.RIGHT_CLICK_BLOCK && !bankcraft.getConfigurationHandler().getString("general.swapClicks").equalsIgnoreCase("false")) || (event.getAction() == Action.LEFT_CLICK_BLOCK && !bankcraft.getConfigurationHandler().getString("general.swapClicks").equalsIgnoreCase("true"))) {
 								
 								//Update scrolling signs
 								if (type == 3 | type == 4 | type == 8 | type == 9 | type == 14 | type == 15) {
 									bankcraft.getSignHandler().updateSign(event.getClickedBlock(), 1);
 								}
 								
 							}
 							if ((event.getAction() == Action.RIGHT_CLICK_BLOCK && !bankcraft.getConfigurationHandler().getString("general.swapClicks").equalsIgnoreCase("true")) || (event.getAction() == Action.LEFT_CLICK_BLOCK && !bankcraft.getConfigurationHandler().getString("general.swapClicks").equalsIgnoreCase("false"))) {
 								String amountAsString = ((Sign) event.getClickedBlock().getState()).getLine(2);
 								if (type == 3 | type == 4 | type == 8 | type == 9 | type == 14 | type == 15) {
 									// Scrolling-signs
 									amountAsString = bankcraft.getSignHandler().updateSign(event.getClickedBlock(), 0);
 								}
 								if (((Sign) event.getClickedBlock().getState()).getLine(2).equals(""))
 									bankcraft.getInteractionHandler().interact(type, amountAsString, p, p.getName());
 								else
 									bankcraft.getInteractionHandler().interact(type, amountAsString, p, ((Sign) event.getClickedBlock().getState()).getLine(2));
 							}
 						
 					} else {
 						coHa.printMessage(p, "message.notAllowed", "0", p.getName());
 					}
 					}
 				}
 				((Sign) event.getClickedBlock().getState()).update();
 			}
 		}
 	}
 
 }
