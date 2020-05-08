 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 
 /**
  * Change a players nickname.
  * @author UndeadScythes
  */
 public class NickCmd extends CommandWrapper {
     @Override
     public void playerExecute() {
         SaveablePlayer target;
         if(args.length == 1) {
             if(noCensor(args[0])) {
                 if(player.getName().toLowerCase().contains(args[0].toLowerCase()) || hasPerm(Perm.NICK_OTHER)) {
                     player.setDisplayName(args[0]);
                     player.sendNormal("Your nickname has been changed to " + args[0] + ".");
                 } else {
                     player.sendError("Your nickname must be a shortened version of your Minecraft name.");
                 }
             }
         } else if(numArgsHelp(2) && hasPerm(Perm.NICK_OTHER) && (target = getMatchingPlayer(args[0])) != null && noCensor(args[1])) {
             target.setDisplayName(args[1]);
            player.sendNormal(player.getName() + "'s nickname has been changed to " + args[1] + ".");
             target.sendNormal("Your nickname has been changed to " + args[1] + ".");
         }
     }
 }
