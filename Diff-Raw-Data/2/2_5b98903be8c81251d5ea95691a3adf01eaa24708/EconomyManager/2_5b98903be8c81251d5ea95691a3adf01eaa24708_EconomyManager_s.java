 package com.timvisee.manager.economymanager;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Server;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import com.timvisee.SimpleEconomy.SimpleEconomyHandler.SimpleEconomyHandler;
 
 import cosine.boseconomy.BOSEconomy;
 
 import com.iCo6.Constants;
 import com.iCo6.iConomy;
 import com.iCo6.system.Accounts;
 import com.iCo6.system.Holdings;
 
 public class EconomyManager {
 	
 	private EconomySystemType economyType = EconomySystemType.NONE;
 	private Server s;
 	private Plugin p;
 
 	// Simple Economy
 	private static SimpleEconomyHandler simpleEconomyHandler;
 	
 	// BOSEconomy
 	BOSEconomy BOSEcon = null;
 	
 	// iConomy 6
 	private Plugin plugin = null;
     protected iConomy economy = null;
     private Accounts accounts;
 	
 	// Vault
     public static Economy vaultEconomy = null;
 	
 	/**
 	 * Constructor
 	 * @param s server
 	 * @param logPrefix log prefix (plugin name)
 	 */
 	public EconomyManager(Server s, Plugin p) {
 		this.s = s;
 		this.p = p;
 	}
 	
 	/**
 	 * Get the used economy system where the economy manager is hooked into
 	 * @return economy system
 	 */
 	public EconomySystemType getUsedEconomySystemType() {
 		return this.economyType;
 	}
 	
 	/**
 	 * Check if the economy manager hooked into any of the supported economy systems
 	 * @return false if there isn't any economy system used
 	 */
 	public boolean isEnabled() {
 		return !economyType.equals(EconomySystemType.NONE);
 	}
 	
 	/**
 	 * Check if the current economy system support banks
 	 * @return true if supported
 	 */
 	public boolean hasBankSupport() {
 		if(!isEnabled()) {
 			// Not hooked into any permissions system, return false
 			return false;
 		}
 		
 		switch (this.economyType) {
 		case SIMPLE_ECONOMY:
 		case NONE:
 			// Simple Economy
 			// This system has no support for banks
 			return false;
 			
 		case BOSECONOMY:
 			// BOSEconomy
 			// This system has support for banks
 			return true;
 			
 		case ICONOMY6:
 			// iConomy 6
 			// This system has support for banks
 			return true;
 			
 		case VAULT:
 			// Vault
 			return vaultEconomy.hasBankSupport();
 			
 		default:
 			// Something went wrong, return false to prevent problems
 			return false;
 		}
 	}
 	
 	public EconomySystemType setup() {
 		// Define the plugin manager
 		final PluginManager pm = this.s.getPluginManager();
 		
 		// Reset used economy system type
 		economyType = EconomySystemType.NONE;
 		
 		// Check if Simple Economy is available
 		Plugin simpleEconomy = pm.getPlugin("Simple Economy"); //TODO Rename plugin without space when updated
 		if(simpleEconomy != null) {
 			simpleEconomyHandler = ((com.timvisee.SimpleEconomy.SimpleEconomy) simpleEconomy).getHandler();
 			economyType = EconomySystemType.SIMPLE_ECONOMY;
 		    System.out.println("[" + p.getName() + "] Hooked into Simple Economy!");
 		    return EconomySystemType.SIMPLE_ECONOMY;
 		}
 		
 		// Check if BOSEconomy is available
 	    Plugin bose = pm.getPlugin("BOSEconomy");
 	    if(bose != null) {
 	        BOSEcon = (BOSEconomy)bose;
 			economyType = EconomySystemType.BOSECONOMY;
 		    System.out.println("[" + p.getName() + "] Hooked into BOSEconomy!");
 		    return EconomySystemType.BOSECONOMY;
 	    }
 		
 	    // Check if iConomy6 is available
 	    Plugin iCon6 = pm.getPlugin("iConomy");
	    if (iCon6 != null && iCon6.isEnabled() && iCon6.getClass().getName().equals("com.iCo6.iConomy")){
 	    	economy = (iConomy) iCon6;
 	    	accounts = new Accounts();
 	    	System.out.println("[" + p.getName() + "] Hooked into iConomy 6!");
 	    	return EconomySystemType.ICONOMY6;
 	    }
 	    
 		// Check if Vault is available
 	    final Plugin vaultPlugin = pm.getPlugin("Vault");
 		if (vaultPlugin != null && vaultPlugin.isEnabled()) {
 			RegisteredServiceProvider<Economy> economyProvider = this.s.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 	        if (economyProvider != null) {
 	            vaultEconomy = economyProvider.getProvider();
 	            if(vaultEconomy.isEnabled()) {
 	            	economyType = EconomySystemType.VAULT;
 	            	System.out.println("[" + p.getName() + "] Hooked into Vault Economy!");
 	    		    return EconomySystemType.VAULT;
 	            } else
 	            	System.out.println("[" + p.getName() + "] Not using Vault Economy, Vault Economy is disabled!");
 	        }
 		}
 		
 	    // No recognized economy system found
 	    economyType = EconomySystemType.NONE;
 	    System.out.println("[" + p.getName() + "] No supported economy system found! Economy disabled!");
 	    
 	    return EconomySystemType.NONE;
 	}
 	
 	/**
 	 * Get the money balance of a player
 	 * @param p player name
 	 * @return money balance
 	 */
 	public double getBalance(String p) {
 		return getBalance(p, 0.00);
 	}
 	
 	/**
 	 * Get the money balance of a player
 	 * @param p player name
 	 * @param def default balance if not hooked into any economy system
 	 * @return money balance
 	 */
 	public double getBalance(String p, double def) {
 		if(!isEnabled()) {
 			// No economy system is used, return zero balance
 			return 0.00;
 		}
 		
 		switch(this.economyType) {
 		case SIMPLE_ECONOMY:
 			// Simple Economy
 			return simpleEconomyHandler.getMoney(p);
 			
 		case BOSECONOMY:
 			// BOSEconomy
 			return BOSEcon.getPlayerMoneyDouble(p);
 			
 		case ICONOMY6:
 			// iConomy6
 			if (accounts.exists(p))
 				return accounts.get(p).getHoldings().getBalance();
 			else
 				return def;
 				
 		case VAULT:
 			// Vault
 			return vaultEconomy.getBalance(p);
 			
 		case NONE:
 			// Not hooked into any economy system, return default balance
 			return def;
 			
 		default:
 			// Something went wrong, return zero balance to prevent problems
 			return 0.00;
 		}
 	}
 	
 	/**
 	 * Check if a player has enough money balance to pay something
 	 * @param p player name
 	 * @param price price to pay
 	 * @return true if the player has enough money
 	 */
 	public boolean hasEnoughMoney(String p, double price) {
 		double balance = getBalance(p);
 		return (balance >= price);
 	}
 	
 	/**
 	 * Deposit money to a player
 	 * @param p player name
 	 * @param money money amount
 	 * @return false when something was wrong
 	 */
 	public boolean depositMoney(String p, double money) {
 		if(!isEnabled()) {
 			// No economy system is used, return false
 			return false;
 		}
 		
 		// Get current player balance
 		//double balance = getBalance(p);
 		
 		// Deposit money
 		switch(this.economyType) {
 		case SIMPLE_ECONOMY:
 			// Simple Economy
 			simpleEconomyHandler.addMoney(p, money);
 			break;
 			
 		case BOSECONOMY:
 			// BOSEconomy
 			BOSEcon.addPlayerMoney(p, money, false);
 			break;
 		
 		case ICONOMY6:
 			// iConomy6
 			if (accounts.exists(p)){
 				accounts.get(p).getHoldings().add(money);
 				return true;
 			} else
 				return false;
 			
 		case VAULT:
 			// Vault
 			vaultEconomy.depositPlayer(p, money);
 			break;
 			
 		case NONE:
 			// Not hooked into any economy system, return false
 			return false;
 			
 		default:
 			// Something went wrong, return false to prevent problems
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Withdraw money from a player
 	 * @param p player name
 	 * @param money money amount
 	 * @return false when something was wrong
 	 */
 	public boolean withdrawMoney(String p, double money) {
 		if(!isEnabled()) {
 			// No economy system is used, return false
 			return false;
 		}
 		
 		// Get current player balance
 		double balance = getBalance(p);
 		double newBalance = balance - money;
 		
 		// The new Balance has to be zero or above
 		if(newBalance < 0) {
 			return false;
 		}
 		
 		// Withdraw money
 		switch(this.economyType) {
 		case SIMPLE_ECONOMY:
 			// Simple Economy
 			simpleEconomyHandler.subtractMoney(p, money);
 			break;
 			
 		case BOSECONOMY:
 			// BOSEconomy
 			BOSEcon.setPlayerMoney(p, newBalance, false);
 			break;
 		
 		case ICONOMY6:
 			// iConomy6
 			if (accounts.exists(p)){
 				accounts.get(p).getHoldings().subtract(money);
 				return true;
 			} else
 				return false;
 				
 		case VAULT:
 			// Vault
 			vaultEconomy.withdrawPlayer(p, money);
 			break;
 			
 		case NONE:
 			// Not hooked into any economy system, return false
 			return false;
 			
 		default:
 			// Something went wrong, return false to prevent problems
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Get the currency name
 	 * @param money the current balance (to get the Singular/Plural thingy right)
 	 * @return currency name
 	 */
 	public String getCurrencyName(double money) {
 		return getCurrencyName(money, "Money");
 	}
 	
 	/**
 	 * Get the currency name
 	 * @param money the current balance (to get the Singular/Plural thingy right)
 	 * @param def the default currency name
 	 * @return currency name
 	 */
 	public String getCurrencyName(double money, String def) {
 		if(!isEnabled()) {
 			// No economy system is used, return false
 			return def;
 		}
 		
 		// Get currency name
 		switch(this.economyType) {
 		case SIMPLE_ECONOMY:
 			// Simple Economy
 			//TODO Finish this function in the API of Simple Economy
 			return "Silver";
 			
 		case BOSECONOMY:
 			// BOSEconomy
 			return BOSEcon.getMoneyNameProper(money);
 		
 		case ICONOMY6:
 			// iConomy6
 			if(money > 1.00) {
 				return Constants.Nodes.Major.getStringList().get(1);
 			} else {
 				return Constants.Nodes.Major.getStringList().get(0);
 			}
 			
 		case VAULT:
 			// Vault
 			if(money > 1.00) {
 				return vaultEconomy.currencyNamePlural();
 			} else {
 				return vaultEconomy.currencyNameSingular();
 			}
 			
 		case NONE:
 			// Not hooked into any economy system, return false
 			return def;
 			
 		default:
 			// Something went wrong, return false to prevent problems
 			return def;
 		}
 	}
 }
