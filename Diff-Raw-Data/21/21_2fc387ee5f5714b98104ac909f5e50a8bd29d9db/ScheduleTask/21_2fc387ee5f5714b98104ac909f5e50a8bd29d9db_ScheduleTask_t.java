 
 package com.quartercode.quarterbukkit.api.scheduler;
 
 import java.util.Collection;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.Plugin;
 import com.quartercode.quarterbukkit.QuarterBukkit;
 import com.quartercode.quarterbukkit.api.thread.ThreadUtil;
 import com.quartercode.quarterbukkit.api.thread.WrongThreadAction;
 
 /**
  * This class is for implementing an easy to use schedule-task fir the Bukkit-Scheduler.
  */
 public abstract class ScheduleTask implements Runnable {
 
     private Plugin plugin;
     private int    id = -1;
 
     /**
      * Creates a schedule task with the QuarterBukkit-{@link Plugin}.
      */
     public ScheduleTask() {
 
         plugin = QuarterBukkit.getPlugin();
     }
 
     /**
      * Creates a schedule task with a custom {@link Plugin}. This is recommended!
      * 
      * @param plugin The custom plugin.
      */
     public ScheduleTask(Plugin plugin) {
 
         this.plugin = plugin;
     }
 
     /**
      * Adds the schedule task to a {@link Collection} (this may be a {@link ScheduleGroup}).
      * 
      * @param collection The {@link Collection} to add the task.
      * @return This schedule task.
      */
     public ScheduleTask add(final Collection<ScheduleTask> collection) {
 
         collection.add(this);
         return this;
     }
 
     private void checkId() {
 
         if (id >= 0) {
             throw new IllegalStateException("ScheduleTask already running (id " + id + ")");
         }
     }
 
     private long getTicks(long millis) {
 
         return millis / 1000 * 20;
     }
 
     /**
      * Runs the scheduler once with a delay. You have to cancel it after running.
      * 
     * @param wrongThreadAction What to do if the thread is not valid.
      * @param sync Should the scheduler runs synced with the Bukkit-Main-{@link Thread}.
      * @param delay The delay in ticks.
      * @return This schedule task.
      */
     public ScheduleTask run(WrongThreadAction wrongThreadAction, final boolean sync, final long delay) {
 
         ThreadUtil.check(wrongThreadAction, ThreadUtil.getMethod(getClass(), "run", WrongThreadAction.class, boolean.class, long.class), this, wrongThreadAction, sync, delay);
 
         checkId();
 
         if (sync) {
             id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, getTicks(delay));
         } else {
             id = Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, this, getTicks(delay));
         }
 
         return this;
     }
 
     /**
      * Runs the scheduler repeating with a delay until it's cancelled.
      * 
      * @param wrongThreadAction What to do if the thread is not valid.
      * @param sync Should the scheduler runs synced with the Bukkit-Main-{@link Thread}.
      * @param delay The delay in ticks.
      * @param period The delay between two repeatings in ticks.
      * @return This schedule task.
      */
     public ScheduleTask run(WrongThreadAction wrongThreadAction, final boolean sync, final long delay, final long period) {
 
         ThreadUtil.check(wrongThreadAction, ThreadUtil.getMethod(getClass(), "run", WrongThreadAction.class, boolean.class, long.class, long.class), this, wrongThreadAction, sync, delay, period);
 
         checkId();
 
         if (sync) {
             id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, getTicks(delay), getTicks(period));
         } else {
             id = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, this, getTicks(delay), getTicks(period));
         }
 
         return this;
     }
 
     /**
      * Cancels this schedule taks.
      * 
      * @return This schedule task.
      */
     public ScheduleTask cancel() {
 
         if (id < 0) {
             throw new IllegalStateException("ScheduleTask isn't running");
         }
 
         Bukkit.getScheduler().cancelTask(id);
         id = -1;
 
         return this;
     }
 
 }
