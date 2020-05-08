 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of AdminStuff.
  * 
  * AdminStuff is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * AdminStuff is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with AdminStuff.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.AdminStuff.manager;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import de.minestar.AdminStuff.Core;
 import de.minestar.AdminStuff.database.DatabaseHandler;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class PlayerManager {
 
     private Map<String, ASPlayer> players;
     private Set<String> banList = new HashSet<String>();
     private DatabaseHandler dbHandler;
 
     private CraftServer cServer = (CraftServer) Bukkit.getServer();
 
     private File dataFolder = null;
 
     public PlayerManager(DatabaseHandler dbHandler, File dataFolder) {

         this.dataFolder = dataFolder;
         players = dbHandler.loadPlayer();
         loadBanList();
     }
 
     /**
      * Get player when player is existing or create a new one
      * 
      * @param player
      *            The target
      * @return Always a ASPlayer object!
      */
     public ASPlayer getPlayer(Player player) {
         return getPlayer(player.getName());
     }
 
     /**
      * Get player when player is existing or create a new one
      * 
      * @param playerName
      *            The targets name, can be capitalized
      * @return Always a ASPlayer object!
      */
     public ASPlayer getPlayer(String playerName) {
         // search for existing ASPlayer
         String temp = playerName.toLowerCase();
         ASPlayer target = players.get(temp);
         // ASPlayer already exists
         if (target != null)
             return target;
         else
             // create a new player and store him in database
             return addPlayer(playerName, temp);
     }
 
     // add player to map and database
     private ASPlayer addPlayer(String accountName, String playerName) {
         ASPlayer player = new ASPlayer(accountName);
         players.put(playerName, player);
         dbHandler.addPlayer(accountName);
         return player;
     }
 
     // ******************************************************
     // ************* COMMOND USER MODIFACTION ***************
     // ******************************************************
 
     // glue or unglue the player and store it in database
     public void setGlue(ASPlayer target, Location loc) {
         target.setGlueLocation(loc);
         dbHandler.saveGlueLocation(target.getPlayerName(), loc);
     }
 
     public void updateLastSeen(ASPlayer target) {
         target.updateLastSeen();
         dbHandler.updateLastSeen(target.getPlayerName());
     }
 
     public void setAFK(ASPlayer target, boolean isAFK, Player player) {
         target.setAFK(isAFK);
         target.updateNick(player);
     }
 
     public void updateNickName(ASPlayer target, String name, Player player) {
         target.setNickname(name);
         target.updateNick(player);
         dbHandler.saveDisplayName(target.getPlayerName(), name);
     }
 
     public void setGodMode(ASPlayer target, boolean isGod) {
         target.setGod(isGod);
         dbHandler.saveGod(target.getPlayerName(), isGod);
     }
 
     public void setGameMode(ASPlayer target, Player player, GameMode mode) {
         target.setGameMode(mode);
         player.setGameMode(mode);
         dbHandler.saveGameMode(target.getPlayerName(), mode);
     }
 
     public void setMuted(ASPlayer target, boolean isMuted) {
         target.setMuted(isMuted);
         dbHandler.saveMuted(target.getPlayerName(), isMuted);
     }
 
     public void setSlapped(ASPlayer target, Player player, boolean isSlapped) {
         target.setSlapped(isSlapped);
         target.updateNick(player);
     }
 
     // ******************************************************
     // *************** INVENTORY HANDLING *******************
     // ******************************************************
 
     public void backupInventory(ASPlayer target, ItemStack[] content) {
         target.saveInventory(content);
         dbHandler.saveInventory(target.getPlayerName(), content);
     }
 
     public ItemStack[] restoreInventory(ASPlayer target) {
         ItemStack[] backUp = target.getInvBackUp();
         // use local copy and delete from database
         if (backUp != null)
             dbHandler.deleteInventory(target.getPlayerName());
         // load from database and delete it
         else
             backUp = dbHandler.loadInventory(target.getPlayerName());
 
         return backUp;
     }
 
     // ******************************************************
     // ***************** BANN HANDLING **********************
     // ******************************************************
 
     public void bannPlayer(ASPlayer target, Player player, String message) {
         if (player != null) {
             player.setBanned(true);
             player.kickPlayer(message);
         }
 
         target.setBanned(true);
         banList.add(target.getPlayerName().toLowerCase());
         dbHandler.saveBanned(target.getPlayerName(), true);
         cServer.getHandle().addUserBan(target.getPlayerName());
         saveBanList();
     }
 
     public void unbannPlayer(ASPlayer target, Player player) {
         if (player != null)
             player.setBanned(false);
 
         target.setBanned(false);
         target.setBanEndTime(0);
         target.setBanEndTime(0);
         banList.remove(target.getPlayerName().toLowerCase());
         dbHandler.saveBanned(target.getPlayerName(), false);
         cServer.getHandle().removeUserBan(target.getPlayerName());
         saveBanList();
     }
 
     public boolean isPermBanned(String playerName) {
         return banList.contains(playerName.toLowerCase());
     }
 
     private void saveBanList() {
         try {
             File banListFile = new File(dataFolder, "banned-adminstuff-players.txt");
             BufferedWriter bWriter = new BufferedWriter(new FileWriter(banListFile));
             for (String bannedPlayer : banList) {
                 bWriter.write(bannedPlayer);
                 bWriter.newLine();
             }
             bWriter.close();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't save ban list!");
         }
     }
 
     private void loadBanList() {
         try {
             File banListFile = new File(dataFolder, "banned-adminstuff-players.txt");
             if (!banListFile.exists()) {
                 ConsoleUtils.printWarning(Core.NAME, "Banlist file does not exist");
                 return;
             }
             Set<String> banList = new HashSet<String>();
             BufferedReader bReader = new BufferedReader(new FileReader(banListFile));
             String s = "";
             while ((s = bReader.readLine()) != null)
                 if (!s.isEmpty())
                     banList.add(s);
 
             bReader.close();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't load ban list!");
         }
     }
 
     public void tempBanPlayer(ASPlayer target, Player player, long time, String message) {
         if (player != null)
             player.kickPlayer(message);
 
         target.setBanEndTime(time);
         dbHandler.saveTempBann(target.getPlayerName(), time);
     }
 }
