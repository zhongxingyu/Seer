 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct;
 
 import java.util.HashMap;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 
 /**
  * 遅延つきテレポート実行タスク
  * @author ucchy
  */
 public class DelayedTeleportTask extends BukkitRunnable {
 
     private HashMap<Player, Location> locationMap;
     private ArrayBlockingQueue<Player> players;
     private int delay;
     private BukkitTask task;
 
     /**
      * コンストラクタ
      * @param locationMap
      * @param delay
      */
     public DelayedTeleportTask(HashMap<Player, Location> locationMap, int delay) {
         this.locationMap = locationMap;
         this.delay = delay;
 
         players = new ArrayBlockingQueue<Player>(locationMap.size());
         for ( Player p : locationMap.keySet() ) {
             players.add(p);
         }
     }
 
     /**
      * タスクを開始する
      */
     public void startTask() {
         task = Bukkit.getScheduler().runTaskTimer(ColorTeaming.instance, this, delay, delay);
     }
 
     /**
      * @see java.lang.Runnable#run()
      */
     @Override
     public void run() {
 
         if ( players.isEmpty() ) {
             
             // プレイヤー表示パケットを送信する (see issue #78)
             int packetDelay = 
                     ColorTeaming.instance.getCTConfig().getTeleportVisiblePacketSendDelay();
             if ( packetDelay > 0 ) {
                 Bukkit.getScheduler().runTaskLater(ColorTeaming.instance, new BukkitRunnable() {
                     public void run() {
                         for ( Player playerA : locationMap.keySet() ) {
                             for ( Player playerB : locationMap.keySet() ) {
                                 playerA.showPlayer(playerB);
                             }
                         }
                     }
                 }, packetDelay);
             }
             
             // 自己キャンセル
             if ( task != null ) {
                 Bukkit.getScheduler().cancelTask(task.getTaskId());
             }
             return;
         }
 
         Player player = players.poll();
         Location location = locationMap.get(player);
         if ( player != null && location != null ) {
             player.teleport(location, TeleportCause.PLUGIN);
         }
     }
 
 }
