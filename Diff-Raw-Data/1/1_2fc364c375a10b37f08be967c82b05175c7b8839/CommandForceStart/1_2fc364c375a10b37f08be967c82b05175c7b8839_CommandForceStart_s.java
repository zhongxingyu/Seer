 package com.entrocorp.linearlogic.oneinthegun.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import com.entrocorp.linearlogic.oneinthegun.OITG;
 
 public class CommandForceStart extends OITGArenaCommand {
 
     public CommandForceStart(CommandSender sender, String[] args) {
         super(sender, args, 1, false, "fstart <arena> [delay]", "oneinthegun.arena.forcestart", false);
     }
 
     public void run() {
         if (arena.isClosed()) {
             sender.sendMessage(OITG.prefix + ChatColor.RED + "That arena is closed.");
             return;
         }
         if (arena.isIngame()) {
             sender.sendMessage(OITG.prefix + ChatColor.RED + "A round is already in progress in that arena.");
             return;
         }
         if (arena.getPlayerCount() < 2) {
             sender.sendMessage(OITG.prefix + ChatColor.RED + "There aren't enough players in that arena to start the round.");
             return;
         }
         if (args.length == 1) {
             arena.setTimer(0);
             sender.sendMessage(OITG.prefix + ChatColor.GREEN + "Forced the round in arena " + arena.toString() + " to start immediately.");
             arena.broadcast(ChatColor.GREEN + "The round was started by an administrator.");
             return;
         }
         int delay = -1;
         try {
             delay = Integer.parseInt(args[1]);
         } catch (NumberFormatException e) { }
         if (delay < 0) {
             sender.sendMessage(OITG.prefix + ChatColor.RED + "The delay must be a number no less than zero.");
             return;
         }
         arena.setTimer(delay);
         sender.sendMessage(OITG.prefix + ChatColor.GREEN + "Forced the round in arena " + arena.toString() +
                 " to start in " + delay + " seconds.");
     }
 }
