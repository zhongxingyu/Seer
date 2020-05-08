 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 
 /**
  * Clears all items from a player's inventory.
  * 
  * @author UndeadScythes
  */
 public class CiCmd extends CommandHandler {
     @Override
     public final void playerExecute() {
        if(argsLength() == 0) {
             player().getInventory().clear(-1, -1);
             player().sendNormal("Inventory cleared.");
        } else if(maxArgsHelp(1)) {
             final SaveablePlayer target = matchOnlinePlayer(arg(0));
             if(target != null && notSelf(target) && outRanks(target)) {
                 target.getInventory().clear(-1, -1);
                 player().sendNormal(target.getNick() + "'s inventory was cleared.");
                 target.sendMessage("Your inventory has been cleared by a mod.");
             }
         }
     }
 }
