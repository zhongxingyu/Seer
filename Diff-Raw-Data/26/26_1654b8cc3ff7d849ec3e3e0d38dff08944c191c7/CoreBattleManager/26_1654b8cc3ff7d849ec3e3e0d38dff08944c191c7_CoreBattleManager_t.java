 package me.limebyte.battlenight.core.managers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.managers.BattleManager;
 import me.limebyte.battlenight.core.battle.battles.FFABattle;
 import me.limebyte.battlenight.core.battle.battles.TDMBattle;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 
 public class CoreBattleManager implements BattleManager {
 
     private String activeBattle;
     private Map<String, Battle> battles = new HashMap<String, Battle>();
 
    public CoreBattleManager() {
         String battle = getSetBattle();
         int time = getDuration();
         int minPlayers = getMinPlayers();
         int maxPlayers = getMaxPlayers();
 
         register(new TDMBattle(time, minPlayers, maxPlayers), "TDM");
         register(new FFABattle(time, minPlayers, maxPlayers), "FFA");
 
         if (getBattle(battle) == null) {
             battle = "TDM";
         }
         setActiveBattle(battle);
     }
 
     @Override
     public boolean deregister(String id) {
         if (activeBattle.equals(id)) throw new IllegalStateException();
         if (!battles.containsKey(id)) return false;
         battles.remove(id);
         return true;
     }
 
     @Override
     public Battle getActiveBattle() {
         return getBattle(activeBattle);
     }
 
     @Override
     public Battle getBattle(String id) {
         return battles.get(id);
     }
 
     @Override
     public List<Battle> getBattles() {
         return new ArrayList<Battle>(battles.values());
     }
 
     @Override
     public void register(Battle battle, String id) {
         if (battle == null || battles.containsKey(id)) throw new IllegalArgumentException();
         battles.put(id, battle);
     }
 
     @Override
     public void reloadBattles() {
         String battle = getSetBattle();
         if (getBattle(battle) == null) {
             battle = "TDM";
         }
         setActiveBattle(battle);
 
         int time = getDuration();
         int minPlayers = getMinPlayers();
         int maxPlayers = getMaxPlayers();
 
         for (Battle b : battles.values()) {
             b.getTimer().setTime(time);
             b.setMinPlayers(minPlayers);
             b.setMaxPlayers(maxPlayers);
         }
     }
 
     @Override
     public boolean setActiveBattle(String id) {
         if (!battles.containsKey(id)) return false;
 
         Battle active = getActiveBattle();
         if (active != null && active.isInProgress()) throw new IllegalStateException();
 
         activeBattle = id;
         return true;
     }
 
     private int getDuration() {
         return ConfigManager.get(Config.MAIN).getInt("Battle.Duration", 300);
     }
 
     private int getMaxPlayers() {
         int maxPlayers = ConfigManager.get(Config.MAIN).getInt("Battle.MaxPlayers", 0);
         int minPlayers = getMinPlayers();
         if (maxPlayers == 0) {
             maxPlayers = Integer.MAX_VALUE;
         }
         return maxPlayers >= minPlayers ? maxPlayers : minPlayers;
     }
 
     private int getMinPlayers() {
         int minPlayers = ConfigManager.get(Config.MAIN).getInt("Battle.MinPlayers", 2);
         return minPlayers >= 2 ? minPlayers : 2;
     }
 
     private String getSetBattle() {
         return ConfigManager.get(Config.MAIN).getString("Battle.Type", "TDM");
     }
 }
