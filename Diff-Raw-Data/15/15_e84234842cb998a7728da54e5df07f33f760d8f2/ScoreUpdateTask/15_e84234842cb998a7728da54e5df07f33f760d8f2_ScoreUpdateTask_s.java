 package me.DDoS.Quarantine.leaderboard.task;
 
 import java.util.Queue;
 import java.util.TimerTask;
 import me.DDoS.Quarantine.Quarantine;
 import me.DDoS.Quarantine.leaderboard.Leaderboard;
 import me.DDoS.Quarantine.leaderboard.ScoreUpdate;
 import redis.clients.jedis.exceptions.JedisConnectionException;
 
 /**
  *
  * @author DDoS
  */
 public class ScoreUpdateTask extends TimerTask {
 
     private final Leaderboard leaderboard;
 
     public ScoreUpdateTask(Leaderboard leaderboard) {
 
         this.leaderboard = leaderboard;
 
     }
 
     @Override
     public void run() {
     
         final Queue<ScoreUpdate> queue = leaderboard.getUpdates();

         try {
 
             while (!queue.isEmpty()) {
 
                 ScoreUpdate update = queue.poll();
                leaderboard.getLeaderBoard().rank(update.getPlayer(), update.getScore());
 
             }
 
         } catch (JedisConnectionException e) {
 
             Quarantine.log.info("[Quarantine] Couldn't connect to Redis server. Is it on?");
             Quarantine.log.info("[Quarantine] Error message: " + e.getMessage());
 
         }
     }
 }
