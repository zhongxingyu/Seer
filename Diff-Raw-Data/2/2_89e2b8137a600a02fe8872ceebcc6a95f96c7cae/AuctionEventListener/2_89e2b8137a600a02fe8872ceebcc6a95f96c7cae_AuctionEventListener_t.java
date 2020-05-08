 package net.invisioncraft.plugins.salesmania.listeners;
 
 import net.invisioncraft.plugins.salesmania.Auction;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
 import net.invisioncraft.plugins.salesmania.configuration.IgnoreAuction;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import net.invisioncraft.plugins.salesmania.event.AuctionEvent;
 import net.invisioncraft.plugins.salesmania.util.ItemManager;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 
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
     IgnoreAuction ignoreAuction;
     AuctionSettings auctionSettings;
     Economy economy;
     @EventHandler
     public void onAuctionEvent(AuctionEvent auctionEvent) {
         this.auctionEvent = auctionEvent;
         auction = auctionEvent.getAuction();
         plugin = auction.getPlugin();
         auctionSettings = plugin.getSettings().getAuctionSettings();
         ignoreAuction = plugin.getIgnoreAuction();
         economy = plugin.getEconomy();
 
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
         // Logging
         Locale locale = plugin.getLocaleHandler().getLocale(plugin.getServer().getConsoleSender());
         List<String> infoList = locale.getMessageList("Auction.startInfo");
         infoList = auction.infoReplace(infoList);
         infoList = auction.enchantReplace(infoList,
                 locale.getMessage("Auction.enchant"),
                 locale.getMessage("Auction.enchantInfo"), locale);
         plugin.getLogger().info(ChatColor.stripColor(infoList.toString()));
 
         // Take item
         ItemManager.takeItem(auction.getOwner(), auction.getItemStack());
 
         // Broadcast
         for(Player player : plugin.getServer().getOnlinePlayers()) {
             if(ignoreAuction.isIgnored(player)) continue;
             locale = plugin.getLocaleHandler().getLocale(player);
             infoList = locale.getMessageList("Auction.startInfo");
             infoList = auction.infoReplace(infoList);
             infoList = auction.enchantReplace(infoList,
                     locale.getMessage("Auction.enchant"),
                     locale.getMessage("Auction.enchantInfo"), locale);
             infoList = auction.addTag(infoList, locale.getMessage("Auction.tag"));
             player.sendMessage(infoList.toArray(new String[0]));
         }
     }
 
     public void onAuctionBidEvent() {
         // Anti-Snipe
         if(auction.getTimeRemaining() < auctionSettings.getSnipeTime()) {
             auction.setTimeRemaining(auction.getTimeRemaining() + auctionSettings.getSnipeValue());
         }
 
         // Logging
         Locale locale = plugin.getLocaleHandler().getLocale(plugin.getServer().getConsoleSender());
         plugin.getLogger().info(ChatColor.stripColor(String.format(locale.getMessage("Auction.bidRaised"),
                 auction.getBid(), auction.getWinner().getName())));
 
         // Give back last bid
         if(auction.getLastWinner() != null) {
             Player player = auction.getLastWinner();
             economy.depositPlayer(player.getName(), auction.getLastBid());
             locale = plugin.getLocaleHandler().getLocale(player);
             player.sendMessage(String.format(
                    locale.getMessage("Auction.Bidding.outBid"), auction.getWinner().getName()));
         }
 
         // Take new bid
         economy.withdrawPlayer(auction.getWinner().getName(), auction.getBid());
 
         // Broadcast
         for(Player player : plugin.getServer().getOnlinePlayers()) {
             if(ignoreAuction.isIgnored(player)) continue;
             locale = plugin.getLocaleHandler().getLocale(player);
             String message = locale.getMessage("Auction.tag");
             message += String.format(locale.getMessage("Auction.bidRaised"),
                     auction.getBid(), auction.getWinner().getName());
             player.sendMessage(message);
         }
     }
 
     public void onAuctionEndEvent() {
         Locale locale = plugin.getLocaleHandler().getLocale(plugin.getServer().getConsoleSender());
 
         // NO BIDS
         if(plugin.getAuction().getWinner() == null) {
             // Logging
             locale = plugin.getLocaleHandler().getLocale(plugin.getServer().getConsoleSender());
             plugin.getLogger().info(ChatColor.stripColor(locale.getMessage("Auction.noBids")));
 
             // Broadcast
             for(Player player : plugin.getServer().getOnlinePlayers()) {
                 if(ignoreAuction.isIgnored(player)) continue;
                 locale = plugin.getLocaleHandler().getLocale(player);
                 player.sendMessage(locale.getMessage("Auction.tag") + locale.getMessage("Auction.noBids"));
             }
             // Give back item to owner
             giveItem(auction.getOwner(), auction.getItemStack());
         }
 
         // BIDS
         else  {
             // Logging
             List<String> infoList = locale.getMessageList("Auction.endInfo");
             infoList = auction.infoReplace(infoList);
             infoList = auction.enchantReplace(infoList,
                     locale.getMessage("Auction.enchant"),
                     locale.getMessage("Auction.enchantInfo"), locale);
             plugin.getLogger().info(ChatColor.stripColor(infoList.toString()));
 
             // Broadcast
             for(Player player : plugin.getServer().getOnlinePlayers()) {
                 if(ignoreAuction.isIgnored(player)) continue;
                 locale = plugin.getLocaleHandler().getLocale(player);
                 infoList = locale.getMessageList("Auction.endInfo");
                 infoList = auction.infoReplace(infoList);
                 infoList = auction.enchantReplace(infoList,
                         locale.getMessage("Auction.enchant"),
                         locale.getMessage("Auction.enchantInfo"), locale);
                 infoList = auction.addTag(infoList, locale.getMessage("Auction.tag"));
                 player.sendMessage(infoList.toArray(new String[0]));
             }
 
             // Give item to winner
             giveItem(auction.getWinner(), auction.getItemStack());
 
             // Give money to owner
             economy.depositPlayer(auction.getOwner().getName(), auction.getBid());
         }
     }
 
     public void onAuctionCancelEvent() {
         // Logging
         Locale locale = plugin.getLocaleHandler().getLocale(plugin.getServer().getConsoleSender());
         plugin.getLogger().info(locale.getMessage("Auction.canceled"));
 
         // Give back bid
         if(auction.getWinner() != null) {
             economy.depositPlayer(auction.getWinner().getName(), auction.getBid());
         }
 
         // Give back item to owner
         giveItem(auction.getOwner(), auction.getItemStack());
 
         // Broadcast
         for(Player player : plugin.getServer().getOnlinePlayers()) {
             if(ignoreAuction.isIgnored(player)) continue;
             locale = plugin.getLocaleHandler().getLocale(player);
             player.sendMessage(locale.getMessage("Auction.tag") + locale.getMessage("Auction.canceled"));
         }
     }
 
     public void onAuctionEnableEvent() {
         // Logging
         Locale locale = plugin.getLocaleHandler().getLocale(plugin.getServer().getConsoleSender());
         plugin.getLogger().info(locale.getMessage("Auction.enabled"));
 
         // Broadcast
         for(Player player : plugin.getServer().getOnlinePlayers()) {
             if(ignoreAuction.isIgnored(player)) continue;
             locale = plugin.getLocaleHandler().getLocale(player);
             player.sendMessage(locale.getMessage("Auction.tag") + locale.getMessage("Auction.enabled"));
         }
     }
 
     public void onAuctionDisableEvent() {
         // Logging
         Locale locale = plugin.getLocaleHandler().getLocale(plugin.getServer().getConsoleSender());
         plugin.getLogger().info(locale.getMessage("Auction.disabled"));
 
         // Cancel current auction
         auction.cancel();
 
         // Broadcast
         for(Player player : plugin.getServer().getOnlinePlayers()) {
             if(ignoreAuction.isIgnored(player)) continue;
             locale = plugin.getLocaleHandler().getLocale(player);
             player.sendMessage(locale.getMessage("Auction.tag") + locale.getMessage("Auction.disabled"));
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
