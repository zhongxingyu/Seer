 package com.kierdavis.clearfx;
 
 import java.util.Iterator;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffect;
 
 public class ClearFXCommandExecutor implements CommandExecutor {
     private ClearFX plugin;
     
     public ClearFXCommandExecutor(ClearFX plugin_) {
         plugin = plugin_;
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (!sender.hasPermission("clearfx.clear")) {
             sender.sendMessage(ChatColor.YELLOW + "You don't have permission to use this command (" + ChatColor.RED + "clearfx.clear" + ChatColor.YELLOW + ")");
             return true;
         }
         
         Player target;
         
         if (args.length >= 1) {
             if (!sender.hasPermission("clearfx.clear.others")) {
                 sender.sendMessage(ChatColor.YELLOW + "You don't have permission to clear other player's effects (" + ChatColor.RED + "clearfx.clear.others" + ChatColor.YELLOW + ")");
                 return true;
             }
             
             String playerName = args[0];
             target = plugin.getServer().getPlayer(playerName);
             if (target == null) {
                 sender.sendMessage(ChatColor.YELLOW + "No player by that name online.");
                 return true;
             }
         }
         
         else {
             if (!sender.hasPermission("clearfx.clear.self")) {
                 sender.sendMessage(ChatColor.YELLOW + "You don't have permission to clear your own effects (" + ChatColor.RED + "clearfx.clear.self" + ChatColor.YELLOW + ")");
                 return true;
             }
             
            if (!sender instanceof Player) {
                 sender.sendMessage(ChatColor.YELLOW + "You must be a player to be able to use this command.");
                 return true;
             }
             
             target = (Player) sender;
         }
         
         Iterator<PotionEffect> it = target.getActivePotionEffects().iterator();
         
         while (it.hasNext()) {
             PotionEffect effect = (PotionEffect) it.next();
             target.removePotionEffect(effect.getType());
         }
         
         sender.sendMessage(ChatColor.YELLOW + "Potion effects cleared for " + ChatColor.GREEN + target.getName());
         if (sender != target) {
             target.sendMessage(ChatColor.YELLOW + "Potion effects cleared by " + ChatColor.GREEN + sender.getName());
         }
         
         return true;
     }
 }
