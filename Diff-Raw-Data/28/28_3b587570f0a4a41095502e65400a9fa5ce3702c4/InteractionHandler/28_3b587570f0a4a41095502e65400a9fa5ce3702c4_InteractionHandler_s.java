 package de.hotmail.gurkilein.bankcraft.banking;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 
 import de.hotmail.gurkilein.bankcraft.Bankcraft;
 
 public class InteractionHandler {
 	
 	private Bankcraft bankcraft;
 	
 	//0 = not listening, 1 = waiting for method, 2 = Amount
 	private HashMap <Player, Integer> chatSignMap = new HashMap<Player, Integer>();
 	
 	//Matches interactions like deposit or withdraw to their typeId
 	private HashMap<String, Integer> typeMap = new HashMap<String, Integer>();
 	
 	//-1 = no account related, 1 = pocket money, 2 = account money, 3= pocket xp, 4= account xp
 	private HashMap<Integer, Integer> currencyMap = new HashMap<Integer, Integer>();
 
 	public InteractionHandler(Bankcraft bankcraft) {
 		this.bankcraft = bankcraft;
 		
 		
 		//Fill currencyMap
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.balance"), 0);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.deposit"), 1);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.withdraw"), 2);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.balancexp"), 5);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.depositxp"), 6);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.withdrawxp"), 7);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.exchange"), 12);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.exchangexp"), 13);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.interesttimer"), 16);
 		typeMap.put(bankcraft.getConfigurationHandler().getString("signAndCommand.chatinteract"), 17);
 		
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
 	}
 
 	public boolean interact(int type, String amountAsString, Player pocketOwner, String accountOwner) {
 		
 		if (amountAsString == null || amountAsString.equalsIgnoreCase("")) {
 			return interact(type, -1 , pocketOwner, accountOwner);
 		}
 		
 		if (amountAsString.equalsIgnoreCase("all")) {
			return interact(type, getMaxAmountForAction(currencyMap.get(type), pocketOwner, accountOwner) , pocketOwner, accountOwner);
 		}
 	
 		
 		return interact(type, Double.parseDouble(amountAsString) , pocketOwner, accountOwner);
 	}
 	
 	
 	//Returns current balance of the related account
	private double getMaxAmountForAction(int currencyType, Player pocketOwner,
			String accountOwner) {
 
 		if (currencyType == 1) {
 			return Bankcraft.econ.getBalance(pocketOwner.getName());
 		} else
 		if (currencyType == 2) {
			return bankcraft.getMoneyDatabaseInterface().getBalance(accountOwner);
 		} else
 		if (currencyType == 3) {
 			return (int)ExperienceBukkitHandler.getTotalExperience(pocketOwner);
 		} else	
 		if (currencyType == 4) {
			return (int)bankcraft.getExperienceDatabaseInterface().getBalance(accountOwner);
 		}
 		return -1;
 	}
 
 	
 	
 	
 	//Main method
 	private boolean interact(int type, double amount, Player interactingPlayer, String targetPlayer) {
 
 			//BALANCE signs
 			if (type == 0) {
 				bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.balance", "", interactingPlayer.getName());
 				return true;
 			}
 			if (type == 5) {
 				bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.balancexp", "", interactingPlayer.getName());
 				return true;
 			}
 			if (type == 10) {
 				bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.balance", "", targetPlayer);
 				return true;
 			}
 			if (type == 11) {
 				bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.balancexp", "", targetPlayer);
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
 				return true;
 			}
 			
 			if (type == 17) {
 				//Starts interaction with chatSigns (everything else is handled in the MinecraftChatListener)
 				chatSignMap.put(interactingPlayer, 1);
 				bankcraft.getConfigurationHandler().printMessage(interactingPlayer, "message.specifyAnInteraction", "", interactingPlayer.getName());
 				return true;
 				}
 		return false;
 	}
 
 	
 	
 	
 	
 	
 	
 	public boolean interact(String type, String amountAsString, Player pocketOwner, String accountOwner) {
 		return interact(typeMap.get(type), amountAsString, pocketOwner, accountOwner);
 	}
 	
 	public HashMap<Player, Integer> getChatSignMap() {
 		return chatSignMap;
 	}
 
 	public HashMap<String, Integer> getTypeMap() {
 		return typeMap;
 	}
 }
