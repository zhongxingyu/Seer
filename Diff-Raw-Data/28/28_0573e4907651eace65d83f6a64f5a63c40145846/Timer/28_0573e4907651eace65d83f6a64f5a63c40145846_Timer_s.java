 package fr.aumgn.diamondrush.util;
 
 import java.util.concurrent.TimeUnit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import fr.aumgn.diamondrush.DiamondRush;
 
 
 public abstract class Timer implements Runnable {
 
     private static final int TICKS_PER_SECONDS = 20;
 
     private final int majorDelay;
     private final int minorDelay;
 
     private Runnable runnable;
     private int seconds;
 
     private int currentDelay;
     private int taskId;
     private long taskStartTime;
     private int pauseDelay;
 
     public Timer(int seconds, Runnable runnable) {
         this.majorDelay = DiamondRush.getConfig().getMajorDelay();
         this.minorDelay = DiamondRush.getConfig().getMinorDelay();
 
         this.seconds = seconds;
         this.runnable = runnable;
 
         this.currentDelay = 0;
     }
 
     @Override
     public void run() {
         seconds -= currentDelay;
         if (seconds > majorDelay) {
             scheduleAndPrintTime(majorDelay);
         } else if (seconds > minorDelay) {
             scheduleAndPrintTime(minorDelay);
         } else if (seconds > 0) {
             scheduleAndPrintTime(1);
         } else {
             runnable.run();
         }
     }
 
     private void scheduleAndPrintTime(int delay) {
         long minutes = TimeUnit.SECONDS.toMinutes(seconds);
         String msg = String.format("%02d:%02d", minutes, seconds % 60);
        sendTimeMessage(getColorFor(delay) + msg);
         currentDelay = delay;
         taskStartTime = System.currentTimeMillis();
         taskId = Util.scheduleDelayed(delay * TICKS_PER_SECONDS, this);
     }
 
    private ChatColor getColorFor(int delay) {
        if (delay > majorDelay) {
             return ChatColor.YELLOW;
        } else if (delay > minorDelay){
             return ChatColor.GOLD;
         } else {
             return ChatColor.RED;
         }
     }
 
     public void pause() {
         Bukkit.getScheduler().cancelTask(taskId);
         pauseDelay = (int) ((System.currentTimeMillis() - taskStartTime ) / 1000);
         seconds -= pauseDelay;
     }
 
     public void resume() {
         scheduleAndPrintTime(currentDelay - pauseDelay); 
     }
 
     public abstract void sendTimeMessage(String string);
 }
