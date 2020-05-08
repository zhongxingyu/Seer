 package net.invisioncraft.plugins.salesmania.listeners;
 
 import net.invisioncraft.plugins.salesmania.Auction;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.AuctionIgnoreList;
 import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import net.invisioncraft.plugins.salesmania.configuration.LocaleHandler;
 import net.invisioncraft.plugins.salesmania.event.AuctionEvent;
 import net.invisioncraft.plugins.salesmania.util.ItemManager;
 import net.invisioncraft.plugins.salesmania.util.MsgUtil;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Owner: Byte 2 O Software LLC
  * Date: 5/25/12
  * Time: 5:08 AM
  */
 /*
 Copyright 2012 Byte 2 O Software LLC
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 public class AuctionEventListener implements Listener {
 
     AuctionEvent auctionEvent;
     Salesmania plugin;
     Auction auction;
     AuctionIgnoreList auctionIgnoreList;
     AuctionSettings auctionSettings;
     Economy economy;
     LocaleHandler localeHandler;
 
     @EventHandler
     public void onAuctionEvent(AuctionEvent auctionEvent) {
         this.auctionEvent = auctionEvent;
         auction = auctionEvent.getAuction();
         plugin = auction.getPlugin();
         auctionSettings = plugin.getSettings().getAuctionSettings();
         auctionIgnoreList = plugin.getAuctionIgnoreList();
         economy = plugin.getEconomy();
         localeHandler = plugin.getLocaleHandler();
         switch (auctionEvent.getEventType()) {
             case BID: onAuctionBidEvent(); break;
             case END: onAuctionEndEvent(); break;
             case START: onAuctionStartEvent(); break;
             case TIMER: onAuctionTimerEvent(); break;
             case CANCEL: onAuctionCancelEvent(); break;
             case ENABLE: onAuctionEnableEvent(); break;
             case DISABLE: onAuctionDisableEvent(); break;
         }
     }
 
     private void onAuctionTimerEvent() {
         long timeRemaining = auctionEvent.getAuction().getTimeRemaining();
         List<Long> notifyTimes = auctionSettings.getNofityTime();
         if(notifyTimes.contains(timeRemaining)) {
             for(Locale locale : localeHandler.getLocales()) {
                 String message =
                         locale.getMessage("Auction.tag") +
                        String.format(locale.getMessage("Auction.timeRemaining"), timeRemaining);
                 locale.broadcastMessage(message, auctionIgnoreList);
             }
 
         }
     }
 
     private void onAuctionStartEvent() {
 
         // Take item
         ItemManager.takeItem(auction.getOwner(), auction.getItemStack());
 
         // Broadcast
         for(Locale locale : localeHandler.getLocales()) {
             ArrayList<String> infoList = locale.getMessageList("Auction.startInfo");
             infoList = auction.infoReplace(infoList);
             infoList = auction.enchantReplace(infoList,
                     locale.getMessage("Auction.enchant"),
                     locale.getMessage("Auction.enchantInfo"), locale);
             infoList = MsgUtil.addPrefix(infoList, locale.getMessage("Auction.tag"));
             locale.broadcastMessage(infoList, auctionIgnoreList);
         }
     }
 
     public void onAuctionBidEvent() {
         // Anti-Snipe
         if(auction.getTimeRemaining() < auctionSettings.getSnipeTime()) {
             auction.setTimeRemaining(auction.getTimeRemaining() + auctionSettings.getSnipeValue());
         }
 
         // Give back last bid
         if(auction.getLastWinner() != null) {
             Player player = auction.getLastWinner();
             economy.depositPlayer(player.getName(), auction.getLastBid());
             Locale locale = plugin.getLocaleHandler().getLocale(player);
             player.sendMessage(String.format(
                     locale.getMessage("Auction.Bidding.outBid"), auction.getWinner().getName()));
         }
 
         // Take new bid
         economy.withdrawPlayer(auction.getWinner().getName(), auction.getBid());
 
         // Broadcast
         for(Locale locale : localeHandler.getLocales()) {
             String message = locale.getMessage("Auction.tag") +
             String.format(locale.getMessage("Auction.bidRaised"),
                 auction.getBid(), auction.getWinner().getName());
             locale.broadcastMessage(message, auctionIgnoreList);
         }
     }
 
     public void onAuctionEndEvent() {
         // NO BIDS
         if(plugin.getAuction().getWinner() == null) {
             // Broadcast
             for(Locale locale : localeHandler.getLocales()) {
                 String message =
                         locale.getMessage("Auction.tag") +
                         locale.getMessage("Auction.noBids");
                 locale.broadcastMessage(message, auctionIgnoreList);
             }
             // Give back item to owner
             giveItem(auction.getOwner(), auction.getItemStack());
         }
 
         // BIDS
         else  {
             // Check if winner logged off
             if(!auction.getWinner().isOnline()) {
                 for(Locale locale : localeHandler.getLocales()) {
                     String message = locale.getMessage("Auction.tag") +
                             locale.getMessage("Auction.winnerOffline");
                     locale.broadcastMessage(message, auctionIgnoreList);
                 }
                 auction.cancel();
                 return;
             }
 
             // Broadcast
             for(Locale locale : localeHandler.getLocales()) {
                 ArrayList<String> infoList = locale.getMessageList("Auction.endInfo");
                 infoList = auction.infoReplace(infoList);
                 infoList = auction.enchantReplace(infoList,
                         locale.getMessage("Auction.enchant"),
                         locale.getMessage("Auction.enchantInfo"), locale);
                 infoList = MsgUtil.addPrefix(infoList, locale.getMessage("Auction.tag"));
                 locale.broadcastMessage(infoList, auctionIgnoreList);
             }
 
             // Give item to winner
             giveItem(auction.getWinner(), auction.getItemStack());
 
             // Give money to owner
             economy.depositPlayer(auction.getOwner().getName(), auction.getBid());
         }
     }
 
     public void onAuctionCancelEvent() {
         // Give back bid
         if(auction.getWinner() != null) {
             economy.depositPlayer(auction.getWinner().getName(), auction.getBid());
         }
 
         // Give back item to owner
         giveItem(auction.getOwner(), auction.getItemStack());
 
         // Broadcast
         for(Locale locale : localeHandler.getLocales()) {
             String message = locale.getMessage("Auction.tag") +
                     locale.getMessage("Auction.canceled");
             locale.broadcastMessage(message, auctionIgnoreList);
         }
     }
 
     public void onAuctionEnableEvent() {
         // Broadcast
         for(Locale locale : localeHandler.getLocales()) {
             String message = locale.getMessage("Auction.tag") +
                     locale.getMessage("Auction.enabled");
             locale.broadcastMessage(message, auctionIgnoreList);
         }
     }
 
     public void onAuctionDisableEvent() {
         // Cancel current auction
         auction.cancel();
 
         // Broadcast
         for(Locale locale : localeHandler.getLocales()) {
             String message = locale.getMessage("Auction.tag") +
                     locale.getMessage("Auction.disabled");
             locale.broadcastMessage(message, auctionIgnoreList);
         }
     }
 
     private void giveItem(Player player, ItemStack itemStack) {
         HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(itemStack);
         if(remainingItems.isEmpty()) return;
         else for(Map.Entry<Integer, ItemStack> entry : remainingItems.entrySet()) {
             player.getWorld().dropItem(player.getLocation(), entry.getValue());
         }
         Locale locale = plugin.getLocaleHandler().getLocale(player);
         player.sendMessage(locale.getMessage("Auction.inventoryFull"));
     }
 }
