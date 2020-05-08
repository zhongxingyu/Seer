 package me.KeybordPiano459.kEssentials.players;
 
 import java.util.HashMap;
 import me.KeybordPiano459.kEssentials.kEssentials;
 
 /*
  * 
  * @author DarkSeraphim
  */
 public class kPlayerManager {
 
     kEssentials plugin;
     HashMap<String, kPlayer> players;
 
     public kPlayerManager(kEssentials plugin) {
         this.plugin = plugin;
        this.players = new HashMap<>();
     }
 
     public kPlayer createPlayer(String name) {
         kPlayer temp = new kPlayer(this.plugin, name);
         this.players.put(name, temp);
         return temp;
     }
 
     public kPlayer getPlayer(String name) {
         kPlayer kplayer = this.players.get(name);
         return (kplayer == null ? createPlayer(name) : kplayer);
     }
 
     public void removePlayer(String name) {
         this.players.remove(name);
     }
 
     public boolean playerExists(String name) {
         return this.players.containsKey(name);
     }
}
