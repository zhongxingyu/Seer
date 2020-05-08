 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.hpiz.ShopAds2.Util.Messaging;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.hpiz.ShopAds2.Shop.Shop;
 
 /**
  *
  * @author Chris
  */
 public class ErrorMessage extends ShopAdsMessage {
     
     
     public void overMaxRunTime(Player player, int parseInt) {
         message.console.debug("overMaxRunTime message");
         player.sendMessage(prefix + "The maximum allowed ad time is " + config.getMaxAdRunTime() + " hours. " + ChatColor.RED + "(" + parseInt + ")");
     }
     
     public void insufficientFunds(Player player, double d) {
         player.sendMessage(prefix + "You do not have " + economy.getEconomy().format(d) + " to spare.");
     }
     
      
     public void noShopEntered(Player player) {
         player.sendMessage(prefix + "You must enter a shop name.");
        commandUsage.setCommand(player);
     }
 
     public void noAdEntered(Player player) {
         player.sendMessage(prefix + "You must enter an advertisement.");
         commandUsage.setCommand(player);
     }
 
     public void inputIgnored(Player player, String string) {
         player.sendMessage(prefix + "Paremeters ignored after " + string + ".");
     }
     
     
     public void noNameEntered(Player player) {
         player.sendMessage(prefix + "You must enter a name.");
         commandUsage.setCommand(player);
     }
     
     
     public void notYourShop(Player player, Shop shop) {
         player.sendMessage(prefix + "The shop '" + shop.getShopName() + " is not owned by you.");
     }
     
     
     public void noPermission(Player player, String command) {
         player.sendMessage(prefix + "You do not have permission to use the " + command + " command.");
     }
 
     public void noShopFound( Player player, String shop) {
         player.sendMessage(prefix + "No shop by that name found. (" + shop + ")");
     }
     
 }
