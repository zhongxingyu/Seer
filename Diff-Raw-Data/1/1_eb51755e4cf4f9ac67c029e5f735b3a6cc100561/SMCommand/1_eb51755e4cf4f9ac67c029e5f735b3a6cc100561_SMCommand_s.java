 package com.evosysdev.bukkit.taylorjb.simplemod.commands;
 
 import java.util.Arrays;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import com.evosysdev.bukkit.taylorjb.simplemod.SimpleMod;
 import com.evosysdev.bukkit.taylorjb.simplemod.SimpleModHandler;
 
 /**
  * Abstract class for basic command requirements sans execution definition
  * 
  * @author taylorjb
  * 
  */
 public abstract class SMCommand implements CommandExecutor
 {
     protected SimpleModHandler handler; // plugin instance
     protected SimpleMod plugin; // handler for moderator actions
     
     protected int reqArgs; // number of required args we have
     protected String[] permissions; // permissions required for command
     
     /**
      * Initialize command
      * 
      * @param plugin
      *            plugin instance
      * @param handler
      *            handler for moderator actions
      * @param reqArgs
      *            number of required args we have
      * @param permissions
      *            permissions required for command
      */
     public SMCommand(SimpleMod plugin, SimpleModHandler handler, int reqArgs, String... permissions)
     {
         this.plugin = plugin;
         this.handler = handler;
         this.reqArgs = reqArgs;
         this.permissions = permissions;
     }
     
     /**
      * Check args and permissions and execute command
      */
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
     {
         if (args.length < reqArgs)
         {
             sender.sendMessage(ChatColor.RED + "Command missing arguments! Usage:");
            sender.sendMessage(ChatColor.GRAY + command.getUsage());
             return false;
         }
         
         /**
          * ensure player has all required permissions
          */
         for (String perm : permissions)
         {
             if (!sender.hasPermission(perm))
             {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                 return false;
             }
         }
         
         return execute(sender, command, label, args);
     }
     
     /**
      * @return String contains all strings on an array after certain index
      */
     protected String stringArrayToString(String[] array, int startIndex)
     {
         String rest = "";
         
         if (array.length > startIndex)
             rest = Arrays.toString(Arrays.copyOfRange(array, startIndex, array.length)).replaceAll("[,\\[\\]]", "");
         
         return rest;
     }
     
     /**
      * Execute command action
      */
     protected abstract boolean execute(CommandSender sender, Command command, String label, String[] args);
 }
