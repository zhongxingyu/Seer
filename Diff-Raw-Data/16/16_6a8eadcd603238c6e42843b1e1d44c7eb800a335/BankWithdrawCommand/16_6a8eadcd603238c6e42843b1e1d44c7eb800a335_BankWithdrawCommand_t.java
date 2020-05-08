 package me.greatman.Craftconomy.commands.bank;
 
 import org.bukkit.ChatColor;
 
 import me.greatman.Craftconomy.AccountHandler;
 import me.greatman.Craftconomy.Bank;
 import me.greatman.Craftconomy.BankHandler;
 import me.greatman.Craftconomy.Craftconomy;
 import me.greatman.Craftconomy.Currency;
 import me.greatman.Craftconomy.CurrencyHandler;
 import me.greatman.Craftconomy.commands.BaseCommand;
 import me.greatman.Craftconomy.utils.Config;
 
 public class BankWithdrawCommand extends BaseCommand {
 
 	public BankWithdrawCommand() {
 		this.command.add("withdraw");
 		this.requiredParameters.add("Bank Name");
 		this.requiredParameters.add("Amount");
 		this.optionalParameters.add("Currency");
 		permFlag = ("Craftconomy.bank.withdraw");
 		helpDescription = "Withdraw money in a bank account";
 	}
 	
 	public void perform() {
 		Currency currency = CurrencyHandler.getCurrency(Config.currencyDefault, true);
 		double amount;
 		if (BankHandler.exists(this.parameters.get(0)))
 		{
 			if (BankHandler.getBank(this.parameters.get(0)).getOwner().equals(player.getName()) || BankHandler.getBank(this.parameters.get(0)).getMembers().contains(player.getName()))
 			{
 				if (Craftconomy.isValidAmount(this.parameters.get(1)))
 				{
 					amount = Double.parseDouble(this.parameters.get(1));
 					if (this.parameters.size() == 3)
 					{
 						if (CurrencyHandler.exists(this.parameters.get(2), false))
 						{
 							currency = CurrencyHandler.getCurrency(this.parameters.get(2), false);
 						}
 						else
 						{
 							sendMessage("This currency doesn't exists!");
 							return;
 						}
 					}
 					Bank bank = BankHandler.getBank(this.parameters.get(0));
 					
 					if (bank.hasEnough(amount, currency, player.getWorld()))
 					{
 						bank.substractMoney(amount, currency, player.getWorld());
 						AccountHandler.getAccount(player).addMoney(amount, currency, player.getWorld());
 						sendMessage(ChatColor.WHITE + Craftconomy.format(amount, currency) + ChatColor.GREEN + " has been withdraw from the " + ChatColor.WHITE + this.parameters.get(0) + ChatColor.GREEN + " bank account!");
 						
 					}
					else
						sendMessage(ChatColor.RED + "There's not enough money in the bank account for this transaction!");
 				}
 				else
 					sendMessage(ChatColor.RED + "Invalid amount!");
 			}
			else
				sendMessage(ChatColor.RED + "You don't have access to that bank!");
 			
 		}
 		else
 			sendMessage(ChatColor.RED + "This bank doesn't exists!");
 	}
 }
