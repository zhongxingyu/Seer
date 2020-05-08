 package com.cianmcgovern.giveit;
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 
 /**
  * This class deals with the /giveto command
  * 
  * @author cianmcgovern91@gmail.com
  * @version 1.3
  *
  */
 public class GiveTo {
 
     private IdChange idchange = new IdChange();
     private int amount = GiveIt.amount;
     private String name = GiveIt.name;
     private final LogToFile log = new LogToFile();
 	
     public boolean giveto(CommandSender sender, String[] trimmedArgs) {
 		
         if (trimmedArgs.length < 3) {
             return false;
         }
 		
         Player player = (Player) sender;
         Player receiver;
 
         receiver = GiveIt.server.getPlayer(trimmedArgs[0]);
         PlayerInventory inventory = receiver.getInventory();
         String item = this.idchange.idChange(trimmedArgs[ 1 ]);
         
         // Check to see if the player requested an item that isn't allowed
         if (GiveIt.prop.getProperty(item) == null) {
             player.sendMessage(
                     ChatColor.DARK_RED
                             + "GiveIt: Sorry but it is not possible to spawn that item"); // $NON-NLS-1$
             return true;
         } else if (GiveIt.prop.getProperty(item).contains(".") == true) { // $NON-NLS-1$
             // Parse the player's name from the allowed.txt file
             String in = GiveIt.prop.getProperty(item);
             int position = in.indexOf("."); // $NON-NLS-1$
 
             this.amount = Integer.parseInt(in.substring(0, position));
             this.name = in.substring(position + 1, in.length());
 
             if (Integer.parseInt(trimmedArgs[ 2 ]) <= this.amount
                     && this.name.equalsIgnoreCase(player.getName())) {
                 this.amount = Integer.parseInt(GiveIt.prop.getProperty(item));
                 ItemStack itemstack = new ItemStack(Integer.valueOf(item));
 
                 itemstack.setAmount(Integer.parseInt(trimmedArgs[ 2 ]));
                 inventory.addItem(itemstack);
                 // Log the player's requested items to log file
                 this.log.writeOut(player, item, trimmedArgs[ 2 ]);
                 player.sendMessage(
                         ChatColor.BLUE + "GiveIt: Item added to your inventory"); // $NON-NLS-1$
             } // Send a message to the player telling them to choose a lower amount
             else if (Integer.parseInt(trimmedArgs[ 2 ]) > this.amount
                     && this.name.equalsIgnoreCase(player.getName())) {
                 player.sendMessage(
                         ChatColor.DARK_RED
                                 + "GiveIt: Sorry, please choose a lower amount");
             } // $NON-NLS-1$
             else if (!this.name.equalsIgnoreCase(player.getName())) {
                 player.sendMessage(
                         ChatColor.DARK_RED
                                 + "GiveIt: Sorry, but you are not allowed to spawn that item");
             } // $NON-NLS-1$
             return true;
         } else if (GiveIt.prop.getProperty(item).contains(".") == false) { // $NON-NLS-1$
             this.amount = Integer.parseInt(GiveIt.prop.getProperty(item));
             ItemStack itemstack = new ItemStack(Integer.valueOf(item));
             
             if (Integer.parseInt(trimmedArgs[ 2 ]) <= this.amount) {
                 itemstack.setAmount(Integer.parseInt(trimmedArgs[ 2 ]));
                 if ((trimmedArgs.length > 3)
                         && trimmedArgs[ 3 ].isEmpty() == false) {
                     System.out.println("Detected extra arg!!"); // $NON-NLS-1$
                     itemstack.setDurability(Short.parseShort(trimmedArgs[ 2 ]));
                 }
                 inventory.addItem(itemstack);
                 player.sendMessage(
                         ChatColor.BLUE + "GiveIt: Item added to "
                         + trimmedArgs[0] + "'s inventory"); // $NON-NLS-1$
                 receiver.sendMessage(
                         ChatColor.BLUE + "GiveIt: You have been given "
                         + trimmedArgs[2] + " of " + item + " by "
                         + player.getDisplayName());
                 // Log the player's requested items to log file
                 this.log.writeOut(player, item, trimmedArgs[ 2 ]);
                 return true;
             } // Send a message to the player telling them to choose a lower amount
            else if (Integer.parseInt(trimmedArgs[ 2 ]) > this.amount
                    && this.name.equalsIgnoreCase(player.getName())) {
                 player.sendMessage(
                         ChatColor.DARK_RED
                                 + "GiveIt: Sorry, please choose a lower amount");
                 return true;
             } // $NON-NLS-1$
            else if (!this.name.equalsIgnoreCase(player.getName())) {
                player.sendMessage(
                        ChatColor.DARK_RED
                                + "GiveIt: Sorry, but you are not allowed to spawn that item");
                return true;
            } // $NON-NLS-1$
           
         } 
         return true;
     }
 
 }
