 /*
  * Copyright (c) 2013 cedeel.
  * All rights reserved.
  * 
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * The name of the author may not be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package be.darnell.timetracker;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 public class TimeTracker extends JavaPlugin {
 
     protected Map<String, Long> players;
 
     // File storage
     private YamlConfiguration Data = null;
     private File DataFile = null;
     private static final String DATAFILENAME = "Data.yml";
 
     // Join message
     private String joinMsg;
     private String colour;
 
     // Date formatting
     private boolean alwaysDate = false;
     private int daysBeforeDate = 30;
 
     protected static String humanTime(long start, long end) {
         if (start != -1L) {
             long finaltime = (end - start) / 1000L;
             long MINUTE = 60L;
             long HOUR = 60*MINUTE;
             long DAY = 24*HOUR;
 
             if (finaltime >= DAY) {
                 String s = (finaltime >= (2*DAY)) ? "days" : "day";
                 return (finaltime / DAY + " " + s);
             } else if (finaltime >= HOUR) {
                 String s = (finaltime >= (2*HOUR)) ? "hours" : "hour";
                 return (finaltime / HOUR + " " + s);
             } else if (finaltime >= MINUTE) {
                 String s = (finaltime >= (2*MINUTE)) ? "minutes" : "minute";
                 return (finaltime / MINUTE + " " + s);
             } else {
                 String s = (finaltime >= 2) ? "seconds" : "second";
                 return (finaltime + " " + s);
             }
         }
         return null;
     }
 
    private static String dateTime(long start) {
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
        Date date = new Date(start);
 
         return df.format(date).toString();
     }
 
     protected String sinceString(long start, long end) {
        long daysSince = (end -start) / 86400000L;
 
         if(daysSince > daysBeforeDate || alwaysDate)
            return dateTime(start) + " (" + humanTime(start, end) + " ago)";
         else return humanTime(start, end) + " ago";
     }
 
     @Override
     public void onDisable() {
         for (String p : players.keySet()) {
             removePlayer(p);
         }
         saveData();
         System.out.println(this + " is now disabled!");
     }
 
     @Override
     public void onEnable() {
         PluginManager pluginManager = this.getServer().getPluginManager();
         pluginManager.registerEvents(new TimeTrackerPlayerListener(this), this);
         saveDefaultConfig();
         saveData();
         players = new HashMap<String, Long>();
 
         this.getCommand("seen").setExecutor(new SeenCommand(this));
         this.getCommand("playtime").setExecutor(new PlaytimeCommand(this));
 
         joinMsg = getConfig().getString("JoinMessage");
         for (Player p : getServer().getOnlinePlayers()) {
             addPlayer(p.getName());
         }
 
         colour = ChatColor.translateAlternateColorCodes('&', getConfig().getString("MessageColour", "&e"));
 
         alwaysDate = getConfig().getBoolean("AlwaysDate", false);
         daysBeforeDate = getConfig().getInt("DaysBeforeDate", 30);
 
         System.out.println(this + " is now enabled.");
     }
 
     /**
      * Get the time in milliseconds of the first join of a given player
      *
      * @param name The player to get the data from
      * @return The time in milliseconds of the first join
      */
     public long getFirstSeen(String name) {
         return getData().getLong(name.toLowerCase() + ".first", -1L);
     }
 
     /**
      * Get the time in milliseconds of the last join of a given player
      *
      * @param name The player to get the data from
      * @return The time in milliseconds of the last join
      */
     public long getLastSeen(String name) {
         return getData().getLong(name.toLowerCase() + ".last", -1L);
     }
 
     /**
      * Get the time a player has spent on the server
      *
      * @param name The player to get the data from
      * @return The time, in milliseconds, the player has spent on the server
      */
     public long getPlayTime(String name) {
         return getData().getLong(name.toLowerCase() + ".playtime", -1L);
     }
 
     /**
      * Add a specific amount of time to the player's file
      *
      * @param name  The player to add the time to
      * @param value The amount of time, in milliseconds, to add
      */
     protected void addPlayTime(String name, long value) {
         getData().set(name.toLowerCase() + ".playtime", Long.valueOf(getPlayTime(name) + value));
         saveData();
     }
 
     /**
      * Set the time a player was first seen
      *
      * @param name  The name of the player
      * @param value The time to be set, in milliseconds
      */
     protected void setFirstSeen(String name, long value) {
         getData().set(name.toLowerCase() + ".first", Long.valueOf(value));
         saveData();
     }
 
     /**
      * Set the time a player was last seen
      *
      * @param name  The name of the player
      * @param value The time to be set, in milliseconds
      */
     protected void setLastSeen(String name, long value) {
         getData().set(name.toLowerCase() + ".last", Long.valueOf(value));
         saveData();
     }
 
     /** Reload the data file */
     private void reloadData() {
         if (DataFile == null)
             DataFile = new File(getDataFolder(), DATAFILENAME);
         Data = YamlConfiguration.loadConfiguration(DataFile);
 
         InputStream defData = this.getResource(DATAFILENAME);
         if (defData != null) {
             YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defData);
             Data.setDefaults(defConfig);
         }
     }
 
     /**
      * Remove a player from the list in memory
      *
      * @param name The player to remove
      */
     protected void removePlayer(String name) {
         long time = (new Date()).getTime();
         setLastSeen(name, time);
         addPlayTime(name, time - players.get(name));
         players.remove(name);
     }
 
     /**
      * Add a player to the list in memory
      *
      * @param name The player to add
      */
     protected void addPlayer(String name) {
         long last = getLastSeen(name);
         long first = getFirstSeen(name);
         long ex = (new Date()).getTime();
         players.put(name, ex);
         if (last == -1L || first == -1L) {
             setFirstSeen(name, ex);
             getServer().broadcastMessage(colour + joinMsg.replace("%p", name));
         }
     }
 
     /**
      * Get the data from file
      *
      * @return The player data
      */
     private YamlConfiguration getData() {
         if (Data == null)
             this.reloadData();
         return Data;
     }
 
     private void saveData() {
         if (Data == null || DataFile == null)
             return;
         try {
             getData().save(DataFile);
         } catch (IOException ex) {
             this.getLogger().log(Level.SEVERE, "Could not save config to " + DataFile, ex);
         }
     }
 }
