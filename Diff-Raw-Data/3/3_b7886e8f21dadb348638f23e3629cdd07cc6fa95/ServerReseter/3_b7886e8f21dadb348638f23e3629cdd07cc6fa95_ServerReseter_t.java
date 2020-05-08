 package com.barroncraft.sce;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import net.sacredlabyrinth.phaed.simpleclans.Clan;
 import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
 
 public class ServerReseter
 {
     private final String fileName = "reset-required";
     private final SimpleClansExtensions plugin;
     private final boolean useCommand;
     private long delay;
     private final String command;
     private boolean resetting;
     private boolean resetTeams;
 
     public ServerReseter(SimpleClansExtensions plugin)
     {
         this.plugin = plugin;
         this.command = plugin.getExtensionsConfig().getResetCommand();
         this.delay = plugin.getExtensionsConfig().getResetDelay();
         this.useCommand = command != null && !command.isEmpty();
         this.resetting = false;
         this.resetTeams = plugin.getExtensionsConfig().getResetTeams();
     }
 
     public boolean getResetFlag()
     {
         return resetting;
     }
 
     public long getDelay()
     {
         return delay;
     }
 
     public void reset()
     {
         resetting = true;
         if (delay > 0)
             Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() { runReset(); } }, delay);
         else
             runReset();
     }
 
     private void runReset()
     {
         if (resetTeams)
             resetTeams();
 
         boolean result = useCommand ? resetWithCommand() : resetWithFile();
         if (!result)
             plugin.getServer().broadcastMessage(ChatColor.YELLOW + "There was an issue resetting the map, please contact an admin.");
     }
 
     private void resetTeams()
     {
         Set<String> teams = plugin.getExtensionsConfig().getClanTeams().keySet();
         for (String team : teams)
         {
             Clan clan = plugin.getClanManager().getClan(team);
            if (clan != null)
                clan.disband();
         }
     }
 
     private boolean resetWithCommand()
     {
         Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
         return true;
     }
 
     private boolean resetWithFile()
     {
         File resetFile = new File(fileName);
         if (resetFile.exists())
             return true;
 
         try
         {
             return resetFile.createNewFile();
         } 
         catch(IOException e)
         {
             return false;
         }
     }
 }
