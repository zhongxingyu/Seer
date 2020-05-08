 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.Region.RegionFlag;
 import com.undeadscythes.udsplugin.*;
 import org.bukkit.util.*;
 
 /**
  * Home region related commands.
  * @author UndeadScythes
  */
 public class HomeCmd extends AbstractPlayerCommand {
     @Override
     public void playerExecute() {
         Region home;
         SaveablePlayer target;
         int price;
         if(args.length == 0) {
             if((home = getHome()) != null && notJailed() && notPinned()) {
                 player.teleport(home.getWarp());
             }
         } else if(args.length == 1) {
             if(args[0].equals("make")) {
                 if(canAfford(Config.homeCost) && noHome()) {
                     final Vector min = player.getLocation().add(-10, 28, -10).toVector();
                     final Vector max = player.getLocation().add(10, 12, 10).toVector();
                     home = new Region(player.getName() + "home", min, max, player.getLocation(), player, "", Region.RegionType.HOME);
                     if(noOverlaps(home)) {
                         player.debit(Config.homeCost);
                         UDSPlugin.getRegions().put(home.getName(), home);
                         UDSPlugin.getHomes().put(home.getName(), home);
                         home.placeCornerMarkers();
                         player.sendMessage(Color.MESSAGE + "Home area protected.");
                     }
                 }
             } else if(args[0].equals("clear")) {
                 if((home = getHome()) != null) {
                     UDSPlugin.getRegions().remove(home.getName());
                     UDSPlugin.getHomes().remove(home.getName());
                     player.sendMessage(Color.MESSAGE + "Home protection removed.");
                 }
             } else if(args[0].equals("set")) {
                 if((home = getHome()) != null) {
                     home.setWarp(player.getLocation());
                     player.sendMessage(Color.MESSAGE + "Home warp point set.");
                 }
             } else if(args[0].equals("roomies")) {
                 String message = "";
                 for(Region otherHome : UDSPlugin.getHomes().values()) {
                     if(otherHome.hasMember(player)) {
                         message = message.concat(otherHome.getOwner().getNick() + ", ");
                     }
                     if(!message.isEmpty()) {
                         player.sendMessage(Color.MESSAGE + "You are room mates with:");
                         player.sendMessage(Color.TEXT + message.substring(0, message.length() - 2));
                     }
                     message = "";
                     if((home = UDSPlugin.getHomes().get(player.getName() + "home")) != null) {
                         for(SaveablePlayer member : home.getMembers()) {
                             message = message.concat(member.getNick() + ", ");
                         }
                     }
                     if(message.equals("")) {
                         player.sendMessage(Color.MESSAGE + "You have no room mates.");
                     } else {
                         player.sendMessage(Color.MESSAGE + "Your room mates are:");
                         player.sendMessage(Color.TEXT + message.substring(0, message.length() - 2));
                     }
                 }
             } else if(args[0].equals("lock")) {
                 if((home = getHome()) != null) {
                     home.setFlag(RegionFlag.LOCK);
                     player.sendMessage(Color.MESSAGE + "Your home is now locked.");
                 }
             } else if(args[0].equals("unlock")) {
                 if((home = getHome()) != null) {
                     home.setFlag(RegionFlag.LOCK);
                     home.toggleFlag(RegionFlag.LOCK);
                     player.sendMessage(Color.MESSAGE + "Your home is now unlocked.");
                 }
             } else if(args[0].equals("help")) {
                 sendHelp(1);
             } else if((target = getMatchingPlayer(args[0])) != null && (home = getHome(target)) != null && (isRoomie(home) || hasPerm(Perm.HOME_OTHER)) && notJailed() && notPinned()) {
                 player.teleport(home.getWarp());
             }
         } else if(args.length == 2) {
             Direction direction;
             if(args[0].equals("expand")) {
                 if((home = getHome()) != null && canAfford(Config.expandCost) && (direction = getCardinalDirection(args[1])) != null) {
                     home.expand(direction, 1);
                     if(noOverlaps(home)) {
                         player.debit(Config.expandCost);
                        player.sendMessage(Color.MESSAGE + "Your home ahs been expanded.");
                     } else {
                         home.expand(direction, -1);
                     }
                 }
             } else if(args[0].equals("boot")) {
                 if((home = getHome()) != null && (target = getMatchingPlayer(args[1])) != null && isOnline(target) && isInHome(target, home)) {
                     target.teleport(player.getWorld().getSpawnLocation());
                     target.sendMessage(Color.MESSAGE + player.getNick() + " has booted you from their home.");
                     player.sendMessage(Color.MESSAGE + target.getNick() + " has been booted.");
                 }
             } else if(args[0].equals("add")) {
                 if((target = getMatchingPlayer(args[1])) != null && (home = getHome()) != null) {
                     home.addMember(target);
                     player.sendMessage(Color.MESSAGE + target.getNick() + " has been added as your room mate.");
                     if(target.isOnline()) {
                         target.sendMessage(Color.MESSAGE + "You have been added as " + player.getNick() + "'s room mate.");
                     }
                 }
             } else if(args[0].equals("kick")) {
                 if((target = getMatchingPlayer(args[1])) != null && (home = getHome()) != null && isRoomie(target, home)) {
                     home.delMember(target);
                     player.sendMessage(Color.MESSAGE + target.getNick() + " is no longer your room mate.");
                     if(target.isOnline()) {
                         target.sendMessage(Color.MESSAGE + "You are no longer " + player.getNick() + "'s room mate.");
                     }
                 }
             } else {
                 subCmdHelp();
             }
         } else if(numArgsHelp(3)) {
             if(args[0].equals("sell")) {
                 if((getHome()) != null && (target = getMatchingPlayer(args[1])) != null && isOnline(target) && (price = parseInt(args[2])) != -1) {
                     player.sendMessage(Message.REQUEST_SENT);
                     target.sendMessage(Color.MESSAGE + player.getNick() + " wants to sell you their house for " + price + " " + Config.currencies + ".");
                     target.sendMessage(Message.REQUEST_Y_N);
                     UDSPlugin.getRequests().put(target.getName(), new Request(player, Request.RequestType.HOME, price, target));
                 }
             } else {
                 subCmdHelp();
             }
         }
     }
 }
