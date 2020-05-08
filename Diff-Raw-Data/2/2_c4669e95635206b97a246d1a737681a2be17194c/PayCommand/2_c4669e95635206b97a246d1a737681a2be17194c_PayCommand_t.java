 package me.greatman.Craftconomy.commands.money;
 
 import me.greatman.Craftconomy.Account;
 import me.greatman.Craftconomy.AccountHandler;
 import me.greatman.Craftconomy.Craftconomy;
 import me.greatman.Craftconomy.Currency;
 import me.greatman.Craftconomy.CurrencyHandler;
 import me.greatman.Craftconomy.commands.BaseCommand;
 import me.greatman.Craftconomy.utils.Config;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class PayCommand extends BaseCommand
 {
 
 	public PayCommand()
 	{
 		this.command.add("pay");
 		this.requiredParameters.add("Player Name");
 		this.requiredParameters.add("Amount");
 		this.optionalParameters.add("Currency");
 		permFlag = ("Craftconomy.money.pay");
 		helpDescription = "Send money to others.";
 	}
 
 	public void perform()
 	{
 		double amount;
 		Currency currency = CurrencyHandler.getCurrency(Config.currencyDefault, true);
 		if (AccountHandler.exists(this.parameters.get(0)))
 		{
 			Account senderAccount = AccountHandler.getAccount((Player) sender);
 			Account receiverAccount = AccountHandler.getAccount(this.parameters.get(0));
 			if (Craftconomy.isValidAmount(this.parameters.get(1)))
 			{
 				amount = Double.parseDouble(this.parameters.get(1));
 				// We have a special currency
 				if (this.parameters.size() >= 3)
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
 
 				Player player = (Player) sender;
 				if (!senderAccount.hasEnough(amount, currency, player.getWorld()))
 				{
 					sendMessage(ChatColor.RED + "You don't have " + ChatColor.WHITE
 							+ Craftconomy.format(amount, currency) + ChatColor.GREEN + "!");
 					return;
 				}
 				senderAccount.substractMoney(amount, currency, player.getWorld());
 				receiverAccount.addMoney(amount, currency, player.getWorld());
				sendMessage("You sent " + ChatColor.WHITE + Craftconomy.format(amount, currency) + ChatColor.GREEN
 						+ " to " + ChatColor.WHITE + receiverAccount.getPlayerName());
 				sendMessage(receiverAccount.getPlayer(),
 						"You received " + ChatColor.WHITE + Craftconomy.format(amount, currency) + ChatColor.GREEN
 								+ " from " + ChatColor.WHITE + senderAccount.getPlayerName());
 			}
 			else sendMessage(ChatColor.RED + "Positive number expected. Received something else.");
 		}
 		else sendMessage(ChatColor.RED + "The account " + ChatColor.WHITE + this.parameters.get(0) + ChatColor.RED
 				+ " does not exists!");
 	}
 }
