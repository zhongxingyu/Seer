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
 
 package net.invisioncraft.plugins.salesmania;
 
 import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
 import net.invisioncraft.plugins.salesmania.configuration.Locale;
 import net.invisioncraft.plugins.salesmania.event.AuctionEvent;
 import net.invisioncraft.plugins.salesmania.util.ItemManager;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Auction {
     private static long TICKS_PER_SECOND = 20;
     Salesmania plugin;
     Economy economy;
     AuctionSettings auctionSettings;
 
     private boolean isRunning = false;
     private boolean inCooldown = false;
 
     private Player owner;
     private Player winner;
     private Player lastWinner;
     private double bid;
     private double lastBid;
 
     private double startTax = 0;
     private double endTax = 0;
 
     private ItemStack itemStack;
 
     private long timeRemaining = 0;
     public static String PLAYER_QUEUE_METADATA = "AUCTIONS_IN_QUEUE";
 
     private Runnable cooldownRunnable = new Runnable() {
         @Override
         public void run() {
             inCooldown = false;
         }
     };
 
     private Runnable timerRunnable = new Runnable() {
         @Override
         public void run() {
             if(isRunning) {
                 timeRemaining -= 1;
                 if (timeRemaining == 0) end();
                 callTimerEvent();
             }
             else {
                 Bukkit.getServer().getScheduler().cancelTask(timerID);
             }
         }
     };
 
     private int timerID;
 
     public static enum AuctionStatus {
         OVER_MAX,
         UNDER_MIN,
         SUCCESS,
         FAILURE,
         WINNING,
         NOT_RUNNING,
         QUEUE_FULL,
         PLAYER_QUEUE_FULL,
         OWNER,
         CANT_AFFORD_TAX
     }
 
     private HashMap<String, String> tokenMap;
     private static Pattern tokenPattern;
     private static String[] tokens = new String[] {
             "%owner%", "%quantity%", "%item%", "%durability%",
             "%bid%", "%winner%", "%enchantinfo%"
     };
 
     public Auction(Salesmania plugin) {
         this.plugin = plugin;
         auctionSettings = plugin.getSettings().getAuctionSettings();
 
         String patternString = "(";
         for(String token : tokens) {
             patternString += token + "|";
         }
         patternString += ")";
         tokenPattern = Pattern.compile(patternString);
         tokenMap = new HashMap<String, String>();
         economy = plugin.getEconomy();
     }
 
     public boolean isRunning() {
         return isRunning;
     }
 
     public boolean isInCooldown() {
         return inCooldown;
     }
 
     public Player getWinner() {
         return winner;
     }
 
     public Player getOwner() {
         return owner;
     }
 
     public double getBid() {
         return bid;
     }
 
     public double getMaxBid() {
         return bid + auctionSettings.getMaxIncrement();
     }
 
     public double getMinBid() {
         if(lastBid == 0) return bid;
         else return bid + auctionSettings.getMinIncrement();
     }
 
     public ItemStack getItemStack() {
         return itemStack;
     }
 
     public AuctionStatus start(Player player, ItemStack itemStack, double startBid)  {
         AuctionStatus checkResult = performChecks(player, startBid);
         if(checkResult != AuctionStatus.SUCCESS) return checkResult;
 
         bid = startBid;
         lastBid = 0;
         this.itemStack = itemStack;
         winner = null;
         owner = player;
         isRunning = true;
         timeRemaining = auctionSettings.getDefaultTime();
         plugin.getAuctionIgnoreList().setIgnore(player, false);
 
         updateInfoTokens();
         Bukkit.getServer().getPluginManager().callEvent(new AuctionEvent(this, AuctionEvent.EventType.START));
         timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, timerRunnable, TICKS_PER_SECOND, TICKS_PER_SECOND);
         return AuctionStatus.SUCCESS;
     }
 
     public AuctionStatus performChecks(Player player, double startBid) {
         if(plugin.getAuctionQueue().size() >= auctionSettings.getMaxQueueSize()) return AuctionStatus.QUEUE_FULL;
        if(player.getMetadata(PLAYER_QUEUE_METADATA).get(0).asInt() >= plugin.getAuctionQueue().playerSize(player)) return AuctionStatus.PLAYER_QUEUE_FULL;
         if(startBid < auctionSettings.getMinStart()) return AuctionStatus.UNDER_MIN;
         if(startBid > auctionSettings.getMaxStart()) return AuctionStatus.OVER_MAX;
 
         // Tax
         if(auctionSettings.getStartTax() != 0) {
             startTax = auctionSettings.getStartTax();
             if(auctionSettings.isStartTaxPercent()) {
                 startTax = (startTax / 100) * startBid;
             }
             if(!economy.has(player.getName(), startTax)) return AuctionStatus.CANT_AFFORD_TAX;
         }
         return AuctionStatus.SUCCESS;
     }
 
     public int getTimerID() {
         return timerID;
     }
 
     public AuctionStatus bid(Player player, double bid) {
         if(!isRunning) return AuctionStatus.NOT_RUNNING;
         if(player == owner) return AuctionStatus.OWNER;
         if(winner != null && winner == player) return AuctionStatus.WINNING;
         if(bid > getMaxBid()) return AuctionStatus.OVER_MAX;
         if(bid < getMinBid()) return AuctionStatus.UNDER_MIN;
 
         lastWinner = winner;
         lastBid = this.bid;
 
         winner = player;
         this.bid = bid;
         plugin.getAuctionIgnoreList().setIgnore(player, false);
         updateInfoTokens();
         Bukkit.getServer().getPluginManager().callEvent(new AuctionEvent(this, AuctionEvent.EventType.BID));
         return AuctionStatus.SUCCESS;
     }
 
     public void end() {
         if(isRunning()) {
             // Tax
             if(auctionSettings.getEndTax() != 0) {
                 endTax = auctionSettings.getEndTax();
                 if(auctionSettings.isEndTaxPercent()) {
                     endTax = (endTax / 100) * getBid();
                 }
             }
 
             Bukkit.getServer().getPluginManager().callEvent(new AuctionEvent(this, AuctionEvent.EventType.END));
             isRunning = false;
             inCooldown = true;
 
             plugin.getServer().getScheduler().cancelTask(timerID);
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,cooldownRunnable, auctionSettings.getCooldown()*TICKS_PER_SECOND);
         }
     }
 
     public AuctionStatus cancel() {
         if(!isRunning()) return AuctionStatus.NOT_RUNNING;
         Bukkit.getServer().getPluginManager().callEvent(new AuctionEvent(this, AuctionEvent.EventType.CANCEL));
         isRunning = false;
         inCooldown = true;
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, cooldownRunnable, auctionSettings.getCooldown()*TICKS_PER_SECOND);
         return AuctionStatus.SUCCESS;
     }
 
     private float getDurability() {
         float dur = (Float.valueOf(itemStack.getType().getMaxDurability()) - Float.valueOf(itemStack.getDurability())) / Float.valueOf(itemStack.getType().getMaxDurability());
         return dur * 100;
     }
 
     public ArrayList<String> infoReplace(ArrayList<String> infoList) {
         ArrayList<String> newInfoList = new ArrayList<String>();
 
         for(String string : infoList) {
             // Remove unused lines
             if(itemStack.getEnchantments().isEmpty() && string.contains("%enchant%")) continue;
             if(itemStack.getType().getMaxDurability() == 0 && string.contains("%durability%")) continue;
 
             // Remove enchant display from spawner
             if(itemStack.getType() == Material.MOB_SPAWNER && string.contains("%enchantinfo%")) continue;
 
 
             // Replace tokens
             StringBuffer buffer = new StringBuffer();
             Matcher matcher = tokenPattern.matcher(string);
             String value;
             while(matcher.find()) {
                 value = tokenMap.get(matcher.group());
                 if(value != null) matcher.appendReplacement(buffer, value);
             }
             matcher.appendTail(buffer);
             newInfoList.add(buffer.toString());
 
         }
         return newInfoList;
     }
 
     public ArrayList<String> enchantReplace(ArrayList<String> infoList, String enchant, String enchantInfo, Locale locale) {
         if(itemStack.getEnchantments().isEmpty()) {
             infoList.remove("%enchantinfo%");
             return infoList;
         }
         if(!infoList.contains("%enchantinfo%")) return infoList;
         for(Map.Entry<Enchantment, Integer> ench : itemStack.getEnchantments().entrySet()) {
             enchant += enchantInfo.replace("%enchantlvl%", String.valueOf(ench.getValue()));
             if(locale != null) enchant = enchant.replace("%enchant%", locale.getMessage("Enchantment." + ench.getKey().getName()));
             else enchant = enchant.replace("%enchant%", ench.getKey().getName());
         }
         infoList.set(infoList.indexOf("%enchantinfo%"), enchant);
         return infoList;
     }
 
     private void updateInfoTokens() {
         tokenMap.put("%owner%", owner.getName());
         tokenMap.put("%quantity%", String.valueOf(itemStack.getAmount()));
         tokenMap.put("%item%", ItemManager.getName(itemStack));
         tokenMap.put("%durability%", String.format("%.2f%%", getDurability()));
         tokenMap.put("%bid%", String.format("%,.2f", bid));
         if(winner != null) tokenMap.put("%winner%", winner.getName());
         else tokenMap.put("%winner%", "None");
     }
 
     public long getTimeRemaining() {
         return timeRemaining;
     }
 
     public void setTimeRemaining(long time) {
         timeRemaining = time;
     }
 
     private void callTimerEvent() {
         Bukkit.getServer().getPluginManager().callEvent(new AuctionEvent(this, AuctionEvent.EventType.TIMER));
     }
 
     public Salesmania getPlugin() {
         return plugin;
     }
 
     public double getLastBid() {
         return lastBid;
     }
 
     public Player getLastWinner() {
         return lastWinner;
     }
 
     public double getStartTax() {
         return startTax;
     }
 
     public double getEndTax() {
         return endTax;
     }
 }
