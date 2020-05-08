 package com.entrocorp.linearlogic.oneinthegun.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.entrocorp.linearlogic.oneinthegun.OITG;
 
 public class CommandJoin extends OITGArenaCommand {
 
     public CommandJoin(CommandSender sender, String[] args) {
         super(sender, args, 1, false, "join <arena>", "oneinthegun.arena.join", true);
     }
 
     public void run() {
         Player player = (Player) sender;
         if (arena.isIngame()) {
             player.sendMessage(OITG.prefix + ChatColor.RED + "A game is already in progress in that arena, choose another.");
             return;
         }
         if (arena.isClosed()) {
             player.sendMessage(OITG.prefix + ChatColor.RED + "That arena is closed, choose another.");
             return;
         }
         if (arena.getPlayerCount() >= arena.getPlayerLimit()) {
             player.sendMessage(OITG.prefix + ChatColor.RED + "That arena is full, choose another.");
             return;
         }
         if (arena.getLobby() == null) {
             player.sendMessage(OITG.prefix + ChatColor.RED + "That arena has not been set up: no lobby has been set.");
             return;
         }
         if (arena.getSpawns().length == 0) {
             player.sendMessage(OITG.prefix + ChatColor.RED + "That arena has not been set up: no spawn points have been set.");
             return;
         }
         if (OITG.instance.getArenaManager().getArena(player) != null) {
             player.sendMessage(OITG.prefix + ChatColor.RED + "You are already in an arena!");
             return;
         }
         arena.addPlayer((Player) sender);
        player.teleport(arena.getLobby());
     }
 
 }
