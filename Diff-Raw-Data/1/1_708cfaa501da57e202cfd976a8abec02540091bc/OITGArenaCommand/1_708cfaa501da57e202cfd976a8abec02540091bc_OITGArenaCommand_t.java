 package com.entrocorp.linearlogic.oneinthegun.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import com.entrocorp.linearlogic.oneinthegun.OITG;
 import com.entrocorp.linearlogic.oneinthegun.game.Arena;
 
 public abstract class OITGArenaCommand extends OITGCommand {
 
     protected Arena arena;
     private boolean mustBeEmpty;
 
     public OITGArenaCommand(CommandSender sender, String[] args, int minimumArgs, boolean mustBeEmpty, String usage,
             String permission, boolean mustBePlayer) {
         super(sender, args, minimumArgs == 0 ? 1 : minimumArgs, usage, permission, mustBePlayer);
        this.mustBeEmpty = mustBeEmpty;
     }
 
     public boolean validateArena() {
         arena = OITG.instance.getArenaManager().getArena(args[0]);
         if (arena == null) {
             sender.sendMessage(OITG.prefix + ChatColor.RED + "There is no arena by that name.");
             return false;
         }
         if (mustBeEmpty && arena.getPlayerCount() > 0) {
             sender.sendMessage(OITG.prefix + ChatColor.RED + "The arena must be empty.");
             return false;
         }
         return true;
     }
 }
