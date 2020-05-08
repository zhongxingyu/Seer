 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.SaveablePlayer.PlayerRank;
 
 /**
  * Demote a player by a single rank.
  * @author UndeadScythes
  */
 public class DemoteCmd extends PlayerCommandExecutor {
     /**
      * @inheritDocs
      */
     @Override
     public void playerExecute(SaveablePlayer player, String[] args) {
         SaveablePlayer target;
         if(numArgsHelp(1) && (target = getMatchingPlayer(args[0])) != null && notSelf(target)) {
             PlayerRank rank;
            if((rank = target.demote()) != null) {
                 player.sendMessage(Color.MESSAGE + target.getDisplayName() + " has been demoted to " + rank.toString() + ".");
                 target.sendMessage(Color.MESSAGE + "You have been demoted to " + rank.toString() + ".");
             } else {
                 player.sendMessage(Color.ERROR + "You can't demote this player any further.");
             }
         }
     }
 }
