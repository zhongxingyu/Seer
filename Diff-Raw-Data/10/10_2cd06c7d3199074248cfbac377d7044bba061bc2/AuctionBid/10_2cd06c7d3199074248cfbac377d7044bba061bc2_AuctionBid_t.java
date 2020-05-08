 package net.invisioncraft.plugins.salesmania.commands.auction;
 
 import net.invisioncraft.plugins.salesmania.Auction;
 import net.invisioncraft.plugins.salesmania.CommandHandler;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * Owner: Justin
  * Date: 5/17/12
  * Time: 10:25 AM
  */
 public class AuctionBid extends CommandHandler {
     public AuctionBid(Salesmania plugin) {
         super(plugin);
     }
 
     @Override
     public boolean execute(CommandSender sender, Command command, String label, String[] args) {
         Locale locale = plugin.getLocaleHandler().getLocale(sender);
         if(!(sender instanceof Player)) {
             sender.sendMessage(locale.getMessage("Console.cantBid"));
         }
         Player player = (Player) sender;
        long bidAmount;

        try {
            bidAmount = Long.valueOf(args[1]);
        }   catch (NumberFormatException ex) {
            sender.sendMessage(locale.getMessage("Syntax.Auction.auctionBid"));
            return false;
        }

         Auction auction = plugin.getAuction();
 
         if(!player.hasPermission("salesmania.auction.bid")) {
             player.sendMessage(String.format(
                     locale.getMessage("Permission.noPermission"),
                     locale.getMessage("Permisson.Auction.bid")));
             return false;
         }
         switch(auction.bid(player, bidAmount)) {
             case SUCCESS:
                 player.sendMessage(String.format(
                         locale.getMessage("Auction.Bidding.bidSuccess"),
                         bidAmount, auction.getItemStack().getType().name()));
                 return true;
             case OVER_MAX:
                 player.sendMessage(String.format(
                         locale.getMessage("Auction.Bidding.overMax"),
                         auction.getMaxBid()));
                 return true;
             case UNDER_MIN:
                 player.sendMessage(String.format(
                         locale.getMessage("Auction.Bidding.underMin"),
                         auction.getMinBid()));
                 return false;
             case NOT_RUNNING:
                 player.sendMessage(locale.getMessage("Auction.notRunning"));
                 return false;
             case WINNING:
                 player.sendMessage(locale.getMessage("Auction.Bidding.playerWinning"));
             case OWNER:
                 player.sendMessage(locale.getMessage("Auction.Bidding.playerOwner"));
                 return false;
         }
 
         return false;
     }
 }
