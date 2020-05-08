 package me.mentioum.plugins.lpmcnavigator;
 
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 
 public class Messenger {
     
     private static Plugin plugin;
     private static Logger logger;
     private static String prefix;
     
         public static void Startup(Plugin p)
         {
             logger = Logger.getLogger("minecraft");
             plugin = p;
             prefix = "[LPMCNavigator] ";
             LogInfo(prefix + plugin.getDescription().getName() + " V." + plugin.getDescription().getVersion() + " by " + plugin.getDescription().getAuthors() + " is enabled");
         }
         public static void StartupDisabled(Plugin p)
         {
             logger = Logger.getLogger("minecraft");
             plugin = p;
             prefix = "[LPMCNavigator] ";
            LogInfo(prefix + plugin.getDescription().getName() + " is disabled in config.yml Plugin will now disable itself!");
         }        
                 public static void Shutdown(Plugin p)
         {
             logger = Logger.getLogger("minecraft");
             plugin = p;
             prefix = "[LPMCNavigator] ";
            LogInfo(prefix + plugin.getDescription().getName() + " V." + plugin.getDescription().getVersion() + " by " + plugin.getDescription().getAuthors() + " is disabled.");
         }
         
         public static void LogInfo(String message)
 	{
 		logger.info(message);
 	}
     
         public static void Reloaded(CommandSender cs){
             cs.sendMessage(ChatColor.BLUE + "LPMCNavigator configuration file" + ChatColor.GREEN + " reloaded" + ChatColor.BLUE +".");
             logger.info(prefix+ " " + cs + "has reloaded the configuration file");   
         }
     
         public static void Hidden(CommandSender cs){
             cs.sendMessage(ChatColor.BLUE + "You are "+ ChatColor.RED + "already hidden!");
         }
         
         public static void NowHidden(CommandSender cs){
         cs.sendMessage(ChatColor.BLUE + "You are" + ChatColor.GREEN + " now hidden!"); 
         }
         
         public static void IsDisabled(CommandSender cs){
             cs.sendMessage(ChatColor.BLUE + "This feature is" + ChatColor.RED + " disabled" + ChatColor.BLUE + " on this server.");
         }
         
         public static void Visible(CommandSender cs){
             cs.sendMessage(ChatColor.BLUE + "You are "+ ChatColor.RED + "already visible!");
         }
         
         public static void IsVisible(CommandSender cs){
             cs.sendMessage(ChatColor.BLUE + "You are" + ChatColor.GREEN + " now visible!");
         }
         
        
         
 }
 
