 package de.minestar.moneypit.listener;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 import de.minestar.moneypit.Core;
 import de.minestar.moneypit.data.BlockVector;
 import de.minestar.moneypit.manager.QueueManager;
 import de.minestar.moneypit.queues.Queue;
 
 public class MonitorListener implements Listener {
 
     private QueueManager queueManager;
     private BlockVector vector;
     private Queue addQueue, removeQueue, interactQueue;
 
     public MonitorListener() {
         this.queueManager = Core.queueManager;
         this.vector = new BlockVector("", 0, 0, 0);
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onBlockPlace(BlockPlaceEvent event) {
         // update the BlockVector
         this.vector.update(event.getBlock().getLocation());
 
         // get the AddQueue
         this.addQueue = this.queueManager.getAndRemoveQueue(this.vector);
         if (this.addQueue != null) {
             // execute the queue, if the event was not cancelled
             if (!event.isCancelled()) {
                 if (!this.addQueue.execute()) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onBlockBreak(BlockBreakEvent event) {
         // update the BlockVector
         this.vector.update(event.getBlock().getLocation());
 
         // get the AddQueue
         this.removeQueue = this.queueManager.getAndRemoveQueue(this.vector);
         if (this.removeQueue != null) {
             // execute the queue, if the event was not cancelled
             if (!event.isCancelled()) {
                 if (!this.removeQueue.execute()) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerInteract(PlayerInteractEvent event) {
 
         // Only handle Left- & Right-Click on a block
         Action action = event.getAction();
         if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
             return;
         }
 
         // update the BlockVector
         this.vector.update(event.getClickedBlock().getLocation());
 
         // get the AddQueue
         this.interactQueue = this.queueManager.getAndRemoveQueue(this.vector);
         if (this.interactQueue != null) {
             // execute the queue, if the event was not cancelled
             if (!event.isCancelled()) {
                 // execute the event
                this.interactQueue.execute();
 
                 // cancel the event
                 event.setCancelled(true);
             } else {
                 PlayerUtils.sendError(event.getPlayer(), Core.NAME, "Could not complete your interact request!");
                 PlayerUtils.sendInfo(event.getPlayer(), "The event was cancelled by another plugin.");
             }
         }
     }
 }
