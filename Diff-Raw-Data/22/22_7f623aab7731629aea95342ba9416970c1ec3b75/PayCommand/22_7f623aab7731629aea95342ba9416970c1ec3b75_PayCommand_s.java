 package com.github.omwah.SDFEconomy.commands;
 
 import com.github.omwah.SDFEconomy.SDFEconomyAPI;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PayCommand extends BasicCommand
 {
     private final SDFEconomyAPI api;
     private final Server server;
 
     public PayCommand(SDFEconomyAPI api, Server server)
     {
         super("pay");
         
         this.api = api;
         this.server = server;
         
         setDescription("Pay another player in your current location");
         setUsage(this.getName() + " §8<player_name> <amount>");
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
                 sender.sendMessage("Invalid amount specified: " + args[1]);
                 return false;
             }
             
             String amount_str = api.format(amount);
             
             // Be paranoid, check that sender has an account
             if (!api.hasAccount(payer, location)) {
                 sender.sendMessage("You does not have an account @ " + location);
                 return true;
             }
             
             // Do not proceed any further if there is no destination account in
             // existence
             if (!api.hasAccount(payee, location)) {
                 sender.sendMessage(payee + " does not have an account @ " + location);
                 return true;
             }
            
             // Withdraw money from player and make sure this command succeeds
            EconomyResponse resp_wd = api.withdrawPlayer(payer, amount);
            
             if (resp_wd.type != ResponseType.SUCCESS) {
                sender.sendMessage("You do not have enough money to pay " + amount_str + " to " + payee);
                 return true;
             }
             
             // Despost in destination account, making sure if this fails to credit
             // back payer
            EconomyResponse resp_dep = api.depositPlayer(payee, amount);
             
             if (resp_dep.type != ResponseType.SUCCESS) {
                sender.sendMessage("Could not deposit " + amount_str + " into the account of " + payee + " crediting your account back.");
                resp_dep = api.depositPlayer(payer, amount);
                 
                 // Wow, something is fubar, offer our apologies
                 if (resp_dep.type != ResponseType.SUCCESS) {
                     sender.sendMessage("Could not credit back " + amount_str + " to your account. Sorry :-(");
                     return false;
                 }
             }
                     
             String balance = api.format(api.getBalance(payer, location));
            sender.sendMessage("Payment of " + amount_str + " to " + payee + " succeeded, your balance is now " + balance);
             
             // Send message to destination player
             Player dest_player = server.getPlayer(payee);
             if(dest_player != null && dest_player.isOnline()) {
                 balance = api.format(api.getBalance(payee, location));
                dest_player.sendMessage(payer + " has sent you " + amount_str + " your balance is now: " + balance);
             }
         } else {
             // This will only be sent when command issued from console
             sender.sendMessage("Pay command must be used by a player");
         }
         return true;
     }
 }
