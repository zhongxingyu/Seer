 package me.autoit4you.bankaccount.commands;
 
 import me.autoit4you.bankaccount.BankAccount;
 import me.autoit4you.bankaccount.exceptions.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 public class CommandAccountOpen extends BankAccountCommand {
 
 	@Override
 	public void run(CommandSender sender, String[] args)
 			throws BankAccountException {
		if(args.length < 1 || args[1] == null)
 			throw new BAArgumentException("Please review your arguments!");
 		
 		if(!BankAccount.perm.user(sender, args))
 			throw new CommandPermissionException();
 		
 		if(args[1].length() > 250) {
 			throw new CommandCustomException("Accountname is too long!");
 		}
 		
 		if(BankAccount.db.existAccount(args[1].toString())) {
 			throw new CommandCustomException("That name is already taken!");
 		}
 		
 		BankAccount.db.createAccount(sender.getName(), args[1].toString());
 		sender.sendMessage(ChatColor.GREEN + "Account opened!");
 	}
 
 }
