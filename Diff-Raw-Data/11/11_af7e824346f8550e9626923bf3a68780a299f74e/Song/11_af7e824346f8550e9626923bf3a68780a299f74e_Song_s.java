 package me.limebyte.battlenight.core.util.sound;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import me.limebyte.battlenight.core.BattleNight;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 
 public class Song {
     private ArrayList<Note> notes;
     private long duration;
     private HashMap<String, Integer> listeners;
 
     public Song() {
         notes = new ArrayList<Note>();
         listeners = new HashMap<String, Integer>();
         duration = 0L;
     }
 
     public void addNote(Note note) {
         this.notes.add(note);
        if (note.getTick() > duration) duration = note.getTick();
     }
 
     public void play(final Player player) {
         stop(player);
         BukkitTask task = new MusicPlayer(player).runTaskTimer(BattleNight.instance, 0L, 2L);
         listeners.put(player.getName(), task.getTaskId());
     }
 
     public void stop(Player player) {
         if (isListening(player)) {
             String name = player.getName();
             Bukkit.getServer().getScheduler().cancelTask(listeners.get(name));
             listeners.remove(name);
         }
     }
 
     public boolean isListening(Player player) {
         return listeners.containsKey(player.getName());
     }
 
     public long getDuration() {
         return duration / 10;
     }
 
     class MusicPlayer extends BukkitRunnable {
         Player player;
         long tick = 0L;
 
         public MusicPlayer(Player player) {
             this.player = player;
         }
 
         public void run() {
             if (tick > duration) {
                 stop(player);
                 return;
             }
             if ((!player.isOnline()) || (player.isDead())) {
                 stop(player);
                 return;
             }
             for (Note note : notes) {
                 if (tick == note.getTick()) note.play(player);
             }
             this.tick += 2L;
         }
 
     }
 }
