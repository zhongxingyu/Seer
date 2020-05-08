 package com.github.wolf480pl.PaidSwitch;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 public class Payment {
 	public static Logger log;
    public String Account;
    public double Amount;
    public boolean bank;
    public boolean none;
    public String description = null;
    public Material type = null;
    
 	public Payment(String account, String amount, String desc, Material type) {
 		this(account, amount);
 		this.description = desc;
 		this.type = type;
 	}
 	public Payment(String account, String amount){
 		parseAccount(account);
 		try{
 			Amount = Double.parseDouble(amount);
 		} catch (NumberFormatException ex){
 			Amount = 0;
 		}
 	}
 	public Payment(String account, double amount, String desc, Material type) {
 		this(account, amount);
 		this.description = desc;
 		this.type = type;
 	}
 	public Payment(String account, double amount){
 		parseAccount(account);
 		Amount = amount;
 	}
 	public Payment(Payment payment){
 		Account = payment.Account;
 		Amount = payment.Amount;
 		description = payment.description;
 		bank = payment.bank;
 		none = payment.none;
 	}
 	public boolean isValid(){
 		return ((none || Account != null) && (Amount != 0));
 	}
 	public boolean isValid(Economy eco){
 		if (bank) {
 			try {
 				eco.getBanks();
 			} catch (UnsupportedOperationException e) {
 				log.fine("Bank unsupported");
 				bank = false;
 			}
 		}
 		return (none
 				|| (	(Account != null)
 						&& (bank
 								? eco.getBanks().contains(Account)
 								: eco.hasAccount(Account)
 							)
 					)
 				) && (Amount != 0);
 	}
 	public boolean execute(Economy eco){
 		EconomyResponse resp;
 		if (!this.isValid(eco)) {
 			return false;
 		}
 		if(none) return true;
 		if(bank)
 			resp = eco.bankDeposit(Account, Amount);
 		else
 			resp = eco.depositPlayer(Account, Amount);
 		return resp.transactionSuccess();
 	}
 	public String toString(){
 		return "Payment: " + String.valueOf(Amount) + " for " + (bank ? "bank" : "player") + ": " + Account;
 	}
	private void parseAccount(String account){
 		if(account.equalsIgnoreCase("none")){
 			none = true;
 			return;
 		}
 		if(account.length() >= 2 && account.substring(0, 2).equalsIgnoreCase("b:")){
 			bank = true;
 			Account = account.substring(2);
 		} else {
 			bank = false;
 			Account = account;
 		}
 
 	}
 }
