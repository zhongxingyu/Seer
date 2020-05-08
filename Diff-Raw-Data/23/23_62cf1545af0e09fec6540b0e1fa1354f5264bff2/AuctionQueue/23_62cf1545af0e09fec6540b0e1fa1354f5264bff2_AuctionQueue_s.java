 /*
 This file is part of Salesmania.
 
     Salesmania is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Salesmania is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Salesmania.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.invisioncraft.plugins.salesmania;
 
 import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
 import net.invisioncraft.plugins.salesmania.configuration.Configuration;
 import net.invisioncraft.plugins.salesmania.event.AuctionEvent;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.*;
 
 public class AuctionQueue extends LinkedList<Auction> {
     private Salesmania plugin;
     private QueueConfig queueConfig;
 
     private boolean isRunning = false;
     private boolean isCooldown = false;
     private long cooldownRemaining;
 
     private Auction currentAuction = null;
     private static long TICKS_PER_SECOND = 20;
     private Integer timerID;
 
     AuctionSettings auctionSettings;
 
     private class QueueConfig extends Configuration {
         public QueueConfig(Salesmania plugin) {
             super(plugin, "auctionQueue.yml");
         }
 
         protected void loadQueue(AuctionQueue queue) {
             if(config.contains("Auctions")) {
                 List<Auction>  auctionList = new LinkedList<Auction>();
                 List<Map<?, ?>> savedAuctions = config.getMapList("Auctions");
                 for(Map<?, ?> dataMap : savedAuctions) {
                     ItemStack itemStack = (ItemStack) dataMap.get("itemStack");
                     double startBid = (Double) dataMap.get("currentBid");
                     OfflinePlayer owner = plugin.getServer().getOfflinePlayer((String)dataMap.get("owner"));
                     OfflinePlayer winner = plugin.getServer().getOfflinePlayer((String)dataMap.get("winner"));
                     auctionList.add(new Auction(plugin, owner, winner, itemStack, startBid));
                 }
                 queue.addAll(auctionList);
             }
         }
 
         protected void saveAuction(Auction auction) {
             List<Map<?, ?>> auctionList;
             if(!config.contains("Auctions")) {
                 auctionList = new ArrayList<Map<?, ?>>();
             }
             else {
                 auctionList = config.getMapList("Auctions");
             }
             HashMap<String, Object> dataMap = new HashMap<String, Object>();
             dataMap.put("itemStack", auction.getItemStack());
             dataMap.put("owner", auction.getOwner().getName());
             dataMap.put("currentBid", auction.getBid());
             auctionList.add(dataMap);
             config.set("Auctions", auctionList);
             save();
         }
 
         protected void removeAuction(int position) {
             if(config.contains("Auctions")) {
                 List<Map<?, ?>> auctionList = config.getMapList("Auctions");
                 auctionList.remove(position);
                 config.set("Auctions", auctionList);
                 save();
             }
         }
 
         @SuppressWarnings("unchecked")
         protected void update() {
             if(config.contains("Auctions")) {
                 List<Map<?, ?>> auctionList = config.getMapList("Auctions");
                 Map<String, Object> dataMap = (Map<String, Object>) auctionList.get(0);
                 dataMap.put("currentBid", currentAuction.getBid());
                 dataMap.put("winner", currentAuction.getWinner().getName());
                 auctionList.set(0, dataMap);
                 config.set("Auctions", auctionList);
                 save();
             }
         }
     }
 
     public AuctionQueue(Salesmania plugin) {
         this.plugin = plugin;
         queueConfig = new QueueConfig(plugin);
         auctionSettings = plugin.getSettings().getAuctionSettings();
         queueConfig.loadQueue(this);
         start();
     }
 
     private Runnable timerRunnable = new Runnable() {
         @Override
         public void run() {
             if(currentAuction == null) {
                 if(size() == 0) stop();
             }
             else {
                 if(isCooldown) {
                     if(cooldownRemaining > 0) {
                         cooldownRemaining--;
                     } else {
                         isCooldown = false;
                     }
                 }
 
                 else if(currentAuction.isRunning()) {
                     currentAuction.timerTick();
                 } else {
                     nextAuction();
                 }
             }
         }
     };
 
     public int playerSize(Player player) {
         int count = 0;
         for(Auction auction : this) {
             if(auction.getOwner().equals(player)) count++;
         }
         return count;
     }
 
     public void update() {
         queueConfig.update();
     }
 
     public boolean nextAuction() {
         if(size() != 0) {
             currentAuction = peek();
             if(!isCooldown) currentAuction.start();
         } else {
             currentAuction = null;
             return false;
         }
         return true;
     }
 
     public void startCooldown() {
         cooldownRemaining = plugin.getSettings().getAuctionSettings().getCooldown();
         isCooldown = true;
     }
 
     public void start() {
         if(!isRunning) {
             if(size() != 0) {
                 isRunning = true;
                 plugin.getServer().getPluginManager().callEvent(new AuctionEvent(null, AuctionEvent.EventType.QUEUE_STARTED));
                 currentAuction = peek();
                 currentAuction.start();
                 timerID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, timerRunnable, TICKS_PER_SECOND, TICKS_PER_SECOND);
             }
         }
     }
 
     public void stop() {
         if(isRunning) {
             plugin.getServer().getScheduler().cancelTask(timerID);
             plugin.getServer().getPluginManager().callEvent(new AuctionEvent(null, AuctionEvent.EventType.QUEUE_STOPPED));
             isRunning = false;
         }
     }
 
     public Auction getCurrentAuction() {
         return currentAuction;
     }
 
     @Override
     public boolean add(Auction auction) {
         if(super.add(auction)) {
             queueConfig.saveAuction(auction);
             plugin.getServer().getPluginManager().callEvent(new AuctionEvent(auction, AuctionEvent.EventType.QUEUED));
             if(auctionSettings.getEnabled() && !isRunning) {
                 start();
             }
             return true;
         }
         return false;
     }
 
     @Override
     // TODO for now, it's only possible to remove the first auction in the queue
     public Auction remove() {
         Auction auction = super.remove();
         queueConfig.removeAuction(0);
         return auction;
     }
 }
