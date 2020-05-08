 package me.greatman.Craftconomy.commands.money;
 
 import org.bukkit.ChatColor;
 
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
			helpDescription = "Create a account";
 		}
 		
 		public void perform() {
 			Currency source;
 			Currency destination;
 			
 			if (CurrencyHandler.exists(this.parameters.get(0), false))
 			{
 				source = CurrencyHandler.getCurrency(this.parameters.get(0), false);
 			}else{
				sendMessage(ChatColor.RED + "Currency " + ChatColor.WHITE + this.parameters.get(0) + ChatColor.GREEN + " does not exist!");
 				return;
 			}
 			if (CurrencyHandler.exists(this.parameters.get(1), false))
 			{
 				destination = CurrencyHandler.getCurrency(this.parameters.get(1), false);
 			}else{
				sendMessage(ChatColor.RED + "Currency " + ChatColor.WHITE + this.parameters.get(1) + ChatColor.GREEN + " does not exist!");
 				return;
 			}
			sendMessage(ChatColor.GRAY + "1" + ChatColor.WHITE + " " + source.getName() +" = " + CurrencyHandler.convert(source, destination, 1.00) + " " + destination.getName());
			sendMessage(ChatColor.GRAY + "1" + ChatColor.WHITE + " " + destination.getName() + " = " + CurrencyHandler.convert(destination, source, 1.00) + " " + source.getName());
 		}
 }
