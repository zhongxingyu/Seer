 package com.division.battlegrounds.mech;
 
 import com.division.battlegrounds.core.Battleground;
 import com.division.battlegrounds.core.BattlegroundCore;
 import com.division.battlegrounds.event.EnteredQueueEvent;
 import com.division.battlegrounds.event.LeftQueueEvent;
 import com.division.battlegrounds.event.RoundStartEvent;
 import java.util.Collection;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 /**
  *
  * @author Evan
  */
 public class BattlegroundQueueManager implements Listener {
 
     private final BattlegroundRegistrar bgr;
     private final String bgaFormat = ChatColor.RED + "[" + ChatColor.YELLOW + "BGAnnouncer" + ChatColor.RED + "]" + ChatColor.YELLOW + " %s of %s players in queue for %s";
 
     public BattlegroundQueueManager(BattlegroundRegistrar bgr) {
         this.bgr = bgr;
         Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(BattlegroundCore.getInstance(), new queueRunner(), 100L, 100L);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onEnteredQueue(EnteredQueueEvent evt) {
         int minPlayers = evt.getBattleground().getMinPlayers();
         int curPlayers = evt.getBattleground().getQueueSize();
         if (evt.getBattleground().isPlayerInBattleground(evt.getPlayer())) {
             return;
         }
         if (evt.getBattleground().isActive()) {
             if (evt.getBattleground().isDynamic() && !evt.getBattleground().isFull()) {
                 evt.getBattleground().addPlayerToBattleground(evt.getPlayer());
                 return;
             }
         }
         if (evt.getBattleground().isBroadcasting()) {
             broadcastQueueStatus(evt.getBattleground().getName(), curPlayers, minPlayers);
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onLeftQueue(LeftQueueEvent evt) {
         int minPlayers = evt.getBattleground().getMinPlayers();
         int curPlayers = evt.getBattleground().getQueueSize();
         broadcastQueueStatus(evt.getBattleground().getName(), curPlayers, minPlayers);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onRoundStart(RoundStartEvent evt) {
         Bukkit.getServer().broadcastMessage(String.format(BattlegroundCore.logFormat, "Battleground: " + evt.getBattleground().getName() + " has started."));
     }
 
     private void broadcastQueueStatus(String name, int curPlayers, int minPlayers) {
         Bukkit.getServer().broadcastMessage(String.format(bgaFormat, curPlayers, minPlayers, name));
     }
 
     private class queueRunner implements Runnable {
 
         @Override
         public void run() {
             Collection<Battleground> bgList = bgr.getRegistrar().values();
             for (Battleground bg : bgList) {
                if(bg.getQueue().isEmpty()){
                    continue;
                }
                 if (bg.isActive()) {
                     if (bg.isDynamic() && !bg.isFull()) {
                         bg.addPlayerToBattleground(bg.getQueue().get(0));
                         bg.getQueue().remove(0);
                     }
                     return;
                 }
                 if (bg.getQueueSize() >= bg.getMinPlayers()) {
                     if (bg.getQueueSize() >= bg.getMaxPlayers()) {
                         Bukkit.getServer().getPluginManager().callEvent(new RoundStartEvent(bg, bg.getQueue().subList(0, bg.getMaxPlayers())));
                     } else {
                         Bukkit.getServer().getPluginManager().callEvent(new RoundStartEvent(bg, bg.getQueue()));
                     }
                 }
             }
         }
     }
 
     public boolean isInQueue(Player player) {
         Collection<Battleground> bgList = bgr.getRegistrar().values();
         for (Battleground bg : bgList) {
             if (bg.getQueue().contains(player)) {
                 return true;
             }
         }
         return false;
     }
 
     public Battleground getQueuedBG(Player player) {
         Collection<Battleground> bgList = bgr.getRegistrar().values();
         for (Battleground bg : bgList) {
             if (bg.getQueue().contains(player)) {
                 return bg;
             }
         }
         return null;
     }
 }
