 import java.util.*;
 
 
 public class EventuallyPerfectFailureDetector implements IFailureDetector {
 
     private final Process process;
     private final SortedMap<Integer, ProcessTimeTrack> suspects = new TreeMap<Integer, ProcessTimeTrack>();
     private Timer timer;
     private int currentLeaderID;
 
     public EventuallyPerfectFailureDetector(Process process) {
         this.process = process;
         this.timer = new Timer();
         currentLeaderID = Utils.UNINITIALIZED_LEADER;
     }
 
 
     public void begin() {
         timer.schedule(new HeartbeatPeriodTask(), 0, Utils.HEARTBEAT_DELAY);
     }
 
     public synchronized void receive(Message message) {
         Utils.out(process.pid, message.toString());
 
         int sourcePid = message.getSource();
         long currentTime = System.currentTimeMillis();
 
         long delay = (currentTime - Long.parseLong(message.getPayload()));
 
         ProcessTimeTrack ptt = suspects.get(sourcePid);
 
         if (ptt == null) {
             suspects.put(sourcePid, new ProcessTimeTrack(currentTime, delay));
         } else {
             ptt.addTime(delay);
             ptt.setLastMessageReceivedTime(currentTime);
         }
 
     }
 
     public boolean isSuspect(Integer process) {
 
         if (!suspects.containsKey(process)) {
             /* Never receive any heartbeat from that process */
             return true;
         } else {
 
             long currentDate = System.currentTimeMillis();
             long processDate = suspects.get(process).getLastMessageReceivedTime();
 
             long heartbeatDelay = currentDate - processDate;
 
             long maxDelay = Utils.HEARTBEAT_DELAY + suspects.get(process).getAverageDelay();
 
             return heartbeatDelay > maxDelay;
         }
 
     }
 
     public int getLeader() {
         return currentLeaderID;
     }
 
     public void isSuspected(Integer process) {
 
     }
 
     private class ProcessTimeTrack {
         private long lastMessageReceivedTime;
         private long averageDelay;
         private long heartbeatMessageCounter;
 
         public ProcessTimeTrack(long lastMessageReceivedTime, long delay) {
             this.lastMessageReceivedTime = lastMessageReceivedTime;
             this.averageDelay = delay;
             this.heartbeatMessageCounter = 1;
         }
 
         public void addTime(long lastDelay) {
             heartbeatMessageCounter++;
             averageDelay = (averageDelay * (heartbeatMessageCounter - 1) + lastDelay) / (heartbeatMessageCounter);
         }
 
         public void setLastMessageReceivedTime(long lastMessageReceivedTime) {
             this.lastMessageReceivedTime = lastMessageReceivedTime;
         }
 
         public long getLastMessageReceivedTime() {
             return lastMessageReceivedTime;
         }
 
         public long getAverageDelay() {
             return averageDelay;
         }
 
     }
 
     private class HeartbeatPeriodTask extends TimerTask {
 
         @Override
         public void run() {
             String payload = String.format("%d", System.currentTimeMillis());
             process.broadcast(Utils.HEARTBEAT_MESSAGE, payload);
         }
     }
 }
