 package net.invisioncraft.plugins.salesmania.listeners;
 
 import net.invisioncraft.plugins.salesmania.Auction;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
 import net.invisioncraft.plugins.salesmania.configuration.IgnoreAuction;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import net.invisioncraft.plugins.salesmania.event.AuctionEvent;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 
 import java.util.List;
 
 /**
  * Owner: Justin
  * Date: 5/25/12
  * Time: 5:08 AM
  */
 public class AuctionEventListener implements Listener {
     AuctionEvent auctionEvent;
     Salesmania plugin;
     Auction auction;
     IgnoreAuction ignoreAuction;
     AuctionSettings auctionSettings;
     @EventHandler
     public void onAuctionEvent(AuctionEvent auctionEvent) {
         this.auctionEvent = auctionEvent;
         auction = auctionEvent.getAuction();
         plugin = auction.getPlugin();
         auctionSettings = plugin.getSettings().getAuctionSettings();
         ignoreAuction = plugin.getIgnoreAuction();
         switch (auctionEvent.getEventType()) {
             case BID:
                 onAuctionBidEvent();
                 break;
             case END:
                 onAuctionEndEvent();
                 break;
             case START:
                 onAuctionStartEvent();
                 break;
             case TIMER:
                 onAuctionTimerEvent();
                 break;
         }
     }
 
     private void onAuctionTimerEvent() {
         long timeRemaining = auctionEvent.getAuction().getTimeRemaining();
         List<Long> notifyTimes = auctionSettings.getNofityTime();
         if(notifyTimes.contains(timeRemaining)) {
             for(Player player : plugin.getServer().getOnlinePlayers()) {
                 if(ignoreAuction.isIgnored(player)) continue;
                 Locale locale = plugin.getLocaleHandler().getLocale(player);
                 player.sendMessage(String.format(
                         locale.getMessage("Auction.tag") + locale.getMessage("Auction.timeRemaining"),
                         timeRemaining));
             }
         }
     }
 
     private void onAuctionStartEvent() {
         // Broadcast
         for(Player player : plugin.getServer().getOnlinePlayers()) {
             if(ignoreAuction.isIgnored(player)) continue;
             Locale locale = plugin.getLocaleHandler().getLocale(player);
             for(String message : locale.getMessageList("Auction.startInfo")) {
                 message = locale.getMessage("Auction.tag") + message;
                 message = auction.infoReplace(message);
                 player.sendMessage(message);
             }
         }
     }
 
     public void onAuctionBidEvent() {
         // Broadcast
         for(Player player : plugin.getServer().getOnlinePlayers()) {
             if(ignoreAuction.isIgnored(player)) continue;
             Locale locale = plugin.getLocaleHandler().getLocale(player);
             String message = String.format(locale.getMessage("Auction.bidRaised"),
                     auction.getCurrentBid(), auction.getWinner());
             player.sendMessage(message);
         }
     }
 
     public void onAuctionEndEvent() {
         // Broadcast
        if(plugin.getAuction().getWinner() == plugin.getAuction().getOwner()) {
            for(Player player : plugin.getServer().getOnlinePlayers()) {
                if(ignoreAuction.isIgnored(player)) continue;
                Locale locale = plugin.getLocaleHandler().getLocale(player);
                player.sendMessage(locale.getMessage("Auction.tag") + locale.getMessage("Auction.noBids"));
            }
        }
        else for(Player player : plugin.getServer().getOnlinePlayers()) {
             if(ignoreAuction.isIgnored(player)) continue;
             Locale locale = plugin.getLocaleHandler().getLocale(player);
             for(String message : locale.getMessageList("Auction.endInfo")) {
                 message = locale.getMessage("Auction.tag") + message;
                 message = auction.infoReplace(message);
                 player.sendMessage(message);
             }
         }
     }
 
     public void onAuctionCancelEvent() {
         for(Player player : plugin.getServer().getOnlinePlayers()) {
             if(ignoreAuction.isIgnored(player)) continue;
             Locale locale = plugin.getLocaleHandler().getLocale(player);
             player.sendMessage(locale.getMessage("Auction.canceled"));
         }
     }
 }
