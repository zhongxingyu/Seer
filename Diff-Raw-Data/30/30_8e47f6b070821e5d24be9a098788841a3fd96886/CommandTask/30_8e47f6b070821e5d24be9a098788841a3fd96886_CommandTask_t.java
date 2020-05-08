 package net.daboross.bukkitdev.commandtimer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author daboross
  */
 public class CommandTask implements Runnable {
 
     private JavaPlugin instance;
 
    public CommandTask(JavaPlugin instance) {
         this.instance = instance;
     }
 
     @Override
     public void run() {
         if (Bukkit.getServer().getOnlinePlayers().length > 0) {
            Controller.runRemoveEntities(instance.getLogger(), false);
            OtherCommands.runOtherCommands(instance.getLogger());
             if (instance.isEnabled()) {
                Bukkit.getScheduler().runTaskLater(instance, this, 6000);
             }
         } else {
             if (instance.isEnabled()) {
                Bukkit.getScheduler().runTaskLater(instance, this, 12000);
             }
         }
     }
 }
