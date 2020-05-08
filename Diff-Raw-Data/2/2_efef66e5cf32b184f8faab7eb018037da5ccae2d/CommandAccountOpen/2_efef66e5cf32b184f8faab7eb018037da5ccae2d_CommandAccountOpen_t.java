 package de.autoit4you.bankaccount.commands;
 
 import java.util.List;
 
 import de.autoit4you.bankaccount.BankAccount;
 import de.autoit4you.bankaccount.api.Account;
 import de.autoit4you.bankaccount.exceptions.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 public class CommandAccountOpen extends BankAccountCommand {
 
 	@Override
 	public void run(CommandSender sender, String[] args, BankAccount plugin)
             throws BAArgumentException, CommandPermissionException{
		if(args.length < 2 || args[1] == null)
 			throw new BAArgumentException();
 		
 		if(!BankAccount.perm.user(sender, args))
 			throw new CommandPermissionException();
 		
 		if(args[1].length() > 250) {
 			sender.sendMessage(ChatColor.RED + "Accountname is too long!");
             return;
 		}
 
         try {
             Account acc = plugin.getAPI().open(args[1].toString());
             acc.setAccess(sender.getName(), 3);
             sender.sendMessage(ChatColor.GREEN + "Account opened!");
         } catch (AccountExistException e) {
             sender.sendMessage(ChatColor.RED + "That name is already taken!");
         }
 	}
 
 	@Override
 	public List<String> tab(CommandSender sender, String[] args) {
 		return null;
 	}
 
 }
