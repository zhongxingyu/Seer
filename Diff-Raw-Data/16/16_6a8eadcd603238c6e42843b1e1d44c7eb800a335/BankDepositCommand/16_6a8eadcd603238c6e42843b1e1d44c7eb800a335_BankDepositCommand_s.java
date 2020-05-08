 package me.greatman.Craftconomy.commands.bank;
 
 import org.bukkit.ChatColor;
 
 import me.greatman.Craftconomy.Account;
 import me.greatman.Craftconomy.AccountHandler;
 import me.greatman.Craftconomy.BankHandler;
 import me.greatman.Craftconomy.Craftconomy;
 import me.greatman.Craftconomy.Currency;
 import me.greatman.Craftconomy.CurrencyHandler;
 import me.greatman.Craftconomy.commands.BaseCommand;
 import me.greatman.Craftconomy.utils.Config;
 
 public class BankDepositCommand extends BaseCommand
 {
 
 	public BankDepositCommand()
 	{
 		this.command.add("deposit");
 		this.requiredParameters.add("Bank Name");
 		this.requiredParameters.add("Amount");
 		this.optionalParameters.add("Currency");
 		permFlag = ("Craftconomy.bank.deposit");
 		helpDescription = "Deposit money in a bank account";
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
 					Account account = AccountHandler.getAccount(player);
 					if (account.hasEnough(amount, currency))
 					{
 						account.substractMoney(amount, currency);
 						BankHandler.getBank(this.parameters.get(0)).addMoney(amount, currency, player.getWorld());
 						sendMessage(ChatColor.WHITE + Craftconomy.format(amount, currency) + ChatColor.GREEN + " has been added into the " + ChatColor.WHITE + this.parameters.get(0) + ChatColor.GREEN + " bank account!");
 						
 					}
 				}
 				else
 					sendMessage(ChatColor.RED + "Invalid amount!");
 			}
 		}
 		else
 			sendMessage(ChatColor.RED + "This bank doesn't exists!");
 	}
 }
