 package com.github.omwah.SDFEconomy.commands;
 
 import com.github.omwah.SDFEconomy.BankAccount;
 import com.github.omwah.SDFEconomy.SDFEconomyAPI;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class BankRenameCommand extends BasicCommand {
 
     private SDFEconomyAPI api;
 
     public BankRenameCommand(SDFEconomyAPI api) {
         super("bank rename");
 
         this.api = api;
         
         setDescription("Renames a bank account");
         setUsage(this.getName() + " §8<old_account_name> <new_account_name>");
         setArgumentRange(2, 2);
         setIdentifiers(this.getName());
         setPermission("sdfeconomy.use_bank");
     }
 
     @Override
     public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args)
     {
         String old_account_name = args[0];
         String new_account_name = args[1];
         BankAccount old_account = api.getBankAccount(old_account_name);
 
         if (old_account != null) {
             if(handler.hasAdminPermission(sender) || 
                     sender instanceof Player && old_account.isOwner(((Player)sender).getName())) {
                 // Create a new bank with same attributes as old but with a new name              
                 EconomyResponse create_res = api.createBank(new_account_name, old_account.getOwner(), old_account.getLocation());
                 if(create_res.type != ResponseType.SUCCESS) {
                     sender.sendMessage("Failed to create renamed bank account: " + create_res.errorMessage);
                     return false;
                 }
                 
                 // Set members the same as the old bank
                 BankAccount new_account = api.getBankAccount(new_account_name);
                 new_account.setMembers(old_account.getMembers());
                 
                // Set balance of new bank account same as old one
                new_account.setBalance(old_account.getBalance());
                
                 // Delete old bank account
                 EconomyResponse delete_res = api.deleteBank(old_account_name);
                 if(delete_res.type != ResponseType.SUCCESS) {
                     sender.sendMessage("Failed to remove old bank account: " + delete_res.errorMessage);
                     return false;
                 }
 
                 sender.sendMessage("Succesfully renamed bank: " + old_account_name + " to: " + new_account_name);
             } else {
                 sender.sendMessage("You are not the owner of the bank: " + old_account_name);
                 return false;
             }
         } else {
             sender.sendMessage("No bank named " + old_account_name + " was found");
             return false;
         }
             
         return true;
     }
    
 }
