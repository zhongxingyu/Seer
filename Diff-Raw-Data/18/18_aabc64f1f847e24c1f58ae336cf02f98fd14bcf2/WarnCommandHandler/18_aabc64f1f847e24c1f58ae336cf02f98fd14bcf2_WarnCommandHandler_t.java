 package com.minecraftserver.warn.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.minecraftserver.warn.commands.*;
 import com.minecraftserver.warn.Warner;
 
 public class WarnCommandHandler {
 
     private CommandSender getSender(CommandSender cmdsender) {
         CommandSender sender;
         if (!cmdsender.equals(Bukkit.getConsoleSender())) {
             sender = (Player) cmdsender;
         } else {
             sender = cmdsender;
         }
         return sender;
     }
 
     public void executeWarnCommand(CommandSender cmdsender, Command cmd, String label,
             String[] args, Warner warner) {
 
         CommandSender sender;
         sender = getSender(cmdsender);
 
         if (!cmd.getName().equalsIgnoreCase("warn")) {
             return;
         }
         if (args.length == 0) {
             Warnhelpcmd.run(sender);
         } else if (Bukkit.getServer().getPlayer(args[0]) != null) {
            Warncmd.run(sender, args, warner);
         } else if (args[0].equalsIgnoreCase("take")) {
             Warntakecmd.run(sender, args, warner);
         } else if (args[0].equalsIgnoreCase("set")) {
             Warnsetcmd.run(sender, args, warner);
         } else if (args[0].equalsIgnoreCase("lookup")) {
             Warnlookupcmd.run(sender, args, warner);
         } else if (args[0].equalsIgnoreCase("reset")) {
             Warnresetcmd.run(sender, args, warner);
         } else if (args[0].equalsIgnoreCase("version")) {
             Warnversioncmd.run(sender);
         } else if (args[0].equalsIgnoreCase("reload")) {
             Warnreloadcmd.run(sender, warner);
         } else {
             Warnhelpcmd.run(sender);
         }
     }
 
 }
