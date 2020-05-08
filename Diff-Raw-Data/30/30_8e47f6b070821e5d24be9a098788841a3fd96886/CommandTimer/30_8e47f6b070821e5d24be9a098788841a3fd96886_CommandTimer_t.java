 package net.daboross.bukkitdev.commandtimer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author daboross
  */
 public class CommandTimer extends JavaPlugin {
 
     @Override
     public void onEnable() {
         PlayerListener pl = new PlayerListener(this);
         Bukkit.getServer().getPluginManager().registerEvents(pl, this);
        Runnable task = new CommandTask(this);
         Bukkit.getScheduler().runTask(this, task);
     }
 
     @Override
     public void onDisable() {
         Bukkit.getScheduler().cancelTasks(this);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("refreshtimer")) {
             if (args.length > 0) {
                 sender.sendMessage(ChatColor.DARK_RED + "There Are Too Many Words After " + ChatColor.RED + "/" + label);
                 return true;
             }
             Controller.runRemoveEntities(getLogger(), false);
             OtherCommands.runOtherCommands(getLogger());
 
             return true;
         }
         return false;
     }
 }
