 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of 'AdminStuff'.
  * 
  * 'AdminStuff' is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * 'AdminStuff' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with 'AdminStuff'.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * AUTHOR: GeMoschen
  * 
  */
 
 package com.bukkit.Souli.AdminStuff;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.Configuration;
 
 public class ASPlayer {
     private Map<Integer, ASItem> unlimitedList = new HashMap<Integer, ASItem>();
     private String playerName = "";
     private boolean isAFK = false;
     private boolean isMuted = false;
     private boolean isGlued = false;
     private boolean isSlapped = false;
     private boolean isBanned = false;
     private boolean isTempBanned = false;
     private boolean hideChat = false;
     private boolean isGod = false;
     private boolean classicMode = false;
     private long banEndTime = 0;
     private Location glueLocation = null;
     private String lastSender = null;
     private String[] Recipients = null;
     private String nickname = "";
     private ItemStack[] invBackUp = new ItemStack[36];
     private String lastSeen = "NEVER";
 
     public ASPlayer(String playerName) {
         this.playerName = playerName;
         this.loadConfig();
     }
 
     public ASPlayer(Player player) {
         this.playerName = player.getName();
         this.loadConfig();
     }
 
     /**
      * UPDATE LAST SEEN
      */
     public void updateLastSeen() {
         SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy");
         SimpleDateFormat hourFormat = new SimpleDateFormat("k:m:s");
         setLastSeen(dateFormat.format(new Date()) + " - " + hourFormat.format(new Date()));
     }
 
     /**
      * GET LAST SEEN
      */
     public String getLastSeen() {
         return lastSeen;
     }
 
     /**
      * SET LAST SEEN
      */
     public void setLastSeen(String lastSeen) {
         if (lastSeen == null) {
             lastSeen = ASLocalizer.getValue("LAST_SEEN_NEVER");
         }
         this.lastSeen = lastSeen;
     }
 
     /**
      * IS RECIPIENT
      */
     public boolean isRecipient(String playerName) {
         if (Recipients == null)
             return false;
 
         for (String name : Recipients) {
             if (name.equalsIgnoreCase(playerName))
                 return true;
         }
         return false;
     }
 
     /**
      * SAVE INVENTORY
      * 
      * @param toSave
      *            Inventory to save
      */
     public void saveInventory(Inventory toSave) {
         invBackUp = new ItemStack[36];
         for (int i = 0; i < toSave.getSize(); i++) {
             if (toSave.getItem(i) != null && toSave.getItem(i).getTypeId() > 0) {
                 getInvBackUp()[i] = toSave.getItem(i).clone();
             }
         }
     }
 
     /**
      * LOAD PLAYERCONFIG
      * 
      * @param playerName
      */
     public void loadConfig() {
         new File("plugins/AdminStuff/userdata/").mkdirs();
         Configuration config = new Configuration(new File("plugins/AdminStuff/userdata/" + this.playerName.toLowerCase() + ".yml"));
 
         config.load();
         setGod(config.getBoolean("isGod", false));
         setGlued(config.getBoolean("glue.isGlued", false));
         setAFK(config.getBoolean("isAFK", false));
         setMuted(config.getBoolean("isMuted", false));
         setBanned(config.getBoolean("isBanned", false));
         setTempBanned(config.getBoolean("isTempBanned", false));
         setBanEndTime(Long.valueOf(config.getString("banEndTime", "0")));
         setNickname(config.getString("Nickname", ""));
         setClassicMode(config.getBoolean("classicMode", false));
         setLastSeen(config.getString("lastSeen"));
 
         // LOAD INFINITE ITEMS
         List<Integer> newList = new ArrayList<Integer>();
         newList = config.getIntList("unlimited", new ArrayList<Integer>());
         for (int ItemID : newList) {
             if (ASItem.isValid(ItemID))
                 this.toggleUnlimitedItem(ItemID);
         }
        
         // LOAD GLUE
         if (isGlued()) {
             World world = ASCore.getMCServer().getWorld(config.getString("glue.Worldname", null));
             if (world != null) {
                 glueLocation = new Location(world, config.getInt("glue.X", 0), config.getInt("glue.Y", 127), config.getInt("glue.Z", 0), config.getInt("glue.Yaw", 0), config.getInt("glue.Pitch", 0));
             } else {
                 glueLocation = null;
             }
         } else {
             glueLocation = null;
         }
        
        // SET THE GAMEMODE, IF NOT EQUAL
        Player thisPlayer = ASCore.getPlayer(this.playerName);
        if(thisPlayer != null) {
            if(thisPlayer.getGameMode() != this.BooleanToGameMode(this.isClassicMode())) {
                thisPlayer.setGameMode(this.BooleanToGameMode(this.isClassicMode()));
            }
        }
    }
    
    public GameMode BooleanToGameMode(boolean inClassic) {
        return (inClassic ? GameMode.CREATIVE : GameMode.SURVIVAL);
     }
 
     /**
      * SAVE PLAYERDATA TO A FILE
      */
     public void saveConfig(boolean saveAFK, boolean saveMute, boolean saveUnlimited, boolean saveGlue, boolean saveBan, boolean saveNick, boolean saveGod, boolean saveClassic) {
         new File("plugins/AdminStuff/userdata/").mkdirs();
         Configuration config = new Configuration(new File("plugins/AdminStuff/userdata/" + this.playerName.toLowerCase() + ".yml"));
         config.load();
 
         config.setProperty("lastSeen", getLastSeen());
 
         if (saveBan) {
             config.setProperty("isBanned", isBanned);
             config.setProperty("isTempBanned", isTempBanned);
             config.setProperty("banEndTime", String.valueOf(getBanEndTime()));
         }
 
         if (saveNick) {
             config.setProperty("Nickname", nickname);
         }
 
         if (saveAFK)
             config.setProperty("isAFK", isAFK);
 
         if (saveClassic)
             config.setProperty("classicMode", classicMode);
 
         if (saveMute)
             config.setProperty("isMuted", isMuted);
 
         if (saveUnlimited) {
             List<Integer> list = new ArrayList<Integer>();
             for (int val : unlimitedList.keySet())
                 list.add(val);
             config.setProperty("unlimited", list);
         }
 
         if (saveGlue) {
             config.setProperty("glue.isGlued", isGlued);
             if (glueLocation != null) {
                 config.setProperty("glue.X", glueLocation.getBlockX());
                 config.setProperty("glue.Y", glueLocation.getBlockY());
                 config.setProperty("glue.Z", glueLocation.getBlockZ());
                 config.setProperty("glue.Pitch", glueLocation.getPitch());
                 config.setProperty("glue.Yaw", glueLocation.getYaw());
                 config.setProperty("glue.Worldname", glueLocation.getWorld().getName());
             }
         }
 
         if (saveGod) {
             config.setProperty("isGod", isGod);
         }
 
         config.save();
     }
 
     /**
      * TOGGLE UNLIMITED ITEM
      * 
      * @param TypeID
      * @return true, if added (false, if removed)
      */
     public boolean toggleUnlimitedItem(int TypeID) {
         if (!hasUnlimitedItem(TypeID)) {
             // ADD ITEM
             unlimitedList.put(TypeID, new ASItem(TypeID));
             return true;
         } else {
             // REMOVE ITEM
             unlimitedList.remove(TypeID);
             return false;
         }
     }
 
     /**
      * 
      * @param TypeID
      * @return true, if found
      */
     public boolean hasUnlimitedItem(int TypeID) {
         return unlimitedList.containsKey(TypeID);
     }
 
     /**
      * @return the isAFK
      */
     public boolean isAFK() {
         return isAFK;
     }
 
     /**
      * @param isAFK
      *            the isAFK to set
      */
     public void setAFK(boolean isAFK) {
         this.isAFK = isAFK;
     }
 
     /**
      * @return the muted
      */
     public boolean isMuted() {
         return isMuted;
     }
 
     /**
      * @param muted
      *            the muted to set
      */
     public void setMuted(boolean muted) {
         this.isMuted = muted;
     }
 
     /**
      * @return the lastSender
      */
     public String getLastSender() {
         return lastSender;
     }
 
     /**
      * @param lastSender
      *            the lastSender to set
      */
     public void setLastSender(String lastSender) {
         this.lastSender = lastSender;
     }
 
     /**
      * @return the invBackUp
      */
     public ItemStack[] getInvBackUp() {
         return invBackUp;
     }
 
     /**
      * @param invBackUp
      *            the invBackUp to set
      */
     public void setInvBackUp(ItemStack[] invBackUp) {
         this.invBackUp = invBackUp;
     }
 
     /**
      * UPDATE NICKNAME
      * 
      * @param playerName
      * @param isAFK
      */
     public void updateNick() {
         Player player = ASCore.getPlayer(playerName);
         if (player == null)
             return;
 
         String nick = ASCore.getPlayerName(player);
         if (!getNickname().equalsIgnoreCase("")) {
             nick = getNickname();
         }
         nick = nick.replace("[AFK] ", "").replace(ASLocalizer.format("SLAP_SUFFIX"), "");
         if (isAFK)
             nick = "[AFK] " + nick;
 
         if (isSlapped)
             nick = nick + ASLocalizer.format("SLAP_SUFFIX");
 
         player.setDisplayName(nick);
     }
 
     /**
      * GET UNLIMITED LIST
      * 
      * @return the unlimitedList
      */
     public Map<Integer, ASItem> getUnlimitedList() {
         return unlimitedList;
     }
 
     /**
      * SET UNLIMITED LIST
      * 
      * @param unlimitedList
      *            the unlimitedList to set
      */
     public void setUnlimitedList(Map<Integer, ASItem> unlimitedList) {
         this.unlimitedList = unlimitedList;
     }
 
     /**
      * IS GLUED
      * 
      * @return the isGlued
      */
     public boolean isGlued() {
         return isGlued;
     }
 
     /**
      * SET GLUED
      * 
      * @param isGlued
      *            the isGlued to set
      */
     public void setGlued(boolean isGlued) {
         this.isGlued = isGlued;
     }
 
     /**
      * GET GLUELOCATION
      * 
      * @return the glueLocation
      */
     public Location getGlueLocation() {
         return glueLocation;
     }
 
     /**
      * SET GLUELOCATION
      * 
      * @param glueLocation
      *            the glueLocation to set
      */
     public void setGlueLocation(Location glueLocation) {
         this.glueLocation = glueLocation;
     }
 
     /**
      * IS SLAPPED
      * 
      * @return the isSlapped
      */
     public boolean isSlapped() {
         return isSlapped;
     }
 
     /**
      * SET SLAPPED
      * 
      * @param isSlapped
      *            the isSlapped to set
      */
     public void setSlapped(boolean isSlapped) {
         this.isSlapped = isSlapped;
     }
 
     /**
      * @return the recipients
      */
     public String[] getRecipients() {
         return Recipients;
     }
 
     /**
      * @param recipients
      *            the recipients to set
      */
     public void setRecipients(String[] recipients) {
         Recipients = recipients;
     }
 
     /**
      * @return the isBanned
      */
     public boolean isBanned() {
         return isBanned;
     }
 
     /**
      * @return the isTempBanned
      */
     public boolean isTempBanned() {
         return isTempBanned;
     }
 
     /**
      * @param isTempBanned
      *            the isTempBanned to set
      */
     public void setTempBanned(boolean isTempBanned) {
         this.isTempBanned = isTempBanned;
     }
 
     /**
      * @return the banEndTime
      */
     public long getBanEndTime() {
         return banEndTime;
     }
 
     /**
      * @param banEndTime
      *            the banEndTime to set
      */
     public void setBanEndTime(long banEndTime) {
         this.banEndTime = banEndTime;
     }
 
     /**
      * @param isBanned
      *            the isBanned to set
      */
     public void setBanned(boolean isBanned) {
         this.isBanned = isBanned;
     }
 
     /**
      * @return the nickname
      */
     public String getNickname() {
         return nickname;
     }
 
     /**
      * @param nickname
      *            the nickname to set
      */
     public void setNickname(String nickname) {
         this.nickname = nickname;
     }
 
     /**
      * @return the hideChat
      */
     public boolean isHideChat() {
         return hideChat;
     }
 
     /**
      * @param hideChat
      *            the hideChat to set
      */
     public void setHideChat(boolean hideChat) {
         this.hideChat = hideChat;
     }
 
     /**
      * @return the isGod
      */
     public boolean isGod() {
         return isGod;
     }
 
     /**
      * @param isGod
      *            the isGod to set
      */
     public void setGod(boolean isGod) {
         this.isGod = isGod;
     }
 
     /**
      * @return the playerName
      */
     public String getPlayerName() {
         return playerName;
     }
 
     /**
      * @param playerName
      *            the playerName to set
      */
     public void setPlayerName(String playerName) {
         this.playerName = playerName;
     }
 
     /**
      * @return the classicMode
      */
     public boolean isClassicMode() {
         return classicMode;
     }
 
     /**
      * @param classicMode
      *            the classicMode to set
      */
     public void setClassicMode(boolean classicMode) {
         this.classicMode = classicMode;
     }
 }
