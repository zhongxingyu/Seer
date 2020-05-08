 package com.minecarts.bouncer.command;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 import com.minecarts.bouncer.*;
 
 public class BouncerCommand extends CommandHandler{
     
     public BouncerCommand(Bouncer plugin){
         super(plugin);
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if(!sender.hasPermission("bouncer.admin")) return true;
 
         if(args[0].equalsIgnoreCase("reload")){
             plugin.reloadConfig();
             return true;
         }
 
         if(args[0].equalsIgnoreCase("lock")){
             //Admin lock ON!
             if(plugin.getConfig().getBoolean("locked")){
                 sender.sendMessage("Server is now unlocked.");
                 plugin.getConfig().set("locked",false);
             } else {
                 sender.sendMessage("Server is now locked.");
                 plugin.getConfig().set("locked",true);
             }
             return true;
         }
         return false;
     }
 }
