 package net.invisioncraft.plugins.salesmania.commands.auction;
 
 import net.invisioncraft.plugins.salesmania.CommandHandler;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * Owner: Justin
  * Date: 5/17/12
  * Time: 10:27 AM
  */
 public class AuctionCancel extends CommandHandler {
     public AuctionCancel(Salesmania plugin) {
         super(plugin);
     }
 
     @Override
     public boolean execute(CommandSender sender, Command command, String label, String[] args) {
         Locale locale = plugin.getLocaleHandler().getLocale(sender);
         if((sender instanceof Player)) {
            if(!sender.hasPermission("salesmania.auction.cancel") | (Player)sender != plugin.getAuction().getOwner()) {
                sender.sendMessage(
                        locale.getMessage("Permission.noPermission") +
                        locale.getMessage("Permission.Auction.cancel"));
             }
             return false;
         }
 
         switch(plugin.getAuction().cancel()) {
             case NOT_RUNNING:
                 sender.sendMessage(locale.getMessage("Auction.notRunning"));
                 return true;
         }
         return false;
     }
 }
