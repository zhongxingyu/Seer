 package me.greatman.Craftconomy.commands.money;
 
 import org.bukkit.ChatColor;
 
import me.greatman.Craftconomy.Craftconomy;
 import me.greatman.Craftconomy.Currency;
 import me.greatman.Craftconomy.CurrencyHandler;
 import me.greatman.Craftconomy.commands.BaseCommand;
 
 /**
  * This commands shows all exchange rates
  * @author steffengy
  *
  */
 public class ExchangeCalcCommand extends BaseCommand{
 		
 		public ExchangeCalcCommand() {
 			this.command.add("exchangecalc");
 			this.requiredParameters.add("Source Currency");
 			this.requiredParameters.add("Destination Currency");
 			permFlag = ("Craftconomy.money.exchange");
			helpDescription = "Show the exchange rate between 2 currency";
 		}
 		
 		public void perform() {
 			Currency source;
 			Currency destination;
 			
 			if (CurrencyHandler.exists(this.parameters.get(0), false))
 			{
 				source = CurrencyHandler.getCurrency(this.parameters.get(0), false);
 			}else{
				sendMessage(ChatColor.RED + "Currency " + ChatColor.WHITE + this.parameters.get(0) + ChatColor.RED + " does not exist!");
 				return;
 			}
 			if (CurrencyHandler.exists(this.parameters.get(1), false))
 			{
 				destination = CurrencyHandler.getCurrency(this.parameters.get(1), false);
 			}else{
				sendMessage(ChatColor.RED + "Currency " + ChatColor.WHITE + this.parameters.get(1) + ChatColor.RED + " does not exist!");
 				return;
 			}
			sendMessage(ChatColor.GRAY + Craftconomy.format(1,source) + " = " + Craftconomy.format(CurrencyHandler.convert(source, destination, 1.00), destination));
			sendMessage(ChatColor.GRAY + Craftconomy.format(1,destination) + " = " + Craftconomy.format(CurrencyHandler.convert(destination, source, 1.00), source));
 		}
 }
