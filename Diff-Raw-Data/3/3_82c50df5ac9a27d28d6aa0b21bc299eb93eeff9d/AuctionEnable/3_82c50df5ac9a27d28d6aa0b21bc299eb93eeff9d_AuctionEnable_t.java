 package net.invisioncraft.plugins.salesmania.commands.auction;
 
 import net.invisioncraft.plugins.salesmania.CommandHandler;
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import net.invisioncraft.plugins.salesmania.event.AuctionEvent;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 /**
  * Owner: Byte 2 O Software LLC
  * Date: 6/1/12
  * Time: 11:28 PM
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
 public class AuctionEnable extends CommandHandler {
     AuctionSettings auctionSettings;
     public AuctionEnable(Salesmania plugin) {
         super(plugin);
         auctionSettings = plugin.getSettings().getAuctionSettings();
     }
 
     @Override
     public boolean execute(CommandSender sender, Command command, String label, String[] args) {
         Locale locale = localeHandler.getLocale(sender);
 
         if(args[0].equalsIgnoreCase("enable") && sender.hasPermission("salesmania.auction.enable")) {
             if(auctionSettings.getEnabled()) {
                 sender.sendMessage(locale.getMessage("Auction.alreadyEnabled"));
             }
             else {
                 sender.sendMessage(locale.getMessage("Auction.enabled"));
                auctionSettings.setEnabled(true);
                 plugin.getServer().getPluginManager().callEvent(new AuctionEvent(plugin.getAuction(), AuctionEvent.EventType.ENABLE));
             }
         }
 
         else if(args[0].equalsIgnoreCase("disable") && sender.hasPermission("salesmania.auction.disable")) {
             if(!auctionSettings.getEnabled()) {
                 sender.sendMessage(locale.getMessage("Auction.alreadyDisabled"));
             }
             else {
                 sender.sendMessage(locale.getMessage("Auction.disabled"));
                auctionSettings.setEnabled(false);
                 plugin.getServer().getPluginManager().callEvent(new AuctionEvent(plugin.getAuction(), AuctionEvent.EventType.DISABLE));
             }
         }
         return true;
     }
 }
