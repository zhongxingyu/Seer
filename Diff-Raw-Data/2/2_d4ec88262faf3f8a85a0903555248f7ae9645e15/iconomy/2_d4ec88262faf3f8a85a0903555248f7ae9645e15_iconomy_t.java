 package tv.mineinthebox.ManCo.utils;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import tv.mineinthebox.ManCo.configuration.configuration;
 
 public class iconomy {
 
 	public static boolean debitMoney(Player p, double money) {
 		if(util.isIconomyEnabled()) {
 			RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 			Economy econ = economyProvider.getProvider();
 			if(econ.has(p.getName(), configuration.returnIconomyPrice())) {
 				if(econ.withdrawPlayer(p.getName(), configuration.returnIconomyPrice()).transactionSuccess()) {
 					return true;
 				} else {
 					return false;
 				}
 			} else {
 				return false;
 			}
 		}
 		return false;
 	}
 
 	public static boolean addMoney(Player p, double money) {
 		if(util.isIconomyEnabled()) {
 			RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 			Economy econ = economyProvider.getProvider();
 			if(econ.depositPlayer(p.getName(), money).transactionSuccess()) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	public static String getSymbol() {
 		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		Economy econ = economyProvider.getProvider();
		return econ.currencyNameSingular()+"(s)";
 	}
 
 }
