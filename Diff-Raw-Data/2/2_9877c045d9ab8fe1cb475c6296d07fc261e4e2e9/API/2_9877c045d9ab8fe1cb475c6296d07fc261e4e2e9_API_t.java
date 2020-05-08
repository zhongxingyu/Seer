 package me.limebyte.battlenight.core;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.util.BattleNightCommand;
 import me.limebyte.battlenight.core.commands.CommandManager;
 
 public class API implements BattleNightAPI {
 
     @Override
     public Battle getBattle() {
         return BattleNight.instance.battle;
     }
 
     @Override
     public boolean setBattle(Battle battle) {
        if (BattleNight.instance.battle == null || BattleNight.instance.battle.isInProgress()) return false;
         BattleNight.instance.battle = battle;
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
 
 }
