 package com.undeadscythes.udsplugin.commands;
 
 import com.undeadscythes.udsplugin.*;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.util.Vector;
 
 /**
  * Description.
  * @author UndeadScythes
  */
 public class CityCmd extends PlayerCommandExecutor {
     /**
      * @inheritDocs
      */
     @Override
     public void playerExecute(SaveablePlayer player, String[] args) {
         Region city;
         if(args.length == 1) {
             if(args[0].equals("set") && (city = getCurrentRegion()).getType() == Region.RegionType.CITY && getMunicipality(city.getName()) != null) {
                 city.setWarp(player.getLocation());
                 player.sendMessage(Color.MESSAGE + "City spawn point set.");
             } else if(args[0].equals("list")) {
                 sendPage(1, player);
             } else {
 //                subCmdHelp(args);
             }
         } else if(args.length == 2) {
             int page;
             if(args[0].equals("new") && canAfford(Config.CITY_COST) && noCensor(args[1]) && notRegion(args[1])) {
                 Vector min = player.getLocation().add(-100, 0, -100).toVector().setY(0);
                 Vector max = player.getLocation().add(100, 0, 100).toVector().setY(player.getWorld().getMaxHeight());
                 city = new Region(args[1], min, max, player.getLocation(), player, "", Region.RegionType.CITY);
                 if(noOverlaps(city)) {
                     player.debit(Config.CITY_COST);
                     UDSPlugin.getRegions().put(args[1], city);
                     UDSPlugin.getCities().put(args[1], city);
                     city.placeMoreMarkers();
                     city.placeTowers();
                     player.sendMessage(Color.MESSAGE + "City founded.");
                     Bukkit.broadcastMessage(Color.BROADCAST + player.getDisplayName() + " has just founded " + args[1] + ".");
                 }
            } else if(args[0].equals("leave") && (city = getMatchingCity(args[1])) != null) {
                 if(city.delMember(player)) {
                     player.sendMessage(Color.MESSAGE + "You have left " + city.getName() + ".");
                 } else {
                     player.sendMessage(Color.ERROR + "You are not a citizen of " + city.getName() + ".");
                 }
             } else if(args[0].equals("warp") && (city = getMatchingCity(args[1])) != null && notJailed() && notPinned()) {
                 player.quietTeleport(city.getWarp());
             } else if(args[0].equals("list") && (page = parseInt(args[1])) != -1) {
                 sendPage(page, player);
             } else {
 //                subCmdHelp(args);
             }
         } else if(numArgsHelp(3)) {
             SaveablePlayer target;
             if(args[0].equals("invite") && (city = getMunicipality(args[1])) != null && (target = getMatchingPlayer(args[2])) != null) {
                 if(city.addMember(target)) {
                     player.sendMessage(Color.MESSAGE + target.getDisplayName() + " was added as a citizen of " + city.getName() + ".");
                     target.sendMessage(Color.MESSAGE + "You have been added as a citizen of " + city.getName());
                 } else {
                     player.sendMessage(Color.ERROR + target.getDisplayName() + " is already a citizen of " + city.getName() + ".");
                 }
             } else if(args[0].equals("banish") && (city = getMunicipality(args[1])) != null && (target = getMatchingPlayer(args[2])) != null) {
                 if(city.delMember(target)) {
                     player.sendMessage(Color.MESSAGE + target.getDisplayName() + " has been banished from " + city.getName() + ".");
                     target.sendMessage(Color.MESSAGE + "You have been banished from " + city.getName() + ".");
                     target.quietTeleport(city.getWarp());
                 } else {
                     player.sendMessage(Color.ERROR + target.getDisplayName() + " is not a citizen of " + city.getName() + ".");
                 }
             } else {
 //                subCmdHelp();
             }
         }
     }
 
     /**
      * Sends a full page of cities to the player.
      * @param page Page to send.
      * @param player Player to send page to.
      */
     private void sendPage(int page, SaveablePlayer player) {
         ArrayList<Region> cities = UDSPlugin.getCities().getSortedValues(new SortByPop());
         int pages = (cities.size() + 8) / 9;
         if(pages == 0) {
             player.sendMessage(Color.MESSAGE + "There are no cities yet.");
         } else if(page > pages) {
             player.sendMessage(Message.NO_PAGE);
         } else {
             player.sendMessage(Color.MESSAGE + "--- Current Cities " + (pages > 1 ? "Page " + page + "/" + pages + " " : "") + "---");
             int posted = 0;
             int skipped = 1;
             for(Region city : cities) {
                 if(skipped > (page - 1) * 9 && posted < 9) {
                     player.sendMessage(Color.ITEM + city.getName() + " - " + Color.TEXT + "Mayor " + city.getOwner() + ", Pop. " + (city.getMemberNo() + 1));
                     posted++;
                 } else {
                     skipped++;
                 }
             }
         }
     }
 }
 
 /**
  * Compare regions by member size.
  * @author UndeadScythe
  */
 class SortByPop implements Comparator<Region> {
     /**
      * @inheritDoc
      */
     @Override
     public int compare(Region region1, Region region2) {
         return region2.getMemberNo() - region1.getMemberNo();
     }
 }
