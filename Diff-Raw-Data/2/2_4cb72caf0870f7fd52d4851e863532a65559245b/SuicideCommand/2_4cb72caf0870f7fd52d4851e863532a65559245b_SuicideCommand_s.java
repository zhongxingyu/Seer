 package com.dumptruckman.gmtools.commands;
 
 import com.dumptruckman.gmtools.locale.Font;
 import com.dumptruckman.gmtools.permissions.Perms;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.List;
 
 /**
  * @author dumptruckman
  */
 public class SuicideCommand implements CommandExecutor {
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (sender instanceof Player)
             playerSuicide((Player)sender);
         return true;
     }
 
     public static void playerSuicide(Player player) {
         Location location = player.getLocation();
         Inventory playerInventory = player.getInventory();
         boolean emptyInv = true;
         ItemStack[] invContents = playerInventory.getContents();
         if (invContents != null) {
             for (ItemStack item : invContents) {
                 if (item != null && item.getTypeId() > 0) {
                     emptyInv = false;
                     break;
                 }
             }
         }
         if (!emptyInv) {
             Block blockAtPlayer = location.getBlock();
             blockAtPlayer.setType(Material.CHEST);
             Chest deathChest = (Chest)blockAtPlayer.getState();
             for (ItemStack item : playerInventory.getContents()) {
                if (item.getTypeId() > 0) {
                     playerInventory.remove(item);
                     deathChest.getInventory().addItem(item);
                 }
             }
             Block deathSignBlock = blockAtPlayer.getRelative(BlockFace.UP);
             deathSignBlock.setType(Material.SIGN_POST);
             Sign sign = (Sign)deathSignBlock.getState();
             sign.setLine(0, player.getName());
             sign.setLine(1, "ended their");
             sign.setLine(2, "pitiful life");
             sign.setLine(3, "here.");
         }
         player.setHealth(0);
     }
     
 }
