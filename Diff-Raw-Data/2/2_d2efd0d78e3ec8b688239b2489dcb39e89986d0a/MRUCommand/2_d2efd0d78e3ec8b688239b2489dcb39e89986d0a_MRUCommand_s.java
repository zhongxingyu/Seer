 package me.stutiguias.mcmmorankup.command;
 
 import java.util.HashMap;
 
 import me.stutiguias.mcmmorankup.Mcmmorankup;
 import me.stutiguias.mcmmorankup.Util;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class MRUCommand extends Util implements CommandExecutor {
     
     private final HashMap<String,CommandHandler> avaibleCommands;
     private final HashMap<String,CommandHandler> consoleCommands;
     
     public MRUCommand(Mcmmorankup plugin) {
         super(plugin);
         avaibleCommands = new HashMap<>();
         consoleCommands = new HashMap<>();
         
         Help help = new Help(plugin);
         Reload reload = new Reload(plugin);
         Report report = new Report(plugin);
         Set set = new Set(plugin);
         
         avaibleCommands.put("buy", new Buy(plugin));
         avaibleCommands.put("display", new Display(plugin));
         avaibleCommands.put("feeds", new Feeds(plugin));
         avaibleCommands.put("female", new Female(plugin));
         avaibleCommands.put("hab", new Hab(plugin));
         avaibleCommands.put("help", help);
         avaibleCommands.put("?", help);
         avaibleCommands.put("male", new Male(plugin));
         avaibleCommands.put("pinfo", new Pinfo(plugin));
        avaibleCommands.put("rankup", new RankUp(plugin));
         avaibleCommands.put("reload", reload);
         avaibleCommands.put("report", report);
         avaibleCommands.put("set", set);
         avaibleCommands.put("stats", new Stats(plugin));
         avaibleCommands.put("update", new Update(plugin));
         avaibleCommands.put("ver", new Ver(plugin));
         avaibleCommands.put("view", new View(plugin));
         
         consoleCommands.put("reload", reload);
         consoleCommands.put("set", set);
         consoleCommands.put("report", report);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] args) {
         this.sender = sender;
         
         if (sender.getName().equalsIgnoreCase("CONSOLE")) return isConsole(args);
 
         if (!(sender instanceof Player)) return false;
         if(args.length < 0 || args.length == 0) return CommandNotFound();
         
         String executedCommand = args[0].toLowerCase();
 
         if(avaibleCommands.containsKey(executedCommand))
             return avaibleCommands.get(executedCommand).OnCommand(sender,args);
         else
             return CommandNotFound();     
     }
     
     private boolean CommandNotFound() {
         SendMessage("&3&lThis command don't exists or you don't have permission");
         SendMessage("&3&lTry /mru ? or help");
         return true;
     }
     
     public boolean isConsole(String[] args) {
         if (args.length < 1) {
             return false;
         }
         String executedCommand = args[0].toLowerCase();
 
         if(consoleCommands.containsKey(executedCommand))
             return consoleCommands.get(executedCommand).OnCommand(sender,args);
         else
             return CommandNotFound();    
 
     }
 }
