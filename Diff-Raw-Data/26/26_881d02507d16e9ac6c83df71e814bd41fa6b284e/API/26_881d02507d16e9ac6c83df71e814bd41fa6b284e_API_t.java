 package me.limebyte.battlenight.core;
 
 import java.util.List;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Arena;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.battle.PlayerClass;
 import me.limebyte.battlenight.api.battle.Waypoint;
 import me.limebyte.battlenight.api.util.BattleNightCommand;
import me.limebyte.battlenight.api.util.PlayerData;
 import me.limebyte.battlenight.core.commands.CommandManager;
 import me.limebyte.battlenight.core.managers.ArenaManager;
 import me.limebyte.battlenight.core.util.Metadata;
 
 import org.bukkit.entity.Player;
 
 public class API implements BattleNightAPI {
 
     private Battle battle;
 
    public API() {
        PlayerData.api = this;
    }

     @Override
     public Battle getBattle() {
         return battle;
     }
 
     @Override
     public boolean setBattle(Battle battle) {
         if (this.battle != null && this.battle.isInProgress()) return false;
         this.battle = battle;
         this.battle.api = this;
         return true;
     }
 
     @Override
     public void registerCommand(BattleNightCommand command) {
         CommandManager.registerCommand(command);
 
     }
 
     @Override
     public void unregisterCommand(BattleNightCommand command) {
         CommandManager.unResgisterCommand(command);
     }
 
     @Override
     public void registerArena(Arena arena) {
         ArenaManager.register(arena);
     }
 
     @Override
     public void unregisterArena(Arena arena) {
         ArenaManager.unregister(arena);
     }
 
     @Override
     public List<Arena> getArenas() {
         return ArenaManager.getArenas();
     }
 
     @Override
     public Arena getRandomArena() {
         return ArenaManager.getRandomArena();
     }
 
     @Override
     public Waypoint getLoungeWaypoint() {
         return ArenaManager.getLounge();
     }
 
     @Override
     public Waypoint getExitWaypoint() {
         return ArenaManager.getExit();
     }
 
     @Override
     public PlayerClass getPlayerClass(Player player) {
         return Metadata.getBattleClass(player);
     }
 
     @Override
     public void setPlayerClass(Player player, PlayerClass playerClass) {
         if (playerClass != null) {
             Metadata.set(player, "class", playerClass.getName());
         } else {
             Metadata.remove(player, "class");
         }
     }
 }
