 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of Contao2.
  * 
  * Contao2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * Contao2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Contao2.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.contao2.manager;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.json.simple.JSONObject;
 
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.contao2.core.Core;
 import de.minestar.contao2.units.ContaoGroup;
 import de.minestar.contao2.units.Settings;
 import de.minestar.core.MinestarCore;
 import de.minestar.core.units.MinestarPlayer;
 import de.minestar.minestarlibrary.events.PlayerChangedGroupEvent;
 import de.minestar.minestarlibrary.utils.ChatUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class PlayerManager {
     private HashMap<ContaoGroup, String> onlineList;
     private HashMap<ContaoGroup, HashSet<String>> groupMap;
     private ConcurrentHashMap<String, ContaoGroup> playerMap;
 
     public PlayerManager() {
 
         this.onlineList = new HashMap<ContaoGroup, String>();
         this.groupMap = new HashMap<ContaoGroup, HashSet<String>>();
         this.playerMap = new ConcurrentHashMap<String, ContaoGroup>();
         this.initGroupMap();
         this.onReload();
     }
 
     private void initGroupMap() {
         for (ContaoGroup group : ContaoGroup.values()) {
             this.groupMap.put(group, new HashSet<String>());
         }
     }
 
     private void onReload() {
         Player[] playerList = Bukkit.getOnlinePlayers();
         for (Player player : playerList) {
             this.updatePlayer(player);
         }
         // UPDATE ONLINE-LISTS
         this.updateOnlineLists();
     }
 
     public void updatePlayer(String playerName) {
         // REMOVE PLAYER FROM THE GROUPMAP
         this.removePlayer(playerName);
 
         // ADD PLAYER
         this.addPlayer(playerName);
     }
 
     public void updatePlayer(Player player) {
         this.updatePlayer(player.getName());
     }
 
     public void printOnlineList(CommandSender sender) {
         // WRITE HEAD
         ChatUtils.writeMessage(sender, ChatColor.GOLD + "" + Bukkit.getOnlinePlayers().length + " / " + Bukkit.getMaxPlayers());
         // WRITE GROUPS
         this.printSingleGroup(Settings.getAdminColor(), ContaoGroup.ADMIN, sender);
         // TODO: Implement other things for mod
         this.printSingleGroup(Settings.getPayColor(), ContaoGroup.PAY, sender);
         this.printSingleGroup(Settings.getFreeColor(), ContaoGroup.FREE, sender);
         this.printSingleGroup(Settings.getProbeColor(), ContaoGroup.PROBE, sender);
         this.printSingleGroup(Settings.getDefaultColor(), ContaoGroup.DEFAULT, sender);
         this.printSingleGroup(Settings.getXColor(), ContaoGroup.X, sender);
     }
 
     private void printSingleGroup(ChatColor color, ContaoGroup group, CommandSender sender) {
         if (this.groupMap.get(group).size() > 0)
             ChatUtils.writeMessage(sender, color + group.name() + " ( " + this.groupMap.get(group).size() + " ) : " + this.onlineList.get(group));
     }
 
     public String updateGroupManagerGroup(String playerName, ContaoGroup group) {
         return this.updateGroupManagerGroup(playerName, group.getName());
     }
 
     public String updateGroupManagerGroup(String playerName, String groupName) {
         String oldGroupName = MinestarCore.getPlayer(playerName).getGroup();
         // FINALLY CHANGE THE GROUP
         String newGroup = MinestarCore.getPlayer(playerName).setGroup(groupName);
         this.updatePlayer(playerName);
         this.updateOnlineLists();
 
         // CALL PlayerChangedGroupEvent-EVENT
         if (!newGroup.equalsIgnoreCase(oldGroupName)) {
             PlayerChangedGroupEvent event = new PlayerChangedGroupEvent(playerName, oldGroupName, newGroup);
             Bukkit.getServer().getPluginManager().callEvent(event);
         }
         return newGroup;
     }
 
     public void updateOnlineLists() {
         StringBuilder sBuilder = new StringBuilder();
         for (Map.Entry<ContaoGroup, HashSet<String>> entry : this.groupMap.entrySet()) {
             sBuilder.setLength(0);
             for (String thisPlayer : entry.getValue()) {
                sBuilder.append(thisPlayer);
                sBuilder.append(", ");
             }
             if (sBuilder.length() > 2)
                 sBuilder.delete(sBuilder.length() - 2, sBuilder.length());
 
             this.onlineList.put(entry.getKey(), sBuilder.toString());
         }
         this.saveJSON();
     }

     private void addPlayer(String playerName) {
         // GET GROUP & PLAYER
         MinestarPlayer thisPlayer = MinestarCore.getPlayer(playerName);
         ContaoGroup thisGroup = ContaoGroup.getGroup(thisPlayer.getGroup());
         if (thisGroup == null)
             thisGroup = ContaoGroup.DEFAULT;
 
         // FINALLY ADD THE PLAYER
         this.playerMap.put(playerName, thisGroup);
         this.groupMap.get(thisGroup).add(playerName);
     }
 
     public void removePlayer(String playerName) {
         // GET MINESTARPLAYER
         MinestarPlayer thisPlayer = MinestarCore.getPlayer(playerName);
 
         // REMOVE PLAYER FROM THE GROUPMAP
         ContaoGroup thisGroup = ContaoGroup.getGroup(thisPlayer.getGroup());
         this.groupMap.get(thisGroup).remove(playerName);
 
         // REMOVE PLAYER FROM THE PLAYERMAP
         this.playerMap.remove(playerName);
     }
 
     public void movePlayer(PlayerChangedGroupEvent event) {
         ContaoGroup oldGroup = ContaoGroup.getGroup(event.getOldGroupName());
         ContaoGroup newGroup = ContaoGroup.getGroup(event.getNewGroupName());
         this.groupMap.get(oldGroup).remove(event.getPlayerName());
         this.groupMap.get(newGroup).add(event.getPlayerName());
         this.playerMap.put(event.getPlayerName(), newGroup);
     }
 
     public ContaoGroup getGroup(Player player) {
         return this.getGroup(player.getName());
     }
 
     public ContaoGroup getGroup(String playerName) {
         return this.playerMap.get(playerName);
     }
 
     /**
      * Use this for player who <b>are not</b> in the HashSets! These are people
      * who are connecting the to server or they are offline!
      * 
      * @param playerName
      *            The name of the player
      * @param group
      * @return True when player is in group
      */
     public boolean isOfflinePlayerInGroup(String playerName, ContaoGroup group) {
         String groupName = UtilPermissions.getGroupName(playerName, "world");
         switch (group) {
             case ADMIN :
                 return groupName.equalsIgnoreCase(ContaoGroup.ADMIN.getName());
             case MOD :
                 return groupName.equalsIgnoreCase(ContaoGroup.MOD.getName());
             case PAY :
                 return groupName.equalsIgnoreCase(ContaoGroup.PAY.getName()) || groupName.equalsIgnoreCase(ContaoGroup.MOD.getName()) || groupName.equalsIgnoreCase(ContaoGroup.ADMIN.getName());
             case FREE :
                 return groupName.equalsIgnoreCase(ContaoGroup.FREE.getName());
             case PROBE :
                 return groupName.equalsIgnoreCase(ContaoGroup.PROBE.getName());
             case DEFAULT :
                 return groupName.equalsIgnoreCase(ContaoGroup.DEFAULT.getName());
             case X :
                 return groupName.equalsIgnoreCase(ContaoGroup.X.getName());
             default :
                 throw new RuntimeException("Wrong groupId " + group.ordinal());
         }
     }
 
     public boolean isInGroup(Player player, ContaoGroup group) {
         return this.isInGroup(player.getName(), group);
     }
 
     public boolean isInGroup(String playerName, ContaoGroup group) {
         return this.getGroup(playerName).equals(group);
     }
 
     public int getFreeSlots() {
         return Settings.getFreeSlots() - this.groupMap.get(ContaoGroup.FREE).size();
     }
 
     @SuppressWarnings("unchecked")
     /**
      * Store all online player in a JSON file, so we can display it on the website
      */
     private void saveJSON() {
         JSONObject json = new JSONObject();
         // FILL JSON FILE WITH DATA
 
         json.put("ConnectedUsers", Bukkit.getOnlinePlayers().length);
         json.put("ConnectedDefaultUsers", this.groupMap.get(ContaoGroup.DEFAULT).size() + this.groupMap.get(ContaoGroup.X).size());
         json.put("ConnectedProbeUsers", this.groupMap.get(ContaoGroup.PROBE).size());
         json.put("ConnectedFreeUsers", this.groupMap.get(ContaoGroup.FREE).size());
         json.put("ConnectedPayUsers", this.groupMap.get(ContaoGroup.PAY).size());
         json.put("ConnectedAdmins", this.groupMap.get(ContaoGroup.ADMIN).size());
         json.put("FreeUserSlots", Settings.getFreeSlots());
         json.put("MaxPublicSlots", Settings.getMaxSlots());
         json.put("TotalSlots", Bukkit.getMaxPlayers());
 
         // WRITE JSON FILE
         try {
             File f = new File(Settings.getJSONFilePath());
             if (!f.exists())
                 f.createNewFile();
             Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF8"));
             writer.write(json.toJSONString());
             writer.flush();
             writer.close();
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't save the JSON file!");
         }
     }
 }
