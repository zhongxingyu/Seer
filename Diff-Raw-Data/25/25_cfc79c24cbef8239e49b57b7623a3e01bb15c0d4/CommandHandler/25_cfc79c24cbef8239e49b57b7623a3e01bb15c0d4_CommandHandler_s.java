 package me.damo1995.AnimalProtect;
 
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class CommandHandler implements CommandExecutor
 {
    private AnimalProtect plugin;
 
    public CommandHandler(AnimalProtect plugin)
    {
       this.plugin = plugin;
    }
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
       if (cmd.getName().equalsIgnoreCase("animalprotect"))
       {
          if (args.length == 1)
          {
             if (args[0].equalsIgnoreCase("version"))
             {
                sender.sendMessage(ChatColor.GREEN + "Auf diesem Server laeuft " + plugin.getDescription().getName() + " Version " + plugin.getDescription().getVersion());
 
                return true;
             }
 
            if (((args[0].equalsIgnoreCase("reload")) && (sender.isOp())) || (sender.hasPermission("animalprotect.admin")))
             {
                this.plugin.reloadConfig();
 
                this.plugin.readConfigValues();
                sender.sendMessage(this.plugin.success + "Configuration Reloaded!");
                return true;
             }

            if (((args[0].equalsIgnoreCase("list")) && (args[1].equalsIgnoreCase("player")) && (sender.isOp())) || (sender.hasPermission("animalprotect.list"))) {
                List<String> pfp = this.plugin.getConfig().getStringList("protect-from-player");
                sender.sendMessage(this.plugin.success + "The following are protected from players");
               for (String i : pfp) {
                   sender.sendMessage(i);
                }
             }
 
            if (((args[0].equalsIgnoreCase("list")) && (args[1].equalsIgnoreCase("mobs")) && (sender.isOp())) || (sender.hasPermission("animalprotect.list"))) {
                List<String> pfp = this.plugin.getConfig().getStringList("protect-from-monsters");
                sender.sendMessage(this.plugin.success + "The following are protected from mobs");
                for (String i : pfp)
                   sender.sendMessage(i);
             }
          }
          else
          {
             sender.sendMessage("Falsche Anzahl Parameter!");
          }
       }
       return false;
    }
 }
