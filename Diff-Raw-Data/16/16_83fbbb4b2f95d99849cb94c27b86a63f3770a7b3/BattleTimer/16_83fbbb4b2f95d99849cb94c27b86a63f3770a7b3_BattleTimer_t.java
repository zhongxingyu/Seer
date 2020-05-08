 package me.limebyte.battlenight.core.util;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.core.battle.SimpleBattle;
 import me.limebyte.battlenight.core.managers.CoreMusicManager;
 import me.limebyte.battlenight.core.tosort.Note;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Sound;
 
 public class BattleTimer extends SimpleTimer {
 
     private BattleNightAPI api;
     private SimpleBattle battle;
     private static final int MINUTE = 60;
     private static final int COUNTDOWN_START = 10;
 
     public BattleTimer(BattleNightAPI api, SimpleBattle battle, long time) {
         super(time);
         this.api = api;
         this.battle = battle;
     }
 
     @Override
     public void onTimeChange(long time) {
         Messenger messenger = api.getMessenger();
 
         if (time == CoreMusicManager.battleEnd.length()) {
             messenger.playSong(CoreMusicManager.battleEnd);
         }
 
         if (time % 10 == 0) {
             long timeSec = time / 10;
             battle.getScoreboard().updateTime(timeSec);
 
             if (timeSec <= COUNTDOWN_START) {
                 messenger.playSound(Sound.NOTE_PIANO, Note.convertPitch(18));
                 messenger.tellEveryone("" + ChatColor.RED + timeSec + "!");
             } else if (timeSec == MINUTE) {
                messenger.tellEveryone(ChatColor.RED + "The time is almost up!  Keep fighting!");
             }
         }
     }
 
     @Override
     public void onTimerEnd() {
         battle.stop();
     }
 
 }
