 package com.hawkfalcon.deathswap.Game;
 
 import com.hawkfalcon.deathswap.API.DeathSwapWinEvent;
 import com.hawkfalcon.deathswap.API.DeathSwapWinGameEvent;
 import com.hawkfalcon.deathswap.DeathSwap;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class WinGame {
 
     public DeathSwap plugin;
 
     public WinGame(DeathSwap ds) {
         this.plugin = ds;
     }
 
     public Player getWinner(final Player loser) {
         Player winner = null;
         if (plugin.match.containsKey(loser.getName())) {
             winner = plugin.getServer().getPlayerExact(plugin.match.get(loser.getName()));
         } else if (plugin.match.containsValue(loser.getName())) {
             winner = null;
             for (String key : plugin.match.keySet()) {
                 if (plugin.match.get(key).equals(loser.getName())) {
                     winner = plugin.getServer().getPlayerExact(key);
                 }
             }
         }
         return winner;
     }
 
     public void winGame(final Player loser, boolean died) {
         final Player winner = getWinner(loser);
         DeathSwapWinEvent dswe = new DeathSwapWinEvent(winner.getName(), loser.getName());
         Bukkit.getServer().getPluginManager().callEvent(dswe);
         DeathSwapWinGameEvent dswge = new DeathSwapWinGameEvent(winner, loser);
         Bukkit.getServer().getPluginManager().callEvent(dswge);
         //JOPHESTUS IS AWESOME
         plugin.utility.broadcast(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gameend").replace("%WINNER%", winner.getName()).replace("%LOSER%", loser.getName())));
         if (died) {
             plugin.utility.message(loser.getName() + " died, you win!", winner);
         } else {
             plugin.utility.message(loser.getName() + " has left the game, you win!", winner);
         }
         plugin.leave.leave(winner);
     }


 }
