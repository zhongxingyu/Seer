 package de.hotmail.gurkilein.bankcraft;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 import de.hotmail.gurkilein.bankcraft.banking.*;
 
 public class SignHandler {
 	
 	private Bankcraft bankcraft;
 	private Map<Block, Integer> signPosition = new HashMap<Block, Integer>();
 	
 	public SignHandler(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 	}
 
 	
 
 	public String updateSign(Block clickedBlock, int steps) {
 		String[] scrollingSignArray = bankcraft.getSignDatabaseInterface().getAmounts(clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ(), clickedBlock.getWorld());
 		Sign sign = (Sign) clickedBlock.getState();
 		if (signPosition.containsKey(clickedBlock)) {
 			if (signPosition.get(clickedBlock) < (scrollingSignArray.length - steps)) {
 				signPosition.put(clickedBlock, signPosition.get(clickedBlock) + steps);
 				sign.setLine(2, "> " + scrollingSignArray[signPosition.get(clickedBlock)] + "");
 				if (signPosition.get(clickedBlock).equals(scrollingSignArray.length - 1)) {
 					sign.setLine(3, scrollingSignArray[0] + "");
 				} else {
 					sign.setLine(3, (scrollingSignArray[signPosition.get(clickedBlock) + 1]) + "");
 				}
 			} else {
 				signPosition.put(clickedBlock, 0);
 				sign.setLine(2, "> " + scrollingSignArray[0] + "");
 				sign.setLine(3, scrollingSignArray[1] + "");
 			}
 		} else {
 			signPosition.put(clickedBlock, 0);
 			sign.setLine(2, "> " + scrollingSignArray[0] + "");
 			sign.setLine(3, scrollingSignArray[1] + "");
 		}
 		sign.update(true);
 		return (String) scrollingSignArray[signPosition.get(clickedBlock)];
 	}
 
 	
 	@Deprecated
 	public void leftClickSign(Player p, String thirdLine, Integer typ,
 			Block clickedBlock, String fourthLine) {
 
 
 
 		boolean all = false;
 		String amountAsString = thirdLine;
 
 		//BALANCE sign
 		if (typ == 0 || typ == 5 || typ == 10 || typ == 11) {
 			if (typ == 0) {
 				bankcraft.getConfigurationHandler().printMessage(p, "message.balance", "", p.getName());
 			}
 			if (typ == 5) {
 				bankcraft.getConfigurationHandler().printMessage(p, "message.balancexp", "", p.getName());
 			}
 			if (typ == 10) {
 				bankcraft.getConfigurationHandler().printMessage(p, "message.balance", "", thirdLine);
 			}
 			if (typ == 11) {
 				bankcraft.getConfigurationHandler().printMessage(p, "message.balancexp", "", thirdLine);
 			}
 		} else {
 			//All other signs
 			if (typ == 3 | typ == 4 | typ == 8 | typ == 9 | typ == 14 | typ == 15) {
 				// Scrolling-sign
 				amountAsString = updateSign(clickedBlock, 0);
 			}
 			
 			
 			if (amountAsString.equalsIgnoreCase("all")) {
 				all = true;
 			}
 			
 			
 			if (typ == 1 | typ == 3) {
 				//Deposit Money
 				double amount;
 				if (all == true) {
 					amount = Bankcraft.econ.getBalance(p.getName());
 				} else {
 					amount = Double.parseDouble(amountAsString);
 				}
 				((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).transferFromPocketToAccount(p, p.getName(), amount,p);
 				
 			}
 			if (typ == 6 | typ == 8) {
 				//Deposit XP
 				int amount;
 				if (all == true) {
 					amount = ExperienceBukkitHandler.getTotalExperience(p);
 				} else {
 					amount = Integer.parseInt(amountAsString);
 				}
 				((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).transferFromPocketToAccount(p, p.getName(), amount,p);
 			}
 
 			if (typ == 2 | typ == 4) {
 				//Withdraw Money
 				double amount;
 				if (all == true) {
 					amount = bankcraft.getMoneyDatabaseInterface().getBalance(p.getName());
 				} else {
 					amount = Double.parseDouble(amountAsString);
 				}
 				
 				((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).transferFromAccountToPocket(p.getName(), p, amount,p);
 				
 			}
 			if (typ == 7 | typ == 9) {
 				//Withdraw XP
 				int amount;
 				if (all == true) {
 					amount = bankcraft.getExperienceDatabaseInterface().getBalance(p.getName());
 				} else {
 					amount = Integer.parseInt(amountAsString);
 				}
 				
 				((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).transferFromAccountToPocket(p.getName(), p, amount,p);
 			}
 			if (typ == 12 | typ == 14) {
 				//exchange Money
 				double amount;
 				if (all == true) {
 					amount = bankcraft.getMoneyDatabaseInterface().getBalance(p.getName());
 				} else {
 					amount = Double.parseDouble(amountAsString);
 				}
 				
 				if (((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).withdrawFromAccount(p.getName(), (double)(int)amount, p)) {
 					 if (((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).depositToAccount(p.getName(), (int)((int)amount*Double.parseDouble(bankcraft.getConfigurationHandler().getString("general.exchangerateFromMoneyToXp"))),p)) {
 						 bankcraft.getConfigurationHandler().printMessage(p, "message.exchangedMoneySuccessfully", amount+"", p.getName());
 					 } else {
 						 ((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).depositToAccount(p.getName(), (double)(int)amount, p);
 					 }
 					
 				}
 			}
 			if (typ == 13 | typ == 15) {
 				//exchange xp
 				double amount;
 				if (all == true) {
 					amount = bankcraft.getExperienceDatabaseInterface().getBalance(p.getName());
 				} else {
 					amount = Double.parseDouble(amountAsString);
 				}
 				
 				if (((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).withdrawFromAccount(p.getName(), (int)amount, p)) {
 					if (((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).depositToAccount(p.getName(), (((int)amount)*Double.parseDouble(bankcraft.getConfigurationHandler().getString("general.exchangerateFromXpToMoney"))),p)) {
 						bankcraft.getConfigurationHandler().printMessage(p, "message.exchangedXpSuccessfully", amount+"", p.getName());
 					} else {
 						((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).depositToAccount(p.getName(), (int)amount, p);
 					}
 				}
 			}
 			  
 			//16 = interestSign
 			//does nothing on click
 			if (typ == 17) {
 				//Allows for Chatsigns
 				//chatSignMAP.put(p, 1);
 				
 				}
 		}
 	}
 	
 	public Sign[] getSigns(World world, int type) {
 		
 		Location[] locations = bankcraft.getSignDatabaseInterface().getLocations(type, world);
		Sign[] signs = new Sign[locations.length];
 		
 		for (int i = 0; i<locations.length; i++) {
			signs[i] = ((Sign)locations[i].getBlock().getState());
 		}
 		
		return signs;
 	}
 
 
 
 
 	
 }
