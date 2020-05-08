 package com.entrocorp.linearlogic.oneinthegun.game;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.entrocorp.linearlogic.oneinthegun.OITG;
 
 public class ArenaManager {
 
     private boolean areTimersRunning = false;
     private int timerTaskID;
 
     private ArrayList<Arena> arenas = new ArrayList<Arena>();
     private Location globalLobby;
 
     public void loadArenas() {
         File arenaDir = new File(OITG.instance.getDataFolder() + File.separator + "arenas");
         arenaDir.mkdirs();
         for (File arenaFile : arenaDir.listFiles()) {
             try {
                 ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arenaFile));
                 Arena arena = (Arena) ois.readObject();
                 arena.init();
                 arenas.add(arena);
                 ois.close();
                 OITG.instance.logInfo("Loaded arena \"" + arena.toString() + "\"");
             } catch (Exception e) {
                 OITG.instance.logSevere("Failed to load an arena from the following file: " + arenaFile.getName());
                 e.printStackTrace();
                 continue;
             }
         }
     }
 
     public void saveArenas() {
         for (Arena arena : arenas)
             arena.save();
     }
 
     public void shutdown() {
         stopTimers();
         for (Arena arena : arenas) {
             arena.clearPlayers();
             arena.populateSigns();
             arena.save();
         }
         arenas.clear();
     }
 
     public void startTimers() {
         if (areTimersRunning)
             return;
         timerTaskID = OITG.instance.getServer().getScheduler().scheduleSyncRepeatingTask(OITG.instance, new Runnable() {
             public void run() {
                 for (Arena arena : arenas)
                     arena.decrementTimer();
             }
        }, 1L, 1L);
         areTimersRunning = true;
     }
 
     public void stopTimers() {
         if (!areTimersRunning)
             return;
         OITG.instance.getServer().getScheduler().cancelTask(timerTaskID);
         areTimersRunning = false;
     }
 
     public Arena[] getArenas() {
         return arenas.toArray(new Arena[arenas.size()]);
     }
 
     public Arena[] getClosedArenas() {
         int counter = 0;
         Arena[] matches = new Arena[arenas.size()];
         for (Arena arena : arenas)
             if (arena.isClosed())
                 matches[counter++] = arena;
         return Arrays.copyOf(matches, counter);
     }
 
     public Arena[] getWaitingArenas() {
         int counter = 0;
         Arena[] matches = new Arena[arenas.size()];
         for (Arena arena : arenas)
             if (!arena.isIngame())
                 matches[counter++] = arena;
         return Arrays.copyOf(matches, counter);
     }
 
     public Arena[] getIngameArenas() {
         int counter = 0;
         Arena[] matches = new Arena[arenas.size()];
         for (Arena arena : arenas)
             if (arena.isIngame())
                 matches[counter++] = arena;
         return Arrays.copyOf(matches, counter);
     }
 
     public Arena[] getEmptyArenas() {
         int counter = 0;
         Arena[] matches = new Arena[arenas.size()];
         for (Arena arena : arenas)
             if (arena.getPlayerCount() == 0)
                 matches[counter++] = arena;
         return Arrays.copyOf(matches, counter);
     }
 
     public boolean areAnyArenasIngame() {
         for (Arena arena : arenas)
             if (arena.isIngame())
                 return true;
         return false;
     }
 
     public boolean areAllArenasEmpty() {
         for (Arena arena : arenas)
             if (arena.getPlayerCount() > 0)
                 return false;
         return true;
     }
 
     public Arena getArena(String name) {
         if (name == null)
             return null;
         for (Arena arena : arenas)
             if (arena.toString().equalsIgnoreCase(name))
                 return arena;
         return null;
     }
 
     public Arena getArena(Location signLoc) {
         if (signLoc == null)
             return null;
         for (Arena arena : arenas)
             if (arena.isSignLocation(signLoc))
                 return arena;
         return null;
     }
 
     public Arena getArena(Player player) {
         if (player == null)
             return null;
         for (Arena arena : arenas)
             if (arena.containsPlayer(player))
                 return arena;
         return null;
     }
 
     public boolean addArena(Arena arena) {
         if (arena == null || getArena(arena.toString()) != null)
             return false;
         arenas.add(arena);
         return true;
     }
 
     public boolean deleteArena(Arena arena) {
         if (arena == null || !arenas.contains(arena))
             return false;
         arenas.remove(arena);
         arena.delete();
         return true;
     }
 
     public void deleteArenas() {
         for (Arena arena : arenas)
             arena.delete();
         arenas.clear();
     }
 
     public void updateArenaSigns() {
         for (Arena arena : arenas)
             arena.populateSigns();
     }
 
     public Location getGlobalLobby() {
         return globalLobby;
     }
 
     public void setGlobalLobby(Location loc, boolean updateConfig) {
         globalLobby = loc;
         if (updateConfig) {
             OITG.instance.getConfig().set("global-lobby.world", loc.getWorld().getName());
             OITG.instance.getConfig().set("global-lobby.x", loc.getX());
             OITG.instance.getConfig().set("global-lobby.y", loc.getY());
             OITG.instance.getConfig().set("global-lobby.z", loc.getZ());
             OITG.instance.getConfig().set("global-lobby.yaw", loc.getYaw());
             OITG.instance.getConfig().set("global-lobby.pitch", loc.getPitch());
             OITG.instance.saveConfig();
         }
     }
 }
