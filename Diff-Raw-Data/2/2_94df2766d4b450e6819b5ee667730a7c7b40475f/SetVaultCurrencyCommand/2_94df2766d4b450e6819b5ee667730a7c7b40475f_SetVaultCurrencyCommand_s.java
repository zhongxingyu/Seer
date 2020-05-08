 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.xhawk87.Coinage.commands;
 
 import me.xhawk87.Coinage.Coinage;
 import me.xhawk87.Coinage.Currency;
 import org.bukkit.command.CommandSender;
 
 /**
  *
  * @author XHawk87
  */
 public class SetVaultCurrencyCommand extends CoinCommand {
 
     private Coinage plugin;
 
     public SetVaultCurrencyCommand(Coinage plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public String getHelpMessage(CommandSender sender) {
         return "/SetVaultCurrency [name|none]. Sets the default currency to use for Vault transactions, or 'none' to not use Coinage for Vault. The name must be the id of the currency not the display alias.";
     }
 
     @Override
     public String getPermission() {
         return "coinage.commands.setvaultcurrency";
     }
 
     @Override
     public boolean execute(CommandSender sender, String[] args) {
         if (args.length != 1) {
             return false;
         }
 
         String name = args[0];
         Currency currency = plugin.getCurrency(name);
         if (currency == null) {
             sender.sendMessage("There is no currency with id " + name);
             return true;
         }
         if (plugin.setVaultCurrency(currency)) {
            sender.sendMessage(currency.toString() + " is now the default currency used in Vault transactions. A restart may be requires for all other plugins to notice the change");
         } else {
             sender.sendMessage("You must have Vault installed to set its default currency");
 
         }
         return true;
     }
 }
