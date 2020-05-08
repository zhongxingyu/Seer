 package de.minestar.moneypit.autoclose;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.TimerTask;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 
 import de.minestar.moneypit.Core;
 import de.minestar.moneypit.data.BlockVector;
 import de.minestar.moneypit.utils.DoorHelper;
 
 public class AutoCloseBackgroundTask extends TimerTask {
 
     private HashMap<BlockVector, QueuedAutoClose> queuedTasks = new HashMap<BlockVector, QueuedAutoClose>();
 
     public void queue(Block block) {
         BlockVector vector = new BlockVector(block.getLocation());
         if (!this.queuedTasks.containsKey(vector)) {
             this.queuedTasks.put(vector, new QueuedAutoClose(vector));
         }
     }
 
     @Override
     public void run() {
         if (queuedTasks.isEmpty())
             return;
 
         ArrayList<QueuedAutoClose> collection = new ArrayList<QueuedAutoClose>(this.queuedTasks.values());
         long timestamp = System.currentTimeMillis();
         for (int i = collection.size() - 1; i >= 0; i--) {
             if (collection.get(i).hasEnded(timestamp)) {
                 if (!DoorHelper.isDoorClosed(collection.get(i).getBlock())) {
                     AutoCloseTask task = new AutoCloseTask(collection.get(i).getBlock());
                     Bukkit.getScheduler().scheduleSyncDelayedTask(Core.INSTANCE, task, 1);
                 }
                 this.queuedTasks.remove(collection.get(i).getVector());
             }
         }
     }
 }
