 package net.invisioncraft.plugins.salesmania.configuration;
 
 import net.milkbowl.vault.item.Items;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.List;
 
 /**
  * Owner: Byte 2 O Software LLC
  * Date: 5/29/12
  * Time: 4:07 PM
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
 public class AuctionSettings implements ConfigurationHandler {
     private FileConfiguration config;
     private Settings settings;
     protected AuctionSettings(Settings settings) {
         this.settings = settings;
         update();
     }
 
     public boolean getAllowCreative() {
         return config.getBoolean("Auction.allowCreative");
     }
 
     // Bidding
     public long getCooldown() {
         return config.getLong("Auction.cooldown");
     }
 
     public double getMinStart() {
         return config.getDouble("Auction.minStart");
     }
 
     public double getMaxStart() {
         return config.getDouble("Auction.maxStart");
     }
 
     public double getMinIncrement() {
         return config.getDouble("Auction.Bidding.minIncrement");
     }
 
     public double getMaxIncrement() {
         return config.getDouble("Auction.Bidding.maxIncrement");
     }
 
     public int getDefaultTime() {
         return config.getInt("Auction.Bidding.defaultTime");
     }
 
     public long getMaxTime() {
         return config.getLong("Auction.maxTime");
     }
 
     public List<Long> getNofityTime() {
         return config.getLongList("Auction.notifyTime");
     }
 
     public long getSnipeTime() {
         return config.getLong("Auction.Bidding.snipeTime");
     }
 
     public long getSnipeValue() {
         return config.getLong("Auction.Bidding.snipeValue");
     }
 
     public List<String> getBlacklist() {
         return config.getStringList("Auction.Blacklist");
     }
 
     public boolean isBlacklisted(ItemStack itemStack) {
         List<String> blacklist = getBlacklist();
         if(blacklist.contains(String.valueOf(itemStack.getTypeId()))) return true;
         if(blacklist.contains(itemStack.getType().name())) return true;
        if(blacklist.contains(Items.itemById(itemStack.getTypeId()).getName())) return true;
         return false;
     }
 
     public boolean getEnabled() {
         return config.getBoolean("Auction.enabled");
     }
 
     public void setEnabled(boolean enabled) {
         config.set("Auction.enabled", enabled);
         settings.save();
     }
 
     @Override
     public void update() {
         config = settings.getConfig();
     }
 }
