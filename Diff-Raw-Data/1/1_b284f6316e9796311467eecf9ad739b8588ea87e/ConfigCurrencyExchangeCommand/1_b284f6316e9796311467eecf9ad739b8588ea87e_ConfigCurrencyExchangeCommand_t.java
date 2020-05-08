 package me.greatman.Craftconomy.commands.config;
 
 import org.bukkit.ChatColor;
 
 import me.greatman.Craftconomy.Currency;
 import me.greatman.Craftconomy.CurrencyHandler;
 import me.greatman.Craftconomy.commands.BaseCommand;
 
 public class ConfigCurrencyExchangeCommand extends BaseCommand {
 	public ConfigCurrencyExchangeCommand() {
 		this.command.add("currencyexchange");
 		this.requiredParameters.add("Source Currency");
 		this.requiredParameters.add("Dest Currency");
 		this.requiredParameters.add("Rate");
 		permFlag = ("Craftconomy.currency.exchange");
 		helpDescription = "Manage exchange rates";
 	}
 	public void perform() {
 		// validate input currencys
 		if(!CurrencyHandler.exists(this.parameters.get(0), true)) {
 			sendMessage(ChatColor.RED + "Currency "+this.parameters.get(0)+" not found!");
 			return;
 		}
 		if(!CurrencyHandler.exists(this.parameters.get(1), true)) {
 			sendMessage(ChatColor.RED + "Currency "+this.parameters.get(1)+" not found!");
 			return;
 		}
 		//get currencys
 		Currency src = CurrencyHandler.getCurrency(this.parameters.get(0), true);
 		Currency dest = CurrencyHandler.getCurrency(this.parameters.get(1), true);
 		double rate;
 		try{
 			rate = Double.parseDouble(this.parameters.get(2));
 		}catch(Exception e) {
 			sendMessage(ChatColor.RED + "Amount not valid! Not numeric!");
 			return;
 		}
 		CurrencyHandler.setExchangeRate(src, dest, rate);
		src.setExchangeRate(dest.getName(), rate);
 		sendMessage(ChatColor.GREEN + "Exchange rate of currencys set!");
 	}
 }
