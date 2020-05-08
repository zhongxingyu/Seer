 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of MinestarCore.
  * 
  * MinestarCore is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * MinestarCore is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MinestarCore.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.core.units;
 
 import java.io.File;
 
 import org.anjocaido.groupmanager.data.Group;
 import org.anjocaido.groupmanager.data.User;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.bukkit.gemo.utils.UtilPermissions;
 
 import de.minestar.core.MinestarCore;
 import de.minestar.minestarlibrary.data.Data;
 import de.minestar.minestarlibrary.data.DataType;
 import de.minestar.minestarlibrary.data.GenericValue;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 
 public class MinestarPlayer {
     private boolean online = false;
     private final String playerName;
     private String group;
     private MinestarGroup msGroup;
     private Data data;
 
     public MinestarPlayer(String playerName) {
         this.playerName = playerName;
         if (Bukkit.getPlayer(playerName) != null)
             this.setOnline();
         else
             this.setOffline();
         this.load();
     }
 
     public MinestarPlayer(Player player) {
         this(player.getName());
         this.setOnline();
         this.updateBukkitPlayer();
     }
 
     public void setOnline() {
         this.online = true;
     }
 
     public void setOffline() {
         this.online = false;
     }
 
     public boolean isOnline() {
         return this.online;
     }
 
     public boolean isOffline() {
         return !this.isOnline();
     }
 
     public void load() {
         // UPDATE THE PLAYER
         this.updateGroup();
 
         // INIT DATA
        this.data = new Data(new File("plugins/MinestarCore", "playerdata"), playerName, DataType.NBT);
 
         // LOAD DATA FROM FILE
         this.data.load();
 
         if (this.getString("general.nickName") == null)
             this.setString("general.nickName", this.playerName);
         if (this.getString("general.listName") == null)
             this.setString("general.listName", this.playerName);
     }
 
     public String getPlayerName() {
         return playerName;
     }
 
     public String getNickName() {
         return this.getString("general.nickName");
     }
 
     public void setNickName(String nickName) {
         this.setString("general.nickName", nickName);
         this.updateBukkitPlayer();
     }
 
     public String getListName() {
         return this.getString("general.listName");
     }
 
     public void setListName(String listName) {
         this.setString("general.listName", listName);
         this.updateBukkitPlayer();
     }
 
     public String updateGroup() {
         // GET THE PLAYER
         Player player = PlayerUtils.getOnlinePlayer(this.playerName);
         if (player != null) {
             // GET GROUP WITH UTILPERMISSIONS
             this.group = UtilPermissions.getGroupName(player);
         } else {
             this.group = MinestarGroup.DEFAULT.getName();
             // GET GROUP FROM GROUPMANAGER
             if (MinestarCore.groupManager != null) {
                 User user = MinestarCore.groupManager.getWorldsHolder().getWorldData("world").getUser(playerName);
                 this.group = user.getGroupName();
             }
         }
         this.msGroup = MinestarGroup.getGroup(this.group);
         return getGroup();
     }
 
     public String getGroup() {
         return this.group;
     }
 
     public MinestarGroup getMinestarGroup() {
         return this.msGroup;
     }
 
     public MinestarGroup setGroup(MinestarGroup group) {
         this.setGroup(group.getName());
         return this.msGroup;
     }
 
     public String setGroup(String groupName) {
         // GET GROUP FROM GROUPMANAGER
         if (MinestarCore.groupManager != null) {
             // update "world"
             User user = MinestarCore.groupManager.getWorldsHolder().getWorldData("world").getUser(playerName);
             Group group = MinestarCore.groupManager.getWorldsHolder().getWorldData("world").getGroup(groupName);
             user.setGroup(group);
 
             // update "goldgrube"
             user = MinestarCore.groupManager.getWorldsHolder().getWorldData("goldgrube").getUser(playerName);
             group = MinestarCore.groupManager.getWorldsHolder().getWorldData("goldgrube").getGroup(groupName);
             user.setGroup(group);
 
             MinestarCore.groupManager.getWorldsHolder().saveChanges();
         } else {
             throw new RuntimeException("Cannot change group: GroupManager not found!");
         }
         // UPDATE GROUP
         this.updateGroup();
         return this.getGroup();
     }
 
     public boolean isInGroup(String groupName) {
         return this.group.equalsIgnoreCase(groupName);
     }
 
     /**
      * Update the bukkitplayer
      */
     public void updateBukkitPlayer() {
         // ONLY UPDATE, IF ONLINE
         if (this.isOffline())
             return;
 
         // GET THE PLAYER
         Player player = Bukkit.getPlayer(this.playerName);
         if (player == null)
             return;
 
         player.setDisplayName(this.getNickName());
         player.setPlayerListName(this.getListName());
     }
 
     /**
      * Get the BukkitPlayer
      * 
      * @return the Bukkit-Player
      */
     public Player getBukkitPlayer() {
         // PLAYER MUST BE ONLINE
         if (this.isOffline())
             return null;
 
         // GET THE PLAYER
         Player player = Bukkit.getPlayer(this.playerName);
         if (player == null)
             return null;
 
         return player;
     }
 
     public void saveData() {
         this.data.save();
     }
 
     /**
      * Sets a value
      * 
      * @param key
      * @param value
      */
     public void setValue(String key, Object value) {
         this.data.setValue(key, value);
     }
 
     public void removeValue(String key, Class<?> clazz) {
         this.data.removeValue(key, clazz);
     }
 
     public void setBoolean(String key, boolean value) {
         this.setValue(key, value);
     }
 
     public void setByte(String key, byte value) {
         this.setValue(key, value);
     }
 
     public void setByteArray(String key, byte[] value) {
         this.setValue(key, value);
     }
 
     public void setDouble(String key, double value) {
         this.setValue(key, value);
     }
 
     public void setFloat(String key, float value) {
         this.setValue(key, value);
     }
 
     public void setInteger(String key, int value) {
         this.setValue(key, value);
     }
 
     public void setLong(String key, long value) {
         this.setValue(key, value);
     }
 
     public void setShort(String key, short value) {
         this.setValue(key, value);
     }
 
     public void setString(String key, String value) {
         this.setValue(key, value);
     }
 
     public void setLocation(String key, Location value) {
         this.setValue(key, value);
     }
 
 //    public void setItemStack(String key, ItemStack value) {
 //        this.setValue(key, value);
 //    }
 //
 //    public void setItemStackArray(String key, ItemStack[] value) {
 //        this.setValue(key, value);
 //    }
 
     /**
      * Returns a value of the specified type
      * 
      * @param key
      * @param clazz
      * @return
      */
     public <T> GenericValue<T> getValue(String key, Class<T> clazz) {
         return this.data.getValue(key, clazz);
     }
 
     public Boolean getBoolean(String key) {
         return this.data.getBoolean(key);
     }
 
     public Byte getByte(String key) {
         return this.data.getByte(key);
     }
 
     public Byte[] getByteArray(String key) {
         return this.data.getByteArray(key);
     }
 
     public Double getDouble(String key) {
         return this.data.getDouble(key);
     }
 
     public Float getFloat(String key) {
         return this.data.getFloat(key);
     }
 
     public Integer getInteger(String key) {
         return this.data.getInteger(key);
     }
 
 //    public ItemStack getItemStack(String key) {
 //       return this.data.getItemStack(key);
 //    }
 //
 //    public ItemStack[] getItemStackArray(String key) {
 //        return this.data.getItemStackArray(key);
 //    }
 
     public Location getLocation(String key) {
         return this.data.getLocation(key);
     }
 
     public Long getLong(String key) {
         return this.data.getLong(key);
     }
 
     public Short getShort(String key) {
         return this.data.getShort(key);
     }
 
     public String getString(String key) {
         return this.data.getString(key);
     }
 }
