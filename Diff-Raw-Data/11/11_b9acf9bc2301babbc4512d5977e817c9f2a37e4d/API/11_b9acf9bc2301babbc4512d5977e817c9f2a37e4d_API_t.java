 package me.limebyte.battlenight.core;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.commands.BattleNightCommand;
 import me.limebyte.battlenight.api.managers.ArenaManager;
 import me.limebyte.battlenight.api.managers.BattleManager;
 import me.limebyte.battlenight.api.managers.ClassManager;
 import me.limebyte.battlenight.api.managers.MusicManager;
 import me.limebyte.battlenight.api.managers.SpectatorManager;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.api.util.PlayerClass;
 import me.limebyte.battlenight.core.commands.CommandManager;
 import me.limebyte.battlenight.core.listeners.SignListener;
 import me.limebyte.battlenight.core.managers.CoreArenaManager;
 import me.limebyte.battlenight.core.managers.CoreBattleManager;
 import me.limebyte.battlenight.core.managers.CoreClassManager;
 import me.limebyte.battlenight.core.managers.CoreMusicManager;
 import me.limebyte.battlenight.core.managers.CoreSpectatorManager;
 import me.limebyte.battlenight.core.tosort.Metadata;
 import me.limebyte.battlenight.core.tosort.PlayerData;
 import me.limebyte.battlenight.core.util.SimpleMessenger;
 
 import org.bukkit.entity.Player;
 
 public class API implements BattleNightAPI {
 
    private Messenger messenger;

     // Managers
     private ArenaManager arenaManager;
     private BattleManager battleManager;
     private ClassManager classManager;
     private MusicManager musicManager;
     private SpectatorManager spectatorManager;
 
     public API(BattleNight plugin) {
        messenger = new SimpleMessenger(this);

         // Managers
         arenaManager = new CoreArenaManager(this);
         battleManager = new CoreBattleManager(this);
         classManager = new CoreClassManager(this);
         musicManager = new CoreMusicManager(this, plugin);
         spectatorManager = new CoreSpectatorManager(this);
 
         PlayerData.api = this;
     }
 
     @Override
     public ArenaManager getArenaManager() {
         return arenaManager;
     }
 
     @Override
     public Battle getBattle() {
         return battleManager.getActiveBattle();
     }
 
     @Override
     public BattleManager getBattleManager() {
         return battleManager;
     }
 
     @Override
     public ClassManager getClassManager() {
         return classManager;
     }
 
     @Override
     public Messenger getMessenger() {
         return messenger;
     }
 
     @Override
     public MusicManager getMusicManager() {
         return musicManager;
     }
 
     @Override
     public PlayerClass getPlayerClass(Player player) {
         return Metadata.getPlayerClass(player);
     }
 
     @Override
     public SpectatorManager getSpectatorManager() {
         return spectatorManager;
     }
 
     @Override
     public void registerCommand(BattleNightCommand command) {
         CommandManager.registerCommand(command);
 
     }
 
     @Override
     public void setPlayerClass(Player player, PlayerClass playerClass) {
         if (playerClass != null) {
             Metadata.set(player, "class", playerClass.getName());
             playerClass.equip(player);
         } else {
             Metadata.remove(player, "class");
             SignListener.cleanSigns(player);
         }
     }
 
     @Override
     public void unregisterCommand(BattleNightCommand command) {
         CommandManager.unResgisterCommand(command);
     }
 }
