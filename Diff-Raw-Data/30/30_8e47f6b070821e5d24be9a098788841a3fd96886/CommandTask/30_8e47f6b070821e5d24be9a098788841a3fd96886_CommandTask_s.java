 package net.daboross.bukkitdev.commandtimer;
 
import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author daboross
  */
 public class CommandTask implements Runnable {
 
    private final Logger mainl;
     private JavaPlugin instance;
 
    public CommandTask(Logger lSet, JavaPlugin instance) {
        mainl = lSet;
         this.instance = instance;
     }
 
     @Override
     public void run() {
         if (Bukkit.getServer().getOnlinePlayers().length > 0) {
            Controller.runRemoveEntities(mainl, false);
            OtherCommands.runOtherCommands(mainl);
             if (instance.isEnabled()) {
                Bukkit.getScheduler().runTaskLater(instance, new CommandTask(mainl, instance), 6000);
             }
         } else {
             if (instance.isEnabled()) {
                Bukkit.getScheduler().runTaskLater(instance, new CommandTask(mainl, instance), 12000);
             }
         }
     }
 }
