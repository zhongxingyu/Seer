 package com.entrocorp.linearlogic.oneinthegun.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import com.entrocorp.linearlogic.oneinthegun.OITG;
 import com.entrocorp.linearlogic.oneinthegun.game.Arena;
 
 public class CommandCreate extends OITGCommand {
 
     public CommandCreate(CommandSender sender, String[] args) {
         super(sender, args, 1, "create <name>", "oneinthegun.arena.create", false);
     }
 
     public void run() {
         if (args[0].length() > 15) {
             sender.sendMessage(OITG.prefix + ChatColor.RED + "Arena names must be 15 characters or less.");
             return;
         }
         if (!OITG.instance.getArenaManager().addArena(new Arena(args[0])))
             sender.sendMessage(OITG.prefix + ChatColor.RED + "An arena with that name already exists.");
         else
            sender.sendMessage(OITG.prefix + ChatColor.GREEN + "Created arena " + args[1] + ".");
     }
 
 }
