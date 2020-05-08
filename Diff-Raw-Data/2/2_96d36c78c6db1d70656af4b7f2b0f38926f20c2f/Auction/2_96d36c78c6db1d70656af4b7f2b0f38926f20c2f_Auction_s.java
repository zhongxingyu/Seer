 package net.invisioncraft.plugins.salesmania;
 
 import net.invisioncraft.plugins.salesmania.configuration.Settings;
 import org.bukkit.entity.Player;
 
 /**
  * Owner: Justin
  * Date: 5/17/12
  * Time: 3:59 PM
  */
 public class Auction {
     private Salesmania plugin;
     private Settings settings;
 
     private boolean isRunning = false;
     private boolean isCooldown = false;
 
     private Player currentWinner;
     private long currentBid;
 
     private Runnable cooldownRunnable = new Runnable() {
 
         @Override
         public void run() {
             isCooldown = false;
         }
     };
 
     public static enum AuctionStatus {
         OVER_MAX,
         UNDER_MIN,
         SUCCESS,
         FAILURE,
         RUNNING,
         COOLDOWN,
         WINNING,
         NOT_RUNNING,
 
     }
 
     public Auction(Salesmania plugin) {
         this.plugin = plugin;
         this.settings = plugin.getSettings();
     }
 
     public void reset() {
 
     }
 
     public boolean isRunning() {
         return isRunning;
     }
 
     public Player getWinner() {
         return currentWinner;
     }
 
     public AuctionStatus bid(Player player, long bid) {
         if(!isRunning) return AuctionStatus.NOT_RUNNING;
         if(currentBid + bid > bid + settings.getMaxIncrement())
             return AuctionStatus.OVER_MAX;
         if(currentBid + bid < bid + settings.getMinIncrement())
             return AuctionStatus.UNDER_MIN;
        if(currentWinner != null && currentWinner == player);
             return AuctionStatus.WINNING;
 
             else {
             currentWinner = player;
             currentBid = bid;
         }
         return AuctionStatus.SUCCESS;
     }
 
 }
