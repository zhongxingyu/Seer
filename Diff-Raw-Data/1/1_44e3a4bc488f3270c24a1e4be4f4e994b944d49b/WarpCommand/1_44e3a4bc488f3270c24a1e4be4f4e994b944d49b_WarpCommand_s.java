 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.rymate.SimpleWarp;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import com.iConomy.*;
 import com.iConomy.system.Account;
 import com.iConomy.system.Holdings;
 import org.bukkit.block.Block;
 
 /**
  *
  * @author Ryan
  */
 class WarpCommand implements CommandExecutor {
 
     private final SimpleWarpPlugin plugin;
     private iConomy iConomy;
 
 
     public WarpCommand(SimpleWarpPlugin plugin) {
         this.plugin = plugin;
         plugin.iConomy = iConomy;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         Player player = (Player) sender;
 
         if ((plugin).permissionHandler.has(player, "warp.go")) {
             if (args.length < 1) {
                 return false;
             } else if (!(sender instanceof Player)) {
                 sender.sendMessage(ChatColor.RED + "You are not an in-game player!");
                 return true;
             }
 
             Location loc = (Location) plugin.m_warps.get(args[0]);
             if ((loc != null) && (plugin.useEconomy = true) && (iConomy != null)) {
                 Account account = iConomy.getAccount(player.getName());
                 Holdings balance = iConomy.getAccount(player.getName()).getHoldings();
                 if ((account != null) && balance.hasEnough(plugin.warpPrice)) {
                     balance.subtract(plugin.warpPrice);
                    //player.teleportTo(loc);
                     warpPlayer(player, loc);
                     player.sendMessage(ChatColor.GREEN + "You have arrived at your destination! " + plugin.warpPrice + "was deducted from your money.");
                 } else {
                     player.sendMessage(ChatColor.RED + "You do not have enough money :(");
                 }
             } else if (loc != null) {
                 //player.teleportTo(loc);
                 warpPlayer(player, loc);
                 player.sendMessage(ChatColor.GREEN + "You have arrived at your destination!");
             } else {
                 player.sendMessage(ChatColor.RED + "There is no warp with that name!");
             }
         } else {
             player.sendMessage(ChatColor.RED + "You do not have the permissions to use this command.");
         }
         return true;
     }
 
     private void warpPlayer(Player player, Location loc){
         Block block = loc.getBlock();
         while (block.getRelative(0, 1, 0).getTypeId() != 0 && block.getY() < 126) {
             if (block.getRelative(0, 2, 0).getTypeId() != 0) {
                 block = block.getRelative(0, 3, 0);
             } else {
                 block = block.getRelative(0, 2, 0);
             }
         }
         player.teleport(new Location(block.getWorld(), block.getX(), block.getY(), block.getZ(), loc.getYaw(), loc.getPitch()));
     }
 }
