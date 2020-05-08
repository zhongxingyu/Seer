 package de.cubenation.plugins.utils.commandapi;
 
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CommandTask {
     private Thread bukkitQueueThread;
     private JavaPlugin plugin;
     private Lock lock = new ReentrantLock();
     private Condition waiter = lock.newCondition();
 
     public CommandTask(JavaPlugin plugin) {
         this.plugin = plugin;
     }
 
     public void syncronTask() {
        if (bukkitQueueThread != null || plugin == null) {
            return;
        }

         lock.lock();
         final Lock blockerInner = new ReentrantLock();
         final Condition waiterInner = blockerInner.newCondition();
         bukkitQueueThread = new Thread("BukkitThreadQueueBlocker") {
             @Override
             public void run() {
                 blockerInner.lock();
                 waiterInner.signal();
                 blockerInner.unlock();
 
                 lock.lock();
                 try {
                     waiter.await();
                 } catch (InterruptedException e) {
                 } finally {
                    bukkitQueueThread = null;
                     lock.unlock();
                 }
             }
         };
 
         Bukkit.getScheduler().runTask(plugin, bukkitQueueThread);
         blockerInner.lock();
         try {
             waiterInner.await();
         } catch (InterruptedException e) {
         } finally {
             blockerInner.unlock();
         }
         lock.unlock();
     }
 
     public void asyncronTask() {
        if (bukkitQueueThread == null || plugin == null) {
            return;
        }

         lock.lock();
         waiter.signalAll();
         lock.unlock();
     }
 }
