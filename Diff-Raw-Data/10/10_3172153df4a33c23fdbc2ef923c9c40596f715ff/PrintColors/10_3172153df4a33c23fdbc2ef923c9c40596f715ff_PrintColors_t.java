 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.seed419;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 /**
  *
  * @author seed419
  */
 public class PrintColors {
     
     public void printcolors(CommandSender sender){
        sender.sendMessage(ChatColor.GOLD + "====================" + ChatColor.DARK_RED + " C" 
                 + ChatColor.RED + "h" + ChatColor.GOLD + "a" + ChatColor.YELLOW + "t" + ChatColor.GREEN + " C" 
                 + ChatColor.DARK_GREEN + "o" + ChatColor.AQUA + "l" + ChatColor.DARK_AQUA + "o" + ChatColor.BLUE 
                + "r" + ChatColor.LIGHT_PURPLE + "s " + ChatColor.DARK_PURPLE + ChatColor.GOLD + "====================");
        sender.sendMessage(ChatColor.DARK_GREEN + "Syntax for Chat Colors is an ampersand '&', followed by");
         sender.sendMessage(ChatColor.DARK_GREEN + "either a number or character as follows:");
         sender.sendMessage("");
         sender.sendMessage(ChatColor.GRAY + "Dark Colors:");
         sender.sendMessage(ChatColor.DARK_BLUE + "1 Dark Blue"+ ChatColor.DARK_GREEN 
                 +" 2 Dark Green" + ChatColor.DARK_AQUA +" 3 Dark Aqua" + ChatColor.DARK_RED + " 4 Dark Red");
         sender.sendMessage(ChatColor.DARK_PURPLE + "5 Dark Purple" + ChatColor.GOLD + " 6 Gold" + ChatColor.GRAY 
                + " 7 Gray" + ChatColor.DARK_GRAY + " 8 Dark Gray" + ChatColor.BLUE + " 9 Blue" + ChatColor.BLACK +" 0 Black");
         sender.sendMessage("");
         sender.sendMessage(ChatColor.GRAY + "Bright Colors:");
         sender.sendMessage(ChatColor.GREEN + "a Green"  + ChatColor.AQUA + " b Aqua" + ChatColor.RED + " c Red" 
                 + ChatColor.LIGHT_PURPLE + " d Pink" + ChatColor.YELLOW + " e Yellow"+ChatColor.WHITE + " f White");
     }
     
 }
