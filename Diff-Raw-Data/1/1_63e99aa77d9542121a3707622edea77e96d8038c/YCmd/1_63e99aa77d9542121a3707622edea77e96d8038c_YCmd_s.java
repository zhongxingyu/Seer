 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.utilities.*;
 import org.bukkit.entity.*;
 
 /**
  * Allows a player to accept a request sent by another player.
  * 
  * @author UndeadScythes
  */
 public class YCmd extends CommandHandler {
     @Override
     public final void playerExecute() {
         Request request;
         SaveablePlayer sender;
         if((request = getRequest()) != null && (sender = request.getSender()) != null && sender.isOnline()) {
             int price;
             switch (request.getType()) {
                 case TP:
                     sender.teleport(player());
                     break;
                 case CLAN:
                     clanRequest(ClanUtils.getClan(request.getData()));
                     break;
                 case HOME:
                     if(canAfford(price = Integer.parseInt(request.getData())) && hasNoHome()) {
                         final Region home = RegionUtils.getRegion(RegionType.HOME, sender.getName() + "home");
                         home.clearMembers();
                         home.changeOwner(player());
                         player().debit(price);
                         sender.credit(price);
                     }
                     break;
                 case PET:
                     if(canAfford(price = Integer.parseInt(request.getData()))) {
                         for(Entity entity : sender.getWorld().getEntities()) {
                             if(entity.getUniqueId().equals(sender.getSelectedPet())) {
                                 player().debit(price);
                                 sender.credit(price);
                                 player().setPet((Tameable)entity);
                                 player().teleportHere(entity);
                                 player().sendNormal("Your bought " + sender.getNick() + "'s pet.");
                                 sender.sendNormal(player().getNick() + " bought your pet.");
                             }
                         }
                     }
                     break;
                 case PVP:
                     if(canAfford(price = Integer.parseInt(request.getData()))) {
                         sender.sendNormal(player().getNick() + " accepted your duel, may the best player win.");
                         player().sendNormal("Duel accepted, may the best player win.");
                         player().setChallenger(sender);
                         sender.setChallenger(sender);
                         player().setWager(price);
                         sender.setWager(price);
                     }
                     break;
                 case SHOP:
                     if(canAfford(price = Integer.parseInt(request.getData())) && hasNoShop()) {
                         final Region shop = RegionUtils.getRegion(RegionType.SHOP, sender.getName() + "shop");
                         shop.clearMembers();
                         shop.changeOwner(player());
                         player().debit(price);
                         sender.credit(price);
                     }
                     break;
                 default:
                     sender.sendNormal(player().getNick() + " was unable to accept your request.");
             }
         }
         UDSPlugin.removeRequest(player().getName());
     }
 
     private void clanRequest(final Clan clan) {
         clan.addMember(player());
         player().setClan(clan);
         clan.sendMessage(player().getNick() + " has joined the clan.");
         final Region base = RegionUtils.getRegion(RegionType.BASE, clan.getName() + "base");
         if(base != null) {
             base.addMember(player());
         }
     }
 }
