 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.inventory.*;
 
 /**
  * Spawn items into the players inventory.
  * @author UndeadScythes
  */
 public class ICmd extends PlayerCommandExecutor {
     /**
      * @inheritDocs
      */
     @Override
     public void playerExecute(SaveablePlayer player, String[] args) {
         if(minArgsHelp(1) && maxArgsHelp(2)) {
             ItemStack item;
             if((item = getItem(args[0])) != null) {
                 int amount;
                 if(args.length == 1) {
                     item.setAmount(item.getMaxStackSize());
                 } else if((amount = parseInt(args[1])) != -1) {
                     item.setAmount(amount);
                 }
                if(player.hasPermission(Perm.I_ADMIN)) {
                     player.getInventory().addItem(item);
                } else if(player.getVIPSpawns() > 0) {
                    if(Config.WHITELIST.contains(item.getType())) {
                        if(item.getAmount() > player.getVIPSpawns()) {
                            item.setAmount(player.getVIPSpawns());
                        }
                        if(player.useVIPSpawns(item.getAmount()) == 0) {
                            player.sendMessage(Color.MESSAGE + "You have just used up your last spawns for today.");
                        }
                        player.getInventory().addItem(item);
                    } else {
                        player.sendMessage(Color.ERROR + "Sorry, " + item.getType().name().toLowerCase().replace("_", " ") + " is not a whitelisted item.");
                    }
                 } else {
                    player.sendMessage(Color.ERROR + "You are out of spawns for today.");
                 }
             }
         }
     }
 }
