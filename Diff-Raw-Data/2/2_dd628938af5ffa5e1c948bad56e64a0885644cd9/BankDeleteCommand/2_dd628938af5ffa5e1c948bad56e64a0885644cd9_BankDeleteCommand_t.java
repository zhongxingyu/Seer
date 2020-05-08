 package me.greatman.Craftconomy.commands.bank;
 
 import org.bukkit.ChatColor;
 
 import me.greatman.Craftconomy.BankHandler;
 import me.greatman.Craftconomy.commands.BaseCommand;
 
 public class BankDeleteCommand extends BaseCommand
 {
 
 	public BankDeleteCommand()
 	{
 		this.command.add("delete");
 		this.requiredParameters.add("Bank Name");
		permFlag = "Craftconomy.bank.delete";
 		helpDescription = "Remove a bank account";
 	}
 
 	public void perform()
 	{
 		if (BankHandler.exists(this.parameters.get(0)))
 		{
 			if (BankHandler.getBank(this.parameters.get(0)).getOwner().equals(player.getName())
 					|| player.hasPermission("Craftconomy.bank.admindelete"))
 			{
 				BankHandler.delete(this.parameters.get(0));
 				sendMessage("The bank account " + ChatColor.WHITE + this.parameters.get(0) + ChatColor.GREEN
 						+ " has been deleted!");
 
 			}
 		}
 		else sendMessage(ChatColor.RED + "A error occured or the bank already exists!");
 	}
 
 }
