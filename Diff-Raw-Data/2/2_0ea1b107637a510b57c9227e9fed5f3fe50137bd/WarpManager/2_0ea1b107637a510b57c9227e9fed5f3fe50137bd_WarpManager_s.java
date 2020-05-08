 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.manager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.bukkit.entity.Player;
 
 import de.minestar.FifthElement.core.Core;
 import de.minestar.FifthElement.core.Settings;
 import de.minestar.FifthElement.data.Warp;
 import de.minestar.FifthElement.data.WarpCounter;
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarGroup;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class WarpManager {
 
     private TreeMap<String, Warp> warpMap;
     private Map<String, WarpCounter> warpCounterMap;
 
     public WarpManager() {
         loadWarps();
     }
 
     // **************************
     // LOADING AND INITIALIZATION
     // **************************
 
     private void loadWarps() {
         warpMap = Core.dbHandler.loadWarps();
         int[] counter = countWarps();
        ConsoleUtils.printInfo(Core.NAME, "Loaded " + warpMap.size() + " Warps. There were " + counter[0] + " public and " + counter[1] + " warps");
     }
 
     private int[] countWarps() {
         // FIRST ELEMENT = GOBAL COUNTER FOR PUBLIC
         // SECOND ELEMENT = GOBAL COUNTER FOR PRIVATE
         int[] counter = new int[2];
         warpCounterMap = new HashMap<String, WarpCounter>();
 
         String owner;
         WarpCounter warpCounter;
 
         for (Warp warp : warpMap.values()) {
             owner = warp.getOwner().toLowerCase();
             // GET COUNTER FOR THE OWNER
             warpCounter = getWarpCounter(owner);
 
             // INCREMENT THE COUNTER
             if (warp.isPublic()) {
                 warpCounter.incrementPublicWarps();
                 counter[0]++;
             } else {
                 warpCounter.incrementPrivateWarps();
                 counter[1]++;
             }
         }
 
         return counter;
     }
 
     // **************************
     // ******* FIND WARPS *******
     // **************************
 
     // EXACT NAME NEEDED
     public boolean isWarpExisting(String warpName) {
         return warpMap.containsKey(warpName.toLowerCase());
     }
 
     // EXACT NAME NEEDED
     public Warp getWarp(String warpName) {
         return warpMap.get(warpName.toLowerCase());
     }
 
     public List<Warp> findWarp(String searchWord) {
 
         List<Warp> results = new ArrayList<Warp>();
         // HAVE FOUND EXACT NAME
         Warp warp = getWarp(searchWord);
         if (warp != null)
             results.add(warp);
 
         searchWord = searchWord.toLowerCase();
         warp = warpMap.ceilingEntry(searchWord).getValue();
         if (warp != null)
             results.add(warp);
 
         warp = warpMap.floorEntry(searchWord).getValue();
         if (warp != null)
             results.add(warp);
 
         return results;
     }
 
     // SEARCH FOR WARP USING CONTAINS
     public List<Warp> searchWarp(String searchWord) {
         List<Warp> results = new LinkedList<Warp>();
 
         searchWord = searchWord.toLowerCase();
 
         for (Entry<String, Warp> entry : warpMap.entrySet()) {
             if (entry.getKey().contains(searchWord))
                 results.add(entry.getValue());
         }
 
         return results;
     }
 
     // USERNAME MUST EXIST!
     public List<Warp> getUserWarps(String userName) {
 
         List<Warp> results = new ArrayList<Warp>(15);
         userName = userName.toLowerCase();
 
         for (Warp warp : warpMap.values()) {
             if (warp.getOwner().equalsIgnoreCase(userName))
                 results.add(warp);
         }
         return results;
     }
 
     // RETURN ALL PUBLIC WARPS
     public List<Warp> getPublicWarps() {
 
         // PUBLIC WARPS ARE CA. 10% OF ALL WARPS
         List<Warp> results = new ArrayList<Warp>(warpMap.size() / 10);
 
         for (Warp warp : warpMap.values()) {
             if (warp.isPublic())
                 results.add(warp);
         }
 
         return results;
     }
 
     public List<Warp> getAccessableWarps(String userName) {
 
         // PUBLIC WARPS ARE CA. 10% OF ALL WARPS AND PRIVATE WARPS ARE TO FEW TO
         // MATTER
         List<Warp> results = new ArrayList<Warp>(warpMap.size() / 10);
         for (Warp warp : warpMap.values()) {
             if (warp.canUse(userName))
                 results.add(warp);
         }
 
         return results;
     }
 
     // **************************
     // MANIPULATE CURRENT WARPS
     // **************************
 
     // NAME MUST BE UNIQUE
     public void createWarp(String warpName, Player creator) {
         Warp warp = new Warp(warpName, creator);
         incrementWarpCount(warp);
         Core.dbHandler.addWarp(warp);
         warpMap.put(warpName.toLowerCase(), warp);
     }
 
     private void incrementWarpCount(Warp warp) {
         WarpCounter counter = getWarpCounter(warp.getOwner());
 
         if (warp.isPublic())
             counter.incrementPublicWarps();
         else
             counter.incrementPrivateWarps();
     }
 
     public void deleteWarp(Warp warp) {
         decrementWarpCount(warp);
         Core.dbHandler.deleteWarp(warp);
         warpMap.remove(warp.getName().toLowerCase());
     }
 
     private void decrementWarpCount(Warp warp) {
         WarpCounter counter = getWarpCounter(warp.getOwner());
 
         if (warp.isPublic())
             counter.decrementPublicWarps();
         else
             counter.decrementPrivateWarps();
     }
 
     public void changeAccess(Warp warp, boolean toPublic) {
         WarpCounter counter = getWarpCounter(warp.getOwner());
         if (toPublic)
             counter.incrementPublicWarps();
         else
             counter.decrementPublicWarps();
 
         warp.setAccessMode(toPublic);
         Core.dbHandler.updateAccess(warp);
     }
     public void addGuest(Warp warp, String guestName) {
         warp.addGuest(guestName);
         Core.dbHandler.updateGuests(warp);
     }
 
     public void removeGuest(Warp warp, String guestName) {
         warp.removeGuest(guestName);
         Core.dbHandler.updateGuests(warp);
     }
 
     public WarpCounter getWarpCounter(String owner) {
         WarpCounter counter = warpCounterMap.get(owner.toLowerCase());
         if (counter == null) {
             counter = new WarpCounter(owner);
             warpCounterMap.put(owner.toLowerCase(), counter);
         }
         return counter;
     }
 
     public boolean canCreatePublic(String playerName) {
         // GET THE CURRENT COUNT
         WarpCounter counter = getWarpCounter(playerName);
         // GET THE GROUP OF THE PLAYER
         MinestarGroup group = MinestarCore.getPlayer(playerName).getMinestarGroup();
         // CURRENT COUNTER IS LOWER THAN ALLOWED
         return counter.getPublicWarps() < Settings.getMaxPublicWarps(group);
     }
 
     public boolean canCreatePrivate(String playerName) {
         // GET THE CURRENT COUNT
         WarpCounter counter = getWarpCounter(playerName);
         // GET THE GROUP OF THE PLAYER
         MinestarGroup group = MinestarCore.getPlayer(playerName).getMinestarGroup();
         // CURRENT COUNTER IS LOWER THAN ALLOWED
         return counter.getPrivateWarps() < Settings.getMaxPrivateWarps(group);
     }
 
     public void moveWarp(Warp warp, Player player) {
         warp.move(player);
         Core.dbHandler.updateWarpLocation(warp);
     }
 
     public void renameWarp(Warp warp, String newName) {
         // DELETE OLD ENTRY
         this.warpMap.remove(warp.getName().toLowerCase());
         // RENAME
         warp.rename(newName);
         this.warpMap.put(warp.getName().toLowerCase(), warp);
         // PERSIST
         Core.dbHandler.updateWarpName(warp);
     }
 
     public void addUseMode(Warp warp, byte useMode) {
         warp.addUseMode(useMode);
 
         // PERSIST
         Core.dbHandler.updateUseMode(warp);
     }
 
     public void removeUseMode(Warp warp, byte useMode) {
         warp.addUseMode(useMode);
 
         // PERSIST
         Core.dbHandler.updateUseMode(warp);
     }
 }
