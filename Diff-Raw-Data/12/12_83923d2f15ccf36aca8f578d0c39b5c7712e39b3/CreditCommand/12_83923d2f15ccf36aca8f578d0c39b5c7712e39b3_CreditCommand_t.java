 package io.github.psgs.serverutils.credits;
 
 import io.github.psgs.serverutils.ServerUtils;
 import org.bukkit.*;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.FireworkMeta;
 
 import net.milkbowl.vault.economy.Economy;
 
 public class CreditCommand implements CommandExecutor {
 
     ServerUtils plugin;
 
     public CreditCommand(ServerUtils plugin) {
         this.plugin = plugin;
 
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 
         if (cmd.getName().equalsIgnoreCase("credits")) {
             if (sender.isOp() || sender.hasPermission("utils.credits")) {
                if (args[0] == null) {
                    //Return balance
                    final String player = sender.getName();
                    sender.sendMessage(ChatColor.GOLD + "You have " + ChatColor.AQUA + plugin.econ.getBalance(player) + ChatColor.GOLD + " credits!");
                } else {
                    //Return balance
                    final String player = args[0];
                    sender.sendMessage(ChatColor.GOLD + player + " has " + ChatColor.AQUA + plugin.econ.getBalance(player) + ChatColor.GOLD + " credits!");
                }
             }
         }
        return false;
     }
 }
