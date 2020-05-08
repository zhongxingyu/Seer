 package com.github.omwah.SDFEconomy.commands;
 
 import com.github.omwah.SDFEconomy.SDFEconomyAPI;
 import com.github.omwah.omcommands.CommandHandler;
 import com.github.omwah.omcommands.TranslatedCommand;
 import java.util.ResourceBundle;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PayCommand extends TranslatedCommand
 {
     private final SDFEconomyAPI api;
     private final Server server;
 
     public PayCommand(SDFEconomyAPI api, Server server, ResourceBundle translation)
     {
         super("pay", translation);
         
         this.api = api;
         this.server = server;
         
         setArgumentRange(2, 2);
         setIdentifiers(this.getName());
         setPermission("sdfeconomy.pay_players");
     }
 
     @Override
     public boolean execute(CommandHandler handler, CommandSender sender, String label, String identifier, String[] args)
     {
         // This command only works for Players, it pays the other player in the current world
         if (sender instanceof Player) {
             Player player = (Player) sender;
             String payer = player.getName();
             String location = api.getPlayerLocationName(payer);
             
             String payee = args[0];
             // Try and parse amount, fail gracefully
             double amount;
             try {
                 amount = Double.parseDouble(args[1]);
             } catch (NumberFormatException e) {
                 sender.sendMessage(getTranslation("AccountCommon-invalid_amount", args[1]));
                 return false;
             }
             
             String amount_str = api.format(amount);
             
             // Be paranoid, check that sender has an account
             if (!api.hasAccount(payer, location)) {
                 sender.sendMessage(getClassTranslation("payer_no_account", location));
                 return true;
             }
             
             // Do not proceed any further if there is no destination account in
             // existence
             if (!api.hasAccount(payee, location)) {
                 sender.sendMessage(getClassTranslation("payee_no_account", payee, location));
                 return true;
             }
                         
             // Withdraw money from player and make sure this command succeeds
             EconomyResponse resp_wd = api.withdrawPlayer(payer, amount, location);
            
             if (resp_wd.type != ResponseType.SUCCESS) {
                sender.sendMessage(getClassTranslation("not_enough_money", amount_str, payee, location));
                 return true;
             }
             
             // Despost in destination account, making sure if this fails to credit
             // back payer
             EconomyResponse resp_dep = api.depositPlayer(payee, amount, location);
             
             if (resp_dep.type != ResponseType.SUCCESS) {
                 sender.sendMessage(getClassTranslation("deposit_failure", amount_str, payee, location));
                 resp_dep = api.depositPlayer(payer, amount, location);
                 
                 // Wow, something is fubar, offer our apologies
                 if (resp_dep.type != ResponseType.SUCCESS) {
                     sender.sendMessage(getClassTranslation("credit_failure", amount_str));
                     return false;
                 }
             }
                     
             String balance = api.format(api.getBalance(payer, location));
             sender.sendMessage(getClassTranslation("payment_success", amount_str, payee, location, balance));
             
             // Send message to destination player
             Player dest_player = server.getPlayer(payee);
             if(dest_player != null && dest_player.isOnline()) {
                 balance = api.format(api.getBalance(payee, location));
                 dest_player.sendMessage(getClassTranslation("notify_payee", payer, amount_str, location, balance));
             }
         } else {
             // This will only be sent when command issued from console
             sender.sendMessage(getClassTranslation("must_be_player"));
         }
         return true;
     }
 }
