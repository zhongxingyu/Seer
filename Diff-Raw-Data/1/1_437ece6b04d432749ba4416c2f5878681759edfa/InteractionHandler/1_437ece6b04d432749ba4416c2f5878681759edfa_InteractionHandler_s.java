 package de.hotmail.gurkilein.bankcraft.banking;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 
 import de.hotmail.gurkilein.bankcraft.Bankcraft;
 import de.hotmail.gurkilein.bankcraft.Util;
 
 public class InteractionHandler {
 	
 	private Bankcraft bankcraft;
 	
 	//0 = not listening, 1 = waiting for method, 2 = Amount
 	private HashMap <Player, Integer> chatSignMap = new HashMap<Player, Integer>();
 	
 	//Matches interactions like deposit or withdraw to their typeId
 	private HashMap<String, Integer> typeMap = new HashMap<String, Integer>();
 	
 	//-1 = no account related, 1 = pocket money, 2 = account money, 3= pocket xp, 4= account xp
 	private HashMap<Integer, Integer> currencyMap = new HashMap<Integer, Integer>();
 
 	private HashMap<Player, Long> lastInteractMap = new HashMap<Player,Long>();
 
 	public InteractionHandler(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 		
 		
 		//Fill currencyMap
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.balance").toLowerCase(), 0);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.deposit").toLowerCase(), 1);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.withdraw").toLowerCase(), 2);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.balancexp").toLowerCase(), 5);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.depositxp").toLowerCase(), 6);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.withdrawxp").toLowerCase(), 7);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.exchange").toLowerCase(), 12);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.exchangexp").toLowerCase(), 13);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.interesttimer").toLowerCase(), 16);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.chatinteract").toLowerCase(), 17);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.rankstats").toLowerCase(), 18);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.rankstatsxp").toLowerCase(), 19);
 		
 		//Fill typeMap
 		currencyMap.put(0, -1);
 		currencyMap.put(1, 1);
 		currencyMap.put(2, 2);
 		currencyMap.put(3, 1);
 		currencyMap.put(4, 2);
 		currencyMap.put(5, -1);
 		currencyMap.put(6, 3);
 		currencyMap.put(7, 4);
 		currencyMap.put(8, 3);
 		currencyMap.put(9, 4);
 		currencyMap.put(10, -1);
 		currencyMap.put(11, -1);
 		currencyMap.put(12, 2);
 		currencyMap.put(13, 4);
 		currencyMap.put(14, 2);
 		currencyMap.put(15, 4);
 		currencyMap.put(16, -1);
 		currencyMap.put(17, -1);
 		currencyMap.put(18, -1);
 		currencyMap.put(19, -1);
 	}
 
 	public boolean interact(int type, String amountAsString, Player interactingPlayer, String targetPlayer) {
 		
 		//Check for interaction interval
 		if (lastInteractMap.containsKey(interactingPlayer) && System.currentTimeMillis()-lastInteractMap.get(interactingPlayer) <= Integer.parseInt(bankcraft.getConfigurationHandler().getString("general.timeBetweenTwoInteractions"))) {
 			bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.tooFastInteraction", "", interactingPlayer.getName());
 			return false;
 		}
 		lastInteractMap.put(interactingPlayer, System.currentTimeMillis());
 		
 		
 		if (amountAsString == null || amountAsString.equalsIgnoreCase("") || (!amountAsString.equalsIgnoreCase("all") && !Util.isDouble(amountAsString))) {
 			return interact(type, -1 , interactingPlayer, targetPlayer);
 		}
 		
 		if (amountAsString.equalsIgnoreCase("all")) {
 			return interact(type, getMaxAmountForAction(currencyMap.get(type), interactingPlayer) , interactingPlayer, targetPlayer);
 		}
 	
 		
 		return interact(type, Double.parseDouble(amountAsString) , interactingPlayer, targetPlayer);
 	}
 	
 	
 	//Returns current balance of the related account
 	private double getMaxAmountForAction(int currencyType, Player pocketOwner) {
 
 		
 		if (currencyType == 1) {
 			return Bankcraft.econ.getBalance(pocketOwner.getName());
 		} else
 		if (currencyType == 2) {
 			return bankcraft.getMoneyDatabaseInterface().getBalance(pocketOwner.getName());
 		} else
 		if (currencyType == 3) {
 			return (int)ExperienceBukkitHandler.getTotalExperience(pocketOwner);
 		} else	
 		if (currencyType == 4) {
 			return (int)bankcraft.getExperienceDatabaseInterface().getBalance(pocketOwner.getName());
 		}
 		return -1;
 	}
 
 	
 	
 	
 	//Main method
 	private boolean interact(int type, double amount, Player interactingPlayer, String targetPlayer) {
 		
 //		System.out.println(type+" "+amount+" "+interactingPlayer.getName()+" "+targetPlayer);
 		
 		
 			//BALANCE signs
 			if (type == 0 || type == 10) {
 				if (targetPlayer != "")
 					bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.balance", "", targetPlayer);	
 				else
 					bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.balance", "", interactingPlayer.getName());
 				return true;
 			}
 			if (type == 5 || type == 11) {
 				if (targetPlayer != "")
 					bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.balancexp", "", targetPlayer);
 				else
 					bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.balancexp", "", interactingPlayer.getName());
 				return true;
 			}
 			
 			if (type == 1 | type == 3) {
 				//Deposit Money
 				return ((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).transferFromPocketToAccount(interactingPlayer, interactingPlayer.getName(), amount,interactingPlayer);
 			}
 			if (type == 6 | type == 8) {
 				//Deposit XP
 				return ((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).transferFromPocketToAccount(interactingPlayer, interactingPlayer.getName(), (int)amount,interactingPlayer);
 			}
 
 			if (type == 2 | type == 4) {
 				//Withdraw Money
 				return ((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).transferFromAccountToPocket(interactingPlayer.getName(), interactingPlayer, amount,interactingPlayer);
 			}
 			if (type == 7 | type == 9) {
 				//Withdraw XP
 				return ((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).transferFromAccountToPocket(interactingPlayer.getName(), interactingPlayer, (int)amount,interactingPlayer);
 			}
 			if (type == 12 | type == 14) {
 				//exchange Money
 				if (((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).withdrawFromAccount(interactingPlayer.getName(), (double)(int)amount, interactingPlayer)) {
 					 if (((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).depositToAccount(interactingPlayer.getName(), (int)((int)amount*Double.parseDouble(bankcraft.getConfigurationHandler().getString("general.exchangerateFromMoneyToXp"))),interactingPlayer)) {
 						 bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.exchangedMoneySuccessfully", amount+"", interactingPlayer.getName());
 						 return true;
 					 } else {
 						 ((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).depositToAccount(interactingPlayer.getName(), (double)(int)amount, interactingPlayer);
 						 return false;
 					 }
 					
 				}
 			}
 			if (type == 13 | type == 15) {
 				//exchange xp
 				if (((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).withdrawFromAccount(interactingPlayer.getName(), (int)amount, interactingPlayer)) {
 					if (((MoneyBankingHandler)bankcraft.getBankingHandlers()[0]).depositToAccount(interactingPlayer.getName(), (((int)amount)*Double.parseDouble(bankcraft.getConfigurationHandler().getString("general.exchangerateFromXpToMoney"))),interactingPlayer)) {
 						bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.exchangedXpSuccessfully", amount+"", interactingPlayer.getName());
 						return true;
 					} else {
 						((ExperienceBankingHandler)bankcraft.getBankingHandlers()[1]).depositToAccount(interactingPlayer.getName(), (int)amount, interactingPlayer);
 						return false;
 					}
 				}
 			}
 			  
 			if (type == 16) {
 				//interestCounter
 				bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.interestTimer", amount+"", interactingPlayer.getName());
 				return true;
 			}
 			
 			if (type == 17) {
 				//Starts interaction with chatSigns (everything else is handled in the MinecraftChatListener)
 				chatSignMap.put(interactingPlayer, 1);
 				bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.specifyAnInteraction", "", interactingPlayer.getName());
 				return true;
 				}
 			
 			if (type == 18) {
 				//rankStatsMoney
 				bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.rankStatsMoney", "", interactingPlayer.getName());
 				return true;
 				}
 			
 			if (type == 19) {
 				//rankStatsExperience
 				bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.rankStatsExperience", "", interactingPlayer.getName());
 				return true;
 				}
 			
 		return false;
 	}
 	
 	public void stopChatInteract(Player p) {
 		chatSignMap.put(p, 0);
 		bankcraft.getConfigurationHandler().printMessage(p, "message.youHaveQuit", "", p.getName());
 		}
 
 	
 	
 	
 	
 	
 	
 	public boolean interact(String type, String amountAsString, Player pocketOwner, String accountOwner) {
 		return interact(typeMap.get(type.toLowerCase()), amountAsString, pocketOwner, accountOwner);
 	}
 	
 	public HashMap<Player, Integer> getChatSignMap() {
 		return chatSignMap;
 	}
 
 	public HashMap<String, Integer> getTypeMap() {
 		return typeMap;
 	}
 }
