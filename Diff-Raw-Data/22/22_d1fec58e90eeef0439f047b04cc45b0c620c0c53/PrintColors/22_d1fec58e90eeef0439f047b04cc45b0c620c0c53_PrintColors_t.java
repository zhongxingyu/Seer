 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.seed419;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author seed419
  */
 public class PrintColors {
 
 
     private String[] rainbowCodes = {"&4","&c","&6","&e","&a","&2","&b","&3","&9","&5","&d"};
     private int position;
     private ChatColors cc;
 
 
     public PrintColors(ChatColors cc) {
         this.cc = cc;
     }
 
 
     public void printcolors(CommandSender sender){
         sender.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_RED + "C"
                 + ChatColor.RED + "h" + ChatColor.GOLD + "a" + ChatColor.YELLOW + "t" + ChatColor.GREEN + "C"
                 + ChatColor.DARK_GREEN + "o" + ChatColor.AQUA + "l" + ChatColor.DARK_AQUA + "o" + ChatColor.BLUE
                 + "r" + ChatColor.LIGHT_PURPLE + "s" + ChatColor.DARK_PURPLE + ChatColor.GOLD + "]");
         sender.sendMessage(ChatColor.GRAY + "Syntax for Chat Colors is an ampersand '&', followed by");
         sender.sendMessage(ChatColor.GRAY + "either a number or character as follows:");
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
         sender.sendMessage("");
         sender.sendMessage(ChatColor.GRAY + "Other: ");
         sender.sendMessage("k " + ChatColor.MAGIC + "m" + ChatColor.RESET +  "   l " + ChatColor.BOLD + "Bold" + ChatColor.RESET +
                 "   o" + ChatColor.ITALIC + " Italics" + "   n " + ChatColor.UNDERLINE + "Underline");
         sender.sendMessage("m " + ChatColor.STRIKETHROUGH + "Strikethrough" + ChatColor.RESET + "   r Reset Formatting");
 
     }
 
     public void rainbowize(CommandSender sender, String[] args) {
         position = 0;
         String colorMessage;
         String fullMessage = "";
         for(String y : args){
             colorMessage = appendColors(y);
             fullMessage+=colorMessage+" ";
         }
         send(sender, fullMessage);
     }
 
     private String appendColors(String y) {
         StringBuilder sb = new StringBuilder(y);
         for(int x = 0; x < y.length()*3; x+=3) {
             sb.insert(x, getColor(position));
             if (position == 10) {
                 position = 0;
             }
             position++;
         }
       return sb.toString();
     }
 
     private String getColor(int position){
         return rainbowCodes[position];
     }
 
     private void send(CommandSender sender, String message) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             player.chat(message);
         } else {
            cc.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "[Server] " + message);
         }
     }
 }
